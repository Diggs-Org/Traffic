package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.core.impl.NeuralNetworkImpl;
import com.ddiggs.neat.trafficsim.Car;
import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SimulationConfig;
import com.ddiggs.neat.trafficsim.SpawnStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class SpawnStrategyImpl implements SpawnStrategy {

    private long nextId = 0;

    @Override
    public boolean shouldSpawn(int currentTick, List<Car> activeCars, SimulationConfig config) {
        if (currentTick % config.getSpawnIntervalTicks() != 0) return false;
        for (Car c : activeCars) {
            if (c.getState().getX() < config.getSpawnClearDistance()) return false;
        }
        return true;
    }

    @Override
    public List<Car> spawnCars(List<Genome> pendingGenomes, Road road, SimulationConfig config) {
        int slots = Math.min(pendingGenomes.size(), road.getLaneCount());
        if (slots == 0) return List.of();

        List<GenomeWithPhysics> batch = new ArrayList<>(slots);
        Iterator<Genome> it = pendingGenomes.iterator();
        for (int i = 0; i < slots; i++) {
            Genome g = it.next();
            it.remove();
            CarPhysicsImpl physics = CarPhysicsImpl.random(new Random(System.identityHashCode(g)));
            batch.add(new GenomeWithPhysics(g, physics));
        }

        batch.sort(Comparator.comparingDouble(gwp -> -gwp.physics.getTargetSpeed()));

        List<Car> spawned = new ArrayList<>(batch.size());
        for (int lane = 0; lane < batch.size(); lane++) {
            GenomeWithPhysics gwp = batch.get(lane);
            double y = road.getLaneCenterY(lane);
            CarStateImpl state = new CarStateImpl(0.0, y, config.getSpawnSpeed(), lane);

            NeuralNetwork network = new NeuralNetworkImpl(
                    gwp.genome, x -> 1.0 / (1.0 + Math.exp(-x)));

            spawned.add(new CarImpl(nextId++, gwp.physics, state, network, gwp.genome));
        }
        return spawned;
    }

    private record GenomeWithPhysics(Genome genome, CarPhysicsImpl physics) {}
}
