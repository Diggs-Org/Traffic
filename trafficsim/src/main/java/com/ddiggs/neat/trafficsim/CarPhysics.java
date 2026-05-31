package com.ddiggs.neat.trafficsim;

/**
 * Fixed physical properties of a car, assigned at spawn and constant for its lifetime.
 *
 * <p>Physical parameters are <em>not</em> part of the NEAT genome; they are sampled from
 * per-species distributions before each generation run. This allows species to specialise
 * into distinct physical archetypes — for example, compact high-speed cars versus longer
 * conservative ones — while keeping the neural-network input/output schema uniform across
 * all cars.
 *
 * <p>All ranges are defined in {@code docs/SIMULATION.md} Section 3 (Car Model).
 */
public interface CarPhysics {

    /**
     * Returns the longitudinal (X) extent of the car's bounding box in metres.
     *
     * <p>The bounding box spans X &isin; [{@code x - L/2}, {@code x + L/2}] where
     * {@code x} is the car's centre position. Valid range: 3 – 8 m.
     *
     * @return car length in metres; positive
     */
    double getLength();

    /**
     * Returns the lateral (Y) extent of the car's bounding box in metres.
     *
     * <p>Width must be less than the lane width so that the car fits within a single
     * lane when not merging. Valid range: 1.8 – 2.5 m.
     *
     * @return car width in metres; positive
     */
    double getWidth();

    /**
     * Returns the maximum acceleration in metres per second squared.
     *
     * <p>Throttle output from the neural network is multiplied by this value to compute
     * the actual acceleration applied each tick:
     * {@code accel = throttle * a_max}. Valid range: 2.0 – 6.0 m/s&sup2;.
     *
     * @return maximum acceleration in m/s&sup2;; positive
     */
    double getMaxAcceleration();

    /**
     * Returns the maximum deceleration (braking force) in metres per second squared.
     *
     * <p>Brake output from the neural network is multiplied by this value:
     * {@code decel = brake * d_max}. Valid range: 4.0 – 10.0 m/s&sup2;.
     *
     * @return maximum deceleration in m/s&sup2;; positive
     */
    double getMaxDeceleration();

    /**
     * Returns the desired cruising speed in metres per second.
     *
     * <p>The speed component of individual fitness rewards the car for maintaining an
     * average speed close to this value. Valid range: 20 – 35 m/s (approx. 72 – 126 km/h).
     *
     * @return target speed in m/s; positive
     */
    double getTargetSpeed();

    /**
     * Returns the maximum sensing distance for the car's current lane in metres.
     *
     * <p>Gap readings in the current lane (forward and rearward) are normalised by
     * dividing by this value. If no car is detected within range, the gap is reported
     * as 1.0 (maximum). Valid range: 50 – 200 m.
     *
     * @return current-lane vision range in metres; positive
     */
    double getVisionRange();

    /**
     * Returns the maximum sensing distance for each adjacent lane in metres.
     *
     * <p>Gap readings in the left and right adjacent lanes are normalised by this value.
     * Typically set to approximately 0.6 × {@link #getVisionRange()}.
     * Valid range: 30 – 120 m.
     *
     * @return adjacent-lane vision range in metres; positive
     */
    double getAdjacentVisionRange();

    /**
     * Returns the minimum accepted following distance in metres.
     *
     * <p>A near-miss is recorded whenever the forward gap in the current lane falls
     * below {@code g_min / 2}. Valid range: 5 – 15 m.
     *
     * @return minimum gap in metres; positive
     */
    double getMinGap();

    /**
     * Returns the time required to complete a lane change in seconds.
     *
     * <p>During a merge, the car's Y position interpolates linearly toward the target
     * lane centre over this duration. Valid range: 1.5 – 3.0 s.
     *
     * @return lane-change completion time in seconds; positive
     */
    double getLaneChangeTime();
}
