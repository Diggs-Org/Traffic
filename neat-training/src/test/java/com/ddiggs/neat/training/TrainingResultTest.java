package com.ddiggs.neat.training;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for the {@link TrainingResult} record.
 */
public class TrainingResultTest {

    private static final int    GENERATIONS   = 42;
    private static final double BEST_FITNESS  = 3.75;

    private Genome champion;

    @BeforeMethod
    public void setUp() {
        champion = new GenomeImpl(
                List.of(new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                        new NodeGeneImpl(2, NodeType.OUTPUT, 0.0)),
                List.of(new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true)));
    }

    // -------------------------------------------------------------------------
    // Accessor tests
    // -------------------------------------------------------------------------

    @Test
    public void testChampion_returnsConstructorValue() {
        TrainingResult result = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        Assert.assertSame(result.champion(), champion,
                "champion() must return the exact object passed to the constructor");
    }

    @Test
    public void testGenerationsElapsed_returnsConstructorValue() {
        TrainingResult result = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        Assert.assertEquals(result.generationsElapsed(), GENERATIONS);
    }

    @Test
    public void testBestFitness_returnsConstructorValue() {
        TrainingResult result = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        Assert.assertEquals(result.bestFitness(), BEST_FITNESS, 1e-15);
    }

    // -------------------------------------------------------------------------
    // Equality and hash-code tests (record contract)
    // -------------------------------------------------------------------------

    @Test
    public void testEquals_sameValues_areEqual() {
        TrainingResult a = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        TrainingResult b = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        Assert.assertEquals(a, b);
    }

    @Test
    public void testEquals_differentGenerations_areNotEqual() {
        TrainingResult a = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        TrainingResult b = new TrainingResult(champion, GENERATIONS + 1, BEST_FITNESS);
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testEquals_differentBestFitness_areNotEqual() {
        TrainingResult a = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        TrainingResult b = new TrainingResult(champion, GENERATIONS, BEST_FITNESS + 1.0);
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testHashCode_sameValues_sameHashCode() {
        TrainingResult a = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        TrainingResult b = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    // -------------------------------------------------------------------------
    // toString test
    // -------------------------------------------------------------------------

    @Test
    public void testToString_containsFieldNames() {
        TrainingResult result = new TrainingResult(champion, GENERATIONS, BEST_FITNESS);
        String s = result.toString();
        Assert.assertTrue(s.contains("champion"),           "toString must contain 'champion'");
        Assert.assertTrue(s.contains("generationsElapsed"), "toString must contain 'generationsElapsed'");
        Assert.assertTrue(s.contains("bestFitness"),        "toString must contain 'bestFitness'");
    }
}
