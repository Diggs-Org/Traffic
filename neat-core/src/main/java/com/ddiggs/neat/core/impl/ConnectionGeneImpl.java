package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ConnectionGene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    /** Exact byte length produced and consumed by {@link #toBytes()} / {@link #fromBytes(byte[])}. */
    static final int BYTE_LENGTH = 25;

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
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public int getInnovationNumber() {
        return innovationNumber;
    }

    /** {@inheritDoc} */
    @Override
    public int getInNodeId() {
        return inNodeId;
    }

    /** {@inheritDoc} */
    @Override
    public int getOutNodeId() {
        return outNodeId;
    }

    /** {@inheritDoc} */
    @Override
    public double getWeight() {
        return weight;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Encodes all six fields in little-endian order — 25 bytes total.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(BYTE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(id);
        buf.putInt(innovationNumber);
        buf.putInt(inNodeId);
        buf.putInt(outNodeId);
        buf.putDouble(weight);
        buf.put((byte) (enabled ? 1 : 0));
        return buf.array();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code data} is {@code null} or not exactly 25 bytes
     */
    @Override
    public ConnectionGeneImpl fromBytes(byte[] data) {
        if (data == null || data.length != BYTE_LENGTH) {
            throw new IllegalArgumentException(
                    "ConnectionGeneImpl requires exactly " + BYTE_LENGTH + " bytes, got: "
                    + (data == null ? "null" : data.length));
        }
        ByteBuffer buf       = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int decodedId        = buf.getInt();
        int decodedInnov     = buf.getInt();
        int decodedInNode    = buf.getInt();
        int decodedOutNode   = buf.getInt();
        double decodedWeight = buf.getDouble();
        boolean decodedEnabled = buf.get() == 1;
        return new ConnectionGeneImpl(decodedId, decodedInnov, decodedInNode,
                                      decodedOutNode, decodedWeight, decodedEnabled);
    }
}
