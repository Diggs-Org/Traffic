# Autonomous Ticket Worker

Handles an assigned Jira ticket end-to-end. Project values (`JIRA_PROJECT_KEY`,
`GITHUB_REVIEWER`, `ATLASSIAN_URL`, etc.) are available as environment variables.

---

## Triggers

The skill uses **intent detection**, not exact phrases. On entry, run the state-detection
algorithm below to determine which phase to execute regardless of how the user worded
their request.

### State-Detection Algorithm

Run these checks in order and execute the first matching phase:

```
1. Does the message contain a Jira ticket key (e.g. PROJ-123)?
   YES → extract the key; call it KEY
   NO  → ask the user "Which ticket should I work on?"

2. Does a remote branch whose name starts with KEY exist?
   NO  → run Phase 1 (Plan)
   YES → fetch the open PR for that branch

3. Is there an open PR for the KEY branch?
   NO  → run Phase 1 (branch exists but no PR — re-create the draft PR from PLAN.md)

4. Is the PR a DRAFT?
   YES, and latest review is APPROVED  → run Phase 2 (Implement)
   YES, no approving review yet        → tell user "Plan is awaiting review" and stop

5. PR is NOT a draft:
   Latest review is CHANGES_REQUESTED → run Phase 3 (Address Comments)
   Latest review is APPROVED          → run Phase 4 (Merge)
   No reviews yet                     → tell user "PR is awaiting review" and stop
```

**Common trigger phrases that all map to this algorithm** (not exhaustive):
- "You have been assigned ticket KEY" → Phase 1 (no branch yet)
- "Complete ticket KEY" / "Do ticket KEY" / "Work on KEY" → Phase 1 (no branch yet)
- "A PR review has been performed" / "Review is done" / "I left a review" → Phase 2/3/4
- "Continue working on KEY" → picks up wherever state-detection lands

To check branch and PR state:

```bash
git ls-remote --heads origin "KEY*"   # check if branch exists remotely
# then use mcp__github__pull_request_read to check PR draft status and review state
```

---

## Phase 1: New Ticket — Create Plan

**When:** State-detection finds no remote branch prefixed with the ticket key.

1. Fetch the ticket via `jira_get_issue` with fields:
   `assignee,issuetype,updated,summary,reporter,description,created,labels,priority,status,customfield_10072`
   Read the summary, description, and acceptance criteria (`customfield_10072` — this field
   is omitted by default and must be requested explicitly).

2. **Explore the codebase** to understand relevant areas. Use 2–3 parallel Explore sub-agents to investigate different subsystems in parallel (this is the highest-value use of sub-agents in this workflow):
   - One agent: find existing code related to the ticket's domain
   - One agent: identify test patterns and how existing tests are structured
   - One agent: check for related utilities, schemas, or config that would be touched

3. **Create `PLAN.md`** with the following structure:

   ```markdown
   # Plan: [TICKET-KEY]: [Summary]

   ## What & Why

   [One paragraph: what needs to change and why, in terms of the acceptance criteria]

   ## Approach

   [Numbered steps describing the implementation. Be specific: file paths, function names,
   schema changes, API changes. Include sub-agent strategy if the implementation is large.]

   ## Files to Change

   - `path/to/file.py` — what changes and why
   - (list all files that will be touched)

   ## Acceptance Criteria Checklist

   - [ ] [Each criterion from customfield_10072, reworded as a verifiable statement]

   ## Out of Scope

   [Anything explicitly NOT being done in this ticket]
   ```

4. Create the implementation branch via `mcp__github__create_branch`:
   - Branch name: `TICKET-KEY/short-description` (Jira key MUST be the prefix)
   - The PostToolUse hook will automatically transition Jira → In Progress and post a comment

5. Commit `PLAN.md` to the branch and push.

6. Open a **draft PR** via `mcp__github__create_pull_request`:
   - Title: `[PLAN] TICKET-KEY: Summary`
   - Body: use `.github/pull_request_template.md` with a note at the top: "**This is a planning draft. Review PLAN.md and leave an Approving review to begin implementation.**"
   - Set `draft: true`
   - Set `base: main` (all PRs must target the `main` branch)
   - Note: the `in_review` Jira hook fires on PR creation — for draft PRs this is slightly premature,
     but keeps Jira status visible. Implementation won't start until the plan is approved.

7. Post a Jira comment summarizing the plan and linking to the draft PR.

8. Tell the user: "Plan is ready for review: [PR URL]. Leave an Approving review on the draft PR on GitHub, then come back and say **'A PR review has been performed'**."

---

## Phase 2: Plan Approved — Implement

**When:** State-detection finds a draft PR with an approving review (and no subsequent changes-requested review).

1. Check out the plan branch (it already exists).

2. Re-read `PLAN.md` and the acceptance criteria from Jira.

3. **Implement** the changes described in PLAN.md, committing as you go with descriptive
   commit messages. After each logical chunk, verify with `git status` and `git diff --cached`.
   Do NOT remove `PLAN.md` — it stays as implementation context.

   Before proceeding to step 4, verify everything is committed and pushed:

   ```bash
   git status          # must show "nothing to commit, working tree clean"
   git push origin <branch>
   git status          # must show "Your branch is up to date with 'origin/<branch>'"
   ```

   Do not proceed if there are uncommitted changes or unpushed commits.

4. For each criterion in the `customfield_10072` acceptance criteria field fetched in Phase 1,
   verify it is satisfied by the committed changes. If any criterion is unmet, implement the
   missing changes, commit, push, and re-verify. Only proceed once every criterion is satisfied.

5. **Convert the draft PR to non-draft**:

   ```bash
   gh pr ready <pr-number>
   ```

   If using `gh pr ready`, manually transition Jira → In Review (the hook only fires on
   `mcp__github__create_pull_request`, not on `gh pr ready`).

6. Tell the user: "Implementation complete. PR is ready for code review: [PR URL]. After the review is submitted, come back and say **'A PR review has been performed'**."

---

## Phase 3: Review Requested — Address Comments

**When:** State-detection finds a non-draft PR whose latest review is `CHANGES_REQUESTED`.

1. Read the PR via `mcp__github__pull_request_read` — the PostToolUse hook will inject all
   review comments, inline comments, and PR-level comments into context automatically.

2. For each review comment:
   - Understand what is being asked
   - Implement the change
   - If a comment is unclear, make your best interpretation and note it in a reply comment

3. Push all changes to the branch.

4. Re-request review from `${GITHUB_REVIEWER}`:

   ```bash
   gh pr edit <number> --add-reviewer ${GITHUB_REVIEWER}
   ```

5. Tell the user: "All review comments addressed. Re-requested review from @${GITHUB_REVIEWER}. Come back and say **'A PR review has been performed'** when the reviewer responds."

---

## Phase 4: PR Approved — Merge

**When:** State-detection finds a non-draft PR whose latest review is `APPROVED` with no subsequent `CHANGES_REQUESTED` review.

1. Squash merge via `mcp__github__merge_pull_request`:
   - Merge method: `squash`
   - Commit message: PR title and body (from the pull request template)
   - The PostToolUse hook transitions Jira → Done automatically

2. Tell the user: "PR merged. Jira ticket closed. Work complete."

---

## Important Notes

- **Branch naming is critical**: all hooks extract the Jira key using `[A-Z]+-[0-9]+` regex.
  The branch MUST start with the Jira key (e.g. `PROJ-123/short-description`).
- **Never force-push** to a branch with an open PR.
- **Sub-agents**: use 2–3 parallel Explore sub-agents during Phase 1 codebase exploration only.
  Implementation (Phase 2) and review response (Phase 3) should be single linear sessions.

---

## Hooks That Fire Automatically

These PostToolUse hooks run without any action required:

| Event        | Trigger                            | Script                           | Effect                                                  |
| ------------ | ---------------------------------- | -------------------------------- | ------------------------------------------------------- |
| PreToolUse   | `Bash` (`git commit`)              | `lint-before-commit.sh`          | Advisory ruff + eslint report                           |
| PostToolUse  | `mcp__github__create_branch`       | `jira-transition.sh in_progress` | Jira → In Progress                                      |
| PostToolUse  | `mcp__github__create_branch`       | `jira-link-branch.sh`            | Posts comment + remote link on Jira issue               |
| PostToolUse  | `mcp__github__create_pull_request` | `jira-transition.sh in_review`   | Jira → In Review                                        |
| PostToolUse  | `mcp__github__create_pull_request` | `post-pr-coverage.sh`            | Posts coverage report as PR comment                     |
| PostToolUse  | `mcp__github__pull_request_read`   | `fetch-pr-comments.sh`           | Injects all PR comments into context                    |
| PostToolUse  | `mcp__github__merge_pull_request`  | `jira-transition.sh done`        | Jira → Done                                             |
| Stop         | session end                        | `session-summary.sh`             | Appends diff summary to `.claude/session-summaries.log` |
| Notification | any                                | `notify.sh`                      | Desktop notify + `.claude/notifications.log`            |

If Jira transitions don't fire, verify transition names match your board:

```bash
curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" \
  "$ATLASSIAN_URL/rest/api/3/issue/${JIRA_PROJECT_KEY}-1/transitions" | python3 -m json.tool
```

Adjust the `PATTERN` strings in `.claude/hooks/jira-transition.sh`.
