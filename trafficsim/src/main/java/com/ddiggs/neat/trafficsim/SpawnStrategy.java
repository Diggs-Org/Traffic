package com.ddiggs.neat.trafficsim;

import com.ddiggs.neat.core.Genome;

import java.util.List;

/**
 * Controls when and how cars are introduced into the simulation.
 *
 * <p>The spawning model is defined in {@code docs/SIMULATION.md} Sections 4 and 7.
 * A spawn event places one car into each lane simultaneously, with the fastest
 * {@link CarPhysics#getTargetSpeed() target speed} assigned to the leftmost lane.
 * Events are triggered every {@link SimulationConfig#getSpawnIntervalTicks()} ticks,
 * subject to a clearance check: no car may be in the spawn zone
 * (X &lt; {@link SimulationConfig#getSpawnClearDistance()}).
 *
 * <p>All spawned cars start at X = 0, their lane's centre Y, with
 * {@link SimulationConfig#getSpawnSpeed()} as initial speed. They are placed under a
 * merge lockout until they have travelled {@link SimulationConfig#getMergeLockoutDistance()}.
 */
public interface SpawnStrategy {

    /**
     * Returns {@code true} if a spawn event should occur on the given tick.
     *
     * <p>Implementations check both the tick interval condition and the spawn-zone
     * clearance condition: a spawn is only permitted if no active car has an X position
     * below {@link SimulationConfig#getSpawnClearDistance()}.
     *
     * @param currentTick the current simulation tick counter; non-negative
     * @param activeCars  all cars currently active in the simulation; never {@code null}
     * @param config      the simulation configuration; never {@code null}
     * @return {@code true} if a spawn event should be executed this tick
     */
    boolean shouldSpawn(int currentTick, List<Car> activeCars, SimulationConfig config);

    /**
     * Creates and returns the next batch of cars to be spawned onto the road.
     *
     * <p>One car is created per lane (up to {@link Road#getLaneCount()} cars per call),
     * consuming that many genomes from the front of {@code pendingGenomes}. Genomes are
     * assigned to lanes in descending order of
     * {@link CarPhysics#getTargetSpeed() target speed}: the genome that produces the
     * highest target speed occupies Lane 0.
     *
     * <p>If {@code pendingGenomes} contains fewer entries than the lane count, only as
     * many cars as there are remaining genomes are spawned.
     *
     * @param pendingGenomes ordered list of genomes not yet spawned; never {@code null};
     *                       the implementation consumes entries from the front of this list
     * @param road           road geometry used to determine spawn positions; never {@code null}
     * @param config         simulation configuration providing spawn speed and lockout
     *                       distance; never {@code null}
     * @return the newly created cars, one per lane assigned; never {@code null};
     *         may be empty if {@code pendingGenomes} is empty
     */
    List<Car> spawnCars(List<Genome> pendingGenomes, Road road, SimulationConfig config);
}
