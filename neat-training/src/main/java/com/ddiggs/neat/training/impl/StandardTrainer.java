package com.ddiggs.neat.training.impl;

import com.ddiggs.neat.core.ActivationFunction;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.core.impl.NeuralNetworkImpl;
import com.ddiggs.neat.evolution.EvolutionEngine;
import com.ddiggs.neat.evolution.FitnessEvaluator;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.training.Trainer;
import com.ddiggs.neat.training.TrainingCallback;
import com.ddiggs.neat.training.TrainingConfig;
import com.ddiggs.neat.training.TrainingEnvironment;
import com.ddiggs.neat.training.TrainingResult;

import java.util.Objects;

/**
 * Standard implementation of {@link Trainer} that drives the full NEAT generational loop.
 *
 * <p>Each call to {@link #train} runs the following cycle until a stopping condition is met:
 * <ol>
 *   <li>Delegate one generational step to the injected {@link EvolutionEngine}, supplying a
 *       {@link FitnessEvaluator} built from the {@link TrainingEnvironment}.</li>
 *   <li>Track the best fitness seen across all genome evaluations in that generation.</li>
 *   <li>Notify {@link TrainingCallback#onGenerationComplete} with the generation index,
 *       the resulting {@link Population}, and the generation's best fitness.</li>
 *   <li>Stop early if the best fitness reaches {@link TrainingConfig#fitnessThreshold()}.</li>
 * </ol>
 *
 * <p>After the loop, {@link TrainingCallback#onTrainingComplete} is fired and a
 * {@link TrainingResult} is returned.
 *
 * <p>The fitness evaluator builds a {@link NeuralNetworkImpl} for each genome using
 * {@link Math#tanh} as the activation function, resets the environment, then runs the
 * standard reset → observe → activate → step → reward loop until the environment signals
 * done. The accumulated reward is the genome's fitness.
 */
public class StandardTrainer implements Trainer {

    private static final ActivationFunction TANH = Math::tanh;

    private final EvolutionEngine engine;
    private final Population initialPopulation;

    /**
     * Constructs a {@code StandardTrainer}.
     *
     * @param engine            the evolution engine that advances one generation at a time;
     *                          never {@code null}
     * @param initialPopulation the seed population for generation 0; never {@code null}
     */
    public StandardTrainer(EvolutionEngine engine, Population initialPopulation) {
        Objects.requireNonNull(engine, "engine must not be null");
        Objects.requireNonNull(initialPopulation, "initialPopulation must not be null");
        this.engine = engine;
        this.initialPopulation = initialPopulation;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    @Override
    public TrainingResult train(TrainingConfig config, TrainingEnvironment env, TrainingCallback callback) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(env, "env must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        double[] generationBest = {0.0};

        FitnessEvaluator evaluator = genome -> {
            double fitness = runEpisode(genome, env);
            if (fitness > generationBest[0]) {
                generationBest[0] = fitness;
            }
            return fitness;
        };

        Population current = initialPopulation;
        Genome champion = null;
        double bestFitness = 0.0;
        int generationsElapsed = 0;

        for (int gen = 0; gen < config.maxGenerations(); gen++) {
            generationBest[0] = 0.0;
            current = engine.nextGeneration(current, evaluator);
            generationsElapsed = gen + 1;

            double genBest = generationBest[0];
            if (champion == null || genBest > bestFitness) {
                bestFitness = genBest;
                champion = current.getChampion();
            }

            callback.onGenerationComplete(gen, current, genBest);

            if (bestFitness >= config.fitnessThreshold()) {
                break;
            }
        }

        if (champion == null) {
            champion = current.getChampion();
        }

        TrainingResult result = new TrainingResult(champion, generationsElapsed, bestFitness);
        callback.onTrainingComplete(result);
        return result;
    }

    private double runEpisode(Genome genome, TrainingEnvironment env) {
        NeuralNetwork network = new NeuralNetworkImpl(genome, TANH);
        env.reset();
        double totalReward = 0.0;
        while (!env.isDone()) {
            double[] obs = env.observe();
            double[] actions = network.activate(obs);
            env.step(actions);
            totalReward += env.reward();
        }
        return totalReward;
    }
}
