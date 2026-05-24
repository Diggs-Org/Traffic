# Plan: CLOD-18: Complete the neat-core module

## What & Why

The `neat-core` module currently defines only interfaces and an enum — it has no concrete
implementations, no tests, and no test infrastructure. This ticket delivers the full
implementation of every `neat-core` abstraction (`NodeGeneImpl`, `ConnectionGeneImpl`,
`GenomeImpl`, `NeuralNetworkImpl`, `InnovationTrackerImpl`), driven by a TDD approach:
failing TestNG tests are committed first (on this planning PR), and Phase 2 provides the
implementations that make all tests pass. JaCoCo and Surefire are configured in the parent
POM to enable code-coverage reporting on every build.

## Approach

1. **Configure test tooling** in the root `pom.xml`:
   - Add TestNG 7.9.0 as a `test`-scoped dependency in `<dependencyManagement>`
   - Add `maven-surefire-plugin` 3.5.2 to `<pluginManagement>` with `reuseForks=false` for
     clean per-test JVM state
   - Add `jacoco-maven-plugin` 0.8.12 with `prepare-agent` (bound to `initialize`) and
     `report` (bound to `test`) execution goals

2. **Create skeleton implementation classes** in `com.ddiggs.neat.core.impl` with the
   correct constructor signatures but all interface methods throwing
   `UnsupportedOperationException("Not yet implemented")`. This allows the test classes to
   compile before implementations exist:
   - `InnovationTrackerImpl` — hash-map-based within-generation cache + monotonic global counter
   - `NodeGeneImpl` — immutable value object; 16-byte little-endian serialisation (int id + int
     NodeType ordinal + double bias)
   - `ConnectionGeneImpl` — immutable value object; 25-byte serialisation (4 ints + 1 double + 1 bool)
   - `GenomeImpl` — wraps unmodifiable node/connection lists; implements standard NEAT
     compatibility-distance formula δ = (c1·E + c2·D)/N + c3·W̄
   - `NeuralNetworkImpl` — topological-sort feed-forward activation; applies per-node sigmoid,
     respects disabled connections and BIAS nodes

3. **Write failing TestNG tests** in `neat-core/src/test/java/com/ddiggs/neat/core/impl/`:
   - `InnovationTrackerImplTest` — initial state, within-generation deduplication, monotonic
     counter, reset clears cache without resetting global counter
   - `NodeGeneImplTest` — accessors, serialisation round-trip, all four NodeTypes, invalid data
   - `ConnectionGeneImplTest` — all accessors, enabled/disabled flag, serialisation round-trip
   - `GenomeImplTest` — list immutability, compatibility-distance formula (zero for identical
     genomes, positive for disjoint/excess, symmetric, coefficient scaling), serialisation round-trip
   - `NeuralNetworkImplTest` — wrong input size exception, output array length, simple
     feedforward correctness, disabled connections excluded, BIAS node contribution

4. **Phase 2 (implementation)**: replace every `UnsupportedOperationException` stub with real
   logic; all tests must then pass and JaCoCo must report ≥ 80% line coverage.

## Files to Change

- `pom.xml` — add TestNG, Surefire 3.5.2, JaCoCo 0.8.12 to `<pluginManagement>` and
  `<dependencyManagement>`
- `neat-core/src/main/java/com/ddiggs/neat/core/impl/InnovationTrackerImpl.java` — new skeleton
- `neat-core/src/main/java/com/ddiggs/neat/core/impl/NodeGeneImpl.java` — new skeleton
- `neat-core/src/main/java/com/ddiggs/neat/core/impl/ConnectionGeneImpl.java` — new skeleton
- `neat-core/src/main/java/com/ddiggs/neat/core/impl/GenomeImpl.java` — new skeleton
- `neat-core/src/main/java/com/ddiggs/neat/core/impl/NeuralNetworkImpl.java` — new skeleton
- `neat-core/src/test/java/com/ddiggs/neat/core/impl/InnovationTrackerImplTest.java` — failing tests
- `neat-core/src/test/java/com/ddiggs/neat/core/impl/NodeGeneImplTest.java` — failing tests
- `neat-core/src/test/java/com/ddiggs/neat/core/impl/ConnectionGeneImplTest.java` — failing tests
- `neat-core/src/test/java/com/ddiggs/neat/core/impl/GenomeImplTest.java` — failing tests
- `neat-core/src/test/java/com/ddiggs/neat/core/impl/NeuralNetworkImplTest.java` — failing tests
- `PLAN.md` — this planning document

## Acceptance Criteria Checklist

- [ ] Complete implementations exist for all classes in neat-core (`NodeGeneImpl`,
  `ConnectionGeneImpl`, `GenomeImpl`, `NeuralNetworkImpl`, `InnovationTrackerImpl`)
- [ ] Tests follow Test-Driven Development format; this plan PR contains a full suite of
  failing tests that pass once implementations are complete
- [ ] Tests use the TestNG test module with `@Test`, `@BeforeMethod`, and TestNG assertions
- [ ] Code coverage is measured by the JaCoCo plugin and reported via the Surefire plugin on
  every `mvn test` run

## Out of Scope

- Implementations for `neat-evolution` or `neat-training` — separate tickets
- Recurrent (cyclic) network topologies in `NeuralNetworkImpl` — feed-forward only for now
- Concrete `ActivationFunction` utility class beyond what is inline-tested (sigmoid via lambda)
- Performance optimisation or GPU acceleration
- Integration tests between `neat-core` and `neat-evolution`
