package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SensorInputImplTest {

    private SensorInputImpl si;

    @BeforeMethod
    public void setUp() {
        double[] v = new double[SensorInputImpl.SIZE];
        for (int i = 0; i < v.length; i++) v[i] = i * 0.05;
        si = new SensorInputImpl(v);
    }

    @Test
    public void testSizeConstant() {
        Assert.assertEquals(SensorInputImpl.SIZE, 19);
    }

    @Test
    public void testAllAccessors() {
        Assert.assertEquals(si.getCurrentSpeed(),           0.00, 1e-9);
        Assert.assertEquals(si.getGapAheadCurrent(),        0.05, 1e-9);
        Assert.assertEquals(si.getRelSpeedAheadCurrent(),   0.10, 1e-9);
        Assert.assertEquals(si.getGapBehindCurrent(),       0.15, 1e-9);
        Assert.assertEquals(si.getRelSpeedBehindCurrent(),  0.20, 1e-9);
        Assert.assertEquals(si.getGapAheadLeft(),           0.25, 1e-9);
        Assert.assertEquals(si.getRelSpeedAheadLeft(),      0.30, 1e-9);
        Assert.assertEquals(si.getGapBehindLeft(),          0.35, 1e-9);
        Assert.assertEquals(si.getRelSpeedBehindLeft(),     0.40, 1e-9);
        Assert.assertEquals(si.getGapAheadRight(),          0.45, 1e-9);
        Assert.assertEquals(si.getRelSpeedAheadRight(),     0.50, 1e-9);
        Assert.assertEquals(si.getGapBehindRight(),         0.55, 1e-9);
        Assert.assertEquals(si.getRelSpeedBehindRight(),    0.60, 1e-9);
        Assert.assertEquals(si.getLeftLaneExists(),         0.65, 1e-9);
        Assert.assertEquals(si.getRightLaneExists(),        0.70, 1e-9);
        Assert.assertEquals(si.getBlindSpotLeft(),          0.75, 1e-9);
        Assert.assertEquals(si.getBlindSpotRight(),         0.80, 1e-9);
        Assert.assertEquals(si.getLaneIndex(),              0.85, 1e-9);
        Assert.assertEquals(si.getMergeLockout(),           0.90, 1e-9);
    }

    @Test
    public void testToArrayIsDefensiveCopy() {
        double[] arr = si.toArray();
        arr[0] = 999.0;
        Assert.assertEquals(si.getCurrentSpeed(), 0.00, 1e-9);
    }

    @Test
    public void testToArrayLength() {
        Assert.assertEquals(si.toArray().length, SensorInputImpl.SIZE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrongLengthThrows() {
        new SensorInputImpl(new double[5]);
    }
}
