# Architecture Decision Records

This directory contains Architecture Decision Records (ADRs) for Team271-Lib.
Each ADR captures a key architectural decision, its context, rationale, and
consequences.

ADRs are permanent records — once `Accepted`, they are not deleted. If a
decision is reversed, the original ADR is marked `Superseded` and a new ADR
is written.

## Index

| ADR | Title | Status |
| --- | ----- | ------ |
| [ADR-001](ADR-001-team271-lib-standalone-library.md) | Team271-Lib as a Standalone Library, Separate from Robot Projects | Accepted |
| [ADR-002](ADR-002-java17-wpilib-gradlerio-toolchain.md) | Java 17 + WPILib + GradleRIO Toolchain | Accepted |
| [ADR-003](ADR-003-passthrough-wrapper-not-wall.md) | Passthrough — Wrapper, Not Wall | Accepted |
| [ADR-004](ADR-004-layered-architecture.md) | Layered Architecture: api ← vendor/ctre ← hardware ← control ← subsystem ← auto | Accepted |
| [ADR-005](ADR-005-composition-over-commands.md) | Composition over Commands: State-Machine + AutoMove | Accepted |
| [ADR-006](ADR-006-ctre-phoenix6-primary-vendor.md) | CTRE Phoenix 6 as the Primary Motor/Sensor Vendor | Accepted |
| [ADR-007](ADR-007-centralized-can-refresh.md) | Centralized Bulk CAN Refresh via CTREManager / HardwareManager | Accepted |
| [ADR-008](ADR-008-logged-nt-input-backed-tuning.md) | LoggedNTInput-Backed Tuning: No Magic Numbers in Docs | Accepted |
| [ADR-009](ADR-009-junit5-hal-simulation-tests.md) | JUnit 5 + JaCoCo + HAL Simulation for Library Tests | Accepted |
| [ADR-010](ADR-010-subsystem-exception-isolation.md) | Per-Subsystem Exception Isolation in SubsystemManager.forEachSafe() | Accepted |
| [ADR-011](ADR-011-mandatory-timeouts-fail-safe.md) | Mandatory Timeouts with Fail-Safe + Driver Alert on All Waiting Operations | Accepted |
| [ADR-012](ADR-012-advantagekit-logging.md) | AdvantageKit for Telemetry and Replay Logging | Accepted |
| [ADR-013](ADR-013-pathplanner-autonomous.md) | PathPlanner for Autonomous Path Following | Accepted |
| [ADR-014](ADR-014-desired-to-actual-state-pattern.md) | Desired-to-Actual State Pattern in Subsystems | Accepted |
| [ADR-015](ADR-015-explicit-instantiation-no-singletons.md) | Explicit Object Instantiation, No Singletons in Library Code | Accepted |

## ADR Template

```markdown
# ADR-NNN: Short Decision Title

## Status

[Proposed | Accepted | Deprecated | Superseded by ADR-XXX]

## Date

YYYY-MM-DD

## Context

What is the problem or situation? What forces are at play?

## Decision

What was decided? One clear statement.

## Rationale

Why was this decision made? What evidence or constraints guided it?

## Consequences

What becomes easier? What becomes harder or constrained?

## Alternatives Considered

What other options were evaluated and why were they rejected?

## References

Links to SCS sections, relevant docs, or related ADRs.
```
