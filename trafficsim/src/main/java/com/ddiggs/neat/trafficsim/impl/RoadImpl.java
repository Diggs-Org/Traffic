package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SimulationConfig;

public final class RoadImpl implements Road {

    private final SimulationConfig config;

    public RoadImpl(SimulationConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
    }

    @Override public double getHighwayLength() { return config.getHighwayLength(); }
    @Override public int    getLaneCount()     { return config.getLaneCount(); }
    @Override public double getLaneWidth()     { return config.getLaneWidth(); }

    @Override
    public double getLaneLowerY(int lane) {
        validateLane(lane);
        return lane * config.getLaneWidth();
    }

    @Override
    public double getLaneCenterY(int lane) {
        validateLane(lane);
        return (lane + 0.5) * config.getLaneWidth();
    }

    @Override
    public double getLaneUpperY(int lane) {
        validateLane(lane);
        return (lane + 1) * config.getLaneWidth();
    }

    @Override
    public boolean isValidLane(int lane) {
        return lane >= 0 && lane < config.getLaneCount();
    }

    @Override
    public SimulationConfig getConfig() { return config; }

    private void validateLane(int lane) {
        if (!isValidLane(lane)) {
            throw new IllegalArgumentException("invalid lane index: " + lane);
        }
    }
}
