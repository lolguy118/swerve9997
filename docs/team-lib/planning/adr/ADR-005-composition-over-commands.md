# ADR-005: Composition over Commands — State-Machine + AutoMove Instead of WPILib Command-Based

## Status

Accepted

## Date

2026-04-20

## Context

WPILib provides a Command-Based framework in which behaviors are
expressed as `Command` objects managed by a `CommandScheduler`.
Commands own subsystem reservations, lifecycle callbacks
(`initialize`/`execute`/`end`/`isFinished`), and have built-in
sequence, parallel, and race combinators.

Team 271 has built autonomous routines under both paradigms across
prior seasons. Command-Based excels at simple, well-bounded behaviors
but grows brittle when a move needs to interact with multiple
subsystems, needs to pre-check conditions before acting, or needs
fine-grained control over retry and timeout semantics. Command-Based
interruptions (via subsystem default commands) have historically been
a source of subtle bugs.

## Decision

Team271-Lib uses a custom autonomous composition model for autonomous
routines and a state-machine pattern for subsystems:

- **Subsystems** are built around a `Subsystem` base class that
  separates **desired state** from **actual state**
  (see [ADR-014](ADR-014-desired-to-actual-state-pattern.md)), with
  `robotPeriodicBefore()`/`robotPeriodicAfter()` as lifecycle hooks.
  They are not WPILib `CommandSubsystem`s.
- **Autonomous routines** compose from `AutoMove` (the atomic unit),
  `AutoMoveSequence`, `AutoMoveParallel`, `AutoMoveTimed`, and
  `AutoMoveConditional` (with mandatory timeout). There is no
  `CommandScheduler`.
- **WPILib interop** is available through `bridge/CommandBridge`:
  library code can still consume a PathPlanner `Command` as an
  `AutoMove`, and vice-versa.

## Rationale

1. **Explicit control flow.** Autonomous code is read top-to-bottom in
   one place. There is no implicit scheduler decision about when a
   command gets interrupted.
2. **Mandatory timeout on conditional moves.** Every
   `AutoMoveConditional` takes a timeout in its constructor; there is
   no way to construct one without it
   (see [ADR-011](ADR-011-mandatory-timeouts-fail-safe.md)).
3. **State-machine friendliness.** Subsystems with discrete states
   (idle, homing, running, error) are naturally expressed as enum
   transitions; forcing them into `Command`s adds ceremony without
   clarity.
4. **Simpler exception isolation.**
   [SubsystemManager.forEachSafe()](ADR-010-subsystem-exception-isolation.md)
   wraps every subsystem's periodic call. Command-Based's scheduler has
   its own exception semantics that interact unpredictably with this.
5. **Still interoperable.** `CommandBridge` lets the library consume
   PathPlanner without re-implementing path following.

## Consequences

**Easier:**

- Autonomous routines read as straight-line compositions.
- Timeouts are enforced at the type level (`AutoMoveConditional`
  constructor requires one).
- Subsystem state machines are explicit and testable.

**Harder:**

- Third-party Command-Based libraries (beyond PathPlanner) require a
  bridge if not already supported.
- Contributors must learn both paradigms if they also use WPILib
  examples.
- Loss of the Command-Based convenience combinators (`andThen`,
  `alongWith`, etc.) — but the `AutoMove` containers cover the cases
  we actually use.

## Alternatives Considered

- **Adopt WPILib Command-Based wholesale.** Rejected — prior-season
  experience with subsystem default-command interruption bugs.
- **No composition framework — just raw periodic code.** Rejected —
  autonomous routines need sequencing and parallelism, and raw periodic
  code reimplements them poorly.

## References

- [SDD-auto.md](../sdd/SDD-auto.md)
- [SDD-subsystem.md](../sdd/SDD-subsystem.md)
- [ADR-014](ADR-014-desired-to-actual-state-pattern.md)
- [ADR-013](ADR-013-pathplanner-autonomous.md)
