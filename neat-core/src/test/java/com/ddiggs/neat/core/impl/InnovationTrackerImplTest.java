package com.ddiggs.neat.core.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TDD tests for {@link InnovationTrackerImpl}.
 *
 * <p>All tests in this class are expected to <strong>fail</strong> until Phase 2
 * provides the concrete implementation.
 */
public class InnovationTrackerImplTest {

    private InnovationTrackerImpl tracker;

    @BeforeMethod
    public void setUp() {
        tracker = new InnovationTrackerImpl();
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    public void testGetCurrentInnovationNumber_initiallyZero() {
        Assert.assertEquals(tracker.getCurrentInnovationNumber(), 0,
                "A fresh tracker should start with getCurrentInnovationNumber() == 0");
    }

    // -------------------------------------------------------------------------
    // getInnovationNumber — basic behaviour
    // -------------------------------------------------------------------------

    @Test
    public void testGetInnovationNumber_newPair_returnsPositiveNumber() {
        int innovationNumber = tracker.getInnovationNumber(1, 2);
        Assert.assertTrue(innovationNumber > 0,
                "A newly assigned innovation number must be positive, got: " + innovationNumber);
    }

    @Test
    public void testGetInnovationNumber_firstCall_incrementsCurrentInnovationNumber() {
        tracker.getInnovationNumber(1, 2);
        Assert.assertTrue(tracker.getCurrentInnovationNumber() > 0,
                "getCurrentInnovationNumber() must be > 0 after the first assignment");
    }

    @Test
    public void testGetInnovationNumber_samePairSameGeneration_returnsSameNumber() {
        int first  = tracker.getInnovationNumber(1, 2);
        int second = tracker.getInnovationNumber(1, 2);
        Assert.assertEquals(second, first,
                "The same (fromNode, toNode) pair within one generation must reuse the same innovation number");
    }

    @Test
    public void testGetInnovationNumber_differentPairs_returnsDifferentNumbers() {
        int num12 = tracker.getInnovationNumber(1, 2);
        int num23 = tracker.getInnovationNumber(2, 3);
        Assert.assertNotEquals(num12, num23,
                "Different node pairs must receive different innovation numbers");
    }

    @Test
    public void testGetInnovationNumber_multiplePairs_areMonotonicallyIncreasing() {
        int a = tracker.getInnovationNumber(1, 2);
        int b = tracker.getInnovationNumber(2, 3);
        int c = tracker.getInnovationNumber(3, 4);
        Assert.assertTrue(a < b && b < c,
                "Innovation numbers must be monotonically increasing across distinct pairs");
    }

    @Test
    public void testGetCurrentInnovationNumber_reflectsHighestAssigned() {
        tracker.getInnovationNumber(1, 2);
        int afterFirst = tracker.getCurrentInnovationNumber();
        tracker.getInnovationNumber(2, 3);
        int afterSecond = tracker.getCurrentInnovationNumber();
        Assert.assertTrue(afterSecond > afterFirst,
                "getCurrentInnovationNumber() must grow as new innovation numbers are assigned");
    }

    // -------------------------------------------------------------------------
    // Directional uniqueness (1→2 is distinct from 2→1)
    // -------------------------------------------------------------------------

    @Test
    public void testGetInnovationNumber_reverseDirection_returnsDifferentNumber() {
        int forward  = tracker.getInnovationNumber(1, 2);
        int backward = tracker.getInnovationNumber(2, 1);
        Assert.assertNotEquals(forward, backward,
                "Connection 1→2 and connection 2→1 are structurally distinct and must get different innovation numbers");
    }

    // -------------------------------------------------------------------------
    // reset() behaviour
    // -------------------------------------------------------------------------

    @Test
    public void testReset_doesNotResetGlobalCounter() {
        tracker.getInnovationNumber(1, 2);
        int counterBeforeReset = tracker.getCurrentInnovationNumber();
        tracker.reset();
        Assert.assertEquals(tracker.getCurrentInnovationNumber(), counterBeforeReset,
                "reset() must NOT reset the global innovation counter");
    }

    @Test
    public void testReset_clearsCacheSoPreviousPairGetsNewNumber() {
        int beforeReset = tracker.getInnovationNumber(1, 2);
        tracker.reset();
        int afterReset = tracker.getInnovationNumber(1, 2);
        Assert.assertNotEquals(afterReset, beforeReset,
                "After reset(), the same pair must receive a brand-new (incremented) innovation number");
    }

    @Test
    public void testReset_newNumberAfterResetIsHigherThanOld() {
        int beforeReset = tracker.getInnovationNumber(1, 2);
        tracker.reset();
        int afterReset = tracker.getInnovationNumber(1, 2);
        Assert.assertTrue(afterReset > beforeReset,
                "The new innovation number issued after reset() must be strictly greater than the old one");
    }

    @Test
    public void testReset_multipleResets_counterContinuesClimbing() {
        tracker.getInnovationNumber(1, 2);
        tracker.reset();
        tracker.getInnovationNumber(1, 2);
        tracker.reset();
        int num = tracker.getInnovationNumber(1, 2);
        Assert.assertTrue(num >= 3,
                "After 2 resets (each forcing a new number for the same pair), the 3rd number must be ≥ 3");
    }
}
