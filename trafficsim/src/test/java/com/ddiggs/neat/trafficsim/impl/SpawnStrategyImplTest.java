package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.trafficsim.Car;
import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SimulationConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SpawnStrategyImplTest {

    private SpawnStrategyImpl strategy;
    private SimulationConfig config;
    private Road road;

    @BeforeMethod
    public void setUp() {
        strategy = new SpawnStrategyImpl();
        config = SimulationConfigImpl.builder()
                .spawnIntervalTicks(20)
                .spawnClearDistance(80.0)
                .laneCount(3)
                .spawnSpeed(10.0)
                .build();
        road = new RoadImpl(config);
    }

    private Genome makeMinimalGenome() {
        List<NodeGeneImpl> nodes = new ArrayList<>();
        for (int i = 0; i < 19; i++) nodes.add(new NodeGeneImpl(i, NodeType.INPUT, 0.0));
        nodes.add(new NodeGeneImpl(19, NodeType.BIAS,   0.0));
        for (int i = 0; i < 4; i++)  nodes.add(new NodeGeneImpl(20 + i, NodeType.OUTPUT, 0.0));
        List<ConnectionGeneImpl> conns = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            conns.add(new ConnectionGeneImpl(i, i, 19, 20 + i, 0.5, true));
        }
        return new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
    }

    // -------------------------------------------------------------------------
    // shouldSpawn
    // -------------------------------------------------------------------------

    @Test
    public void testShouldSpawnAtTick0WithNoCars() {
        Assert.assertTrue(strategy.shouldSpawn(0, List.of(), config));
    }

    @Test
    public void testShouldSpawnOnIntervalTick() {
        Assert.assertTrue(strategy.shouldSpawn(20, List.of(), config));
        Assert.assertFalse(strategy.shouldSpawn(21, List.of(), config));
    }

    @Test
    public void testShouldNotSpawnWhenCarInSpawnZone() {
        CarImpl blockingCar;
        CarPhysicsImpl physics = new CarPhysicsImpl(4.0, 2.0, 3.0, 7.0, 25.0, 100.0, 60.0, 8.0, 2.0);
        CarStateImpl st = new CarStateImpl(50.0, 1.75, 10.0, 0); // X=50 < 80 (spawnClearDistance)
        NeuralNetwork fakeNet1 = new NeuralNetwork() {
            @Override public double[] activate(double[] i) { return new double[4]; }
            @Override public Genome getGenome() { return null; }
        };
        blockingCar = new CarImpl(0L, physics, st, fakeNet1, null);
        Assert.assertFalse(strategy.shouldSpawn(0, List.of(blockingCar), config));
    }

    @Test
    public void testShouldSpawnWhenCarBeyondClearDistance() {
        CarPhysicsImpl physics = new CarPhysicsImpl(4.0, 2.0, 3.0, 7.0, 25.0, 100.0, 60.0, 8.0, 2.0);
        CarStateImpl st = new CarStateImpl(100.0, 1.75, 10.0, 0); // X=100 >= 80
        NeuralNetwork fakeNet2 = new NeuralNetwork() {
            @Override public double[] activate(double[] i) { return new double[4]; }
            @Override public Genome getGenome() { return null; }
        };
        CarImpl clearedCar = new CarImpl(0L, physics, st, fakeNet2, null);
        Assert.assertTrue(strategy.shouldSpawn(0, List.of(clearedCar), config));
    }

    // -------------------------------------------------------------------------
    // spawnCars
    // -------------------------------------------------------------------------

    @Test
    public void testSpawnCarsOneCar() {
        List<Genome> pending = new ArrayList<>();
        pending.add(makeMinimalGenome());
        List<Car> spawned = strategy.spawnCars(pending, road, config);
        Assert.assertEquals(spawned.size(), 1);
        Assert.assertTrue(pending.isEmpty(), "genome should be consumed");
    }

    @Test
    public void testSpawnCarsUpToLaneCount() {
        List<Genome> pending = new ArrayList<>();
        for (int i = 0; i < 5; i++) pending.add(makeMinimalGenome());
        List<Car> spawned = strategy.spawnCars(pending, road, config);
        Assert.assertEquals(spawned.size(), 3, "should spawn at most laneCount=3 cars");
        Assert.assertEquals(pending.size(), 2, "remaining genomes should be preserved");
    }

    @Test
    public void testSpawnedCarsAtSpawnSpeed() {
        List<Genome> pending = new ArrayList<>();
        pending.add(makeMinimalGenome());
        List<Car> spawned = strategy.spawnCars(pending, road, config);
        Assert.assertEquals(spawned.get(0).getState().getVx(), 10.0, 1e-9);
    }

    @Test
    public void testSpawnedCarsAtX0() {
        List<Genome> pending = new ArrayList<>();
        pending.add(makeMinimalGenome());
        List<Car> spawned = strategy.spawnCars(pending, road, config);
        Assert.assertEquals(spawned.get(0).getState().getX(), 0.0, 1e-9);
    }

    @Test
    public void testEmptyPendingReturnsEmpty() {
        List<Car> spawned = strategy.spawnCars(new ArrayList<>(), road, config);
        Assert.assertTrue(spawned.isEmpty());
    }
}
