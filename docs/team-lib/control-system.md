<!-- markdownlint-disable MD013 MD060 -->
# Control System Design

This document describes the PID control hierarchy and Balance algorithm
in Team271-Lib.

---

## PID Hierarchy

```text
PIDBase (abstract — error tracking, telemetry, tuning infrastructure)
├── PIDSimple       Software PID — basic proportional-integral-derivative
├── PIDTrap         Software PID with WPILib TrapezoidProfile
├── PIDWPI          Wrapper around WPILib PIDController
├── PIDWPI_Trap     Wrapper around WPILib ProfiledPIDController
└── PIDFX           TalonFX onboard PID (reads closed-loop state from hardware)
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

Every PIDBase creates `LoggedNTInput` fields for P, I, D, tolerances,
iZone, and output range. The `checkTuning()` method detects dashboard
changes and applies them. This is called from `outputTelemetry()`.

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
Phoenix 6's Slot0/Slot1/Slot2 configuration.

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

All parameters are dashboard-tunable via `LoggedNTInput`.

### Usage

```java
Balance balance = new Balance(/* isFwd */ true);
balance.init();  // call once at start

// In periodic:
double speed = balance.autoBalanceRoutineForward(imu.getPitch());
drivetrain.setOutputVoltage(speed);
```
