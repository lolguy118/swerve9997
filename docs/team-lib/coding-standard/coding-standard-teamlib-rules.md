<!-- markdownlint-disable MD013 -->

# Team271-Lib Coding Rules (`CODE-LIB-*`)

Rules that apply to **library source code** under
`src/main/java/com/team271/lib/...`. These rules extend the
[common Team 271 Java Coding Standard](../../common/coding-standard/Team271-Software-Coding-Standard.md)
with Team271-Lib-specific architectural patterns — most of which are
load-bearing for the library's public contract and are therefore
enforced rather than recommended.

> **Consumers of Team271-Lib (robot projects):** these rules apply
> to library code only. For library-consumer guidance, see
> [`../../robot-yyyy/`](../../robot-yyyy/)
> (robot-project code templates and project-level coding standard) and
> [`coding-standard-library-notes.md`](coding-standard-library-notes.md)
> (common-rule → library-API bindings).

## Rule Precedence

When writing library code, rules apply in the following order of
specificity (most specific wins):

1. **This document** — `CODE-LIB-NNN`
2. **Common** — `CODE-GEN-*`, `CODE-VAR-*`, `CODE-FMT-*`, etc.

Library code does **not** inherit from the robot-project templates
in [`../../robot-yyyy/`](../../robot-yyyy/) — those
patterns (singleton, Globals, etc.) are explicitly for consuming
projects. Library code uses the inverse patterns described below.

---

## CODE-LIB-001 -- Explicit Instantiation

a. Library subsystems, hardware wrappers, and other reusable classes
   **shall not** use the singleton pattern. Instances are created
   via constructors and passed through dependency injection (usually
   a `TObj argParent` parameter).

b. Library classes **shall not** expose a `getInstance()` accessor.
   Consumers obtain library instances by constructing them directly
   (or by accessing them through their own project's `Globals.java`
   after construction).

**Anchor:** [ADR-004](../planning/adr/ADR-004-explicit-instantiation-no-singletons.md).

**Why this is here:** the inverse rule — "robot-project subsystems
**shall** use the singleton pattern" — used to live in common as
CODE-GEN-013. It moved to
[`../../robot-yyyy/subsystem-template.md`](../../robot-yyyy/subsystem-template.md);
the library-side inversion belongs here.

---

## CODE-LIB-002 -- Centralized CAN Refresh

a. Library code **shall not** invoke `refresh()` on individual CTRE
   signals from per-subsystem code. All CTRE signal refreshes go
   through `CTREManager.refreshAll()`, invoked once per cycle by
   `HardwareManager`.

b. New library hardware classes **shall** register their signals
   with `CTREManager` at construction time, not refresh them
   ad-hoc.

**Anchor:** [ADR-009](../planning/adr/ADR-009-centralized-can-refresh.md).

**Related binding:** [library-notes CODE-SAF-* "centralized CAN refresh"](coding-standard-library-notes.md#code-saf----safety-practices).

---

## CODE-LIB-003 -- Desired-to-Actual State Pattern

a. Library subsystems **shall** separate *desired* state (set by
   callers and mode-specific periodic methods) from *actual* state
   (applied to hardware). Fields follow the `mDesiredX` / `mX`
   naming pair or equivalent.

b. Motor outputs, actuator commands, and any other
   externally-visible effects **shall** be issued in
   `robotPeriodicAfter()`, never in `teleopPeriodic()`,
   `autonomousPeriodic()`, or setter methods invoked from outside.

c. Sensor reads **shall** happen in `robotPeriodicBefore()`, so that
   all periodic logic sees a consistent snapshot.

**Anchor:** [ADR-010](../planning/adr/ADR-010-desired-to-actual-state-pattern.md).

---

## CODE-LIB-004 -- Subsystem Exception Isolation

a. Library subsystem periodic methods **shall not** let exceptions
   propagate to the robot loop. `SubsystemManager.forEachSafe()`
   catches and reports; library subsystem code **shall not** require
   per-method try-catch wrappers for this purpose.

b. When library code catches an exception for reporting rather than
   recovery, it **shall** use the reporting mechanisms in
   [library-notes CODE-GEN-011](coding-standard-library-notes.md#code-gen-011--exception-handling)
   (e.g., `DriverStation.reportError` plus optional
   `Elastic.sendNotification`), not `System.err` or silent swallow.

**Anchor:** [ADR-011](../planning/adr/ADR-011-subsystem-exception-isolation.md).

**Why this is here:** common `CODE-GEN-011(a)` was generalized from
"`DriverStation.reportError()`" to "the project's error-reporting
facility" during this refactor; the library-specific mechanism lives
in library-notes, and the library-code-writing rule lives here.

---

## CODE-LIB-005 -- Mandatory Timeouts with Fail-Safe

Every library waiting operation — homing sequences, launcher spin-up
waits, sensor-gated state transitions, PathPlanner follow-to-waypoint
— **shall**:

a. Have a named timeout constant (no magic numbers) declared in the
   subsystem's constants class.

b. On timeout, transition to a fail-safe state: stop motors, restore
   default current limits, transition to `IDLE` (or equivalent safe
   state for the subsystem).

c. On timeout, notify the driver via the library's driver-
   notification facility (`Elastic.sendNotification(WARNING, …)`).
   Silent timeouts are prohibited.

d. Document the timeout and its fail-safe behavior in the
   subsystem's SDD.

**Anchor:** [ADR-012](../planning/adr/ADR-012-mandatory-timeouts-fail-safe.md).

**Enforced by:** the `Elastic` driver-notification binding in
[library-notes CODE-BUG-002](coding-standard-library-notes.md#code-bug-002--dashboard-notifications),
and the safety guardrails in
[`.claude/rules/safety.md`](../../../.claude/rules/safety.md).

---

## CODE-LIB-006 -- Subsystem Lifecycle Contract

a. Subsystem lifecycle methods **shall** be called in this order by
   `SubsystemManager`:

   ```text
   robotInit(argTimestamp)
   → robotPeriodicBefore(argTimestamp)    [read sensors]
   → <mode>Periodic(argTimestamp)         [state machine logic]
   → robotPeriodicAfter(argTimestamp)     [apply motor outputs]
   → outputTelemetry()                   [publish to NT/logs — no parameter]
   ```

b. Motor outputs **shall** only be commanded in `robotPeriodicAfter()`,
   never in `teleopPeriodic()` or `autonomousPeriodic()`. The mode-
   specific periodic methods set *desired* state; `robotPeriodicAfter()`
   *applies* it.

c. Sensor reading **shall** be done in `robotPeriodicBefore()`, not in
   the mode-specific periodic methods, so that all periodic logic
   within a cycle sees a consistent sensor snapshot.

**Anchor:** [ADR-010](../planning/adr/ADR-010-desired-to-actual-state-pattern.md)
and [SDD-subsystem.md](../planning/sdd/SDD-subsystem.md).

---

## CODE-LIB-007 -- Library Package and Constants Layout

a. Library source **shall** reside under `com.team271.lib` and its
   subpackages. Robot-project code **shall not** be placed in
   `com.team271.lib*`.

b. Library-wide constants **shall** be defined in
   [`ConstantsLib.java`](../../../src/main/java/com/team271/lib/ConstantsLib.java)
   at the root of `com.team271.lib` and **shall** follow the common
   [CODE-MAF-003](../../common/coding-standard/Team271-Software-Coding-Standard-Modules.md#code-maf-003----constants-organization)
   discipline (shared location for cross-class constants,
   `private` constructors on static-only holders).

c. Library code **shall not** import the consuming project's
   `Constants` class. Values needed at construction time are
   supplied by the robot project through constructor parameters or
   setter calls.

**Related binding:** [library-notes CODE-BUG-004 "the project's `Constants` class"](coding-standard-library-notes.md#code-bug-004--runtime-tunability).
