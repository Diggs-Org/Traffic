package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ConnectionGene;

/**
 * Immutable implementation of {@link ConnectionGene}.
 *
 * <h3>Serialisation format (25 bytes, little-endian)</h3>
 * <pre>
 *   bytes  0– 3 : int     id
 *   bytes  4– 7 : int     innovationNumber
 *   bytes  8–11 : int     inNodeId
 *   bytes 12–15 : int     outNodeId
 *   bytes 16–23 : double  weight
 *   byte     24 : byte    enabled (1 = true, 0 = false)
 * </pre>
 */
public class ConnectionGeneImpl implements ConnectionGene {

    private final int id;
    private final int innovationNumber;
    private final int inNodeId;
    private final int outNodeId;
    private final double weight;
    private final boolean enabled;

    /**
     * Constructs a new {@code ConnectionGeneImpl}.
     *
     * @param id               unique, non-negative gene identifier
     * @param innovationNumber globally unique innovation number for this connection
     * @param inNodeId         id of the source (pre-synaptic) node
     * @param outNodeId        id of the destination (post-synaptic) node
     * @param weight           synaptic weight; may be any finite double
     * @param enabled          {@code true} if this connection participates in activation
     */
    public ConnectionGeneImpl(int id, int innovationNumber, int inNodeId,
                              int outNodeId, double weight, boolean enabled) {
        this.id = id;
        this.innovationNumber = innovationNumber;
        this.inNodeId = inNodeId;
        this.outNodeId = outNodeId;
        this.weight = weight;
        this.enabled = enabled;
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public int getInnovationNumber() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public int getInNodeId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public int getOutNodeId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public double getWeight() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Encodes all six fields in little-endian order — 25 bytes total.
     */
    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code data} is {@code null} or not exactly 25 bytes
     */
    @Override
    public ConnectionGeneImpl fromBytes(byte[] data) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
