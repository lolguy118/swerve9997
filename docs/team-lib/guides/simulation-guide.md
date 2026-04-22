# Simulation Guide for Robot Projects

> **Scope:** This guide explains how robot projects implement physics
> simulation using the library's simulation infrastructure. For the
> infrastructure itself (SimState architecture, DCMotor models,
> lifecycle, capability matrix), see
> [SDD-team271-lib §3.6 Simulation Architecture](../planning/sdd/SDD-team271-lib.md#36-simulation-architecture).

---

## Overview

The library provides two simulation layers:

1. **CTRE SimState** — device-level simulation (TalonFXSimState,
   CANcoderSimState, etc.) initialized automatically by the library
2. **WPILib DCMotor** — physics-accurate motor models created by
   `TransmissionBase.robotInit()` based on motor type

Robot projects are responsible for:

- Creating WPILib physics simulation objects (e.g., `SingleJointedArmSim`)
- Feeding motor output voltage into the physics model
- Feeding physics results back into the library's SimState

---

## Step-by-Step for Each Mechanism Type

### Arm (SingleJointedArmSim)

```java
private SingleJointedArmSim armSim;

@Override
public void simulationInit(double timestamp) {
    super.simulationInit(timestamp);  // initializes CTRE SimStates
    armSim = new SingleJointedArmSim(
        transmission.getDCMotor(),    // from library
        gearRatio,                    // motor-to-arm ratio
        jKgMetersSquared,             // moment of inertia
        armLengthMeters,              // arm length
        minAngleRad,                  // lower limit
        maxAngleRad,                  // upper limit
        simulateGravity,              // true for arms affected by gravity
        startingAngleRad              // initial position
    );
}

@Override
public void simulationPeriodic(double timestamp) {
    super.simulationPeriodic(timestamp);  // updates supply voltages

    // Feed motor voltage into physics
    armSim.setInputVoltage(
        transmission.getLeaderController().getSimState().getMotorVoltage());

    // Step physics (20 ms)
    armSim.update(0.020);

    // Feed back into hardware sim
    double rotorPos = Units.radiansToRotations(armSim.getAngleRads()) * gearRatio;
    double rotorVel = Units.radiansToRotations(armSim.getVelocityRadPerSec()) * gearRatio;
    transmission.setSimPosRotations(rotorPos);
    transmission.setSimVelRotations(rotorVel);
}
```

### Elevator (ElevatorSim)

```java
private ElevatorSim elevatorSim;

@Override
public void simulationInit(double timestamp) {
    super.simulationInit(timestamp);
    elevatorSim = new ElevatorSim(
        transmission.getDCMotor(),
        gearRatio,
        carriageMassKg,
        drumRadiusMeters,
        minHeightMeters,
        maxHeightMeters,
        simulateGravity,
        startingHeightMeters
    );
}

@Override
public void simulationPeriodic(double timestamp) {
    super.simulationPeriodic(timestamp);

    elevatorSim.setInputVoltage(
        transmission.getLeaderController().getSimState().getMotorVoltage());
    elevatorSim.update(0.020);

    // Convert linear position to rotor rotations
    double rotorPos = elevatorSim.getPositionMeters()
        / (2.0 * Math.PI * drumRadiusMeters) * gearRatio;
    double rotorVel = elevatorSim.getVelocityMetersPerSecond()
        / (2.0 * Math.PI * drumRadiusMeters) * gearRatio;
    transmission.setSimPosRotations(rotorPos);
    transmission.setSimVelRotations(rotorVel);
}
```

### Flywheel (FlywheelSim)

```java
private FlywheelSim flywheelSim;

@Override
public void simulationInit(double timestamp) {
    super.simulationInit(timestamp);
    flywheelSim = new FlywheelSim(
        transmission.getDCMotor(),
        gearRatio,
        jKgMetersSquared    // moment of inertia of the flywheel
    );
}

@Override
public void simulationPeriodic(double timestamp) {
    super.simulationPeriodic(timestamp);

    flywheelSim.setInputVoltage(
        transmission.getLeaderController().getSimState().getMotorVoltage());
    flywheelSim.update(0.020);

    // Flywheel: only velocity matters (position is unbounded)
    double rotorVel = Units.radiansToRotations(
        flywheelSim.getAngularVelocityRadPerSec()) * gearRatio;
    transmission.setSimVelRotations(rotorVel);
}
```

### Differential Drivetrain (DifferentialDrivetrainSim)

```java
private DifferentialDrivetrainSim drivetrainSim;

@Override
public void simulationInit(double timestamp) {
    super.simulationInit(timestamp);
    drivetrainSim = new DifferentialDrivetrainSim(
        leftTransmission.getDCMotor(),
        gearRatio,
        jKgMetersSquared,       // robot moment of inertia
        massKg,                 // robot mass
        wheelRadiusMeters,
        trackWidthMeters,
        null                    // measurement noise (null = none)
    );
}

@Override
public void simulationPeriodic(double timestamp) {
    super.simulationPeriodic(timestamp);

    drivetrainSim.setInputs(
        leftTransmission.getLeaderController().getSimState().getMotorVoltage(),
        rightTransmission.getLeaderController().getSimState().getMotorVoltage());
    drivetrainSim.update(0.020);

    // Feed back position and velocity for both sides
    double leftPos = drivetrainSim.getLeftPositionMeters()
        / (2.0 * Math.PI * wheelRadiusMeters) * gearRatio;
    double leftVel = drivetrainSim.getLeftVelocityMetersPerSecond()
        / (2.0 * Math.PI * wheelRadiusMeters) * gearRatio;
    leftTransmission.setSimPosRotations(leftPos);
    leftTransmission.setSimVelRotations(leftVel);

    double rightPos = drivetrainSim.getRightPositionMeters()
        / (2.0 * Math.PI * wheelRadiusMeters) * gearRatio;
    double rightVel = drivetrainSim.getRightVelocityMetersPerSecond()
        / (2.0 * Math.PI * wheelRadiusMeters) * gearRatio;
    rightTransmission.setSimPosRotations(rightPos);
    rightTransmission.setSimVelRotations(rightVel);

    // Update IMU with simulated heading
    imu.getSimState().setRawYaw(
        drivetrainSim.getHeading().getDegrees());
}
```

---

## Common Patterns

### Reading Motor Voltage from SimState

The physics model needs the motor's commanded voltage:

```java
double voltage = transmission.getLeaderController()
    .getSimState().getMotorVoltage();
```

This returns the voltage the TalonFX firmware simulation is applying
to the motor model — it accounts for current limits, voltage
saturation, and neutral mode.

### Converting Physics Output to Rotor Rotations

WPILib physics models output in SI units (radians, meters). The
library's `setSimPosRotations()` / `setSimVelRotations()` expect
**rotor rotations** (before gear reduction). Multiply by the gear
ratio:

```java
// Rotational mechanism (arm, flywheel)
double rotorRot = Units.radiansToRotations(simAngleRad) * gearRatio;

// Linear mechanism (elevator)
double rotorRot = linearMeters / (2 * PI * drumRadius) * gearRatio;
```

### Feeding Position/Velocity Back

`transmission.setSimPosRotations()` and `setSimVelRotations()`
propagate values through the entire hardware stack:

- CANcoder SimState (if present)
- All controller SimStates (leader + followers)
- EncoderFX reads from the controller SimState automatically

### IMU Simulation

For mechanisms that affect heading (drivetrains), update the Pigeon 2
SimState:

```java
imu.getSimState().setRawYaw(headingDegrees);
```

---

## Debugging Simulation

### SimState Not Initialized

**Symptom:** `NullPointerException` when accessing `getSimState()`

**Cause:** `super.simulationInit(timestamp)` was not called, or the
transmission's `robotInit()` was not called before `simulationInit()`.

**Fix:** Ensure the subsystem calls `super.simulationInit(timestamp)`
at the start of its override.

### Wrong Gear Ratio Direction

**Symptom:** Motor spins the wrong way in simulation, or position
feedback is inverted.

**Cause:** The gear ratio is applied as a multiplier. If the mechanism's
gear ratio inverts direction, ensure the sign is correct.

**Fix:** Check that `rotorToMechanism` and `gearRatio` in the sim
model use the same convention. If the motor spins CW but the mechanism
moves CCW, one of them needs a sign flip.

### Motor Voltage Always Zero

**Symptom:** `getMotorVoltage()` returns 0.0 even though the motor
is commanded.

**Cause:** The SimState's supply voltage was not set. Without supply
voltage, the simulated motor cannot produce output.

**Fix:** `super.simulationPeriodic(timestamp)` sets supply voltage
from `RobotController.getBatteryVoltage()`. Ensure it is called.

### Position Drifts Over Time

**Symptom:** Simulated position slowly diverges from expected value.

**Cause:** The physics model's `update()` timestep doesn't match
the robot loop period.

**Fix:** Use `0.020` (20 ms) for the standard 50 Hz robot loop. If
using a faster loop, adjust accordingly.

---

## Devices Without Simulation Support

> **Status: Planned — Not Yet Implemented.**
>
> The following CTRE devices do not yet have library wrapper classes,
> so they cannot be simulated. When wrapper classes are added, each
> will need its corresponding SimState integration.

- **CANdi** — No `CANdi` wrapper class exists. When implemented, will
  require a `CANdiSimState` for simulating digital/analog inputs and
  current distribution channels.
- **TalonFXS** — No `TalonFXS` controller wrapper exists. When
  implemented, will need SimState integration with motor type
  selection similar to `ControllerTalonFX.simulationInit()`.

See the [Phoenix 6 Feature Coverage Matrix](../planning/sdd/SDD-vendor-ctre.md#7-phoenix-6-feature-coverage-matrix)
for the full list of planned device support.
