# ADR-010: Per-Subsystem Exception Isolation in SubsystemManager.forEachSafe()

## Status

Accepted

## Date

2026-04-20

## Context

A typical robot has 5–10 subsystems, each with a periodic method that
reads sensors, runs state-machine logic, and commands motors. Any one
of these can throw an exception — null pointer from a misconfigured
constant, arithmetic error, or an unrecovered CAN failure.

Without isolation, an exception in one subsystem's periodic propagates
up through the periodic dispatcher and kills the entire robot loop.
In a match, this means losing drivetrain control because the intake
threw an NPE.

## Decision

`SubsystemManager.forEachSafe()` wraps every subsystem lifecycle call
(`robotPeriodicBefore`, `robotPeriodicAfter`, `outputTelemetry`) in a
try-catch. On exception:

1. The error is logged to the Driver Station console.
2. A throttled Elastic notification alerts the driver (per-subsystem
   throttle window is a named constant in `ConstantsLib`).
3. The exception is swallowed. Other subsystems continue to run.

`robotInit()` is the one exception to isolation: a failure during init
is fatal because the robot cannot safely operate with a partially
initialized subsystem.

## Rationale

1. **Containment.** One broken subsystem does not cascade into loss
   of drivetrain, leaving the robot uncontrollable.
2. **Driver visibility.** Throttled alerts keep the driver informed
   without flooding the dashboard.
3. **Match recovery.** During an FRC match, there is no time to
   diagnose and recover a broken subsystem — the priority is
   preserving function in the rest of the robot.
4. **Init failure is fatal on purpose.** A partially-initialized
   subsystem is worse than a crashed robot: it will silently misbehave
   in unknown ways. Forcing init failure to crash ensures the team
   debugs it on the bench, not the field.

## Consequences

**Easier:**

- A single broken subsystem does not take the robot down.
- Driver always has drivetrain control, even in the face of software
  failures elsewhere.
- Logs capture each exception with the phase name (before/after/
  telemetry) for debugging.

**Harder:**

- Exceptions are not surfaced loudly — a student may not realize a
  subsystem is broken because the robot keeps running. Mitigation:
  Elastic notifications + persistent `Alert.ERROR` state.
- Unit tests must use `assertThrows` to verify exceptions because the
  subsystem manager would otherwise swallow them.

## Alternatives Considered

- **Let exceptions crash the robot.** Rejected — loss of drivetrain
  control in a match is worse than degraded intake function.
- **Catch exceptions per-method within the subsystem.** Rejected —
  every subsystem author would have to remember to do this, and most
  would forget; centralizing in `SubsystemManager` makes it a library
  invariant.

## References

- [SDD-subsystem.md §6.1 Exception Isolation](../sdd/SDD-subsystem.md#61-exception-isolation)
- [ADR-011](ADR-011-mandatory-timeouts-fail-safe.md)
