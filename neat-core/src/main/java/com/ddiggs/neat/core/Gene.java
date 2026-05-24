package com.ddiggs.neat.core;

/**
 * Base marker interface for all NEAT gene types.
 *
 * <p>Every gene in a {@link Genome} — whether a node or a connection — carries a
 * unique identifier that is stable across generations and used for alignment during
 * crossover and compatibility-distance calculations.
 */
public interface Gene {

    /**
     * Returns the unique identifier of this gene.
     *
     * @return a non-negative integer identifying this gene
     */
    int getId();

    /**
     * Serializes this gene to a byte array for persistence or transmission.
     *
     * <p>The encoding must be self-contained so that it can be fully reconstructed
     * via {@link #fromBytes(byte[])} without any external context.
     *
     * @return a non-null byte array encoding all state of this gene
     */
    byte[] toBytes();

    /**
     * Deserializes a gene of the same concrete type from the supplied byte array.
     *
     * <p>This follows the <em>prototype</em> pattern: the method is dispatched on an
     * existing instance solely for type resolution; the returned object is entirely
     * independent of {@code this}.  Sub-interfaces should refine the return type
     * covariantly (e.g. {@code NodeGene fromBytes(byte[] data)}).
     *
     * @param data the byte array previously produced by {@link #toBytes()}; must not be {@code null}
     * @return a new {@code Gene} instance whose state matches the serialised form
     * @throws IllegalArgumentException if {@code data} is malformed or incompatible with this type
     */
    Gene fromBytes(byte[] data);
}
