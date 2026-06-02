package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.PopulationMetrics;

public final class PopulationMetricsImpl implements PopulationMetrics {

    private final double throughput;
    private final double averageSpeed;
    private final double speedVariance;
    private final double collisionRate;
    private final double nearMissRate;
    private final double diversityIndex;

    public PopulationMetricsImpl(double throughput, double averageSpeed, double speedVariance,
                                 double collisionRate, double nearMissRate, double diversityIndex) {
        this.throughput = throughput;
        this.averageSpeed = averageSpeed;
        this.speedVariance = speedVariance;
        this.collisionRate = collisionRate;
        this.nearMissRate = nearMissRate;
        this.diversityIndex = diversityIndex;
    }

    @Override public double getThroughput()     { return throughput; }
    @Override public double getAverageSpeed()   { return averageSpeed; }
    @Override public double getSpeedVariance()  { return speedVariance; }
    @Override public double getCollisionRate()  { return collisionRate; }
    @Override public double getNearMissRate()   { return nearMissRate; }
    @Override public double getDiversityIndex() { return diversityIndex; }
}
