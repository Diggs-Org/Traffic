package com.ddiggs.neat.trafficsim;

/**
 * Immutable configuration for a single traffic simulation experiment.
 *
 * <p>All physical constants, timing parameters, spawn settings, and fitness weights
 * are exposed through this interface. Implementations are typically built once at
 * experiment start and shared across all components ({@link Road}, {@link Simulation},
 * {@link FitnessRecord}, etc.) for the lifetime of the run.
 *
 * <p>Default values documented on each accessor reflect the reference configuration
 * described in {@code docs/SIMULATION.md}.
 */
public interface SimulationConfig {

    // -------------------------------------------------------------------------
    // Road geometry
    // -------------------------------------------------------------------------

    /**
     * Total X extent of the simulated highway in metres.
     *
     * <p>Cars that reach this boundary are counted as successful exits.
     *
     * @return highway length in metres; positive (default 1000.0)
     */
    double getHighwayLength();

    /**
     * Number of parallel lanes on the highway.
     *
     * <p>Lanes are indexed 0 (leftmost / fastest) through {@code lane_count - 1}
     * (rightmost / slowest). Lane count is fixed for the duration of an experiment.
     *
     * @return number of lanes; positive (default 3)
     */
    int getLaneCount();

    /**
     * Lateral (Y) width of each lane in metres.
     *
     * <p>Lane {@code i} occupies Y &isin; [{@code i * lane_width},
     * {@code (i+1) * lane_width}]. All lanes share the same width.
     *
     * @return lane width in metres; positive (default 3.5)
     */
    double getLaneWidth();

    // -------------------------------------------------------------------------
    // Time stepping
    // -------------------------------------------------------------------------

    /**
     * Duration of one simulation tick in seconds.
     *
     * <p>All physics updates (position, speed, lane-change progress) are applied
     * once per tick using this step size.
     *
     * @return tick duration in seconds; positive (default 0.1)
     */
    double getTickDt();

    /**
     * Maximum number of ticks per generation run.
     *
     * <p>A generation terminates when all cars have been removed <em>or</em> this
     * limit is reached. Cars still active at termination receive partial fitness credit.
     *
     * @return tick limit per generation; positive (default 2000)
     */
    int getMaxTicks();

    // -------------------------------------------------------------------------
    // Speed limits
    // -------------------------------------------------------------------------

    /**
     * Global hard cap on longitudinal speed in metres per second.
     *
     * <p>No car may exceed this speed regardless of throttle output or
     * {@link CarPhysics#getMaxAcceleration()}. Sensor readings are normalised
     * by dividing by this value.
     *
     * @return absolute speed ceiling in m/s; positive (default 50.0)
     */
    double getVMaxAbsolute();

    // -------------------------------------------------------------------------
    // Spawn parameters
    // -------------------------------------------------------------------------

    /**
     * Number of ticks between successive multi-lane spawn events.
     *
     * <p>A spawn event places one car into every lane simultaneously. Events are
     * attempted every {@code spawn_interval_ticks} ticks, subject to the
     * {@link #getSpawnClearDistance()} check.
     *
     * @return ticks between spawn attempts; positive (default 20)
     */
    int getSpawnIntervalTicks();

    /**
     * Minimum X distance that cars in the spawn zone must have advanced before the
     * next spawn event is permitted.
     *
     * <p>A spawn event is suppressed if any car with X &lt; {@code spawn_clear_distance}
     * is still in the spawn zone, preventing immediate rear-end collisions.
     *
     * @return spawn clearance distance in metres; positive (default 80.0)
     */
    double getSpawnClearDistance();

    /**
     * Minimum X distance a car must travel from its spawn point before it is
     * allowed to initiate a lane change.
     *
     * <p>While a car's distance is below this threshold, the {@code merge_lockout}
     * sensor input is set to 1.0 and lane-change commands are ignored.
     *
     * @return lockout distance in metres; positive (default 50.0)
     */
    double getMergeLockoutDistance();

    /**
     * Initial longitudinal speed assigned to every car at the moment of spawning.
     *
     * @return spawn speed in m/s; non-negative (default 10.0)
     */
    double getSpawnSpeed();

    // -------------------------------------------------------------------------
    // Culling thresholds
    // -------------------------------------------------------------------------

    /**
     * Speed below which a car is considered stalled.
     *
     * <p>A car that remains below this speed for more than {@link #getStallGraceTicks()}
     * consecutive ticks is culled from the simulation.
     *
     * @return stall speed threshold in m/s; non-negative (default 1.0)
     */
    double getStallThreshold();

    /**
     * Number of consecutive ticks a car may remain below {@link #getStallThreshold()}
     * before being culled.
     *
     * <p>A grace period prevents premature culling of cars that are merely braking
     * temporarily.
     *
     * @return stall grace period in ticks; positive (default 30)
     */
    int getStallGraceTicks();

    // -------------------------------------------------------------------------
    // Fitness weights (Section 8.1 of SIMULATION.md)
    // -------------------------------------------------------------------------

    /**
     * Weight applied to the progress component of individual car fitness.
     *
     * <p>Progress score = {@code w_progress * (x_final / highway_length)}.
     *
     * @return progress weight; non-negative (default 0.3)
     */
    double getWeightProgress();

    /**
     * Weight applied to the speed component of individual car fitness.
     *
     * <p>Speed score = {@code w_speed * (avg_vx / v_target)}.
     *
     * @return speed weight; non-negative (default 0.3)
     */
    double getWeightSpeed();

    /**
     * Weight applied to the exit bonus of individual car fitness.
     *
     * <p>Exit bonus = {@code w_exit * 1.0} when the car successfully exits the highway;
     * {@code 0.0} otherwise.
     *
     * @return exit weight; non-negative (default 0.3)
     */
    double getWeightExit();

    /**
     * Penalty weight applied per collision event to individual car fitness.
     *
     * <p>Collision penalty = {@code w_collision * collision_count}. This term is
     * <em>subtracted</em> and may push fitness below zero.
     *
     * @return collision penalty weight; non-negative (default 0.5)
     */
    double getWeightCollision();

    /**
     * Penalty weight applied per near-miss tick to individual car fitness.
     *
     * <p>A near-miss is any tick where the forward gap in the current lane is less than
     * half the car's {@link CarPhysics#getMinGap()}. The penalty =
     * {@code w_near_miss * near_miss_count} is subtracted from fitness.
     *
     * @return near-miss penalty weight; non-negative (default 0.1)
     */
    double getWeightNearMiss();
}
