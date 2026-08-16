package com.curveforensics.optimization;

import com.curveforensics.model.CurveModel;
import com.curveforensics.model.Point;
import java.util.List;

/**
 * Assignment-aligned L1 objective.
 *
 * For uniformly ordered observations, the i-th CSV point is compared
 * with the i-th uniformly sampled t value in (6, 60).
 */
public final class L1Objective {
    private final List<Point> observed;
    private final double[] tValues;

    public L1Objective(List<Point> observed) {
        this.observed = observed;
        this.tValues = uniformT(observed.size());
    }

    public double evaluate(Parameters p) {
        double total = 0.0;
        for (int i = 0; i < observed.size(); i++) {
            Point predicted = CurveModel.point(
                    tValues[i], p.thetaDeg(), p.m(), p.xOffset()
            );
            Point actual = observed.get(i);

            total += Math.abs(predicted.x() - actual.x());
            total += Math.abs(predicted.y() - actual.y());
        }
        return total / observed.size();
    }

    public double[] getTValues() {
        return tValues.clone();
    }

    private static double[] uniformT(int n) {
        if (n < 2) {
            throw new IllegalArgumentException("At least two data points are required.");
        }
        double[] t = new double[n];
        double start = 6.000001;
        double end = 59.999999;
        double step = (end - start) / (n - 1);
        for (int i = 0; i < n; i++) {
            t[i] = start + i * step;
        }
        return t;
    }
}
