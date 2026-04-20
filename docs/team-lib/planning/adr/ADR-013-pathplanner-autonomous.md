# ADR-013: PathPlanner for Autonomous Path Following

## Status

Accepted

## Date

2026-04-20

## Context

Autonomous routines in FRC require driving the robot along smooth
trajectories — from a starting pose to a scoring location, around
obstacles, with consistent speed and heading. Building a trajectory
generator and a path follower from scratch is a significant
undertaking (quintic splines, holonomic drive kinematics, pose
estimation, heading controllers) and has been done well by the
community.

**PathPlanner** provides a mature, community-maintained solution:
a desktop path-editing tool (draw paths on a field image), a runtime
library that follows those paths, and AutoBuilder convenience APIs.

Team271-Lib chose composition over WPILib Commands
([ADR-005](ADR-005-composition-over-commands.md)), but PathPlanner's
runtime API is Command-Based.

## Decision

Team271-Lib integrates PathPlanner for trajectory generation and path
following in autonomous mode. Integration follows three rules:

1. **Library wraps PathPlanner commands via `CommandBridge`.** The
   bridge makes a PathPlanner `Command` usable as a library
   `AutoMove` with a mandatory timeout, matching the
   [ADR-011](ADR-011-mandatory-timeouts-fail-safe.md) rule.
2. **Path definitions are robot-project responsibility.** The library
   does not know what paths a given robot uses; it provides the
   integration layer only.
3. **Alliance flipping, heading logic, and velocity tuning are
   robot-project responsibility.** These are competition-specific and
   change year-over-year.

## Rationale

1. **Community-proven.** PathPlanner is used by a large fraction of
   FRC teams; bugs are found and fixed quickly.
2. **GUI path editing.** Students can author paths visually rather
   than typing coordinates. This reduces mistakes and enables rapid
   iteration.
3. **AutoBuilder pattern.** PathPlanner's AutoBuilder API composes
   well with our `CommandBridge` — the library wraps AutoBuilder
   outputs as `AutoMove`s.
4. **Not our core competency.** Writing our own path follower would
   consume offseason time we'd rather spend on mechanism code,
   testing, and documentation.

## Consequences

**Easier:**

- Path authoring is a graphical, iterative workflow.
- Path following quality improves with each PathPlanner release.
- Students coming from other FRC teams recognize the workflow.

**Harder:**

- PathPlanner's dependencies add build time and JAR size.
- PathPlanner API breaking changes (rare, but real) require library
  updates.
- PathPlanner's Command-Based runtime must be bridged via
  `CommandBridge` — a layer that the library must maintain.

## Alternatives Considered

- **Build our own path follower.** Rejected — significant effort for
  a solved problem.
- **Choreo (community alternative to PathPlanner).** Deferred — newer,
  less battle-tested; could be revisited in a future offseason.
- **Open-loop path following.** Rejected — too imprecise for modern
  FRC autonomous scoring.

## References

- [SDD-auto.md](../sdd/SDD-auto.md)
- [SDD-vendor-ctre.md §CommandBridge](../sdd/SDD-vendor-ctre.md)
- [SCMP.md §4 Vendordep Management](../SCMP.md)
- [ADR-005](ADR-005-composition-over-commands.md)
- PathPlanner docs: <https://pathplanner.dev/home.html>
