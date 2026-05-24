package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;

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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public NodeType getNodeType() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** {@inheritDoc} */
    @Override
    public double getBias() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Encodes {@code id} (4 bytes), {@code NodeType} ordinal (4 bytes), and
     * {@code bias} (8 bytes) in little-endian order — 16 bytes total.
     */
    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code data} is {@code null} or not exactly 16 bytes
     */
    @Override
    public NodeGeneImpl fromBytes(byte[] data) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
