package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.InnovationTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard implementation of {@link InnovationTracker}.
 *
 * <p>Maintains a monotonically increasing global innovation counter and a
 * within-generation deduplication map that maps {@code (fromNodeId, toNodeId)}
 * pairs to the innovation number assigned in the current generation.
 *
 * <p>Calling {@link #reset()} clears the deduplication map so that a new
 * generation begins fresh, but the global counter is preserved — innovation
 * numbers are never re-used across generations.
 */
public class InnovationTrackerImpl implements InnovationTracker {

    private int currentInnovationNumber;
    private final Map<String, Integer> generationCache;

    public InnovationTrackerImpl() {
        this.currentInnovationNumber = 0;
        this.generationCache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the {@code (fromNodeId, toNodeId)} pair already received an innovation
     * number in this generation, that number is returned unchanged. Otherwise the
     * global counter is incremented and the new number is recorded in the
     * within-generation cache.
     */
    @Override
    public int getInnovationNumber(int fromNodeId, int toNodeId) {
        String key = fromNodeId + ":" + toNodeId;
        return generationCache.computeIfAbsent(key, k -> ++currentInnovationNumber);
    }

    /** {@inheritDoc} */
    @Override
    public int getCurrentInnovationNumber() {
        return currentInnovationNumber;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Clears the within-generation cache so the next call for any
     * {@code (fromNode, toNode)} pair will produce a fresh, incremented innovation
     * number. The global counter is preserved.
     */
    @Override
    public void reset() {
        generationCache.clear();
    }
}
