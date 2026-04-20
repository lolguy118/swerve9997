# SDD: `com.team271.lib.api` — Vendor-Neutral Interfaces

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-API |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | API-001 through API-NNN (SRS §4.1) |

## 1. Purpose

Defines the vendor-neutral interfaces that allow library control code,
subsystem code, and tests to be written without depending on CTRE-specific
types. All motor and sensor abstractions that need to be portable depend
on this layer.

## 2. Scope and Boundaries

**This SDD covers:**

- `Motor` — basic motor control (duty cycle, voltage, stop)
- `ClosedLoopMotor` — extends Motor with position/velocity closed-loop
- `Encoder` — relative position/velocity
- `AbsoluteEncoder` — absolute position with zero offset
- `Gyro` — rotation rate and angle
- `LimitSwitch` — binary sensor
- `RangeSensor` — distance measurement
- `MotorCapabilities` — feature query interface
- `SignalRefreshable` — signal refresh contract

**This SDD does not cover:**

- CTRE implementations of these interfaces → [SDD-vendor-ctre.md](SDD-vendor-ctre.md)
- Hardware wrappers that use these interfaces → [SDD-hardware.md](SDD-hardware.md)

## 3. Module Decomposition

| Package | Interface | Responsibility |
| ------- | --------- | -------------- |
| `api/` | `DeviceID` | Vendor-neutral device identity (bus name + device number) |
| `api/` | `SignalRefreshable` | Per-cycle refresh contract; implementations hook into `HardwareManager.refreshAll()` |
| `api/motor/` | `Motor` | Open-loop control: `setDutyCycle`, `setVoltage`, `stop`, neutral mode, direction |
| `api/motor/` | `ClosedLoopMotor` | Extends `Motor` with closed-loop position and velocity, PID gain setting, current and voltage limits, continuous wrap, slot-based gains |
| `api/motor/` | `MotorCapabilities` | Runtime feature query (`supportsMotionMagic`, `supportsFOC`, etc.) so portable code can branch safely |
| `api/motor/` | `NeutralMode` | Portable enum for brake / coast / unknown |
| `api/motor/` | `FollowStatus` | Portable enum for follower-link health |
| `api/sensor/` | `Encoder` | Relative rotor-space position and velocity |
| `api/sensor/` | `AbsoluteEncoder` | Absolute position plus magnet offset / zero-point |
| `api/sensor/` | `Gyro` | Yaw / pitch / roll angle and rate |
| `api/sensor/` | `LimitSwitch` | Binary triggered state with configurable NO/NC |
| `api/sensor/` | `RangeSensor` | Time-of-flight / ultrasonic distance reading |

The `api/` package has **zero vendor imports**. A class in `api/` that
imports `com.ctre.phoenix6.*` or `edu.wpi.first.hal.*` is a layering
violation — see [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md).

## 4. Data Flow

Interfaces have no intrinsic data flow; they describe contracts that
implementations fulfill. The canonical flow through a `ClosedLoopMotor`
implementation is:

```text
// Portable call site
ClosedLoopMotor motor = transmission.getCTRELeader();  // typed as interface
motor.setOutputPosition(rotations, slot, ffVolts);
  → [implementation] CTREMotor delegates to ControllerTalonFX
    → talonFX.setControl(PositionVoltage)

// Per-cycle refresh
HardwareManager.refreshAll()
  → CTREManager.refreshAll()
    → refreshes all registered StatusSignals
    → SignalRefreshable implementations update their cached values
```

`MotorCapabilities` is consulted once per call site, typically at
construction time, so runtime behavior does not branch on capability
queries inside periodic code.

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| CTRE-focused — interfaces scoped to CTRE feature set | See ADR-006 | [ADR-006](../adr/ADR-006-ctre-phoenix6-primary-vendor.md) |
| Passthrough getters on implementations, not on interfaces | See ADR-003 | [ADR-003](../adr/ADR-003-passthrough-wrapper-not-wall.md) |
| CTRE-only features (Motion Magic, FOC, torque current) excluded from api/ | Keeps api/ portable; CTRE-only features live on `CTREMotor` passthrough | [vendor-abstraction-guide.md](SDD-vendor-ctre.md) |

## 6. Error Handling

The `api/` interfaces define no exception contract. Implementations
choose their own failure semantics and typically follow the library's
standard fault-tolerance patterns:

- No checked exceptions on any interface method.
- Closed-loop methods silently no-op when the underlying device is
  disconnected (see [SDD-hardware.md §CAN Fault Handling](SDD-hardware.md)
  and [fault-tolerance.md](SDD-subsystem.md)).
- Callers should not assume any particular failure mode; portable code
  that needs to detect failure should use `MotorCapabilities` at
  construction and avoid runtime branches.

Input validation (NaN / Infinity guards) is the implementation's
responsibility. `CTREMotor` inherits the `TransmissionFX`
`hasInvalidInput()` guard — see
[fault-tolerance.md §Input Validation](SDD-subsystem.md#input-validation-naninfinity-guards).

## 7. Platform Portability Notes

The `api/` package contains no platform-specific code. Its interfaces
compile and run identically on the RoboRIO and on desktop
simulation.

A future WPILib-native implementation (or a REV SparkMax
implementation) could satisfy these interfaces without changing any
consumer above this layer — that is the point of the abstraction.
Per [hardware-abstraction rule](../../../../.claude/rules/team271-lib.md),
new vendor implementations are built only when a concrete need
arises, not on speculation.

## 8. Configuration

No configuration is exposed at the interface level. Implementations
define their own configuration surfaces (`applyConfig`, `setGains`,
`setCurrentLimit`) and document them in the corresponding vendor SDD.
`ClosedLoopMotor.setGains(slot, gains)` and
`ClosedLoopMotor.setCurrentLimit(config)` are the portable touch
points; CTRE-specific configuration happens through the
`CTREMotor.getUnderlyingTalonFX()` passthrough.

## 9. Test Coverage Requirements

The `api/` package is pure interfaces — it has no executable code and
no direct test coverage. Coverage of the interface contracts is
achieved via implementation tests in
[SDD-vendor-ctre.md](SDD-vendor-ctre.md) and
[SDD-hardware.md](SDD-hardware.md).

Test IDs: TEST-API-NNN. Any new interface added to `api/` requires a
paired implementation test that verifies the behavior contract
through the `CTREMotor` (or equivalent) wrapper.
