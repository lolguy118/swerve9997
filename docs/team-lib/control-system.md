<!-- markdownlint-disable MD013 MD060 -->
# Control System Design

> **Scope:** This document covers the library's PID control hierarchy
> and Balance algorithm. Robot-specific PID tuning values, mechanism
> constraints, and feedforward characterization belong in each robot
> project's subsystem design docs.

---

## PID Hierarchy

```text
Interfaces (for swapping implementations):
  PIDController                 (calculate, atSetpoint, gains, continuous input, reset)
  └── ProfiledPIDController     (setGoal, setConstraints, atGoal, setpoint position/velocity)

Implementation hierarchy:
  TObj
  └── PIDBase  implements PIDController
      ├── PIDSimple             Software PID — basic proportional-integral-derivative
      ├── PIDTrap               implements ProfiledPIDController — custom trapezoidal profile
      ├── PIDWPI                Wrapper around WPILib PIDController
      ├── PIDWPI_Trap           implements ProfiledPIDController — WPILib profiled PID
      └── PIDFX                 TalonFX onboard PID (configurable slot, ContinuousWrap support)

Feedforward (composable with any PID):
  Feedforward                   @FunctionalInterface — simple(), elevator(), fromWPILib()
  ArmFeedforward                Position-dependent: kS*sign(v) + kG*cos(pos) + kV*v + kA*a
```

All five PID implementations satisfy the `PIDController` interface,
allowing code to swap between them without changing calling code:

```java
// Swap by changing the constructor — the rest stays the same
PIDController pid = new PIDSimple(parent, "Arm", 1.0, 0.0, 0.05, 0.02);
// or: PIDController pid = new PIDFX(parent, "Arm", talonFX, 1.0, 0.0, 0.05, 0.02);

double output = pid.calculate(measurement, setpoint, timestamp);
```

---

## PIDBase — Abstract Foundation

All PID implementations share a common base that provides:

### Error Tracking

- `prevError`, `totalError` — for derivative and integral terms
- `posError`, `velError` — separate position/velocity error tracking
- `outputP`, `outputI`, `outputD`, `output` — individual term contributions

### Output Clamping

- `minOutput`, `maxOutput` — clamp final output
- `iMin`, `iMax` (via `setIntegratorRange`) — clamp integral accumulation
- `iZone` — zero the integral when error exceeds this threshold

### Continuous Mode

For mechanisms with circular motion (e.g., turrets, swerve modules),
continuous mode wraps the error calculation:

```java
pid.enableContinuousInput(0.0, 1.0);  // wraps at rotation boundary
```

This ensures the controller takes the shortest path across the
wrap point instead of going the long way around.

### Tolerance & Deadband

- `posTolerance`, `velTolerance` — `atSetpoint()` returns true when
  both position and velocity errors are within tolerance
- `pDeadband` — proportional deadband; errors smaller than this
  produce zero P output

### Live Tuning

PIDBase follows the standard `checkTuning()` pattern documented in
[Library Architecture — Tuning Infrastructure](library-architecture.md#tuning-infrastructure).

**PIDBase tunable inventory:**

| LoggedNTInput | NT Key | Default | Applies To |
|---------------|--------|---------|------------|
| `tuneP` | `Tune P` | Constructor arg | `pidSlot.kP` |
| `tuneI` | `Tune I` | Constructor arg | `pidSlot.kI` |
| `tuneD` | `Tune D` | Constructor arg | `pidSlot.kD` |
| `tunePosTol` | `Tune Pos Tol` | Constructor arg | `pidSlot.posTolerance` |
| `tunePDeadband` | `Tune P Deadband` | 0.0 | `pidSlot.pDeadband` |
| `tuneIZone` | `Tune I Zone` | +Infinity | `pidSlot.iZone` |
| `tuneOutputMin` | `Tune Output Min` | -1.0 | `minOutput` |
| `tuneOutputMax` | `Tune Output Max` | 1.0 | `maxOutput` |

For `PIDFX`, `setP()`, `setI()`, `setD()` additionally sync gains
to the TalonFX hardware via `ControllerTalonFX.setPSlot()`.

**PIDBase telemetry output** (read-only NTEntry fields, published
every cycle after `checkTuning()`):

- P/I/D contributions: `outputP`, `outputI`, `outputD`, `output`
- Error tracking: `prevError`, `totalError`, `posError`, `velError`
- State: `lastInputMeasurement`, `lastTimestamp`, `atSetpoint`

### Telemetry Levels

PIDBase supports configurable telemetry levels to reduce NT overhead
when multiple PIDs are active:

| Level | NTEntry Fields | Tuning Inputs | Use Case |
|-------|---------------|---------------|----------|
| `FULL` | All 20+ entries | All 8 tunables | Development and tuning (default) |
| `MINIMAL` | `output`, `posError`, `velError`, `atSetpoint` | None | Competition — reduced NT traffic |
| `OFF` | None | None | Embedded PIDs where telemetry is managed by parent |

Set via constructor: `new PIDSimple(parent, "Arm", P, I, D, tol, TelemetryLevel.MINIMAL)`

Default is `FULL` for backward compatibility. Competition code should
use `MINIMAL` for subsystem PIDs that don't need live tuning.

### Accessing WPILib Controllers

PIDWPI and PIDWPI_Trap expose their underlying WPILib controllers via
`getController()` for features the library doesn't wrap:

```java
// Access WPILib PIDController directly
pidwpi.getController().enableContinuousInput(-Math.PI, Math.PI);
pidwpi.getController().setIZone(5.0);

// Access WPILib ProfiledPIDController directly
pidwpiTrap.getController().setTolerance(0.1, 0.5);
```

See [Passthrough Design](passthrough-design.md) for the full reference.

---

## PID Type Selection Guide

| Type | Runs On | Profile | Best For |
|------|---------|---------|----------|
| `PIDSimple` | RoboRIO (50 Hz) | None | Simple software loops, prototyping |
| `PIDTrap` | RoboRIO (50 Hz) | Trapezoidal | Smooth position control with velocity/acceleration limits |
| `PIDWPI` | RoboRIO (50 Hz) | None | When you want WPILib's tested PID math |
| `PIDWPI_Trap` | RoboRIO (50 Hz) | Trapezoidal | WPILib profiled PID with motion constraints |
| `PIDFX` | TalonFX (1 kHz) | Hardware-dependent | Position/velocity control where 1 kHz loop rate matters |

### Decision Tree

1. **Is the mechanism driven by a TalonFX?**
   - Yes → **Prefer `PIDFX`** — runs at 1 kHz on the motor controller,
     lower latency, less CAN traffic
   - No → Use a software PID variant

2. **Do you need motion profiling (velocity/acceleration limits)?**
   - Yes → `PIDTrap` or `PIDWPI_Trap`
   - No → `PIDSimple` or `PIDWPI`

3. **TalonFX with Motion Magic?**
   - Use `TransmissionFX.setOutputMMPosition*()` directly — this uses
     the TalonFX's onboard Motion Magic, not a PIDBase subclass

### When NOT to Use PIDBase

TransmissionFX provides direct access to TalonFX control modes:
- `setOutputPosition()` — hardware position PID
- `setOutputVelocity()` — hardware velocity PID
- `setOutputMMPosition*()` — Motion Magic with profiling
- `setOutputDynMMPosition*()` — Dynamic Motion Magic

These bypass PIDBase entirely and run at 1 kHz on the motor. Use
PIDBase only when you need software-side control logic (e.g.,
combining sensor inputs, custom error calculations, or controlling
non-TalonFX mechanisms).

---

## Unit Conventions for Velocity Parameters

The `argRPS` parameter name appears in multiple TransmissionFX methods
but refers to different unit spaces depending on context:

| Method Family | Parameter | Unit Space | Library Converts? |
|--------------|-----------|-----------|-------------------|
| `setOutputVelocity*()` | `argRPS` | Mechanism output units/sec | Yes — divides by gear ratio chain |
| `setOutputPosition*()` | `argPositionRot` | Mechanism output rotations | Yes — divides by gear ratio chain |
| `setMMConfig()` | `argCruiseVelRPS` | Rotor rotations/sec (raw) | No — sent directly to TalonFX |
| `setMMConfig()` | `argCruiseAccelRPSS` | Rotor rotations/sec² (raw) | No — sent directly to TalonFX |

**Key distinction:** Velocity and position control methods accept values
in the mechanism's output unit system (after gear ratios and
`mechanismToUnits`). The library unscales them internally. Motion Magic
configuration methods accept raw rotor values because they configure
the TalonFX hardware directly.

See the gear ratio conversion chain in
[Hardware Abstraction — Gear Ratio Conversion](hardware-abstraction.md#gear-ratio-conversion-chain).

---

## PID Implementations

### PIDSimple

Basic PID with no motion profiling. Calculates P, I, D terms directly
from the error between measurement and setpoint. Stores the setpoint
for telemetry.

### PIDTrap

Integrates WPILib's `TrapezoidProfile` with PIDBase. Instead of
jumping to the setpoint, it generates a smooth trajectory with
configurable max velocity and max acceleration.

```java
PIDTrap pid = new PIDTrap(parent, "Arm", P, I, D, tolerance,
    maxVelRPS, maxAccelRPSS);

pid.setGoal(targetPosition);
double output = pid.calc(currentPosition, timestamp);
```

The profile is recalculated from the current state each cycle.
Supports continuous mode for rotational mechanisms.

### PIDWPI

Thin wrapper around `edu.wpi.first.math.controller.PIDController`.
All calculations delegate to the WPILib controller. Synchronized
setters ensure PIDBase and WPILib controller stay in sync.

### PIDWPI_Trap

Wraps `edu.wpi.first.math.controller.ProfiledPIDController`. Combines
WPILib's PID math with trapezoidal motion profiling. Provides access
to the profiled setpoint state (position and velocity).

### PIDFX

Reads closed-loop error and output from a `ControllerTalonFX` rather
than calculating them in software. The actual PID runs at 1 kHz on
the TalonFX hardware.

```java
PIDFX pid = new PIDFX(parent, "Elevator", controller, P, I, D, tolerance);

// Sends position command to hardware
pid.setGoal(targetPosition);
// or with feedforward:
pid.setGoal(targetPosition, feedForwardVolts);

// Reads hardware state for telemetry
double output = pid.calc(measurement, setpoint, timestamp);
```

`setP()`, `setI()`, `setD()` sync gains to the TalonFX hardware
via `ControllerTalonFX.setPSlot()`.

---

## PIDSlot Configuration

PIDBase uses a `PIDSlot` inner class to store:

| Field | Default | Purpose |
|-------|---------|---------|
| `kP` | 0.0 | Proportional gain |
| `kI` | 0.0 | Integral gain |
| `kD` | 0.0 | Derivative gain |
| `posTolerance` | 0.0 | Position error tolerance |
| `velTolerance` | Double.MAX | Velocity error tolerance |
| `pDeadband` | 0.0 | Proportional deadband |
| `iZone` | 0.0 | Integral zone (0 = no zone) |
| `iMin` | -1.0 | Min integral accumulation |
| `iMax` | 1.0 | Max integral accumulation |

For TalonFX hardware PID, gains are configured via
`ControllerSmart.setPIDFSlot(slot, P, I, D, V, S)` which maps to
Phoenix 6's Slot0/Slot1/Slot2 configuration. Note that the hardware
slots support kV and kS (velocity and static feedforward) through
`setPIDFSlot()`, but the software `PIDSlot` inner class stores only
kP/kI/kD.

---

## Phoenix 6 Advanced PID Features

> **Status: Planned — Not Yet Implemented.**
>
> The following Phoenix 6 v26 PID features are available in the CTRE
> API but not yet exposed through the library's controller or
> transmission layers. See the
> [CTRE Feature Coverage](hardware-abstraction.md#ctre-phoenix-6-feature-coverage)
> matrix for the full list.

### kG Gravity Feedforward

Phoenix 6 v26 supports a `kG` gain in each PID slot configuration
(`Slot0Configs.kG`) along with a `GravityTypeValue` that selects
the compensation model:

- **`Elevator_Static`** — applies a constant feedforward voltage to
  counteract gravity on an elevator (output does not vary with
  position)
- **`Arm_Cosine`** — applies a cosine-scaled feedforward based on
  mechanism position, matching the gravity torque curve of a
  pivoting arm

The library's `setPIDFSlot()` currently accepts P, I, D, V, S but
not G. Without hardware kG support, arm and elevator subsystems must
compute gravity compensation in software and pass it as the
`feedforward` parameter to `setOutputPosition()`, which runs at
50 Hz (robot loop) rather than the TalonFX's 1 kHz PID loop.

**Impact:** Adding kG support would allow gravity compensation to
run at 1 kHz on the motor controller, improving hold stability for
arms and elevators.

### ContinuousWrap

Phoenix 6 provides `ClosedLoopGeneralConfigs.ContinuousWrap` which
enables the TalonFX to wrap error calculation for mechanisms with
continuous rotation (swerve azimuth modules, turrets). When enabled,
the hardware PID automatically takes the shortest path across the
wrap boundary.

The library's software PID classes support continuous mode via
`PIDBase.enableContinuousInput()`, but this does not propagate to
the TalonFX hardware when using `PIDFX` or TransmissionFX closed-loop
modes. The `ContinuousWrap` config field is not currently exposed
through `ControllerTalonFX` or `ControllerSmart`.

**Impact:** Swerve modules and turrets using hardware PID must
currently handle wrap in software or accept suboptimal pathing across
the wrap point.

### Runtime Slot Selection

All control requests in `ControllerTalonFX` and `TransmissionFX`
hardcode `.Slot = 0`. Phoenix 6 supports up to 3 PID slots per
device, allowing different gain profiles to be pre-loaded and
selected at runtime without reconfiguring the device.

Use cases:
- Different position hold gains vs. fast slew gains
- Separate gains for loaded vs. unloaded mechanism states
- Auto vs. teleop gain profiles

Currently, switching gains requires calling `setPIDFSlot()` to
reconfigure Slot 0, which triggers a config write over CAN.

---

## Balance Algorithm

`Balance` implements a state machine for autonomous charge station
balancing. Unlike the PID classes, it does not extend TObj or PIDBase.

### State Machine

| State | Behavior |
|-------|----------|
| 0 | **Approach** — drive at fast speed toward charge station |
| 1 | **Climb** — detected tilt, continue at fast speed |
| 2 | **Level** — tilt reversed, switch to slow speed |
| 3 | **Maintain** — within level threshold, stop with debounce |

### Configuration

| Parameter | Purpose |
|-----------|---------|
| `isFwd` | Direction (forward/reverse variants) |
| `robotSpeedSlow` | Speed during leveling |
| `robotSpeedFast` | Speed during approach/climb |
| `onChargeStationDegree` | Tilt threshold for climb detection |
| `levelDegree` | Tilt threshold for "level" detection |
| `debounceTime` | Stability time before declaring balanced |

**Balance tunable inventory:**

| LoggedNTInput | NT Key | Default | Notes |
|---------------|--------|---------|-------|
| `tuneSpeedSlow` | `Tune Speed Slow` | 0.2 | Leveling speed |
| `tuneSpeedFast` | `Tune Speed Fast` | 0.6 | Approach/climb speed |
| `tuneOnChargeDeg` | `Tune On Charge Deg` | 13.0 | Sign-inverted if reverse |
| `tuneLevelDeg` | `Tune Level Deg` | 6.0 | Sign-inverted if reverse |
| `tuneDebounceTime` | `Tune Debounce Time` | 0.1 | Stability time (seconds) |

Balance's `checkTuning()` applies sign inversion based on `isFwd`:
tilt thresholds are negated for reverse-facing balance routines.

### Usage

```java
Balance balance = new Balance(/* isFwd */ true);
balance.init();  // call once at start

// In periodic:
double speed = balance.autoBalanceRoutineForward(imu.getPitch());
drivetrain.setOutputVoltage(speed);
```

---

## Tuning PID in Simulation

The library's tuning infrastructure works identically in simulation
and on real hardware. This enables a rapid tuning workflow:

1. Run `./gradlew simulateJava`
2. Connect Elastic Dashboard or Shuffleboard to `localhost`
3. Navigate to the mechanism's NT path → `Tune/` subtable
4. Adjust P, I, D gains while watching simulated response
5. Once the sim response looks right, deploy to real robot for fine-tuning
6. Copy final values back to `Constants.java`

For software PID (PIDSimple, PIDTrap, PIDWPI), the full control loop
runs on the RoboRIO — simulation accuracy depends on the physics model.

For hardware PID (PIDFX, TransmissionFX closed-loop), the CTRE firmware
simulation models the TalonFX's 1 kHz PID loop, so sim tuning is a
reasonable starting point for real-hardware tuning.

> **Robot project responsibility:** The library provides tuning
> infrastructure and change detection. Robot projects define initial
> gain values in their `Constants` classes and implement physics models
> in `simulationPeriodic()` for accurate sim-based tuning.
