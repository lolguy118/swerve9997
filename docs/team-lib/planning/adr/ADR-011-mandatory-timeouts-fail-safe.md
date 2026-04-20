# ADR-011: Mandatory Timeouts with Fail-Safe + Driver Alert on All Waiting Operations

## Status

Accepted

## Date

2026-04-20

## Context

Real robots have waiting operations: homing a mechanism against a
limit switch, spinning a launcher up to target velocity, waiting for
a coral to clear an intake, following a path to a waypoint. Each of
these has a condition that *usually* becomes true quickly. When it
does not — because the limit switch is broken, the launcher is jammed,
the coral is stuck, the path is blocked — an unbounded wait will hang
the robot.

Hangs during a match are worse than crashes. A crashed subsystem at
least lets the driver fall back to other systems. A hang of the
scheduler means the whole robot stops responding.

## Decision

Every operation that waits on a condition has three mandatory elements:

1. **A named timeout constant** in the subsystem's `Constants` class
   (no magic numbers).
2. **A fail-safe action on timeout:** stop motors, restore default
   current limits, transition to `IDLE`.
3. **A driver alert** via `Elastic.sendNotification()` on timeout.

Unbounded `while` loops on sensor conditions are forbidden.
`AutoMoveConditional` enforces this at the type level: it cannot be
constructed without a timeout.

This rule is codified in the coding standard as `CODE-SAF-008` through
`CODE-SAF-011`. A subsystem design doc must document its timeouts
and fail-safe behavior.

## Rationale

1. **No hang is acceptable.** A robot that hangs mid-match forfeits
   all remaining scoring opportunities.
2. **Fail-safe, not fail-fast.** An arm that times out its homing
   sequence should stop with default current limits, not fall back
   to open-loop; open-loop on a misaligned mechanism is worse than
   no motion.
3. **Named constants.** Timeouts get tuned; magic numbers prevent
   tuning and make code reviews harder.
4. **Driver alert.** The driver needs to know a subsystem failed so
   they can avoid requesting actions that depend on it.
5. **Type-level enforcement.** `AutoMoveConditional`'s constructor
   signature makes the timeout non-optional — a reviewer cannot
   accidentally approve an auto move without one.

## Consequences

**Easier:**

- Robot never hangs on a stuck mechanism; worst case is degraded
  function with driver awareness.
- Auto routines continue past a failed conditional move rather than
  blocking indefinitely.
- Code review can check for this rule mechanically (named constants
  in Constants, Elastic call on timeout path).

**Harder:**

- Choosing the right timeout value requires thought per mechanism —
  too short causes spurious timeouts on cold motors, too long causes
  real problems to take long to surface.
- Every subsystem design doc must include a timeouts section; the
  drift hook flags changes to timeout handling.

## Alternatives Considered

- **Timeout as a guideline, not a rule.** Rejected — guidelines decay
  under schedule pressure; rules enforced at the type system do not.
- **Global timeout wrapper.** Rejected — different mechanisms need
  different timeouts (homing takes 2 s, launcher spin-up takes 3 s);
  globals are always wrong somewhere.

## References

- [Team271-Software-Coding-Standard-Safety.md](../../Team271-Software-Coding-Standard-Safety.md)
  (CODE-SAF-008 through CODE-SAF-011)
- [SDD-auto.md §AutoMoveConditional](../sdd/SDD-auto.md)
- [SDD-subsystem.md §Homing](../sdd/SDD-subsystem.md)
- [.claude/rules/safety.md](../../../../.claude/rules/safety.md)
- [ADR-010](ADR-010-subsystem-exception-isolation.md)
