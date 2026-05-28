package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Standard implementation of {@link Genome}.
 *
 * <p>Stores an immutable (unmodifiable) snapshot of node genes and connection genes.
 * The compatibility-distance formula follows the original NEAT paper:
 * <pre>
 *   δ = (c1 × E + c2 × D) / N   +   c3 × W̄
 * </pre>
 * where {@code E} = excess genes, {@code D} = disjoint genes, {@code W̄} = mean absolute
 * weight difference of matching genes, and {@code N} = max(|genes_a|, |genes_b|, 1) for
 * genome-size normalisation.
 *
 * <p>A gene is <em>excess</em> if its innovation number is beyond the highest innovation
 * number of the shorter genome. A gene is <em>disjoint</em> if it falls within the
 * overlapping range but is absent from one genome.
 *
 * <h3>Serialisation format</h3>
 * <pre>
 *   int nodeCount          (4 bytes, little-endian)
 *   nodeCount × 16 bytes   (one {@link NodeGeneImpl} per entry)
 *   int connectionCount    (4 bytes, little-endian)
 *   connectionCount × 25   (one {@link ConnectionGeneImpl} per entry)
 * </pre>
 */
public class GenomeImpl implements Genome {

    private final List<NodeGene> nodeGenes;
    private final List<ConnectionGene> connectionGenes;

    /**
     * Constructs a {@code GenomeImpl} from the supplied gene lists.
     *
     * <p>The lists are defensively copied and wrapped in an unmodifiable view.
     *
     * @param nodeGenes       ordered list of node genes; must not be {@code null}
     * @param connectionGenes ordered list of connection genes; must not be {@code null}
     */
    public GenomeImpl(List<NodeGene> nodeGenes, List<ConnectionGene> connectionGenes) {
        this.nodeGenes        = Collections.unmodifiableList(new ArrayList<>(nodeGenes));
        this.connectionGenes  = Collections.unmodifiableList(new ArrayList<>(connectionGenes));
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of this genome's node genes
     */
    @Override
    public List<NodeGene> getNodeGenes() {
        return nodeGenes;
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of this genome's connection genes
     */
    @Override
    public List<ConnectionGene> getConnectionGenes() {
        return connectionGenes;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Aligns connection genes by innovation number to classify each gene as
     * matching, disjoint, or excess, then applies the NEAT compatibility formula.
     */
    @Override
    public double compatibilityDistance(Genome other, double c1, double c2, double c3) {
        List<ConnectionGene> genesA = this.getConnectionGenes();
        List<ConnectionGene> genesB = other.getConnectionGenes();

        // Build innovation → weight maps for each genome
        Map<Integer, Double> weightsA = new HashMap<>();
        for (ConnectionGene g : genesA) weightsA.put(g.getInnovationNumber(), g.getWeight());

        Map<Integer, Double> weightsB = new HashMap<>();
        for (ConnectionGene g : genesB) weightsB.put(g.getInnovationNumber(), g.getWeight());

        // The excess/disjoint boundary: genes beyond the shorter genome's max are excess
        int maxInnA = genesA.stream().mapToInt(ConnectionGene::getInnovationNumber).max().orElse(0);
        int maxInnB = genesB.stream().mapToInt(ConnectionGene::getInnovationNumber).max().orElse(0);
        int boundary = Math.min(maxInnA, maxInnB); // innovations > boundary are excess

        // Walk all innovation numbers present in either genome
        Set<Integer> allInnovations = new HashSet<>();
        allInnovations.addAll(weightsA.keySet());
        allInnovations.addAll(weightsB.keySet());

        int excess = 0;
        int disjoint = 0;
        double weightDiffSum = 0.0;
        int matching = 0;

        for (int inn : allInnovations) {
            boolean inA = weightsA.containsKey(inn);
            boolean inB = weightsB.containsKey(inn);
            if (inA && inB) {
                matching++;
                weightDiffSum += Math.abs(weightsA.get(inn) - weightsB.get(inn));
            } else if (inn > boundary) {
                excess++;
            } else {
                disjoint++;
            }
        }

        int n = Math.max(Math.max(genesA.size(), genesB.size()), 1);
        double wBar = matching > 0 ? weightDiffSum / matching : 0.0;
        return (c1 * excess + c2 * disjoint) / n + c3 * wBar;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Encodes node count, all node genes, connection count, and all connection
     * genes in sequence.
     */
    @Override
    public byte[] toBytes() {
        int totalBytes = Integer.BYTES
                + nodeGenes.size() * NodeGeneImpl.BYTE_LENGTH
                + Integer.BYTES
                + connectionGenes.size() * ConnectionGeneImpl.BYTE_LENGTH;

        ByteBuffer buf = ByteBuffer.allocate(totalBytes).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(nodeGenes.size());
        for (NodeGene node : nodeGenes) {
            buf.put(node.toBytes());
        }
        buf.putInt(connectionGenes.size());
        for (ConnectionGene conn : connectionGenes) {
            buf.put(conn.toBytes());
        }
        return buf.array();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code data} is {@code null} or malformed
     */
    @Override
    public GenomeImpl fromBytes(byte[] data) {
        if (data == null || data.length < Integer.BYTES) {
            throw new IllegalArgumentException(
                    "Data too short to decode GenomeImpl: "
                    + (data == null ? "null" : data.length + " bytes"));
        }
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int nodeCount = buf.getInt();
        int minRequired = Integer.BYTES + nodeCount * NodeGeneImpl.BYTE_LENGTH + Integer.BYTES;
        if (data.length < minRequired) {
            throw new IllegalArgumentException(
                    "Data too short to decode " + nodeCount + " node genes");
        }

        NodeGeneImpl nodeProto = new NodeGeneImpl(0, NodeType.INPUT, 0.0);
        List<NodeGene> nodes = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            byte[] nodeBytes = new byte[NodeGeneImpl.BYTE_LENGTH];
            buf.get(nodeBytes);
            nodes.add(nodeProto.fromBytes(nodeBytes));
        }

        int connCount = buf.getInt();
        int expectedTotal = minRequired + connCount * ConnectionGeneImpl.BYTE_LENGTH;
        if (data.length < expectedTotal) {
            throw new IllegalArgumentException(
                    "Data too short to decode " + connCount + " connection genes");
        }

        ConnectionGeneImpl connProto = new ConnectionGeneImpl(0, 0, 0, 0, 0.0, true);
        List<ConnectionGene> conns = new ArrayList<>(connCount);
        for (int i = 0; i < connCount; i++) {
            byte[] connBytes = new byte[ConnectionGeneImpl.BYTE_LENGTH];
            buf.get(connBytes);
            conns.add(connProto.fromBytes(connBytes));
        }

        if (buf.hasRemaining()) {
            throw new IllegalArgumentException(
                    "Malformed genome data: " + buf.remaining() + " unexpected trailing byte(s)");
        }

        return new GenomeImpl(nodes, conns);
    }
}
