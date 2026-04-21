# SDD: `com.team271.lib.nt` — NetworkTables Tuning Infrastructure

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-NT |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | `[NT-001]`..`[NT-003]` (SRS §4.8) |

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

## 1. Purpose

Provides the `LoggedNTInput` pattern that backs all live-tunable parameters
(PID gains, motion constraints, thresholds) with NetworkTables entries that
are simultaneously logged to AdvantageKit for replay. This is the
implementation of ADR-015.

## 2. Scope and Boundaries

This SDD covers:

- `NTTable` — wrapper around a NetworkTable topic
- `NTEntry<T>` — typed NT entry with default and get/set
- `LoggedNTInput` — combines `NTEntry` with AdvantageKit recording so
  every tunable read is also captured in the match log

## 3. Module Decomposition

### 3.1 Telemetry, Logging, and Dashboard Landscape

The library emits observable data to several sinks via primitives
that live across `nt/`, `util/`, and `hardware/`. This subsection
is the navigation hub; overlap rules live here so reviewers do not
invent new paths.

**Sinks:**

| Sink | Nature | Readers |
| ---- | ------ | ------- |
| NetworkTables 4 | Live key/value, bidirectional | Elastic, Shuffleboard, operator tools during a match |
| AdvantageKit `.wpilog` | Time-series log; on-robot write, offline replay | AdvantageScope, WPILib SysID tool, post-match analysis |
| Elastic notifications | Transient UI popups (INFO / WARNING / ERROR) | Elastic dashboard during a match |
| Driver Station console | Text messages from `DriverStation.report*` | Driver Station app's console |
| CTRE SignalLogger `.hoot` | Raw Phoenix 6 CANivore signals | AdvantageScope (via Phoenix 6 tooling) |

**Library primitives:**

| Primitive | Owning SDD | Writes to |
| --------- | ---------- | --------- |
| `NTEntry` | this SDD (§3.3) | NT **and** AdvantageKit (dual sink) |
| `LoggedNTInput` | this SDD (§3.4) | reads NT; records to AdvantageKit on every read |
| `NTTable` | this SDD (§3.2) | namespace only; no value writes |
| `Alert` | [SDD-util.md](SDD-util.md) | AdvantageKit (active state per cycle); Elastic + Driver Station on activation |
| `Elastic.sendNotification` | [SDD-util.md](SDD-util.md) | Elastic UI only (transient; not replayable) |
| CTRE `SignalLogger` | [SDD-hardware.md §3.5](SDD-hardware.md) | `.hoot` files on CANivore buses |
| AdvantageKit `Logger.recordOutput` | external (AdvantageKit) | AdvantageKit only (no NT) |

**Decision matrix — "I need to emit this data; which primitive?":**

| Intent | Use |
| ------ | --- |
| Telemetry value visible live **and** captured for replay | `NTEntry` (dual sink) |
| Tunable the operator can adjust from the dashboard | `LoggedNTInput` |
| Log-only metric (too noisy for NT, no operator value) | AdvantageKit `Logger.recordOutput` directly |
| Persistent state flag surviving across cycles (e.g., disconnected device) | `Alert` |
| One-shot operator notification (timeout, command failure) | `Elastic.sendNotification` |
| Raw Phoenix 6 signal capture for post-match analysis | Let CTRE `SignalLogger` handle it (wired by `CTREManager`) |

Two primitives write to the same AdvantageKit log — `NTEntry` (as a
side effect of its NT publish) and direct `Logger.recordOutput`
calls. Prefer `NTEntry` whenever the value is also useful on a
live dashboard; fall back to direct AdvantageKit recording only
when NT bandwidth would be wasted on a reader-less topic.

### 3.2 `NTTable`

Hierarchical namespace container wrapping a WPILib `NetworkTable`.
Each `TObj` creates a child `NTTable` under its parent, producing
automatic NT path hierarchy such as `/Drivetrain/LeftTransmission/Leader`.
Provides typed entry construction (key + default) and a sub-table
helper for further nesting. `NTTable` has no change detection — it
is a namespace only.

### 3.3 `NTEntry`

Typed NT entry for **output-only** telemetry. Supports `boolean`,
`double`, `long`, `int`, and `String` types. Publishing a value sets
the NT topic and simultaneously records the value through
AdvantageKit so it appears in the match log as well as on the live
dashboard. `NTEntry` has no change detection — callers that read an
entry receive the most recently published value.

### 3.4 `LoggedNTInput`

Dashboard-tunable parameter with built-in change detection and
AdvantageKit logging. Each instance owns both an NT publisher (to push
the default value to the dashboard at startup) and an NT subscriber
(to read operator changes). Type-specific accessors pair with change
detection:

| Type | Check | Get |
| ---- | ----- | --- |
| double | `hasChanged()` | `getDbl()` |
| boolean | `hasBoolChanged()` | `getBool()` |
| long | `hasLongChanged()` | `getLong()` |
| String | `hasStringChanged()` | `getString()` |

Every read also records the value through AdvantageKit so replays
reproduce the exact tunable value used on each cycle.
`LoggedNTInput` is null-safe: if the parent `NTTable` is null the
input returns cached defaults and change detection always returns
false, so unit tests can exercise consumers without a live NT
connection.

## 4. Data Flow

```text
// Initialization
robotInit()
  → subsystem constructor creates NTTable (child of parent)
  → creates LoggedNTInput(table, "Tune kP", defaultKp)
    → publisher writes default to NT (dashboard sees value)
    → subscriber registers for dashboard edits

// Periodic cycle
SubsystemManager.outputTelemetry()
  → subsystem.outputTelemetry()
    → subsystem.checkTuning()
      → if (tuneP.hasChanged()) setP(tuneP.getDbl())
        → getDbl() also calls Logger.recordOutput(key, value)
    → super.outputTelemetry() [publishes current state via NTEntry]
```

The `checkTuning()` pattern runs once per cycle as part of the
standard `outputTelemetry()` order. See
[SDD-team271-lib.md §3.5 Tuning Infrastructure](SDD-team271-lib.md)
for the canonical implementation template.

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Log every tunable read | Replay requires knowing tunable values at time of use | [ADR-016](../adr/ADR-016-advantagekit-logging.md) |
| Default values required | Robot runs safely without dashboard connected | [ADR-012](../adr/ADR-012-mandatory-timeouts-fail-safe.md) |
| Dashboard tunables are inputs, not outputs | Separating input/output enables replay faithfulness | [ADR-015](../adr/ADR-015-logged-nt-input-backed-tuning.md) |
| No numeric values in docs | Tunable names in docs, values in code | [ADR-015](../adr/ADR-015-logged-nt-input-backed-tuning.md) |

## 6. Error Handling

`LoggedNTInput` is explicitly null-safe: if the parent table is null
(as in unit tests without an NT context), the entry returns the
cached default and `hasChanged()` always returns false. No exceptions
are thrown from `get*()` or `has*Changed()`.

If the underlying NT4 connection drops mid-match, the subscriber
continues to return the last known value; it does not revert to the
default. `NTEntry.publish()` is fire-and-forget — NT publication
failures are swallowed by WPILib and do not propagate. Callers that
need stronger delivery guarantees should use `Elastic` notifications
(see [SDD-util.md §Elastic](SDD-util.md)) or write directly through
AdvantageKit `Logger`.

## 7. Platform Portability Notes

NetworkTables 4 is fully available in desktop simulation through the
WPILib HAL simulation backend. `LoggedNTInput` behavior is identical
on the RoboRIO and on desktop — the same tunable names can be
adjusted from a dashboard connected to `localhost` during
`./gradlew simulateJava`.

AdvantageKit logging requires no platform-specific code from
`LoggedNTInput`; on desktop, logs are written to the WPILib data log
directory configured by the robot project.

## 8. Configuration

- **Table prefix** is set at `NTTable` construction time (usually by
  a parent `TObj`).
- **Default values** are required arguments to every `LoggedNTInput`
  constructor — the library refuses to publish a tunable without a
  compiled-in safe default.
- **No runtime reconfiguration** is supported; a tunable's key, type,
  and default are fixed at construction.

Tunable key conventions (`Tune/kP`, `Tune/StatorLimit`, etc.) live in
the component that owns the tunable — `PIDBase`, `ControllerSmart`,
`TransmissionFX`, and `Balance`. See
[SDD-team271-lib.md §3.5 Tuning Infrastructure](SDD-team271-lib.md)
for the full inventory.

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| `NTTable` subtable nesting | Yes (NT4) | `HAL.initialize(500, 0)` required |
| `NTEntry` publish round-trip | Yes (NT4) | Verify value round-trips through NT |
| `LoggedNTInput.hasChanged()` | Yes (NT4) | Publish via a separate subscriber to simulate dashboard edit |
| `LoggedNTInput` null-safety | No | Test with null table; verify no NPE |

Test IDs: `[TEST-NT-NNN]`. Reset of NT between tests is generally not
required — HAL teardown handles it — but tests that share table
names should use unique keys to avoid cross-contamination (same
pattern as unique CAN IDs per test; see
[SVP.md §5 Per-Layer Test Requirements](../SVP.md)).
