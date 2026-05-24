package com.ddiggs.neat.evolution;

import com.ddiggs.neat.core.Genome;

import java.util.List;

/**
 * A group of genetically similar {@link Genome}s within a {@link Population}.
 *
 * <p>Speciation is one of NEAT's core mechanisms. By grouping similar genomes into
 * species, the algorithm protects structural innovations — a new topology is not
 * immediately forced to compete against highly-optimised simpler networks. Each
 * species is allotted offspring proportional to its total adjusted fitness.
 *
 * <p>Stagnant species (those that show no improvement for several generations) are
 * eligible for extinction to free up population slots for more promising lineages.
 */
public interface Species {

    /**
     * Returns the unique identifier of this species.
     *
     * @return a positive integer stable across generations
     */
    int getId();

    /**
     * Returns the representative genome used to test membership.
     *
     * <p>A candidate genome joins this species if its compatibility distance to
     * the representative is below the configured threshold. The representative is
     * typically chosen randomly from the previous generation's members.
     *
     * @return the representative {@link Genome}; never {@code null}
     */
    Genome getRepresentative();

    /**
     * Returns an unmodifiable view of all genomes currently assigned to this species.
     *
     * @return list of member genomes; never {@code null}, never empty
     */
    List<Genome> getMembers();

    /**
     * Returns the sum of adjusted (shared) fitness values across all members.
     *
     * <p>Adjusted fitness = raw fitness / species size. The sum is used to
     * calculate each species' share of the next generation's offspring.
     *
     * @return the total shared fitness; non-negative
     */
    double getSharedFitnessSum();

    /**
     * Returns the highest raw fitness achieved by any member of this species.
     *
     * @return the best fitness seen in this species so far; non-negative
     */
    double getBestFitness();

    /**
     * Returns the number of consecutive generations during which this species has
     * shown no improvement in best fitness.
     *
     * <p>Species exceeding the stagnation limit are candidates for elimination.
     *
     * @return a non-negative stagnation counter
     */
    int getGenerationsSinceImprovement();
}
