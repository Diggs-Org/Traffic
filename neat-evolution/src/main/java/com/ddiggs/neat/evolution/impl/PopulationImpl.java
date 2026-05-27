package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.Species;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Standard implementation of {@link Population} that supports in-place speciation.
 *
 * <p>Genomes are stored as an unmodifiable list (set at construction time and never
 * changed). Species are held in a mutable internal list so that
 * {@link DefaultSpeciationStrategy} can update them via the package-private
 * {@link #setSpecies(List)} method. The champion genome is recorded by
 * {@link StandardEvolutionEngine} after fitness evaluation via
 * {@link #setChampion(Genome)}.
 */
public class PopulationImpl implements Population {

    private final List<Genome> genomes;
    private final int generation;
    private List<Species> species;
    private Genome champion;

    /**
     * Constructs a new {@code PopulationImpl} with no species and no champion.
     *
     * @param genomes    all genomes in this generation; must not be {@code null}
     * @param generation zero-based generation index; non-negative
     * @throws NullPointerException     if {@code genomes} is {@code null}
     * @throws IllegalArgumentException if {@code generation} is negative
     */
    public PopulationImpl(List<Genome> genomes, int generation) {
        Objects.requireNonNull(genomes, "genomes must not be null");
        if (generation < 0) {
            throw new IllegalArgumentException("generation must be non-negative, got: " + generation);
        }
        this.genomes = Collections.unmodifiableList(new ArrayList<>(genomes));
        this.generation = generation;
        this.species = new ArrayList<>();
        this.champion = null;
    }

    /**
     * Replaces the current species list in place.
     *
     * <p>Called by {@link DefaultSpeciationStrategy} after grouping genomes into
     * species. The provided list is defensively copied.
     *
     * @param species updated species list; must not be {@code null}
     */
    void setSpecies(List<Species> species) {
        Objects.requireNonNull(species, "species must not be null");
        this.species = new ArrayList<>(species);
    }

    /**
     * Records the champion genome for this generation.
     *
     * <p>Called by {@link StandardEvolutionEngine} after fitness evaluation.
     *
     * @param champion the genome with the highest fitness; must not be {@code null}
     */
    void setChampion(Genome champion) {
        Objects.requireNonNull(champion, "champion must not be null");
        this.champion = champion;
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of all genomes in this generation
     */
    @Override
    public List<Genome> getGenomes() {
        return genomes;
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of all species; empty until speciation runs
     */
    @Override
    public List<Species> getSpecies() {
        return Collections.unmodifiableList(species);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} if called before {@link #setChampion(Genome)}.
     */
    @Override
    public Genome getChampion() {
        return champion;
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return genomes.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getGeneration() {
        return generation;
    }
}
