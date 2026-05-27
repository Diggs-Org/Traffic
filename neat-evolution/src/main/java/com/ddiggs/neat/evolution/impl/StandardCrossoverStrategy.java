package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.evolution.CrossoverStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * <p>The child is returned as a new {@link GenomeImpl};
 * neither parent is modified.
 */
public class StandardCrossoverStrategy implements CrossoverStrategy {

    private final Random random;

    /**
     * Constructs a {@code StandardCrossoverStrategy}.
     *
     * @param random source of randomness for matching-gene inheritance; must not be
     *               {@code null}
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public StandardCrossoverStrategy(Random random) {
        Objects.requireNonNull(random, "random must not be null");
        this.random = random;
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
        Objects.requireNonNull(parent1, "parent1 must not be null");
        Objects.requireNonNull(parent2, "parent2 must not be null");

        // Build innovation→gene map for parent2
        Map<Integer, ConnectionGene> parent2Genes = new HashMap<>();
        for (ConnectionGene cg : parent2.getConnectionGenes()) {
            parent2Genes.put(cg.getInnovationNumber(), cg);
        }

        List<ConnectionGene> childGenes = new ArrayList<>();

        // Walk parent1's genes; inherit matching randomly, disjoint/excess from parent1
        for (ConnectionGene p1Gene : parent1.getConnectionGenes()) {
            ConnectionGene p2Gene = parent2Genes.get(p1Gene.getInnovationNumber());
            if (p2Gene != null) {
                // Matching gene: 50/50 from either parent.
                // XOR two nextBoolean() calls to avoid LCG bias for small seeds.
                ConnectionGene chosen = (random.nextBoolean() ^ random.nextBoolean()) ? p1Gene : p2Gene;
                childGenes.add(new ConnectionGeneImpl(
                        chosen.getId(),
                        chosen.getInnovationNumber(),
                        chosen.getInNodeId(),
                        chosen.getOutNodeId(),
                        chosen.getWeight(),
                        chosen.isEnabled()));
            } else {
                // Disjoint or excess gene from parent1: always inherit
                childGenes.add(new ConnectionGeneImpl(
                        p1Gene.getId(),
                        p1Gene.getInnovationNumber(),
                        p1Gene.getInNodeId(),
                        p1Gene.getOutNodeId(),
                        p1Gene.getWeight(),
                        p1Gene.isEnabled()));
            }
        }

        // Node genes from parent1
        return new GenomeImpl(parent1.getNodeGenes(), childGenes);
    }
}
