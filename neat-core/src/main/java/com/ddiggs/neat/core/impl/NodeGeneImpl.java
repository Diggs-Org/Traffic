package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Immutable implementation of {@link NodeGene}.
 *
 * <h3>Serialisation format (16 bytes, little-endian)</h3>
 * <pre>
 *   bytes  0–3  : int   id
 *   bytes  4–7  : int   NodeType ordinal (0=INPUT, 1=HIDDEN, 2=OUTPUT, 3=BIAS)
 *   bytes 8–15  : double bias
 * </pre>
 */
public class NodeGeneImpl implements NodeGene {

    /** Exact byte length produced and consumed by {@link #toBytes()} / {@link #fromBytes(byte[])}. */
    static final int BYTE_LENGTH = 16;

    private final int id;
    private final NodeType nodeType;
    private final double bias;

    /**
     * Constructs a new {@code NodeGeneImpl}.
     *
     * @param id       unique, non-negative gene identifier
     * @param nodeType the functional role of this node; must not be {@code null}
     * @param bias     bias value added to the node's net input before activation
     */
    public NodeGeneImpl(int id, NodeType nodeType, double bias) {
        this.id = id;
        this.nodeType = nodeType;
        this.bias = bias;
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    /** {@inheritDoc} */
    @Override
    public double getBias() {
        return bias;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Encodes {@code id} (4 bytes), {@code NodeType} ordinal (4 bytes), and
     * {@code bias} (8 bytes) in little-endian order — 16 bytes total.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(BYTE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(id);
        buf.putInt(nodeType.ordinal());
        buf.putDouble(bias);
        return buf.array();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code data} is {@code null} or not exactly 16 bytes
     */
    @Override
    public NodeGeneImpl fromBytes(byte[] data) {
        if (data == null || data.length != BYTE_LENGTH) {
            throw new IllegalArgumentException(
                    "NodeGeneImpl requires exactly " + BYTE_LENGTH + " bytes, got: "
                    + (data == null ? "null" : data.length));
        }
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int decodedId      = buf.getInt();
        int ordinal        = buf.getInt();
        NodeType[] values  = NodeType.values();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid NodeType ordinal: " + ordinal);
        }
        double decodedBias = buf.getDouble();
        return new NodeGeneImpl(decodedId, values[ordinal], decodedBias);
    }
}
