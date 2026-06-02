package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.CarState;

public final class CarStateImpl implements CarState {

    private double x;
    private double y;
    private double vx;
    private int lane;
    private boolean merging;
    private int mergeTarget;
    private double mergeProgress;
    private double distance;
    private int collisions;
    private int ticksAlive;

    double spawnX;
    double sumVx;
    int nearMissTicks;
    int stallTicks;
    boolean exited;
    boolean collided;
    int sourceLane;

    public CarStateImpl(double x, double y, double vx, int lane) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.lane = lane;
        this.mergeTarget = -1;
        this.spawnX = x;
        this.sourceLane = lane;
    }

    @Override public double getX()             { return x; }
    @Override public double getY()             { return y; }
    @Override public double getVx()            { return vx; }
    @Override public int    getLane()          { return lane; }
    @Override public boolean isMerging()       { return merging; }
    @Override public int    getMergeTarget()   { return mergeTarget; }
    @Override public double getMergeProgress() { return mergeProgress; }
    @Override public double getDistance()      { return distance; }
    @Override public int    getCollisions()    { return collisions; }
    @Override public int    getTicksAlive()    { return ticksAlive; }

    void setX(double x)                       { this.x = x; }
    void setY(double y)                       { this.y = y; }
    void setVx(double vx)                     { this.vx = vx; }
    void setLane(int lane)                    { this.lane = lane; }
    void setMerging(boolean merging)          { this.merging = merging; }
    void setMergeTarget(int mergeTarget)      { this.mergeTarget = mergeTarget; }
    void setMergeProgress(double p)           { this.mergeProgress = p; }
    void addDistance(double d)                { this.distance += d; }
    void incrementCollisions()                { this.collisions++; }
    void incrementTicksAlive()                { this.ticksAlive++; }
}
