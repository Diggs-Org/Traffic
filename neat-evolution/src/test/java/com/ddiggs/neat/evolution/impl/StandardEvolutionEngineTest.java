package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.InnovationTrackerImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.core.InnovationTracker;
import com.ddiggs.neat.evolution.FitnessEvaluator;
import com.ddiggs.neat.evolution.Population;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tests for {@link StandardEvolutionEngine}.
 *
 * <p>All tests are currently failing because {@link StandardEvolutionEngine} methods throw
 * {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class StandardEvolutionEngineTest {

    private static final long SEED = 7L;

    private Genome genome1;
    private Genome genome2;
    private Genome genome3;
    private PopulationImpl generation0;
    private InnovationTracker tracker;
    private StandardEvolutionEngine engine;

    /** Simple fitness evaluator: returns the sum of all connection weights. */
    private static final FitnessEvaluator WEIGHT_SUM_EVALUATOR =
            genome -> genome.getConnectionGenes().stream()
                    .mapToDouble(ConnectionGene::getWeight)
                    .sum();

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        genome1 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true)));
        genome2 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 3.0, true)));
        genome3 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 2.0, true)));

        generation0 = new PopulationImpl(new ArrayList<>(List.of(genome1, genome2, genome3)), 0);

        tracker = new InnovationTrackerImpl();

        DefaultSpeciationStrategy speciation =
                new DefaultSpeciationStrategy(1.0, 1.0, 0.4, 3.0);
        TournamentSelectionStrategy selection =
                new TournamentSelectionStrategy(2, new Random(SEED));
        StandardCrossoverStrategy crossover =
                new StandardCrossoverStrategy(new Random(SEED));
        StandardMutationStrategy mutation = new StandardMutationStrategy(
                0.8, 0.05, 0.03, 0.05, 0.1, new Random(SEED));

        engine = new StandardEvolutionEngine(
                speciation, selection, crossover, mutation, tracker,
                0.75, 2, new Random(SEED));
    }

    // -------------------------------------------------------------------------
    // Basic output tests
    // -------------------------------------------------------------------------

    @Test
    public void testNextGeneration_returnsNonNullPopulation() {
        Population next = engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        Assert.assertNotNull(next, "nextGeneration() must return a non-null Population");
    }

    @Test
    public void testNextGeneration_returnsSameSizePopulation() {
        Population next = engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        Assert.assertEquals(next.getSize(), generation0.getSize(),
                "Next generation should have the same population size");
    }

    @Test
    public void testNextGeneration_generationNumberIncrementedByOne() {
        Population next = engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        Assert.assertEquals(next.getGeneration(), 1,
                "Next generation number should be current generation + 1");
    }

    @Test
    public void testNextGeneration_populationIsSpeciated() {
        Population next = engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        Assert.assertFalse(next.getSpecies().isEmpty(),
                "Next generation should have at least one species after speciation");
    }

    // -------------------------------------------------------------------------
    // Champion tests
    // -------------------------------------------------------------------------

    @Test
    public void testNextGeneration_championIsNonNull() {
        Population next = engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        Assert.assertNotNull(next.getChampion(),
                "Next generation must have a non-null champion genome");
    }

    @Test
    public void testNextGeneration_championHasHighestFitness() {
        // genome2 has weight 3.0, which is the highest under WEIGHT_SUM_EVALUATOR
        // The champion should come from the lineage of genome2
        // We check that the champion's connection weight sum >= all other genomes'
        Population next = engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        double championFitness = WEIGHT_SUM_EVALUATOR.evaluate(next.getChampion());
        for (Genome g : next.getGenomes()) {
            double gFitness = WEIGHT_SUM_EVALUATOR.evaluate(g);
            Assert.assertTrue(championFitness >= gFitness,
                    "Champion fitness (" + championFitness
                    + ") must be >= all other genomes' fitness (" + gFitness + ")");
        }
    }

    // -------------------------------------------------------------------------
    // Fitness evaluation coverage test
    // -------------------------------------------------------------------------

    @Test
    public void testNextGeneration_fitnessEvaluatorCalledForEachGenome() {
        int[] callCount = {0};
        FitnessEvaluator countingEvaluator = genome -> {
            callCount[0]++;
            return WEIGHT_SUM_EVALUATOR.evaluate(genome);
        };
        engine.nextGeneration(generation0, countingEvaluator);
        Assert.assertEquals(callCount[0], generation0.getSize(),
                "FitnessEvaluator must be called exactly once per genome");
    }

    // -------------------------------------------------------------------------
    // Innovation tracker reset test
    // -------------------------------------------------------------------------

    @Test
    public void testNextGeneration_resetsInnovationTrackerAtEndOfGeneration() {
        int innovBefore = tracker.getCurrentInnovationNumber();
        engine.nextGeneration(generation0, WEIGHT_SUM_EVALUATOR);
        // After the generation, getCurrentInnovationNumber() should be >= the before value
        // (counter never decreases) but the within-generation cache should be cleared.
        // We verify the counter is non-decreasing (not reset to 0).
        Assert.assertTrue(tracker.getCurrentInnovationNumber() >= innovBefore,
                "Innovation tracker counter must not decrease across generations");
    }

    // -------------------------------------------------------------------------
    // Minimal population tests
    // -------------------------------------------------------------------------

    @Test
    public void testNextGeneration_singleGenomePopulation() {
        PopulationImpl tiny = new PopulationImpl(new ArrayList<>(List.of(genome1)), 0);
        Population next = engine.nextGeneration(tiny, WEIGHT_SUM_EVALUATOR);
        Assert.assertNotNull(next, "nextGeneration() should handle a single-genome population");
        Assert.assertEquals(next.getSize(), 1,
                "Single-genome population should produce a single-genome next generation");
    }

    // -------------------------------------------------------------------------
    // Null / invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testNextGeneration_nullCurrent_throwsNullPointerException() {
        engine.nextGeneration(null, WEIGHT_SUM_EVALUATOR);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNextGeneration_nullEvaluator_throwsNullPointerException() {
        engine.nextGeneration(generation0, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullSpeciationStrategy_throwsNullPointerException() {
        new StandardEvolutionEngine(null,
                new TournamentSelectionStrategy(2, new Random(SEED)),
                new StandardCrossoverStrategy(new Random(SEED)),
                new StandardMutationStrategy(0.5, 0.05, 0.03, 0.05, 0.1, new Random(SEED)),
                tracker, 0.75, 2, new Random(SEED));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_crossoverRateAboveOne_throwsIllegalArgumentException() {
        new StandardEvolutionEngine(
                new DefaultSpeciationStrategy(1.0, 1.0, 0.4, 3.0),
                new TournamentSelectionStrategy(2, new Random(SEED)),
                new StandardCrossoverStrategy(new Random(SEED)),
                new StandardMutationStrategy(0.5, 0.05, 0.03, 0.05, 0.1, new Random(SEED)),
                tracker, 1.1, 2, new Random(SEED));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_elitismThresholdZero_throwsIllegalArgumentException() {
        new StandardEvolutionEngine(
                new DefaultSpeciationStrategy(1.0, 1.0, 0.4, 3.0),
                new TournamentSelectionStrategy(2, new Random(SEED)),
                new StandardCrossoverStrategy(new Random(SEED)),
                new StandardMutationStrategy(0.5, 0.05, 0.03, 0.05, 0.1, new Random(SEED)),
                tracker, 0.75, 0, new Random(SEED));
    }
}
