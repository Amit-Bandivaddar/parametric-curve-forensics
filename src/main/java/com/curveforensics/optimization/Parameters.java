package com.curveforensics.optimization;

/** Candidate parameter vector. */
public record Parameters(double thetaDeg, double m, double xOffset) {
    public Parameters clamp(Bounds b) {
        return new Parameters(
                b.clampTheta(thetaDeg),
                b.clampM(m),
                b.clampX(xOffset)
        );
    }

    @Override
    public String toString() {
        return String.format(
                "theta=%.10f deg, M=%.10f, X=%.10f",
                thetaDeg, m, xOffset
        );
    }
}
