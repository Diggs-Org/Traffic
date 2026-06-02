package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.DriveCommand;

import java.util.Arrays;

public final class DriveCommandImpl implements DriveCommand {

    private final double[] values;

    public DriveCommandImpl(double[] outputs) {
        if (outputs == null || outputs.length != 4) {
            throw new IllegalArgumentException("outputs must have length 4");
        }
        this.values = Arrays.copyOf(outputs, 4);
    }

    @Override public double getThrottle() { return values[0]; }
    @Override public double getBrake() { return values[1]; }
    @Override public double getLaneChangeLeft() { return values[2]; }
    @Override public double getLaneChangeRight() { return values[3]; }

    @Override
    public double[] toArray() {
        return Arrays.copyOf(values, values.length);
    }
}
