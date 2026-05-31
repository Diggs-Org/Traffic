# Plan: CLOD-21: Complete neat-training

## What & Why

The `neat-training` module defines five types — `Trainer`, `TrainingCallback`,
`TrainingConfig`, `TrainingEnvironment`, and `TrainingResult` — but ships with no
concrete implementation and no tests. The acceptance criterion requires every class and
function to be complete and covered by unit tests. This work adds `StandardTrainer`, the
concrete implementation of the `Trainer` orchestration loop, and full TestNG test suites
for the implementation and both record types.

## Approach

1. **Update `neat-training/pom.xml`** — add TestNG (test scope) and JaCoCo plugin to
   match the sibling modules.

2. **Implement `StandardTrainer`** in
   `neat-training/src/main/java/com/ddiggs/neat/training/impl/StandardTrainer.java`:
   - Constructor accepts `EvolutionEngine` and an initial `Population`.
   - `train()` builds a `FitnessEvaluator` that wraps the `TrainingEnvironment`:
     reset → observe → activate → step → reward loop until `isDone()`.
   - Tracks the best fitness seen across all evaluations each generation via a
     capturing lambda.
   - Calls `callback.onGenerationComplete()` after each generation.
   - Stops when champion fitness ≥ `fitnessThreshold` or `maxGenerations` reached.
   - Calls `callback.onTrainingComplete()` and returns `TrainingResult`.

3. **Write `StandardTrainerTest`** in
   `neat-training/src/test/java/com/ddiggs/neat/training/impl/StandardTrainerTest.java`:
   - Uses a deterministic single-step `TrainingEnvironment` stub whose reward equals
     a fixed value, making fitness predictable.
   - Tests: non-null result, champion non-null, stops at maxGenerations, stops at
     fitnessThreshold early, callback invocation counts, null-arg NPEs.

4. **Write `TrainingConfigTest`** and **`TrainingResultTest`** in
   `neat-training/src/test/java/com/ddiggs/neat/training/`:
   - Verify record accessors, equality, and `toString` content.

5. **Run `mvn test -pl neat-training`** and fix any failures.

## Files to Change

- `neat-training/pom.xml` — add testng dependency + jacoco plugin
- `neat-training/src/main/java/com/ddiggs/neat/training/impl/StandardTrainer.java` — new
- `neat-training/src/test/java/com/ddiggs/neat/training/impl/StandardTrainerTest.java` — new
- `neat-training/src/test/java/com/ddiggs/neat/training/TrainingConfigTest.java` — new
- `neat-training/src/test/java/com/ddiggs/neat/training/TrainingResultTest.java` — new

## Acceptance Criteria Checklist

- [ ] All functions and classes are completed and have full unit testing.

## Out of Scope

- Concrete `TrainingEnvironment` implementations (future `neat-reward` module).
- Parallel/multi-threaded fitness evaluation.
- Checkpoint/resume functionality.
