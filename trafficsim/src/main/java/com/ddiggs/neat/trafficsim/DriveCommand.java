package com.ddiggs.neat.trafficsim;

/**
 * The 4-element action vector produced by a car's neural network each tick.
 *
 * <p>Each value is the raw sigmoid activation of the corresponding output node, in [0.0, 1.0].
 * The simulation interprets these values to apply throttle, braking, and lane-change
 * commands to the car's {@link CarState}.
 *
 * <p>{@link #getThrottle()} and {@link #getBrake()} may both be non-zero simultaneously;
 * the net acceleration applied is
 * {@code (throttle * a_max) - (brake * d_max)} per tick.
 *
 * <p>A lane change is initiated when either {@link #getLaneChangeLeft()} or
 * {@link #getLaneChangeRight()} exceeds the action threshold of 0.5, the car is not
 * already merging, and the merge-lockout distance has been cleared. If both outputs
 * simultaneously exceed 0.5 the command is treated as ambiguous and neither merge is started.
 *
 * <p>Output definitions are specified in {@code docs/SIMULATION.md} Section 6
 * (Neural-Network Interface).
 */
public interface DriveCommand {

    /**
     * Returns the throttle output in [0.0, 1.0].
     *
     * <p>Acceleration applied this tick = {@code throttle * a_max} where {@code a_max}
     * is {@link CarPhysics#getMaxAcceleration()}.
     *
     * @return throttle activation; in [0.0, 1.0]
     */
    double getThrottle();

    /**
     * Returns the brake output in [0.0, 1.0].
     *
     * <p>Deceleration applied this tick = {@code brake * d_max} where {@code d_max}
     * is {@link CarPhysics#getMaxDeceleration()}.
     *
     * @return brake activation; in [0.0, 1.0]
     */
    double getBrake();

    /**
     * Returns the left lane-change intention output in [0.0, 1.0].
     *
     * <p>A left merge is initiated when this value exceeds 0.5 and the car is eligible
     * to merge (not already merging, merge-lockout cleared, and the left lane exists).
     * Simultaneously exceeding the threshold on both {@code lane_change_left} and
     * {@link #getLaneChangeRight()} suppresses both merges.
     *
     * @return left lane-change activation; in [0.0, 1.0]
     */
    double getLaneChangeLeft();

    /**
     * Returns the right lane-change intention output in [0.0, 1.0].
     *
     * <p>A right merge is initiated when this value exceeds 0.5 and the car is eligible
     * to merge. See {@link #getLaneChangeLeft()} for the ambiguous-command rule.
     *
     * @return right lane-change activation; in [0.0, 1.0]
     */
    double getLaneChangeRight();

    /**
     * Returns the complete drive command as a fixed-length {@code double[4]} array.
     *
     * <p>Array indices correspond to the output node numbering in
     * {@code docs/SIMULATION.md} Section 6, offset by one:
     * <pre>
     *  [0] throttle
     *  [1] brake
     *  [2] lane_change_left
     *  [3] lane_change_right
     * </pre>
     *
     * @return a new {@code double[4]} array; never {@code null}
     */
    double[] toArray();
}
