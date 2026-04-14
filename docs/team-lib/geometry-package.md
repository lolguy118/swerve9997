<!-- markdownlint-disable MD013 MD060 -->
# Geometry Package

> **Scope:** This document covers the library's custom 2D geometry
> classes. These are Team 271's implementations — not WPILib wrappers.
> WPILib has its own `edu.wpi.first.math.geometry` package; the
> library's geometry classes predate WPILib's and are used in legacy
> utility code and path-related calculations.

---

## Class Hierarchy

```text
State<S>                    (interface — distance, interpolation, CSV)
├── IRotation2d<S>          (interface — getRotation())
├── ITranslation2d<S>       (interface — getTranslation())
└── IPose2d<S>              (interface — extends IRotation2d + ITranslation2d)

Rotation2d                  (implements IRotation2d — heading/angle)
Translation2d               (implements ITranslation2d — 2D point)
Pose2d                      (implements IPose2d — rigid transform)
Twist2d                     (differential motion: dx, dy, dtheta)
```

---

## Unit Conventions

| Quantity | Unit | Notes |
|----------|------|-------|
| Position (x, y) | Meters | Translation2d coordinates |
| Angles | Radians (internal) | Rotation2d stores cos/sin + radians |
| Angle output | Radians and degrees | `getRadians()` / `getDegrees()` |
| Twist dx, dy | Meters | Differential translation |
| Twist dtheta | Radians | Differential rotation |

---

## Rotation2d — Heading/Angle

Represents a 2D rotation. Internally stores both `cos_angle_` and
`sin_angle_` (to avoid recomputing trig for rotation composition)
plus `radians_` (lazy-computed via `atan2` when needed).

### Factory Methods

| Method | Creates From |
|--------|-------------|
| `Rotation2d()` | Identity (0 degrees) |
| `Rotation2d(radians, normalize)` | Angle in radians |
| `Rotation2d(x, y, normalize)` | Cos/sin components |
| `fromRadians(angle)` | Radians (static) |
| `fromDegrees(angle)` | Degrees (static) |
| `identity()` | Cached identity (static) |

### Key Operations

| Method | Returns | Description |
|--------|---------|-------------|
| `cos()` / `sin()` / `tan()` | `double` | Trig values (lazy-computed) |
| `getRadians()` / `getDegrees()` | `double` | Angle in specified unit |
| `rotateBy(other)` | `Rotation2d` | Composition via rotation matrix multiplication |
| `inverse()` | `Rotation2d` | Negated rotation (undoes this rotation) |
| `normal()` | `Rotation2d` | 90-degree counterclockwise: `[-sin, cos]` |
| `isParallel(other)` | `boolean` | Same or opposite direction |
| `toTranslation()` | `Translation2d` | Unit vector `[cos, sin]` |
| `interpolate(other, x)` | `Rotation2d` | Spherical linear interpolation |
| `distance(other)` | `double` | Angular distance in radians |

---

## Translation2d — 2D Point

Represents a 2D position as Cartesian coordinates `(x, y)`.

### Factory Methods

| Method | Creates From |
|--------|-------------|
| `Translation2d()` | Origin (0, 0) |
| `Translation2d(x, y)` | Coordinates |
| `Translation2d(start, end)` | Vector from start to end |
| `identity()` | Cached origin (static) |

### Key Operations

| Method | Returns | Description |
|--------|---------|-------------|
| `x()` / `y()` | `double` | Coordinates |
| `norm()` | `double` | Euclidean distance from origin |
| `translateBy(other)` | `Translation2d` | Vector addition |
| `rotateBy(rotation)` | `Translation2d` | Apply rotation matrix |
| `direction()` | `Rotation2d` | Direction angle via `atan2(y, x)` |
| `inverse()` | `Translation2d` | Negation: `(-x, -y)` |
| `scale(s)` | `Translation2d` | Scalar multiplication |
| `interpolate(other, x)` | `Translation2d` | Linear interpolation |
| `distance(other)` | `double` | Euclidean distance to other |

### Static Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `dot(a, b)` | `double` | Dot product |
| `cross(a, b)` | `double` | 2D cross product (scalar) |
| `getAngle(a, b)` | `Rotation2d` | Angle between vectors |

---

## Pose2d — Rigid Transform (SE(2))

Represents a 2D rigid body transform: position (`Translation2d`) plus
heading (`Rotation2d`). Uses Sophus SE(2) group operations for
mathematically correct interpolation and composition.

### Factory Methods

| Method | Creates From |
|--------|-------------|
| `Pose2d()` | Identity (origin, 0 degrees) |
| `Pose2d(x, y, rotation)` | Coordinates + rotation |
| `Pose2d(translation, rotation)` | Components |
| `identity()` | Cached identity (static) |
| `fromTranslation(t)` | Translation with zero rotation |
| `fromRotation(r)` | Origin with given rotation |

### Key Operations

| Method | Returns | Description |
|--------|---------|-------------|
| `getTranslation()` | `Translation2d` | Position component |
| `getRotation()` | `Rotation2d` | Heading component |
| `transformBy(other)` | `Pose2d` | Apply rigid transform (translate then rotate) |
| `inverse()` | `Pose2d` | Undoes this transform |
| `interpolate(other, x)` | `Pose2d` | Constant-curvature interpolation via twist space |
| `intersection(other)` | `Translation2d` | Intersection of two heading lines |
| `isColinear(other)` | `boolean` | Parallel headings, aligned positions |
| `distance(other)` | `double` | Distance metric via twist norm |

### Exponential / Logarithmic Maps

These implement the SE(2) Lie group operations for converting between
differential motion (`Twist2d`) and rigid transforms (`Pose2d`):

| Method | Signature | Description |
|--------|-----------|-------------|
| `exp(twist)` | `static Pose2d exp(Twist2d)` | Integrates a twist into a pose change |
| `log(pose)` | `static Twist2d log(Pose2d)` | Extracts the twist that produces a pose |

`exp()` uses exact trigonometric formulas for large rotations and
Taylor expansion for small angles (`|dtheta| < 1e-9`). This ensures
numerically stable constant-curvature motion — the robot follows an
arc, not a straight line followed by a rotation.

`interpolate()` works by computing `log(inverse().transformBy(other))`,
scaling the resulting twist by `x`, then applying `exp()`. This gives
geometrically correct interpolation along the constant-curvature path.

---

## Twist2d — Differential Motion

Represents an incremental motion: translational velocity `(dx, dy)` in
meters and angular velocity `dtheta` in radians. Used as the tangent
vector for Pose2d operations.

### Fields (public final)

| Field | Type | Unit |
|-------|------|------|
| `dx` | `double` | Meters |
| `dy` | `double` | Meters |
| `dtheta` | `double` | Radians |

### Key Operations

| Method | Returns | Description |
|--------|---------|-------------|
| `scaled(scale)` | `Twist2d` | Scales all components |
| `norm()` | `double` | Translational magnitude: `sqrt(dx^2 + dy^2)` |
| `curvature()` | `double` | Path curvature: `dtheta / norm()` |

---

## Relationship to WPILib Geometry

These classes are structurally similar to `edu.wpi.first.math.geometry`
(`Pose2d`, `Rotation2d`, `Translation2d`, `Twist2d`) but are **not
interchangeable**. Key differences:

- WPILib's `Rotation2d` constructor takes radians; this library's takes
  `(cos, sin)` or `(radians, normalize)`
- WPILib uses `plus()` / `minus()` operators; this library uses
  `rotateBy()` / `translateBy()` / `transformBy()`
- No conversion methods are provided between the two

The library's geometry classes predate WPILib's mature geometry API.
Future consideration: migrate to WPILib geometry if the team decides
to standardize on a single geometry library.
