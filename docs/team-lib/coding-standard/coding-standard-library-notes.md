# Coding Standard — Library Applications

Concrete Team271-Lib API mappings for the framework-agnostic rules in
[`../../common/coding-standard/Team271-Software-Coding-Standard*.md`](../../common/). The
common rules describe *what* to do ("use the project's tuning
mechanism"); this document describes *how* to do it with library
classes ("`LoggedNTInput` + `checkTuning()`").

Sections are named after the common rule they concretize. A robot
project that swaps out a library facility (e.g., uses WPILib
`Preferences` instead of `LoggedNTInput`) would author an equivalent
notes file in its own `docs/<project>/`.

---

## CODE-BUG-001 — Telemetry Discipline

| Common concept | Library binding |
| -------------- | --------------- |
| "the project's telemetry publishing method" | `TObj.outputTelemetry()` — called once per robot cycle after `robotPeriodicAfter()` |
| "replay-capable logging facility" | AdvantageKit `Logger.recordOutput(key, value)` — writes to both .wpilog and NT4 |
| Telemetry key helper | `NTEntry` wraps `Logger.recordOutput` for cached publishing |

Implementation lives in
[SDD-nt.md](../planning/sdd/SDD-nt.md) (`NTEntry`) and
[SDD-subsystem.md](../planning/sdd/SDD-subsystem.md)
(`SubsystemManager.outputTelemetry()` driving the per-cycle call).

---

## CODE-BUG-002 — Dashboard Notifications

| Common concept | Library binding |
| -------------- | --------------- |
| "driver-notification facility" | `Elastic.sendNotification(level, title, text)` |
| "display-time argument" | `withDisplayMilliseconds(int)` on the `Notification` builder, or the `displayTimeMillis` constructor parameter |

See [SDD-util.md](../planning/sdd/SDD-util.md) for the `Elastic`
wrapper's API.

---

## CODE-BUG-004 — Runtime Tunability

| Common concept | Library binding |
| -------------- | --------------- |
| "replay-faithful tuning mechanism" | `LoggedNTInput` — wraps an NT subscriber + publisher and records every read to AdvantageKit |
| "per-cycle tuning hook" | `checkTuning()` — convention method called at the top of `outputTelemetry()` |
| "values already tunable through a project-supplied library class" | `PIDBase`, `ControllerSmart`, `TransmissionFX` — their `checkTuning()` is called by the framework; subsystem code does not need to re-expose their gains |
| "the project's `Constants` class" | Robot project `Constants.java`; library-level constants live in `ConstantsLib.java` |

Anchor decisions and pipeline:

- [ADR-015](../planning/adr/ADR-015-logged-nt-input-backed-tuning.md)
  — `LoggedNTInput`-backed tuning decision
- [SDD-nt.md](../planning/sdd/SDD-nt.md)
  — `NTTable`, `NTEntry`, `LoggedNTInput` implementation
- [SDD-team271-lib.md §Tuning Infrastructure](../planning/sdd/SDD-team271-lib.md)
  — lifecycle integration point

Minimum `checkTuning()` shape:

```java
@Override
public void outputTelemetry() {
    checkTuning();          // apply any dashboard edits from the last cycle
    super.outputTelemetry(); // parent publishes cached values
    // publish additional NTEntry values here
}

protected void checkTuning() {
    if (tuneP.hasChanged()) {
        setP(tuneP.getDbl());
    }
    // ... one branch per LoggedNTInput
}
```

---

## CODE-GEN-008 — External Input Validation

| Common concept | Library binding |
| -------------- | --------------- |
| Vendor return-code check | CTRE `StatusCode` returns from `getConfigurator().apply(...)` and signal-refresh operations |

Operator input validation is concretized by each robot project —
the library's `libtest` package (which the init script renames to
the robot's own package during fork initialization) provides a
worked reference example in its `InputDriver`.

---

## CODE-GEN-011 — Exception Handling

| Common concept | Library binding |
| -------------- | --------------- |
| "error-reporting facility" | `DriverStation.reportError()` for log-and-DS reporting; `Elastic.sendNotification(WARNING, ...)` for driver-visible alerts |

Per-subsystem exception isolation is handled by
`SubsystemManager.forEachSafe()` (see the CODE-SAF-* section below
and [ADR-011](../planning/adr/ADR-011-subsystem-exception-isolation.md)).

---

## CODE-SAF-* — Safety Practices

Library bindings for safety rules live alongside their definitions — see
[`../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md`](../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md)
and its cross-references to:

- [ADR-012 — Mandatory Timeouts with Fail-Safe + Driver Alert](../planning/adr/ADR-012-mandatory-timeouts-fail-safe.md)
- [SDD-subsystem.md §Fault Tolerance](../planning/sdd/SDD-subsystem.md)
- [SDD-hardware.md §CAN Fault Handling](../planning/sdd/SDD-hardware.md)

Key bindings:

| Common concept | Library binding |
| -------------- | --------------- |
| "driver alert on timeout" | `Elastic.sendNotification(WARNING, ...)` |
| "fail-safe default output" | `TransmissionFX.stop()` or `ControllerBase.stop()`; restores default current limits |
| "per-subsystem exception isolation" | `SubsystemManager.forEachSafe()` |
| "centralized CAN refresh" | `CTREManager.refreshAll()` via `HardwareManager.refreshAll()` |

---

## Adding to this document

When you introduce a library facility that concretizes a common rule,
add a row here rather than embedding the library API in the common
standard. This keeps `docs/common/` portable across projects.
