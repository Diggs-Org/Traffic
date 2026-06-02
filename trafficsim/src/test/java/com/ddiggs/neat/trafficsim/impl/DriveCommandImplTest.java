package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DriveCommandImplTest {

    @Test
    public void testGetters() {
        DriveCommandImpl cmd = new DriveCommandImpl(new double[]{0.8, 0.2, 0.6, 0.1});
        Assert.assertEquals(cmd.getThrottle(),        0.8, 1e-9);
        Assert.assertEquals(cmd.getBrake(),           0.2, 1e-9);
        Assert.assertEquals(cmd.getLaneChangeLeft(),  0.6, 1e-9);
        Assert.assertEquals(cmd.getLaneChangeRight(), 0.1, 1e-9);
    }

    @Test
    public void testToArrayReturnsDefensiveCopy() {
        double[] original = {0.5, 0.5, 0.5, 0.5};
        DriveCommandImpl cmd = new DriveCommandImpl(original);
        double[] copy = cmd.toArray();
        copy[0] = 0.0;
        Assert.assertEquals(cmd.getThrottle(), 0.5, 1e-9);
    }

    @Test
    public void testInputMutationDoesNotAffectCmd() {
        double[] arr = {1.0, 0.0, 0.0, 0.0};
        DriveCommandImpl cmd = new DriveCommandImpl(arr);
        arr[0] = 0.0;
        Assert.assertEquals(cmd.getThrottle(), 1.0, 1e-9);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrongLengthThrows() {
        new DriveCommandImpl(new double[]{0.5, 0.5});
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullThrows() {
        new DriveCommandImpl(null);
    }
}
