package com.ddiggs.neat.core;

/**
 * Classifies a node (neuron) within a NEAT genome.
 *
 * <ul>
 *   <li>{@link #INPUT}  — receives external observations; no incoming connections.</li>
 *   <li>{@link #HIDDEN} — internal processing node; may appear or disappear via mutation.</li>
 *   <li>{@link #OUTPUT} — produces the network's final activations.</li>
 *   <li>{@link #BIAS}   — always outputs 1.0; acts as a per-node bias source.</li>
 * </ul>
 */
public enum NodeType {
    INPUT,
    HIDDEN,
    OUTPUT,
    BIAS
}
