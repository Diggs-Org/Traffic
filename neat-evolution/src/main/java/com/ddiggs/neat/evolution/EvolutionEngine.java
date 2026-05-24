package com.ddiggs.neat.evolution;

/**
 * Orchestrates one complete generational cycle of the NEAT evolutionary algorithm.
 *
 * <p>A single call to {@link #nextGeneration} performs, in order:
 * <ol>
 *   <li>Fitness evaluation of every genome via the supplied {@link FitnessEvaluator}.</li>
 *   <li>Speciation via the configured {@link SpeciationStrategy}.</li>
 *   <li>Offspring allocation — each species is awarded a number of offspring
 *       proportional to its total adjusted fitness.</li>
 *   <li>Elitism — the champion of sufficiently large species survives unchanged.</li>
 *   <li>Parent selection via {@link SelectionStrategy}.</li>
 *   <li>Crossover via {@link CrossoverStrategy} (with probability) or asexual reproduction.</li>
 *   <li>Mutation via {@link MutationStrategy}.</li>
 *   <li>Replacement — the new offspring form the next generation's {@link Population}.</li>
 * </ol>
 *
 * <p>Implementations hold references to all strategy dependencies and the
 * {@link com.ddiggs.neat.core.InnovationTracker}.
 */
public interface EvolutionEngine {

    /**
     * Produces the next generation from the current population.
     *
     * @param current   the current generation's population; never {@code null}
     * @param evaluator the fitness function for this task; never {@code null}
     * @return a new {@link Population} representing the next generation; never {@code null}
     */
    Population nextGeneration(Population current, FitnessEvaluator evaluator);
}
