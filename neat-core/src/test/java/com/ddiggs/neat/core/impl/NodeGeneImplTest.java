package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.NodeType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TDD tests for {@link NodeGeneImpl}.
 *
 * <p>All tests in this class are expected to <strong>fail</strong> until Phase 2
 * provides the concrete implementation.
 */
public class NodeGeneImplTest {

    // -------------------------------------------------------------------------
    // Accessor tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetId_returnsConstructorValue() {
        NodeGeneImpl gene = new NodeGeneImpl(42, NodeType.INPUT, 0.0);
        Assert.assertEquals(gene.getId(), 42);
    }

    @Test
    public void testGetId_zeroIsValid() {
        NodeGeneImpl gene = new NodeGeneImpl(0, NodeType.HIDDEN, 0.0);
        Assert.assertEquals(gene.getId(), 0);
    }

    @DataProvider(name = "nodeTypes")
    public Object[][] nodeTypes() {
        return new Object[][] {
            { NodeType.INPUT },
            { NodeType.HIDDEN },
            { NodeType.OUTPUT },
            { NodeType.BIAS }
        };
    }

    @Test(dataProvider = "nodeTypes")
    public void testGetNodeType_returnsConstructorValue(NodeType nodeType) {
        NodeGeneImpl gene = new NodeGeneImpl(1, nodeType, 0.0);
        Assert.assertEquals(gene.getNodeType(), nodeType,
                "getNodeType() must return the NodeType supplied to the constructor");
    }

    @Test
    public void testGetBias_returnsConstructorValue() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.HIDDEN, 0.75);
        Assert.assertEquals(gene.getBias(), 0.75, 1e-15);
    }

    @Test
    public void testGetBias_negativeValue_returnsConstructorValue() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.HIDDEN, -1.5);
        Assert.assertEquals(gene.getBias(), -1.5, 1e-15);
    }

    @Test
    public void testGetBias_zero_returnsZero() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.0);
        Assert.assertEquals(gene.getBias(), 0.0, 1e-15);
    }

    // -------------------------------------------------------------------------
    // Serialisation — toBytes()
    // -------------------------------------------------------------------------

    @Test
    public void testToBytes_returnsNonNull() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.5);
        Assert.assertNotNull(gene.toBytes(), "toBytes() must not return null");
    }

    @Test
    public void testToBytes_returns16Bytes() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.5);
        Assert.assertEquals(gene.toBytes().length, 16,
                "toBytes() must produce exactly 16 bytes (4 id + 4 ordinal + 8 bias)");
    }

    @Test
    public void testToBytes_deterministicForSameValues() {
        NodeGeneImpl a = new NodeGeneImpl(7, NodeType.OUTPUT, 0.25);
        NodeGeneImpl b = new NodeGeneImpl(7, NodeType.OUTPUT, 0.25);
        Assert.assertEquals(a.toBytes(), b.toBytes(),
                "toBytes() must return identical bytes for equivalent gene state");
    }

    @Test
    public void testToBytes_differentIdProducesDifferentBytes() {
        NodeGeneImpl a = new NodeGeneImpl(1, NodeType.INPUT, 0.0);
        NodeGeneImpl b = new NodeGeneImpl(2, NodeType.INPUT, 0.0);
        Assert.assertFalse(
                java.util.Arrays.equals(a.toBytes(), b.toBytes()),
                "Genes with different ids must produce different byte arrays");
    }

    // -------------------------------------------------------------------------
    // Serialisation — fromBytes() round-trips
    // -------------------------------------------------------------------------

    @Test
    public void testFromBytes_roundTrip_preservesId() {
        NodeGeneImpl original = new NodeGeneImpl(99, NodeType.HIDDEN, 0.1);
        NodeGeneImpl restored = original.fromBytes(original.toBytes());
        Assert.assertEquals(restored.getId(), 99);
    }

    @Test(dataProvider = "nodeTypes")
    public void testFromBytes_roundTrip_preservesNodeType(NodeType nodeType) {
        NodeGeneImpl original = new NodeGeneImpl(1, nodeType, 0.0);
        NodeGeneImpl restored = original.fromBytes(original.toBytes());
        Assert.assertEquals(restored.getNodeType(), nodeType,
                "Round-trip must preserve NodeType: " + nodeType);
    }

    @Test
    public void testFromBytes_roundTrip_preservesBias() {
        NodeGeneImpl original = new NodeGeneImpl(3, NodeType.OUTPUT, -0.333);
        NodeGeneImpl restored = original.fromBytes(original.toBytes());
        Assert.assertEquals(restored.getBias(), -0.333, 1e-15,
                "Round-trip must preserve bias value exactly (no floating-point loss)");
    }

    @Test
    public void testFromBytes_roundTrip_biasIsNodeGeneInstance() {
        NodeGeneImpl original = new NodeGeneImpl(1, NodeType.BIAS, 0.0);
        NodeGeneImpl restored = original.fromBytes(original.toBytes());
        Assert.assertNotNull(restored);
        Assert.assertEquals(restored.getNodeType(), NodeType.BIAS);
    }

    // -------------------------------------------------------------------------
    // Serialisation — fromBytes() error handling
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_nullData_throwsIllegalArgumentException() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.0);
        gene.fromBytes(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_emptyArray_throwsIllegalArgumentException() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.0);
        gene.fromBytes(new byte[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_tooShortArray_throwsIllegalArgumentException() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.0);
        gene.fromBytes(new byte[10]); // needs 16
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_tooLongArray_throwsIllegalArgumentException() {
        NodeGeneImpl gene = new NodeGeneImpl(1, NodeType.INPUT, 0.0);
        gene.fromBytes(new byte[20]); // needs exactly 16
    }
}
