package com.ddiggs.neat.trafficsim;

/**
 * Represents the static geometry of the highway used in a traffic simulation.
 *
 * <p>The highway is a straight, multi-lane road running left-to-right along the X axis.
 * Lane indices increase from left (fast lane, index 0) to right (slow lane, index
 * {@code lane_count - 1}). The Y coordinate of each lane is fixed for the duration of
 * an experiment.
 *
 * <p>A {@code Road} is constructed once per experiment from a {@link SimulationConfig}
 * and shared across all simulation ticks. It provides the canonical reference for
 * lane geometry queries used by {@link VisionSystem}, {@link SpawnStrategy}, and the
 * {@link Simulation} itself.
 */
public interface Road {

    /**
     * Returns the total X extent of the highway in metres.
     *
     * <p>Cars whose front edge ({@code x + L/2}) reaches or exceeds this value are
     * counted as successful exits.
     *
     * @return highway length in metres; positive
     */
    double getHighwayLength();

    /**
     * Returns the number of parallel lanes on the highway.
     *
     * <p>Valid lane indices are in the range [{@code 0}, {@code getLaneCount() - 1}].
     *
     * @return lane count; positive
     */
    int getLaneCount();

    /**
     * Returns the lateral (Y) width of each lane in metres.
     *
     * <p>All lanes share the same width. Lane {@code i} occupies Y &isin;
     * [{@code i * lane_width}, {@code (i+1) * lane_width}].
     *
     * @return lane width in metres; positive
     */
    double getLaneWidth();

    /**
     * Returns the Y coordinate of the lower boundary of the given lane.
     *
     * <p>Equivalent to {@code lane * getLaneWidth()}.
     *
     * @param lane the zero-based lane index
     * @return lower Y boundary of the lane in metres
     * @throws IllegalArgumentException if {@code lane} is not a valid lane index
     */
    double getLaneLowerY(int lane);

    /**
     * Returns the Y coordinate of the centre of the given lane.
     *
     * <p>Equivalent to {@code (lane + 0.5) * getLaneWidth()}. Spawned cars are placed
     * at the lane centre and lane-change interpolation targets this Y coordinate.
     *
     * @param lane the zero-based lane index
     * @return centre Y of the lane in metres
     * @throws IllegalArgumentException if {@code lane} is not a valid lane index
     */
    double getLaneCenterY(int lane);

    /**
     * Returns the Y coordinate of the upper boundary of the given lane.
     *
     * <p>Equivalent to {@code (lane + 1) * getLaneWidth()}.
     *
     * @param lane the zero-based lane index
     * @return upper Y boundary of the lane in metres
     * @throws IllegalArgumentException if {@code lane} is not a valid lane index
     */
    double getLaneUpperY(int lane);

    /**
     * Returns {@code true} if the given lane index is valid for this road.
     *
     * <p>A lane index is valid when it satisfies {@code 0 <= lane < getLaneCount()}.
     *
     * @param lane the lane index to test
     * @return {@code true} if the lane exists; {@code false} otherwise
     */
    boolean isValidLane(int lane);

    /**
     * Returns the {@link SimulationConfig} from which this road was constructed.
     *
     * @return the simulation configuration; never {@code null}
     */
    SimulationConfig getConfig();
}
