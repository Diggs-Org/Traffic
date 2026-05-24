package com.ddiggs.neat.core;

/**
 * The phenotype of a NEAT individual — an executable neural network constructed
 * from a {@link Genome}.
 *
 * <p>Implementations are responsible for propagating activations through the
 * network topology described by the underlying genome, respecting disabled
 * connections and applying node-level {@link ActivationFunction}s.
 *
 * <p>Network topology may be acyclic (feed-forward) or cyclic (recurrent),
 * depending on the mutations that have been applied over generations.
 */
public interface NeuralNetwork {

    /**
     * Runs a single forward pass through the network.
     *
     * <p>Input values are applied to {@link NodeType#INPUT} nodes in the order
     * they appear in the underlying genome's node-gene list (excluding bias nodes).
     * The returned array contains the activations of all {@link NodeType#OUTPUT}
     * nodes in the same order.
     *
     * @param inputs the observation vector; its length must match the number of
     *               input nodes in the network
     * @return the output activation vector; never {@code null}
     * @throws IllegalArgumentException if {@code inputs.length} does not match
     *                                  the expected input size
     */
    double[] activate(double[] inputs);

    /**
     * Returns the genome from which this network was constructed.
     *
     * @return the underlying {@link Genome}; never {@code null}
     */
    Genome getGenome();
}
