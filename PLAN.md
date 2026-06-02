# Plan: CLOD-23: Complete the trafficsim module

## What & Why

The `trafficsim` module already has 13 well-defined interfaces in
`com.ddiggs.neat.trafficsim`, a complete `pom.xml`, and is registered in the parent build.
CLOD-22 delivered those interfaces; CLOD-23 is the implementation ticket. The acceptance
criteria require replacing the interface stubs with functional concrete implementations in
`com.ddiggs.neat.trafficsim.impl`, fully covered by unit tests. Every method contract
documented in the interfaces must be honoured, and all logic must match
`docs/SIMULATION.md`.

No `pom.xml` or parent-pom changes are needed — the module skeleton is complete.

## Approach

### Step 1 — Pure value / immutable classes (no business logic)

Implement these first as they have no dependencies on other impls:

- **`CarPhysicsImpl`** — implements `CarPhysics`; constructor takes all 8 properties with
  validation (e.g., length in [3, 8]). Factory method `CarPhysicsImpl.random(Random)`
  samples values from the ranges in SIMULATION.md Section 3 for use by `SpawnStrategy`.

- **`DriveCommandImpl`** — implements `DriveCommand`; constructor takes `double[4]` from
  `NeuralNetwork.activate()`. `toArray()` returns a defensive copy.

- **`SensorInputImpl`** — implements `SensorInput`; constructor takes `double[19]`. All 19
  accessor methods index into the internal array. `toArray()` returns a defensive copy.
  `SIZE = 19` constant.

- **`SimulationConfigImpl`** — implements `SimulationConfig`. Builder pattern:
  `SimulationConfigImpl.builder()` with per-field setters and a `build()` that applies
  defaults from SIMULATION.md (highway=1000, lanes=3, laneWidth=3.5, tickDt=0.1,
  maxTicks=2000, vMax=50, spawnInterval=20, spawnClear=80, mergeLockout=50, spawnSpeed=10,
  stallThreshold=1.0, stallGrace=30, weights 0.3/0.3/0.3/0.5/0.1).

- **`PopulationMetricsImpl`** — implements `PopulationMetrics`; constructor takes all 6
  metric values.

### Step 2 — Mutable state class

- **`CarStateImpl`** — implements `CarState`; package-private setters for `x`, `y`, `vx`,
  `lane`, `merging`, `mergeTarget`, `mergeProgress`, `distance`, `collisions`,
  `ticksAlive`. Also tracks internal-only accumulators not on the interface: `sumVx`
  (for average speed), `nearMissTicks`, `stallTicks`, `spawnX` (for merge-lockout check),
  `exited` and `collided` (removal flags).

### Step 3 — Car

- **`CarImpl`** — implements `Car`. Constructor: `(long id, CarPhysics, CarState, NeuralNetwork, Genome)`.
  Package-private helpers used by the simulation engine:
  - `double frontX()` — `getState().getX() + getPhysics().getLength() / 2`
  - `boolean isMergeLocked(SimulationConfig)` — distance from spawnX < mergeLockoutDistance
  - `boolean isStalled(SimulationConfig)` — stall ticks exceed grace period
  - `FitnessRecord toFitnessRecord()` — snapshot, stores `v_target` for use in `computeFitness`

### Step 4 — FitnessRecord and GenerationResult

- **`FitnessRecordImpl`** — implements `FitnessRecord`. Stores `finalX`, `averageVx`,
  `exited`, `collisionCount`, `nearMissCount`, and `vTarget` (per-car, from
  `CarPhysics.getTargetSpeed()` at removal time — needed by `computeFitness` which only
  receives `SimulationConfig`).
  `computeFitness(SimulationConfig)` implements the formula from Section 8.1.

- **`GenerationResultImpl`** — implements `GenerationResult`. Constructor takes
  `Map<Genome, FitnessRecord>`, `PopulationMetrics`, `int exitCount`, `int spawnedCount`,
  `int totalTicks`. `getFitnessRecord(Genome)` throws `IllegalArgumentException` on miss.

### Step 5 — Road

- **`RoadImpl`** — implements `Road`. Constructor: `(SimulationConfig)`.
  `getLaneCenterY(lane)` = `(lane + 0.5) * laneWidth`.
  `isValidLane(lane)` = `lane >= 0 && lane < laneCount`.
  Throws `IllegalArgumentException` for out-of-bounds lane in boundary methods.

### Step 6 — VisionSystem

- **`VisionSystemImpl`** — implements `VisionSystem`. Stateless; all state passed in.
  `observe(Car ego, Road road, List<Car> allCars, SimulationConfig config)`:
  1. For each of the three lanes (left, current, right): collect candidates visible in
     that lane. A car is visible in lane `L` if `car.getState().getLane() == L` OR
     (`car.getState().isMerging() && car.getState().getMergeTarget() == L`).
  2. Find nearest ahead (smallest positive delta-x) and nearest behind (largest negative
     delta-x), bounded by the appropriate vision range.
  3. Check blind-spot zone (delta-x in [-(L/2 + 2), +(L_other/2)] and in adjacent lane);
     when triggered, replace gap=0.0, relSpeed=0.5 and set flag=1.0.
  4. Normalise: current-lane gaps / vRange, adjacent / vRangeAdjacent; relative speed
     = (speed_other - ego_speed + vMax) / (2 * vMax); absent lanes → gap=1.0, speed=0.5.
  5. Pack results into `SensorInputImpl` in the order defined by `SensorInput.toArray()`.

### Step 7 — SpawnStrategy

- **`SpawnStrategyImpl`** — implements `SpawnStrategy`.
  `shouldSpawn(tick, activeCars, config)`: returns `true` if
  `tick % spawnIntervalTicks == 0` AND no active car has `X < spawnClearDistance`.
  `spawnCars(pendingGenomes, road, config)`:
  1. Take up to `laneCount` genomes from the front of `pendingGenomes` (remove them).
  2. For each genome build `CarPhysicsImpl.random(new Random(genome.hashCode()))` (seeded
     per-genome for reproducibility), `CarStateImpl` at lane-centre Y with `vx=spawnSpeed`.
  3. Build `NeuralNetwork` from genome via `NeuralNetworkImpl`.
  4. Sort by `physics.getTargetSpeed()` descending; assign to lanes 0..N.
  5. Return `List<Car>`.

### Step 8 — Simulation

- **`SimulationImpl`** — implements `Simulation`.
  - `setup(Population, Road, SimulationConfig)`: builds `pendingGenomes` list from all
    genomes in the population; stores road and config.
  - `runGeneration()`: ten-step tick loop until `activeCars.isEmpty() || tick >= maxTicks`.
    Uses `SpawnStrategyImpl`, `VisionSystemImpl` instances internally.
    After the loop: compute `PopulationMetrics` from all `FitnessRecord`s; build
    `GenerationResultImpl`; return it.

### Step 9 — Unit tests

TestNG, `@BeforeMethod` setup, no Mockito. One test class per impl:

- `CarPhysicsImplTest` — constructor validation, getters, `random()` bounds
- `DriveCommandImplTest` — construction from array, `toArray()` defensive copy
- `SensorInputImplTest` — SIZE=19, all 19 accessors, `toArray()` copy
- `SimulationConfigImplTest` — builder defaults, custom values
- `PopulationMetricsImplTest` — constructor and accessors
- `CarStateImplTest` — initial state, setters, internal accumulator arithmetic
- `CarImplTest` — `frontX()`, merge-lockout boundary, stall detection, `toFitnessRecord()`
- `FitnessRecordImplTest` — `computeFitness` with known values (positive, negative, exit bonus)
- `GenerationResultImplTest` — `getFitnessRecord` hit/miss, unmodifiable map
- `RoadImplTest` — `getLaneCenterY`, `isValidLane`, exception on bad lane index
- `VisionSystemImplTest` — no cars (all gaps=1.0), car ahead, car behind, blind spot,
  merging car visible from both lanes
- `SpawnStrategyImplTest` — tick interval, blocked by spawn zone, lane ordering by vTarget
- `SimulationImplTest` — single-car run returns 1 FitnessRecord; tick limit terminates run

## Files to Change

No pom.xml changes needed — module skeleton is complete.

**Implementations (`impl` package):**
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/CarPhysicsImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/DriveCommandImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/SensorInputImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/SimulationConfigImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/PopulationMetricsImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/CarStateImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/CarImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/FitnessRecordImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/GenerationResultImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/RoadImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/VisionSystemImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/SpawnStrategyImpl.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/impl/SimulationImpl.java` — new

**Tests:**
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/CarPhysicsImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/DriveCommandImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/SensorInputImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/SimulationConfigImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/PopulationMetricsImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/CarStateImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/CarImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/FitnessRecordImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/GenerationResultImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/RoadImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/VisionSystemImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/SpawnStrategyImplTest.java` — new
- `trafficsim/src/test/java/com/ddiggs/neat/trafficsim/impl/SimulationImplTest.java` — new

## Acceptance Criteria Checklist

- [ ] Every interface in `com.ddiggs.neat.trafficsim` has a concrete `impl` class
      implementing all methods per `docs/SIMULATION.md`.
- [ ] `mvn -pl trafficsim compile` passes with zero errors.
- [ ] `mvn -pl trafficsim test` passes with all tests green.

## Out of Scope

- Changes to `pom.xml` files (skeleton is complete from CLOD-22).
- A `FitnessEvaluator` bridge to `StandardEvolutionEngine` (future integration ticket).
- A `Trainer` implementation (CLOD-21).
- Visualisation, serialisation of simulation state, or CLI entry points.
- Any feature in Section 10 "Out of Scope" of `docs/SIMULATION.md`.
