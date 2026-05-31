package com.ddiggs.neat.trafficsim;

/**
 * Accumulates and exposes the individual fitness components for a single car.
 *
 * <p>The overall fitness formula is defined in {@code docs/SIMULATION.md} Section 8.1:
 * <pre>
 *   fitness =   w_progress  * (x_final / highway_length)
 *             + w_speed     * (avg_vx / v_target)
 *             + w_exit      * exit_bonus
 *             - w_collision * collision_count
 *             - w_near_miss * near_miss_count
 * </pre>
 *
 * <p>This interface exposes the <em>raw</em> measured values (final X, average speed, etc.)
 * separately from the weighted score so that researchers may re-evaluate fitness with
 * different weight settings without re-running the simulation. The weighted total is
 * computed on demand by {@link #computeFitness(SimulationConfig)}.
 *
 * <p>A {@code FitnessRecord} is created for every car at the moment it is removed from
 * the simulation (exit, collision, or cull) and is accessible via
 * {@link GenerationResult#getFitnessRecord(com.ddiggs.neat.core.Genome)}.
 */
public interface FitnessRecord {

    /**
     * Returns the X position of the car at the time it was removed from the simulation.
     *
     * <p>For cars that successfully exited, this equals or exceeds {@code highway_length}.
     * For cars that were culled or collided, this is the X position at the final tick.
     *
     * @return final X position in metres; non-negative
     */
    double getFinalX();

    /**
     * Returns the car's average longitudinal speed over its lifetime.
     *
     * <p>Computed as the mean of {@code vx} across all ticks the car was alive. Used
     * to determine the speed component of fitness relative to
     * {@link CarPhysics#getTargetSpeed()}.
     *
     * @return average speed in m/s; non-negative
     */
    double getAverageVx();

    /**
     * Returns {@code true} if the car successfully reached the end of the highway.
     *
     * <p>A successful exit occurs when the car's front edge
     * ({@code x + L/2}) reaches or exceeds {@code highway_length}. The exit bonus
     * weight ({@link SimulationConfig#getWeightExit()}) is applied only for exiting cars.
     *
     * @return {@code true} if the car exited the highway; {@code false} if culled or timed out
     */
    boolean isExited();

    /**
     * Returns the total number of collision events the car was involved in.
     *
     * <p>Each tick in which this car's bounding box overlapped another car's bounding box
     * counts as one event. A non-zero count always results in the car being culled.
     *
     * @return collision count; non-negative
     */
    int getCollisionCount();

    /**
     * Returns the number of ticks in which a near-miss was recorded.
     *
     * <p>A near-miss tick is any tick where the forward gap in the current lane drops
     * below {@code g_min / 2} (half the car's minimum gap setting). The near-miss
     * penalty is proportional to the number of such ticks.
     *
     * @return near-miss tick count; non-negative
     */
    int getNearMissCount();

    /**
     * Computes and returns the weighted fitness score using the weights from
     * {@code config}.
     *
     * <p>The formula applied is:
     * <pre>
     *   fitness =   getWeightProgress()  * (getFinalX() / config.getHighwayLength())
     *             + getWeightSpeed()     * (getAverageVx() / v_target)
     *             + getWeightExit()      * (isExited() ? 1.0 : 0.0)
     *             - getWeightCollision() * getCollisionCount()
     *             - getWeightNearMiss()  * getNearMissCount()
     * </pre>
     * where {@code v_target} is the target speed from the car's {@link CarPhysics}.
     * The result may be negative if collision or near-miss penalties outweigh the rewards.
     *
     * @param config the simulation configuration providing fitness weights;
     *               never {@code null}
     * @return the computed fitness score; may be negative
     */
    double computeFitness(SimulationConfig config);
}
