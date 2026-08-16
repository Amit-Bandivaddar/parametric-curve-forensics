package com.curveforensics.optimization;

/** Final optimization result and diagnostics. */
public record OptimizationResult(
        Parameters parameters,
        double l1,
        double rmse,
        double maxAbsoluteError
) {
}
