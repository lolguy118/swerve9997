<!-- markdownlint-disable MD013 MD060 -->
# Vendor Abstraction Guide

> **Scope:** How the vendor-neutral interfaces (`api/`) and CTRE vendor
> wrappers (`vendor/ctre/`) work together. When to use the neutral interface
> vs. the vendor passthrough.

---

## Architecture

```text
Robot Subsystem
  ↓ uses
TransmissionFX (mechanism units → native units conversion)
  ↓ delegates to
CTREMotor (implements ClosedLoopMotor — vendor-neutral interface)
  ↓ wraps
ControllerTalonFX (CTRE-specific implementation)
  ↓ controls
TalonFX (raw CTRE hardware)
```

**Two access paths for every motor:**

1. **Vendor-neutral** — `ClosedLoopMotor` interface for position, velocity,
   PID, current limits, etc. Works with any future motor vendor.
2. **Vendor-specific** — `CTREMotor` passthrough for Motion Magic, FOC,
   timesync, and raw `TalonFX` access. Full CTRE power.

---

## Using the Vendor-Neutral Interface

```java
// TransmissionFX exposes the vendor-neutral wrapper:
ClosedLoopMotor motor = transmission.getCTRELeader();

// Standard closed-loop operations work through the neutral interface:
motor.setOutputPosition(rotations, slot, ffVolts);
motor.setOutputVelocity(rps, slot, ffVolts);
motor.setGains(slot, gains);
motor.setCurrentLimit(config);
motor.setContinuousWrap(true);

// Query capabilities at runtime:
if (motor.capabilities().supportsMotionMagic()) {
    // safe to cast for CTRE-specific features
}
```

---

## Using CTRE-Specific Features

```java
// Direct access to CTREMotor for Motion Magic and advanced features:
CTREMotor ctre = transmission.getCTRELeader();

// Motion Magic (CTRE-only — not on the neutral interface):
ctre.setOutputMMPositionVoltage(nativePos, slot, ff);
ctre.setOutputMMVelocityVoltage(nativeRPS, slot, ff);
ctre.setOutputDynMMPositionVoltage(nativePos, slot, vel, accel, jerk, ff);
ctre.configMotionMagic(cruiseVel, accel, jerk);

// Torque current FOC (CTRE-only):
ctre.setOutputTorqueCurrent(amps);

// Raw TalonFX passthrough (for anything not yet wrapped):
TalonFX fx = ctre.getTalonFX();
TalonFXConfiguration config = ctre.getConfig();
TalonFXSimState sim = ctre.getSimState();
```

---

## TransmissionFX: Mechanism Units → Native Units

`TransmissionFX` output methods accept **mechanism output units** and
convert to **native rotor units** via the `EncoderAdapter` before
delegating to `CTREMotor`:

```java
// Subsystem calls with mechanism units (e.g., degrees, inches):
transmission.setOutputMMPositionVoltage(mechanismPos, slot, ff);

// Internally:
//   nativePos = encoder.mechanismToNative(mechanismPos)
//   mCTRELeader.setOutputMMPositionVoltage(nativePos, slot, ff)
//     → talonFX.setControl(motionMagicVoltageRequest)
```

---

## HardwareManager: Mixed-Vendor Refresh

For CTRE-only setups (the common case), `HardwareManager.refreshAll()`
is functionally identical to `CTREManager.refreshAll()`. It also supports
non-CTRE devices that implement `SignalRefreshable`:

```java
// In Robot.robotPeriodic():
HardwareManager.refreshAll();  // replaces CTREManager.refreshAll()
```

---

## CommandBridge: WPILib Interop

For teams that want PathPlanner's command-based API alongside Team271's
lifecycle pattern:

```java
// Wrap a Team271 Lifecycle as a WPILib Subsystem:
SubsystemBase wpiSub = CommandBridge.asWPISubsystem(myLifecycle, "MySubsystem");

// Wrap a WPILib Command as a Team271 AutoMove (with mandatory timeout):
AutoMove move = CommandBridge.asAutoMove(pathPlannerCommand, kPathTimeoutSec);

// Wrap a Team271 AutoMode as a WPILib Command:
Command cmd = CommandBridge.asCommand(myAutoMode);
```

---

## When to Add a New Vendor Implementation

The vendor-neutral interfaces exist so new vendors CAN be added, but only
when there's a concrete need:

- A new motor controller vendor is adopted (e.g., REV SparkMax)
- A WPILib sensor needs to interface with library control loops
- The `CommandBridge` requires a WPILib sensor type

Do NOT prebuilt speculative implementations. The library is CTRE-focused.
