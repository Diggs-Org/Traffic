package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.Species;

import java.util.List;

/**
 * Immutable implementation of {@link Species}.
 *
 * <p>Stores all fields supplied at construction time. The member list is defensively
 * copied and wrapped in an unmodifiable view so callers cannot mutate the species
 * after creation.
 *
 * <p>Instances are normally produced by {@link DefaultSpeciationStrategy} (with
 * placeholder fitness values of zero) and then re-created by
 * {@link StandardEvolutionEngine} once per-genome fitness scores are available.
 */
public class SpeciesImpl implements Species {

    private final int id;
    private final Genome representative;
    private final List<Genome> members;
    private final double sharedFitnessSum;
    private final double bestFitness;
    private final int generationsSinceImprovement;

    /**
     * Constructs a new {@code SpeciesImpl}.
     *
     * @param id                        unique positive identifier for this species
     * @param representative            the genome used as the membership test representative;
     *                                  must not be {@code null}
     * @param members                   the genomes assigned to this species; must not be
     *                                  {@code null} and must not be empty
     * @param sharedFitnessSum          sum of adjusted (shared) fitness values; non-negative
     * @param bestFitness               highest raw fitness seen in this species; non-negative
     * @param generationsSinceImprovement number of consecutive generations without fitness
     *                                  improvement; non-negative
     * @throws IllegalArgumentException if {@code members} is empty or any value is invalid
     * @throws NullPointerException     if {@code representative} or {@code members} is {@code null}
     */
    public SpeciesImpl(int id,
                       Genome representative,
                       List<Genome> members,
                       double sharedFitnessSum,
                       double bestFitness,
                       int generationsSinceImprovement) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public Genome getRepresentative() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of this species' member genomes
     */
    @Override
    public List<Genome> getMembers() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public double getSharedFitnessSum() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public double getBestFitness() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public int getGenerationsSinceImprovement() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
