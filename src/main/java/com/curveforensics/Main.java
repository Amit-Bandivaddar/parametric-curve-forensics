package com.curveforensics;

import com.curveforensics.io.CsvReader;
import com.curveforensics.model.Point;
import com.curveforensics.optimization.*;
import com.curveforensics.util.Metrics;

import java.nio.file.Path;
import java.util.List;

/**
 * Entry point.
 *
 * Usage:
 *   mvn -q exec:java -Dexec.mainClass=com.curveforensics.Main \
 *       -Dexec.args="sample-data/xy_data_sample.csv"
 *
 * Or, after packaging:
 *   java -cp target/classes com.curveforensics.Main path/to/xy_data.csv
 */
public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ... Main <xy_data.csv>");
            System.exit(2);
        }

        Path csv = Path.of(args[0]);
        List<Point> points = CsvReader.readXY(csv);

        System.out.println("==============================================");
        System.out.println(" PARAMETRIC CURVE FORENSICS");
        System.out.println(" Hidden-Parameter Recovery");
        System.out.println("==============================================");
        System.out.println("Dataset : " + csv.toAbsolutePath());
        System.out.println("Points  : " + points.size());
        System.out.println();

        Bounds bounds = Bounds.assignment();
        L1Objective objective = new L1Objective(points);

        System.out.println("Stage 1/2: Differential Evolution...");
        DifferentialEvolution de = new DifferentialEvolution(bounds, 42L);

        Parameters global = de.optimize(
                objective::evaluate,
                80,       // population
                1500,     // generations
                0.75,     // mutation
                0.90      // crossover
        );

        System.out.println("Global candidate: " + global);
        System.out.println();

        System.out.println("Stage 2/2: Nelder-Mead refinement...");
        NelderMead nm = new NelderMead(bounds);
        Parameters best = nm.optimize(global, objective::evaluate, 10000);

        double l1 = objective.evaluate(best);
        double rmse = Metrics.rmse(points, objective.getTValues(), best);
        double maxError = Metrics.maxAbs(points, objective.getTValues(), best);

        System.out.println();
        System.out.println("=============== FINAL RESULT ================");
        System.out.printf("theta = %.10f degrees%n", best.thetaDeg());
        System.out.printf("M     = %.10f%n", best.m());
        System.out.printf("X     = %.10f%n", best.xOffset());
        System.out.println("----------------------------------------------");
        System.out.printf("Mean L1 error      = %.12e%n", l1);
        System.out.printf("RMSE               = %.12e%n", rmse);
        System.out.printf("Maximum abs error  = %.12e%n", maxError);
        System.out.println("==============================================");
    }
}
