package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.SimulationConfig;

public final class SimulationConfigImpl implements SimulationConfig {

    private final double highwayLength;
    private final int laneCount;
    private final double laneWidth;
    private final double tickDt;
    private final int maxTicks;
    private final double vMaxAbsolute;
    private final int spawnIntervalTicks;
    private final double spawnClearDistance;
    private final double mergeLockoutDistance;
    private final double spawnSpeed;
    private final double stallThreshold;
    private final int stallGraceTicks;
    private final double weightProgress;
    private final double weightSpeed;
    private final double weightExit;
    private final double weightCollision;
    private final double weightNearMiss;

    private SimulationConfigImpl(Builder b) {
        this.highwayLength = b.highwayLength;
        this.laneCount = b.laneCount;
        this.laneWidth = b.laneWidth;
        this.tickDt = b.tickDt;
        this.maxTicks = b.maxTicks;
        this.vMaxAbsolute = b.vMaxAbsolute;
        this.spawnIntervalTicks = b.spawnIntervalTicks;
        this.spawnClearDistance = b.spawnClearDistance;
        this.mergeLockoutDistance = b.mergeLockoutDistance;
        this.spawnSpeed = b.spawnSpeed;
        this.stallThreshold = b.stallThreshold;
        this.stallGraceTicks = b.stallGraceTicks;
        this.weightProgress = b.weightProgress;
        this.weightSpeed = b.weightSpeed;
        this.weightExit = b.weightExit;
        this.weightCollision = b.weightCollision;
        this.weightNearMiss = b.weightNearMiss;
    }

    public static Builder builder() { return new Builder(); }

    @Override public double getHighwayLength()      { return highwayLength; }
    @Override public int    getLaneCount()           { return laneCount; }
    @Override public double getLaneWidth()           { return laneWidth; }
    @Override public double getTickDt()              { return tickDt; }
    @Override public int    getMaxTicks()            { return maxTicks; }
    @Override public double getVMaxAbsolute()        { return vMaxAbsolute; }
    @Override public int    getSpawnIntervalTicks()  { return spawnIntervalTicks; }
    @Override public double getSpawnClearDistance()  { return spawnClearDistance; }
    @Override public double getMergeLockoutDistance(){ return mergeLockoutDistance; }
    @Override public double getSpawnSpeed()          { return spawnSpeed; }
    @Override public double getStallThreshold()      { return stallThreshold; }
    @Override public int    getStallGraceTicks()     { return stallGraceTicks; }
    @Override public double getWeightProgress()      { return weightProgress; }
    @Override public double getWeightSpeed()         { return weightSpeed; }
    @Override public double getWeightExit()          { return weightExit; }
    @Override public double getWeightCollision()     { return weightCollision; }
    @Override public double getWeightNearMiss()      { return weightNearMiss; }

    public static final class Builder {
        private double highwayLength      = 1000.0;
        private int    laneCount          = 3;
        private double laneWidth          = 3.5;
        private double tickDt             = 0.1;
        private int    maxTicks           = 2000;
        private double vMaxAbsolute       = 50.0;
        private int    spawnIntervalTicks = 20;
        private double spawnClearDistance = 80.0;
        private double mergeLockoutDistance = 50.0;
        private double spawnSpeed         = 10.0;
        private double stallThreshold     = 1.0;
        private int    stallGraceTicks    = 30;
        private double weightProgress     = 0.3;
        private double weightSpeed        = 0.3;
        private double weightExit         = 0.3;
        private double weightCollision    = 0.5;
        private double weightNearMiss     = 0.1;

        public Builder highwayLength(double v)       { highwayLength = v; return this; }
        public Builder laneCount(int v)              { laneCount = v; return this; }
        public Builder laneWidth(double v)           { laneWidth = v; return this; }
        public Builder tickDt(double v)              { tickDt = v; return this; }
        public Builder maxTicks(int v)               { maxTicks = v; return this; }
        public Builder vMaxAbsolute(double v)        { vMaxAbsolute = v; return this; }
        public Builder spawnIntervalTicks(int v)     { spawnIntervalTicks = v; return this; }
        public Builder spawnClearDistance(double v)  { spawnClearDistance = v; return this; }
        public Builder mergeLockoutDistance(double v){ mergeLockoutDistance = v; return this; }
        public Builder spawnSpeed(double v)          { spawnSpeed = v; return this; }
        public Builder stallThreshold(double v)      { stallThreshold = v; return this; }
        public Builder stallGraceTicks(int v)        { stallGraceTicks = v; return this; }
        public Builder weightProgress(double v)      { weightProgress = v; return this; }
        public Builder weightSpeed(double v)         { weightSpeed = v; return this; }
        public Builder weightExit(double v)          { weightExit = v; return this; }
        public Builder weightCollision(double v)     { weightCollision = v; return this; }
        public Builder weightNearMiss(double v)      { weightNearMiss = v; return this; }

        public SimulationConfigImpl build() { return new SimulationConfigImpl(this); }
    }
}
