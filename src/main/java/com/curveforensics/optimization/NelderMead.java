package com.curveforensics.optimization;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * Dependency-free Nelder-Mead local optimizer for final polishing.
 */
public final class NelderMead {
    private final Bounds bounds;

    public NelderMead(Bounds bounds) {
        this.bounds = bounds;
    }

    public Parameters optimize(
            Parameters start,
            ToDoubleFunction<Parameters> objective,
            int maxIterations
    ) {
        double[][] simplex = new double[4][3];
        simplex[0] = vector(start);

        double[] scales = {
                0.05,   // theta
                0.0005, // M
                0.05    // X
        };

        for (int i = 1; i < 4; i++) {
            simplex[i] = simplex[0].clone();
            simplex[i][i - 1] += scales[i - 1];
            simplex[i] = clamp(simplex[i]);
        }

        double[] values = new double[4];
        for (int i = 0; i < 4; i++) values[i] = objective.applyAsDouble(toParameters(simplex[i]));

        final double alpha = 1.0;
        final double gamma = 2.0;
        final double rho = 0.5;
        final double sigma = 0.5;

        for (int iter = 0; iter < maxIterations; iter++) {
            sort(simplex, values);

            double spread = values[3] - values[0];
            if (spread < 1e-14) break;

            double[] centroid = new double[3];
            for (int i = 0; i < 3; i++) {
                for (int d = 0; d < 3; d++) centroid[d] += simplex[i][d] / 3.0;
            }

            double[] reflected = new double[3];
            for (int d = 0; d < 3; d++) {
                reflected[d] = centroid[d] + alpha * (centroid[d] - simplex[3][d]);
            }
            reflected = clamp(reflected);
            double fr = objective.applyAsDouble(toParameters(reflected));

            if (fr < values[0]) {
                double[] expanded = new double[3];
                for (int d = 0; d < 3; d++) {
                    expanded[d] = centroid[d] + gamma * (reflected[d] - centroid[d]);
                }
                expanded = clamp(expanded);
                double fe = objective.applyAsDouble(toParameters(expanded));
                if (fe < fr) {
                    simplex[3] = expanded;
                    values[3] = fe;
                } else {
                    simplex[3] = reflected;
                    values[3] = fr;
                }
            } else if (fr < values[2]) {
                simplex[3] = reflected;
                values[3] = fr;
            } else {
                double[] contracted = new double[3];
                if (fr < values[3]) {
                    for (int d = 0; d < 3; d++) {
                        contracted[d] = centroid[d] + rho * (reflected[d] - centroid[d]);
                    }
                } else {
                    for (int d = 0; d < 3; d++) {
                        contracted[d] = centroid[d] + rho * (simplex[3][d] - centroid[d]);
                    }
                }
                contracted = clamp(contracted);
                double fc = objective.applyAsDouble(toParameters(contracted));

                if (fc < values[3]) {
                    simplex[3] = contracted;
                    values[3] = fc;
                } else {
                    for (int i = 1; i < 4; i++) {
                        for (int d = 0; d < 3; d++) {
                            simplex[i][d] = simplex[0][d] + sigma * (simplex[i][d] - simplex[0][d]);
                        }
                        simplex[i] = clamp(simplex[i]);
                        values[i] = objective.applyAsDouble(toParameters(simplex[i]));
                    }
                }
            }
        }

        sort(simplex, values);
        return toParameters(simplex[0]);
    }

    private void sort(double[][] simplex, double[] values) {
        Integer[] idx = {0, 1, 2, 3};
        Arrays.sort(idx, (a, b) -> Double.compare(values[a], values[b]));

        double[][] s = simplex.clone();
        double[] v = values.clone();

        for (int i = 0; i < 4; i++) {
            simplex[i] = s[idx[i]].clone();
            values[i] = v[idx[i]];
        }
    }

    private double[] clamp(double[] v) {
        return new double[]{
                bounds.clampTheta(v[0]),
                bounds.clampM(v[1]),
                bounds.clampX(v[2])
        };
    }

    private static double[] vector(Parameters p) {
        return new double[]{p.thetaDeg(), p.m(), p.xOffset()};
    }

    private static Parameters toParameters(double[] v) {
        return new Parameters(v[0], v[1], v[2]);
    }
}
