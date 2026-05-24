package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * TDD tests for {@link GenomeImpl}.
 *
 * <p>All tests in this class are expected to <strong>fail</strong> until Phase 2
 * provides the concrete implementation.
 *
 * <h3>NEAT compatibility-distance formula</h3>
 * <pre>
 *   δ = (c1 × E + c2 × D) / N   +   c3 × W̄
 * </pre>
 * {@code E} = excess genes, {@code D} = disjoint genes,
 * {@code W̄} = mean absolute weight difference of matching genes,
 * {@code N} = max(size_a, size_b, 1).
 */
public class GenomeImplTest {

    /**
     * Minimal genome: 1 input node, 1 output node, 1 enabled connection (innovation 1).
     * Used as the "base" genome in many tests.
     */
    private GenomeImpl minimalGenome;

    /**
     * Identical copy of {@code minimalGenome} — same structure, same weights.
     */
    private GenomeImpl identicalGenome;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT, 0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 1.0, true)
        );
        minimalGenome   = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
        identicalGenome = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
    }

    // -------------------------------------------------------------------------
    // getNodeGenes()
    // -------------------------------------------------------------------------

    @Test
    public void testGetNodeGenes_returnsNonNull() {
        Assert.assertNotNull(minimalGenome.getNodeGenes());
    }

    @Test
    public void testGetNodeGenes_hasExpectedSize() {
        Assert.assertEquals(minimalGenome.getNodeGenes().size(), 2);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetNodeGenes_isUnmodifiable() {
        // Must throw UnsupportedOperationException when caller tries to mutate
        minimalGenome.getNodeGenes().add(new NodeGeneImpl(99, NodeType.HIDDEN, 0.0));
    }

    // -------------------------------------------------------------------------
    // getConnectionGenes()
    // -------------------------------------------------------------------------

    @Test
    public void testGetConnectionGenes_returnsNonNull() {
        Assert.assertNotNull(minimalGenome.getConnectionGenes());
    }

    @Test
    public void testGetConnectionGenes_hasExpectedSize() {
        Assert.assertEquals(minimalGenome.getConnectionGenes().size(), 1);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetConnectionGenes_isUnmodifiable() {
        minimalGenome.getConnectionGenes().add(
                new ConnectionGeneImpl(99, 99, 0, 1, 0.0, true));
    }

    // -------------------------------------------------------------------------
    // Empty genome
    // -------------------------------------------------------------------------

    @Test
    public void testEmptyGenome_nodeGenesIsEmpty() {
        GenomeImpl empty = new GenomeImpl(new ArrayList<>(), new ArrayList<>());
        Assert.assertTrue(empty.getNodeGenes().isEmpty());
    }

    @Test
    public void testEmptyGenome_connectionGenesIsEmpty() {
        GenomeImpl empty = new GenomeImpl(new ArrayList<>(), new ArrayList<>());
        Assert.assertTrue(empty.getConnectionGenes().isEmpty());
    }

    // -------------------------------------------------------------------------
    // compatibilityDistance — zero for identical/same genomes
    // -------------------------------------------------------------------------

    @Test
    public void testCompatibilityDistance_sameInstance_returnsZero() {
        double d = minimalGenome.compatibilityDistance(minimalGenome, 1.0, 1.0, 1.0);
        Assert.assertEquals(d, 0.0, 1e-9,
                "Distance of a genome to itself must be 0.0");
    }

    @Test
    public void testCompatibilityDistance_identicalGenomes_returnsZero() {
        double d = minimalGenome.compatibilityDistance(identicalGenome, 1.0, 1.0, 1.0);
        Assert.assertEquals(d, 0.0, 1e-9,
                "Distance between structurally identical genomes must be 0.0");
    }

    // -------------------------------------------------------------------------
    // compatibilityDistance — positive for different genomes
    // -------------------------------------------------------------------------

    @Test
    public void testCompatibilityDistance_disjointGene_returnsPositive() {
        // genomeB has an extra connection with a higher innovation number → disjoint/excess
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT, 0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> extendedConns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 1.0, true),
                new ConnectionGeneImpl(1, 2, 0, 1, 0.5, true)  // innovation 2 — not in minimalGenome
        );
        GenomeImpl genomeB = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(extendedConns));

        double d = minimalGenome.compatibilityDistance(genomeB, 1.0, 1.0, 0.0);
        Assert.assertTrue(d > 0.0,
                "Genomes with different connection genes must have compatibility distance > 0");
    }

    @Test
    public void testCompatibilityDistance_differentWeights_returnsPositive() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT, 0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        // Same topology (innovation 1) but different weight
        List<ConnectionGene> diffWeightConns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, -1.0, true)
        );
        GenomeImpl diffWeight = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(diffWeightConns));

        double d = minimalGenome.compatibilityDistance(diffWeight, 0.0, 0.0, 1.0);
        Assert.assertTrue(d > 0.0,
                "Genomes with matching topology but different weights must have distance > 0 when c3 > 0");
    }

    // -------------------------------------------------------------------------
    // compatibilityDistance — symmetry
    // -------------------------------------------------------------------------

    @Test
    public void testCompatibilityDistance_isSymmetric() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT, 0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> extendedConns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 1.0, true),
                new ConnectionGeneImpl(1, 2, 0, 1, 0.5, true)
        );
        GenomeImpl genomeB = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(extendedConns));

        double dAB = minimalGenome.compatibilityDistance(genomeB, 1.0, 1.0, 1.0);
        double dBA = genomeB.compatibilityDistance(minimalGenome, 1.0, 1.0, 1.0);
        Assert.assertEquals(dAB, dBA, 1e-9,
                "compatibilityDistance must be symmetric: d(A,B) == d(B,A)");
    }

    // -------------------------------------------------------------------------
    // compatibilityDistance — coefficient scaling
    // -------------------------------------------------------------------------

    @Test
    public void testCompatibilityDistance_zeroCoefficients_returnsZero() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT, 0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> extendedConns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 9.0, true),  // very different weight
                new ConnectionGeneImpl(1, 2, 0, 1, 0.5, true)
        );
        GenomeImpl genomeB = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(extendedConns));

        double d = minimalGenome.compatibilityDistance(genomeB, 0.0, 0.0, 0.0);
        Assert.assertEquals(d, 0.0, 1e-9,
                "When all coefficients are 0, compatibility distance must be 0 regardless of gene differences");
    }

    // -------------------------------------------------------------------------
    // Serialisation — toBytes()
    // -------------------------------------------------------------------------

    @Test
    public void testToBytes_returnsNonNull() {
        Assert.assertNotNull(minimalGenome.toBytes(), "toBytes() must not return null");
    }

    @Test
    public void testToBytes_deterministicForSameValues() {
        byte[] a = minimalGenome.toBytes();
        byte[] b = identicalGenome.toBytes();
        Assert.assertEquals(a, b, "toBytes() must be deterministic for equivalent genome state");
    }

    // -------------------------------------------------------------------------
    // Serialisation — fromBytes() round-trips
    // -------------------------------------------------------------------------

    @Test
    public void testFromBytes_roundTrip_preservesNodeCount() {
        byte[] bytes = minimalGenome.toBytes();
        Genome restored = minimalGenome.fromBytes(bytes);
        Assert.assertEquals(restored.getNodeGenes().size(),
                minimalGenome.getNodeGenes().size(),
                "Round-trip must preserve the number of node genes");
    }

    @Test
    public void testFromBytes_roundTrip_preservesConnectionCount() {
        byte[] bytes = minimalGenome.toBytes();
        Genome restored = minimalGenome.fromBytes(bytes);
        Assert.assertEquals(restored.getConnectionGenes().size(),
                minimalGenome.getConnectionGenes().size(),
                "Round-trip must preserve the number of connection genes");
    }

    @Test
    public void testFromBytes_roundTrip_preservesNodeId() {
        Genome restored = minimalGenome.fromBytes(minimalGenome.toBytes());
        Assert.assertEquals(restored.getNodeGenes().get(0).getId(),
                minimalGenome.getNodeGenes().get(0).getId());
    }

    @Test
    public void testFromBytes_roundTrip_preservesConnectionWeight() {
        Genome restored = minimalGenome.fromBytes(minimalGenome.toBytes());
        Assert.assertEquals(
                restored.getConnectionGenes().get(0).getWeight(),
                minimalGenome.getConnectionGenes().get(0).getWeight(),
                1e-15);
    }

    @Test
    public void testFromBytes_roundTrip_emptyGenome() {
        GenomeImpl empty = new GenomeImpl(new ArrayList<>(), new ArrayList<>());
        Genome restored = empty.fromBytes(empty.toBytes());
        Assert.assertTrue(restored.getNodeGenes().isEmpty());
        Assert.assertTrue(restored.getConnectionGenes().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Serialisation — fromBytes() error handling
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_nullData_throwsIllegalArgumentException() {
        minimalGenome.fromBytes(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_tooShortArray_throwsIllegalArgumentException() {
        minimalGenome.fromBytes(new byte[2]); // too short to decode even node count
    }
}
