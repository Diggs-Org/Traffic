package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.InnovationTracker;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.evolution.MutationStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Standard NEAT mutation strategy supporting four independent mutation operators.
 */
public class StandardMutationStrategy implements MutationStrategy {

    private final double weightMutationRate;
    private final double addConnectionRate;
    private final double addNodeRate;
    private final double toggleConnectionRate;
    private final double perturbStdDev;
    private final Random random;

    public StandardMutationStrategy(double weightMutationRate,
                                    double addConnectionRate,
                                    double addNodeRate,
                                    double toggleConnectionRate,
                                    double perturbStdDev,
                                    Random random) {
        validateRate(weightMutationRate, "weightMutationRate");
        validateRate(addConnectionRate, "addConnectionRate");
        validateRate(addNodeRate, "addNodeRate");
        validateRate(toggleConnectionRate, "toggleConnectionRate");
        if (perturbStdDev <= 0.0) {
            throw new IllegalArgumentException("perturbStdDev must be positive, got: " + perturbStdDev);
        }
        Objects.requireNonNull(random, "random must not be null");
        this.weightMutationRate = weightMutationRate;
        this.addConnectionRate = addConnectionRate;
        this.addNodeRate = addNodeRate;
        this.toggleConnectionRate = toggleConnectionRate;
        this.perturbStdDev = perturbStdDev;
        this.random = random;
    }

    private static void validateRate(double rate, String name) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException(name + " must be in [0, 1], got: " + rate);
        }
    }

    @Override
    public Genome mutate(Genome genome, InnovationTracker tracker) {
        Objects.requireNonNull(genome, "genome must not be null");
        Objects.requireNonNull(tracker, "tracker must not be null");

        List<NodeGene> nodes = new ArrayList<>(genome.getNodeGenes());
        List<ConnectionGene> conns = new ArrayList<>(genome.getConnectionGenes());

        // 1. Weight mutation
        if (random.nextDouble() < weightMutationRate) {
            conns = mutateWeights(conns);
        }

        // 2. Add connection
        if (random.nextDouble() < addConnectionRate) {
            conns = addConnection(nodes, conns, tracker);
        }

        // 3. Add node
        if (random.nextDouble() < addNodeRate) {
            addNode(nodes, conns, tracker);
            // nodes and conns are mutated in-place by addNode
        }

        // 4. Toggle connection
        if (random.nextDouble() < toggleConnectionRate) {
            conns = toggleConnection(conns);
        }

        return new GenomeImpl(nodes, conns);
    }

    private List<ConnectionGene> mutateWeights(List<ConnectionGene> conns) {
        List<ConnectionGene> result = new ArrayList<>(conns.size());
        for (ConnectionGene cg : conns) {
            double newWeight = cg.getWeight() + random.nextGaussian() * perturbStdDev;
            result.add(new ConnectionGeneImpl(
                    cg.getId(), cg.getInnovationNumber(),
                    cg.getInNodeId(), cg.getOutNodeId(),
                    newWeight, cg.isEnabled()));
        }
        return result;
    }

    private List<ConnectionGene> addConnection(List<NodeGene> nodes,
                                               List<ConnectionGene> conns,
                                               InnovationTracker tracker) {
        Set<String> existing = new HashSet<>();
        for (ConnectionGene cg : conns) {
            existing.add(cg.getInNodeId() + ":" + cg.getOutNodeId());
        }

        List<int[]> candidates = new ArrayList<>();
        for (NodeGene from : nodes) {
            for (NodeGene to : nodes) {
                if (to.getNodeType() == NodeType.INPUT) continue;
                if (from.getId() == to.getId()) continue;
                if (!existing.contains(from.getId() + ":" + to.getId())) {
                    candidates.add(new int[]{from.getId(), to.getId()});
                }
            }
        }

        if (candidates.isEmpty()) {
            return conns;
        }

        int[] pair = candidates.get(random.nextInt(candidates.size()));
        int innovNum = tracker.getInnovationNumber(pair[0], pair[1]);
        double weight = random.nextGaussian();
        List<ConnectionGene> result = new ArrayList<>(conns);
        result.add(new ConnectionGeneImpl(innovNum, innovNum, pair[0], pair[1], weight, true));
        return result;
    }

    /** Mutates {@code nodes} and {@code conns} in-place to add a new hidden node. */
    private void addNode(List<NodeGene> nodes,
                         List<ConnectionGene> conns,
                         InnovationTracker tracker) {
        List<Integer> enabledIndices = new ArrayList<>();
        for (int i = 0; i < conns.size(); i++) {
            if (conns.get(i).isEnabled()) enabledIndices.add(i);
        }

        if (enabledIndices.isEmpty()) return;

        int splitIdx = enabledIndices.get(random.nextInt(enabledIndices.size()));
        ConnectionGene split = conns.get(splitIdx);

        // New node
        int newNodeId = nodes.stream().mapToInt(NodeGene::getId).max().orElse(0) + 1;
        nodes.add(new NodeGeneImpl(newNodeId, NodeType.HIDDEN, 0.0));

        // Disable original connection in-place
        conns.set(splitIdx, new ConnectionGeneImpl(
                split.getId(), split.getInnovationNumber(),
                split.getInNodeId(), split.getOutNodeId(),
                split.getWeight(), false));

        // in-node → new node (weight 1.0)
        int innov1 = tracker.getInnovationNumber(split.getInNodeId(), newNodeId);
        conns.add(new ConnectionGeneImpl(innov1, innov1,
                split.getInNodeId(), newNodeId, 1.0, true));

        // new node → out-node (original weight)
        int innov2 = tracker.getInnovationNumber(newNodeId, split.getOutNodeId());
        conns.add(new ConnectionGeneImpl(innov2, innov2,
                newNodeId, split.getOutNodeId(), split.getWeight(), true));
    }

    private List<ConnectionGene> toggleConnection(List<ConnectionGene> conns) {
        if (conns.isEmpty()) return conns;
        int idx = random.nextInt(conns.size());
        List<ConnectionGene> result = new ArrayList<>(conns);
        ConnectionGene cg = result.get(idx);
        result.set(idx, new ConnectionGeneImpl(
                cg.getId(), cg.getInnovationNumber(),
                cg.getInNodeId(), cg.getOutNodeId(),
                cg.getWeight(), !cg.isEnabled()));
        return result;
    }
}
