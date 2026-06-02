package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public class CarPhysicsImplTest {

    private CarPhysicsImpl make() {
        return new CarPhysicsImpl(5.0, 2.0, 3.0, 7.0, 25.0, 100.0, 60.0, 8.0, 2.0);
    }

    @Test
    public void testGetters() {
        CarPhysicsImpl p = make();
        Assert.assertEquals(p.getLength(),              5.0,  1e-9);
        Assert.assertEquals(p.getWidth(),               2.0,  1e-9);
        Assert.assertEquals(p.getMaxAcceleration(),     3.0,  1e-9);
        Assert.assertEquals(p.getMaxDeceleration(),     7.0,  1e-9);
        Assert.assertEquals(p.getTargetSpeed(),         25.0, 1e-9);
        Assert.assertEquals(p.getVisionRange(),         100.0,1e-9);
        Assert.assertEquals(p.getAdjacentVisionRange(), 60.0, 1e-9);
        Assert.assertEquals(p.getMinGap(),              8.0,  1e-9);
        Assert.assertEquals(p.getLaneChangeTime(),      2.0,  1e-9);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroLengthThrows() {
        new CarPhysicsImpl(0.0, 2.0, 3.0, 7.0, 25.0, 100.0, 60.0, 8.0, 2.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeTargetSpeedThrows() {
        new CarPhysicsImpl(5.0, 2.0, 3.0, 7.0, -1.0, 100.0, 60.0, 8.0, 2.0);
    }

    @Test
    public void testRandomInBounds() {
        Random rng = new Random(42);
        for (int i = 0; i < 100; i++) {
            CarPhysicsImpl p = CarPhysicsImpl.random(rng);
            Assert.assertTrue(p.getLength()          >= 3.0  && p.getLength()          <= 8.0);
            Assert.assertTrue(p.getWidth()           >= 1.8  && p.getWidth()           <= 2.5);
            Assert.assertTrue(p.getMaxAcceleration() >= 2.0  && p.getMaxAcceleration() <= 6.0);
            Assert.assertTrue(p.getMaxDeceleration() >= 4.0  && p.getMaxDeceleration() <= 10.0);
            Assert.assertTrue(p.getTargetSpeed()     >= 20.0 && p.getTargetSpeed()     <= 35.0);
            Assert.assertTrue(p.getVisionRange()     >= 50.0 && p.getVisionRange()     <= 200.0);
            Assert.assertTrue(p.getMinGap()          >= 5.0  && p.getMinGap()          <= 15.0);
            Assert.assertTrue(p.getLaneChangeTime()  >= 1.5  && p.getLaneChangeTime()  <= 3.0);
        }
    }
}
