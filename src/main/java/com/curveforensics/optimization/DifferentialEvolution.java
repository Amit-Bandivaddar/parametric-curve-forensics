package com.curveforensics.optimization;

import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * Small dependency-free Differential Evolution optimizer.
 *
 * This is intentionally implemented in Java so the repository can be
 * built without a numerical-optimization framework.
 */
public final class DifferentialEvolution {
    private final Bounds bounds;
    private final Random random;

    public DifferentialEvolution(Bounds bounds, long seed) {
        this.bounds = bounds;
        this.random = new Random(seed);
    }

    public Parameters optimize(
            ToDoubleFunction<Parameters> objective,
            int populationSize,
            int generations,
            double mutationFactor,
            double crossoverRate
    ) {
        if (populationSize < 5) {
            throw new IllegalArgumentException("Population size must be >= 5.");
        }

        Parameters[] population = new Parameters[populationSize];
        double[] scores = new double[populationSize];

        for (int i = 0; i < populationSize; i++) {
            population[i] = randomParameters();
            scores[i] = objective.applyAsDouble(population[i]);
        }

        for (int gen = 0; gen < generations; gen++) {
            for (int i = 0; i < populationSize; i++) {
                int a, b, c;
                do { a = random.nextInt(populationSize); } while (a == i);
                do { b = random.nextInt(populationSize); } while (b == i || b == a);
                do { c = random.nextInt(populationSize); } while (c == i || c == a || c == b);

                Parameters pa = population[a];
                Parameters pb = population[b];
                Parameters pc = population[c];

                double[] base = {pa.thetaDeg(), pa.m(), pa.xOffset()};
                double[] diffB = {pb.thetaDeg(), pb.m(), pb.xOffset()};
                double[] diffC = {pc.thetaDeg(), pc.m(), pc.xOffset()};

                double[] donor = new double[3];
                for (int d = 0; d < 3; d++) {
                    donor[d] = base[d] + mutationFactor * (diffB[d] - diffC[d]);
                }

                double[] target = {population[i].thetaDeg(), population[i].m(), population[i].xOffset()};
                double[] trial = target.clone();
                int forced = random.nextInt(3);

                for (int d = 0; d < 3; d++) {
                    if (random.nextDouble() < crossoverRate || d == forced) {
                        trial[d] = donor[d];
                    }
                }

                Parameters candidate = new Parameters(trial[0], trial[1], trial[2]).clamp(bounds);
                double candidateScore = objective.applyAsDouble(candidate);

                if (candidateScore <= scores[i]) {
                    population[i] = candidate;
                    scores[i] = candidateScore;
                }
            }
        }

        int best = 0;
        for (int i = 1; i < populationSize; i++) {
            if (scores[i] < scores[best]) best = i;
        }
        return population[best];
    }

    private Parameters randomParameters() {
        return new Parameters(
                lerp(bounds.thetaMin(), bounds.thetaMax(), random.nextDouble()),
                lerp(bounds.mMin(), bounds.mMax(), random.nextDouble()),
                lerp(bounds.xMin(), bounds.xMax(), random.nextDouble())
        );
    }

    private static double lerp(double lo, double hi, double u) {
        return lo + u * (hi - lo);
    }
}
