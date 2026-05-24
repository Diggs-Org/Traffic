package com.ddiggs.neat.core;

/**
 * Base marker interface for all NEAT gene types.
 *
 * <p>Every gene in a {@link Genome} — whether a node or a connection — carries a
 * unique identifier that is stable across generations and used for alignment during
 * crossover and compatibility-distance calculations.
 */
public interface Gene {

    /**
     * Returns the unique identifier of this gene.
     *
     * @return a non-negative integer identifying this gene
     */
    int getId();
}
