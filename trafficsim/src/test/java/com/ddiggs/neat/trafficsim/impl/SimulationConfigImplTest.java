package com.ddiggs.neat.trafficsim.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimulationConfigImplTest {

    @Test
    public void testDefaults() {
        SimulationConfigImpl cfg = SimulationConfigImpl.builder().build();
        Assert.assertEquals(cfg.getHighwayLength(),       1000.0, 1e-9);
        Assert.assertEquals(cfg.getLaneCount(),           3);
        Assert.assertEquals(cfg.getLaneWidth(),           3.5,  1e-9);
        Assert.assertEquals(cfg.getTickDt(),              0.1,  1e-9);
        Assert.assertEquals(cfg.getMaxTicks(),            2000);
        Assert.assertEquals(cfg.getVMaxAbsolute(),        50.0, 1e-9);
        Assert.assertEquals(cfg.getSpawnIntervalTicks(),  20);
        Assert.assertEquals(cfg.getSpawnClearDistance(),  80.0, 1e-9);
        Assert.assertEquals(cfg.getMergeLockoutDistance(),50.0, 1e-9);
        Assert.assertEquals(cfg.getSpawnSpeed(),          10.0, 1e-9);
        Assert.assertEquals(cfg.getStallThreshold(),      1.0,  1e-9);
        Assert.assertEquals(cfg.getStallGraceTicks(),     30);
        Assert.assertEquals(cfg.getWeightProgress(),      0.3,  1e-9);
        Assert.assertEquals(cfg.getWeightSpeed(),         0.3,  1e-9);
        Assert.assertEquals(cfg.getWeightExit(),          0.3,  1e-9);
        Assert.assertEquals(cfg.getWeightCollision(),     0.5,  1e-9);
        Assert.assertEquals(cfg.getWeightNearMiss(),      0.1,  1e-9);
    }

    @Test
    public void testCustomValues() {
        SimulationConfigImpl cfg = SimulationConfigImpl.builder()
                .highwayLength(500.0)
                .laneCount(2)
                .maxTicks(100)
                .weightCollision(1.0)
                .build();
        Assert.assertEquals(cfg.getHighwayLength(), 500.0, 1e-9);
        Assert.assertEquals(cfg.getLaneCount(),     2);
        Assert.assertEquals(cfg.getMaxTicks(),      100);
        Assert.assertEquals(cfg.getWeightCollision(),1.0, 1e-9);
        Assert.assertEquals(cfg.getLaneWidth(),     3.5, 1e-9);
    }

    @Test
    public void testBuilderReturnsNewInstance() {
        SimulationConfigImpl a = SimulationConfigImpl.builder().highwayLength(100).build();
        SimulationConfigImpl b = SimulationConfigImpl.builder().highwayLength(200).build();
        Assert.assertNotSame(a, b);
        Assert.assertEquals(a.getHighwayLength(), 100.0, 1e-9);
        Assert.assertEquals(b.getHighwayLength(), 200.0, 1e-9);
    }
}
