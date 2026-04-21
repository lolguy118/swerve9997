# ADR-010: Desired-to-Actual State Pattern in Subsystems

## Status

Accepted

## Date

2026-04-20

## Context

A common beginner mistake in robot code is to act on operator input
directly:

```java
// ANTI-PATTERN
if (driver.getAButton()) {
    armMotor.set(0.5);
}
```

This couples three concerns — input, intent, and actuation — into one
statement. The consequences show up later: input jitter causes motor
commands to chatter; safe-state behavior (e.g., "go to IDLE when
disabled") must be duplicated in every if-branch; code review cannot
easily tell whether a given motor command is safe.

Separating these concerns is a pattern used widely in aerospace and
automotive control software.

## Decision

Every subsystem in Team271-Lib separates **desired state** from
**actual state**:

- **Desired state** — the enum or struct value representing what the
  robot has been *asked* to do. Set by callers (operator commands,
  autonomous moves, higher-level controllers).
- **Actual state** — the value derived from sensors in
  `robotPeriodicBefore()`. Read-only to callers.
- **Action** — `robotPeriodicAfter()` compares desired and actual and
  issues the motor commands that reconcile them.

Operator input is never applied directly to hardware. Buttons and
axes update `desiredState`; hardware is commanded in
`robotPeriodicAfter()`.

## Rationale

1. **Safety state is one place.** "What should this subsystem do when
   disabled?" is a single method on desired state, not scattered
   across every caller.
2. **Jitter filtering is centralized.** `robotPeriodicAfter()` can
   debounce, rate-limit, or clamp commands in one location.
3. **Auto and teleop share the mechanism.** An autonomous move sets
   the same `desiredState` a driver button would; there is no special
   "auto code" that bypasses safety.
4. **Testable.** `desiredState` is a pure data value; unit tests can
   set it and assert on `robotPeriodicAfter()`'s output without
   real hardware.
5. **State-machine clarity.** Enums enforce exhaustive switch handling
   — a new state added to the enum surfaces as a compile error in
   every place that switched on it.

## Consequences

**Easier:**

- One place to implement safe-state behavior per subsystem.
- Auto and teleop are symmetric — they both set `desiredState`.
- Subsystem unit tests don't need real hardware.

**Harder:**

- One extra indirection layer compared to "act on input directly."
  Students new to the pattern need it explained.
- The `robotPeriodicAfter()` method grows as state-machine complexity
  grows; splitting it requires care.

## Alternatives Considered

- **Act on input directly.** Rejected — coupling is the problem this
  ADR solves.
- **Command-Based subsystems with default commands.**
  Rejected — see [ADR-013](ADR-013-composition-over-commands.md).

## References

- [SDD-subsystem.md §3.1 Subsystem](../sdd/SDD-subsystem.md#31-subsystem)
- [ADR-013](ADR-013-composition-over-commands.md)
- [ADR-011](ADR-011-subsystem-exception-isolation.md)
- [ADR-012](ADR-012-mandatory-timeouts-fail-safe.md)
