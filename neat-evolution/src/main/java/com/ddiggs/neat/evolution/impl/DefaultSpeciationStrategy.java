package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.SpeciationStrategy;

/**
 * Standard NEAT speciation strategy using compatibility distance.
 *
 * <p>Each genome in the population is compared against the representative of every
 * existing species via {@link com.ddiggs.neat.core.Genome#compatibilityDistance}.
 * If the distance is below {@code compatibilityThreshold} the genome joins that
 * species; otherwise a new species is created with the genome as its founding
 * representative.
 *
 * <p>Species IDs are stable: an existing species retains its ID across generations.
 * New species receive the next available auto-incremented ID. Empty species are
 * removed after assignment.
 *
 * <p>Fitness fields ({@code sharedFitnessSum}, {@code bestFitness},
 * {@code generationsSinceImprovement}) on the newly created {@link SpeciesImpl}
 * objects are initialised to zero; {@link StandardEvolutionEngine} updates them
 * after fitness evaluation.
 */
public class DefaultSpeciationStrategy implements SpeciationStrategy {

    /**
     * Constructs a {@code DefaultSpeciationStrategy}.
     *
     * @param c1                      excess-gene coefficient for compatibility distance
     * @param c2                      disjoint-gene coefficient for compatibility distance
     * @param c3                      weight-difference coefficient for compatibility distance
     * @param compatibilityThreshold  maximum distance for a genome to be considered part of
     *                                an existing species; positive
     * @throws IllegalArgumentException if {@code compatibilityThreshold} is not positive
     */
    public DefaultSpeciationStrategy(double c1,
                                     double c2,
                                     double c3,
                                     double compatibilityThreshold) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Casts {@code population} to {@link PopulationImpl} to update the species list
     * via the package-private {@code setSpecies} method.
     *
     * @throws ClassCastException if {@code population} is not a {@link PopulationImpl}
     */
    @Override
    public void speciate(Population population) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
