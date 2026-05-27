package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.evolution.Species;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tests for {@link TournamentSelectionStrategy}.
 *
 * <p>All tests are currently failing because {@link TournamentSelectionStrategy} methods
 * throw {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class TournamentSelectionStrategyTest {

    private static final long SEED = 42L;

    private Genome genome1;
    private Genome genome2;
    private Genome genome3;
    private Species singleMemberSpecies;
    private Species threeMemberSpecies;
    private TournamentSelectionStrategy strategy;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        genome1 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true)));
        genome2 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 2.0, true)));
        genome3 = new GenomeImpl(nodes, List.of(new ConnectionGeneImpl(1, 1, 1, 2, 3.0, true)));

        singleMemberSpecies = new SpeciesImpl(1, genome1, List.of(genome1), 1.0, 1.0, 0);
        threeMemberSpecies = new SpeciesImpl(2, genome1,
                new ArrayList<>(List.of(genome1, genome2, genome3)), 3.0, 3.0, 0);

        strategy = new TournamentSelectionStrategy(2, new Random(SEED));
    }

    // -------------------------------------------------------------------------
    // Result size tests
    // -------------------------------------------------------------------------

    @Test
    public void testSelect_returnsRequestedCount() {
        List<Genome> selected = strategy.select(threeMemberSpecies, 3);
        Assert.assertEquals(selected.size(), 3,
                "select() should return exactly the requested count");
    }

    @Test
    public void testSelect_returnsOneWhenCountIsOne() {
        List<Genome> selected = strategy.select(threeMemberSpecies, 1);
        Assert.assertEquals(selected.size(), 1,
                "select() with count=1 should return exactly 1 genome");
    }

    @Test
    public void testSelect_countExceedsMembersAllowsDuplicates() {
        // Per the SelectionStrategy contract, duplicates are allowed when count > |members|
        List<Genome> selected = strategy.select(singleMemberSpecies, 5);
        Assert.assertEquals(selected.size(), 5,
                "select() should return count genomes even if it exceeds member count");
    }

    // -------------------------------------------------------------------------
    // Result content tests
    // -------------------------------------------------------------------------

    @Test
    public void testSelect_selectedGenomesAreMembersOfSpecies() {
        List<Genome> selected = strategy.select(threeMemberSpecies, 10);
        List<Genome> members = threeMemberSpecies.getMembers();
        for (Genome g : selected) {
            Assert.assertTrue(members.contains(g),
                    "Every selected genome must be a member of the species");
        }
    }

    @Test
    public void testSelect_singleMemberSpecies_alwaysReturnsThatGenome() {
        List<Genome> selected = strategy.select(singleMemberSpecies, 3);
        for (Genome g : selected) {
            Assert.assertSame(g, genome1,
                    "With only one member, that genome must always be selected");
        }
    }

    // -------------------------------------------------------------------------
    // Unmodifiable result test
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testSelect_returnsUnmodifiableList() {
        strategy.select(threeMemberSpecies, 1).add(genome1);
    }

    // -------------------------------------------------------------------------
    // Deterministic behaviour with fixed seed
    // -------------------------------------------------------------------------

    @Test
    public void testSelect_deterministicWithFixedSeed() {
        TournamentSelectionStrategy s1 = new TournamentSelectionStrategy(2, new Random(SEED));
        TournamentSelectionStrategy s2 = new TournamentSelectionStrategy(2, new Random(SEED));
        List<Genome> result1 = s1.select(threeMemberSpecies, 3);
        List<Genome> result2 = s2.select(threeMemberSpecies, 3);
        Assert.assertEquals(result1, result2,
                "Same seed should produce identical selection results");
    }

    // -------------------------------------------------------------------------
    // Tournament size 1 test
    // -------------------------------------------------------------------------

    @Test
    public void testSelect_tournamentSizeOne_returnsRandomMember() {
        TournamentSelectionStrategy sizeOneStrategy =
                new TournamentSelectionStrategy(1, new Random(SEED));
        List<Genome> selected = sizeOneStrategy.select(threeMemberSpecies, 1);
        Assert.assertEquals(selected.size(), 1,
                "Tournament of size 1 should still return the requested count");
        Assert.assertTrue(threeMemberSpecies.getMembers().contains(selected.get(0)),
                "Tournament of size 1 should return a valid member");
    }

    // -------------------------------------------------------------------------
    // Invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testSelect_nullSpecies_throwsNullPointerException() {
        strategy.select(null, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSelect_zeroCount_throwsIllegalArgumentException() {
        strategy.select(threeMemberSpecies, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSelect_negativeCount_throwsIllegalArgumentException() {
        strategy.select(threeMemberSpecies, -1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullRandom_throwsNullPointerException() {
        new TournamentSelectionStrategy(2, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_zeroTournamentSize_throwsIllegalArgumentException() {
        new TournamentSelectionStrategy(0, new Random(SEED));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_negativeTournamentSize_throwsIllegalArgumentException() {
        new TournamentSelectionStrategy(-1, new Random(SEED));
    }
}
