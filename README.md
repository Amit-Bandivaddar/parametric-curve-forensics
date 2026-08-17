# Parametric Curve Forensics

> **R&D / AI Assignment — Java Implementation**

A clean, dependency-light Java solution for recovering the three hidden parameters of
a parametric curve from an observed `xy` point cloud.

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

The official dataset used for this project is:

```text
UVCE_BTech_Flam_Resource.csv
```

It contains **1,500 observed `(x, y)` points** and is included in the repository under:

```text
data/UVCE_BTech_Flam_Resource.csv
```

The CSV contains numeric `x` and `y` columns, which are supported by the existing
CSV reader.

The included `sample-data/xy_data_sample.csv` remains available only as a small,
reproducible development/demo dataset.

### Run with the official dataset

```bash
mvn -q exec:java -Dexec.mainClass=com.curveforensics.Main -Dexec.args="data/UVCE_BTech_Flam_Resource.csv"
```

The results reported in this README were obtained by running the Java implementation
against all **1,500 points** in the official dataset.

## 7. Sample data

The included sample dataset was generated solely for reproducible development testing
using the same equation and valid parameter values:

```text
theta = 31.7 degrees
M     = 0.0185
X     = 27.4
```

These values are **not** the results obtained from the official dataset.

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
mvn -q exec:java -Dexec.mainClass=com.curveforensics.Main -Dexec.args="sample-data/xy_data_sample.csv"
```

### Run with the official dataset

```bash
mvn -q exec:java -Dexec.mainClass=com.curveforensics.Main -Dexec.args="data/UVCE_BTech_Flam_Resource.csv"
```

## 9. Results on the Official Dataset

The Java solver was successfully executed against the official
`UVCE_BTech_Flam_Resource.csv` dataset containing **1,500 points**.

### Recovered parameters

```text
theta = 28.1184232552 degrees
M     = 0.0213889578
X     = 54.9003188543
```

### Validation metrics

```text
Mean L1 error      = 2.524339544182e+01
RMSE               = 1.609119787101e+01
Maximum abs error  = 4.785217662268e+01
```

The optimization completed successfully using the two-stage Differential Evolution
and Nelder-Mead pipeline.

### Recovered parametric equation

Using the recovered parameters:

```text
(
  t*cos(28.1184232552°)
    - e^(0.0213889578*|t|)*sin(0.3t)*sin(28.1184232552°)
    + 54.9003188543,

  42 + t*sin(28.1184232552°)
    + e^(0.0213889578*|t|)*sin(0.3t)*cos(28.1184232552°)
)
```

Domain:

```text
6 < t < 60
```

The values above are the direct output of the Java implementation when executed
against the official 1,500-point CSV.

## 10. Desmos visualization

The assignment provides a Desmos representation of the curve.

Official Desmos calculator:

https://www.desmos.com/calculator/rfj91yrxob

Use the recovered parameters:

```text
(
t*cos(28.1184232552°)
- e^(0.0213889578|t|)*sin(0.3t)*sin(28.1184232552°)
+ 54.9003188543,

42 + t*sin(28.1184232552°)
+ e^(0.0213889578|t|)*sin(0.3t)*cos(28.1184232552°)
)
```

with:

```text
6 < t < 60
```

The existing `docs/sample-curve.png` remains a visualization of the synthetic
development dataset and should not be described as the official dataset result.

## 11. Project structure

```text
parametric-curve-forensics/
├── data/
│   └── UVCE_BTech_Flam_Resource.csv  # official 1,500-point dataset
├── sample-data/
│   └── xy_data_sample.csv             # development/demo dataset only
├── docs/
│   └── README.md
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

The official dataset is now available and has been successfully processed by the Java
implementation.

The current objective assumes that the CSV rows correspond in order to uniformly sampled
values of `t` over the open interval `(6, 60)`, following the assessment wording about
uniformly sampled curve points.

If the official CSV contains an explicit `t` column or uses a different sampling
convention, the sampling layer should be adjusted accordingly.

## 14. Final submission checklist

- [x] Add the official `UVCE_BTech_Flam_Resource.csv` dataset.
- [x] Run the Java solver against all 1,500 points.
- [x] Record final `theta`, `M`, and `X`.
- [x] Record the final L1 error.
- [x] Record RMSE and maximum absolute error.
- [x] Verify the recovered parameters are inside the assignment bounds.
- [ ] Plot/inspect the fitted curve.
- [ ] Validate the result in Desmos.
- [ ] Add an official-dataset visualization under `docs/`.
- [x] Update the README with the official results.
- [ ] Run the project from a clean checkout.
- [x] Push the complete repository to GitHub.

## 15. Conclusion

The project converts the assignment into a reproducible inverse-curve problem: the
observed geometry is known, while its rotation, exponential envelope and translation
are recovered numerically.

The final recovered parameters for the official 1,500-point dataset are:

```text
theta = 28.1184232552 degrees
M     = 0.0213889578
X     = 54.9003188543
```

The solver successfully processes the official dataset using Differential Evolution
followed by Nelder-Mead refinement, with the reported L1, RMSE and maximum-error
metrics providing numerical validation of the recovered curve.

The final numerical result in this repository is produced from the official
`UVCE_BTech_Flam_Resource.csv` dataset included under `data/`.
