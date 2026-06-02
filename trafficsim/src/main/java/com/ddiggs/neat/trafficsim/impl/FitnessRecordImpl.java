package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.FitnessRecord;
import com.ddiggs.neat.trafficsim.SimulationConfig;

public final class FitnessRecordImpl implements FitnessRecord {

    private final double finalX;
    private final double averageVx;
    private final boolean exited;
    private final int collisionCount;
    private final int nearMissCount;
    private final double vTarget;

    public FitnessRecordImpl(double finalX, double averageVx, boolean exited,
                              int collisionCount, int nearMissCount, double vTarget) {
        this.finalX = finalX;
        this.averageVx = averageVx;
        this.exited = exited;
        this.collisionCount = collisionCount;
        this.nearMissCount = nearMissCount;
        this.vTarget = vTarget;
    }

    @Override public double getFinalX()        { return finalX; }
    @Override public double getAverageVx()     { return averageVx; }
    @Override public boolean isExited()        { return exited; }
    @Override public int getCollisionCount()   { return collisionCount; }
    @Override public int getNearMissCount()    { return nearMissCount; }

    @Override
    public double computeFitness(SimulationConfig config) {
        return config.getWeightProgress()  * (finalX / config.getHighwayLength())
             + config.getWeightSpeed()     * (averageVx / vTarget)
             + config.getWeightExit()      * (exited ? 1.0 : 0.0)
             - config.getWeightCollision() * collisionCount
             - config.getWeightNearMiss()  * nearMissCount;
    }
}
