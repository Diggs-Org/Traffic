package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.CarPhysics;

import java.util.Random;

public final class CarPhysicsImpl implements CarPhysics {

    private final double length;
    private final double width;
    private final double maxAcceleration;
    private final double maxDeceleration;
    private final double targetSpeed;
    private final double visionRange;
    private final double adjacentVisionRange;
    private final double minGap;
    private final double laneChangeTime;

    public CarPhysicsImpl(double length, double width, double maxAcceleration,
                          double maxDeceleration, double targetSpeed, double visionRange,
                          double adjacentVisionRange, double minGap, double laneChangeTime) {
        if (length <= 0) throw new IllegalArgumentException("length must be positive");
        if (width <= 0) throw new IllegalArgumentException("width must be positive");
        if (maxAcceleration <= 0) throw new IllegalArgumentException("maxAcceleration must be positive");
        if (maxDeceleration <= 0) throw new IllegalArgumentException("maxDeceleration must be positive");
        if (targetSpeed <= 0) throw new IllegalArgumentException("targetSpeed must be positive");
        if (visionRange <= 0) throw new IllegalArgumentException("visionRange must be positive");
        if (adjacentVisionRange <= 0) throw new IllegalArgumentException("adjacentVisionRange must be positive");
        if (minGap <= 0) throw new IllegalArgumentException("minGap must be positive");
        if (laneChangeTime <= 0) throw new IllegalArgumentException("laneChangeTime must be positive");
        this.length = length;
        this.width = width;
        this.maxAcceleration = maxAcceleration;
        this.maxDeceleration = maxDeceleration;
        this.targetSpeed = targetSpeed;
        this.visionRange = visionRange;
        this.adjacentVisionRange = adjacentVisionRange;
        this.minGap = minGap;
        this.laneChangeTime = laneChangeTime;
    }

    public static CarPhysicsImpl random(Random rng) {
        double length = 3.0 + rng.nextDouble() * 5.0;
        double width = 1.8 + rng.nextDouble() * 0.7;
        double maxAcceleration = 2.0 + rng.nextDouble() * 4.0;
        double maxDeceleration = 4.0 + rng.nextDouble() * 6.0;
        double targetSpeed = 20.0 + rng.nextDouble() * 15.0;
        double visionRange = 50.0 + rng.nextDouble() * 150.0;
        double adjacentVisionRange = visionRange * 0.6;
        double minGap = 5.0 + rng.nextDouble() * 10.0;
        double laneChangeTime = 1.5 + rng.nextDouble() * 1.5;
        return new CarPhysicsImpl(length, width, maxAcceleration, maxDeceleration,
                targetSpeed, visionRange, adjacentVisionRange, minGap, laneChangeTime);
    }

    @Override public double getLength() { return length; }
    @Override public double getWidth() { return width; }
    @Override public double getMaxAcceleration() { return maxAcceleration; }
    @Override public double getMaxDeceleration() { return maxDeceleration; }
    @Override public double getTargetSpeed() { return targetSpeed; }
    @Override public double getVisionRange() { return visionRange; }
    @Override public double getAdjacentVisionRange() { return adjacentVisionRange; }
    @Override public double getMinGap() { return minGap; }
    @Override public double getLaneChangeTime() { return laneChangeTime; }
}
