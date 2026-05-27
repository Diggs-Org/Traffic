# Plan: CLOD-19: Complete the neat-evolution module

## What & Why

The `neat-evolution` module contains eight strategy/entity interfaces
(`Population`, `Species`, `EvolutionEngine`, `SpeciationStrategy`,
`SelectionStrategy`, `CrossoverStrategy`, `MutationStrategy`,
`FitnessEvaluator`) but has **zero concrete implementations and no tests**.
This ticket completes the module by adding seven concrete implementation
classes and a comprehensive TestNG test suite (JaCoCo + Surefire) following
the same TDD pattern established in CLOD-18: failing tests are committed on
this planning PR, and Phase 2 provides the implementations that make them pass.

---

## Approach

This ticket uses a **Test-Driven Development** workflow:

- **Phase 1 (this PR)** — update `pom.xml`, write skeleton classes (all methods
  throw `UnsupportedOperationException`), commit a comprehensive failing test suite.
- **Phase 2 (next PR)** — replace stubs with real logic; all tests pass; JaCoCo
  line coverage ≥ 80 %.

### Implementation Steps (Phase 2 detail)

1. **`SpeciesImpl`** — immutable value object. Constructor validates: non-null
   representative, non-empty non-null member list, non-negative counts. Members are
   defensively copied into `Collections.unmodifiableList`.

2. **`PopulationImpl`** — partially mutable to support in-place speciation.
   Stores genomes as an unmodifiable list; stores species in a mutable internal
   list exposed as `unmodifiableList` via `getSpecies()`. Package-private
   `setSpecies(List<Species>)` lets `DefaultSpeciationStrategy` update species.
   Package-private `setChampion(Genome)` lets `StandardEvolutionEngine` record the
   champion after fitness evaluation. `getChampion()` returns `null` before
   evaluation (documented as undefined before fitness evaluation).

3. **`DefaultSpeciationStrategy`** — constructor takes `c1, c2, c3`
   (compatibility-distance coefficients) and `compatibilityThreshold`.
   `speciate(population)` casts the `Population` to `PopulationImpl` and:
   a. Clears current members (keeping previous-gen representatives).
   b. For each genome in population: computes `compatibilityDistance` vs. each
      existing species representative; joins the first species within threshold
      or creates a new species (next auto-incremented ID).
   c. Removes empty species.
   d. Builds new `SpeciesImpl` objects with `sharedFitnessSum = 0`,
      `bestFitness = 0`, `generationsSinceImprovement = 0` (fitness fields are
      populated later by `StandardEvolutionEngine`).
   e. Calls `((PopulationImpl) population).setSpecies(newSpecies)`.

4. **`TournamentSelectionStrategy`** — constructor takes `tournamentSize k` and a
   `Random`. `select(species, count)` runs `count` independent k-tournaments
   over the species member list; returns an unmodifiable list of size `count`;
   allows duplicates if `count > |members|`.

5. **`StandardCrossoverStrategy`** — constructor takes a `Random`.
   `crossover(parent1, parent2)`: parent1 is the fitter parent by convention.
   Aligns genes by innovation number. Matching genes are inherited from either
   parent with equal probability. Excess and disjoint genes always come from
   `parent1`. Node gene list is copied from `parent1`. Returns a new `GenomeImpl`.

6. **`StandardMutationStrategy`** — constructor takes five probabilities
   (`weightMutationRate`, `addConnectionRate`, `addNodeRate`,
   `toggleConnectionRate`, `perturbStdDev`) and a `Random`. Each operator is
   applied independently at its configured rate. Structural mutations use the
   `InnovationTracker` for consistent innovation numbers. Returns a new
   `GenomeImpl`; the input genome is never modified.

7. **`StandardEvolutionEngine`** — constructor takes all five strategies plus
   `innovationTracker`, `crossoverRate`, and `elitismThreshold`. `nextGeneration`:
   a. Evaluate every genome via `FitnessEvaluator` → `Map<Genome, Double> fitnessMap`.
   b. Find champion (argmax of fitnessMap).
   c. Call `speciationStrategy.speciate(population)` (assigns genomes to species).
   d. Update each species with `sharedFitnessSum`, `bestFitness`, and
      `generationsSinceImprovement` derived from `fitnessMap` and the previous
      generation's species records (keyed by species ID).
   e. Compute offspring quota per species proportional to `sharedFitnessSum`.
   f. Preserve champion of each species ≥ `elitismThreshold` unchanged.
   g. For remaining slots: select parents via `SelectionStrategy`, apply crossover
      at `crossoverRate` (else asexual reproduction), then mutate.
   h. Call `innovationTracker.reset()` at end of generation.
   i. Return new `PopulationImpl` with offspring genomes, updated species, and
      the new champion.

---

## Files to Change

| File | Change |
|------|--------|
| `neat-evolution/pom.xml` | Add `testng` (test scope) and `jacoco-maven-plugin` |
| `neat-evolution/src/main/java/.../impl/SpeciesImpl.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/main/java/.../impl/PopulationImpl.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/main/java/.../impl/DefaultSpeciationStrategy.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/main/java/.../impl/TournamentSelectionStrategy.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/main/java/.../impl/StandardCrossoverStrategy.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/main/java/.../impl/StandardMutationStrategy.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/main/java/.../impl/StandardEvolutionEngine.java` | New skeleton → Phase 2 real impl |
| `neat-evolution/src/test/java/.../impl/SpeciesImplTest.java` | New: ~20 failing tests |
| `neat-evolution/src/test/java/.../impl/PopulationImplTest.java` | New: ~15 failing tests |
| `neat-evolution/src/test/java/.../impl/DefaultSpeciationStrategyTest.java` | New: ~18 failing tests |
| `neat-evolution/src/test/java/.../impl/TournamentSelectionStrategyTest.java` | New: ~14 failing tests |
| `neat-evolution/src/test/java/.../impl/StandardCrossoverStrategyTest.java` | New: ~16 failing tests |
| `neat-evolution/src/test/java/.../impl/StandardMutationStrategyTest.java` | New: ~18 failing tests |
| `neat-evolution/src/test/java/.../impl/StandardEvolutionEngineTest.java` | New: ~15 failing tests |
| `PLAN.md` | This planning document |

---

## Acceptance Criteria Checklist

- [ ] Complete implementations exist for all concrete classes in `neat-evolution`
  (`SpeciesImpl`, `PopulationImpl`, `DefaultSpeciationStrategy`,
  `TournamentSelectionStrategy`, `StandardCrossoverStrategy`,
  `StandardMutationStrategy`, `StandardEvolutionEngine`).
- [ ] Implementation follows a TDD format: a full suite of failing unit tests is
  committed before implementations are added (this PR = Phase 1; Phase 2 PR adds
  implementations).
- [ ] Tests use the **TestNG** test module.
- [ ] Code coverage is measured with **JaCoCo** and test execution verified with
  the **Surefire** plugin (`mvn test` succeeds after Phase 2).

---

## Out of Scope

- `FitnessEvaluator` concrete implementations — these belong in `neat-reward`.
- Dynamic compatibility threshold adjustment (target-species-count auto-tuning).
- Multi-threaded fitness evaluation.
- The `neat-training` module — not touched in this ticket.
- Recurrent (cyclic) network topologies.
