package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.SimulationConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FitnessRecordImplTest {

    private SimulationConfig config;

    @BeforeMethod
    public void setUp() {
        config = SimulationConfigImpl.builder()
                .highwayLength(1000.0)
                .weightProgress(0.3)
                .weightSpeed(0.3)
                .weightExit(0.3)
                .weightCollision(0.5)
                .weightNearMiss(0.1)
                .build();
    }

    @Test
    public void testGetters() {
        FitnessRecordImpl r = new FitnessRecordImpl(500.0, 20.0, false, 1, 3, 25.0);
        Assert.assertEquals(r.getFinalX(),       500.0, 1e-9);
        Assert.assertEquals(r.getAverageVx(),    20.0,  1e-9);
        Assert.assertFalse(r.isExited());
        Assert.assertEquals(r.getCollisionCount(), 1);
        Assert.assertEquals(r.getNearMissCount(),  3);
    }

    @Test
    public void testComputeFitnessFullExit() {
        // x=1000, avgVx=25, exited, 0 collision, 0 near-miss
        FitnessRecordImpl r = new FitnessRecordImpl(1000.0, 25.0, true, 0, 0, 25.0);
        double expected = 0.3 * (1000.0 / 1000.0) + 0.3 * (25.0 / 25.0) + 0.3 * 1.0;
        Assert.assertEquals(r.computeFitness(config), expected, 1e-9);
    }

    @Test
    public void testComputeFitnessPartialNoExit() {
        // x=500, avgVx=20, not exited, 0 collision, 0 near-miss, vTarget=25
        FitnessRecordImpl r = new FitnessRecordImpl(500.0, 20.0, false, 0, 0, 25.0);
        double expected = 0.3 * (500.0 / 1000.0) + 0.3 * (20.0 / 25.0) + 0.3 * 0.0;
        Assert.assertEquals(r.computeFitness(config), expected, 1e-9);
    }

    @Test
    public void testComputeFitnessNegative() {
        // Lots of collisions should push fitness below zero
        FitnessRecordImpl r = new FitnessRecordImpl(0.0, 0.0, false, 5, 0, 25.0);
        double expected = 0.3 * 0 + 0.3 * 0 + 0.0 - 0.5 * 5;
        Assert.assertEquals(r.computeFitness(config), expected, 1e-9);
        Assert.assertTrue(r.computeFitness(config) < 0);
    }

    @Test
    public void testNearMissPenalty() {
        FitnessRecordImpl r = new FitnessRecordImpl(0.0, 0.0, false, 0, 10, 25.0);
        double expected = -0.1 * 10;
        Assert.assertEquals(r.computeFitness(config), expected, 1e-9);
    }
}
