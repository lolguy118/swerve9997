<!-- markdownlint-disable MD013 MD060 -->
# Hardware Abstraction Design

This document describes the hardware abstraction layer in Team271-Lib.
The library provides a layered hierarchy from individual motor controllers
up to complete multi-motor transmissions with sensors and shifting.

---

## Controller Hierarchy

```text
TObj
└── ControllerBase              (type system, follower validation, basic output)
    └── ControllerSmart         (current limits, voltage limits, ramping, PID slots)
        └── ControllerTalonFX   (Phoenix 6 concrete implementation)
```

### ControllerBase — Abstract Foundation

Defines the type system and basic motor operations that all controllers share.

**Enums:**

| Enum | Values | Purpose |
|------|--------|---------|
| `ControllerType` | TALONFX, TALONFXS, SPARK_MAX, SPARK_FLEX | Motor controller hardware type |
| `ControllerStatus` | UNKNOWN, ERROR, ERROR_INVALID_BUS, OK | Init/config result |
| `NeutralState` | NONE, BRAKE, COAST | Idle behavior |
| `MotorDirection` | CW, CCW | Rotation direction |

**Key behaviors:**
- Each controller wraps a `CANDeviceID` (bus name + device number)
- `follow()` validates that leader and follower are on the **same CAN bus** —
  returns `ERROR_INVALID_BUS` if not
- `isConnected` / `isConfigured` flags track device health
- Abstract output methods: `setOutputDuty()`, `setOutputVoltage()`, `stop()`

### ControllerSmart — Advanced Features

Adds abstractions for features common to smart motor controllers:

- **Current limits:** stator (output-side) and supply (input-side),
  with optional time-based supply thresholds
- **Voltage limits:** peak forward/reverse voltage capping
- **Ramping:** open-loop and closed-loop ramp rates for duty cycle,
  voltage, and torque
- **PID by slot:** up to 3 PID slot configurations (P, I, D, V, S gains)
- **Live tuning:** `LoggedNTInput` fields for current limits, voltage
  limits, and PID gains — changes detected in `checkTuning()` and
  applied automatically

### ControllerTalonFX — Phoenix 6 Implementation

The concrete TalonFX implementation. Key design choices:

**Signal filtering:** An `EnumSet<Signals>` controls which StatusSignals
are registered with CTREManager. Defaults to `ALL`, but can be restricted
to reduce CAN bus load for follower motors that don't need every signal.

**Control objects:** Pre-allocated control request objects with timesync
enabled (`UpdateFreqHz = 0`):

| Object | Type | Use |
|--------|------|-----|
| `motorBrake` | `NeutralOut` | Brake mode stop |
| `motorOut` | `DutyCycleOut` | Open-loop duty cycle |
| `motorOutV` | `VoltageOut` | Open-loop voltage |
| `motorOutTC` | `TorqueCurrentFOC` | Torque current FOC |
| `motorPosition` | `PositionVoltage` | Closed-loop position |
| `motorVelocity` | `VelocityVoltage` | Closed-loop velocity |

**Config application:** `applyConfig()` retries up to `CAN_RETRY_COUNT`
times with 50 ms timeout per attempt. Sets `isConfigured` flag on
success/failure.

**Connection tracking:** `robotPeriodicBefore()` checks
`talonFX.isConnected()` every cycle to detect CAN disconnection.

---

## Transmission Architecture

Transmissions aggregate motors, encoders, switches, and shifters into
a coordinated unit.

```text
TObj
└── TransmissionBase            (multi-motor + encoder + shifter aggregation)
    └── TransmissionFX          (TalonFX-specific: Motion Magic, dynamic MM)
```

### TransmissionBase — Multi-Motor Aggregation

Manages up to 4 motors (1 leader + 3 followers) plus sensors:

**Motor management:**
- `leader` — executes all control commands
- `follower1`, `follower2`, `follower3` — optional followers
- `allControllers` — `LinkedHashSet` for iteration
- Configuration methods (current limits, voltage, ramping) apply to
  **all** motors; PID and direction apply to **leader only**

**Encoder system (dual encoder support):**

| Encoder | Source | Purpose |
|---------|--------|---------|
| `encFX` | TalonFX integrated rotor | Always available, high-speed |
| `encCANCoder` | External CANcoder | Absolute position, mechanism-side |

The library prefers CANCoder when available — position queries fall
back to FX if no CANCoder is configured.

**Gear ratio conversion chain:**

```text
rotor rotations
  × rotorToMechanism      → mechanism rotations
  × mechanismToUnits      → output units (inches, degrees, etc.)

sensor rotations (CANCoder)
  × sensorRelToMechanism  → mechanism rotations (relative)
  × sensorAbsToMechanism  → mechanism rotations (absolute)
  × mechanismToUnits      → output units
```

All position/velocity queries apply the appropriate conversion chain
automatically. Closed-loop commands unscale the setpoint back to
rotor/sensor rotations before sending to hardware.

**Limit switches:**
- `addRevLimit()` / `addFwdLimit()` — factory methods creating `SwitchFX`
  instances from TalonFX internal limit switch signals
- Configurable trigger type (NO/NC), enable/disable, auto-zero on trigger

**Shifter support:**
- `Shifter` interface with `ShifterPneumatic` implementation
- `ShifterState`: GEAR_NONE, GEAR_1, GEAR_2
- Per-gear sensor ratios (`sensorRatioGear1`, `sensorRatioGear2`)
- `shift()` actuates the pneumatic and updates the gear state

### TransmissionFX — TalonFX Specialization

Extends TransmissionBase with Phoenix 6-specific control modes:

**Control mode matrix:**

| Mode | Duty Cycle | Voltage | Torque Current |
|------|-----------|---------|---------------|
| Position | `setOutputPositionDuty` | `setOutputPosition` | `setOutputPositionTorqueCurrent` |
| Velocity | `setOutputVelocityDuty` | `setOutputVelocity` | `setOutputVelocityTorqueCurrent` |
| Motion Magic Position | `setOutputMMPositionDuty` | `setOutputMMPositionVoltage` | `setOutputMMPositionTorqueCurrent` |
| Motion Magic Velocity | `setOutputMMVelocityDuty` | `setOutputMMVelocityVoltage` | `setOutputMMVelocityTorqueCurrent` |
| Motion Magic Expo | `setOutputMMExpoPositionDuty` | `setOutputMMExpoPositionVoltage` | `setOutputMMExpoPositionTorqueCurrent` |
| Dynamic Motion Magic | `setOutputDynMMPositionDuty` | `setOutputDynMMPositionVoltage` | `setOutputDynMMPositionTorqueCurrent` |

All control requests use timesync (`UpdateFreqHz = 0`) and set `Slot = 0`.

**Gear shifting integration:**
`TransmissionFX.shift()` updates the TalonFX config's
`Feedback.RotorToSensorRatio` to the gear-specific ratio *before*
actuating the pneumatic. This ensures the motor's internal position
tracking stays accurate across gear changes.

**Live tuning:**
LoggedNTInput fields for Motion Magic parameters (`CruiseVelocity`,
`Acceleration`, `Jerk`) and PID gains (`kP`, `kI`, `kD`, `kV`, `kS`).
Changes are detected in `checkTuning()` and applied to hardware config.

**Constructor overloads (1–4 motors):**

```java
// Single motor
new TransmissionFX(parent, "Left", Motor.kKrakenX60, canID);

// Two motors with opposed follower
new TransmissionFX(parent, "Left", Motor.kKrakenX60,
    leaderID, followerID, /* oppose */ true);

// Three motors
new TransmissionFX(parent, "Left", Motor.kKrakenX60,
    leaderID, follower1ID, false, follower2ID, true);

// Four motors
new TransmissionFX(parent, "Left", Motor.kKrakenX60,
    leaderID, f1ID, false, f2ID, true, f3ID, false);
```

---

## Sensor Abstractions

### Encoders

```text
TObj
└── EncoderBase                 (abstract — position/velocity in rotations)
    └── EncoderCTRE             (CTRE intermediate — refresh/latency compensation)
        ├── EncoderFX           (TalonFX integrated rotor encoder)
        ├── EncoderFXComp       (FX with compensation)
        ├── EncoderCANCoder     (CTRE CANcoder absolute encoder)
        └── EncoderCANCoderComp (CANCoder with compensation)
```

**EncoderBase** defines:
- `EncoderType`: INTERNAL_FX, INTERNAL_MAX, CANCODER
- `EncoderDirection`: CW, CCW
- Position (rotations) and velocity (RPS) tracking
- Abstract simulation methods

**EncoderFX** reads the TalonFX's internal rotor position and velocity
signals. Registers signals with CTREManager during `robotInit()`.
Uses latency compensation for accurate position reads.

**EncoderCANCoder** wraps a CTRE CANcoder for absolute position sensing.
Provides both boot position and absolute position signals. Supports
magnet offset configuration and direction inversion.

### IMU

```text
TObj
└── IMUBase                     (abstract — yaw, roll, pitch)
    └── IMUCTRE                 (CTRE intermediate)
        └── IMUPigeon2          (CTRE Pigeon 2 implementation)
```

**IMUBase** tracks yaw, yaw rate, roll, and pitch. Provides
`getHeading()` returning a `Rotation2d`.

**IMUPigeon2** registers 4 signals with CTREManager (yaw, roll, pitch,
yaw rate). Yaw uses latency compensation for field-relative tracking.

### Range Sensors

```text
TObj
└── RangeBase                   (abstract — distance with scale factor)
    └── RangeCTRE
        └── RangeCANrange       (CTRE CANrange time-of-flight)
```

**RangeBase** provides raw distance plus a configurable scale factor
for unit conversion. `getDist()` returns `raw × scale`.

### Limit Switches

```text
TObj
└── SwitchBase                  (abstract — triggered state, auto-zero)
    ├── SwitchFX                (TalonFX internal switch signal)
    └── SwitchCANCoder          (CANCoder absolute position threshold)
```

**SwitchBase** defines:
- `SwitchType`: FX, CANCODER, MAX, RIO_DIO
- `SwitchTrigger`: NO (normally open), NC (normally closed)
- `autoSet` / `autoSetPos` — when triggered, automatically set encoder
  position (for homing)

---

## Input System

```text
TObj
└── Subsystem
    └── Input                   (base gamepad abstraction)
        ├── InputPS4            (PS4 controller)
        ├── InputXBox           (Xbox controller)
        ├── Input8BitDuo        (8BitDo controller)
        └── InputEnvisionPro    (EnvisionPro controller)
```

**Input** extends Subsystem (not just TObj) because it participates
in the SubsystemManager lifecycle.

**Key features:**
- Tracks raw axis, button, and POV values plus their previous values
- Connection detection with state change tracking
- Returns 0 for all axes when disconnected (safe default)
- Axes clamped to [-1.0, 1.0]

**Input shaping modes** (`InputShaping` enum):

| Mode | Transform | Curve |
|------|-----------|-------|
| NONE | `v` | No shaping |
| LINEAR | `v` | Linear (same as none) |
| SOFT | Polynomial blend | Gentle curve |
| SQUARED | `v²` × sign | Moderate precision |
| CUBED | `v³` | High precision at center |
| AGGRESSIVE | Custom polynomial | Strong center deadzone |
| MORE_AGGRESSIVE | Custom polynomial | Very strong center deadzone |
| DYNAMIC | Adaptive | Runtime-selected shaping |

Input shaping makes the joystick less sensitive near center for fine
control while preserving full range at extremes.

---

## Simulation Support

Every hardware class provides simulation hooks:

- TransmissionBase creates the appropriate `DCMotor` model based on
  motor type (Falcon500Foc, KrakenX60Foc, KrakenX44, NEO, etc.)
- `simulationInit()` lazy-initializes sim state objects
- `simulationPeriodic()` updates sim state (supply voltage from
  RobotController, position/velocity from physics models)
- Encoder sim methods (`setSimPosRotations`, `setSimVelRotations`)
  propagate to both CANCoder and motor sim states
