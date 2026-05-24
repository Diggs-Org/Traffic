package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ActivationFunction;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;

/**
 * Feed-forward implementation of {@link NeuralNetwork}.
 *
 * <p>Performs a topological-sort activation pass over the network described by the
 * underlying {@link Genome}. Nodes are processed in dependency order (inputs first,
 * outputs last). Disabled connections are ignored. {@link com.ddiggs.neat.core.NodeType#BIAS}
 * nodes always contribute an activation of {@code 1.0}.
 *
 * <p>The supplied {@link ActivationFunction} is applied to every
 * {@link com.ddiggs.neat.core.NodeType#HIDDEN} and
 * {@link com.ddiggs.neat.core.NodeType#OUTPUT} node after summing weighted inputs and
 * adding the node's bias.
 *
 * <p><strong>Limitation:</strong> this implementation supports acyclic (feed-forward)
 * topologies only. Cyclic (recurrent) genomes are out of scope for this ticket.
 */
public class NeuralNetworkImpl implements NeuralNetwork {

    private final Genome genome;
    private final ActivationFunction activationFunction;

    /**
     * Constructs a {@code NeuralNetworkImpl}.
     *
     * @param genome             the genotype from which this phenotype is built; must not be {@code null}
     * @param activationFunction the activation function applied to hidden and output nodes;
     *                           must not be {@code null}
     */
    public NeuralNetworkImpl(Genome genome, ActivationFunction activationFunction) {
        this.genome = genome;
        this.activationFunction = activationFunction;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Input values are assigned to {@link com.ddiggs.neat.core.NodeType#INPUT} nodes in the
     * order they appear in the genome's node-gene list. The method then propagates activations
     * through the topology in topological order and returns the activations of all
     * {@link com.ddiggs.neat.core.NodeType#OUTPUT} nodes.
     *
     * @throws IllegalArgumentException if {@code inputs.length} does not match the number of
     *                                  INPUT nodes in the genome
     */
    @Override
    public double[] activate(double[] inputs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public Genome getGenome() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
