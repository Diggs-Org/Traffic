package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RoadImplTest {

    private RoadImpl road;

    @BeforeMethod
    public void setUp() {
        SimulationConfigImpl cfg = SimulationConfigImpl.builder()
                .highwayLength(1000.0)
                .laneCount(3)
                .laneWidth(3.5)
                .build();
        road = new RoadImpl(cfg);
    }

    @Test
    public void testGetHighwayLength() {
        Assert.assertEquals(road.getHighwayLength(), 1000.0, 1e-9);
    }

    @Test
    public void testGetLaneCount() {
        Assert.assertEquals(road.getLaneCount(), 3);
    }

    @Test
    public void testGetLaneWidth() {
        Assert.assertEquals(road.getLaneWidth(), 3.5, 1e-9);
    }

    @Test
    public void testLaneCenterY() {
        Assert.assertEquals(road.getLaneCenterY(0), 1.75,  1e-9);
        Assert.assertEquals(road.getLaneCenterY(1), 5.25,  1e-9);
        Assert.assertEquals(road.getLaneCenterY(2), 8.75,  1e-9);
    }

    @Test
    public void testLaneLowerY() {
        Assert.assertEquals(road.getLaneLowerY(0), 0.0, 1e-9);
        Assert.assertEquals(road.getLaneLowerY(1), 3.5, 1e-9);
    }

    @Test
    public void testLaneUpperY() {
        Assert.assertEquals(road.getLaneUpperY(0), 3.5, 1e-9);
        Assert.assertEquals(road.getLaneUpperY(2), 10.5,1e-9);
    }

    @Test
    public void testIsValidLane() {
        Assert.assertTrue(road.isValidLane(0));
        Assert.assertTrue(road.isValidLane(2));
        Assert.assertFalse(road.isValidLane(-1));
        Assert.assertFalse(road.isValidLane(3));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidLaneCenterThrows() {
        road.getLaneCenterY(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidLaneUpperThrows() {
        road.getLaneUpperY(10);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullConfigThrows() {
        new RoadImpl(null);
    }
}
