package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.Species;
import com.ddiggs.neat.evolution.SpeciationStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard NEAT speciation strategy using compatibility distance.
 *
 * <p>Each genome in the population is compared against the representative of every
 * existing species via {@link com.ddiggs.neat.core.Genome#compatibilityDistance}.
 * If the distance is below {@code compatibilityThreshold} the genome joins that
 * species; otherwise a new species is created with the genome as its founding
 * representative.
 *
 * <p>Species IDs are stable: an existing species retains its ID across generations.
 * New species receive the next available auto-incremented ID. Empty species are
 * removed after assignment.
 *
 * <p>Fitness fields ({@code sharedFitnessSum}, {@code bestFitness},
 * {@code generationsSinceImprovement}) on the newly created {@link SpeciesImpl}
 * objects are initialised to zero; {@link StandardEvolutionEngine} updates them
 * after fitness evaluation.
 */
public class DefaultSpeciationStrategy implements SpeciationStrategy {

    private final double c1;
    private final double c2;
    private final double c3;
    private final double compatibilityThreshold;
    private int nextSpeciesId;

    /**
     * Constructs a {@code DefaultSpeciationStrategy}.
     *
     * @param c1                      excess-gene coefficient for compatibility distance
     * @param c2                      disjoint-gene coefficient for compatibility distance
     * @param c3                      weight-difference coefficient for compatibility distance
     * @param compatibilityThreshold  maximum distance for a genome to be considered part of
     *                                an existing species; positive
     * @throws IllegalArgumentException if {@code compatibilityThreshold} is not positive
     */
    public DefaultSpeciationStrategy(double c1,
                                     double c2,
                                     double c3,
                                     double compatibilityThreshold) {
        if (compatibilityThreshold <= 0.0) {
            throw new IllegalArgumentException(
                    "compatibilityThreshold must be positive, got: " + compatibilityThreshold);
        }
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.compatibilityThreshold = compatibilityThreshold;
        this.nextSpeciesId = 1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Casts {@code population} to {@link PopulationImpl} to update the species list
     * via the package-private {@code setSpecies} method.
     *
     * @throws ClassCastException if {@code population} is not a {@link PopulationImpl}
     */
    @Override
    public void speciate(Population population) {
        PopulationImpl pop = (PopulationImpl) population;

        // Collect previous species representatives (keyed by species ID)
        List<Species> previousSpecies = new ArrayList<>(pop.getSpecies());

        // Build mutable lists of members for each bucket.
        // Each bucket is [speciesId, representative, mutableMemberList]
        List<int[]> bucketIds = new ArrayList<>();
        List<Genome> bucketReps = new ArrayList<>();
        List<List<Genome>> bucketMembers = new ArrayList<>();

        // Seed buckets from previous species (keeping their IDs and representatives)
        for (Species s : previousSpecies) {
            bucketIds.add(new int[]{s.getId()});
            bucketReps.add(s.getRepresentative());
            bucketMembers.add(new ArrayList<>());
        }

        // Assign each genome to a bucket
        for (Genome genome : pop.getGenomes()) {
            int matchedBucket = -1;
            for (int i = 0; i < bucketReps.size(); i++) {
                double dist = genome.compatibilityDistance(bucketReps.get(i), c1, c2, c3);
                if (dist < compatibilityThreshold) {
                    matchedBucket = i;
                    break;
                }
            }
            if (matchedBucket >= 0) {
                bucketMembers.get(matchedBucket).add(genome);
            } else {
                // New species: genome becomes its own representative
                bucketIds.add(new int[]{nextSpeciesId++});
                bucketReps.add(genome);
                List<Genome> newMembers = new ArrayList<>();
                newMembers.add(genome);
                bucketMembers.add(newMembers);
            }
        }

        // Build SpeciesImpl objects, skipping empty buckets
        List<Species> newSpecies = new ArrayList<>();
        for (int i = 0; i < bucketIds.size(); i++) {
            List<Genome> members = bucketMembers.get(i);
            if (members.isEmpty()) {
                continue; // remove empty species
            }
            Genome rep = bucketReps.get(i);
            // Representative must be in the member list; if not (old rep not reassigned),
            // use the first member as representative instead
            if (!members.contains(rep)) {
                rep = members.get(0);
            }
            newSpecies.add(new SpeciesImpl(
                    bucketIds.get(i)[0],
                    rep,
                    members,
                    0.0, 0.0, 0));
        }

        pop.setSpecies(newSpecies);
    }
}
