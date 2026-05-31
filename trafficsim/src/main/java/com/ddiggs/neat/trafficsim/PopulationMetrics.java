package com.ddiggs.neat.trafficsim;

/**
 * Aggregate statistics recorded at the end of each generation run.
 *
 * <p>These metrics quantify the collective behaviour of the entire population and are
 * intended for researcher observation and early-stopping decisions. They do <em>not</em>
 * feed back into NEAT's selection loop directly.
 *
 * <p>Metric definitions follow {@code docs/SIMULATION.md} Section 8.2. Target values
 * documented on each accessor reflect the desired end-state of a well-trained population.
 */
public interface PopulationMetrics {

    /**
     * Returns the fraction of spawned cars that successfully exited the highway.
     *
     * <p>Computed as {@code successful_exits / spawned_count}. A value of 1.0 indicates
     * that every car traversed the full highway length without collision or stalling.
     *
     * @return throughput in [0.0, 1.0]; target value &rarr; 1.0
     */
    double getThroughput();

    /**
     * Returns the mean longitudinal speed across all cars in the generation, in m/s.
     *
     * <p>Computed as the mean of each car's {@link FitnessRecord#getAverageVx()}. A
     * well-trained population should converge toward the population-average
     * {@link CarPhysics#getTargetSpeed()}.
     *
     * @return average speed in m/s; non-negative; target value &rarr; mean v_target
     */
    double getAverageSpeed();

    /**
     * Returns the standard deviation of per-car average speeds, in m/s.
     *
     * <p>A moderate variance indicates that multiple speed archetypes (slow, medium,
     * fast) coexist in the population, which is required for highway diversity. Very low
     * variance signals convergence to a single strategy.
     *
     * @return speed standard deviation in m/s; non-negative; target: moderate
     */
    double getSpeedVariance();

    /**
     * Returns the collision rate for this generation.
     *
     * <p>Computed as {@code total_collision_events / spawned_count}. Lower is better.
     *
     * @return collision rate; non-negative; target value &rarr; 0.0
     */
    double getCollisionRate();

    /**
     * Returns the fraction of total car-ticks in which a near-miss was recorded.
     *
     * <p>Computed as {@code total_near_miss_ticks / total_car_ticks} across all cars.
     * A near-miss tick is any tick where the forward gap falls below
     * {@code g_min / 2}. Lower is better.
     *
     * @return near-miss rate in [0.0, 1.0]; target value &rarr; 0.0
     */
    double getNearMissRate();

    /**
     * Returns the diversity index for this generation.
     *
     * <p>Computed as the standard deviation of individual
     * {@link FitnessRecord#computeFitness(SimulationConfig)} scores across all cars.
     * A higher value indicates a more heterogeneous population, which guards against
     * the brittleness of a single dominant strategy. See
     * {@code docs/SIMULATION.md} Section 9 for the diversity requirements.
     *
     * @return fitness diversity index; non-negative; higher values indicate more diversity
     */
    double getDiversityIndex();
}
