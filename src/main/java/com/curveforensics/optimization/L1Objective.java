package com.curveforensics.optimization;

import com.curveforensics.model.CurveModel;
import com.curveforensics.model.Point;

import java.util.List;

/**
 * Permutation-invariant L1 objective for the observed point cloud.
 *
 * The supplied CSV is a point cloud rather than an ordered sequence of samples.
 * For a candidate parameter set, each observed point is transformed into the
 * curve's local coordinates. The longitudinal coordinate gives its implied t,
 * and the curve is evaluated at that t. The mean coordinate-wise L1 error is
 * then minimized.
 */
public final class L1Objective {
    private final List<Point> observed;

    public L1Objective(List<Point> observed) {
        if (observed.size() < 2) {
            throw new IllegalArgumentException("At least two data points are required.");
        }
        this.observed = observed;
    }

    public double evaluate(Parameters p) {
        double[] t = getTValues(p);
        double total = 0.0;
        double penalty = 0.0;

        for (int i = 0; i < observed.size(); i++) {
            double ti = t[i];
            if (ti < 6.0) {
                penalty += 10.0 * (6.0 - ti);
            } else if (ti > 60.0) {
                penalty += 10.0 * (ti - 60.0);
            }

            Point predicted = CurveModel.point(
                    ti, p.thetaDeg(), p.m(), p.xOffset()
            );
            Point actual = observed.get(i);

            total += Math.abs(predicted.x() - actual.x());
            total += Math.abs(predicted.y() - actual.y());
        }

        return total / observed.size() + penalty / observed.size();
    }

    /**
     * Returns the implied t value for each observed point under the candidate
     * rotation and horizontal translation. Since t is positive in the assignment,
     * |t| has no practical effect here but CurveModel keeps the assignment formula.
     */
    public double[] getTValues(Parameters p) {
        double[] t = new double[observed.size()];
        double theta = Math.toRadians(p.thetaDeg());
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);

        for (int i = 0; i < observed.size(); i++) {
            Point point = observed.get(i);
            double u = point.x() - p.xOffset();
            double v = point.y() - 42.0;
            // Inverse rotation: t is the component along the curve's linear axis.
            t[i] = u * cos + v * sin;
        }
        return t;
    }
}
