package com.ddiggs.neat.evolution;

import com.ddiggs.neat.core.Genome;

/**
 * Evaluates the fitness of a {@link Genome} within a specific task domain.
 *
 * <p>Fitness is a non-negative scalar representing how well the network encoded by
 * the genome solves the target problem. Higher values indicate better performance.
 *
 * <p>This is a {@linkplain FunctionalInterface functional interface}. Concrete
 * implementations live in the {@code neat-reward} module (future) and are injected
 * into the {@link EvolutionEngine} at training time.
 *
 * <p>Implementations are expected to:
 * <ul>
 *   <li>Build a {@link com.ddiggs.neat.core.NeuralNetwork} phenotype from the genome.</li>
 *   <li>Run the network in the task environment for one or more episodes.</li>
 *   <li>Return a summarised fitness score (e.g. total reward, accuracy, time-alive).</li>
 * </ul>
 */
@FunctionalInterface
public interface FitnessEvaluator {

    /**
     * Evaluates the fitness of the given genome.
     *
     * @param genome the genome to evaluate; never {@code null}
     * @return a non-negative fitness score; higher is better
     */
    double evaluate(Genome genome);
}
