package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.core.NeuralNetwork;
import com.ddiggs.neat.trafficsim.Car;
import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SensorInput;
import com.ddiggs.neat.trafficsim.SimulationConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class VisionSystemImplTest {

    private VisionSystemImpl vision;
    private SimulationConfig config;
    private Road road;
    private NeuralNetwork fakeNet;

    @BeforeMethod
    public void setUp() {
        vision = new VisionSystemImpl();
        config = SimulationConfigImpl.builder()
                .highwayLength(1000.0)
                .laneCount(3)
                .laneWidth(3.5)
                .vMaxAbsolute(50.0)
                .mergeLockoutDistance(50.0)
                .build();
        road = new RoadImpl(config);
        fakeNet = new NeuralNetwork() {
            @Override public double[] activate(double[] i) { return new double[]{0.5, 0.5, 0.0, 0.0}; }
            @Override public Genome getGenome() { return null; }
        };
    }

    private CarImpl makeCar(long id, double x, int lane, double vx) {
        CarPhysicsImpl physics = new CarPhysicsImpl(4.0, 2.0, 3.0, 7.0, 25.0, 100.0, 60.0, 8.0, 2.0);
        CarStateImpl state = new CarStateImpl(x, road.getLaneCenterY(lane), vx, lane);
        return new CarImpl(id, physics, state, fakeNet, null);
    }

    // -------------------------------------------------------------------------
    // No other cars
    // -------------------------------------------------------------------------

    @Test
    public void testNoOtherCarsAllGapsOne() {
        CarImpl ego = makeCar(0, 500.0, 1, 20.0);
        SensorInput si = vision.observe(ego, road, List.of(ego), config);

        Assert.assertEquals(si.getGapAheadCurrent(),  1.0, 1e-9);
        Assert.assertEquals(si.getGapBehindCurrent(), 1.0, 1e-9);
        Assert.assertEquals(si.getGapAheadLeft(),     1.0, 1e-9);
        Assert.assertEquals(si.getGapBehindLeft(),    1.0, 1e-9);
        Assert.assertEquals(si.getGapAheadRight(),    1.0, 1e-9);
        Assert.assertEquals(si.getGapBehindRight(),   1.0, 1e-9);
    }

    @Test
    public void testCurrentSpeedNormalised() {
        CarImpl ego = makeCar(0, 500.0, 1, 25.0);
        SensorInput si = vision.observe(ego, road, List.of(ego), config);
        Assert.assertEquals(si.getCurrentSpeed(), 25.0 / 50.0, 1e-9);
    }

    // -------------------------------------------------------------------------
    // Car ahead in current lane
    // -------------------------------------------------------------------------

    @Test
    public void testCarAheadCurrentLane() {
        CarImpl ego   = makeCar(0, 500.0, 1, 20.0);
        CarImpl ahead = makeCar(1, 540.0, 1, 30.0);
        SensorInput si = vision.observe(ego, road, List.of(ego, ahead), config);

        // gap = 40m / 100m range = 0.4
        Assert.assertEquals(si.getGapAheadCurrent(), 0.4, 1e-9);
        // rel speed = (30 - 20 + 50) / 100 = 0.6
        Assert.assertEquals(si.getRelSpeedAheadCurrent(), 0.6, 1e-9);
        Assert.assertEquals(si.getGapBehindCurrent(), 1.0, 1e-9);
    }

    // -------------------------------------------------------------------------
    // Car behind in current lane
    // -------------------------------------------------------------------------

    @Test
    public void testCarBehindCurrentLane() {
        CarImpl ego    = makeCar(0, 500.0, 1, 20.0);
        CarImpl behind = makeCar(1, 460.0, 1, 15.0);
        SensorInput si = vision.observe(ego, road, List.of(ego, behind), config);

        Assert.assertEquals(si.getGapBehindCurrent(), 0.4, 1e-9);
        Assert.assertEquals(si.getGapAheadCurrent(),  1.0, 1e-9);
    }

    // -------------------------------------------------------------------------
    // Lane existence flags
    // -------------------------------------------------------------------------

    @Test
    public void testLaneExistenceFlags() {
        CarImpl ego = makeCar(0, 500.0, 1, 20.0);
        SensorInput si = vision.observe(ego, road, List.of(ego), config);
        Assert.assertEquals(si.getLeftLaneExists(),  1.0, 1e-9);
        Assert.assertEquals(si.getRightLaneExists(), 1.0, 1e-9);
    }

    @Test
    public void testNoLeftLaneInLane0() {
        CarImpl ego = makeCar(0, 500.0, 0, 20.0);
        SensorInput si = vision.observe(ego, road, List.of(ego), config);
        Assert.assertEquals(si.getLeftLaneExists(), 0.0, 1e-9);
        Assert.assertEquals(si.getGapAheadLeft(),   1.0, 1e-9);
    }

    @Test
    public void testNoRightLaneInLastLane() {
        CarImpl ego = makeCar(0, 500.0, 2, 20.0);
        SensorInput si = vision.observe(ego, road, List.of(ego), config);
        Assert.assertEquals(si.getRightLaneExists(), 0.0, 1e-9);
        Assert.assertEquals(si.getGapAheadRight(),   1.0, 1e-9);
    }

    // -------------------------------------------------------------------------
    // Blind spot detection
    // -------------------------------------------------------------------------

    @Test
    public void testBlindSpotLeftDetected() {
        // ego at x=500, lane 1; car at x=500, lane 0 → directly alongside
        CarImpl ego  = makeCar(0, 500.0, 1, 20.0);
        CarImpl bsCar= makeCar(1, 500.0, 0, 20.0);
        SensorInput si = vision.observe(ego, road, List.of(ego, bsCar), config);
        Assert.assertEquals(si.getBlindSpotLeft(), 1.0, 1e-9);
        // gap replaced with 0 on blind spot
        Assert.assertEquals(si.getGapAheadLeft(),  0.0, 1e-9);
    }

    // -------------------------------------------------------------------------
    // Merging car visible from both lanes
    // -------------------------------------------------------------------------

    @Test
    public void testMergingCarVisibleFromBothLanes() {
        // Car at x=540, lane 1, merging to lane 0
        CarImpl ego     = makeCar(0, 500.0, 1, 20.0);
        CarImpl merging = makeCar(1, 540.0, 1, 20.0);
        merging.getMutableState().setMerging(true);
        merging.getMutableState().setMergeTarget(0);
        merging.getMutableState().setMergeProgress(0.5);

        // Observed from lane 1 (current) — should see car ahead
        SensorInput siFromLane1 = vision.observe(ego, road, List.of(ego, merging), config);
        Assert.assertTrue(siFromLane1.getGapAheadCurrent() < 1.0,
                "merging car should be visible in current lane");

        // Observed from lane 0 — set ego in lane 0
        CarImpl ego0 = makeCar(2, 500.0, 0, 20.0);
        SensorInput siFromLane0 = vision.observe(ego0, road, List.of(ego0, merging), config);
        Assert.assertTrue(siFromLane0.getGapAheadCurrent() < 1.0,
                "merging car should be visible from target lane");
    }

    // -------------------------------------------------------------------------
    // Lane index normalisation
    // -------------------------------------------------------------------------

    @Test
    public void testLaneIndexNormalised() {
        CarImpl egoLane0 = makeCar(0, 500.0, 0, 10.0);
        CarImpl egoLane2 = makeCar(1, 500.0, 2, 10.0);
        Assert.assertEquals(vision.observe(egoLane0, road, List.of(egoLane0), config).getLaneIndex(), 0.0, 1e-9);
        Assert.assertEquals(vision.observe(egoLane2, road, List.of(egoLane2), config).getLaneIndex(), 1.0, 1e-9);
    }
}
