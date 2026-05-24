package com.ddiggs.neat.evolution;

import com.ddiggs.neat.core.Genome;

import java.util.List;

/**
 * Selects parent genomes from a {@link Species} for reproduction.
 *
 * <p>Implementations define how parents are chosen within a species — for example,
 * tournament selection, fitness-proportionate (roulette-wheel) selection, or
 * truncation selection (top-N only). The selected genomes are then passed to a
 * {@link CrossoverStrategy} or reproduced asexually via {@link MutationStrategy}.
 */
public interface SelectionStrategy {

    /**
     * Selects {@code count} parent genomes from the given species.
     *
     * <p>The returned list may contain duplicates if {@code count} exceeds the
     * number of viable candidates.
     *
     * @param species the species from which parents are drawn; never {@code null}
     * @param count   the number of parents to select; positive
     * @return an unmodifiable list of selected genomes; size equals {@code count}
     */
    List<Genome> select(Species species, int count);
}
