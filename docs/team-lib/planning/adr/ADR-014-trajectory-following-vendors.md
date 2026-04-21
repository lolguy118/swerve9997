# ADR-014: Trajectory-Following Vendors — PathPlanner and Choreo

## Status

Accepted

## Date

2026-04-21

## Context

Autonomous routines in FRC require driving the robot along smooth
trajectories — from a starting pose to a scoring location, around
obstacles, with consistent speed and heading. Building a trajectory
generator and a path follower from scratch is a significant undertaking
(quintic splines, holonomic drive kinematics, pose estimation, heading
controllers) and has been done well by the community. Two trajectory
vendors dominate the FRC ecosystem:

- **PathPlanner** — mature, community-maintained, desktop path-editing
  GUI, command-based runtime, AutoBuilder convenience APIs. Widely
  adopted; students coming from other FRC teams already know it.
- **Choreo** — newer, optimizer-driven path authoring (solves for
  time-optimal trajectories under kinematic and dynamic constraints),
  lightweight runtime, different workflow. Increasingly adopted by
  teams that want offline-optimized paths.

Team271-Lib chose composition over WPILib Commands
([ADR-013](ADR-013-composition-over-commands.md)); PathPlanner's runtime
API is command-based and Choreo's is follower-based. Both must fit into
the library's lifecycle-based
[`AutoMove`](../sdd/SDD-auto.md)
composition primitive. The library's auto composition layer is
trajectory-vendor-agnostic by design — no `com.pathplanner.*` or
`com.choreo.*` imports leak into `auto/` — but the trajectory-wrapping
glue belongs inside the library rather than duplicated per robot
project.

## Decision

Team271-Lib introduces a vendor-neutral trajectory-following abstraction
at `com.team271.lib.api.trajectory` with two vendor implementations under
`com.team271.lib.vendor.pathplanner` and `com.team271.lib.vendor.choreo`,
mirroring the
[ADR-007 Vision](ADR-007-vendor-neutral-vision-abstraction.md) and
[ADR-006 CAN Bus](ADR-006-can-bus-abstraction.md) patterns:

- `api/trajectory/Trajectory` — interface describing a sampleable
  trajectory: `Optional<TrajectorySample> sample(double timestampSec)`,
  `double totalTimeSeconds()`, `Pose2d initialPose()`,
  `Pose2d endPose()`, `String name()`.
- `api/trajectory/TrajectorySample` — immutable record carrying pose,
  chassis speeds, timestamp, and optional vendor-extensible module-force
  fields.
- `api/trajectory/TrajectoryFollower` — factory for an `AutoMove` that
  samples a `Trajectory` each cycle and emits `ChassisSpeeds` to a
  consumer supplied by the robot project.

V1 ships two vendor implementations:

- `vendor/pathplanner/PathPlannerTrajectoryLoader` +
  `vendor/pathplanner/PathPlannerFollower` — wraps
  `PathPlannerPath.fromPathFile` and `PPHolonomicDriveController`.
- `vendor/choreo/ChoreoTrajectoryLoader` +
  `vendor/choreo/ChoreoFollower` — wraps `Choreo.loadTrajectory` and
  Choreo's built-in follower.

Three integration rules govern both vendors:

1. **Trajectory followers are wrapped as `AutoMove`s.** The factory
   method returns an `AutoMove` with a mandatory timeout per
   [ADR-012](ADR-012-mandatory-timeouts-fail-safe.md). On timeout the
   follower stops the drivetrain and emits an `Elastic` WARNING
   notification, consistent with `AutoMoveConditional`.
2. **Path / trajectory definitions are robot-project responsibility.**
   The library does not know what trajectories a given robot uses; it
   provides the integration layer only.
3. **Alliance flipping, heading logic, and velocity tuning are
   robot-project responsibility.** These are competition-specific and
   change year-over-year.

`CommandBridge.asAutoMove` is retained as the escape hatch for
command-based PathPlanner entry points (AutoBuilder, `PathPlannerAuto`
with event markers). The new `TrajectoryFollower` path is the preferred
lifecycle-native integration for straight trajectory following.

## Rationale

1. **Two-vendor launch is the real abstraction test.** Shipping only one
   vendor risks baking that vendor's assumptions into the interface.
   PathPlanner (command-based, holonomic drive controller) and Choreo
   (follower-based, pre-optimized trajectories) differ enough that
   anything `TrajectoryFollower` can express cleanly across both has a
   realistic chance of holding. This is the same reasoning applied in
   [ADR-007 §Rationale #1](ADR-007-vendor-neutral-vision-abstraction.md).
2. **Community-proven trajectory math.** Both PathPlanner and Choreo
   are maintained by teams larger than 271; bugs are found and fixed
   quickly. Writing our own trajectory generator and follower would
   consume offseason time we'd rather spend on mechanism code.
3. **GUI path authoring stays with the vendor.** Students can author
   paths visually in either tool rather than typing coordinates. The
   library contract does not prescribe authoring workflow — robot
   projects choose per path.
4. **AutoBuilder and named-auto patterns compose well.**
   PathPlanner's AutoBuilder outputs `Command`s that wrap cleanly via
   the existing `CommandBridge.asAutoMove`. Choreo's trajectory objects
   wrap cleanly via the new `TrajectoryFollower`. Both reach an
   `AutoMove` without leaking vendor types into `AutoMode`.
5. **Not our core competency.** Writing our own path follower is a
   solved problem with two healthy community solutions. Abstracting
   between them is cheaper than reimplementing either.

## Consequences

**Easier:**

- Robot-project control code depends on `Trajectory` /
  `TrajectoryFollower`, not a specific vendor — swapping PathPlanner
  for Choreo on a given bot becomes a construction change, not a
  rewrite.
- Path authoring remains a graphical, iterative workflow in whichever
  tool the robot project prefers.
- Path-following quality improves with each vendor release.
- Students coming from other FRC teams recognize the workflow — either
  tool is widely known.
- Test doubles (`FakeTrajectory`, `FakeFollower`) stand in for either
  vendor and live in `libtest/trajectory/`.

**Harder:**

- Both vendor APIs must be kept in sync with the `Trajectory` /
  `TrajectoryFollower` contract. When one vendor adds a feature, the
  library must either expose it through the shared contract, through
  the vendor wrapper's own methods, or through the raw passthrough
  (per [ADR-005](ADR-005-passthrough-wrapper-not-wall.md)).
- Two vendordeps now appear in [SCMP §4](../SCMP.md#4-vendordep-management-team271-lib-specifics)
  vendordep-freshness tracking — PathplannerLib and ChoreoLib.
- Vendor API breaking changes (rare, but real) require library updates
  in two vendor packages instead of one.
- The asymmetry between PathPlanner's command-based entry points
  (AutoBuilder, event markers) and Choreo's follower-based entry
  points remains visible: event-driven autos still route through
  `CommandBridge` regardless of trajectory source. This asymmetry
  needs one paragraph of explanation in
  [SDD-auto.md](../sdd/SDD-auto.md) §5.

## Alternatives Considered

- **Retain PathPlanner-only (status quo).** Rejected — teams that
  prefer Choreo's optimizer-driven authoring have no library path to
  adopt it, and the library's trajectory seam stays informal
  (duplicated per robot project).
- **Choreo-only, deprecate PathPlanner.** Rejected — PathPlanner's
  ecosystem (AutoBuilder, event markers, GUI authoring) is more
  mature, and robot-project muscle memory is built around it.
- **Hide both vendors behind a walled abstraction (no passthrough).**
  Rejected per [ADR-005](ADR-005-passthrough-wrapper-not-wall.md).
  Walled abstractions force the library to re-expose every vendor
  feature as library API; passthrough lets advanced users reach
  vendor-specific APIs (`PPHolonomicDriveController` tuning, Choreo's
  event-based triggers) without waiting on a library release.
- **Command-based-only via `CommandBridge` (no new abstraction).**
  Rejected — works for PathPlanner's `Command`-shaped APIs but forces
  Choreo users to invent a `Command` wrapper per trajectory. The new
  `TrajectoryFollower` contract matches Choreo's natural shape
  without losing the command-based escape hatch.
- **Build our own path follower.** Rejected — significant effort for
  a solved problem; offseason time better spent on mechanism code.
- **Open-loop path following.** Rejected — too imprecise for modern
  FRC autonomous scoring.
- **Ship the interface with a single vendor in V1 (PathPlanner),
  add Choreo later.** Rejected for the same reason ADR-007 rejected
  single-vendor-launch for vision: a one-vendor abstraction is not a
  real test, and the interface would almost certainly need breaking
  changes the first time Choreo landed.

## References

- [SDD-auto.md](../sdd/SDD-auto.md) — autonomous composition +
  trajectory-follower integration.
- [SDD-api.md](../sdd/SDD-api.md) — `api/trajectory/` interface family.
- [SDD-vendor-ctre.md §3.3 bridge/CommandBridge](../sdd/SDD-vendor-ctre.md#33-bridgecommandbridge)
  — command-based escape hatch.
- [SCMP.md §4 Vendordep Management](../SCMP.md#4-vendordep-management-team271-lib-specifics)
  — PathplannerLib + ChoreoLib freshness policy.
- [ADR-005](ADR-005-passthrough-wrapper-not-wall.md) — passthrough contract.
- [ADR-003](ADR-003-layered-architecture.md) — layered architecture.
- [ADR-013](ADR-013-composition-over-commands.md) — composition over Commands.
- [ADR-012](ADR-012-mandatory-timeouts-fail-safe.md) — mandatory timeouts.
- [ADR-007](ADR-007-vendor-neutral-vision-abstraction.md) — two-vendor
  launch precedent (Vision).
- [ADR-006](ADR-006-can-bus-abstraction.md) — bounded-vendor-set
  abstraction precedent (CAN Bus).
- PathPlanner docs: <https://pathplanner.dev/home.html>
- Choreo docs: <https://choreo.autos/>
- ChoreoLib source: <https://github.com/SleipnirGroup/Choreo>
- TrajoptLib (Choreo's trajectory-optimization backend, C++ API
  reference): <https://choreo.autos/api/trajoptlib/cpp/index.html>
