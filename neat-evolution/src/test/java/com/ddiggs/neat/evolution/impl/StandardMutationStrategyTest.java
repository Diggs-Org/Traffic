package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.InnovationTrackerImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.core.InnovationTracker;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

/**
 * Tests for {@link StandardMutationStrategy}.
 *
 * <p>All tests are currently failing because {@link StandardMutationStrategy} methods throw
 * {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class StandardMutationStrategyTest {

    private static final long SEED = 99L;
    private static final double PERTURB_STD = 0.1;

    private Genome baseGenome;
    private InnovationTracker tracker;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true));
        baseGenome = new GenomeImpl(nodes, conns);
        tracker = new InnovationTrackerImpl();
    }

    // -------------------------------------------------------------------------
    // Zero-rate mutation tests (rate=0 means no mutation)
    // -------------------------------------------------------------------------

    @Test
    public void testMutate_zeroRates_returnsSameConnectionCount() {
        StandardMutationStrategy noMutation = new StandardMutationStrategy(
                0.0, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = noMutation.mutate(baseGenome, tracker);
        Assert.assertEquals(mutated.getConnectionGenes().size(),
                baseGenome.getConnectionGenes().size(),
                "With all rates=0, connection count must not change");
    }

    @Test
    public void testMutate_zeroWeightRate_weightsUnchanged() {
        // All rates=0, weight must be unchanged
        StandardMutationStrategy noMutation = new StandardMutationStrategy(
                0.0, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = noMutation.mutate(baseGenome, tracker);
        double originalWeight = baseGenome.getConnectionGenes().get(0).getWeight();
        double mutatedWeight = mutated.getConnectionGenes().get(0).getWeight();
        Assert.assertEquals(mutatedWeight, originalWeight, 1e-15,
                "With weightMutationRate=0, weight must remain unchanged");
    }

    // -------------------------------------------------------------------------
    // Unit-rate mutation tests (rate=1 means always mutate)
    // -------------------------------------------------------------------------

    @Test
    public void testMutate_unitWeightRate_weightChanges() {
        StandardMutationStrategy weightOnly = new StandardMutationStrategy(
                1.0, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = weightOnly.mutate(baseGenome, tracker);
        double originalWeight = baseGenome.getConnectionGenes().get(0).getWeight();
        double mutatedWeight = mutated.getConnectionGenes().get(0).getWeight();
        Assert.assertNotEquals(mutatedWeight, originalWeight,
                "With weightMutationRate=1, weight should be perturbed from " + originalWeight);
    }

    @Test
    public void testMutate_unitAddConnectionRate_increasesConnectionCount() {
        // The base genome has 1 connection (1→2); adding a new connection should increase count
        // Note: if the only possible pair is already connected, count stays the same — edge case
        // We use a larger genome to have room for new connections
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.HIDDEN, 0.0),
                new NodeGeneImpl(3, NodeType.OUTPUT, 0.0));
        Genome bigGenome = new GenomeImpl(nodes, List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true)));

        StandardMutationStrategy addConnOnly = new StandardMutationStrategy(
                0.0, 1.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = addConnOnly.mutate(bigGenome, tracker);
        Assert.assertTrue(mutated.getConnectionGenes().size() > bigGenome.getConnectionGenes().size(),
                "With addConnectionRate=1, a new connection should be added");
    }

    @Test
    public void testMutate_unitAddNodeRate_increasesNodeCount() {
        StandardMutationStrategy addNodeOnly = new StandardMutationStrategy(
                0.0, 0.0, 1.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = addNodeOnly.mutate(baseGenome, tracker);
        Assert.assertTrue(mutated.getNodeGenes().size() > baseGenome.getNodeGenes().size(),
                "With addNodeRate=1, a new hidden node should be inserted");
    }

    @Test
    public void testMutate_unitAddNodeRate_disablesOriginalConnection() {
        // Add-node splits an existing connection: original must be disabled
        StandardMutationStrategy addNodeOnly = new StandardMutationStrategy(
                0.0, 0.0, 1.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = addNodeOnly.mutate(baseGenome, tracker);
        // The original connection (inn=1) should now be disabled
        boolean originalDisabled = mutated.getConnectionGenes().stream()
                .anyMatch(cg -> cg.getInnovationNumber() == 1 && !cg.isEnabled());
        Assert.assertTrue(originalDisabled,
                "Add-node mutation must disable the original connection gene");
    }

    @Test
    public void testMutate_unitAddNodeRate_addsTwoNewConnections() {
        StandardMutationStrategy addNodeOnly = new StandardMutationStrategy(
                0.0, 0.0, 1.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = addNodeOnly.mutate(baseGenome, tracker);
        // baseGenome has 1 connection; add-node adds 2 new connections → total = 3 (1 disabled + 2 new)
        Assert.assertEquals(mutated.getConnectionGenes().size(), 3,
                "Add-node must produce 3 total connections (1 disabled original + 2 new)");
    }

    @Test
    public void testMutate_unitToggleRate_flipsEnabledState() {
        StandardMutationStrategy toggleOnly = new StandardMutationStrategy(
                0.0, 0.0, 0.0, 1.0, PERTURB_STD, new Random(SEED));
        Genome mutated = toggleOnly.mutate(baseGenome, tracker);
        boolean originalEnabled = baseGenome.getConnectionGenes().get(0).isEnabled(); // true
        boolean mutatedEnabled = mutated.getConnectionGenes().get(0).isEnabled();
        Assert.assertNotEquals(mutatedEnabled, originalEnabled,
                "With toggleConnectionRate=1, the connection enabled flag should be flipped");
    }

    // -------------------------------------------------------------------------
    // Immutability tests
    // -------------------------------------------------------------------------

    @Test
    public void testMutate_returnsNewGenome_originalUnchanged() {
        StandardMutationStrategy allMutations = new StandardMutationStrategy(
                1.0, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        double originalWeight = baseGenome.getConnectionGenes().get(0).getWeight();
        allMutations.mutate(baseGenome, tracker);
        Assert.assertEquals(baseGenome.getConnectionGenes().get(0).getWeight(), originalWeight, 1e-15,
                "mutate() must not modify the original genome's connection weights");
    }

    @Test
    public void testMutate_returnsNonNullGenome() {
        StandardMutationStrategy s = new StandardMutationStrategy(
                0.5, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        Genome mutated = s.mutate(baseGenome, tracker);
        Assert.assertNotNull(mutated, "mutate() must never return null");
    }

    // -------------------------------------------------------------------------
    // Innovation tracker usage test
    // -------------------------------------------------------------------------

    @Test
    public void testMutate_addNode_usesInnovationTracker() {
        // Add-node must assign innovation numbers through the tracker
        int initialInnovation = tracker.getCurrentInnovationNumber();
        StandardMutationStrategy addNodeOnly = new StandardMutationStrategy(
                0.0, 0.0, 1.0, 0.0, PERTURB_STD, new Random(SEED));
        addNodeOnly.mutate(baseGenome, tracker);
        Assert.assertTrue(tracker.getCurrentInnovationNumber() > initialInnovation,
                "Add-node mutation must obtain new innovation numbers from the tracker");
    }

    // -------------------------------------------------------------------------
    // Null / invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testMutate_nullGenome_throwsNullPointerException() {
        StandardMutationStrategy s = new StandardMutationStrategy(
                0.5, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        s.mutate(null, tracker);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testMutate_nullTracker_throwsNullPointerException() {
        StandardMutationStrategy s = new StandardMutationStrategy(
                0.5, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
        s.mutate(baseGenome, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullRandom_throwsNullPointerException() {
        new StandardMutationStrategy(0.5, 0.5, 0.5, 0.5, PERTURB_STD, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_weightRateAboveOne_throwsIllegalArgumentException() {
        new StandardMutationStrategy(1.1, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_negativeWeightRate_throwsIllegalArgumentException() {
        new StandardMutationStrategy(-0.1, 0.0, 0.0, 0.0, PERTURB_STD, new Random(SEED));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_zeroPerturbStdDev_throwsIllegalArgumentException() {
        new StandardMutationStrategy(0.5, 0.0, 0.0, 0.0, 0.0, new Random(SEED));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_negativePerturbStdDev_throwsIllegalArgumentException() {
        new StandardMutationStrategy(0.5, 0.0, 0.0, 0.0, -0.1, new Random(SEED));
    }
}
