package com.ddiggs.neat.core;

import java.util.List;

/**
 * The genotype of a NEAT individual — a complete description of a neural network's
 * topology and parameterisation in terms of {@link NodeGene}s and {@link ConnectionGene}s.
 *
 * <p>A {@code Genome} is the unit of selection, mutation, and crossover in the NEAT
 * algorithm. The corresponding executable network (phenotype) is a {@link NeuralNetwork}
 * built from this genome by a factory in the evolution module.
 *
 * <p><strong>Compatibility distance</strong> measures how structurally different two
 * genomes are. It drives speciation: genomes with a distance below a configurable
 * threshold are grouped into the same {@code Species}. The standard NEAT formula is:
 * <pre>
 *   δ = (c1 × E / N) + (c2 × D / N) + c3 × W̄
 * </pre>
 * where {@code E} = excess genes, {@code D} = disjoint genes, {@code W̄} = average
 * weight difference of matching genes, and {@code N} normalises for genome size.
 * {@code c1}, {@code c2}, {@code c3} are tunable coefficients.
 */
public interface Genome {

    /**
     * Returns an ordered, unmodifiable view of this genome's node genes.
     *
     * @return list of {@link NodeGene}s; never {@code null}, may be empty
     */
    List<NodeGene> getNodeGenes();

    /**
     * Returns an ordered, unmodifiable view of this genome's connection genes.
     *
     * @return list of {@link ConnectionGene}s; never {@code null}, may be empty
     */
    List<ConnectionGene> getConnectionGenes();

    /**
     * Computes the compatibility distance between this genome and another.
     *
     * @param other the genome to compare against; must not be {@code null}
     * @param c1    coefficient for excess genes
     * @param c2    coefficient for disjoint genes
     * @param c3    coefficient for average weight difference of matching genes
     * @return a non-negative distance value; {@code 0.0} means the genomes are identical
     */
    double compatibilityDistance(Genome other, double c1, double c2, double c3);
}
