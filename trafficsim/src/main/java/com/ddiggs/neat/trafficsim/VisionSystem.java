package com.ddiggs.neat.trafficsim;

import java.util.List;

/**
 * Computes the {@link SensorInput} observation vector for a car in a given tick.
 *
 * <p>The vision system implements the sensing model described in
 * {@code docs/SIMULATION.md} Section 5. For each tick it scans the current lane,
 * the left adjacent lane, and the right adjacent lane — both forward and rearward —
 * returning the nearest car in each direction per lane. It also applies:
 * <ul>
 *   <li><em>Occlusion</em>: only the nearest car per direction per lane is returned;
 *       cars behind a closer obstacle are not visible.</li>
 *   <li><em>Blind-spot detection</em>: a car that falls within the lateral blind-spot
 *       zone triggers the corresponding flag and replaces the gap/speed readings with
 *       worst-case values (gap = 0.0, relative speed = 0.5).</li>
 *   <li><em>Merging car visibility</em>: a car currently merging is visible from both
 *       its source lane and its target lane for the full duration of the merge.</li>
 *   <li><em>Normalisation</em>: current-lane gaps are divided by
 *       {@link CarPhysics#getVisionRange()}; adjacent-lane gaps by
 *       {@link CarPhysics#getAdjacentVisionRange()}. Speed differentials are mapped
 *       into [0.0, 1.0] with 0.5 representing equal speeds.</li>
 * </ul>
 */
public interface VisionSystem {

    /**
     * Produces the 19-element {@link SensorInput} observation for the given car.
     *
     * <p>The observation is computed from the positions and speeds of all currently
     * active cars on the road as of the start of the current tick. The ego car itself
     * is excluded from sensor readings.
     *
     * @param ego      the car for which the observation is being computed; never {@code null}
     * @param road     the road geometry used to determine lane boundaries and existence;
     *                 never {@code null}
     * @param allCars  all cars currently active in the simulation, including {@code ego};
     *                 never {@code null}
     * @param config   the simulation configuration providing normalisation constants;
     *                 never {@code null}
     * @return the computed sensor observation; never {@code null}
     */
    SensorInput observe(Car ego, Road road, List<Car> allCars, SimulationConfig config);
}
