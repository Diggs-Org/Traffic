package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PopulationMetricsImplTest {

    @Test
    public void testGetters() {
        PopulationMetricsImpl m = new PopulationMetricsImpl(0.8, 25.0, 3.5, 0.1, 0.05, 0.4);
        Assert.assertEquals(m.getThroughput(),    0.8,  1e-9);
        Assert.assertEquals(m.getAverageSpeed(),  25.0, 1e-9);
        Assert.assertEquals(m.getSpeedVariance(), 3.5,  1e-9);
        Assert.assertEquals(m.getCollisionRate(), 0.1,  1e-9);
        Assert.assertEquals(m.getNearMissRate(),  0.05, 1e-9);
        Assert.assertEquals(m.getDiversityIndex(),0.4,  1e-9);
    }

    @Test
    public void testZeroValues() {
        PopulationMetricsImpl m = new PopulationMetricsImpl(0, 0, 0, 0, 0, 0);
        Assert.assertEquals(m.getThroughput(), 0.0, 1e-9);
    }
}
