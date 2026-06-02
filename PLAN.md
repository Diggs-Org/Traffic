# Plan: CLOD-23: Complete the trafficsim module

## What & Why

The `trafficsim` module currently has no source files — only a stale Maven target directory from
a previous compile attempt. The module is missing a `pom.xml` and is excluded from the parent
build. The acceptance criteria require replacing the stub interfaces with fully functional
concrete implementations, all covered by unit tests. The implementation must follow the
authoritative specification in `docs/SIMULATION.md`, which defines the road model, car model,
vision system, neural-network I/O, simulation lifecycle, and fitness formula.

## Approach

1. **Create the module skeleton** — `trafficsim/pom.xml` (depends on `neat-core` +
   `neat-evolution`; includes TestNG + JaCoCo) and add `trafficsim` to the parent
   `pom.xml` `<modules>` list.

2. **Implement data / value classes** (pure records or simple immutable classes, no logic):
   - `SimulationConfig` — all road and spawn parameters with sane defaults.
   - `CarPhysics` — immutable physical properties per car (size, acceleration, vision ranges).
   - `CarState` — mutable per-tick state (position, speed, merge progress, fitness accumulators).
   - `DriveCommand` — four-element neural-network output record.
   - `SensorInput` — 19-element normalised input vector record (SIZE constant = 19).
   - `FitnessRecord` — snapshot of a finished car's fitness accumulators; computes final score.
   - `PopulationMetrics` — generation-level aggregate statistics record.
   - `GenerationResult` — full output of one simulation run (per-car fitness + metrics).

3. **Implement `Car`** — pairs `CarPhysics` + `CarState` with a `NeuralNetwork` reference and
   a car-id. Provides helpers: `getBoundingBox()`, `isMergeLocked(SimulationConfig)`,
   `isStalled(SimulationConfig)`, `toFitnessRecord(SimulationConfig)`.

4. **Implement `VisionSystem`** — stateless utility class. Central method:
   `compute(Car ego, List<Car> allCars, SimulationConfig config) → SensorInput`.
   Implements the full vision model: current-lane vs adjacent-lane ranges, occlusion
   (nearest-only), blind-spot detection (the ±L/2+2m zone), merging-car dual-lane
   visibility, and normalisation formulas from Section 5 of SIMULATION.md.

5. **Implement `SpawnStrategy`** — manages the unspawned genome queue across a generation.
   `trySpawn(List<Car> active, int tick, SimulationConfig) → List<Car>`: triggers a spawn
   event every `spawnIntervalTicks` when the spawn zone (X in [0, spawnClearDistance]) is
   clear; assigns genomes to lanes ordered by `v_target` descending.

6. **Implement `Road`** — mutable highway state. Holds `List<Car> activeCars`. Provides:
   lane-centre Y, lane-boundary check, `addCar`, `removeCar`, `getActiveCars`. Does NOT
   own simulation logic — purely a container with geometry helpers.

7. **Implement `Simulation`** — the main engine. Constructor takes `SimulationConfig`.
   Public API: `GenerationResult run(List<NeuralNetwork> networks)`.
   Executes the ten-step tick loop from Section 7 of SIMULATION.md:
   SPAWN → OBSERVE → ACTIVATE → ACT → ADVANCE → DETECT → EXIT → CULL → SCORE → TICK++.
   After the loop, computes `PopulationMetrics` and assembles `GenerationResult`.

8. **Write unit tests** (TestNG, no Mockito, `@BeforeMethod` setup pattern) for every
   non-trivial class:
   - `SimulationConfigTest` — default factory, validation edge cases.
   - `CarPhysicsTest` — constructor, getters, boundary values.
   - `CarStateTest` — initial state, mutation, stall logic, merge-lockout check.
   - `CarTest` — bounding-box calculation, helpers delegating to state/physics.
   - `DriveCommandTest` — construction, threshold checks.
   - `SensorInputTest` — SIZE constant, construction, value access.
   - `FitnessRecordTest` — fitness formula with known inputs (progress, speed, exit bonus,
     collision penalty, near-miss penalty); negative fitness possible.
   - `VisionSystemTest` — isolated scenarios: no cars (all gaps = 1.0), car ahead only,
     car behind only, blind-spot detection, merging car visible from both lanes.
   - `SpawnStrategyTest` — spawn on tick 0, skip when spawn zone occupied, lane ordering
     by v_target, multiple spawn events.
   - `RoadTest` — add/remove cars, lane geometry.
   - `PopulationMetricsTest` — record construction and accessors.
   - `GenerationResultTest` — fitness map construction.
   - `SimulationTest` — integration-style: single-car solo run produces a `GenerationResult`
     with one fitness entry; two-car collision test; timeout test.

## Files to Change

- `pom.xml` — add `<module>trafficsim</module>` to the parent modules list
- `trafficsim/pom.xml` — new Maven module descriptor
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/SimulationConfig.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/CarPhysics.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/CarState.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/Car.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/DriveCommand.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/SensorInput.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/FitnessRecord.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/PopulationMetrics.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/GenerationResult.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/Road.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/VisionSystem.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/SpawnStrategy.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/Simulation.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/SimulationConfigTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/CarPhysicsTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/CarStateTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/CarTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/DriveCommandTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/SensorInputTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/FitnessRecordTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/VisionSystemTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/SpawnStrategyTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/RoadTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/PopulationMetricsTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/GenerationResultTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/SimulationTest.java` — new

## Acceptance Criteria Checklist

- [ ] Every file that was a stub interface has been replaced with a functional concrete class
      whose logic matches `docs/SIMULATION.md` (road model, car physics, vision, lifecycle,
      fitness formula).
- [ ] The `trafficsim` module compiles cleanly (`mvn -pl trafficsim compile`).
- [ ] All unit tests pass (`mvn -pl trafficsim test`).

## Out of Scope

- A concrete `Trainer` implementation in `neat-training` (separate ticket).
- A `FitnessEvaluator` bridge between `Simulation` and `StandardEvolutionEngine`
  (would belong in a future integration/training module).
- Visualisation, serialisation of simulation state, or CLI entry points.
- Multi-directional traffic, weather, ramps, or any feature listed under
  Section 10 "Out of Scope" in `docs/SIMULATION.md`.
