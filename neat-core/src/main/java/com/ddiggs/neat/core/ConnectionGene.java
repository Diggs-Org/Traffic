package com.ddiggs.neat.core;

/**
 * Represents a directed, weighted connection (synapse) between two nodes in a NEAT
 * {@link Genome}.
 *
 * <p>NEAT's key innovation: every structural mutation that creates a new connection is
 * assigned a globally unique <em>innovation number</em>. This historical marking allows
 * matching genes during crossover without requiring a costly topological analysis.
 *
 * <p>Connections can be {@linkplain #isEnabled() disabled} rather than deleted;
 * this preserves the historical record and allows re-enabling via mutation.
 */
public interface ConnectionGene extends Gene {

    /**
     * Returns the global innovation number assigned to this connection when it first
     * appeared in the population.
     *
     * @return a positive integer; globally unique per (fromNode, toNode) pair
     */
    int getInnovationNumber();

    /**
     * Returns the id of the source node.
     *
     * @return id of the node whose activation is sent along this connection
     */
    int getInNodeId();

    /**
     * Returns the id of the destination node.
     *
     * @return id of the node that receives the weighted signal
     */
    int getOutNodeId();

    /**
     * Returns the connection weight.
     *
     * <p>The weight is multiplied by the source node's activation before being
     * summed at the destination node.
     *
     * @return the synaptic weight; may be any finite double
     */
    double getWeight();

    /**
     * Returns whether this connection is currently active.
     *
     * <p>Disabled connections are retained in the genome for historical alignment
     * but do not contribute to the network's forward pass.
     *
     * @return {@code true} if the connection participates in activation; {@code false} if disabled
     */
    boolean isEnabled();
}
