package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.trafficsim.FitnessRecord;
import com.ddiggs.neat.trafficsim.PopulationMetrics;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenerationResultImplTest {

    private Genome genome;
    private FitnessRecord record;
    private GenerationResultImpl result;
    private PopulationMetrics metrics;

    @BeforeMethod
    public void setUp() {
        genome = new GenomeImpl(
                new ArrayList<>(java.util.List.of(new NodeGeneImpl(0, NodeType.INPUT, 0.0))),
                new ArrayList<>());
        record = new FitnessRecordImpl(500.0, 20.0, false, 0, 0, 25.0);
        metrics = new PopulationMetricsImpl(0.5, 20.0, 3.0, 0.1, 0.05, 0.3);

        Map<Genome, FitnessRecord> map = new HashMap<>();
        map.put(genome, record);
        result = new GenerationResultImpl(map, metrics, 1, 2, 100);
    }

    @Test
    public void testGetFitnessRecord() {
        Assert.assertSame(result.getFitnessRecord(genome), record);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetFitnessRecordMissingGenomeThrows() {
        Genome other = new GenomeImpl(new ArrayList<>(), new ArrayList<>());
        result.getFitnessRecord(other);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetGenomeFitnessMapIsUnmodifiable() {
        result.getGenomeFitnessMap().put(genome, record);
    }

    @Test
    public void testAggregateFields() {
        Assert.assertEquals(result.getExitCount(),    1);
        Assert.assertEquals(result.getSpawnedCount(), 2);
        Assert.assertEquals(result.getTotalTicks(),   100);
        Assert.assertSame(result.getPopulationMetrics(), metrics);
    }
}
