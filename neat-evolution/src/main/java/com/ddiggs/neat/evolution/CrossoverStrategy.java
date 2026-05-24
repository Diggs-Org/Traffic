package com.ddiggs.neat.evolution;

import com.ddiggs.neat.core.Genome;

/**
 * Produces an offspring {@link Genome} by combining genetic material from two parents.
 *
 * <p>NEAT crossover aligns the parents' genes by innovation number. Matching genes
 * (present in both parents) are inherited randomly from either parent. Excess and
 * disjoint genes are inherited from the fitter parent (or randomly if both parents
 * have equal fitness).
 */
public interface CrossoverStrategy {

    /**
     * Creates a child genome from two parent genomes.
     *
     * <p>The fitter parent should be passed as {@code parent1}; implementations
     * may use this ordering to resolve disjoint/excess gene inheritance.
     *
     * @param parent1 the fitter (or equally fit) parent; never {@code null}
     * @param parent2 the other parent; never {@code null}
     * @return a new offspring genome; never {@code null}
     */
    Genome crossover(Genome parent1, Genome parent2);
}
