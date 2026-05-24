package com.ddiggs.neat.training;

/**
 * Top-level orchestrator for a NEAT training run.
 *
 * <p>A {@code Trainer} wires together all components — {@link com.ddiggs.neat.evolution.EvolutionEngine},
 * {@link TrainingEnvironment}, {@link TrainingConfig}, and {@link TrainingCallback} — and
 * drives the generational loop until a stopping condition is met:
 * <ul>
 *   <li>The champion fitness reaches or exceeds {@link TrainingConfig#fitnessThreshold()}, or</li>
 *   <li>The number of generations reaches {@link TrainingConfig#maxGenerations()}.</li>
 * </ul>
 *
 * <p>The trainer is stateless with respect to the training run — all state lives in the
 * {@link com.ddiggs.neat.evolution.Population} passed between generations. This makes it
 * straightforward to pause, resume, or restart training.
 */
public interface Trainer {

    /**
     * Executes a complete NEAT training run and returns the final result.
     *
     * @param config   hyperparameters controlling the run; never {@code null}
     * @param env      the task environment used to evaluate genomes; never {@code null}
     * @param callback lifecycle event listener; never {@code null} (supply a no-op
     *                 if callbacks are not needed)
     * @return the outcome of the run including the champion genome; never {@code null}
     */
    TrainingResult train(TrainingConfig config, TrainingEnvironment env, TrainingCallback callback);
}
