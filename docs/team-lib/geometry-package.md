<!-- markdownlint-disable MD013 MD060 -->
# Geometry Package — Removed

> **Status: Removed** — The custom geometry package was deleted. All
> geometry operations use `edu.wpi.first.math.geometry` (Rotation2d,
> Translation2d, Pose2d, Twist2d).

If Lie group SE(2) operations (exp/log) or heading-line intersection
math is ever needed, add a `GeometryUtil` with static methods operating
on WPILib types rather than recreating a parallel class hierarchy.
