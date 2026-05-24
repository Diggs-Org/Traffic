package com.ddiggs.neat.training;

/**
 * Immutable configuration for a NEAT training run.
 *
 * <p>All parameters are supplied up-front and do not change during training.
 * Use the standard Java record copy-with pattern to derive modified configurations.
 *
 * @param populationSize           number of genomes per generation; must be positive
 * @param maxGenerations           upper bound on generations to run before stopping;
 *                                 must be positive
 * @param fitnessThreshold         training stops early when the champion genome reaches
 *                                 this fitness; must be non-negative
 * @param compatibilityThreshold   initial compatibility distance threshold for speciation;
 *                                 must be positive
 * @param compatibilityModifier    delta applied each generation to adjust the compatibility
 *                                 threshold towards {@code targetSpeciesCount}; must be
 *                                 non-negative
 * @param targetSpeciesCount       desired number of species; used with
 *                                 {@code compatibilityModifier} for dynamic threshold tuning;
 *                                 must be positive
 */
public record TrainingConfig(
        int populationSize,
        int maxGenerations,
        double fitnessThreshold,
        double compatibilityThreshold,
        double compatibilityModifier,
        int targetSpeciesCount
) {}
