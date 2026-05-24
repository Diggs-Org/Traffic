package com.ddiggs.neat.core;

/**
 * Represents a neuron (node) within a NEAT {@link Genome}.
 *
 * <p>A {@code NodeGene} encodes:
 * <ul>
 *   <li>the node's role in the network ({@link NodeType}), and</li>
 *   <li>an optional bias value added to the node's net input before activation.</li>
 * </ul>
 *
 * <p>The node's {@linkplain Gene#getId() id} serves as its stable identifier across
 * genomes for crossover alignment.
 */
public interface NodeGene extends Gene {

    /**
     * Returns the functional role of this node in the network.
     *
     * @return the {@link NodeType} of this node; never {@code null}
     */
    NodeType getNodeType();

    /**
     * Returns the bias value for this node.
     *
     * <p>The bias is added to the weighted sum of inputs before the activation
     * function is applied. A bias of {@code 0.0} has no effect.
     *
     * @return the node's bias
     */
    double getBias();
}
