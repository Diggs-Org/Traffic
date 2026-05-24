package com.ddiggs.neat.evolution;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.InnovationTracker;

/**
 * Applies random structural or parametric mutations to a {@link Genome}.
 *
 * <p>NEAT supports several mutation operators:
 * <ul>
 *   <li><strong>Weight mutation</strong> — perturbs or resets existing connection weights.</li>
 *   <li><strong>Add connection</strong> — inserts a new {@link com.ddiggs.neat.core.ConnectionGene}
 *       between two previously unconnected nodes; requires a new innovation number.</li>
 *   <li><strong>Add node</strong> — splits an existing connection by inserting a hidden node;
 *       the original connection is disabled and two new connections replace it.</li>
 *   <li><strong>Toggle connection</strong> — enables or disables an existing connection gene.</li>
 * </ul>
 *
 * <p>Each mutation type is applied with a configurable probability. The
 * {@link InnovationTracker} must be supplied for structural mutations so that new
 * connections receive globally consistent innovation numbers.
 */
public interface MutationStrategy {

    /**
     * Returns a mutated copy (or the same object, depending on the implementation)
     * of the given genome.
     *
     * <p>Implementations must not modify the input genome if they return a new instance.
     *
     * @param genome  the genome to mutate; never {@code null}
     * @param tracker the global innovation tracker for this generation; never {@code null}
     * @return the (possibly mutated) genome; never {@code null}
     */
    Genome mutate(Genome genome, InnovationTracker tracker);
}
