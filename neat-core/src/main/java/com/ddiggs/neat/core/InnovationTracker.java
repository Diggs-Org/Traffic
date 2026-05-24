package com.ddiggs.neat.core;

/**
 * Maintains the global innovation counter for structural mutations.
 *
 * <p>Whenever the NEAT algorithm adds a new connection between two nodes that have
 * never been connected before in the current generation, the {@code InnovationTracker}
 * assigns a globally unique <em>innovation number</em> to that connection. If the same
 * (fromNode, toNode) pair appears again within the same generation, the existing
 * innovation number is reused — ensuring that matching structures receive matching
 * historical markers across genomes.
 *
 * <p>The tracker is typically reset (or snapshotted) at the start of each new
 * generation so that within-generation re-use is enforced while cross-generation
 * numbers remain monotonically increasing.
 */
public interface InnovationTracker {

    /**
     * Returns the innovation number for a connection from {@code fromNodeId} to
     * {@code toNodeId}, creating a new one if this pair has not been seen before
     * in the current generation.
     *
     * @param fromNodeId the id of the source node
     * @param toNodeId   the id of the destination node
     * @return a positive, globally unique innovation number for this structural mutation
     */
    int getInnovationNumber(int fromNodeId, int toNodeId);

    /**
     * Returns the highest innovation number issued so far.
     *
     * @return the current maximum innovation number; {@code 0} if none issued yet
     */
    int getCurrentInnovationNumber();

    /**
     * Clears the within-generation cache of (fromNode, toNode) → innovation mappings.
     *
     * <p>Should be called at the start of each new generation. The global counter
     * is <em>not</em> reset; only the generation-scoped deduplication map is cleared.
     */
    void reset();
}
