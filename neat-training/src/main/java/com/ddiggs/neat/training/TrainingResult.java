package com.ddiggs.neat.training;

import com.ddiggs.neat.core.Genome;

/**
 * Immutable snapshot of the outcome of a completed training run.
 *
 * @param champion           the genome that achieved the highest fitness during
 *                           training; never {@code null}
 * @param generationsElapsed the number of generations executed before training
 *                           stopped (either by reaching {@link TrainingConfig#maxGenerations()}
 *                           or by exceeding {@link TrainingConfig#fitnessThreshold()});
 *                           non-negative
 * @param bestFitness        the fitness of the champion genome; non-negative
 */
public record TrainingResult(
        Genome champion,
        int generationsElapsed,
        double bestFitness
) {}
