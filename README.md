# Parametric Curve Forensics

> **R&D / AI Assignment — Java Implementation**

A clean, dependency-light Java solution for recovering the three hidden parameters of
a parametric curve from an observed `xy_data.csv` point cloud.

## 1. Problem

The assignment defines:

```text
x(t) = t*cos(theta) - exp(M*|t|)*sin(0.3t)*sin(theta) + X

y(t) = 42 + t*sin(theta) + exp(M*|t|)*sin(0.3t)*cos(theta)
```

Unknown parameters:

| Parameter | Constraint |
|---|---|
| `theta` | `0 < theta < 50` degrees |
| `M` | `-0.05 < M < 0.05` |
| `X` | `0 < X < 100` |

The curve domain is:

```text
6 < t < 60
```

The assessment evaluates the **L1 distance between uniformly sampled expected and
predicted curve points**, followed by an explanation of the process and the submitted
code/GitHub repository.

## 2. Project idea

This project treats the task as **parametric curve forensics**:

> Given the visible trajectory but not its generating parameters, reconstruct the hidden
> rotation, exponential envelope and horizontal translation.

The three parameters have distinct geometric effects:

- `theta` — rotates the combined trajectory.
- `M` — controls the exponential growth/decay of the oscillatory component.
- `X` — horizontally translates the curve.

## 3. Mathematical formulation

Let

```text
A(t) = exp(M*|t|) * sin(0.3t)
```

Then:

```text
x(t) = t*cos(theta) - A(t)*sin(theta) + X
y(t) = 42 + t*sin(theta) + A(t)*cos(theta)
```

The implementation uses radians internally because Java's `Math.sin` and `Math.cos`
expect radians, while `theta` is reported in degrees.

## 4. Optimization objective

For observed points `(x_i, y_i)` and predicted points `(x'_i, y'_i)`:

```text
L1 = (1/N) * sum(
        |x'_i - x_i| +
        |y'_i - y_i|
     )
```

The optimizer minimizes this quantity subject to the assignment's parameter bounds.

This is intentional: the objective is aligned with the metric named in the assignment
rather than substituting a different loss such as ordinary squared error.

## 5. Optimization strategy

### Stage 1 — Differential Evolution

A dependency-free Java implementation searches the entire bounded three-dimensional
parameter space:

```text
theta ∈ (0, 50)
M     ∈ (-0.05, 0.05)
X     ∈ (0, 100)
```

A fixed random seed is used for reproducibility.

### Stage 2 — Nelder-Mead

The best Differential Evolution candidate is locally refined using a dependency-free
Nelder-Mead implementation.

This two-stage design combines:

- global exploration,
- deterministic local polishing,
- reproducibility,
- no external numerical-optimization library.

## 6. Dataset

The original assignment refers to a file named `xy_data.csv`. That file was **not included
with the assignment PDF available to us**, so this repository deliberately does not pretend
that a synthetic file is the official dataset.

Instead, `sample-data/xy_data_sample.csv` is included only to demonstrate that the complete
pipeline is runnable.

### When the official CSV is available

Place the official file anywhere you like and run:

```bash
mvn -q exec:java   -Dexec.mainClass=com.curveforensics.Main   -Dexec.args="/path/to/xy_data.csv"
```

The first two numeric columns should be `x` and `y`; conventional `x,y` headers are
also supported.

> **Important:** the final parameter values must be obtained from the official
> `xy_data.csv`, not from the included sample dataset.

## 7. Sample data

The included sample dataset was generated solely for reproducible development testing
using the same equation and valid parameter values:

```text
theta = 31.7 degrees
M     = 0.0185
X     = 27.4
```

These values are **not claimed to be the official assignment answer**.

## 8. Running the project

### Requirements

- Java 17+
- Maven 3.8+

Check:

```bash
java -version
mvn -version
```

### Run with the sample

```bash
mvn -q exec:java   -Dexec.mainClass=com.curveforensics.Main   -Dexec.args="sample-data/xy_data_sample.csv"
```

### Run with the official dataset

```bash
mvn -q exec:java   -Dexec.mainClass=com.curveforensics.Main   -Dexec.args="data/xy_data.csv"
```

If you prefer, simply put the official CSV at `data/xy_data.csv`.

## 9. Expected console flow

```text
PARAMETRIC CURVE FORENSICS
        ↓
CSV validation
        ↓
Differential Evolution
        ↓
Best global candidate
        ↓
Nelder-Mead refinement
        ↓
Final theta, M, X
        ↓
L1 / RMSE / maximum-error validation
```

## 10. Desmos visualization

The assignment provides a Desmos representation of the curve.

Official Desmos calculator:

https://www.desmos.com/calculator/rfj91yrxob

Use:

```text
(
t*cos(theta) - e^(M*abs(t))*sin(0.3t)*sin(theta) + X,
42 + t*sin(theta) + e^(M*abs(t))*sin(0.3t)*cos(theta)
)
```

with:

```text
6 < t < 60
```

After obtaining the parameters from the official CSV, substitute them into Desmos and
capture the resulting curve for the final submission.

## 11. Project structure

```text
parametric-curve-forensics/
├── data/
│   └── xy_data.csv                 # add official dataset here
├── sample-data/
│   └── xy_data_sample.csv          # development/demo dataset only
├── docs/
│   └── README.md                   # space for screenshots/notes
├── src/
│   └── main/
│       └── java/com/curveforensics/
│           ├── Main.java
│           ├── model/
│           │   ├── Point.java
│           │   └── CurveModel.java
│           ├── io/
│           │   └── CsvReader.java
│           ├── optimization/
│           │   ├── Bounds.java
│           │   ├── Parameters.java
│           │   ├── L1Objective.java
│           │   ├── DifferentialEvolution.java
│           │   ├── NelderMead.java
│           │   └── OptimizationResult.java
│           └── util/
│               └── Metrics.java
├── .gitignore
├── pom.xml
└── README.md
```

## 12. Why this implementation is suitable for an R&D assignment

### Reproducibility
The global optimizer uses a fixed seed.

### Correct scoring objective
The optimization directly minimizes mean L1 coordinate error.

### Bounded search
All three parameters remain inside the assignment constraints.

### No black-box external optimizer
The core Differential Evolution and Nelder-Mead algorithms are implemented directly
in Java, making the methodology inspectable.

### Separation of concerns
CSV reading, mathematical modelling, optimization and validation are separate classes.

## 13. Limitations and assumptions

The assignment PDF available to us does not expose the actual rows of `xy_data.csv`.
Therefore the final solver must be run against the official CSV before submitting numerical
results.

The current objective assumes that the CSV rows correspond in order to uniformly sampled
values of `t` over the open interval `(6, 60)`, which follows the assessment wording about
uniformly sampled curve points. If the official CSV contains an explicit `t` column or a
different sampling convention, inspect it first and adjust the sampling layer accordingly.

## 14. Final submission checklist

- [ ] Replace the sample dataset with the official `xy_data.csv`.
- [ ] Run the Java solver.
- [ ] Record final `theta`, `M`, and `X`.
- [ ] Record the final L1 error.
- [ ] Verify the result against the parameter bounds.
- [ ] Plot/inspect the fitted curve.
- [ ] Validate the result in Desmos.
- [ ] Add a Desmos screenshot under `docs/`.
- [ ] Update the Results section with the official values.
- [ ] Run the project from a clean checkout.
- [ ] Push the complete repository to GitHub.

## 15. Conclusion

The project converts the assignment into a reproducible inverse-curve problem: the
observed geometry is known, while its rotation, exponential envelope and translation
are recovered numerically.

The final answer should always be produced from the official `xy_data.csv`.
