package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.SensorInput;

import java.util.Arrays;

public final class SensorInputImpl implements SensorInput {

    public static final int SIZE = 19;

    private final double[] values;

    public SensorInputImpl(double[] values) {
        if (values == null || values.length != SIZE) {
            throw new IllegalArgumentException("values must have length " + SIZE);
        }
        this.values = Arrays.copyOf(values, SIZE);
    }

    @Override public double getCurrentSpeed()           { return values[0]; }
    @Override public double getGapAheadCurrent()        { return values[1]; }
    @Override public double getRelSpeedAheadCurrent()   { return values[2]; }
    @Override public double getGapBehindCurrent()       { return values[3]; }
    @Override public double getRelSpeedBehindCurrent()  { return values[4]; }
    @Override public double getGapAheadLeft()           { return values[5]; }
    @Override public double getRelSpeedAheadLeft()      { return values[6]; }
    @Override public double getGapBehindLeft()          { return values[7]; }
    @Override public double getRelSpeedBehindLeft()     { return values[8]; }
    @Override public double getGapAheadRight()          { return values[9]; }
    @Override public double getRelSpeedAheadRight()     { return values[10]; }
    @Override public double getGapBehindRight()         { return values[11]; }
    @Override public double getRelSpeedBehindRight()    { return values[12]; }
    @Override public double getLeftLaneExists()         { return values[13]; }
    @Override public double getRightLaneExists()        { return values[14]; }
    @Override public double getBlindSpotLeft()          { return values[15]; }
    @Override public double getBlindSpotRight()         { return values[16]; }
    @Override public double getLaneIndex()              { return values[17]; }
    @Override public double getMergeLockout()           { return values[18]; }

    @Override
    public double[] toArray() {
        return Arrays.copyOf(values, SIZE);
    }
}
