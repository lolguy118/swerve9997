# SDD: `com.team271.lib.util` — Utilities

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-UTIL |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | UTL-001 through UTL-NNN (SRS §4.9) |

## 1. Purpose

Provides cross-cutting utilities used by all layers: the Alert system,
Elastic dashboard notifications, `DriveSignal`, `LimelightHelpers`, and
math utilities.

## 2. Scope and Boundaries

**This SDD covers:**

- `Alert` — persistent, typed (ERROR / WARNING / INFO) driver-station alerts;
  grouped and output to AdvantageKit
- `Elastic` — fire-and-forget notifications to the Elastic dashboard
- `DriveSignal` — velocity + rotation drive command data class
- `LimelightHelpers` — wrapper around Limelight JSON API
- `Util` — math helpers (clamp, deadband, interpolation, etc.)

**This SDD does not cover:**

- NT tuning entries → [SDD-nt.md](SDD-nt.md)
- Vision processing beyond Limelight helpers

## 3. Module Decomposition

### 3.1 `Alert`

`Alert` represents a single persistent, categorized driver-station alert.
Each alert has a type (`ERROR`, `WARNING`, or `INFO`), a message, and
an active/inactive state. When `set(true)` is called on an inactive
alert, the alert logs to the DriverStation and sends a matching Elastic
notification at the corresponding severity; `set(false)` clears the
active state silently. Alerts live in named groups (default group:
`"Alerts"`) auto-created via a thread-safe `ConcurrentHashMap`. The
`Alert.outputTelemetry()` static method publishes every group's active
alerts to AdvantageKit as string arrays, keyed by alert type — it is
invoked each cycle by `SubsystemManager` so alerts appear in both the
dashboard and the match log. `alert.remove()` deregisters an alert
from its group.

### 3.2 `Elastic`

`Elastic` provides fire-and-forget notifications to the Elastic
Dashboard. `Elastic.sendNotification(Notification)` serializes a
notification to JSON via Jackson and publishes it to
`/Elastic/RobotNotifications`. The `Notification` class supports a
fluent builder (`withLevel`, `withTitle`, `withDescription`,
`withDisplaySeconds`, `withWidth`, `withHeight`) and three severity
levels (`INFO`, `WARNING`, `ERROR`). `Elastic.selectTab(name)` and
`Elastic.selectTab(index)` switch the visible tab. Unlike `Alert`,
Elastic has no persistent state — each call produces a transient
popup.

### 3.3 `DriveSignal`

Immutable data class carrying differential-drive motor outputs: left
and right duty cycle (clamped to [-1.0, 1.0] at construction) plus a
brake-mode flag. Exposes `getLeft()`, `getRight()`, `getBrakeMode()`,
and `normalize()` (scales outputs so the larger magnitude equals 1.0).
Static constants `NEUTRAL` and `BRAKE` provide common defaults. The
class is final with `private final` fields — it cannot be mutated
after construction.

### 3.4 `LimelightHelpers`

Static helper class wrapping the Limelight NetworkTables JSON API.
Provides accessors for targeting validity (`getTV`), horizontal and
vertical offset (`getTX`, `getTY`), area, robot pose in 2D and 3D,
and Megatag pose estimation. When the camera is disconnected, the
helpers return safe defaults (zero or empty `Pose2d`) rather than
throwing.

`LimelightHelpers` is the raw vendor surface consumed by
`vendor/limelight/LimelightCamera` — see
[SDD-vision.md](SDD-vision.md). It plays the same role for
Limelight that `TalonFX` plays for CTRE (ADR-003 passthrough).
Robot projects may call it directly when they need a Limelight
feature `Camera` does not express.

### 3.5 `Util`

Static math utility class with no instance state. Provides:

- Comparison/range: `epsilonEquals`, `inRange`, `allCloseTo`
- Clamping/mapping: `limit`, `interpolate`, `reMap`
- Joystick shaping: `handleDeadzone` (1D), `handleDeadzone_Radial` (2D),
  `convertTrigger`
- Miscellaneous: `getMACAddress` (first non-loopback MAC)

The `kEpsilon` constant is used as the default tolerance for
floating-point comparisons.

## 4. Data Flow

```text
// Alert lifecycle
subsystem.detectFault() → alert.set(true)
  → DriverStation.reportWarning / Error
  → Elastic.sendNotification(matching level)
  → [next cycle] SubsystemManager.outputTelemetry()
    → Alert.outputTelemetry() [static]
      → Logger.recordOutput("Alerts/{group}/active{Type}", String[])

// Elastic notification
subsystem.onHomingTimeout() → Elastic.sendNotification(WARNING, ...)
  → Jackson serialize
  → NetworkTables publish "/Elastic/RobotNotifications"
  → dashboard renders popup

// DriveSignal (immutable pipeline)
teleopPeriodic → new DriveSignal(left, right) → drivetrain.applyDriveSignal()

// Util (pure functions, no side effects)
joystick.getRawAxis() → Util.handleDeadzone() → Util.limit() → motor output
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Alert separate from Elastic | Persistent vs. transient; different dashboard roles | SRS §4.9 |
| Alert groups output to AdvantageKit | Alerts appear in match log for post-match review | [ADR-012](../adr/ADR-012-advantagekit-logging.md) |
| DriveSignal immutable | Prevents accidental mutation between read and apply | SRS §4.9 |
| Util as static-only | Pure functions with no state — safe to call from any thread | SRS §4.9 |

## 6. Error Handling

`Util` methods are stateless pure functions — no exceptions are thrown,
and out-of-range inputs are clamped rather than rejected. `DriveSignal`
clamps left/right values at construction, so a caller cannot inject
out-of-range duty cycles into downstream code.

`LimelightHelpers` returns zero-valued defaults (e.g., `new Pose2d()`,
`0.0`) when the camera's NetworkTables keys are absent; callers must
check validity via `getTV()` before trusting pose data.

`Elastic.sendNotification` reports Jackson serialization errors to the
DriverStation but never throws, so a malformed notification cannot
crash the calling subsystem. `Alert.set(true)` routes through
`Elastic.sendNotification`, so it shares the same fail-quiet behavior.
Throttling of repeated notifications is the caller's responsibility
(see [SDD-subsystem.md §6.2](SDD-subsystem.md)).

## 7. Platform Portability Notes

All utility classes are platform-neutral pure Java except where noted:

- `Alert` and `Elastic` publish via NetworkTables, which works
  identically in simulation and on the RoboRIO.
- `LimelightHelpers` reads from NetworkTables — in simulation, values
  default to zero until a sim harness publishes them.
- `Util.getMACAddress` uses `NetworkInterface` and returns a MAC from
  either the RoboRIO or the desktop host; callers that rely on a
  specific MAC for robot identification must handle both cases.

## 8. Configuration

The utility package has no persistent configuration. Elastic
notifications accept optional `displaySeconds`, `width`, and `height`
values on a per-call basis via the `Notification` builder; defaults
are defined as constants inside `Elastic.Notification`. Alert groups
are created on first use by name — no explicit registration is
required.

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| `Util` math helpers | No | Pure functions; test via direct calls |
| `DriveSignal` | No | Immutable data class; verify clamping and `normalize()` |
| `Alert` | Yes (NT4) | Requires `HAL.initialize(500, 0)` for NT publishing |
| `Elastic` | Yes (NT4) | Requires HAL for NT publishing; test JSON serialization |
| `LimelightHelpers` | Yes (NT4) | NT backed; publish values in test fixture |

Test IDs: TEST-UTL-NNN. The `libtest` package is excluded from
coverage metrics per
[SVP.md §Coverage Targets](../SVP.md).
