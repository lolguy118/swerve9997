# SDD: `com.team271.lib.vendor.ctre` — CTRE Phoenix 6 Implementations

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-VENDOR-CTRE |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | CTRE-001 through CTRE-NNN (SRS §4.2) |

## 1. Purpose

Implements the `api/` vendor-neutral interfaces using CTRE Phoenix 6
devices. Provides `CTREMotor` (the primary CTRE motor wrapper) and
corresponding sensor wrappers. Exposes raw CTRE types via passthrough
getters (ADR-003). Also covers `bridge/CommandBridge` for WPILib interop.

## 2. Scope and Boundaries

**This SDD covers:**

- `CTREMotor` — implements `ClosedLoopMotor`; wraps CTRE `ControllerTalonFX`
- `CTREEncoder` — implements `Encoder`
- `CTREAbsoluteEncoder` — implements `AbsoluteEncoder`
- `CTREGyro` — implements `Gyro` (Pigeon2)
- `CTRELimitSwitch` — implements `LimitSwitch` (CANdi, digital input)
- `CTRERangeSensor` — implements `RangeSensor` (CANrange)
- Passthrough getter reference (all `getUnderlying*()` methods)
- Phoenix 6 feature coverage matrix
- `bridge/CommandBridge` — wraps `CTREMotor` as a WPILib `MotorController`

**This SDD does not cover:**

- api/ interfaces → [SDD-api.md](SDD-api.md)
- Hardware lifecycle wrappers → [SDD-hardware.md](SDD-hardware.md)

## 3. Module Decomposition

### 3.1 `CTREMotor`

Implements `api/motor/ClosedLoopMotor` by wrapping a
`hardware/controllers/ControllerTalonFX` (or `ControllerTalonFXS`)
internally. Portable code depends on the `ClosedLoopMotor` interface
and receives a `CTREMotor` instance through `TransmissionFX.getCTRELeader()`.
`CTREMotor` is the single bridge point between vendor-neutral control
code and CTRE Phoenix 6; the `Controller*` classes do not implement
`api/` interfaces directly — only `CTREMotor` does.

CTRE-only features (Motion Magic, Dynamic Motion Magic, torque current
FOC, timesync, direct `TalonFXConfiguration` access) are exposed as
methods on `CTREMotor` itself, above the `ClosedLoopMotor` interface.
Callers that need these features cast (or hold directly) a `CTREMotor`
reference.

### 3.2 CTRE Sensor Wrappers

| Class | Implements | Wraps | Registers With CTREManager |
| ----- | ---------- | ----- | -------------------------- |
| `CTREEncoder` | `Encoder` | `CANcoder` (relative read) | Position, velocity |
| `CTREAbsoluteEncoder` | `AbsoluteEncoder` | `CANcoder` (absolute read) | Absolute position |
| `CTREGyro` | `Gyro` | `Pigeon2` | Yaw (latency-compensated), pitch, roll, yaw rate |
| `CTRELimitSwitch` | `LimitSwitch` | CANdi input or digital input | Triggered state signal |
| `CTRERangeSensor` | `RangeSensor` | `CANrange` | Distance |

Each sensor wrapper manages its own signal registration during
`robotInit()` and delegates cached-value reads to the
`CTREManager.refreshAll()` pipeline (see [SDD-hardware.md §CTREManager](SDD-hardware.md)).
All sensor wrappers expose a passthrough getter returning the raw CTRE
device — see §6 below.

### 3.3 `bridge/CommandBridge`

Bi-directional adapter between Team271's lifecycle model and WPILib's
command-based framework.

- `CommandBridge.asWPISubsystem(Lifecycle, name)` — wraps a Team271
  `Lifecycle` (typically a `Subsystem`) as a WPILib `SubsystemBase` so
  it can appear as a requirement in a WPILib `Command`.
- `CommandBridge.asAutoMove(Command, timeoutSec)` — wraps a WPILib
  `Command` (e.g., a PathPlanner follow command) as a Team271
  `AutoMove` with a **mandatory** timeout argument per ADR-011.
- `CommandBridge.asCommand(AutoMode)` — wraps a Team271 `AutoMode` as
  a WPILib `Command` for integration with existing command-based
  tooling.

Only three adapter functions are provided; the bridge is deliberately
narrow.

## 4. Data Flow

```text
// Control path — portable caller through CTREMotor
subsystem.setOutputPosition(mechanismRot, slot, ffVolts)
  → TransmissionFX.setOutputPosition()
    → unscale mechanismRot → rotorRot using EncoderAdapter
    → CTREMotor.setOutputPosition(rotorRot, slot, ffVolts)       // api/ interface
      → ControllerTalonFX.setOutputPosition()
        → talonFX.setControl(PositionVoltage)

// Signal refresh — CTRE-managed
HardwareManager.refreshAll()
  → CTREManager.refreshAll()
    → BaseStatusSignal.refreshAll(allSignalsArray)
      → every sensor wrapper's cached values updated atomically
    → sensor.getPosition() / gyro.getYaw() returns the cached value

// CommandBridge — PathPlanner into AutoMove
robot project
  → pathPlannerFollow = AutoBuilder.followPath(...)             // WPILib Command
  → CommandBridge.asAutoMove(pathPlannerFollow, kPathTimeoutSec)
  → AutoMode.addMove(autoMove)
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| CTRE as sole vendor | See ADR-006 | [ADR-006](../adr/ADR-006-ctre-phoenix6-primary-vendor.md) |
| Passthrough getters | See ADR-003 | [ADR-003](../adr/ADR-003-passthrough-wrapper-not-wall.md) |
| `CTREMotor` is the api/ bridge (not `Controller*`) | Keeps controller hierarchy free of vendor-neutral obligations | [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md) |
| CommandBridge for WPILib interop | Allows PathPlanner + other WPILib APIs without exposing CTRE types | [SDD-auto.md](SDD-auto.md) |
| Mandatory timeout on `asAutoMove` | Physical safety for wrapped WPILib commands | [ADR-011](../adr/ADR-011-mandatory-timeouts-fail-safe.md) |

## 6. Passthrough Getter Reference

The table below is the authoritative passthrough getter inventory
for the `vendor/ctre/` layer. Each wrapper exposes at least one
`getUnderlying*()`-style getter per [ADR-003](../adr/ADR-003-passthrough-wrapper-not-wall.md).

| Class | Method | Returns | Notes |
| ----- | ------ | ------- | ----- |
| `CTREMotor` | `getTalonFX()` | `com.ctre.phoenix6.hardware.TalonFX` | Raw CTRE motor controller |
| `CTREMotor` | `getConfig()` | `TalonFXConfiguration` | Deferred config — modify fields then `applyConfig()` |
| `CTREMotor` | `getSimState()` | `TalonFXSimState` | Simulation proxy |
| `CTREEncoder` | `getCANcoder()` | `com.ctre.phoenix6.hardware.CANcoder` | Raw CANcoder (relative reads) |
| `CTREAbsoluteEncoder` | `getCANcoder()` | `com.ctre.phoenix6.hardware.CANcoder` | Raw CANcoder (absolute reads) |
| `CTREAbsoluteEncoder` | `getConfig()` | `CANcoderConfiguration` | Deferred config object |
| `CTREAbsoluteEncoder` | `getSimState()` | `CANcoderSimState` | Simulation proxy |
| `CTREGyro` | `getPigeon2()` | `com.ctre.phoenix6.hardware.Pigeon2` | Raw Pigeon2 IMU |
| `CTREGyro` | `getConfig()` | `Pigeon2Configuration` | Deferred config object |
| `CTREGyro` | `getSimState()` | `Pigeon2SimState` | Simulation proxy |
| `CTRERangeSensor` | `getCANrange()` | `com.ctre.phoenix6.hardware.CANrange` | Raw CANrange sensor |
| `CTRERangeSensor` | `getConfig()` | `CANrangeConfiguration` | Deferred config object |

Every new vendor wrapper added to `vendor/ctre/` must provide a
passthrough getter returning the raw CTRE type (see
[.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md)).
Returning a wrapped type from a passthrough getter defeats the
purpose.

## 7. Phoenix 6 Feature Coverage Matrix

The matrix below is the authoritative summary of which Phoenix 6
features are accessible through each of the three access paths:
the vendor-neutral `api/ClosedLoopMotor` interface, the
`CTREMotor` convenience API, and the raw CTRE types reached via
`getTalonFX()` passthrough.

| Feature | `api/ClosedLoopMotor` | `CTREMotor` method | Raw CTRE (`getTalonFX()`) |
| ------- | --------------------- | ------------------ | -------------------------- |
| Duty cycle / voltage output | Yes | Yes | Yes |
| Position closed-loop (voltage) | Yes (`setOutputPosition`) | Yes | Yes |
| Velocity closed-loop (voltage) | Yes (`setOutputVelocity`) | Yes | Yes |
| Position DutyCycle / TorqueCurrent | No | Via TransmissionFX | Yes |
| Motion Magic Position | No | `setOutputMMPositionVoltage` | Yes |
| Motion Magic Velocity | No | `setOutputMMVelocityVoltage` | Yes |
| Dynamic Motion Magic | No | `setOutputDynMMPosition*` | Yes |
| Motion Magic Expo | No | `setOutputMMExpoPosition*` | Yes |
| Torque Current FOC | No | `setOutputTorqueCurrent` | Yes |
| Current / voltage limits | Yes | Yes | Yes |
| PID gains (P/I/D/V/S) | Yes (`setGains(slot, PIDGains)`) | Yes | Yes |
| Gravity kG feedforward | No | No (planned) | Yes (via `Slot0Configs.kG`) |
| Continuous wrap | Yes (`setContinuousWrap`) | Yes | Yes |
| Software limit switches | No | No | Yes |
| Audio / beep configuration | No | No | Yes |
| Orchestra / music | No | No | Yes (via passthrough) |

> **Status: Planned — Not Yet Implemented.**

`CTREMotor` does not yet expose a method for kG gravity
feedforward. The Phoenix 6 `Slot0Configs.kG` field is reachable via
`getConfig()` passthrough (per [ADR-003](../adr/ADR-003-passthrough-wrapper-not-wall.md)).
Adding a first-class method to `CTREMotor` is deferred until
multiple callers need it.

## 8. Error Handling

Phoenix 6 operations return a `StatusCode`. The CTRE wrappers handle
errors according to the library's fault-tolerance patterns:

- **Signal refresh errors** — `CTREManager.refreshAll()` returns the
  aggregate `StatusCode`. Non-OK codes route to a throttled Elastic
  notification (one per 2 seconds) so persistent errors are visible
  without flooding the driver station.
- **Config apply errors** — `ControllerTalonFX.applyConfig()` retries
  up to `CAN_CONFIG_APPLY_RETRIES` times. Persistent failure sets
  `isConfigured = false` and the motor runs with Phoenix 6 factory
  defaults (which include safe current limits).
- **Device disconnect** — `ControllerTalonFX.robotPeriodicBefore()`
  updates the `isConnected` flag every cycle; output methods guard on
  this flag so control frames are not sent to disconnected devices.
- **Fault signals** — each device's `FaultMonitor` tracks sticky
  faults and raises persistent `Alert`s in the "Faults" group. See
  [SDD-hardware.md §3.7 FaultMonitor](SDD-hardware.md).
- **Input validation** — `TransmissionFX.hasInvalidInput()` rejects
  NaN/Infinity setpoints before they reach `CTREMotor`, logging a
  throttled DriverStation warning.

`CommandBridge.asAutoMove` surfaces WPILib command exceptions through
the `AutoMove` lifecycle's standard fail-safe path (timeout fires,
WARNING notification sent).

## 9. Test Coverage Requirements

| Area | HAL Required | CTREManager Reset | Notes |
| ---- | ------------ | ----------------- | ----- |
| `CTREMotor` construction | Yes | Yes | `HAL.initialize(500, 0)` + `CTREManager.resetForTesting()` |
| Closed-loop API delegation | Yes | Yes | Verify setpoint unscale / passthrough without live hardware |
| Passthrough getters | Yes | Yes | Verify `getTalonFX()`, `getConfig()`, `getSimState()` non-null |
| Sensor wrapper signal registration | Yes | Yes | Verify signals registered via `addSignalTalonFX` / `addSignalCANCoder` / etc. |
| `CommandBridge` | Yes | Yes | Verify round-trip lifecycle wrapping without deadlock |

Every test that creates a CTRE device must call
`CTREManager.resetForTesting()` in `@BeforeEach` (preferred) or use
the reflection teardown pattern documented in
[SVP.md §Test Levels](../SVP.md#3-test-levels-library-specific-notes).

Test IDs: TEST-CTRE-NNN. Use unique CAN IDs across tests within a
class to prevent device collisions.
