package com.ddiggs.neat.core.impl;

import com.ddiggs.neat.core.ActivationFunction;
import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * TDD tests for {@link NeuralNetworkImpl}.
 *
 * <p>All tests in this class are expected to <strong>fail</strong> until Phase 2
 * provides the concrete implementation.
 *
 * <p>Tests use a sigmoid activation function ({@code σ(x) = 1/(1+e^{-x})}) defined
 * as a lambda, matching the example in {@link ActivationFunction}'s Javadoc.
 */
public class NeuralNetworkImplTest {

    /** σ(x) = 1 / (1 + e^{-x}) */
    private static final ActivationFunction SIGMOID = x -> 1.0 / (1.0 + Math.exp(-x));

    /**
     * Minimal feed-forward network: 1 INPUT → 1 OUTPUT with weight 1.0, no bias.
     * Expected output for input {@code v}: σ(v × 1.0 + 0.0) = σ(v).
     */
    private NeuralNetworkImpl minimalNet;
    private GenomeImpl        minimalGenome;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 1.0, true)
        );
        minimalGenome = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
        minimalNet    = new NeuralNetworkImpl(minimalGenome, SIGMOID);
    }

    // -------------------------------------------------------------------------
    // getGenome()
    // -------------------------------------------------------------------------

    @Test
    public void testGetGenome_returnsConstructorGenome() {
        Assert.assertSame(minimalNet.getGenome(), minimalGenome,
                "getGenome() must return the exact Genome instance supplied to the constructor");
    }

    // -------------------------------------------------------------------------
    // activate() — argument validation
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testActivate_emptyInputArray_throwsIllegalArgumentException() {
        // Minimal network has 1 INPUT node; empty array is the wrong size
        minimalNet.activate(new double[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testActivate_tooManyInputs_throwsIllegalArgumentException() {
        minimalNet.activate(new double[]{0.5, 0.5}); // expects 1 input
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testActivate_tooFewInputs_throwsIllegalArgumentException() {
        // Build a network that needs 2 inputs
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.INPUT,  0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 2, 1.0, true),
                new ConnectionGeneImpl(1, 2, 1, 2, 1.0, true)
        );
        Genome twoInputGenome = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
        NeuralNetworkImpl twoInputNet = new NeuralNetworkImpl(twoInputGenome, SIGMOID);
        twoInputNet.activate(new double[]{0.5}); // only 1 provided, needs 2
    }

    // -------------------------------------------------------------------------
    // activate() — output array shape
    // -------------------------------------------------------------------------

    @Test
    public void testActivate_returnsNonNull() {
        Assert.assertNotNull(minimalNet.activate(new double[]{0.0}));
    }

    @Test
    public void testActivate_returnsSingleOutputForMinimalNet() {
        double[] output = minimalNet.activate(new double[]{0.0});
        Assert.assertEquals(output.length, 1,
                "Minimal network has 1 OUTPUT node; activate() must return an array of length 1");
    }

    @Test
    public void testActivate_twoOutputNodes_returnsTwoValues() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 1.0, true),
                new ConnectionGeneImpl(1, 2, 0, 2, 1.0, true)
        );
        Genome g = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
        NeuralNetworkImpl net = new NeuralNetworkImpl(g, SIGMOID);
        double[] out = net.activate(new double[]{1.0});
        Assert.assertEquals(out.length, 2,
                "Network with 2 OUTPUT nodes must return an array of length 2");
    }

    // -------------------------------------------------------------------------
    // activate() — correctness: simple feed-forward
    // -------------------------------------------------------------------------

    @Test
    public void testActivate_zeroInput_returnsSigmoidZero() {
        // σ(0.0 × 1.0 + bias 0.0) = σ(0) = 0.5
        double[] output = minimalNet.activate(new double[]{0.0});
        Assert.assertEquals(output[0], 0.5, 1e-9,
                "σ(0) must equal 0.5");
    }

    @Test
    public void testActivate_unitInput_returnsSigmoidOne() {
        // σ(1.0 × 1.0) = σ(1) ≈ 0.7311
        double expected = 1.0 / (1.0 + Math.exp(-1.0));
        double[] output = minimalNet.activate(new double[]{1.0});
        Assert.assertEquals(output[0], expected, 1e-9,
                "σ(1) must equal " + expected);
    }

    @Test
    public void testActivate_negativeInput_returnsLessThanHalf() {
        double[] output = minimalNet.activate(new double[]{-2.0});
        Assert.assertTrue(output[0] < 0.5,
                "σ(negative) must be less than 0.5");
    }

    @Test
    public void testActivate_scaledWeight_affectsOutput() {
        // weight = 2.0: σ(1.0 × 2.0) ≠ σ(1.0 × 1.0)
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 2.0, true)
        );
        NeuralNetworkImpl heavyNet =
                new NeuralNetworkImpl(new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns)), SIGMOID);
        double defaultOut = minimalNet.activate(new double[]{1.0})[0];
        double heavyOut   = heavyNet.activate(new double[]{1.0})[0];
        Assert.assertNotEquals(defaultOut, heavyOut, 1e-9,
                "Doubling the weight must change the output activation");
    }

    // -------------------------------------------------------------------------
    // activate() — disabled connections are excluded
    // -------------------------------------------------------------------------

    @Test
    public void testActivate_disabledConnection_doesNotContribute() {
        // Disabled-only network: output should equal σ(0) = 0.5 (no signal gets through)
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> disabledConns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 99.0, false) // disabled
        );
        GenomeImpl disabledGenome = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(disabledConns));
        NeuralNetworkImpl disabledNet = new NeuralNetworkImpl(disabledGenome, SIGMOID);

        double[] output = disabledNet.activate(new double[]{1.0});
        Assert.assertEquals(output[0], 0.5, 1e-9,
                "A disabled connection must not contribute to the output; σ(0+bias0) = 0.5");
    }

    // -------------------------------------------------------------------------
    // activate() — BIAS node always contributes 1.0
    // -------------------------------------------------------------------------

    @Test
    public void testActivate_biasNode_contributesFixed1() {
        // Network: 1 INPUT, 1 BIAS (always outputs 1.0), 1 OUTPUT
        // Connections: INPUT→OUTPUT (weight 0.0), BIAS→OUTPUT (weight 0.5)
        // Output: σ(0.0*0.0 + 1.0*0.5 + bias_of_output_node=0.0) = σ(0.5)
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.BIAS,   0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0)
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 2, 0.0, true),  // INPUT → OUTPUT weight 0
                new ConnectionGeneImpl(1, 2, 1, 2, 0.5, true)   // BIAS  → OUTPUT weight 0.5
        );
        GenomeImpl biasGenome = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
        NeuralNetworkImpl biasNet = new NeuralNetworkImpl(biasGenome, SIGMOID);

        double expected = 1.0 / (1.0 + Math.exp(-0.5)); // σ(0.5)
        double[] output = biasNet.activate(new double[]{0.0}); // 1 INPUT node
        Assert.assertEquals(output[0], expected, 1e-9,
                "BIAS node (fixed output 1.0) * weight 0.5 → output must be σ(0.5)");
    }

    // -------------------------------------------------------------------------
    // activate() — node-level bias field is added to net input
    // -------------------------------------------------------------------------

    @Test
    public void testActivate_nodeBiasShiftsOutput() {
        // OUTPUT node has bias = 1.0; no connections → net input = 0.0 + bias 1.0 = 1.0
        // Expected: σ(1.0)
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(0, NodeType.INPUT,  0.0),
                new NodeGeneImpl(1, NodeType.OUTPUT, 1.0)  // ← node bias = 1.0
        );
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(0, 1, 0, 1, 0.0, true)  // weight = 0 → contributes 0
        );
        GenomeImpl biasedOutputGenome = new GenomeImpl(new ArrayList<>(nodes), new ArrayList<>(conns));
        NeuralNetworkImpl biasedNet = new NeuralNetworkImpl(biasedOutputGenome, SIGMOID);

        double expected = 1.0 / (1.0 + Math.exp(-1.0)); // σ(1.0)
        double[] output = biasedNet.activate(new double[]{0.0});
        Assert.assertEquals(output[0], expected, 1e-9,
                "Node bias of 1.0 on OUTPUT node must shift activation from σ(0) to σ(1)");
    }
}
