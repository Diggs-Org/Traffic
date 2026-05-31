package com.ddiggs.neat.trafficsim;

/**
 * The mutable per-tick state of a car in the traffic simulation.
 *
 * <p>State variables are updated once per tick by the simulation engine. All reads
 * during a tick observe the state from the <em>previous</em> tick; positions are only
 * advanced at step 5 (ADVANCE) of the generational loop, after all cars have acted.
 *
 * <p>State variables are defined in {@code docs/SIMULATION.md} Section 3 (Car Model).
 */
public interface CarState {

    /**
     * Returns the X coordinate of the car's centre position in metres.
     *
     * <p>X increases in the direction of travel. Cars are spawned at X = 0 and exit at
     * X = {@code highway_length}.
     *
     * @return current X position in metres; non-negative
     */
    double getX();

    /**
     * Returns the Y coordinate of the car's centre position in metres.
     *
     * <p>Y is the lateral position. During a merge, Y interpolates continuously from
     * the source lane centre toward the target lane centre over
     * {@link CarPhysics#getLaneChangeTime()} seconds.
     *
     * @return current Y position in metres; non-negative
     */
    double getY();

    /**
     * Returns the longitudinal speed of the car in metres per second.
     *
     * <p>Speed is always non-negative — cars cannot reverse. The net speed change per
     * tick is {@code (throttle * a_max - brake * d_max) * tick_dt}, clamped to
     * [{@code 0}, {@code v_max_absolute}].
     *
     * @return current longitudinal speed in m/s; non-negative
     */
    double getVx();

    /**
     * Returns the index of the lane the car currently occupies.
     *
     * <p>During a merge, this field still reflects the <em>source</em> lane until the
     * merge completes ({@link #getMergeProgress()} = 1.0), at which point it is updated
     * to {@link #getMergeTarget()}.
     *
     * @return current lane index; &ge; 0
     */
    int getLane();

    /**
     * Returns {@code true} while a lane change is in progress.
     *
     * <p>A car can only initiate a new merge when this flag is {@code false}. While
     * merging, the car's bounding box spans both the source lane and the target lane,
     * making it visible to observers in either lane.
     *
     * @return {@code true} if a lane change is currently in progress
     */
    boolean isMerging();

    /**
     * Returns the index of the target lane during an active merge, or {@code -1} if
     * the car is not currently merging.
     *
     * @return target lane index during a merge; {@code -1} if not merging
     */
    int getMergeTarget();

    /**
     * Returns the completion fraction of the current lane change, in [0.0, 1.0].
     *
     * <p>At 0.0 the merge has just started; at 1.0 the car is fully in the target lane.
     * Meaningful only when {@link #isMerging()} is {@code true}; implementation-defined
     * value when not merging.
     *
     * @return merge completion fraction; in [0.0, 1.0]
     */
    double getMergeProgress();

    /**
     * Returns the total X distance covered by this car since spawning, in metres.
     *
     * <p>This is the cumulative sum of {@code vx * tick_dt} over all ticks alive. Used
     * to compute the progress component of the fitness score.
     *
     * @return total distance travelled in metres; non-negative
     */
    double getDistance();

    /**
     * Returns the cumulative number of collision events this car has been involved in.
     *
     * <p>Each tick in which this car's axis-aligned bounding box overlaps another car's
     * bounding box counts as one collision event. Collisions are terminal: the colliding
     * cars are removed from the simulation after the tick.
     *
     * @return collision count; non-negative
     */
    int getCollisions();

    /**
     * Returns the number of simulation ticks this car has been alive.
     *
     * <p>Incremented at the end of each tick in which the car is active.
     *
     * @return ticks survived; non-negative
     */
    int getTicksAlive();
}
