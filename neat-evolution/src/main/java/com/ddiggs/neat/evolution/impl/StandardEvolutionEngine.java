package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.InnovationTracker;
import com.ddiggs.neat.evolution.CrossoverStrategy;
import com.ddiggs.neat.evolution.EvolutionEngine;
import com.ddiggs.neat.evolution.FitnessEvaluator;
import com.ddiggs.neat.evolution.MutationStrategy;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.SelectionStrategy;
import com.ddiggs.neat.evolution.Species;
import com.ddiggs.neat.evolution.SpeciationStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Standard implementation of {@link EvolutionEngine} orchestrating one complete NEAT
 * generational cycle.
 */
public class StandardEvolutionEngine implements EvolutionEngine {

    private final SpeciationStrategy speciationStrategy;
    private final SelectionStrategy selectionStrategy;
    private final CrossoverStrategy crossoverStrategy;
    private final MutationStrategy mutationStrategy;
    private final InnovationTracker innovationTracker;
    private final double crossoverRate;
    private final int elitismThreshold;
    private final Random random;

    public StandardEvolutionEngine(SpeciationStrategy speciationStrategy,
                                   SelectionStrategy selectionStrategy,
                                   CrossoverStrategy crossoverStrategy,
                                   MutationStrategy mutationStrategy,
                                   InnovationTracker innovationTracker,
                                   double crossoverRate,
                                   int elitismThreshold,
                                   Random random) {
        Objects.requireNonNull(speciationStrategy, "speciationStrategy must not be null");
        Objects.requireNonNull(selectionStrategy, "selectionStrategy must not be null");
        Objects.requireNonNull(crossoverStrategy, "crossoverStrategy must not be null");
        Objects.requireNonNull(mutationStrategy, "mutationStrategy must not be null");
        Objects.requireNonNull(innovationTracker, "innovationTracker must not be null");
        Objects.requireNonNull(random, "random must not be null");
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("crossoverRate must be in [0, 1], got: " + crossoverRate);
        }
        if (elitismThreshold < 1) {
            throw new IllegalArgumentException("elitismThreshold must be >= 1, got: " + elitismThreshold);
        }
        this.speciationStrategy = speciationStrategy;
        this.selectionStrategy = selectionStrategy;
        this.crossoverStrategy = crossoverStrategy;
        this.mutationStrategy = mutationStrategy;
        this.innovationTracker = innovationTracker;
        this.crossoverRate = crossoverRate;
        this.elitismThreshold = elitismThreshold;
        this.random = random;
    }

    @Override
    public Population nextGeneration(Population current, FitnessEvaluator evaluator) {
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(evaluator, "evaluator must not be null");

        int popSize = current.getSize();

        // 1. Evaluate fitness for each genome
        Map<Genome, Double> fitnessMap = new HashMap<>();
        for (Genome g : current.getGenomes()) {
            fitnessMap.put(g, evaluator.evaluate(g));
        }

        // 2. Find champion (argmax fitness)
        Genome champion = current.getGenomes().stream()
                .max(Comparator.comparingDouble(fitnessMap::get))
                .orElse(current.getGenomes().get(0));

        // Record champion on the current population
        ((PopulationImpl) current).setChampion(champion);

        // 3. Speciate
        speciationStrategy.speciate(current);

        // 4. Update species fitness stats
        // Build previous-gen species map by ID for stagnation tracking
        List<Species> species = current.getSpecies();
        List<SpeciesImpl> updatedSpecies = new ArrayList<>();

        for (Species s : species) {
            List<Genome> members = s.getMembers();
            int size = members.size();

            double sharedFitnessSum = 0.0;
            double bestFitness = 0.0;
            for (Genome g : members) {
                double f = fitnessMap.getOrDefault(g, 0.0);
                sharedFitnessSum += f / size;
                if (f > bestFitness) bestFitness = f;
            }

            // Stagnation: compare to previous best (placeholder — use 0 for new species)
            int stagnation = bestFitness > s.getBestFitness() ? 0 : s.getGenerationsSinceImprovement() + 1;

            updatedSpecies.add(new SpeciesImpl(
                    s.getId(), s.getRepresentative(), members,
                    sharedFitnessSum, bestFitness, stagnation));
        }

        // 5. Compute offspring quotas proportional to sharedFitnessSum
        double totalSharedFitness = updatedSpecies.stream()
                .mapToDouble(SpeciesImpl::getSharedFitnessSum)
                .sum();

        int[] quotas = new int[updatedSpecies.size()];
        int assigned = 0;
        if (totalSharedFitness > 0.0) {
            for (int i = 0; i < updatedSpecies.size(); i++) {
                quotas[i] = (int) Math.floor(
                        updatedSpecies.get(i).getSharedFitnessSum() / totalSharedFitness * popSize);
                assigned += quotas[i];
            }
            // Distribute remainder to species with highest fractional share
            // (simple: give remaining slots to first species)
            for (int i = 0; assigned < popSize; i = (i + 1) % updatedSpecies.size()) {
                quotas[i]++;
                assigned++;
            }
        } else {
            // All fitness is zero: distribute evenly
            int base = popSize / updatedSpecies.size();
            for (int i = 0; i < updatedSpecies.size(); i++) quotas[i] = base;
            for (int i = 0; i < popSize % updatedSpecies.size(); i++) quotas[i]++;
        }

        // 6. Produce offspring
        List<Genome> offspring = new ArrayList<>(popSize);

        for (int si = 0; si < updatedSpecies.size(); si++) {
            SpeciesImpl s = updatedSpecies.get(si);
            int quota = quotas[si];
            if (quota == 0) continue;

            List<Genome> members = new ArrayList<>(s.getMembers());
            // Sort members by fitness descending (so index 0 = champion)
            members.sort((a, b) -> Double.compare(
                    fitnessMap.getOrDefault(b, 0.0),
                    fitnessMap.getOrDefault(a, 0.0)));

            int start = 0;
            // Elitism: preserve champion of large enough species
            if (members.size() >= elitismThreshold && quota > 0) {
                offspring.add(members.get(0));
                start = 1;
            }

            // Build species with sorted members for selection
            SpeciesImpl sortedSpecies = new SpeciesImpl(
                    s.getId(), s.getRepresentative(), members,
                    s.getSharedFitnessSum(), s.getBestFitness(), s.getGenerationsSinceImprovement());

            for (int i = start; i < quota; i++) {
                Genome child;
                if (members.size() >= 2 && random.nextDouble() < crossoverRate) {
                    // Crossover: select two parents
                    List<Genome> parents = selectionStrategy.select(sortedSpecies, 2);
                    child = crossoverStrategy.crossover(parents.get(0), parents.get(1));
                } else {
                    // Asexual: clone a parent
                    List<Genome> parents = selectionStrategy.select(sortedSpecies, 1);
                    child = parents.get(0);
                }
                child = mutationStrategy.mutate(child, innovationTracker);
                offspring.add(child);
            }
        }

        // If offspring count doesn't match popSize due to rounding edge-cases, pad/trim
        while (offspring.size() < popSize) {
            Genome last = offspring.get(offspring.size() - 1);
            offspring.add(mutationStrategy.mutate(last, innovationTracker));
        }
        while (offspring.size() > popSize) {
            offspring.remove(offspring.size() - 1);
        }

        // 7. Reset innovation tracker
        innovationTracker.reset();

        // 8. Build next generation population
        PopulationImpl nextPop = new PopulationImpl(offspring, current.getGeneration() + 1);

        // The champion is the current champion carried over via elitism.
        // We do NOT re-evaluate next-gen genomes here; the evaluator is called
        // exactly once per genome in the CURRENT generation only.
        nextPop.setChampion(champion);

        // Run speciation on next generation
        speciationStrategy.speciate(nextPop);

        return nextPop;
    }
}
