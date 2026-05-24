package com.ddiggs.neat.training;

/**
 * The task environment in which NEAT agents are evaluated.
 *
 * <p>Implementors encapsulate one episode of interaction between a neural network
 * and a problem domain (e.g. a physics simulation, a game, or a benchmark function).
 * The environment follows the standard reset–observe–step–reward loop:
 *
 * <ol>
 *   <li>{@link #reset()} — initialise or reinitialise the episode.</li>
 *   <li>{@link #observe()} — return the current state as a numeric vector.</li>
 *   <li>{@link #step(double[])} — apply the agent's actions.</li>
 *   <li>{@link #reward()} — query the reward earned by the last action.</li>
 *   <li>Repeat 2–4 until {@link #isDone()} returns {@code true}.</li>
 * </ol>
 *
 * <p>Concrete implementations live in the {@code neat-reward} module (future).
 */
public interface TrainingEnvironment {

    /**
     * Resets the environment to its initial state, ready for a new episode.
     */
    void reset();

    /**
     * Returns the current observation (state) of the environment.
     *
     * <p>The length of the returned array must always equal {@link #getObservationSize()}.
     *
     * @return a snapshot of the environment's state; never {@code null}
     */
    double[] observe();

    /**
     * Advances the environment by one time step using the given actions.
     *
     * <p>The length of {@code actions} must equal {@link #getActionSize()}.
     *
     * @param actions the agent's output vector; never {@code null}
     */
    void step(double[] actions);

    /**
     * Returns {@code true} when the current episode has ended (success, failure, or timeout).
     *
     * @return {@code true} if the episode is over
     */
    boolean isDone();

    /**
     * Returns the scalar reward earned by the agent during the last {@link #step}.
     *
     * @return the immediate reward; may be negative
     */
    double reward();

    /**
     * Returns the fixed length of the observation vector produced by {@link #observe()}.
     *
     * <p>This value must match the number of {@link com.ddiggs.neat.core.NodeType#INPUT}
     * nodes in every network evaluated by this environment.
     *
     * @return a positive integer
     */
    int getObservationSize();

    /**
     * Returns the fixed length of the action vector consumed by {@link #step}.
     *
     * <p>This value must match the number of {@link com.ddiggs.neat.core.NodeType#OUTPUT}
     * nodes in every network evaluated by this environment.
     *
     * @return a positive integer
     */
    int getActionSize();
}
