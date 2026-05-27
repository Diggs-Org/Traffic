package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for {@link StandardCrossoverStrategy}.
 *
 * <p>All tests are currently failing because {@link StandardCrossoverStrategy} methods throw
 * {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class StandardCrossoverStrategyTest {

    private static final long SEED = 12345L;

    private Genome parent1; // fitter parent: innovations 1, 2, 3
    private Genome parent2; // weaker parent: innovations 1, 2, 4 (4 is excess vs parent1's max=3)
    private StandardCrossoverStrategy strategy;

    @BeforeMethod
    public void setUp() {
        strategy = new StandardCrossoverStrategy(new Random(SEED));

        List<NodeGene> sharedNodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.HIDDEN, 0.0),
                new NodeGeneImpl(3, NodeType.OUTPUT, 0.0));

        // parent1 has innovations 1, 2, 3
        parent1 = new GenomeImpl(sharedNodes, List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true),
                new ConnectionGeneImpl(2, 2, 2, 3, 2.0, true),
                new ConnectionGeneImpl(3, 3, 1, 3, 0.5, false)));

        // parent2 has innovations 1, 2 (matching) and 4 (excess beyond parent1's max of 3)
        parent2 = new GenomeImpl(sharedNodes, List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 9.0, true),
                new ConnectionGeneImpl(2, 2, 2, 3, 8.0, true),
                new ConnectionGeneImpl(4, 4, 1, 3, 7.0, true)));
    }

    // -------------------------------------------------------------------------
    // Basic result tests
    // -------------------------------------------------------------------------

    @Test
    public void testCrossover_returnsNonNullChild() {
        Genome child = strategy.crossover(parent1, parent2);
        Assert.assertNotNull(child, "crossover() must never return null");
    }

    @Test
    public void testCrossover_childHasNodeGenesFromParent1() {
        Genome child = strategy.crossover(parent1, parent2);
        Assert.assertEquals(child.getNodeGenes().size(), parent1.getNodeGenes().size(),
                "Child node count should match parent1's node count");
    }

    @Test
    public void testCrossover_childHasConnectionGenes() {
        Genome child = strategy.crossover(parent1, parent2);
        Assert.assertFalse(child.getConnectionGenes().isEmpty(),
                "Child should have at least one connection gene");
    }

    // -------------------------------------------------------------------------
    // Matching gene inheritance tests
    // -------------------------------------------------------------------------

    @Test
    public void testCrossover_matchingGeneInheritedFromEitherParent() {
        // Innovation 1 is matching; run many times and verify both parents' weights appear
        Set<Double> weightsForInn1 = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) {
            StandardCrossoverStrategy s = new StandardCrossoverStrategy(new Random(i));
            Genome child = s.crossover(parent1, parent2);
            child.getConnectionGenes().stream()
                    .filter(cg -> cg.getInnovationNumber() == 1)
                    .map(ConnectionGene::getWeight)
                    .forEach(weightsForInn1::add);
        }
        Assert.assertTrue(weightsForInn1.contains(1.0),
                "Matching gene (inn=1) should sometimes be inherited from parent1 (weight=1.0)");
        Assert.assertTrue(weightsForInn1.contains(9.0),
                "Matching gene (inn=1) should sometimes be inherited from parent2 (weight=9.0)");
    }

    // -------------------------------------------------------------------------
    // Excess / disjoint gene inheritance tests
    // -------------------------------------------------------------------------

    @Test
    public void testCrossover_excessGeneFromParent2NotInherited() {
        // Innovation 4 is excess from parent2's perspective — should NOT appear in child
        Genome child = strategy.crossover(parent1, parent2);
        boolean hasInn4 = child.getConnectionGenes().stream()
                .anyMatch(cg -> cg.getInnovationNumber() == 4);
        Assert.assertFalse(hasInn4,
                "Excess gene from parent2 (inn=4) should not appear in child");
    }

    @Test
    public void testCrossover_disjointGeneFromParent1AlwaysInherited() {
        // Innovation 3 is in parent1 only (disjoint) — should ALWAYS appear in child
        Genome child = strategy.crossover(parent1, parent2);
        boolean hasInn3 = child.getConnectionGenes().stream()
                .anyMatch(cg -> cg.getInnovationNumber() == 3);
        Assert.assertTrue(hasInn3,
                "Disjoint gene from parent1 (inn=3) must always be inherited by child");
    }

    // -------------------------------------------------------------------------
    // Equal parents (both empty) tests
    // -------------------------------------------------------------------------

    @Test
    public void testCrossover_bothEmptyGenomes_returnsEmptyConnectionGenes() {
        Genome emptyA = new GenomeImpl(List.of(new NodeGeneImpl(1, NodeType.INPUT, 0.0)), List.of());
        Genome emptyB = new GenomeImpl(List.of(new NodeGeneImpl(1, NodeType.INPUT, 0.0)), List.of());
        Genome child = strategy.crossover(emptyA, emptyB);
        Assert.assertTrue(child.getConnectionGenes().isEmpty(),
                "Crossing two empty genomes should produce an empty child");
    }

    // -------------------------------------------------------------------------
    // Deterministic behaviour with fixed seed
    // -------------------------------------------------------------------------

    @Test
    public void testCrossover_deterministicWithFixedSeed() {
        StandardCrossoverStrategy s1 = new StandardCrossoverStrategy(new Random(SEED));
        StandardCrossoverStrategy s2 = new StandardCrossoverStrategy(new Random(SEED));
        Genome child1 = s1.crossover(parent1, parent2);
        Genome child2 = s2.crossover(parent1, parent2);
        Assert.assertEquals(child1.getConnectionGenes().size(),
                child2.getConnectionGenes().size(),
                "Same seed should produce children with the same number of connection genes");
        for (int i = 0; i < child1.getConnectionGenes().size(); i++) {
            Assert.assertEquals(
                    child1.getConnectionGenes().get(i).getInnovationNumber(),
                    child2.getConnectionGenes().get(i).getInnovationNumber(),
                    "Same seed should produce identical children");
        }
    }

    // -------------------------------------------------------------------------
    // Symmetry / convention test
    // -------------------------------------------------------------------------

    @Test
    public void testCrossover_parent1IsConventionallyFitter_noExcessFromParent2() {
        // Verify the convention: passing parent1 as fitter always excludes parent2-only genes
        for (int seed = 0; seed < 20; seed++) {
            StandardCrossoverStrategy s = new StandardCrossoverStrategy(new Random(seed));
            Genome child = s.crossover(parent1, parent2);
            Set<Integer> childInnovations = child.getConnectionGenes().stream()
                    .map(ConnectionGene::getInnovationNumber)
                    .collect(Collectors.toSet());
            Assert.assertFalse(childInnovations.contains(4),
                    "Innovation 4 (excess from parent2) must never appear in child (seed=" + seed + ")");
        }
    }

    // -------------------------------------------------------------------------
    // Null argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testCrossover_nullParent1_throwsNullPointerException() {
        strategy.crossover(null, parent2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCrossover_nullParent2_throwsNullPointerException() {
        strategy.crossover(parent1, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullRandom_throwsNullPointerException() {
        new StandardCrossoverStrategy(null);
    }
}
