package com.ddiggs.neat.trafficsim;

/**
 * The 19-element normalised observation vector supplied to a car's neural network each tick.
 *
 * <p>Every value is normalised to [0.0, 1.0] unless otherwise noted. The ordering of
 * {@link #toArray()} is canonical: index 0 is {@link #getCurrentSpeed()}, index 1 is
 * {@link #getGapAheadCurrent()}, and so on through index 18 ({@link #getMergeLockout()}).
 * This ordering is fixed for all cars and all genomes, ensuring fair cross-genome comparison.
 *
 * <p>Absent lanes (e.g. left-lane readings when the car is already in lane 0) are reported
 * as worst-case-safe values: gap = 1.0 (no obstacle), relative speed = 0.5 (same speed),
 * lane-exists flag = 0.0. Blind-spot values are replaced with gap = 0.0, relative speed = 0.5.
 *
 * <p>Input definitions are specified in {@code docs/SIMULATION.md} Section 6
 * (Neural-Network Interface).
 */
public interface SensorInput {

    // -------------------------------------------------------------------------
    // Ego state (input 1)
    // -------------------------------------------------------------------------

    /**
     * Returns the ego car's normalised speed ({@code vx / v_max_absolute}).
     *
     * @return current speed normalised to [0.0, 1.0]
     */
    double getCurrentSpeed();

    // -------------------------------------------------------------------------
    // Current-lane readings (inputs 2 – 5)
    // -------------------------------------------------------------------------

    /**
     * Returns the normalised distance to the nearest car ahead in the current lane
     * ({@code gap / v_range}).
     *
     * <p>1.0 indicates no car is detected within {@link CarPhysics#getVisionRange()}.
     *
     * @return forward gap in current lane, normalised to [0.0, 1.0]
     */
    double getGapAheadCurrent();

    /**
     * Returns the normalised relative speed of the nearest car ahead in the current lane.
     *
     * <p>Computed as {@code (speed_ahead - ego_speed + v_max) / (2 * v_max)}, so that
     * 0.5 represents equal speeds, values below 0.5 mean the car ahead is slower, and
     * values above 0.5 mean it is faster. 1.0 indicates no car detected.
     *
     * @return relative speed ahead in current lane, in [0.0, 1.0]
     */
    double getRelSpeedAheadCurrent();

    /**
     * Returns the normalised distance to the nearest car behind in the current lane
     * ({@code gap / v_range}).
     *
     * <p>1.0 indicates no car is detected within {@link CarPhysics#getVisionRange()}.
     *
     * @return rearward gap in current lane, normalised to [0.0, 1.0]
     */
    double getGapBehindCurrent();

    /**
     * Returns the normalised relative speed of the nearest car behind in the current lane.
     *
     * <p>Uses the same formula as {@link #getRelSpeedAheadCurrent()}: 0.5 = same speed.
     *
     * @return relative speed behind in current lane, in [0.0, 1.0]
     */
    double getRelSpeedBehindCurrent();

    // -------------------------------------------------------------------------
    // Left-lane readings (inputs 6 – 9)
    // -------------------------------------------------------------------------

    /**
     * Returns the normalised forward gap in the left adjacent lane
     * ({@code gap / v_range_adjacent}).
     *
     * <p>1.0 if no car is detected or if the left lane does not exist.
     *
     * @return forward gap in left lane, normalised to [0.0, 1.0]
     */
    double getGapAheadLeft();

    /**
     * Returns the normalised relative speed of the nearest car ahead in the left lane.
     *
     * <p>0.5 if no car is detected or if the left lane does not exist.
     *
     * @return relative speed ahead in left lane, in [0.0, 1.0]
     */
    double getRelSpeedAheadLeft();

    /**
     * Returns the normalised rearward gap in the left adjacent lane
     * ({@code gap / v_range_adjacent}).
     *
     * <p>1.0 if no car is detected or if the left lane does not exist.
     *
     * @return rearward gap in left lane, normalised to [0.0, 1.0]
     */
    double getGapBehindLeft();

    /**
     * Returns the normalised relative speed of the nearest car behind in the left lane.
     *
     * <p>0.5 if no car is detected or if the left lane does not exist.
     *
     * @return relative speed behind in left lane, in [0.0, 1.0]
     */
    double getRelSpeedBehindLeft();

    // -------------------------------------------------------------------------
    // Right-lane readings (inputs 10 – 13)
    // -------------------------------------------------------------------------

    /**
     * Returns the normalised forward gap in the right adjacent lane
     * ({@code gap / v_range_adjacent}).
     *
     * <p>1.0 if no car is detected or if the right lane does not exist.
     *
     * @return forward gap in right lane, normalised to [0.0, 1.0]
     */
    double getGapAheadRight();

    /**
     * Returns the normalised relative speed of the nearest car ahead in the right lane.
     *
     * <p>0.5 if no car is detected or if the right lane does not exist.
     *
     * @return relative speed ahead in right lane, in [0.0, 1.0]
     */
    double getRelSpeedAheadRight();

    /**
     * Returns the normalised rearward gap in the right adjacent lane
     * ({@code gap / v_range_adjacent}).
     *
     * <p>1.0 if no car is detected or if the right lane does not exist.
     *
     * @return rearward gap in right lane, normalised to [0.0, 1.0]
     */
    double getGapBehindRight();

    /**
     * Returns the normalised relative speed of the nearest car behind in the right lane.
     *
     * <p>0.5 if no car is detected or if the right lane does not exist.
     *
     * @return relative speed behind in right lane, in [0.0, 1.0]
     */
    double getRelSpeedBehindRight();

    // -------------------------------------------------------------------------
    // Lane-existence flags (inputs 14 – 15)
    // -------------------------------------------------------------------------

    /**
     * Returns 1.0 if a lane exists to the left of the car's current lane, 0.0 otherwise.
     *
     * @return left-lane existence flag; either 0.0 or 1.0
     */
    double getLeftLaneExists();

    /**
     * Returns 1.0 if a lane exists to the right of the car's current lane, 0.0 otherwise.
     *
     * @return right-lane existence flag; either 0.0 or 1.0
     */
    double getRightLaneExists();

    // -------------------------------------------------------------------------
    // Blind-spot flags (inputs 16 – 17)
    // -------------------------------------------------------------------------

    /**
     * Returns 1.0 if another car occupies the left blind-spot zone, 0.0 otherwise.
     *
     * <p>The blind-spot zone on the left side covers any car in the left adjacent lane
     * whose X overlap with the ego car satisfies the blind-spot geometry defined in
     * {@code docs/SIMULATION.md} Section 5. When this flag is 1.0 the corresponding
     * left-lane gap and speed inputs are replaced with worst-case readings.
     *
     * @return left blind-spot occupancy; either 0.0 or 1.0
     */
    double getBlindSpotLeft();

    /**
     * Returns 1.0 if another car occupies the right blind-spot zone, 0.0 otherwise.
     *
     * <p>See {@link #getBlindSpotLeft()} for the definition of the blind-spot zone.
     *
     * @return right blind-spot occupancy; either 0.0 or 1.0
     */
    double getBlindSpotRight();

    // -------------------------------------------------------------------------
    // Positional and lockout flags (inputs 18 – 19)
    // -------------------------------------------------------------------------

    /**
     * Returns the ego car's normalised lane index
     * ({@code current_lane / (lane_count - 1)}).
     *
     * <p>0.0 represents the leftmost (fastest) lane; 1.0 represents the rightmost
     * (slowest) lane.
     *
     * @return normalised lane index; in [0.0, 1.0]
     */
    double getLaneIndex();

    /**
     * Returns 1.0 while the car is within {@code merge_lockout_distance} of its spawn
     * point (lane changes are disabled), 0.0 once the car is free to merge.
     *
     * @return merge-lockout flag; either 0.0 or 1.0
     */
    double getMergeLockout();

    // -------------------------------------------------------------------------
    // Serialisation
    // -------------------------------------------------------------------------

    /**
     * Returns the complete observation as a fixed-length {@code double[19]} array.
     *
     * <p>Array indices correspond to the input numbering in
     * {@code docs/SIMULATION.md} Section 6, offset by one (index 0 = input node 1):
     * <pre>
     *  [0]  current_speed
     *  [1]  gap_ahead_current
     *  [2]  relspeed_ahead_current
     *  [3]  gap_behind_current
     *  [4]  relspeed_behind_current
     *  [5]  gap_ahead_left
     *  [6]  relspeed_ahead_left
     *  [7]  gap_behind_left
     *  [8]  relspeed_behind_left
     *  [9]  gap_ahead_right
     *  [10] relspeed_ahead_right
     *  [11] gap_behind_right
     *  [12] relspeed_behind_right
     *  [13] left_lane_exists
     *  [14] right_lane_exists
     *  [15] blind_spot_left
     *  [16] blind_spot_right
     *  [17] lane_index
     *  [18] merge_lockout
     * </pre>
     *
     * @return a new {@code double[19]} array; never {@code null}
     */
    double[] toArray();
}
