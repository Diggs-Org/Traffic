package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.CrossoverStrategy;

import java.util.Random;

/**
 * Standard NEAT crossover strategy.
 *
 * <p>Implements the canonical NEAT crossover algorithm:
 * <ol>
 *   <li>Align the parents' connection genes by innovation number.</li>
 *   <li>For each <em>matching</em> gene (present in both parents): randomly inherit
 *       from either parent with equal probability.</li>
 *   <li>For each <em>disjoint</em> or <em>excess</em> gene: always inherit from
 *       {@code parent1} (the fitter parent, by convention).</li>
 *   <li>Node genes are copied directly from {@code parent1}.</li>
 * </ol>
 *
 * <p>The child is returned as a new {@link com.ddiggs.neat.core.impl.GenomeImpl};
 * neither parent is modified.
 */
public class StandardCrossoverStrategy implements CrossoverStrategy {

    /**
     * Constructs a {@code StandardCrossoverStrategy}.
     *
     * @param random source of randomness for matching-gene inheritance; must not be
     *               {@code null}
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public StandardCrossoverStrategy(Random random) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code parent1} is treated as the fitter parent. Disjoint and excess genes
     * from {@code parent2} are discarded.
     *
     * @throws NullPointerException if either parent is {@code null}
     */
    @Override
    public Genome crossover(Genome parent1, Genome parent2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
