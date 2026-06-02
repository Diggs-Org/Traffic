package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.trafficsim.Car;
import com.ddiggs.neat.trafficsim.DriveCommand;
import com.ddiggs.neat.trafficsim.FitnessRecord;
import com.ddiggs.neat.trafficsim.GenerationResult;
import com.ddiggs.neat.trafficsim.PopulationMetrics;
import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SensorInput;
import com.ddiggs.neat.trafficsim.Simulation;
import com.ddiggs.neat.trafficsim.SimulationConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class SimulationImpl implements Simulation {

    private List<Genome> pendingGenomes;
    private Road road;
    private SimulationConfig config;
    private boolean setupDone = false;

    @Override
    public void setup(Population population, Road road, SimulationConfig config) {
        this.pendingGenomes = new ArrayList<>(population.getGenomes());
        this.road = road;
        this.config = config;
        this.setupDone = true;
    }

    @Override
    public GenerationResult runGeneration() {
        if (!setupDone) throw new IllegalStateException("setup() must be called before runGeneration()");

        SpawnStrategyImpl spawnStrategy = new SpawnStrategyImpl();
        VisionSystemImpl  visionSystem  = new VisionSystemImpl();

        List<Car> activeCars = new ArrayList<>();
        Map<Genome, FitnessRecord> fitnessMap = new HashMap<>();
        int spawnedCount = 0;
        int exitCount    = 0;
        int tick         = 0;

        // Attempt spawn at tick 0 before the main loop
        if (!pendingGenomes.isEmpty() && spawnStrategy.shouldSpawn(0, activeCars, config)) {
            List<Car> newCars = spawnStrategy.spawnCars(pendingGenomes, road, config);
            activeCars.addAll(newCars);
            spawnedCount += newCars.size();
        }

        while ((!activeCars.isEmpty() || !pendingGenomes.isEmpty()) && tick < config.getMaxTicks()) {

            // 1. SPAWN (skip tick 0 — handled above)
            if (tick > 0 && !pendingGenomes.isEmpty()
                    && spawnStrategy.shouldSpawn(tick, activeCars, config)) {
                List<Car> newCars = spawnStrategy.spawnCars(pendingGenomes, road, config);
                activeCars.addAll(newCars);
                spawnedCount += newCars.size();
            }

            // 2. OBSERVE
            Map<Car, SensorInput> sensorMap = new IdentityHashMap<>();
            for (Car car : activeCars) {
                sensorMap.put(car, visionSystem.observe(car, road, activeCars, config));
            }

            // 3. ACTIVATE + build drive commands
            Map<Car, DriveCommand> commandMap = new IdentityHashMap<>();
            for (Car car : activeCars) {
                double[] out = car.getNeuralNetwork().activate(sensorMap.get(car).toArray());
                commandMap.put(car, new DriveCommandImpl(out));
            }

            // 4. ACT — apply speed and lane-change commands
            for (Car car : activeCars) {
                if (!(car instanceof CarImpl ci)) continue;
                CarStateImpl st = ci.getMutableState();
                DriveCommand cmd = commandMap.get(car);

                double accel = cmd.getThrottle() * car.getPhysics().getMaxAcceleration();
                double decel = cmd.getBrake()    * car.getPhysics().getMaxDeceleration();
                double newVx = st.getVx() + (accel - decel) * config.getTickDt();
                st.setVx(Math.max(0.0, Math.min(newVx, config.getVMaxAbsolute())));

                if (!st.isMerging() && !ci.isMergeLocked(config)) {
                    boolean wantsLeft  = cmd.getLaneChangeLeft()  > 0.5;
                    boolean wantsRight = cmd.getLaneChangeRight() > 0.5;
                    if (wantsLeft != wantsRight) {
                        int targetLane = st.getLane() + (wantsLeft ? -1 : 1);
                        SensorInput si = sensorMap.get(car);
                        boolean blocked = wantsLeft
                                ? si.getBlindSpotLeft()  > 0.5
                                : si.getBlindSpotRight() > 0.5;
                        if (road.isValidLane(targetLane) && !blocked) {
                            st.sourceLane = st.getLane();
                            st.setMerging(true);
                            st.setMergeTarget(targetLane);
                            st.setMergeProgress(0.0);
                        }
                    }
                }
            }

            // 5. ADVANCE — update positions and merge progress
            for (Car car : activeCars) {
                if (!(car instanceof CarImpl ci)) continue;
                CarStateImpl st = ci.getMutableState();
                double dx = st.getVx() * config.getTickDt();
                st.setX(st.getX() + dx);
                st.addDistance(dx);

                if (st.isMerging()) {
                    double progress = st.getMergeProgress()
                            + config.getTickDt() / car.getPhysics().getLaneChangeTime();
                    if (progress >= 1.0) {
                        st.setLane(st.getMergeTarget());
                        st.setY(road.getLaneCenterY(st.getMergeTarget()));
                        st.setMerging(false);
                        st.setMergeTarget(-1);
                        st.setMergeProgress(0.0);
                    } else {
                        st.setMergeProgress(progress);
                        double srcY = road.getLaneCenterY(st.sourceLane);
                        double tgtY = road.getLaneCenterY(st.getMergeTarget());
                        st.setY(srcY + (tgtY - srcY) * progress);
                    }
                }
            }

            // 6. DETECT — bounding-box collision check (O(n²))
            List<Car> toRemove = new ArrayList<>();
            for (int i = 0; i < activeCars.size(); i++) {
                Car a = activeCars.get(i);
                for (int j = i + 1; j < activeCars.size(); j++) {
                    Car b = activeCars.get(j);
                    if (overlaps(a, b)) {
                        if (a instanceof CarImpl ca) ca.getMutableState().incrementCollisions();
                        if (b instanceof CarImpl cb) cb.getMutableState().incrementCollisions();
                        if (!toRemove.contains(a)) toRemove.add(a);
                        if (!toRemove.contains(b)) toRemove.add(b);
                    }
                }
            }
            for (Car c : toRemove) {
                if (c instanceof CarImpl ci) ci.getMutableState().collided = true;
            }

            // 7. EXIT — cars that reached highway end
            for (Car car : activeCars) {
                if (toRemove.contains(car)) continue;
                if (!(car instanceof CarImpl ci)) continue;
                if (ci.frontX() >= road.getHighwayLength()) {
                    ci.getMutableState().exited = true;
                    toRemove.add(car);
                    exitCount++;
                }
            }

            // 8. CULL — stalled cars
            for (Car car : activeCars) {
                if (toRemove.contains(car)) continue;
                if (!(car instanceof CarImpl ci)) continue;
                CarStateImpl st = ci.getMutableState();
                if (st.getVx() < config.getStallThreshold()) {
                    st.stallTicks++;
                } else {
                    st.stallTicks = 0;
                }
                if (ci.isStalled(config)) toRemove.add(car);
            }
            for (Car car : toRemove) {
                if (!(car instanceof CarImpl ci)) continue;
                fitnessMap.put(ci.getGenome(), ci.toFitnessRecord());
            }
            activeCars.removeAll(toRemove);

            // 9. SCORE — update fitness accumulators for surviving cars
            for (Car car : activeCars) {
                if (!(car instanceof CarImpl ci)) continue;
                CarStateImpl st = ci.getMutableState();
                st.sumVx += st.getVx();
                st.incrementTicksAlive();

                SensorInput si = sensorMap.get(car);
                if (si != null) {
                    double gapActual = si.getGapAheadCurrent() * car.getPhysics().getVisionRange();
                    if (gapActual < car.getPhysics().getMinGap() / 2.0) {
                        st.nearMissTicks++;
                    }
                }
            }

            tick++;
        }

        // Collect cars still active at the tick limit
        for (Car car : activeCars) {
            if (!(car instanceof CarImpl ci)) continue;
            fitnessMap.put(ci.getGenome(), ci.toFitnessRecord());
        }

        PopulationMetrics metrics = computeMetrics(fitnessMap, spawnedCount, config);
        return new GenerationResultImpl(fitnessMap, metrics, exitCount, spawnedCount, tick);
    }

    private boolean overlaps(Car a, Car b) {
        double dx = Math.abs(a.getState().getX() - b.getState().getX());
        double dy = Math.abs(a.getState().getY() - b.getState().getY());
        double halfLenSum = (a.getPhysics().getLength() + b.getPhysics().getLength()) / 2.0;
        double halfWidSum = (a.getPhysics().getWidth()  + b.getPhysics().getWidth())  / 2.0;
        return dx < halfLenSum && dy < halfWidSum;
    }

    private PopulationMetrics computeMetrics(Map<Genome, FitnessRecord> map,
                                              int spawnedCount, SimulationConfig config) {
        if (map.isEmpty()) {
            return new PopulationMetricsImpl(0, 0, 0, 0, 0, 0);
        }
        double n = map.size();
        int totalCollisions = 0, totalNearMiss = 0, exitCount = 0;
        double sumSpeed = 0, sumFitness = 0, sumFitSq = 0;
        double[] speeds = new double[(int) n];
        int idx = 0;

        for (FitnessRecord r : map.values()) {
            totalCollisions += r.getCollisionCount();
            totalNearMiss   += r.getNearMissCount();
            if (r.isExited()) exitCount++;
            sumSpeed += r.getAverageVx();
            speeds[idx++] = r.getAverageVx();
            double f = r.computeFitness(config);
            sumFitness += f;
            sumFitSq   += f * f;
        }

        double avgSpeed   = sumSpeed   / n;
        double avgFitness = sumFitness / n;

        double speedVar = 0;
        for (double s : speeds) speedVar += (s - avgSpeed) * (s - avgSpeed);
        speedVar = Math.sqrt(speedVar / n);

        // Variance of fitness (std dev)
        double fitVar = Math.sqrt(Math.max(0, sumFitSq / n - avgFitness * avgFitness));

        double throughput    = spawnedCount > 0 ? (double) exitCount / spawnedCount : 0;
        double collisionRate = spawnedCount > 0 ? (double) totalCollisions / spawnedCount : 0;
        // near-miss rate: we don't track total car-ticks here; use spawned as approximation
        double nearMissRate  = spawnedCount > 0 ? (double) totalNearMiss / spawnedCount : 0;

        return new PopulationMetricsImpl(throughput, avgSpeed, speedVar, collisionRate, nearMissRate, fitVar);
    }
}
