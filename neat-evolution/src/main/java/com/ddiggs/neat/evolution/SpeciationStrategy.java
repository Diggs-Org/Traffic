package com.ddiggs.neat.evolution;

/**
 * Assigns the genomes of a {@link Population} to {@link Species}.
 *
 * <p>Each genome is compared against the representatives of existing species using
 * {@link com.ddiggs.neat.core.Genome#compatibilityDistance}. If the distance is below
 * the configured threshold the genome joins that species; otherwise a new species is
 * created with the genome as its founding representative.
 *
 * <p>Implementations are also responsible for:
 * <ul>
 *   <li>Removing empty species (no members assigned).</li>
 *   <li>Updating stagnation counters.</li>
 *   <li>Optionally adjusting the compatibility threshold to maintain a target species count.</li>
 * </ul>
 */
public interface SpeciationStrategy {

    /**
     * Partitions the genomes of {@code population} into species, mutating the
     * population's species list in place.
     *
     * @param population the population whose genomes are to be speciated; never {@code null}
     */
    void speciate(Population population);
}
