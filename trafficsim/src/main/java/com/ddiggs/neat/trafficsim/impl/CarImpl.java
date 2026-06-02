package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.trafficsim.Car;
import com.ddiggs.neat.trafficsim.CarPhysics;
import com.ddiggs.neat.trafficsim.CarState;
import com.ddiggs.neat.trafficsim.FitnessRecord;
import com.ddiggs.neat.trafficsim.SimulationConfig;

public final class CarImpl implements Car {

    private final long id;
    private final CarPhysics physics;
    private final CarStateImpl state;
    private final NeuralNetwork network;
    private final Genome genome;

    public CarImpl(long id, CarPhysics physics, CarStateImpl state, NeuralNetwork network, Genome genome) {
        this.id = id;
        this.physics = physics;
        this.state = state;
        this.network = network;
        this.genome = genome;
    }

    @Override public long getId()                { return id; }
    @Override public CarPhysics getPhysics()     { return physics; }
    @Override public CarState getState()         { return state; }
    @Override public NeuralNetwork getNeuralNetwork() { return network; }
    @Override public Genome getGenome()          { return genome; }

    CarStateImpl getMutableState() { return state; }

    double frontX() {
        return state.getX() + physics.getLength() / 2.0;
    }

    boolean isMergeLocked(SimulationConfig config) {
        return (state.getX() - state.spawnX) < config.getMergeLockoutDistance();
    }

    boolean isStalled(SimulationConfig config) {
        return state.stallTicks > config.getStallGraceTicks();
    }

    FitnessRecord toFitnessRecord() {
        double avgVx = state.getTicksAlive() > 0 ? state.sumVx / state.getTicksAlive() : 0.0;
        return new FitnessRecordImpl(
                state.getX(),
                avgVx,
                state.exited,
                state.getCollisions(),
                state.nearMissTicks,
                physics.getTargetSpeed());
    }
}
