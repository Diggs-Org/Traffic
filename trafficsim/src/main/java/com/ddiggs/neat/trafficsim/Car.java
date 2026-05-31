package com.ddiggs.neat.trafficsim;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;

/**
 * A car participating in the traffic simulation.
 *
 * <p>Each car is the physical instantiation of one NEAT {@link Genome}: its
 * {@link NeuralNetwork} phenotype receives a {@link SensorInput} observation each tick
 * and produces a {@link DriveCommand} that the simulation uses to update the car's
 * {@link CarState}. The car's {@link CarPhysics} are fixed at spawn and define the
 * physical constraints under which the network operates.
 *
 * <p>A car is considered active from spawn until it is removed by exit, collision, or
 * stall culling. The {@link GenerationResult} records the final {@link FitnessRecord}
 * for each car after the generation run ends.
 */
public interface Car {

    /**
     * Returns the unique identifier for this car within its generation.
     *
     * <p>IDs are assigned at spawn and do not change during the car's lifetime.
     *
     * @return car identifier; non-negative
     */
    long getId();

    /**
     * Returns the fixed physical properties of this car.
     *
     * @return car physics; never {@code null}
     */
    CarPhysics getPhysics();

    /**
     * Returns the current mutable state of this car.
     *
     * <p>The returned object reflects the state as of the end of the most recently
     * completed tick. Callers must not cache the return value across ticks.
     *
     * @return current car state; never {@code null}
     */
    CarState getState();

    /**
     * Returns the neural network that drives this car's behaviour.
     *
     * <p>Each tick, the simulation calls {@link NeuralNetwork#activate(double[])} with
     * the 19-element {@link SensorInput} array and interprets the 4-element output as
     * a {@link DriveCommand}.
     *
     * @return this car's neural network; never {@code null}
     */
    NeuralNetwork getNeuralNetwork();

    /**
     * Returns the NEAT genome from which this car's neural network was constructed.
     *
     * <p>The genome is used to map simulation results back to NEAT fitness scores via
     * {@link GenerationResult#getFitnessRecord(Genome)}.
     *
     * @return the underlying genome; never {@code null}
     */
    Genome getGenome();
}
