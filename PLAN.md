# Plan: CLOD-20: Create a markdown file explaining how the actual traffic simulation will work

## What & Why

This ticket produces `docs/SIMULATION.md` — a design specification describing the traffic
simulation that will eventually be implemented as the `neat-reward` module. The document must
cover simulation mechanics (lanes, car properties, enter/exit flow), the neural-network I/O
contract (inputs the car senses, outputs it emits), and the NEAT-specific evaluation strategy
(how individual fitness, population fitness, and species fitness are measured). It establishes
the shared vocabulary and behavioural contract before any Java code is written.

## Approach

1. Create `docs/` directory and write `docs/SIMULATION.md` with the following top-level sections:
   - **Overview** — goals, non-goals, guiding constraints
   - **Road Model** — lane layout, coordinate system, simulation tick/time-step
   - **Car Model** — rectangle geometry, per-car properties (some fixed, some evolved)
   - **Car Behaviours** — speed control, lane changing with gap acceptance, entering/exiting
   - **Vision Model** — forward/rearward/lateral sensing, blind-spot zones, occlusion
   - **Neural-Network Interface** — exact list of INPUT nodes and OUTPUT nodes
   - **Simulation Lifecycle** — how a generation run proceeds (spawn → traverse → score → end)
   - **Fitness & Evaluation** — individual fitness formula, population-level metrics, species evaluation
   - **Population Diversity** — why heterogeneity is required and how it is encouraged
   - **Out of Scope** — explicit non-goals (physics engine, weather, turning, fuel, etc.)

2. Define the **19 INPUT nodes** covering: current speed, per-lane forward/rearward gaps and
   speed differentials (3 lanes × 4 readings = 12), lane-existence flags (2), blind-spot
   occupancy flags (2), normalised lane index (1), and proximity to on/off ramps (2).

3. Define the **4 OUTPUT nodes**: throttle (0–1), brake (0–1), lane-change-left intention
   (0–1), lane-change-right intention (0–1).

4. Write the evaluation section covering:
   - Individual fitness: throughput contribution + average speed ratio + collision penalty
   - Population evaluation: aggregate throughput, speed variance, collision rate per generation
   - Species evaluation: species average fitness with stagnation penalty mirroring
     `DefaultSpeciationStrategy.generationsSinceImprovement`

## Files to Change

- `docs/SIMULATION.md` — new file; the entire deliverable for this ticket

## Acceptance Criteria Checklist

- [ ] A file has been created explaining the major points of the simulation (road model, car
      model, behaviours, vision, lifecycle)
- [ ] A list of input and output nodes the simulation will use is documented (19 inputs, 4
      outputs, each with name, range, and description)
- [ ] An explanation of how the simulation would be run (how cars enter, traverse, and exit
      the simulation system)
- [ ] An explanation of how different populations will be evaluated (aggregate fitness formula,
      throughput, speed, collision metrics)
- [ ] An explanation of how different species will be evaluated (species average fitness,
      stagnation penalty, niche protection)

## Out of Scope

- Any Java implementation (that is a future ticket)
- Realistic physics: steering around corners, vehicle dynamics, suspension
- Environmental conditions: weather, road surface friction, lighting
- Multi-directional traffic or intersections
- Traffic signals or road rules beyond basic lane discipline
- Fuel consumption, emissions, or driver fatigue modelling
