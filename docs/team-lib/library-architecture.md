<!-- markdownlint-disable MD013 MD060 -->
# Library Architecture

> **Scope:** This is the authoritative reference for Team271-Lib's design —
> the reusable library that all robot projects build on. Robot-specific
> design docs (subsystem state machines, CAN ID assignments, controller
> bindings, auto paths) belong in each robot project's own `docs/` folder.
> This document describes the framework; robot docs describe how a specific
> robot uses it.

---

## How Library and Robot Docs Coexist

```text
Team271-Lib/docs/
├── team-lib/                  ← YOU ARE HERE — library design docs
│   ├── library-architecture.md
│   ├── hardware-abstraction.md
│   ├── control-system.md
│   └── ...
├── team271-java-coding-standard.md   ← shared coding rules (in team-lib/)
└── prompts/                          ← review prompts

Robot-2026-Comp/docs/          ← robot-specific (separate repo)
├── drivetrain-design.md
├── intake-design.md
├── auto-paths.md
└── ...
```

The library docs describe *what building blocks exist and how to use them*.
Robot docs describe *how a specific robot is assembled from those blocks*.
When both cover the same topic (e.g., simulation), the library doc covers
the infrastructure and the robot doc covers the physics models and tuning
values specific to that robot.

---

## Package Map

```text
com.team271.lib
├── TObj.java                  Base class — lifecycle hooks + NT hierarchy
├── TRobot.java                Robot singleton
├── ConstantsLib.java          Library-wide constants
│
├── subsystem/
│   ├── Subsystem.java         Subsystem base — SensorMode, homing
│   └── SubsystemManager.java  Singleton lifecycle orchestrator
│
├── auto/                      Autonomous move composition (see auto-design.md)
│
├── hardware/                  Hardware abstractions (see hardware-abstraction.md)
│   ├── CANBus.java            CAN bus wrapper
│   ├── CANDeviceID.java       Device ID (bus + number)
│   ├── CTREManager.java       Centralized signal refresh
│   ├── controllers/           Motor controller hierarchy
│   ├── motors/                Motor type definitions
│   ├── transmissions/         Multi-motor aggregation + shifters
│   ├── sensors/               Encoders, IMUs, range, switches
│   └── Input/                 Gamepad/joystick abstraction
│
├── control/                   Control algorithms (see control-system.md)
│   ├── pid/                   PID variants (5 types)
│   └── Balance.java           Charge station balancing
│
├── nt/                        NetworkTables integration
│   ├── NTEntry.java           Topic subscription/publication (output only)
│   ├── NTTable.java           Hierarchical table manager
│   └── LoggedNTInput.java     Dashboard-tunable values (input with change detection)
│
├── sysid/                     System identification logging
├── geometry/                  2D pose and transformation utilities
├── util/                      Utilities (math, alerts, signals)
├── misc/
│   └── Elastic.java           Elastic Dashboard notification API
│
└── wpilib/                    WPILib framework extensions
```

---

## TObj — Base Class

Every library object extends `TObj`. It provides two things:

1. **Lifecycle hooks** — empty methods that subclasses override as needed
2. **NetworkTables hierarchy** — each TObj creates an NT subtable under
   its parent, giving automatic telemetry namespacing

### Lifecycle Methods

```java
robotInit(double timestamp)
robotPeriodicBefore(double timestamp)
robotPeriodicAfter(double timestamp)
disabledInit / disabledPeriodic / disabledExit
autonomousInit / autonomousPeriodic / autonomousExit
teleopInit / teleopPeriodic / teleopExit
simulationInit / simulationPeriodic              // ← simulation hooks
testInit / testPeriodic / testExit
outputTelemetry()
```

All methods receive a timestamp parameter (seconds) for consistent
time-based calculations. The `outputTelemetry()` method has no
timestamp — it publishes cached values to NT.

### NT Table Hierarchy

Passing a parent to the TObj constructor creates nested tables:

```java
// Creates NT path: /Drivetrain/LeftTransmission/Leader
new ControllerTalonFX(transmission, "Leader", ...);
```

This hierarchy flows automatically — no manual path construction needed.
Every component's tunables and telemetry appear under its parent's
NT path, making it easy to find in a dashboard.

---

## Subsystem Base Class

`Subsystem` extends TObj and adds sensor management:

### SensorMode State Machine

| Mode | Purpose |
|------|---------|
| `SENSORED_AUTO` | Normal operation — sensors read automatically |
| `SENSORED_MANUAL` | Manual sensor control (e.g., during calibration) |
| `SENSORLESS` | Sensors disabled — open-loop only |
| `SYSID` | System identification mode |

### Homing Pattern

Subsystems that require zeroing/calibration implement:

```java
@Override
public void sensorsZero() {
    transmission.setPosRotations(0.0);
    isZeroed = true;
}
```

The `isZeroed` flag gates closed-loop operation — subsystems must not
run position control until zeroed. See coding standard 4.9c for homing
timeout requirements.

---

## SubsystemManager — Lifecycle Orchestrator

A singleton that broadcasts lifecycle events to all registered subsystems.
The robot's main loop calls SubsystemManager methods in a specific order:

```text
Robot.robotPeriodic()
  → CTREManager.refreshAll()              — bulk CAN signal refresh
  → SubsystemManager.robotPeriodicBefore() — read sensors
  → <mode>Periodic()                      — state machine logic
  → SubsystemManager.robotPeriodicAfter()  — apply outputs
  → SubsystemManager.outputTelemetry()     — publish to NT + check tunables
```

See [coding standard Appendix D](team271-java-coding-standard.md) for
the full lifecycle reference.

### Exception Isolation

`SubsystemManager.forEachSafe()` wraps each subsystem call in a
try/catch. If one subsystem throws, the others continue executing.
Error notifications are throttled to at most once per 2 seconds per
subsystem to avoid flooding the driver station.

### Registration Order

Subsystems are added via `addSubsystem()`. The add order determines
execution order — subsystems that depend on others must be added after
their dependencies. This is load-bearing and documented in robot
project code.

---

## CTREManager — Centralized CAN Refresh

A static singleton that manages all CTRE Phoenix 6 devices and signals.

### How It Works

1. During `robotInit()`, each hardware component registers its
   StatusSignals with CTREManager via `addSignal()`
2. After all subsystems initialize, `CTREManager.init()` converts the
   signal list to an array and optimizes per-bus scheduling
3. Every robot cycle, `CTREManager.refreshAll()` refreshes all signals
   in a single call — this is more efficient than per-device refresh

### Signal Registration

Type-specific registration helpers ensure correct per-bus optimization:

```java
CTREManager.addSignalTalonFX(talonFX.getPosition(), updateFreqHz);
CTREManager.addSignalCANCoder(cancoder.getAbsolutePosition(), updateFreqHz);
CTREManager.addSignalPigeon(pigeon.getYaw(), updateFreqHz);
CTREManager.addSignalCANrange(canrange.getDistance(), updateFreqHz);
```

### Signal Lifecycle Detail

The complete lifecycle of a CTRE signal through CTREManager:

1. **Registration** — During `robotInit()`, each hardware wrapper
   calls the appropriate `addSignal*()` method (e.g.,
   `addSignalTalonFX()`). Internally, `addSignalInternal()` validates
   that the signal's `StatusCode` is OK, adds it to the global list,
   and sets the signal's update frequency via `setUpdateFrequency()`.
   Invalid signals are silently skipped.

2. **Initialization** — After all subsystems register, calling
   `CTREManager.init()`:
   - Converts the `ArrayList<StatusSignal<?>>` to an array for
     efficient bulk refresh
   - Runs `ParentDevice.optimizeBusUtilizationForAll()` per CAN bus,
     which adjusts frame scheduling to minimize bus collisions
   - Starts `SignalLogger` with auto-logging if any CANivore bus
     is registered (hoot files written to `/U/logs`)

3. **Refresh** — Each robot cycle, `refreshAll()`:
   - Calls `BaseStatusSignal.refreshAll()` on the entire signal array
   - Tracks `AllTimestamps` from the first signal for `getDt()`
   - On error, sends a throttled Elastic notification (max 1 per 2s)
   - Returns `StatusCode` for caller inspection

4. **Teardown** — `stopLogging()` stops SignalLogger on CANivore buses.

### Device Registration Pattern

`CTREManager.addDevice()` tracks each CTRE device in two data
structures:

- **Global list** — all devices across all buses
- **Per-bus map** — `LinkedHashMap<String, ArrayList<ParentDevice>>`
  keyed by bus name, used for per-bus optimization

The device's bus is auto-registered via `addBus()` if not already
tracked. This means explicit bus registration (`CTREManager.addBus()`)
is optional but recommended for clarity in multi-CANivore setups.

**Important:** All device and signal registration must complete before
`CTREManager.init()` is called. Signals added after `init()` will
not be included in the refresh array.

`addSignalCANdi()` exists for future CANdi device support but no
wrapper class has been created. See the
[CTRE Feature Coverage](hardware-abstraction.md#ctre-phoenix-6-feature-coverage)
matrix for planned device support.

### Timing

- Default signal refresh rate: 250 Hz (CANrange: 100 Hz)
- `refreshAll()` returns a `StatusCode` — errors are throttled to
  one Elastic notification per 2 seconds
- `getDt()` calculates frame time delta from signal timestamps
- `getLastRefreshTime()` provides the best timestamp from the last refresh

---

## State Machine Pattern

The library enforces a **desired-to-actual** state machine pattern:

1. **State setting** happens in `teleopPeriodic()` or `autonomousPeriodic()`
   — code sets the *desired* state
2. **State application** happens in `robotPeriodicAfter()` — the subsystem
   reads the desired state and commands hardware

This separation ensures:

- All sensor reads complete before state decisions
- All state decisions complete before hardware commands
- No cross-subsystem race conditions within a single cycle

---

## Tuning Infrastructure

The library provides a complete dashboard-tuning system that lets
operators adjust values at runtime without redeploying code.

### Three NT Classes — Different Purposes

| Class | Direction | Change Detection | Use For |
|-------|-----------|-----------------|---------|
| `NTTable` | — | — | Hierarchical namespace container |
| `NTEntry` | Output only | No | Publishing telemetry to dashboard |
| `LoggedNTInput` | Input + Output | Yes (`hasChanged()`) | Dashboard-tunable parameters |

### NTEntry — Output Telemetry

Wraps NT4 topic subscriptions. Publishes values via AdvantageKit Logger:

```java
NTEntry ntPos = new NTEntry(table, "Position", 0.0);

// In outputTelemetry():
ntPos.publish(currentPosition);  // logs to AdvantageKit
```

Supports boolean, double, long, int, and string types.

### LoggedNTInput — Dashboard Tunables

The primary tuning API. Maintains both a publisher (sets defaults on
dashboard) and a subscriber (reads operator changes). All reads are
automatically logged to AdvantageKit.

```java
// In constructor — register with default:
LoggedNTInput tuneP = new LoggedNTInput(table, "Tune/kP", 0.1);

// In checkTuning() — detect and apply:
if (tuneP.hasChanged()) {
    setP(tuneP.getDbl());
}
```

**Change detection methods by type:**

| Type | Check | Get |
|------|-------|-----|
| double | `hasChanged()` | `getDbl()` |
| boolean | `hasBoolChanged()` | `getBool()` |
| long | `hasLongChanged()` | `getLong()` |
| String | `hasStringChanged()` | `getString()` |

**Null-safe:** If table is null, LoggedNTInput still works (returns
cached default values) but change detection always returns false.

### The checkTuning() Pattern

Every class with tunables follows this pattern:

```java
@Override
public void outputTelemetry() {
    checkTuning();           // 1. Detect and apply dashboard changes
    super.outputTelemetry(); // 2. Call parent telemetry
    // 3. Publish current values via NTEntry
}

protected void checkTuning() {
    if (tuneP.hasChanged()) setP(tuneP.getDbl());
    if (tuneI.hasChanged()) setI(tuneI.getDbl());
    // ...
}
```

The tuning check runs every robot cycle as part of `outputTelemetry()`,
which is called by `SubsystemManager.outputTelemetry()`.

### Where Tunables Live

| Layer | What's Tunable | NT Path Example |
|-------|---------------|-----------------|
| PIDBase | P, I, D, tolerances, iZone, output range | `/Arm/PID/Tune P` |
| ControllerSmart | Stator/supply current limits, voltage limits | `/Arm/Trans/Leader/Tune Stator Limit` |
| TransmissionFX | Motion Magic profile, PID gains (kP-kS) | `/Arm/Trans/Tune MM Cruise Vel` |
| Balance | Speeds, tilt thresholds, debounce time | `/Balance/Tune Speed Slow` |

See [Hardware Abstraction](hardware-abstraction.md) and
[Control System](control-system.md) for complete tunable inventories.

### Tuning Workflow

1. Deploy code to robot (or run `./gradlew simulateJava`)
2. Open Elastic Dashboard / Shuffleboard / AdvantageScope
3. Navigate to the component's NT path → `Tune/` subtable
4. Modify values — changes apply on the next robot cycle
5. Once dialed in, copy values back to `Constants.java`

### Elastic Dashboard Notifications

`Elastic` sends JSON notifications to the Elastic Dashboard:

```java
Elastic.sendNotification(new Elastic.Notification(
    Elastic.NotificationLevel.WARNING,
    "Homing Timeout",
    "Arm homing timed out after 2.0s"));
```

Levels: `INFO` (green), `WARNING` (yellow), `ERROR` (red).
Supports builder pattern for display time, width, height.

### Alert System

`Alert` manages persistent, categorized alerts logged to AdvantageKit
and SmartDashboard. Unlike Elastic notifications (one-shot), Alerts
have active/inactive state:

```java
Alert homingAlert = new Alert("Arm", "Homing failed", AlertType.WARNING);
homingAlert.set(true);   // activate — sends Elastic + DriverStation
homingAlert.set(false);  // deactivate
```

`Alert.outputTelemetry()` is called once per cycle by SubsystemManager
to log all active alerts to AdvantageKit.

---

## Simulation Architecture

The library provides two complementary simulation layers:

### Layer 1: CTRE SimState (Device-Level)

Every CTRE device has a SimState object that models device behavior
in the WPILib simulation environment:

| Device | SimState | Initialized In |
|--------|----------|----------------|
| TalonFX | `TalonFXSimState` | `ControllerTalonFX.simulationInit()` |
| CANcoder | `CANcoderSimState` | `EncoderCANCoder.create()` |
| Pigeon 2 | `Pigeon2SimState` | `IMUPigeon2.create()` |
| CANrange | `CANrangeSimState` | `RangeCANrange.create()` |

SimState allows setting simulated position, velocity, and supply voltage.
The CTRE firmware simulation uses these to model device responses.

### Layer 2: WPILib DCMotor (Physics-Level)

`TransmissionBase.robotInit()` creates a `DCMotor` model matching the
actual motor hardware:

| Motor Type | DCMotor Model |
|-----------|--------------|
| `FALCON500` | `DCMotor.getFalcon500Foc(numMotors)` |
| `KRAKENX60` | `DCMotor.getKrakenX60Foc(numMotors)` |
| `KRAKENX44` | Custom (12V, 4.05A, 275Nm, 7530 RPM) |
| `NEO` / `NEO550` / `NEO_VORTEX` | Matching WPILib model |

The `numMotors` parameter is the total count (leader + followers),
giving accurate aggregate torque for physics simulation. Access via
`transmission.getDCMotor()`.

### Simulation Lifecycle

```text
robotInit()
  → TransmissionBase creates DCMotor model
  → CTRE devices create SimState objects

simulationInit()                        (once, on first sim cycle)
  → ControllerTalonFX: configures motor type + orientation on SimState
  → Delegated to all sensors via TransmissionBase

simulationPeriodic()                    (every 20 ms sim cycle)
  → All CTRE devices: set supply voltage from RobotController.getBatteryVoltage()
  → [Robot-specific]: update physics model, feed results back via setSimPos/Vel
```

### Position/Velocity Propagation

When a robot-specific physics model computes new state, it propagates
through the entire hardware stack:

```text
transmission.setSimPosRotations(position)      ← subsystem calls this
  → encCANCoder.setSimPosRotations()     → CANcoderSimState.setRawPosition()
  → allControllers.setSimPosRotations()  → TalonFXSimState.setRawRotorPosition()
  → EncoderFX reads from TalonFXSimState automatically
```

> Subsystems only call `transmission.setSimPosRotations()` — the internal
> dispatch to encoder/controller types is handled by TransmissionBase.

### Robot Project Simulation Pattern

The library provides infrastructure; robot projects implement the physics:

```java
// In a robot project subsystem:
private SingleJointedArmSim armSim;

@Override
public void simulationInit(double timestamp) {
    super.simulationInit(timestamp);  // sets up CTRE SimStates
    armSim = new SingleJointedArmSim(
        transmission.getDCMotor(),    // DCMotor from library
        gearRatio, jKgMetersSquared, armLengthMeters,
        minAngleRad, maxAngleRad, simulateGravity, startAngleRad);
}

@Override
public void simulationPeriodic(double timestamp) {
    super.simulationPeriodic(timestamp);  // updates supply voltages

    // Read what the motor is commanding
    armSim.setInputVoltage(
        transmission.getSimState().getMotorVoltage());

    // Step physics (20 ms)
    armSim.update(0.020);

    // Feed results back into hardware sim
    double rotorPos = Units.radiansToRotations(armSim.getAngleRads()) * gearRatio;
    double rotorVel = Units.radiansToRotations(armSim.getVelocityRadPerSec()) * gearRatio;
    transmission.setSimPosRotations(rotorPos);
    transmission.setSimVelRotations(rotorVel);
}
```

### WPILib Simulation Classes Reference

These WPILib classes pair with library DCMotor models:

| WPILib Class | Use With |
|-------------|----------|
| `SingleJointedArmSim` | Arms, wrists |
| `ElevatorSim` | Elevators, linear mechanisms |
| `FlywheelSim` | Flywheels, shooters |
| `DCMotorSim` | Generic rotational mechanisms |
| `DifferentialDrivetrainSim` | Tank drivetrains |

### Simulation Capability Matrix

| Component | SimState | setSimPos | setSimVel | simulationInit | simulationPeriodic |
|-----------|----------|-----------|-----------|----------------|-------------------|
| TransmissionBase | DCMotor | Yes | Yes | Yes (delegates) | Yes (delegates) |
| TransmissionFX | TalonFXSimState (via leader) | Inherited | Inherited | Inherited | Inherited |
| ControllerTalonFX | TalonFXSimState | Yes | Yes | Yes (motor type) | Yes (voltage) |
| EncoderFX | Via controller SimState | Yes | Yes | Stub | Stub |
| EncoderCANCoder | CANcoderSimState | Yes | Yes | Stub | Yes (voltage) |
| IMUPigeon2 | Pigeon2SimState | — | — | — | Yes (voltage) |
| RangeCANrange | CANrangeSimState | — | — | — | Yes (voltage) |

---

## Key Design Decisions

### Why Singletons for Managers

SubsystemManager and CTREManager use the singleton pattern because:

- There is exactly one robot with one set of subsystems
- There is exactly one CAN bus system
- The lifecycle must be globally coordinated

### Why TObj Instead of WPILib Subsystem

WPILib's `SubsystemBase` is designed for the command-based framework.
Team 271 uses a state machine pattern instead, which requires:

- Finer-grained lifecycle hooks (before/after periodic)
- Explicit execution ordering
- Exception isolation per subsystem
- Simulation hooks built into the lifecycle

### Why Centralized CAN Refresh

Per-device signal refresh causes CAN bus contention and inconsistent
timestamps. `CTREManager.refreshAll()` batches all signals into a single
CAN frame exchange, ensuring:

- Consistent timestamps across all devices
- Lower bus utilization
- Simpler latency compensation

### Why LoggedNTInput Instead of Raw NT

Raw NT subscriptions require manual logging and change detection.
`LoggedNTInput` provides:

- Automatic AdvantageKit logging on every read
- Built-in change detection (`hasChanged()`)
- Null-safe operation (works with or without a live NT connection)
- Type-specific constructors with default values published to dashboard
