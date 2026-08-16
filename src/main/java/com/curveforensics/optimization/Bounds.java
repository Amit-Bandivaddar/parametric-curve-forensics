package com.curveforensics.optimization;

/** Parameter bounds specified by the assignment. */
public record Bounds(
        double thetaMin,
        double thetaMax,
        double mMin,
        double mMax,
        double xMin,
        double xMax
) {
    public static Bounds assignment() {
        return new Bounds(
                1.0e-9, 49.999999999,
                -0.049999999, 0.049999999,
                1.0e-9, 99.999999999
        );
    }

    public double clampTheta(double v) { return clamp(v, thetaMin, thetaMax); }
    public double clampM(double v) { return clamp(v, mMin, mMax); }
    public double clampX(double v) { return clamp(v, xMin, xMax); }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
