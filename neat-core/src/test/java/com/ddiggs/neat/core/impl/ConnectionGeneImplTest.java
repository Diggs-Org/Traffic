package com.ddiggs.neat.core.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * TDD tests for {@link ConnectionGeneImpl}.
 *
 * <p>All tests in this class are expected to <strong>fail</strong> until Phase 2
 * provides the concrete implementation.
 */
public class ConnectionGeneImplTest {

    /** An enabled connection used as a baseline in most tests. */
    private ConnectionGeneImpl enabledConn;
    /** A disabled connection used for enabled-flag tests. */
    private ConnectionGeneImpl disabledConn;

    @BeforeMethod
    public void setUp() {
        enabledConn  = new ConnectionGeneImpl(10, 1, 0, 1, 0.5, true);
        disabledConn = new ConnectionGeneImpl(11, 2, 1, 2, -0.3, false);
    }

    // -------------------------------------------------------------------------
    // Accessor tests — enabled connection
    // -------------------------------------------------------------------------

    @Test
    public void testGetId_returnsConstructorValue() {
        Assert.assertEquals(enabledConn.getId(), 10);
    }

    @Test
    public void testGetInnovationNumber_returnsConstructorValue() {
        Assert.assertEquals(enabledConn.getInnovationNumber(), 1);
    }

    @Test
    public void testGetInNodeId_returnsConstructorValue() {
        Assert.assertEquals(enabledConn.getInNodeId(), 0);
    }

    @Test
    public void testGetOutNodeId_returnsConstructorValue() {
        Assert.assertEquals(enabledConn.getOutNodeId(), 1);
    }

    @Test
    public void testGetWeight_returnsConstructorValue() {
        Assert.assertEquals(enabledConn.getWeight(), 0.5, 1e-15);
    }

    @Test
    public void testIsEnabled_enabledConnection_returnsTrue() {
        Assert.assertTrue(enabledConn.isEnabled(),
                "isEnabled() must return true for an enabled connection");
    }

    // -------------------------------------------------------------------------
    // Accessor tests — disabled connection
    // -------------------------------------------------------------------------

    @Test
    public void testIsEnabled_disabledConnection_returnsFalse() {
        Assert.assertFalse(disabledConn.isEnabled(),
                "isEnabled() must return false for a disabled connection");
    }

    @Test
    public void testGetWeight_negativeWeight_returnsConstructorValue() {
        Assert.assertEquals(disabledConn.getWeight(), -0.3, 1e-15);
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    public void testGetWeight_zeroWeight_returnsZero() {
        ConnectionGeneImpl zeroWeight = new ConnectionGeneImpl(99, 5, 2, 3, 0.0, true);
        Assert.assertEquals(zeroWeight.getWeight(), 0.0, 1e-15);
    }

    @Test
    public void testGetWeight_largeWeight_preserved() {
        ConnectionGeneImpl heavy = new ConnectionGeneImpl(1, 1, 0, 1, Double.MAX_VALUE / 2, true);
        Assert.assertEquals(heavy.getWeight(), Double.MAX_VALUE / 2, 1e-10);
    }

    // -------------------------------------------------------------------------
    // Serialisation — toBytes()
    // -------------------------------------------------------------------------

    @Test
    public void testToBytes_returnsNonNull() {
        Assert.assertNotNull(enabledConn.toBytes(), "toBytes() must not return null");
    }

    @Test
    public void testToBytes_returns25Bytes() {
        Assert.assertEquals(enabledConn.toBytes().length, 25,
                "toBytes() must produce exactly 25 bytes");
    }

    @Test
    public void testToBytes_deterministicForSameValues() {
        ConnectionGeneImpl a = new ConnectionGeneImpl(1, 1, 0, 1, 0.5, true);
        ConnectionGeneImpl b = new ConnectionGeneImpl(1, 1, 0, 1, 0.5, true);
        Assert.assertEquals(a.toBytes(), b.toBytes(),
                "toBytes() must be deterministic for equal state");
    }

    @Test
    public void testToBytes_differentEnabledFlagProducesDifferentBytes() {
        byte[] enabledBytes  = enabledConn.toBytes();
        ConnectionGeneImpl same = new ConnectionGeneImpl(10, 1, 0, 1, 0.5, false);
        byte[] disabledBytes = same.toBytes();
        Assert.assertFalse(Arrays.equals(enabledBytes, disabledBytes),
                "Enabled and disabled versions of the same connection must produce different bytes");
    }

    // -------------------------------------------------------------------------
    // Serialisation — fromBytes() round-trips
    // -------------------------------------------------------------------------

    @Test
    public void testFromBytes_roundTrip_preservesId() {
        ConnectionGeneImpl restored = enabledConn.fromBytes(enabledConn.toBytes());
        Assert.assertEquals(restored.getId(), enabledConn.getId());
    }

    @Test
    public void testFromBytes_roundTrip_preservesInnovationNumber() {
        ConnectionGeneImpl restored = enabledConn.fromBytes(enabledConn.toBytes());
        Assert.assertEquals(restored.getInnovationNumber(), enabledConn.getInnovationNumber());
    }

    @Test
    public void testFromBytes_roundTrip_preservesInNodeId() {
        ConnectionGeneImpl restored = enabledConn.fromBytes(enabledConn.toBytes());
        Assert.assertEquals(restored.getInNodeId(), enabledConn.getInNodeId());
    }

    @Test
    public void testFromBytes_roundTrip_preservesOutNodeId() {
        ConnectionGeneImpl restored = enabledConn.fromBytes(enabledConn.toBytes());
        Assert.assertEquals(restored.getOutNodeId(), enabledConn.getOutNodeId());
    }

    @Test
    public void testFromBytes_roundTrip_preservesWeight() {
        ConnectionGeneImpl restored = enabledConn.fromBytes(enabledConn.toBytes());
        Assert.assertEquals(restored.getWeight(), enabledConn.getWeight(), 1e-15);
    }

    @Test
    public void testFromBytes_roundTrip_preservesEnabledTrue() {
        ConnectionGeneImpl restored = enabledConn.fromBytes(enabledConn.toBytes());
        Assert.assertTrue(restored.isEnabled());
    }

    @Test
    public void testFromBytes_roundTrip_preservesEnabledFalse() {
        ConnectionGeneImpl restored = disabledConn.fromBytes(disabledConn.toBytes());
        Assert.assertFalse(restored.isEnabled());
    }

    // -------------------------------------------------------------------------
    // Serialisation — fromBytes() error handling
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_nullData_throwsIllegalArgumentException() {
        enabledConn.fromBytes(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_emptyArray_throwsIllegalArgumentException() {
        enabledConn.fromBytes(new byte[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_tooShortArray_throwsIllegalArgumentException() {
        enabledConn.fromBytes(new byte[20]); // needs 25
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromBytes_tooLongArray_throwsIllegalArgumentException() {
        enabledConn.fromBytes(new byte[30]); // needs exactly 25
    }
}
