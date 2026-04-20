# SDD: `com.team271.lib.control` — PID and Control Algorithms

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-CONTROL |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | CTL-001 through CTL-NNN (SRS §4.4) |

## 1. Purpose

Provides the PID controller hierarchy, feedforward algorithms, and the
Balance algorithm. All implementations satisfy the common `PIDController`
interface, enabling hot-swap by changing the constructor call only.

## 2. Scope and Boundaries

**This SDD covers:**

- `PIDBase` — shared features (error tracking, output clamping, continuous
  mode, tolerance, live tuning)
- `PIDSimple` — basic PID (no profiling)
- `PIDTrap` — trapezoidal motion profile
- `PIDWPI` — wraps WPILib `PIDController`
- `PIDWPI_Trap` — wraps WPILib `ProfiledPIDController`
- `PIDFX` — Phoenix 6 on-motor closed-loop
- `HardwarePIDController` — routes to `PIDFX` or software PID transparently
- `PIDGains` — gains data class
- `Feedforward`, `ArmFeedforward`
- `Balance` — tilt-correction algorithm

**This SDD does not cover:**

- PathPlanner trajectory following → [SDD-auto.md](SDD-auto.md)
- CAN-layer signal refresh → [SDD-hardware.md](SDD-hardware.md)

## 3. Module Decomposition

### 3.1 PID Hierarchy

```text
Interfaces (for swapping implementations):
  PIDController                 (calculate, atSetpoint, gains, continuous input, reset)
  └── ProfiledPIDController     (setGoal, setConstraints, atGoal, setpoint pos/vel)

Implementation hierarchy:
  TObj
  └── PIDBase  implements PIDController
      ├── PIDSimple             Software PID — basic P/I/D
      ├── PIDTrap               implements ProfiledPIDController — custom trapezoidal
      ├── PIDWPI                Wrapper around WPILib PIDController
      ├── PIDWPI_Trap           implements ProfiledPIDController — WPILib profiled PID
      └── PIDFX                 TalonFX onboard PID (slot-configurable)

Vendor-neutral hardware-backed PID:
  HardwarePIDController (interface)   extends PIDController
    └── PIDFX                         currently the only implementation
```

| Class | Responsibility |
| ----- | -------------- |
| `PIDBase` | Shared state: P/I/D output terms, error tracking, `minOutput`/`maxOutput` clamp, `iZone`/`iMin`/`iMax` integral guards, `posTolerance`/`velTolerance` for `atSetpoint`, `pDeadband`, continuous-mode wrap, `PIDSlot` inner value class, `LoggedNTInput` tuning wiring, telemetry levels (FULL / MINIMAL / OFF). |
| `PIDSimple` | Software PID without motion profiling. Produces output from (measurement, setpoint, timestamp). Used for simple software loops and prototyping. |
| `PIDTrap` | Integrates WPILib `TrapezoidProfile` with `PIDBase` for smooth position control under velocity/acceleration limits. Implements `ProfiledPIDController`. |
| `PIDWPI` | Thin wrapper around `edu.wpi.first.math.controller.PIDController`. Setters synchronized so PIDBase and WPILib state stay in sync. `getController()` passthrough. |
| `PIDWPI_Trap` | Wraps `ProfiledPIDController`. Exposes profiled setpoint position and velocity. `getController()` passthrough. |
| `PIDFX` | Reads closed-loop error and output from a `ControllerTalonFX`; actual PID runs at 1 kHz on the motor firmware. `setP/I/D` sync hardware gains via `setPSlot()` etc. Implements `HardwarePIDController`. |
| `HardwarePIDController` | Vendor-neutral interface for hardware-backed PID. Adds `setGoalPosition(pos, ff)`, `setGoalVelocity(rps, ff)`, `getMotor()` returning the backing `ClosedLoopMotor`. See [SDD-vendor-ctre.md](SDD-vendor-ctre.md). |
| `PIDGains` | Immutable record of kP, kI, kD, kV, kS, kG, kA. Builder methods for incremental construction. |
| `Feedforward` | `@FunctionalInterface` supplying feedforward voltage given (position, velocity). Factory methods: `simple()`, `elevator()`, `fromWPILib(...)`. Composable with any PID variant. |
| `ArmFeedforward` | Position-dependent feedforward: `kS·sign(v) + kG·cos(pos) + kV·v + kA·a`. |
| `Balance` | State machine for autonomous charge-station balancing. Not a PID — uses four discrete speed states gated by tilt angle thresholds and a debounce timer. Its own `LoggedNTInput` tunables for speeds and angles. |

### 3.2 PID Tunable Inventory

PIDBase creates the following tunables (see §3.1 above for the
PIDBase responsibility summary):

| LoggedNTInput | NT Key | Applies To |
| ------------- | ------ | ---------- |
| `tuneP` / `tuneI` / `tuneD` | `Tune P` / `Tune I` / `Tune D` | `pidSlot.kP` / `kI` / `kD` |
| `tunePosTol` | `Tune Pos Tol` | `pidSlot.posTolerance` |
| `tunePDeadband` | `Tune P Deadband` | `pidSlot.pDeadband` |
| `tuneIZone` | `Tune I Zone` | `pidSlot.iZone` |
| `tuneOutputMin` / `tuneOutputMax` | `Tune Output Min/Max` | `minOutput` / `maxOutput` |

For `PIDFX`, `setP/I/D` additionally call
`ControllerTalonFX.setPSlot()` to sync to hardware.

## 4. Data Flow

```text
// Software PID (PIDSimple / PIDTrap / PIDWPI / PIDWPI_Trap)
subsystem.robotPeriodicAfter(t):
  measurement = sensor.getPosition()
  output = pid.calculate(measurement, setpoint, t)          // P+I+D in software
    → clamp to [minOutput, maxOutput]
    → apply iZone / iMin / iMax guards
  transmission.setOutputVoltage(output + feedforward.apply(...))

// Hardware PID (PIDFX via TransmissionFX)
subsystem.robotPeriodicAfter(t):
  transmission.setOutputPosition(targetMechanismRot, ffVolts)
    → unscale via GearRatio → rotorRot
    → CTREMotor.setOutputPosition(rotorRot, slot, ffVolts)
      → talonFX.setControl(PositionVoltage)                 // 1 kHz firmware loop

// PIDFX telemetry read-back (in outputTelemetry)
pidfx.outputTelemetry():
  output = controllerTalonFX.getClosedLoopOutput()
  error  = controllerTalonFX.getClosedLoopError()
  → publish via NTEntry + AdvantageKit

// Live tuning (every cycle through SubsystemManager.outputTelemetry)
pid.checkTuning():
  if (tuneP.hasChanged()) setP(tuneP.getDbl())
  ... (other tunables)
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| 5 PID variants + swap-by-constructor | Match mechanism needs without API changes | SRS §4.4 |
| Continuous mode for wrapping axes | Prevents wrap-around error spike on swerve / turret | See §8.3 below |
| `PIDFX` uses on-motor closed-loop | Deterministic 1 kHz timing, no CAN round-trip per cycle | [ADR-007](../adr/ADR-007-centralized-can-refresh.md) |
| `HardwarePIDController` interface | Portable abstraction for future non-CTRE hardware PID | [SDD-vendor-ctre.md](SDD-vendor-ctre.md) |
| `Balance` is stateful, not PID | Physical behavior better modeled as discrete speed states with debounce | See §3.1 (Balance row) and §8.4 |
| Telemetry levels (FULL / MINIMAL / OFF) | Reduce NT overhead when many PIDs are active in competition | See §3.1 (PIDBase row) |

## 6. Error Handling

- **Output clamping** — every `PIDBase` subclass clamps its final
  output to `[minOutput, maxOutput]`, preventing runaway values from
  reaching motors.
- **Integral wind-up** — the integral term is clamped to
  `[iMin, iMax]` and zeroed when `|error| > iZone`. These safeguards
  are applied in `PIDBase` and inherited by every subclass.
- **`PIDFX` error propagation** — Phoenix 6 `StatusCode` failures
  reported by the underlying `ControllerTalonFX` are surfaced via the
  standard CAN fault-handling path (throttled Elastic notification,
  `isConnected` guard on output calls). See
  [SDD-subsystem.md](SDD-subsystem.md).
- **`HardwarePIDController` fallback** — when a `PIDFX` init fails,
  the consumer is free to fall back to a software PID variant by
  reconstructing with a different implementation. The library does
  not automate this fallback; the decision is robot-project scoped.
- **Balance tilt read** — if the IMU returns NaN, the input-validation
  guard in `TransmissionFX` blocks the downstream voltage command and
  the robot holds position. `Balance` itself does not validate IMU
  input.
- **Continuous-mode mismatch** — `PIDBase.enableContinuousInput` does
  **not** propagate to the TalonFX hardware when using `PIDFX`. Teams
  that need hardware continuous wrap must use Phoenix 6's
  `ClosedLoopGeneralConfigs.ContinuousWrap` via the CTRE passthrough;
  this is called out in
  [SDD-vendor-ctre.md §Passthrough Getter Reference](SDD-vendor-ctre.md).

## 7. Platform Portability Notes

| Implementation | Runs On | Simulation Behavior |
| -------------- | ------- | ------------------- |
| `PIDSimple` | Any Java runtime | Identical to RoboRIO — pure software |
| `PIDTrap` | Any Java runtime | Identical to RoboRIO — pure software |
| `PIDWPI` | RoboRIO / desktop | Delegates to WPILib; identical in both |
| `PIDWPI_Trap` | RoboRIO / desktop | Delegates to WPILib; identical in both |
| `PIDFX` | TalonFX firmware | Phoenix 6 firmware simulation models the 1 kHz loop; sim tuning is a reasonable starting point for hardware tuning |
| `Balance` | Any Java runtime | Pure software — identical in both |

Tuning workflow: run `./gradlew simulateJava` with Elastic connected
to `localhost`, then adjust `Tune/*` values via the dashboard. For
setup details see
[docs/team-lib/guides/simulation-guide.md](../../guides/simulation-guide.md).

## 8. Configuration

### 8.1 Gain Configuration

`PIDGains` is an immutable record that carries all configurable
coefficients. Gains are applied via:

- **Constructor args** — `new PIDSimple(parent, name, kP, kI, kD, posTolerance)`
  and similar overloads set initial values.
- **`setP/I/D/...` setters** — runtime updates, visible to live tuning.
- **`PIDFX` hardware sync** — `setP/I/D` on `PIDFX` call through to
  `ControllerTalonFX.setPSlot()` etc., applying the gains to Slot 0 on
  the device.
- **Transmission-level** — `TransmissionFX.configPIDFSlot(slot, P, I, D, V, S)`
  configures the Phoenix 6 slot directly (kP–kS).

> **Status: Planned — Not Yet Implemented.** kG (gravity feedforward)
> is reachable today via the CTRE passthrough
> (`getLeaderConfig().Slot0.kG`) but is not yet wired into
> `configPIDFSlot`. A first-class method will be added when multiple
> callers need it.

### 8.2 Output Clamping & Integrator Range

`PIDBase` constructors accept defaults; `setOutputRange(min, max)`
and `setIntegratorRange(iMin, iMax)` override at runtime.
`setIZone(zone)` sets the integral zone.

### 8.3 Continuous Mode

`pid.enableContinuousInput(min, max)` enables wrap-aware error math
for rotational mechanisms. Must be set once at construction time or
during `robotInit()`; flipping continuous mode mid-match is not
supported.

### 8.4 Balance Tunables

`Balance` exposes `tuneSpeedSlow`, `tuneSpeedFast`, `tuneOnChargeDeg`,
`tuneLevelDeg`, `tuneDebounceTime`. Defaults live in the `Balance`
source file; tilt thresholds are sign-inverted when constructed with
`isFwd = false`.

## 9. Test Coverage Requirements

| Area | HAL Required | CTREManager Reset | Notes |
| ---- | ------------ | ----------------- | ----- |
| `PIDSimple` / `PIDTrap` math | No | No | Pure-Java; verify P/I/D terms, wrap math, tolerance |
| `PIDWPI` / `PIDWPI_Trap` delegation | No | No | Verify sync with underlying WPILib controller |
| `PIDFX` | Yes | Yes | Requires `ControllerTalonFX`; verify `setPSlot` wiring |
| `HardwarePIDController` | Yes | Yes | Verify goal-send semantics and `getMotor()` passthrough |
| `Feedforward` / `ArmFeedforward` | No | No | Verify factory methods and math |
| `Balance` state machine | No | No | Drive simulated pitch through states; verify debounce |

Test IDs: TEST-CTL-NNN. The existing test suite covers PIDSimple,
PIDTrap, PIDWPI, PIDFX, and Balance (see
[SVP.md §Test Structure](../SVP.md#test-structure)).
