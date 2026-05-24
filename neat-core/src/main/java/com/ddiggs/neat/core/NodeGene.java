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

    /**
     * {@inheritDoc}
     *
     * <p>The returned instance is a {@code NodeGene} whose {@link #getNodeType()} and
     * {@link #getBias()} match the serialised values.
     *
     * @param data the byte array previously produced by {@link #toBytes()}; must not be {@code null}
     * @return a new {@code NodeGene} instance whose state matches the serialised form
     * @throws IllegalArgumentException if {@code data} is malformed or incompatible with this type
     */
    @Override
    NodeGene fromBytes(byte[] data);
}
