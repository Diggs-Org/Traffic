package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.trafficsim.FitnessRecord;
import com.ddiggs.neat.trafficsim.SimulationConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CarImplTest {

    private CarPhysicsImpl physics;
    private CarStateImpl   state;
    private CarImpl        car;
    private SimulationConfig config;

    @BeforeMethod
    public void setUp() {
        physics = new CarPhysicsImpl(4.0, 2.0, 3.0, 7.0, 25.0, 100.0, 60.0, 8.0, 2.0);
        state   = new CarStateImpl(0.0, 5.25, 10.0, 1);

        NeuralNetwork fakeNet = new NeuralNetwork() {
            @Override public double[] activate(double[] i) { return new double[]{0.5, 0.5, 0.5, 0.5}; }
            @Override public Genome getGenome() { return null; }
        };
        car = new CarImpl(1L, physics, state, fakeNet, null);

        config = SimulationConfigImpl.builder()
                .mergeLockoutDistance(50.0)
                .stallThreshold(1.0)
                .stallGraceTicks(5)
                .highwayLength(1000.0)
                .build();
    }

    @Test
    public void testGetters() {
        Assert.assertEquals(car.getId(), 1L);
        Assert.assertSame(car.getPhysics(), physics);
        Assert.assertSame(car.getState(), state);
    }

    @Test
    public void testFrontX() {
        state.setX(100.0);
        Assert.assertEquals(car.frontX(), 102.0, 1e-9); // x + L/2 = 100 + 2
    }

    @Test
    public void testMergeLockAtSpawn() {
        Assert.assertTrue(car.isMergeLocked(config)); // x=0, spawnX=0, distance=0 < 50
    }

    @Test
    public void testMergeLockReleasedAfterDistance() {
        state.setX(60.0); // beyond 50m lockout from spawnX=0
        Assert.assertFalse(car.isMergeLocked(config));
    }

    @Test
    public void testNotStalledInitially() {
        Assert.assertFalse(car.isStalled(config));
    }

    @Test
    public void testStalledAfterGracePeriod() {
        state.stallTicks = 6; // > stallGraceTicks=5
        Assert.assertTrue(car.isStalled(config));
    }

    @Test
    public void testToFitnessRecord() {
        state.setX(500.0);
        state.sumVx = 100.0;
        state.incrementTicksAlive(); // ticksAlive = 1
        state.nearMissTicks = 2;
        state.incrementCollisions();
        state.exited = false;

        FitnessRecord r = car.toFitnessRecord();
        Assert.assertEquals(r.getFinalX(),       500.0, 1e-9);
        Assert.assertEquals(r.getAverageVx(),    100.0, 1e-9); // sumVx/ticksAlive = 100/1
        Assert.assertEquals(r.getNearMissCount(), 2);
        Assert.assertEquals(r.getCollisionCount(), 1);
        Assert.assertFalse(r.isExited());
    }
}
