package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CarStateImplTest {

    private CarStateImpl state;

    @BeforeMethod
    public void setUp() {
        state = new CarStateImpl(0.0, 5.25, 10.0, 1);
    }

    @Test
    public void testInitialState() {
        Assert.assertEquals(state.getX(),            0.0,  1e-9);
        Assert.assertEquals(state.getY(),            5.25, 1e-9);
        Assert.assertEquals(state.getVx(),           10.0, 1e-9);
        Assert.assertEquals(state.getLane(),         1);
        Assert.assertFalse(state.isMerging());
        Assert.assertEquals(state.getMergeTarget(),  -1);
        Assert.assertEquals(state.getMergeProgress(),0.0, 1e-9);
        Assert.assertEquals(state.getDistance(),     0.0, 1e-9);
        Assert.assertEquals(state.getCollisions(),   0);
        Assert.assertEquals(state.getTicksAlive(),   0);
    }

    @Test
    public void testSetters() {
        state.setX(100.0);
        state.setY(3.5);
        state.setVx(20.0);
        state.setLane(0);
        state.setMerging(true);
        state.setMergeTarget(2);
        state.setMergeProgress(0.5);
        state.addDistance(10.0);
        state.incrementCollisions();
        state.incrementTicksAlive();

        Assert.assertEquals(state.getX(),            100.0, 1e-9);
        Assert.assertEquals(state.getY(),            3.5,   1e-9);
        Assert.assertEquals(state.getVx(),           20.0,  1e-9);
        Assert.assertEquals(state.getLane(),         0);
        Assert.assertTrue(state.isMerging());
        Assert.assertEquals(state.getMergeTarget(),  2);
        Assert.assertEquals(state.getMergeProgress(),0.5, 1e-9);
        Assert.assertEquals(state.getDistance(),     10.0, 1e-9);
        Assert.assertEquals(state.getCollisions(),   1);
        Assert.assertEquals(state.getTicksAlive(),   1);
    }

    @Test
    public void testSpawnXSetAtConstruction() {
        CarStateImpl s = new CarStateImpl(50.0, 3.5, 5.0, 0);
        Assert.assertEquals(s.spawnX, 50.0, 1e-9);
    }

    @Test
    public void testSumVxAccumulator() {
        state.sumVx += 10.0;
        state.sumVx += 20.0;
        Assert.assertEquals(state.sumVx, 30.0, 1e-9);
    }

    @Test
    public void testAddDistanceAccumulates() {
        state.addDistance(5.0);
        state.addDistance(3.0);
        Assert.assertEquals(state.getDistance(), 8.0, 1e-9);
    }
}
