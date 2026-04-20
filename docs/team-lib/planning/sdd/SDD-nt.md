# SDD: `com.team271.lib.nt` — NetworkTables Tuning Infrastructure

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-NT |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | NT-001 through NT-NNN (SRS §4.8) |

## 1. Purpose

Provides the `LoggedNTInput` pattern that backs all live-tunable parameters
(PID gains, motion constraints, thresholds) with NetworkTables entries that
are simultaneously logged to AdvantageKit for replay. This is the
implementation of ADR-008.

## 2. Scope and Boundaries

**This SDD covers:**

- `NTTable` — wrapper around a NetworkTable topic
- `NTEntry<T>` — typed NT entry with default and get/set
- `LoggedNTInput` — combines `NTEntry` with `Logger.recordOutput()` so
  every tunable read is also captured in the match log

**This SDD does not cover:**

- General telemetry output (use `Logger.recordOutput()` directly)
- Dashboard layout (Elastic / Shuffleboard concern)

## 3. Module Decomposition

### 3.1 `NTTable`

Hierarchical namespace container wrapping a WPILib `NetworkTable`.
Each `TObj` creates a child `NTTable` under its parent, producing
automatic NT path hierarchy such as `/Drivetrain/LeftTransmission/Leader`.
Provides `getEntry(key, default)` for typed subscription/publication
and a `getSubTable(name)` helper for further nesting. `NTTable` has
no change detection — it is a namespace only.

### 3.2 `NTEntry`

Typed NT entry for **output-only** telemetry. Supports `boolean`,
`double`, `long`, `int`, and `String` types. The `publish(value)`
method sets the NT topic value and routes through AdvantageKit
`Logger.recordOutput()` so the value appears in the match log as well
as on the live dashboard. `NTEntry` has no change detection — callers
that read an entry receive the most recently published value.

### 3.3 `LoggedNTInput`

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

Every `get*()` call also records the value through AdvantageKit so
replays reproduce the exact tunable value used on each cycle.
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
[library-architecture.md §checkTuning Pattern](SDD-team271-lib.md#the-checktuning-pattern)
for the canonical implementation template.

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Log every tunable read | Replay requires knowing tunable values at time of use | [ADR-012](../adr/ADR-012-advantagekit-logging.md) |
| Default values required | Robot runs safely without dashboard connected | [ADR-011](../adr/ADR-011-mandatory-timeouts-fail-safe.md) |
| Dashboard tunables are inputs, not outputs | Separating input/output enables replay faithfulness | [ADR-008](../adr/ADR-008-logged-nt-input-backed-tuning.md) |
| No numeric values in docs | Tunable names in docs, values in code | [ADR-008](../adr/ADR-008-logged-nt-input-backed-tuning.md) |

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
[library-architecture.md §Where Tunables Live](SDD-team271-lib.md#where-tunables-live)
for the full inventory.

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| `NTTable` subtable nesting | Yes (NT4) | `HAL.initialize(500, 0)` required |
| `NTEntry` publish round-trip | Yes (NT4) | Verify value round-trips through NT |
| `LoggedNTInput.hasChanged()` | Yes (NT4) | Publish via a separate subscriber to simulate dashboard edit |
| `LoggedNTInput` null-safety | No | Test with null table; verify no NPE |

Test IDs: TEST-NT-NNN. Reset of NT between tests is generally not
required — HAL teardown handles it — but tests that share table
names should use unique keys to avoid cross-contamination (see
[testing-strategy.md §CAN ID Uniqueness](../SVP.md#can-id-uniqueness)
for the equivalent pattern).
