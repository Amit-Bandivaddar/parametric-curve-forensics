package com.curveforensics.util;

import com.curveforensics.model.CurveModel;
import com.curveforensics.model.Point;
import com.curveforensics.optimization.Parameters;

import java.util.List;

/** Computes validation metrics against the observed points. */
public final class Metrics {
    private Metrics() {}

    public static double rmse(List<Point> observed, double[] t, Parameters p) {
        double sum = 0.0;
        long count = 0;
        for (int i = 0; i < observed.size(); i++) {
            Point pred = CurveModel.point(t[i], p.thetaDeg(), p.m(), p.xOffset());
            Point obs = observed.get(i);
            sum += sq(pred.x() - obs.x());
            sum += sq(pred.y() - obs.y());
            count += 2;
        }
        return Math.sqrt(sum / count);
    }

    public static double maxAbs(List<Point> observed, double[] t, Parameters p) {
        double max = 0.0;
        for (int i = 0; i < observed.size(); i++) {
            Point pred = CurveModel.point(t[i], p.thetaDeg(), p.m(), p.xOffset());
            Point obs = observed.get(i);
            max = Math.max(max, Math.abs(pred.x() - obs.x()));
            max = Math.max(max, Math.abs(pred.y() - obs.y()));
        }
        return max;
    }

    private static double sq(double x) { return x*x; }
}
