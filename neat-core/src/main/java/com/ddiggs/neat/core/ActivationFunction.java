package com.ddiggs.neat.core;

/**
 * A function applied to a node's net input to produce its activation output.
 *
 * <p>Common implementations include sigmoid, tanh, ReLU, and step functions.
 * This is a {@linkplain FunctionalInterface functional interface}, so it can be
 * supplied as a lambda or method reference.
 *
 * <p>Example:
 * <pre>{@code
 * ActivationFunction sigmoid = x -> 1.0 / (1.0 + Math.exp(-x));
 * ActivationFunction tanh    = Math::tanh;
 * }</pre>
 */
@FunctionalInterface
public interface ActivationFunction {

    /**
     * Applies this activation function to the given net input.
     *
     * @param x the weighted sum of inputs (plus bias) arriving at a node
     * @return the node's output activation
     */
    double activate(double x);
}
