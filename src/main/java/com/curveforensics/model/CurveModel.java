package com.curveforensics.model;

/**
 * Implements the parametric curve from the assignment.
 *
 * x(t) = t*cos(theta) - exp(M*|t|)*sin(0.3t)*sin(theta) + X
 * y(t) = 42 + t*sin(theta) + exp(M*|t|)*sin(0.3t)*cos(theta)
 *
 * theta is supplied in degrees; Java trigonometric functions use radians.
 */
public final class CurveModel {
    private CurveModel() {}

    public static Point point(double t, double thetaDeg, double m, double xOffset) {
        double theta = Math.toRadians(thetaDeg);
        double wave = Math.exp(m * Math.abs(t)) * Math.sin(0.3 * t);

        double x = t * Math.cos(theta)
                - wave * Math.sin(theta)
                + xOffset;

        double y = 42.0
                + t * Math.sin(theta)
                + wave * Math.cos(theta);

        return new Point(x, y);
    }
}
