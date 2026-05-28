# Traffic Simulation Design

This document is the authoritative design specification for the traffic simulation component
of this NEAT project. It defines the road model, car model, vision system, neural-network
interface, simulation lifecycle, and evaluation strategy. Future implementation tickets
(the `neat-reward` module) should treat this document as their primary contract.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Road Model](#2-road-model)
3. [Car Model](#3-car-model)
4. [Car Behaviours](#4-car-behaviours)
5. [Vision Model](#5-vision-model)
6. [Neural-Network Interface](#6-neural-network-interface)
7. [Simulation Lifecycle](#7-simulation-lifecycle)
8. [Fitness and Evaluation](#8-fitness-and-evaluation)
9. [Population Diversity](#9-population-diversity)
10. [Out of Scope](#10-out-of-scope)

---

## 1. Overview

### Goal

Evolve a heterogeneous population of car controllers (neural networks) that collectively
achieve **high highway throughput** and **zero collisions**. Success is not a single champion
that drives perfectly — it is a diverse population where many different driving styles co-exist
and together produce efficient, safe traffic flow.

### Guiding Constraints

- Traffic flows in **one direction only**.
- Cars are **axis-aligned rectangles** (no angular rotation).
- Merging, acceleration, deceleration, entry, and exit are modelled; no corner-turning,
  no weather, no physics engine.
- Every car's behaviour is determined solely by its **neural network** (the NEAT genome),
  which takes sensor readings as input and produces driving commands as output.
- Cars may differ in physical parameters (size, speed capability) but all share the same
  neural-network input/output schema, making the population directly comparable.

---

## 2. Road Model

### Layout

The highway is a straight, multi-lane road running left-to-right along the **X axis**.

```
X = 0                                                X = highway_length
  |  Lane 0 (leftmost / fast lane)                           |
  |------------------------------------------------------------
  |  Lane 1                                                   |
  |------------------------------------------------------------
  |  Lane 2 (rightmost / slow lane + on/off ramps)           |
  |                                                           |
```

| Parameter       | Default | Description                              |
|-----------------|---------|------------------------------------------|
| `highway_length`| 1000 m  | Total X extent of the simulated road     |
| `lane_count`    | 3       | Number of parallel lanes (configurable)  |
| `lane_width`    | 3.5 m   | Y extent of each lane                    |
| `tick_dt`       | 0.1 s   | Simulation time step                     |
| `max_ticks`     | 2000    | Hard cut-off per generation run (200 s)  |

### Coordinate System

- **X**: direction of travel, 0 at entry, `highway_length` at exit.
- **Y**: lateral position; each lane occupies a fixed Y band.
  - Lane `i` spans Y ∈ `[i × lane_width, (i+1) × lane_width]`.
  - Lane centre: `(i + 0.5) × lane_width`.
- A car's position is the centre of its bounding rectangle.

### On-Ramps and Off-Ramps

- **On-ramp**: located at X = 0 on the rightmost lane. Cars spawn here and must accelerate
  to merge into moving traffic.
- **Off-ramp**: located at X = `highway_length` on the rightmost lane. Cars approaching
  within `ramp_detection_distance` (default 150 m) and intending to exit must move to the
  rightmost lane.
- Additional ramps at intermediate X positions are configurable but not required by default.

### Collision Model

A collision occurs when the axis-aligned bounding rectangles of two cars overlap. Overlaps
are checked every tick. A collision is terminal for the tick but the simulation continues
(the colliding cars are removed and penalised).

---

## 3. Car Model

Each car is a **rectangle** with a fixed set of physical parameters. These parameters are
assigned at spawn and do not change during a car's lifetime. They are not part of the NEAT
genome; instead they are sampled from per-species distributions, which allows species to
specialise into different physical archetypes (compact fast cars vs. longer conservative ones).

### Physical Properties

| Property           | Symbol            | Range        | Unit  | Description                                      |
|--------------------|-------------------|--------------|-------|--------------------------------------------------|
| Length             | `L`               | 3 – 8        | m     | Longitudinal (X) extent of the bounding box      |
| Width              | `W`               | 1.8 – 2.5    | m     | Lateral (Y) extent; must fit within a lane       |
| Max acceleration   | `a_max`           | 2.0 – 6.0    | m/s²  | Upper limit on positive speed change per second  |
| Max deceleration   | `d_max`           | 4.0 – 10.0   | m/s²  | Upper limit on braking force per second          |
| Target speed       | `v_target`        | 20 – 35      | m/s   | Desired cruising speed (~72–126 km/h)            |
| Vision range       | `v_range`         | 50 – 200     | m     | Maximum sensing distance (forward and rearward)  |
| Min gap            | `g_min`           | 5 – 15       | m     | Minimum accepted following distance              |
| Lane-change time   | `t_lc`            | 1.5 – 3.0    | s     | Time to complete a lane transition               |

### State Variables (per tick)

| Variable        | Description                                           |
|-----------------|-------------------------------------------------------|
| `x`, `y`        | Centre position                                       |
| `vx`            | Longitudinal speed (m/s); always ≥ 0                 |
| `lane`          | Current integer lane index                            |
| `merging`       | Boolean; true while a lane change is in progress      |
| `merge_target`  | Target lane during a merge; −1 if not merging        |
| `merge_progress`| Fraction ∈ [0, 1] of lane-change completion           |
| `distance`      | Total X distance covered since spawn                  |
| `collisions`    | Cumulative collision count                            |
| `ticks_alive`   | Ticks survived since spawn                            |

---

## 4. Car Behaviours

### Speed Control

Each tick:

```
accel  = throttle_output × a_max
decel  = brake_output    × d_max
dvx    = (accel − decel) × tick_dt
vx_new = clamp(vx + dvx, 0, v_max_absolute)
```

`v_max_absolute` is a global hard cap (e.g., 50 m/s) preventing runaway acceleration.
The car cannot reverse (vx ≥ 0).

### Lane Changing

1. The car's neural network outputs a `lane_change_left` or `lane_change_right` value.
2. If either value exceeds the **action threshold** (0.5) and the car is not currently
   merging, it initiates a merge:
   - Check that the target lane exists.
   - Check that the blind-spot zone in the target lane is clear (`blind_spot_left/right` = 0).
   - If checks pass, set `merging = true`, record `merge_target`.
3. During the merge (`merging = true`):
   - `y` interpolates linearly toward the target lane centre over `t_lc` seconds.
   - The car's bounding box spans both lanes until `merge_progress = 1.0`.
   - Speed control continues normally; no additional restrictions.
4. On completion: `lane` = `merge_target`, `merging = false`.

If both `lane_change_left` and `lane_change_right` are simultaneously above threshold, neither
is executed (ambiguous command treated as hold).

### Entering the Highway

- Cars are queued before the simulation. At each spawn tick, if the on-ramp zone is clear,
  the next car in queue spawns at X = 0, rightmost lane, with `vx = 0`.
- The car must accelerate to merge into Lane `lane_count − 2` (second-from-right) before
  reaching X = `entry_merge_deadline` (default 80 m). If it fails (no gap), it is removed
  and counted as a failed entry.

### Exiting the Highway

- Cars that reach X = `highway_length` are removed and counted as **successful exits**.
- Cars that have no off-ramp intention drive to the end naturally.
- Cars can also be configured with an explicit exit intention: they must move to the rightmost
  lane before the off-ramp X coordinate. A car that passes the off-ramp without being in
  the rightmost lane loses its exit bonus but continues until the highway end.

---

## 5. Vision Model

### Sensing Zones

Each car observes three lanes: its **current lane**, the **lane to the left**, and the
**lane to the right**. Within each lane it looks:

- **Forward**: up to `v_range` metres ahead along X.
- **Rearward**: up to `v_range` metres behind along X.

The car finds the **nearest** other car in each direction per lane. If no car is detected
within `v_range`, the reading is reported as maximum range (normalised to 1.0).

### Occlusion

If Car A is directly behind Car B in the same lane and Car C is further ahead, Car A cannot
see Car C. Only the nearest car per direction per lane is returned. Cars in adjacent lanes
do not occlude each other.

### Blind Spots

Each car has a blind-spot zone on each side. A car in the adjacent lane is in the blind spot
if it satisfies:

```
Δx ∈ [−(L/2 + 2m), +(L_other/2)]   (just alongside or slightly behind)
Δy ∈ (0, lane_width)                (in the adjacent lane)
```

When a car is in the blind-spot zone, the gap and speed inputs for that side are replaced
with worst-case readings (gap = 0, relative_speed = 0), and the `blind_spot_left` or
`blind_spot_right` input is set to 1.0.

### Normalisation

All distance readings are divided by `v_range` to produce values in [0, 1].
All speed readings are divided by `v_max_absolute` to produce values in [0, 1].
Relative speed readings (speed differential) are shifted into [−1, 1] then rescaled to [0, 1]
for network compatibility (0.5 = same speed as ego car).

---

## 6. Neural-Network Interface

The neural network implemented by `NeuralNetworkImpl` takes a fixed-size input vector and
produces a fixed-size output vector. The I/O schema is identical for every car and every
genome, enabling fair cross-genome comparison.

### Inputs (19 nodes)

All values normalised to [0, 1] unless noted.

| #  | Name                    | Range   | Description                                                          |
|----|-------------------------|---------|----------------------------------------------------------------------|
| 1  | `current_speed`         | [0, 1]  | Ego car speed / `v_max_absolute`                                     |
| 2  | `gap_ahead_current`     | [0, 1]  | Distance to nearest car ahead in current lane / `v_range`            |
| 3  | `relspeed_ahead_current`| [0, 1]  | (speed_ahead − ego_speed + v_max) / (2 × v_max); 0.5 = same speed   |
| 4  | `gap_behind_current`    | [0, 1]  | Distance to nearest car behind in current lane / `v_range`           |
| 5  | `relspeed_behind_current`| [0, 1] | (speed_behind − ego_speed + v_max) / (2 × v_max)                    |
| 6  | `gap_ahead_left`        | [0, 1]  | Forward gap in left lane; 1.0 if lane does not exist                 |
| 7  | `relspeed_ahead_left`   | [0, 1]  | Relative speed of car ahead in left lane; 0.5 if lane absent         |
| 8  | `gap_behind_left`       | [0, 1]  | Rearward gap in left lane; 1.0 if lane does not exist                |
| 9  | `relspeed_behind_left`  | [0, 1]  | Relative speed of car behind in left lane; 0.5 if lane absent        |
| 10 | `gap_ahead_right`       | [0, 1]  | Forward gap in right lane; 1.0 if lane does not exist                |
| 11 | `relspeed_ahead_right`  | [0, 1]  | Relative speed of car ahead in right lane; 0.5 if lane absent        |
| 12 | `gap_behind_right`      | [0, 1]  | Rearward gap in right lane; 1.0 if lane does not exist               |
| 13 | `relspeed_behind_right` | [0, 1]  | Relative speed of car behind in right lane; 0.5 if lane absent       |
| 14 | `left_lane_exists`      | {0, 1}  | 1.0 if a lane exists to the left, 0.0 otherwise                      |
| 15 | `right_lane_exists`     | {0, 1}  | 1.0 if a lane exists to the right, 0.0 otherwise                     |
| 16 | `blind_spot_left`       | {0, 1}  | 1.0 if another car occupies the left blind-spot zone                 |
| 17 | `blind_spot_right`      | {0, 1}  | 1.0 if another car occupies the right blind-spot zone                |
| 18 | `lane_index`            | [0, 1]  | Current lane / (lane_count − 1); 0 = leftmost, 1 = rightmost        |
| 19 | `ramp_proximity`        | [0, 1]  | 1 − (distance to nearest ramp / ramp_detection_distance); 0 = far   |

**Bias node**: the existing `NodeType.BIAS` node in the NEAT genome is always activated at
1.0 and is not counted in the 19 sensory inputs.

### Outputs (4 nodes)

All outputs are the raw sigmoid activation of the corresponding output node, in [0, 1].

| #  | Name                 | Range  | Action                                                                 |
|----|----------------------|--------|------------------------------------------------------------------------|
| 1  | `throttle`           | [0, 1] | Accelerate at `output × a_max` m/s²                                   |
| 2  | `brake`              | [0, 1] | Decelerate at `output × d_max` m/s²                                   |
| 3  | `lane_change_left`   | [0, 1] | Initiates left merge when > 0.5 (if not already merging)              |
| 4  | `lane_change_right`  | [0, 1] | Initiates right merge when > 0.5 (if not already merging)             |

`throttle` and `brake` may both be non-zero simultaneously; net acceleration is
`(throttle × a_max) − (brake × d_max)`.

---

## 7. Simulation Lifecycle

### Setup Phase (once per experiment)

1. Instantiate the highway with the configured `lane_count` and `highway_length`.
2. Build a spawn queue of `population_size` genomes from the current `Population`.
3. Set the global tick counter to 0.

### Generational Run (once per generation)

A generation run evaluates all genomes in the current population. It proceeds as follows:

```
while (cars_remaining > 0 AND tick < max_ticks):
    1. SPAWN    — if the on-ramp is clear and the queue is non-empty, add the next car
    2. OBSERVE  — for each active car, compute its 19-element input vector
    3. ACTIVATE — for each active car, call NeuralNetwork.activate(inputs)
    4. ACT      — apply throttle, brake, and lane-change outputs
    5. ADVANCE  — update positions: x += vx × tick_dt
    6. DETECT   — check bounding-box overlaps; mark colliding cars
    7. EXIT     — remove cars that have reached highway_length; record exit fitness
    8. CULL     — remove cars that collided or stalled (vx < stall_threshold for > stall_grace)
    9. SCORE    — update per-car running fitness accumulators
   10. TICK++
```

### Entry (Step 1 in detail)

- Spawn interval: one new car every `spawn_interval_ticks` (default 10 ticks = 1 s).
- The on-ramp zone (X ∈ [0, 20 m], rightmost lane) must be free of other cars.
- Spawned car: X = 0, Y = rightmost lane centre, `vx = 5 m/s` (initial push).

### Traverse (Steps 2–9 in detail)

Each tick every active car perceives its environment, runs its neural network, and acts.
The simulation is synchronous: all cars act on the state from the *previous* tick before
positions are advanced.

### Exit (Step 7 in detail)

A car is removed when its front edge (X + L/2) exceeds `highway_length`. Its final fitness
contribution is recorded (see Section 8). Cars that time out (`tick = max_ticks`) are
removed with partial credit.

### Termination

The generation run ends when **all cars have been removed** (exited, collided, or culled) or
`max_ticks` is reached. After termination the `FitnessEvaluator` returns the recorded fitness
for each genome to the `EvolutionEngine` for selection and reproduction.

---

## 8. Fitness and Evaluation

### 8.1 Individual Car Fitness

Each car accumulates fitness across its lifetime. Final fitness is:

```
fitness =   w_progress  × (x_final / highway_length)
          + w_speed     × (avg_vx / v_target)
          + w_exit      × exit_bonus
          − w_collision × collision_count
          − w_near_miss × near_miss_count
```

| Term            | Weight | Description                                                       |
|-----------------|--------|-------------------------------------------------------------------|
| `w_progress`    | 0.3    | Reward for distance covered; partial credit for non-exiters       |
| `w_speed`       | 0.3    | Reward for maintaining speed close to `v_target`                 |
| `w_exit`        | 0.3    | Bonus of 1.0 for successfully reaching `highway_length`           |
| `w_collision`   | 0.5    | Penalty per collision event (can drive fitness below 0)           |
| `w_near_miss`   | 0.1    | Penalty per tick with gap < `g_min / 2` in the current lane      |

A near-miss is defined as any tick where the forward gap in the current lane drops below
half the car's minimum gap setting.

### 8.2 Population-Level Metrics (logged, not used in selection)

At the end of every generation the following aggregate statistics are recorded:

| Metric                | Formula                                       | Target    |
|-----------------------|-----------------------------------------------|-----------|
| **Throughput**        | `successful_exits / spawned_count`            | → 1.0     |
| **Average speed**     | `mean(avg_vx)` across all cars                | → v_target|
| **Speed variance**    | `std(avg_vx)` across all cars                 | Moderate  |
| **Collision rate**    | `total_collisions / spawned_count`            | → 0.0     |
| **Near-miss rate**    | `total_near_miss_ticks / total_car_ticks`     | → 0.0     |
| **Diversity index**   | Std. deviation of individual fitness scores   | Maximise  |

These metrics inform the researcher but do not feed back into NEAT's selection loop directly.

### 8.3 Species Evaluation

Species fitness and offspring allocation follow the NEAT fitness-sharing scheme already
implemented in `DefaultSpeciationStrategy` and `StandardEvolutionEngine`:

**Species fitness** (shared fitness):

```
species_fitness = Σ (individual_fitness_i / species_size)   for all i in species
```

Dividing by species size implements **niche protection**: a species of 10 average individuals
does not crowd out a species of 1 exceptional individual.

**Offspring quota**:

```
quota_s = round( species_fitness_s / Σ species_fitness_all × population_size )
```

Rounding remainders are distributed to the species with the highest fractional parts.

**Stagnation penalty**:

A species that has not improved its `bestFitness` for `stagnation_threshold` generations
(default 15, matching `generationsSinceImprovement` in `SpeciesImpl`) has its quota halved.
This forces resources away from stuck lineages toward novel structures.

**Species elimination**:

Any species whose quota rounds to zero is removed; its members are left unprotected and
compete in open selection next generation.

**Representative update**:

At the end of each generation the `DefaultSpeciationStrategy` selects a random survivor from
each species as the new representative. Incoming genomes in the next generation are assigned
to the first species whose representative is within the compatibility threshold.

---

## 9. Population Diversity

### Why Diversity is Required

A homogeneous population — where every car has the same strategy — is brittle:
- A single emergent bottleneck (e.g., everyone tries to merge at the same point) causes
  cascading collisions.
- Throughput collapses when all cars have the same `v_target` and form a single platoon.
- NEAT's fitness landscape is deceptive: conservative safe drivers initially outscore
  bold mergers, but bold mergers are necessary for high throughput at scale.

Diversity requirements:
1. Multiple **speed archetypes** must coexist (slow, medium, fast).
2. Multiple **merging strategies** must be represented.
3. No single genome phenotype should constitute > 50 % of the population at any generation.

### How NEAT Encourages Diversity

| Mechanism                        | Effect                                                         |
|----------------------------------|----------------------------------------------------------------|
| Speciation by compatibility distance | Novel topologies are shielded from immediate competition  |
| Stagnation penalty               | Prevents any single strategy from monopolising resources       |
| Mutation (add node / add connection) | Continuously introduces structural novelty                |
| Crossover across compatible genomes | Recombines diverse strategies into new hybrids            |
| Species representative sampling  | Representatives change each generation, keeping species fluid  |

### Measuring Diversity

At each generation the simulation records:
- **Behavioural variance**: standard deviation of `avg_vx`, `successful_exit` rate, and
  `collision_count` across all cars. Low variance signals convergence to a single strategy.
- **Topological diversity**: mean pairwise `compatibilityDistance` across the population.
  This is available directly from `GenomeImpl.compatibilityDistance`.
- **Species count**: should remain above 2 throughout training; target range is 3–8 species.

A researcher may set an early-stopping criterion on diversity: if `species_count = 1` for
more than 10 consecutive generations, training is considered stagnated and should be restarted
with higher mutation rates or a larger initial population.

---

## 10. Out of Scope

The following are explicitly excluded from this simulation design:

| Excluded Feature               | Reason                                                         |
|--------------------------------|----------------------------------------------------------------|
| Realistic vehicle physics       | Steering, suspension, tyre friction add complexity without contributing to the NEAT learning signal |
| Weather / road conditions       | Would require additional inputs and complicate training without addressing core objectives |
| Multi-directional traffic       | Intersections and opposing-lane traffic require a fundamentally different road model |
| Traffic signals or right-of-way rules | Adds rule-following as a secondary objective that would dilute the primary throughput signal |
| Fuel consumption / emissions   | Not relevant to the optimisation goals                        |
| Driver fatigue / attention      | Human cognitive modelling is outside NEAT's scope             |
| Cornering / curved roads        | Rotation would require a 2D physics engine; the axis-aligned bounding-box collision model would no longer apply |
| Vehicle communication (V2X)    | Cars may only sense passively; no inter-car messaging         |
| Multi-lane side-by-side spawning | Only one on-ramp at a time; simplifies entry sequencing      |
