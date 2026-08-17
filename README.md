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

The implementation evaluates the observed point cloud against the predicted curve
without relying on the CSV row order as the curve's parameter order.

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

The CSV contains numeric `x` and `y` columns, which are supported by the CSV reader.

The included `sample-data/xy_data_sample.csv` remains available only as a small,
reproducible development/demo dataset.

### Run with the official dataset

```bash
mvn -q exec:java -Dexec.mainClass=com.curveforensics.Main -Dexec.args="data/UVCE_BTech_Flam_Resource.csv"
```

The results reported in this README were obtained by running the Java implementation
against all **1,500 points** in the official dataset.

## 7. Sample data

The included `sample-data/xy_data_sample.csv` was generated solely for reproducible
development testing using the same equation and valid parameter values.

```text
theta = 31.7 degrees
M     = 0.0185
X     = 27.4
```

These values are **development/sample values only** and are not the final assignment result.

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

## 9. Final Results on the Official Dataset

The corrected Java solver was successfully executed against the official
`UVCE_BTech_Flam_Resource.csv` dataset containing **1,500 points**.

### Recovered parameters

The optimizer returned:

```text
theta = 29.9999730015 degrees
M     = 0.0299999971
X     = 54.9999983399
```

These values are effectively:

```text
theta ≈ 30 degrees
M     ≈ 0.03
X     ≈ 55
```

The clean values `30°`, `0.03`, and `55` reproduce the observed curve to within the
rounding precision of the supplied CSV.

### Validation metrics

```text
Mean L1 error      = 3.495102745831e-06
RMSE               = 2.465724320986e-06
Maximum abs error  = 1.511965457723e-05
```

The optimization completed successfully using Differential Evolution followed by
Nelder-Mead refinement.

### Final answer

```text
theta = 30°
M     = 0.03
X     = 55
```

### Recovered parametric equation

Using the clean recovered values:

```text
(
  t*cos(30°)
    - e^(0.03*|t|)*sin(0.3t)*sin(30°)
    + 55,

  42 + t*sin(30°)
    + e^(0.03*|t|)*sin(0.3t)*cos(30°)
)
```

For Desmos, the angle can be entered in radians:

```text
0.5235987756
```

The equivalent Desmos-ready expression is:

```text
(t*cos(0.5235987756)-exp(0.03*abs(t))*sin(0.3*t)*sin(0.5235987756)+55,42+t*sin(0.5235987756)+exp(0.03*abs(t))*sin(0.3*t)*cos(0.5235987756))
```

with:

```text
6 <= t <= 60
```

## 10. Desmos Visualization

The assignment provides a Desmos representation of the curve:

https://www.desmos.com/calculator/rfj91yrxob

The project also maintains a Desmos graph using the recovered parameters.

Desmos graph:

https://www.desmos.com/calculator/5m6dx8fnco

The recovered curve uses:

```text
theta = 30°
M     = 0.03
X     = 55
```

and the domain:

```text
6 <= t <= 60
```

The repository visualization is stored as:

```text
docs/sample_curve.png
```

This image represents the recovered curve using the official dataset result.

## 11. Project structure

```text
parametric-curve-forensics/
├── data/
│   └── UVCE_BTech_Flam_Resource.csv  # official 1,500-point dataset
├── sample-data/
│   └── xy_data_sample.csv             # development/demo dataset
├── docs/
│   ├── sample_curve.png               # recovered official curve visualization
│   ├── assignment-reference.pdf
│   ├── BUILD_VERIFICATION.txt
│   ├── FINAL_RESULTS_TEMPLATE.md
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

### Point-cloud handling

The official CSV is treated as a point cloud rather than assuming that its row order
represents increasing values of `t`.

## 13. Limitations and assumptions

The supplied CSV contains an observed `(x, y)` point cloud. The solver therefore does
not depend on the input rows being sorted by the hidden parameter `t`.

The fitted curve is evaluated over the assignment domain `(6, 60)`, and the recovered
parameters are validated against the assignment's parameter bounds.

The supplied CSV coordinates are rounded numeric observations, so the optimizer's
recovered values differ from the clean values by only a few millionths.

## 14. Final submission checklist

- [x] Add the official `UVCE_BTech_Flam_Resource.csv` dataset.
- [x] Run the corrected Java solver against all 1,500 points.
- [x] Recover final `theta`, `M`, and `X`.
- [x] Record the final L1 error.
- [x] Record RMSE and maximum absolute error.
- [x] Verify the recovered parameters are inside the assignment bounds.
- [x] Generate the recovered-curve visualization.
- [x] Update the Desmos graph with the recovered parameters.
- [x] Update the README with the official results.
- [ ] Run the project from a clean checkout.
- [x] Push the complete repository to GitHub.

## 15. Conclusion

The project converts the assignment into a reproducible inverse-curve problem: the
observed geometry is known, while its rotation, exponential envelope and translation
are recovered numerically.

Using the official 1,500-point dataset, the corrected solver recovered:

```text
theta = 29.9999730015 degrees
M     = 0.0299999971
X     = 54.9999983399
```

which corresponds to the clean final values:

```text
theta = 30°
M     = 0.03
X     = 55
```

The extremely small validation errors confirm that these parameters reproduce the
supplied point cloud to within the precision of the dataset.

The final answer for submission is:

```text
θ = 30°
M = 0.03
X = 55
```
