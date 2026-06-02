package com.ddiggs.neat.trafficsim.impl;

import com.ddiggs.neat.trafficsim.Car;
import com.ddiggs.neat.trafficsim.Road;
import com.ddiggs.neat.trafficsim.SensorInput;
import com.ddiggs.neat.trafficsim.SimulationConfig;
import com.ddiggs.neat.trafficsim.VisionSystem;

import java.util.List;

public final class VisionSystemImpl implements VisionSystem {

    @Override
    public SensorInput observe(Car ego, Road road, List<Car> allCars, SimulationConfig config) {
        double egoX  = ego.getState().getX();
        double egoVx = ego.getState().getVx();
        int egoLane  = ego.getState().getLane();
        double vMax  = config.getVMaxAbsolute();
        double vRange       = ego.getPhysics().getVisionRange();
        double vRangeAdj    = ego.getPhysics().getAdjacentVisionRange();
        double egoL         = ego.getPhysics().getLength();

        double[] v = new double[SensorInputImpl.SIZE];

        // [0] current speed
        v[0] = egoVx / vMax;

        // Current lane (inputs 1-4)
        double[] curr = scanLane(ego, egoLane, allCars, egoX, egoVx, vRange, vMax, false, road);
        v[1] = curr[0]; v[2] = curr[1]; v[3] = curr[2]; v[4] = curr[3];

        // Left lane (inputs 5-8)
        int leftLane = egoLane - 1;
        if (road.isValidLane(leftLane)) {
            v[13] = 1.0;
            boolean blindLeft = isBlindSpot(ego, leftLane, allCars, egoL);
            if (blindLeft) {
                v[15] = 1.0;
                v[5] = 0.0; v[6] = 0.5; v[7] = 0.0; v[8] = 0.5;
            } else {
                double[] left = scanLane(ego, leftLane, allCars, egoX, egoVx, vRangeAdj, vMax, false, road);
                v[5] = left[0]; v[6] = left[1]; v[7] = left[2]; v[8] = left[3];
            }
        } else {
            v[13] = 0.0;
            v[5] = 1.0; v[6] = 0.5; v[7] = 1.0; v[8] = 0.5;
        }

        // Right lane (inputs 9-12)
        int rightLane = egoLane + 1;
        if (road.isValidLane(rightLane)) {
            v[14] = 1.0;
            boolean blindRight = isBlindSpot(ego, rightLane, allCars, egoL);
            if (blindRight) {
                v[16] = 1.0;
                v[9] = 0.0; v[10] = 0.5; v[11] = 0.0; v[12] = 0.5;
            } else {
                double[] right = scanLane(ego, rightLane, allCars, egoX, egoVx, vRangeAdj, vMax, false, road);
                v[9] = right[0]; v[10] = right[1]; v[11] = right[2]; v[12] = right[3];
            }
        } else {
            v[14] = 0.0;
            v[9] = 1.0; v[10] = 0.5; v[11] = 1.0; v[12] = 0.5;
        }

        // [17] lane index
        int laneCount = road.getLaneCount();
        v[17] = laneCount > 1 ? (double) egoLane / (laneCount - 1) : 0.0;

        // [18] merge lockout
        if (ego instanceof CarImpl carImpl) {
            v[18] = carImpl.isMergeLocked(config) ? 1.0 : 0.0;
        } else {
            v[18] = 0.0;
        }

        return new SensorInputImpl(v);
    }

    /**
     * Returns [gapAhead_norm, relSpeedAhead_norm, gapBehind_norm, relSpeedBehind_norm]
     * for the given lane, normalised by range.
     */
    private double[] scanLane(Car ego, int lane, List<Car> allCars,
                               double egoX, double egoVx, double range, double vMax,
                               boolean ignored, Road road) {
        double nearestAheadGap  = range;
        double nearestAheadVx   = Double.NaN;
        double nearestBehindGap = range;
        double nearestBehindVx  = Double.NaN;

        for (Car other : allCars) {
            if (other == ego) continue;
            if (!isVisibleInLane(other, lane)) continue;

            double dx = other.getState().getX() - egoX;
            double absGap = Math.abs(dx);

            if (dx > 0 && absGap <= range) {
                if (absGap < nearestAheadGap) {
                    nearestAheadGap = absGap;
                    nearestAheadVx  = other.getState().getVx();
                }
            } else if (dx < 0 && absGap <= range) {
                if (absGap < nearestBehindGap) {
                    nearestBehindGap = absGap;
                    nearestBehindVx  = other.getState().getVx();
                }
            }
        }

        double gapAhead  = nearestAheadGap  / range;
        double gapBehind = nearestBehindGap / range;
        double rsAhead   = Double.isNaN(nearestAheadVx)  ? 0.5 : relSpeed(nearestAheadVx,  egoVx, vMax);
        double rsBehind  = Double.isNaN(nearestBehindVx) ? 0.5 : relSpeed(nearestBehindVx, egoVx, vMax);

        return new double[]{gapAhead, rsAhead, gapBehind, rsBehind};
    }

    private boolean isVisibleInLane(Car car, int lane) {
        int carLane = car.getState().getLane();
        if (carLane == lane) return true;
        return car.getState().isMerging() && car.getState().getMergeTarget() == lane;
    }

    private boolean isBlindSpot(Car ego, int adjacentLane, List<Car> allCars, double egoL) {
        double egoX = ego.getState().getX();
        for (Car other : allCars) {
            if (other == ego) continue;
            if (!isVisibleInLane(other, adjacentLane)) continue;
            double dx = other.getState().getX() - egoX;
            double otherL = other.getPhysics().getLength();
            // Δx ∈ [-(egoL/2 + 2), +(otherL/2)]
            if (dx >= -(egoL / 2.0 + 2.0) && dx <= (otherL / 2.0)) {
                return true;
            }
        }
        return false;
    }

    private double relSpeed(double otherVx, double egoVx, double vMax) {
        return (otherVx - egoVx + vMax) / (2.0 * vMax);
    }
}
