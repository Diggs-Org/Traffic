# Plan: CLOD-22: Create traffic-sim interfaces

## What & Why

A new Maven module (`trafficsim`) must be created to house the traffic simulation domain.
Based on `docs/SIMULATION.md`, this module defines all interfaces — road geometry, car
physics & state, vision sensing, neural-network I/O, simulation lifecycle, fitness
recording, and aggregate metrics — with full Javadoc. Downstream implementation tickets
(`neat-reward` / `neat-trafficsim-impl`) will provide concrete implementations of these
contracts. No implementation code or tests are included in this ticket (interfaces have no
behaviour to test).

## Approach

1. Add `trafficsim` as a Maven sub-module in the parent `pom.xml`.
2. Create `trafficsim/pom.xml` inheriting from `neat-parent`; depend on `neat-core` and
   `neat-evolution` since `Simulation` references `Population` and `GenerationResult`
   references `Genome`.
3. Create the following interfaces in
   `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/`, each with full Javadoc matching
   the style of `NeuralNetwork.java` and `FitnessEvaluator.java`:

   | Interface           | Section in SIMULATION.md | Responsibility |
   |---------------------|--------------------------|----------------|
   | `SimulationConfig`  | §2, §3, §4, §7           | All tuneable parameters (highway_length, lane_count, tick_dt, v_max_absolute, spawn params, etc.) |
   | `Road`              | §2                       | Lane geometry queries (lane count, width, centre-Y, validity) |
   | `CarPhysics`        | §3 Physical Properties   | Fixed properties assigned at spawn (L, W, a_max, d_max, v_target, v_range, g_min, t_lc) |
   | `CarState`          | §3 State Variables       | Mutable per-tick state (x, y, vx, lane, merging, merge_progress, distance, collisions, ticks_alive) |
   | `Car`               | §3                       | Simulation entity: id, physics, state, and associated NeuralNetwork |
   | `SensorInput`       | §6 Inputs (19 nodes)     | The 19-element normalised observation vector fed into the neural network; `toArray()` method |
   | `DriveCommand`      | §6 Outputs (4 nodes)     | The 4-element action vector produced by the neural network |
   | `VisionSystem`      | §5                       | Computes a `SensorInput` for a car given the road and all active cars |
   | `SpawnStrategy`     | §4 Spawning, §7 Spawn    | Decides when to spawn and creates new `Car` instances from genomes |
   | `Simulation`        | §7                       | Generational run lifecycle: `setup`, `runGeneration` |
   | `FitnessRecord`     | §8.1                     | Per-car fitness accumulator with component breakdown; `computeFitness(SimulationConfig)` |
   | `GenerationResult`  | §7 Termination, §8       | Maps genomes to fitness records; exposes exit/spawn counts |
   | `PopulationMetrics` | §8.2                     | Six aggregate statistics (throughput, avg speed, variance, collision rate, near-miss rate, diversity index) |

4. Update parent `pom.xml` to include the new `<module>trafficsim</module>`.
5. Commit and push.

## Files to Change

- `pom.xml` — add `<module>trafficsim</module>`
- `trafficsim/pom.xml` — new module POM
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/SimulationConfig.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/Road.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/CarPhysics.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/CarState.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/Car.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/SensorInput.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/DriveCommand.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/VisionSystem.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/SpawnStrategy.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/Simulation.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/FitnessRecord.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/GenerationResult.java` — new
- `trafficsim/src/main/java/com/ddiggs/neat/trafficsim/PopulationMetrics.java` — new

## Acceptance Criteria Checklist

- [ ] A new `trafficsim` Maven module exists and is registered in `pom.xml`
- [ ] All interfaces listed above are created in package `com.ddiggs.neat.trafficsim`
- [ ] Every interface and every method has full Javadoc (summary line, `<p>` paragraphs for context, `@param`, `@return`, `@throws` as appropriate)
- [ ] The module compiles cleanly (`mvn compile` passes)

## Out of Scope

- No concrete implementation classes (`impl` package is left empty)
- No unit tests (interfaces have no executable behaviour)
- No simulation runner, rendering, or visualisation
- No `neat-reward` integration (that is a future ticket)
