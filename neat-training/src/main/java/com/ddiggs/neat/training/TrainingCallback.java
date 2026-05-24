package com.ddiggs.neat.training;

import com.ddiggs.neat.evolution.Population;

/**
 * Receives lifecycle events fired by a {@link Trainer} during a training run.
 *
 * <p>Implement this interface to log progress, visualise the evolving network,
 * persist checkpoints, or implement early-stopping logic outside the {@link Trainer}.
 *
 * <p>A no-op implementation can be supplied when callbacks are not needed:
 * <pre>{@code
 * TrainingCallback noop = new TrainingCallback() {
 *     public void onGenerationComplete(int gen, Population pop, double best) {}
 *     public void onTrainingComplete(TrainingResult result) {}
 * };
 * }</pre>
 */
public interface TrainingCallback {

    /**
     * Called at the end of each generation, after fitness evaluation and before
     * producing the next generation.
     *
     * @param generation  the zero-based index of the generation that just completed
     * @param population  the current population (post-evaluation, post-speciation);
     *                    never {@code null}
     * @param bestFitness the fitness of the champion genome in this generation;
     *                    non-negative
     */
    void onGenerationComplete(int generation, Population population, double bestFitness);

    /**
     * Called once when the training run finishes, whether by meeting the fitness
     * threshold or exhausting the maximum number of generations.
     *
     * @param result the final training outcome; never {@code null}
     */
    void onTrainingComplete(TrainingResult result);
}
