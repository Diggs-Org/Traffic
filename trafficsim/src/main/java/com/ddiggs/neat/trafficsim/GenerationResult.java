package com.ddiggs.neat.trafficsim;

import com.ddiggs.neat.core.Genome;

import java.util.Map;

/**
 * The complete results of a single generation run produced by {@link Simulation#runGeneration()}.
 *
 * <p>A {@code GenerationResult} maps every {@link Genome} in the evaluated
 * {@link com.ddiggs.neat.evolution.Population} to its {@link FitnessRecord}, and
 * exposes aggregate {@link PopulationMetrics} computed at the end of the run.
 *
 * <p>The fitness values returned here are the inputs to NEAT's selection and speciation
 * machinery: implementations of {@link com.ddiggs.neat.evolution.FitnessEvaluator} that
 * wrap the traffic simulation will extract per-genome scores from this object and return
 * them to the {@link com.ddiggs.neat.evolution.EvolutionEngine}.
 */
public interface GenerationResult {

    /**
     * Returns the {@link FitnessRecord} for the given genome.
     *
     * @param genome the genome whose fitness record is requested; never {@code null}
     * @return the fitness record for {@code genome}; never {@code null}
     * @throws IllegalArgumentException if {@code genome} was not part of the evaluated
     *                                  population
     */
    FitnessRecord getFitnessRecord(Genome genome);

    /**
     * Returns an unmodifiable view of the complete genome-to-fitness-record mapping.
     *
     * @return map of all genomes to their fitness records; never {@code null}
     */
    Map<Genome, FitnessRecord> getGenomeFitnessMap();

    /**
     * Returns the aggregate population-level metrics computed at the end of the run.
     *
     * <p>These metrics are recorded for research observation but do not feed back into
     * NEAT's selection loop (see {@code docs/SIMULATION.md} Section 8.2).
     *
     * @return population metrics; never {@code null}
     */
    PopulationMetrics getPopulationMetrics();

    /**
     * Returns the number of cars that successfully exited the highway during this run.
     *
     * <p>A car exits when its front edge reaches {@code highway_length}. This count
     * contributes to the {@link PopulationMetrics#getThroughput()} metric.
     *
     * @return successful exit count; non-negative
     */
    int getExitCount();

    /**
     * Returns the total number of cars that were spawned during this generation run.
     *
     * <p>This equals the size of the evaluated population, since every genome is
     * spawned exactly once per generation.
     *
     * @return total spawned car count; positive
     */
    int getSpawnedCount();

    /**
     * Returns the number of simulation ticks that elapsed during this generation run.
     *
     * <p>The run ends early (before {@link SimulationConfig#getMaxTicks()}) if all
     * cars are removed before the tick limit is reached.
     *
     * @return total ticks elapsed; positive
     */
    int getTotalTicks();
}
