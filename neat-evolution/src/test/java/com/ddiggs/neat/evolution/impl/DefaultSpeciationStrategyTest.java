package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.ConnectionGene;
import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NodeGene;
import com.ddiggs.neat.core.NodeType;
import com.ddiggs.neat.core.impl.ConnectionGeneImpl;
import com.ddiggs.neat.core.impl.GenomeImpl;
import com.ddiggs.neat.core.impl.NodeGeneImpl;
import com.ddiggs.neat.evolution.Population;
import com.ddiggs.neat.evolution.Species;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link DefaultSpeciationStrategy}.
 *
 * <p>All tests are currently failing because {@link DefaultSpeciationStrategy} methods throw
 * {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class DefaultSpeciationStrategyTest {

    // Coefficients matching the standard NEAT paper values
    private static final double C1 = 1.0;
    private static final double C2 = 1.0;
    private static final double C3 = 0.4;
    private static final double THRESHOLD = 3.0;

    private DefaultSpeciationStrategy strategy;
    private Genome genomeA;
    private Genome genomeB;

    @BeforeMethod
    public void setUp() {
        strategy = new DefaultSpeciationStrategy(C1, C2, C3, THRESHOLD);

        // genomeA: input→output with innovation 1, weight 1.0
        List<NodeGene> nodesA = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        List<ConnectionGene> connsA = List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 1.0, true));
        genomeA = new GenomeImpl(nodesA, connsA);

        // genomeB: same topology but very different weight (close to A — same species expected)
        List<NodeGene> nodesB = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        List<ConnectionGene> connsB = List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 1.2, true));
        genomeB = new GenomeImpl(nodesB, connsB);
    }

    // -------------------------------------------------------------------------
    // Basic speciation tests
    // -------------------------------------------------------------------------

    @Test
    public void testSpeciate_singleGenome_createsOneSpecies() {
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        strategy.speciate(pop);
        Assert.assertEquals(pop.getSpecies().size(), 1,
                "A single genome should produce exactly one species");
    }

    @Test
    public void testSpeciate_singleGenome_allGenomesAssigned() {
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        strategy.speciate(pop);
        int totalMembers = pop.getSpecies().stream()
                .mapToInt(s -> s.getMembers().size())
                .sum();
        Assert.assertEquals(totalMembers, 1,
                "Total members across all species should equal total genome count");
    }

    @Test
    public void testSpeciate_twoCompatibleGenomes_sameSpecies() {
        // genomeA and genomeB have the same topology and close weight → should be in same species
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA, genomeB)), 0);
        strategy.speciate(pop);
        Assert.assertEquals(pop.getSpecies().size(), 1,
                "Two genomes with distance < threshold should share a species");
    }

    @Test
    public void testSpeciate_twoIncompatibleGenomes_differentSpecies() {
        // Build a genome with many disjoint connections → large compatibility distance from genomeA
        List<NodeGene> nodesC = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        // Innovation numbers 10-19 are all disjoint from genomeA's innovation 1
        List<ConnectionGene> connsC = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            connsC.add(new ConnectionGeneImpl(i, i, 1, 2, 1.0, true));
        }
        Genome genomeC = new GenomeImpl(nodesC, connsC);

        // Use a very small threshold so any difference causes a split
        DefaultSpeciationStrategy strictStrategy = new DefaultSpeciationStrategy(C1, C2, C3, 0.01);
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA, genomeC)), 0);
        strictStrategy.speciate(pop);
        Assert.assertEquals(pop.getSpecies().size(), 2,
                "Two incompatible genomes should each get their own species");
    }

    @Test
    public void testSpeciate_allGenomesAssigned() {
        PopulationImpl pop = new PopulationImpl(
                new ArrayList<>(List.of(genomeA, genomeB)), 0);
        strategy.speciate(pop);
        int totalMembers = pop.getSpecies().stream()
                .mapToInt(s -> s.getMembers().size())
                .sum();
        Assert.assertEquals(totalMembers, 2,
                "Total members across species must equal the population size");
    }

    // -------------------------------------------------------------------------
    // Representative tests
    // -------------------------------------------------------------------------

    @Test
    public void testSpeciate_speciesHasRepresentative() {
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        strategy.speciate(pop);
        Species s = pop.getSpecies().get(0);
        Assert.assertNotNull(s.getRepresentative(),
                "Every species must have a non-null representative");
    }

    @Test
    public void testSpeciate_representativeIsMember() {
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA, genomeB)), 0);
        strategy.speciate(pop);
        for (Species s : pop.getSpecies()) {
            Assert.assertTrue(s.getMembers().contains(s.getRepresentative()),
                    "The representative genome must be in the species' member list");
        }
    }

    // -------------------------------------------------------------------------
    // Empty species removal test
    // -------------------------------------------------------------------------

    @Test
    public void testSpeciate_emptySpeciesRemoved() {
        // Create a population with existing species whose representative has no matching genomes
        // Use a very strict threshold to force a split and leave previous species empty
        List<NodeGene> nodesD = List.of(new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        Genome genomeD = new GenomeImpl(nodesD, List.of(
                new ConnectionGeneImpl(100, 100, 1, 2, 99.0, true)));

        // Seed the population with a species whose representative is genomeD
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        // Manually add a species for genomeD so it starts in species list
        pop.setSpecies(List.of(
                new SpeciesImpl(1, genomeD, List.of(genomeD), 0.0, 0.0, 0)));

        DefaultSpeciationStrategy strictStrategy = new DefaultSpeciationStrategy(C1, C2, C3, 0.01);
        strictStrategy.speciate(pop);

        // genomeD's species has no member genomes matching genomeA → should be removed
        for (Species s : pop.getSpecies()) {
            Assert.assertFalse(s.getMembers().isEmpty(),
                    "Species with no members should be removed during speciation");
        }
    }

    // -------------------------------------------------------------------------
    // Species ID stability tests
    // -------------------------------------------------------------------------

    @Test
    public void testSpeciate_speciesIdIsPositive() {
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        strategy.speciate(pop);
        for (Species s : pop.getSpecies()) {
            Assert.assertTrue(s.getId() > 0,
                    "All species IDs should be positive");
        }
    }

    @Test
    public void testSpeciate_existingSpeciesRetainsId() {
        // Run speciation twice; the species that was created in round 1 should keep its ID in round 2
        PopulationImpl pop1 = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        strategy.speciate(pop1);
        int firstId = pop1.getSpecies().get(0).getId();

        // Run again with same genome → same species should be matched
        PopulationImpl pop2 = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 1);
        pop2.setSpecies(pop1.getSpecies()); // seed with previous species
        strategy.speciate(pop2);
        Assert.assertEquals(pop2.getSpecies().get(0).getId(), firstId,
                "The same species should retain its ID across generations");
    }

    // -------------------------------------------------------------------------
    // Fitness placeholder tests
    // -------------------------------------------------------------------------

    @Test
    public void testSpeciate_newSpeciesHasZeroSharedFitnessSum() {
        PopulationImpl pop = new PopulationImpl(new ArrayList<>(List.of(genomeA)), 0);
        strategy.speciate(pop);
        for (Species s : pop.getSpecies()) {
            Assert.assertEquals(s.getSharedFitnessSum(), 0.0, 1e-15,
                    "Newly created species should have sharedFitnessSum = 0 (engine sets it later)");
        }
    }

    // -------------------------------------------------------------------------
    // Invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_zeroThreshold_throwsIllegalArgumentException() {
        new DefaultSpeciationStrategy(C1, C2, C3, 0.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_negativeThreshold_throwsIllegalArgumentException() {
        new DefaultSpeciationStrategy(C1, C2, C3, -1.0);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testSpeciate_nonPopulationImpl_throwsClassCastException() {
        // Covers the cast guard: only PopulationImpl is supported
        strategy.speciate(new Population() {
            public List<Genome> getGenomes() { return List.of(genomeA); }
            public List<com.ddiggs.neat.evolution.Species> getSpecies() { return List.of(); }
            public Genome getChampion() { return null; }
            public int getSize() { return 1; }
            public int getGeneration() { return 0; }
        });
    }
}
