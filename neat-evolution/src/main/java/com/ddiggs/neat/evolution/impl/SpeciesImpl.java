package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.Species;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        Objects.requireNonNull(representative, "representative must not be null");
        Objects.requireNonNull(members, "members must not be null");
        if (members.isEmpty()) {
            throw new IllegalArgumentException("members must not be empty");
        }
        if (generationsSinceImprovement < 0) {
            throw new IllegalArgumentException(
                    "generationsSinceImprovement must be non-negative, got: " + generationsSinceImprovement);
        }
        this.id = id;
        this.representative = representative;
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        this.sharedFitnessSum = sharedFitnessSum;
        this.bestFitness = bestFitness;
        this.generationsSinceImprovement = generationsSinceImprovement;
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public Genome getRepresentative() {
        return representative;
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of this species' member genomes
     */
    @Override
    public List<Genome> getMembers() {
        return members;
    }

    /** {@inheritDoc} */
    @Override
    public double getSharedFitnessSum() {
        return sharedFitnessSum;
    }

    /** {@inheritDoc} */
    @Override
    public double getBestFitness() {
        return bestFitness;
    }

    /** {@inheritDoc} */
    @Override
    public int getGenerationsSinceImprovement() {
        return generationsSinceImprovement;
    }
}
