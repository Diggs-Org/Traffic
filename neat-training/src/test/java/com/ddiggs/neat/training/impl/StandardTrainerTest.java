package com.ddiggs.neat.training.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.InnovationTrackerImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.impl.DefaultSpeciationStrategy;
import com.ddiggs.neat.evolution.impl.PopulationImpl;
import com.ddiggs.neat.evolution.impl.StandardCrossoverStrategy;
import com.ddiggs.neat.evolution.impl.StandardEvolutionEngine;
import com.ddiggs.neat.evolution.impl.StandardMutationStrategy;
import com.ddiggs.neat.evolution.impl.TournamentSelectionStrategy;
import com.ddiggs.neat.training.TrainingCallback;
import com.ddiggs.neat.training.TrainingConfig;
import com.ddiggs.neat.training.TrainingEnvironment;
import com.ddiggs.neat.training.TrainingResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tests for {@link StandardTrainer}.
 *
 * <p>A minimal deterministic {@link TrainingEnvironment} stub is used: the environment
 * immediately signals done after a single step and always returns a fixed reward of
 * {@value FIXED_REWARD}. This makes fitness evaluation predictable regardless of network
 * weights.
 */
public class StandardTrainerTest {

    private static final long SEED = 42L;
    private static final double FIXED_REWARD = 1.0;
    private static final int MAX_GENERATIONS = 5;

    private StandardEvolutionEngine engine;
    private PopulationImpl initialPopulation;
    private TrainingConfig config;
    private TrainingEnvironment fixedEnv;
    private TrainingCallback noopCallback;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        Genome g1 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 0.5, true)));
        Genome g2 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true)));
        Genome g3 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, -0.5, true)));

        initialPopulation = new PopulationImpl(new ArrayList<>(List.of(g1, g2, g3)), 0);

        engine = new StandardEvolutionEngine(
                new DefaultSpeciationStrategy(1.0, 1.0, 0.4, 3.0),
                new TournamentSelectionStrategy(2, new Random(SEED)),
                new StandardCrossoverStrategy(new Random(SEED)),
                new StandardMutationStrategy(0.8, 0.05, 0.03, 0.05, 0.1, new Random(SEED)),
                new InnovationTrackerImpl(),
                0.75, 2, new Random(SEED));

        config = new TrainingConfig(3, MAX_GENERATIONS, Double.MAX_VALUE, 1.0, 0.1, 3);

        fixedEnv = new SingleStepEnvironment(FIXED_REWARD);

        noopCallback = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) {}
            @Override public void onTrainingComplete(TrainingResult result) {}
        };
    }

    // -------------------------------------------------------------------------
    // Basic output tests
    // -------------------------------------------------------------------------

    @Test
    public void testTrain_returnsNonNullResult() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        TrainingResult result = trainer.train(config, fixedEnv, noopCallback);
        Assert.assertNotNull(result, "train() must return a non-null TrainingResult");
    }

    @Test
    public void testTrain_resultChampionIsNonNull() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        TrainingResult result = trainer.train(config, fixedEnv, noopCallback);
        Assert.assertNotNull(result.champion(), "TrainingResult champion must not be null");
    }

    @Test
    public void testTrain_resultBestFitnessIsNonNegative() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        TrainingResult result = trainer.train(config, fixedEnv, noopCallback);
        Assert.assertTrue(result.bestFitness() >= 0.0,
                "Best fitness must be non-negative, got: " + result.bestFitness());
    }

    // -------------------------------------------------------------------------
    // Generation count tests
    // -------------------------------------------------------------------------

    @Test
    public void testTrain_stopsAtMaxGenerations() {
        int[] genCount = {0};
        TrainingCallback counter = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) { genCount[0]++; }
            @Override public void onTrainingComplete(TrainingResult result) {}
        };

        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(config, fixedEnv, counter);

        Assert.assertEquals(genCount[0], MAX_GENERATIONS,
                "onGenerationComplete must be called exactly maxGenerations times");
    }

    @Test
    public void testTrain_generationsElapsedMatchesCallbackCount() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        TrainingResult result = trainer.train(config, fixedEnv, noopCallback);
        Assert.assertEquals(result.generationsElapsed(), MAX_GENERATIONS,
                "generationsElapsed must equal maxGenerations when threshold is never reached");
    }

    // -------------------------------------------------------------------------
    // Early stopping test
    // -------------------------------------------------------------------------

    @Test
    public void testTrain_stopsEarlyWhenFitnessThresholdReached() {
        // Set threshold to FIXED_REWARD so the first generation should satisfy it
        TrainingConfig earlyStopConfig = new TrainingConfig(3, 100, FIXED_REWARD, 1.0, 0.1, 3);

        int[] genCount = {0};
        TrainingCallback counter = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) { genCount[0]++; }
            @Override public void onTrainingComplete(TrainingResult result) {}
        };

        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(earlyStopConfig, fixedEnv, counter);

        Assert.assertTrue(genCount[0] < 100,
                "Training should stop early when fitness threshold is reached, not run all 100 generations");
    }

    // -------------------------------------------------------------------------
    // Callback tests
    // -------------------------------------------------------------------------

    @Test
    public void testTrain_onTrainingCompleteCalledExactlyOnce() {
        int[] completeCount = {0};
        TrainingCallback counter = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) {}
            @Override public void onTrainingComplete(TrainingResult result) { completeCount[0]++; }
        };

        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(config, fixedEnv, counter);

        Assert.assertEquals(completeCount[0], 1,
                "onTrainingComplete must be called exactly once");
    }

    @Test
    public void testTrain_onTrainingCompleteReceivesNonNullResult() {
        TrainingResult[] captured = {null};
        TrainingCallback capturingCallback = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) {}
            @Override public void onTrainingComplete(TrainingResult result) { captured[0] = result; }
        };

        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(config, fixedEnv, capturingCallback);

        Assert.assertNotNull(captured[0], "onTrainingComplete must receive a non-null result");
    }

    @Test
    public void testTrain_callbackResultMatchesReturnedResult() {
        TrainingResult[] captured = {null};
        TrainingCallback capturingCallback = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) {}
            @Override public void onTrainingComplete(TrainingResult result) { captured[0] = result; }
        };

        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        TrainingResult returned = trainer.train(config, fixedEnv, capturingCallback);

        Assert.assertEquals(captured[0], returned,
                "Result passed to onTrainingComplete must equal the value returned by train()");
    }

    @Test
    public void testTrain_onGenerationCompleteReceivesNonNullPopulation() {
        List<Population> populations = new ArrayList<>();
        TrainingCallback capturingCallback = new TrainingCallback() {
            @Override public void onGenerationComplete(int gen, Population pop, double best) { populations.add(pop); }
            @Override public void onTrainingComplete(TrainingResult result) {}
        };

        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(config, fixedEnv, capturingCallback);

        for (Population p : populations) {
            Assert.assertNotNull(p, "onGenerationComplete must receive a non-null population");
        }
    }

    // -------------------------------------------------------------------------
    // Null / invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullEngine_throwsNullPointerException() {
        new StandardTrainer(null, initialPopulation);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullPopulation_throwsNullPointerException() {
        new StandardTrainer(engine, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testTrain_nullConfig_throwsNullPointerException() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(null, fixedEnv, noopCallback);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testTrain_nullEnv_throwsNullPointerException() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(config, null, noopCallback);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testTrain_nullCallback_throwsNullPointerException() {
        StandardTrainer trainer = new StandardTrainer(engine, initialPopulation);
        trainer.train(config, fixedEnv, null);
    }

    // -------------------------------------------------------------------------
    // Stub environment
    // -------------------------------------------------------------------------

    /** A deterministic single-step environment that always returns a fixed reward. */
    private static final class SingleStepEnvironment implements TrainingEnvironment {

        private final double reward;
        private boolean done;

        SingleStepEnvironment(double reward) {
            this.reward = reward;
        }

        @Override public void reset() { done = false; }
        @Override public double[] observe() { return new double[]{1.0}; }
        @Override public void step(double[] actions) { done = true; }
        @Override public boolean isDone() { return done; }
        @Override public double reward() { return reward; }
        @Override public int getObservationSize() { return 1; }
        @Override public int getActionSize() { return 1; }
    }
}
