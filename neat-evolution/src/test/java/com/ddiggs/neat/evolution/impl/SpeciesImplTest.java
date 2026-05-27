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

/**
 * Tests for {@link SpeciesImpl}.
 *
 * <p>All tests are currently failing because {@link SpeciesImpl} methods throw
 * {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class SpeciesImplTest {

    private Genome representative;
    private List<Genome> members;
    private SpeciesImpl species;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 0.5, true));
        representative = new GenomeImpl(nodes, conns);
        members = new ArrayList<>(List.of(representative));
        species = new SpeciesImpl(1, representative, members, 2.5, 1.0, 0);
    }

    // -------------------------------------------------------------------------
    // Accessor tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetId_returnsConstructorValue() {
        Assert.assertEquals(species.getId(), 1,
                "getId() should return the id supplied at construction");
    }

    @Test
    public void testGetRepresentative_returnsConstructorValue() {
        Assert.assertSame(species.getRepresentative(), representative,
                "getRepresentative() should return the exact genome supplied at construction");
    }

    @Test
    public void testGetMembers_returnsCorrectGenomes() {
        List<Genome> actual = species.getMembers();
        Assert.assertEquals(actual.size(), 1,
                "getMembers() should contain exactly the genomes supplied at construction");
        Assert.assertSame(actual.get(0), representative,
                "getMembers() element should be the genome supplied at construction");
    }

    @Test
    public void testGetSharedFitnessSum_returnsConstructorValue() {
        Assert.assertEquals(species.getSharedFitnessSum(), 2.5, 1e-15,
                "getSharedFitnessSum() should return the value supplied at construction");
    }

    @Test
    public void testGetBestFitness_returnsConstructorValue() {
        Assert.assertEquals(species.getBestFitness(), 1.0, 1e-15,
                "getBestFitness() should return the value supplied at construction");
    }

    @Test
    public void testGetGenerationsSinceImprovement_returnsConstructorValue() {
        Assert.assertEquals(species.getGenerationsSinceImprovement(), 0,
                "getGenerationsSinceImprovement() should return the value supplied at construction");
    }

    @Test
    public void testGetGenerationsSinceImprovement_positiveValue() {
        SpeciesImpl stagnant = new SpeciesImpl(2, representative, members, 0.0, 0.5, 5);
        Assert.assertEquals(stagnant.getGenerationsSinceImprovement(), 5,
                "getGenerationsSinceImprovement() should reflect 5 stagnant generations");
    }

    // -------------------------------------------------------------------------
    // Unmodifiable list tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetMembers_returnsUnmodifiableList() {
        // Covers the unmodifiable-view guard on the members list
        species.getMembers().add(representative);
    }

    @Test
    public void testGetMembers_defensiveCopy_originalListMutationIgnored() {
        // Mutating the original list after construction should not affect the species
        List<Genome> originalList = new ArrayList<>(List.of(representative));
        SpeciesImpl s = new SpeciesImpl(3, representative, originalList, 0.0, 0.0, 0);
        originalList.clear();
        Assert.assertEquals(s.getMembers().size(), 1,
                "Mutating the original list should not affect species members");
    }

    // -------------------------------------------------------------------------
    // Multiple members tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetMembers_multipleMembers() {
        List<NodeGene> nodes2 = List.of(new NodeGeneImpl(1, NodeType.INPUT, 0.1));
        Genome other = new GenomeImpl(nodes2, List.of());
        List<Genome> multi = new ArrayList<>(List.of(representative, other));
        SpeciesImpl s = new SpeciesImpl(4, representative, multi, 1.0, 0.5, 0);
        Assert.assertEquals(s.getMembers().size(), 2,
                "Species with two members should report size 2");
    }

    // -------------------------------------------------------------------------
    // Fitness value edge cases
    // -------------------------------------------------------------------------

    @Test
    public void testGetSharedFitnessSum_zeroValue() {
        SpeciesImpl s = new SpeciesImpl(5, representative, members, 0.0, 0.0, 0);
        Assert.assertEquals(s.getSharedFitnessSum(), 0.0, 1e-15,
                "sharedFitnessSum of zero should be returned exactly");
    }

    @Test
    public void testGetBestFitness_zeroValue() {
        SpeciesImpl s = new SpeciesImpl(6, representative, members, 0.0, 0.0, 0);
        Assert.assertEquals(s.getBestFitness(), 0.0, 1e-15,
                "bestFitness of zero should be returned exactly");
    }

    // -------------------------------------------------------------------------
    // Null / invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullRepresentative_throwsNullPointerException() {
        new SpeciesImpl(1, null, members, 0.0, 0.0, 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullMembers_throwsNullPointerException() {
        new SpeciesImpl(1, representative, null, 0.0, 0.0, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_emptyMembers_throwsIllegalArgumentException() {
        // Species Javadoc: "never empty"
        new SpeciesImpl(1, representative, new ArrayList<>(), 0.0, 0.0, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_negativeGenerationsSinceImprovement_throwsIllegalArgumentException() {
        new SpeciesImpl(1, representative, members, 0.0, 0.0, -1);
    }

    // -------------------------------------------------------------------------
    // Implements Species interface test
    // -------------------------------------------------------------------------

    @Test
    public void testSpeciesImpl_implementsSpeciesInterface() {
        Assert.assertTrue(species instanceof Species,
                "SpeciesImpl must implement the Species interface");
    }
}
