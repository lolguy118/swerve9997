# Rule: Hardware Abstraction Layering

Team271-Lib has three layers for motor/sensor hardware. Do not collapse
them.

1. **`api/`** — vendor-neutral interfaces
   (`Motor`, `ClosedLoopMotor`, `Encoder`, `Gyro`, `RangeSensor`,
   `LimitSwitch`, `MotorCapabilities`, `SignalRefreshable`).
   Portable code depends on these.
2. **`vendor/<vendor>/`** — vendor implementations
   (currently only `vendor/ctre/CTREMotor` etc.). Implements Layer 1.
3. **`hardware/controllers/`, `hardware/transmissions/`,
   `hardware/sensors/`** — CTRE-facing wrappers that extend `TObj` and
   participate in the lifecycle. `TransmissionFX` wraps `CTREMotor`
   which wraps `ControllerTalonFX` which wraps `TalonFX`.

## Rules

- Library is **CTRE-focused**. No WPILib PWM motors. Do not speculate
  REV/WPILib implementations — build them only when a concrete need
  exists (e.g., a WPILib control loop must receive an api/ sensor).
- When adding a new capability to a motor, decide first whether it
  belongs on the `api/` interface (portable) or the `CTREMotor`
  passthrough (CTRE-only). Motion Magic, FOC, torque current, and
  timesync are CTRE-only. Gains, current limits, continuous wrap,
  basic closed-loop go on `ClosedLoopMotor`.
- Prefer `HardwareManager.refreshAll()` over `CTREManager.refreshAll()`
  in robot startup — it delegates to `CTREManager` today and is the
  forward-compatible entry point.
- `ControllerBase`/`ControllerSmart`/`ControllerTalonFX(S)` are the
  CTRE-facing wrapper hierarchy. They do **not** implement the `api/`
  interfaces — `CTREMotor` does. Do not add `api/` interface
  implementations directly to the `Controller*` classes.

## Authoritative doc

[docs/team-lib/vendor-abstraction-guide.md](../../docs/team-lib/architecture/vendor-abstraction-guide.md)
and
[docs/team-lib/hardware-abstraction.md](../../docs/team-lib/architecture/hardware-abstraction.md).
Read both before changing the layering.
