package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ActivationFunction;
import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Feed-forward implementation of {@link NeuralNetwork}.
 *
 * <p>Performs a topological-sort (Kahn's algorithm) activation pass over the network
 * described by the underlying {@link Genome}. Nodes are processed in dependency order
 * (inputs first, outputs last). Disabled connections are ignored.
 * {@link NodeType#BIAS} nodes always contribute an activation of {@code 1.0}.
 *
 * <p>The supplied {@link ActivationFunction} is applied to every {@link NodeType#HIDDEN}
 * and {@link NodeType#OUTPUT} node after summing weighted inputs and adding the node's
 * own bias field.
 *
 * <p><strong>Limitation:</strong> this implementation supports acyclic (feed-forward)
 * topologies only. Passing a genome that contains a cycle will cause {@link #activate}
 * to throw {@link IllegalStateException}.
 */
public class NeuralNetworkImpl implements NeuralNetwork {

    private final Genome genome;
    private final ActivationFunction activationFunction;

    /**
     * Constructs a {@code NeuralNetworkImpl}.
     *
     * @param genome             the genotype from which this phenotype is built; must not be {@code null}
     * @param activationFunction the activation function applied to hidden and output nodes;
     *                           must not be {@code null}
     */
    public NeuralNetworkImpl(Genome genome, ActivationFunction activationFunction) {
        this.genome = genome;
        this.activationFunction = activationFunction;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Input values are assigned to {@link NodeType#INPUT} nodes in the order they appear
     * in the genome's node-gene list (BIAS nodes are excluded from this count). The method
     * then propagates activations through the topology in topological order and returns the
     * activations of all {@link NodeType#OUTPUT} nodes in genome order.
     *
     * @throws IllegalArgumentException if {@code inputs.length} does not match the number of
     *                                  INPUT nodes in the genome, or if any input value is
     *                                  not finite (NaN or infinite)
     * @throws IllegalStateException    if the genome contains a cycle (not a valid feed-forward
     *                                  topology), detected when Kahn's algorithm cannot process
     *                                  all nodes
     */
    @Override
    public double[] activate(double[] inputs) {
        List<NodeGene> nodes       = genome.getNodeGenes();
        List<ConnectionGene> conns = genome.getConnectionGenes();

        // ── 1. Collect INPUT node ids (in genome order) ─────────────────────
        List<Integer> inputIds = new ArrayList<>();
        for (NodeGene node : nodes) {
            if (node.getNodeType() == NodeType.INPUT) {
                inputIds.add(node.getId());
            }
        }
        if (inputs.length != inputIds.size()) {
            throw new IllegalArgumentException(
                    "Expected " + inputIds.size() + " input(s) but got " + inputs.length);
        }
        for (double v : inputs) {
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException(
                        "Input values must be finite; got: " + v);
            }
        }

        // ── 2. Build helper structures ───────────────────────────────────────
        // nodeId → node gene (for fast lookup)
        Map<Integer, NodeGene> nodeMap = new HashMap<>();
        for (NodeGene node : nodes) nodeMap.put(node.getId(), node);

        // nodeId → list of enabled outgoing connections
        Map<Integer, List<ConnectionGene>> outgoing = new HashMap<>();
        for (NodeGene node : nodes) outgoing.put(node.getId(), new ArrayList<>());

        // nodeId → number of enabled incoming connections (in-degree for topo sort)
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (NodeGene node : nodes) inDegree.put(node.getId(), 0);

        for (ConnectionGene conn : conns) {
            if (conn.isEnabled()) {
                outgoing.get(conn.getInNodeId()).add(conn);
                inDegree.merge(conn.getOutNodeId(), 1, Integer::sum);
            }
        }

        // ── 3. Initialise activations and net-inputs ─────────────────────────
        // activations: INPUT → provided value; BIAS → 1.0; others set during propagation
        Map<Integer, Double> activations = new HashMap<>();
        int inputIdx = 0;
        for (NodeGene node : nodes) {
            if (node.getNodeType() == NodeType.INPUT) {
                activations.put(node.getId(), inputs[inputIdx++]);
            } else if (node.getNodeType() == NodeType.BIAS) {
                activations.put(node.getId(), 1.0);
            }
        }

        // netInput for HIDDEN/OUTPUT nodes starts at the node's own bias field
        Map<Integer, Double> netInput = new HashMap<>();
        for (NodeGene node : nodes) netInput.put(node.getId(), node.getBias());

        // ── 4. Kahn's topological sort ───────────────────────────────────────
        // Seed queue with INPUT, BIAS, and any other nodes that have no enabled incoming edges
        Queue<Integer> queue = new LinkedList<>();
        for (NodeGene node : nodes) {
            NodeType type = node.getNodeType();
            if (type == NodeType.INPUT || type == NodeType.BIAS || inDegree.get(node.getId()) == 0) {
                queue.add(node.getId());
            }
        }

        int processedCount = 0;
        while (!queue.isEmpty()) {
            int nodeId   = queue.poll();
            NodeGene gene = nodeMap.get(nodeId);
            NodeType type = gene.getNodeType();

            // Compute this node's activation
            double activation;
            if (type == NodeType.INPUT) {
                activation = activations.get(nodeId);          // raw input
            } else if (type == NodeType.BIAS) {
                activation = 1.0;                              // fixed bias source
            } else {
                activation = activationFunction.activate(netInput.get(nodeId));
            }
            activations.put(nodeId, activation);
            processedCount++;

            // Propagate to downstream nodes via enabled connections
            for (ConnectionGene conn : outgoing.get(nodeId)) {
                int downId = conn.getOutNodeId();
                netInput.merge(downId, activation * conn.getWeight(), Double::sum);
                int remaining = inDegree.merge(downId, -1, Integer::sum);
                if (remaining == 0) {
                    queue.add(downId);
                }
            }
        }

        // Nodes left unprocessed means a cycle prevented them from ever reaching in-degree 0
        if (processedCount < nodes.size()) {
            throw new IllegalStateException(
                    "Cyclic genome detected: " + (nodes.size() - processedCount)
                    + " node(s) could not be processed — genome is not a valid feed-forward topology");
        }

        // ── 5. Collect OUTPUT activations in genome order ────────────────────
        return nodes.stream()
                .filter(n -> n.getNodeType() == NodeType.OUTPUT)
                .mapToDouble(n -> activations.get(n.getId()))
                .toArray();
    }

    /** {@inheritDoc} */
    @Override
    public Genome getGenome() {
        return genome;
    }
}
