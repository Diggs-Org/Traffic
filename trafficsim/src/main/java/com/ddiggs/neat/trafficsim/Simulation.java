package com.ddiggs.neat.trafficsim;

import com.ddiggs.neat.evolution.Population;

/**
 * Orchestrates the traffic simulation lifecycle for a single generation.
 *
 * <p>The simulation evaluates an entire NEAT {@link Population} by running all genomes
 * through a shared highway environment. The generational loop follows the ten-step
 * procedure defined in {@code docs/SIMULATION.md} Section 7:
 * <ol>
 *   <li>SPAWN — introduce new cars when the spawn interval and clearance conditions are met</li>
 *   <li>OBSERVE — compute the 19-element {@link SensorInput} for each active car</li>
 *   <li>ACTIVATE — call each car's {@link com.ddiggs.neat.core.NeuralNetwork#activate}</li>
 *   <li>ACT — apply {@link DriveCommand} outputs (throttle, brake, lane changes)</li>
 *   <li>ADVANCE — update positions: {@code x += vx * tick_dt}</li>
 *   <li>DETECT — check bounding-box overlaps; mark colliding cars</li>
 *   <li>EXIT — remove cars that have reached {@code highway_length}</li>
 *   <li>CULL — remove stalled or colliding cars</li>
 *   <li>SCORE — update per-car running fitness accumulators</li>
 *   <li>TICK++ — increment the tick counter</li>
 * </ol>
 *
 * <p>The run terminates when all cars have been removed or
 * {@link SimulationConfig#getMaxTicks()} is reached.
 */
public interface Simulation {

    /**
     * Prepares the simulation for a new generation run.
     *
     * <p>Clears any state from a previous run, initialises the highway, and builds the
     * spawn queue from the genomes in {@code population}. Must be called before
     * {@link #runGeneration()}.
     *
     * @param population the NEAT population to evaluate; never {@code null}
     * @param road       the road geometry to simulate on; never {@code null}
     * @param config     the simulation configuration for this run; never {@code null}
     */
    void setup(Population population, Road road, SimulationConfig config);

    /**
     * Executes the full generational run and returns the results.
     *
     * <p>The simulation loops through the ten-step tick procedure until all cars have
     * exited, collided, or been culled, or until the tick count reaches
     * {@link SimulationConfig#getMaxTicks()}. Returns a {@link GenerationResult} that
     * maps each genome to its {@link FitnessRecord} and exposes aggregate
     * {@link PopulationMetrics}.
     *
     * <p>{@link #setup(Population, Road, SimulationConfig)} must have been called before
     * this method.
     *
     * @return the complete results of the generation run; never {@code null}
     * @throws IllegalStateException if {@link #setup} has not been called
     */
    GenerationResult runGeneration();
}
