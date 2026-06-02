package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.Species;
import com.ddiggs.neat.trafficsim.GenerationResult;
import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SimulationConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SimulationImplTest {

    private SimulationImpl sim;
    private SimulationConfig config;
    private Road road;

    @BeforeMethod
    public void setUp() {
        sim = new SimulationImpl();
        config = SimulationConfigImpl.builder()
                .highwayLength(100.0)
                .laneCount(1)
                .laneWidth(3.5)
                .tickDt(0.1)
                .maxTicks(500)
                .vMaxAbsolute(50.0)
                .spawnIntervalTicks(1)
                .spawnClearDistance(5.0)
                .mergeLockoutDistance(10.0)
                .spawnSpeed(30.0)
                .stallThreshold(0.5)
                .stallGraceTicks(10)
                .build();
        road = new RoadImpl(config);
    }

    private Genome makeThrottleOnlyGenome() {
        // 19 inputs + bias + 4 outputs
        // bias(19) → throttle(20) with weight 5.0 → sigmoid(5) ≈ 0.993 (almost full throttle)
        List<NodeGeneImpl> nodes = new ArrayList<>();
        for (int i = 0; i < 19; i++) nodes.add(new NodeGeneImpl(i, NodeType.INPUT, 0.0));
        nodes.add(new NodeGeneImpl(19, NodeType.BIAS,   0.0));
        nodes.add(new NodeGeneImpl(20, NodeType.OUTPUT, 0.0));
        nodes.add(new NodeGeneImpl(21, NodeType.OUTPUT, 0.0));
        nodes.add(new NodeGeneImpl(22, NodeType.OUTPUT, 0.0));
        nodes.add(new NodeGeneImpl(23, NodeType.OUTPUT, 0.0));
        List<ConnectionGeneImpl> conns = new ArrayList<>();
        conns.add(new ConnectionGeneImpl(0, 0, 19, 20, 5.0, true));  // bias → throttle
        conns.add(new ConnectionGeneImpl(1, 1, 19, 21, -5.0, true)); // bias → brake (neg: low brake)
        conns.add(new ConnectionGeneImpl(2, 2, 19, 22, -5.0, true)); // bias → lane left (neg: no lane change)
        conns.add(new ConnectionGeneImpl(3, 3, 19, 23, -5.0, true)); // bias → lane right (neg)
        return new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
    }

    private Population makePopulation(List<Genome> genomes) {
        return new Population() {
            @Override public List<Genome>  getGenomes()    { return genomes; }
            @Override public List<Species> getSpecies()    { return List.of(); }
            @Override public Genome        getChampion()   { return genomes.isEmpty() ? null : genomes.get(0); }
            @Override public int           getGeneration() { return 0; }
            @Override public int           getSize()       { return genomes.size(); }
        };
    }

    // -------------------------------------------------------------------------
    // Basic lifecycle
    // -------------------------------------------------------------------------

    @Test
    public void testSingleCarRunProducesOneRecord() {
        Genome g = makeThrottleOnlyGenome();
        sim.setup(makePopulation(List.of(g)), road, config);
        GenerationResult result = sim.runGeneration();

        Assert.assertEquals(result.getSpawnedCount(), 1);
        Assert.assertEquals(result.getGenomeFitnessMap().size(), 1);
        Assert.assertNotNull(result.getFitnessRecord(g));
        Assert.assertNotNull(result.getPopulationMetrics());
    }

    @Test
    public void testCarExitsHighway() {
        // With highway=100m and high throttle, car should exit well within 500 ticks
        Genome g = makeThrottleOnlyGenome();
        sim.setup(makePopulation(List.of(g)), road, config);
        GenerationResult result = sim.runGeneration();

        Assert.assertEquals(result.getExitCount(), 1, "car should exit the highway");
        Assert.assertTrue(result.getFitnessRecord(g).isExited());
    }

    @Test
    public void testTickLimitTerminatesRun() {
        SimulationConfig tightConfig = SimulationConfigImpl.builder()
                .highwayLength(10000.0)  // very long highway; car cannot exit in 5 ticks
                .laneCount(1)
                .spawnIntervalTicks(1)
                .spawnClearDistance(5.0)
                .maxTicks(5)
                .spawnSpeed(10.0)
                .stallThreshold(0.0)
                .stallGraceTicks(1000)
                .build();
        Road longRoad = new RoadImpl(tightConfig);
        Genome g = makeThrottleOnlyGenome();
        sim.setup(makePopulation(List.of(g)), longRoad, tightConfig);
        GenerationResult result = sim.runGeneration();

        Assert.assertTrue(result.getTotalTicks() <= 5, "run must stop at maxTicks");
        Assert.assertEquals(result.getExitCount(), 0, "car should not have exited");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRunWithoutSetupThrows() {
        sim.runGeneration();
    }

    @Test
    public void testMultipleCarsProduceMultipleRecords() {
        List<Genome> genomes = new ArrayList<>();
        for (int i = 0; i < 3; i++) genomes.add(makeThrottleOnlyGenome());
        sim.setup(makePopulation(genomes), road, config);
        GenerationResult result = sim.runGeneration();

        Assert.assertEquals(result.getSpawnedCount(), 3);
        Assert.assertEquals(result.getGenomeFitnessMap().size(), 3);
    }
}
