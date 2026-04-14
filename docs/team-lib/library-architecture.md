<!-- markdownlint-disable MD013 MD060 -->
# Library Architecture

This document is the authoritative reference for Team271-Lib's design.
It describes the core abstractions, lifecycle model, and package structure
that robot projects build on top of.

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
│   ├── AutoMode.java
│   ├── AutoMove.java
│   ├── AutoMoveSingle.java
│   ├── AutoMoveSequence.java
│   ├── AutoMoveParallel.java
│   ├── AutoMoveConditional.java
│   └── AutoMoveTimed.java
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
│   ├── pid/                   PID variants
│   └── Balance.java           Charge station balancing
│
├── nt/                        NetworkTables integration
│   ├── NTEntry.java           Topic subscription/publication
│   ├── NTTable.java           Hierarchical table manager
│   └── LoggedNTInput.java     Dashboard-tunable values
│
├── sysid/                     System identification logging
│   ├── Logger.java            Base SysID logger
│   └── LoggerGeneral.java     General characterization
│
├── geometry/                  2D pose and transformation utilities
│   ├── Pose2d.java, Rotation2d.java, Translation2d.java, Twist2d.java
│   └── Interfaces: IPose2d, IRotation2d, ITranslation2d
│
├── util/                      Utilities
│   ├── Util.java              Math helpers (limit, interpolate, epsilonEquals)
│   ├── Alert.java             Driver notification system
│   ├── DriveSignal.java       Drivetrain voltage pair
│   ├── CSVWritable.java       CSV export interface
│   └── Interpolable.java      Interpolation interface
│
├── misc/
│   └── Elastic.java           Elastic Dashboard integration
│
└── wpilib/                    WPILib framework extensions
    ├── IterativeRobot.java
    └── TimedRobot.java
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
simulationInit / simulationPeriodic
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
    // Reset encoder to known position
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
  → SubsystemManager.outputTelemetry()     — publish to NT
```

See [coding standard Appendix D](../team271-java-coding-standard.md) for
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

Devices register signals during `robotInit()`:

```java
CTREManager.addSignalTalonFX(talonFX.getPosition(), updateFreqHz);
CTREManager.addSignalCANCoder(cancoder.getAbsolutePosition(), updateFreqHz);
```

### Timing

- Default signal refresh rate: 250 Hz
- `refreshAll()` returns a `StatusCode` — errors are throttled to
  one Elastic notification per 2 seconds
- `getDt()` calculates frame time delta from signal timestamps
- `getLastRefreshTime()` provides the best timestamp from the last refresh

---

## Hardware Abstraction Stack

The hardware layer follows a layered architecture:

```text
TransmissionFX          (multi-motor + MM + shifting + telemetry)
  └── ControllerTalonFX (single motor + signals + config)
      └── ControllerSmart (current/voltage/PID abstractions)
          └── ControllerBase (type system + follower validation)
              └── TObj (lifecycle + NT)
```

Transmissions aggregate motors, encoders, switches, and shifters into
a single coordinated unit. See [Hardware Abstraction](hardware-abstraction.md)
for the full design.

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

## NetworkTables & Telemetry

### NTEntry

Wrapper around NT4 topic subscriptions. Provides typed constructors
for boolean, double, and array values. Integrates with AdvantageKit
log keys via `logKey()`.

### LoggedNTInput

Dashboard-tunable values that can be adjusted at runtime without
redeploying code. Used for PID gains, current limits, voltage limits,
and other tunables. Changes are detected in `checkTuning()` methods
and applied to hardware.

```java
// In constructor:
tuneP = new LoggedNTInput(table, "Tune/kP", pidSlot.kP);

// In outputTelemetry():
if (tuneP.hasChanged()) {
    setP(tuneP.get());
}
```

### Alert System

`Alert` provides driver notifications at three severity levels
(ERROR, WARNING, INFO). Notifications appear on the Elastic Dashboard
and are logged via AdvantageKit.

---

## Simulation Support

Every hardware class provides simulation hooks:

- `simulationInit(double)` — lazy-initialize sim state
- `simulationPeriodic(double)` — update sim state each cycle
- `setSimPosRotations(double)` / `setSimVelRotations(double)` — set
  simulated sensor values

TransmissionBase creates the appropriate `DCMotor` model
(Falcon500Foc, KrakenX60Foc, etc.) based on the motor type,
enabling physics-accurate simulation.

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

### Why Centralized CAN Refresh

Per-device signal refresh causes CAN bus contention and inconsistent
timestamps. `CTREManager.refreshAll()` batches all signals into a single
CAN frame exchange, ensuring:
- Consistent timestamps across all devices
- Lower bus utilization
- Simpler latency compensation
