package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.trafficsim.FitnessRecord;
import com.ddiggs.neat.trafficsim.GenerationResult;
import com.ddiggs.neat.trafficsim.PopulationMetrics;

import java.util.Collections;
import java.util.Map;

public final class GenerationResultImpl implements GenerationResult {

    private final Map<Genome, FitnessRecord> fitnessMap;
    private final PopulationMetrics metrics;
    private final int exitCount;
    private final int spawnedCount;
    private final int totalTicks;

    public GenerationResultImpl(Map<Genome, FitnessRecord> fitnessMap, PopulationMetrics metrics,
                                 int exitCount, int spawnedCount, int totalTicks) {
        this.fitnessMap = Collections.unmodifiableMap(fitnessMap);
        this.metrics = metrics;
        this.exitCount = exitCount;
        this.spawnedCount = spawnedCount;
        this.totalTicks = totalTicks;
    }

    @Override
    public FitnessRecord getFitnessRecord(Genome genome) {
        FitnessRecord r = fitnessMap.get(genome);
        if (r == null) throw new IllegalArgumentException("genome was not part of the evaluated population");
        return r;
    }

    @Override public Map<Genome, FitnessRecord> getGenomeFitnessMap() { return fitnessMap; }
    @Override public PopulationMetrics getPopulationMetrics()         { return metrics; }
    @Override public int getExitCount()                               { return exitCount; }
    @Override public int getSpawnedCount()                            { return spawnedCount; }
    @Override public int getTotalTicks()                              { return totalTicks; }
}
