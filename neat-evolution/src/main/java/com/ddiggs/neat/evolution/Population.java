package com.ddiggs.neat.evolution;

import com.ddiggs.neat.core.Genome;

import java.util.List;

/**
 * Manages the entire set of {@link Genome}s and {@link Species} that constitute one
 * generation of the NEAT evolutionary process.
 *
 * <p>A {@code Population} is the primary input and output of {@link EvolutionEngine#nextGeneration}.
 * It is read by strategies (e.g. {@link SpeciationStrategy}) and produced by the engine
 * at the end of each generation.
 */
public interface Population {

    /**
     * Returns an unmodifiable view of all genomes in this generation.
     *
     * @return list of genomes; never {@code null}
     */
    List<Genome> getGenomes();

    /**
     * Returns an unmodifiable view of all species in this generation.
     *
     * @return list of species; never {@code null}, may be empty before speciation
     */
    List<Species> getSpecies();

    /**
     * Returns the genome with the highest fitness in this generation.
     *
     * <p>Calling this before fitness evaluation returns an undefined result.
     *
     * @return the champion genome; never {@code null}
     */
    Genome getChampion();

    /**
     * Returns the total number of genomes in this generation.
     *
     * @return the population size; positive
     */
    int getSize();

    /**
     * Returns the zero-based generation index of this population.
     *
     * <p>Generation 0 is the initial (randomly generated) population.
     *
     * @return the generation number; non-negative
     */
    int getGeneration();
}
