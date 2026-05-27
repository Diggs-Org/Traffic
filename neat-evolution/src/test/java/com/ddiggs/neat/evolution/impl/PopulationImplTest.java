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
 * Tests for {@link PopulationImpl}.
 *
 * <p>All tests are currently failing because {@link PopulationImpl} methods throw
 * {@link UnsupportedOperationException}. They will pass once Phase 2 provides
 * real implementations.
 */
public class PopulationImplTest {

    private Genome genome1;
    private Genome genome2;
    private List<Genome> genomes;
    private PopulationImpl population;

    @BeforeMethod
    public void setUp() {
        List<NodeGene> nodes = List.of(
                new NodeGeneImpl(1, NodeType.INPUT, 0.0),
                new NodeGeneImpl(2, NodeType.OUTPUT, 0.0));
        List<ConnectionGene> conns = List.of(
                new ConnectionGeneImpl(1, 1, 1, 2, 0.5, true));
        genome1 = new GenomeImpl(nodes, conns);
        genome2 = new GenomeImpl(List.of(new NodeGeneImpl(3, NodeType.INPUT, 0.1)), List.of());
        genomes = new ArrayList<>(List.of(genome1, genome2));
        population = new PopulationImpl(genomes, 0);
    }

    // -------------------------------------------------------------------------
    // Accessor tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetGenomes_returnsAllGenomes() {
        List<Genome> actual = population.getGenomes();
        Assert.assertEquals(actual.size(), 2,
                "getGenomes() should return all genomes supplied at construction");
    }

    @Test
    public void testGetGenomes_containsSuppliedGenomes() {
        List<Genome> actual = population.getGenomes();
        Assert.assertTrue(actual.contains(genome1),
                "getGenomes() should contain genome1");
        Assert.assertTrue(actual.contains(genome2),
                "getGenomes() should contain genome2");
    }

    @Test
    public void testGetSpecies_initiallyEmpty() {
        Assert.assertEquals(population.getSpecies().size(), 0,
                "getSpecies() should return empty list before speciation");
    }

    @Test
    public void testGetSize_equalsGenomeCount() {
        Assert.assertEquals(population.getSize(), 2,
                "getSize() should equal the number of genomes supplied at construction");
    }

    @Test
    public void testGetGeneration_returnsConstructorValue() {
        Assert.assertEquals(population.getGeneration(), 0,
                "getGeneration() should return the generation supplied at construction");
    }

    @Test
    public void testGetGeneration_positiveGeneration() {
        PopulationImpl p = new PopulationImpl(genomes, 7);
        Assert.assertEquals(p.getGeneration(), 7,
                "getGeneration() should return 7 for generation 7");
    }

    // -------------------------------------------------------------------------
    // Unmodifiable list tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetGenomes_returnsUnmodifiableList() {
        population.getGenomes().add(genome1);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetSpecies_returnsUnmodifiableList() {
        population.getSpecies().add(
                new SpeciesImpl(1, genome1, List.of(genome1), 0.0, 0.0, 0));
    }

    @Test
    public void testGetGenomes_defensiveCopy_originalMutationIgnored() {
        // Mutating the original list after construction should not affect the population
        List<Genome> original = new ArrayList<>(List.of(genome1));
        PopulationImpl p = new PopulationImpl(original, 0);
        original.clear();
        Assert.assertEquals(p.getSize(), 1,
                "Mutating original genome list should not affect population size");
    }

    // -------------------------------------------------------------------------
    // setSpecies / setChampion mutation tests
    // -------------------------------------------------------------------------

    @Test
    public void testSetSpecies_updatesSpeciesList() {
        Species s = new SpeciesImpl(1, genome1, List.of(genome1), 0.5, 1.0, 0);
        population.setSpecies(List.of(s));
        Assert.assertEquals(population.getSpecies().size(), 1,
                "getSpecies() should reflect the list set via setSpecies()");
        Assert.assertSame(population.getSpecies().get(0), s,
                "getSpecies() element should be the species set via setSpecies()");
    }

    @Test
    public void testSetChampion_getChampionReturnsIt() {
        population.setChampion(genome1);
        Assert.assertSame(population.getChampion(), genome1,
                "getChampion() should return the genome set via setChampion()");
    }

    // -------------------------------------------------------------------------
    // Champion before fitness evaluation
    // -------------------------------------------------------------------------

    @Test
    public void testGetChampion_beforeEvaluation_returnsNull() {
        // Undefined before evaluation — implementation should return null
        Assert.assertNull(population.getChampion(),
                "getChampion() before fitness evaluation should return null");
    }

    // -------------------------------------------------------------------------
    // Null / invalid argument validation tests
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_nullGenomes_throwsNullPointerException() {
        new PopulationImpl(null, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_negativeGeneration_throwsIllegalArgumentException() {
        new PopulationImpl(genomes, -1);
    }

    // -------------------------------------------------------------------------
    // Implements Population interface test
    // -------------------------------------------------------------------------

    @Test
    public void testPopulationImpl_implementsPopulationInterface() {
        Assert.assertTrue(population instanceof Population,
                "PopulationImpl must implement the Population interface");
    }
}
