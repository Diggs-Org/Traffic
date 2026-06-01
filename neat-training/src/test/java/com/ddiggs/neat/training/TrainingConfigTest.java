package com.ddiggs.neat.training;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the {@link TrainingConfig} record.
 */
public class TrainingConfigTest {

    private static final int    POP_SIZE       = 150;
    private static final int    MAX_GEN        = 500;
    private static final double FITNESS_THRESH = 0.95;
    private static final double COMPAT_THRESH  = 3.0;
    private static final double COMPAT_MOD     = 0.3;
    private static final int    TARGET_SPECIES = 5;

    private TrainingConfig config() {
        return new TrainingConfig(POP_SIZE, MAX_GEN, FITNESS_THRESH, COMPAT_THRESH, COMPAT_MOD, TARGET_SPECIES);
    }

    // -------------------------------------------------------------------------
    // Accessor tests
    // -------------------------------------------------------------------------

    @Test
    public void testPopulationSize_returnsConstructorValue() {
        Assert.assertEquals(config().populationSize(), POP_SIZE);
    }

    @Test
    public void testMaxGenerations_returnsConstructorValue() {
        Assert.assertEquals(config().maxGenerations(), MAX_GEN);
    }

    @Test
    public void testFitnessThreshold_returnsConstructorValue() {
        Assert.assertEquals(config().fitnessThreshold(), FITNESS_THRESH, 1e-15);
    }

    @Test
    public void testCompatibilityThreshold_returnsConstructorValue() {
        Assert.assertEquals(config().compatibilityThreshold(), COMPAT_THRESH, 1e-15);
    }

    @Test
    public void testCompatibilityModifier_returnsConstructorValue() {
        Assert.assertEquals(config().compatibilityModifier(), COMPAT_MOD, 1e-15);
    }

    @Test
    public void testTargetSpeciesCount_returnsConstructorValue() {
        Assert.assertEquals(config().targetSpeciesCount(), TARGET_SPECIES);
    }

    // -------------------------------------------------------------------------
    // Equality and hash-code tests (record contract)
    // -------------------------------------------------------------------------

    @Test
    public void testEquals_sameValues_areEqual() {
        Assert.assertEquals(config(), config());
    }

    @Test
    public void testEquals_differentPopulationSize_areNotEqual() {
        Assert.assertNotEquals(
                new TrainingConfig(1, MAX_GEN, FITNESS_THRESH, COMPAT_THRESH, COMPAT_MOD, TARGET_SPECIES),
                config());
    }

    @Test
    public void testEquals_differentMaxGenerations_areNotEqual() {
        Assert.assertNotEquals(
                new TrainingConfig(POP_SIZE, 1, FITNESS_THRESH, COMPAT_THRESH, COMPAT_MOD, TARGET_SPECIES),
                config());
    }

    @Test
    public void testHashCode_sameValues_sameHashCode() {
        Assert.assertEquals(config().hashCode(), config().hashCode());
    }

    // -------------------------------------------------------------------------
    // toString test
    // -------------------------------------------------------------------------

    @Test
    public void testToString_containsFieldNames() {
        String s = config().toString();
        Assert.assertTrue(s.contains("populationSize"),   "toString must contain 'populationSize'");
        Assert.assertTrue(s.contains("maxGenerations"),   "toString must contain 'maxGenerations'");
        Assert.assertTrue(s.contains("fitnessThreshold"), "toString must contain 'fitnessThreshold'");
    }
}
