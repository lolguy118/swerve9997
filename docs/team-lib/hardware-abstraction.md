<!-- markdownlint-disable MD013 MD060 -->
# Hardware Abstraction Design

> **Scope:** This document covers the Team271-Lib hardware abstraction
> layer — the reusable building blocks that all robot projects share.
> Robot-specific hardware choices (CAN IDs, motor counts per mechanism,
> gear ratios, current limits) belong in each robot project's own
> design docs. This document describes *what the library provides*;
> robot docs describe *how a specific robot uses it*.

---

## Controller Hierarchy

```text
TObj
└── ControllerBase              (type system, follower validation, basic output)
    └── ControllerSmart         (current limits, voltage limits, ramping, PID slots, live tuning)
        └── ControllerTalonFX   (Phoenix 6 concrete: signals, config, sim state)
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

### ControllerSmart — Advanced Features + Live Tuning

Adds abstractions for features common to smart motor controllers:

- **Current limits:** stator (output-side) and supply (input-side),
  with optional time-based supply thresholds
- **Voltage limits:** peak forward/reverse voltage capping
- **Ramping:** open-loop and closed-loop ramp rates for duty cycle,
  voltage, and torque
- **PID by slot:** up to 3 PID slot configurations (P, I, D, V, S gains)

**Live tuning via LoggedNTInput:**

ControllerSmart creates dashboard-tunable fields that can be adjusted
at runtime without redeploying code:

| Tunable | NT Key Pattern | What It Controls |
|---------|---------------|------------------|
| `tuneStatorEnable` | `Tune/StatorEnable` | Stator current limit on/off |
| `tuneStatorLimit` | `Tune/StatorLimit` | Stator current limit (amps) |
| `tuneSupplyEnable` | `Tune/SupplyEnable` | Supply current limit on/off |
| `tuneSupplyLimit` | `Tune/SupplyLimit` | Supply current limit (amps) |
| `tuneVoltagePeakFwd` | `Tune/VoltagePeakFwd` | Peak forward voltage |
| `tuneVoltagePeakRev` | `Tune/VoltagePeakRev` | Peak reverse voltage |

Changes are detected in `checkTuning()` (called from `outputTelemetry()`)
and applied to hardware automatically. See the
[Tuning Infrastructure](#tuning-infrastructure) section for the full pattern.

### Multi-Bus Considerations

When a robot uses multiple CAN buses (RIO + one or more CANivores):

- **Timesync is per-CANivore bus.** Each CANivore maintains an
  independent time domain. The RIO CAN bus does not support timesync
  at all — timesync only works on CANivore.
- **`CTREManager.refreshAll()`** groups signals by bus and calls
  `BaseStatusSignal.refreshAll()` per group for optimization.
- **Follower validation** in `ControllerBase.follow()` enforces that
  leader and follower are on the same CAN bus, preventing cross-bus
  follower issues where the follower would silently sit at zero output.
- **Per-bus timestamps:** `CTREManager.getDt()` uses the first signal's
  timestamp, which may come from any bus. Robot projects that need
  per-bus timing accuracy for multi-bus latency compensation should
  track timestamps separately.

### ControllerTalonFX — Phoenix 6 Implementation

The concrete TalonFX implementation.

**Signal filtering:** An `EnumSet<Signals>` controls which StatusSignals
are registered with CTREManager. Defaults to `ALL`, but can be restricted
to reduce CAN bus load for follower motors that don't need every signal.

Available signal flags: `SUPPLY_VOLT`, `SUPPLY_CURRENT`, `OUTPUT_DUTY`,
`OUTPUT_VOLT`, `OUTPUT_TORQUE_CURRENT`, `LIMIT_SW_FWD`, `LIMIT_SW_REV`,
`CLOSED_LOOP_ERROR`, `CLOSED_LOOP_OUTPUT`, `ALL`, `NONE`.

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
    └── TransmissionFX          (TalonFX-specific: Motion Magic, dynamic MM, live tuning)
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

## Tuning Infrastructure

The library's tuning infrastructure (`LoggedNTInput`, `checkTuning()` pattern,
and workflow) is documented in
[Library Architecture — Tuning Infrastructure](library-architecture.md#tuning-infrastructure).
This section covers only the hardware-specific tunable inventories.

### TransmissionFX Tunables

| Tunable | NT Key | Controls |
|---------|--------|----------|
| `tuneMMCruiseVel` | `Tune/MMCruiseVel` | Motion Magic cruise velocity (RPS) |
| `tuneMMAccel` | `Tune/MMAccel` | Motion Magic acceleration (RPS/s) |
| `tuneMMJerk` | `Tune/MMJerk` | Motion Magic jerk (RPS/s²) |
| `tunePIDkP` | `Tune/PID_kP` | PID proportional gain |
| `tunePIDkI` | `Tune/PID_kI` | PID integral gain |
| `tunePIDkD` | `Tune/PID_kD` | PID derivative gain |
| `tunePIDkV` | `Tune/PID_kV` | Velocity feedforward |
| `tunePIDkS` | `Tune/PID_kS` | Static feedforward |

When any tunable changes, `checkTuning()` applies the new values:
- MM parameters → `setMMConfig(cruiseVel, accel, jerk)` → updates config
- PID gains → `configPIDFSlot(0, P, I, D, V, S)` → applies to hardware

### ControllerSmart Tunables

Current limits and voltage limits are tunable at the controller level.
See the [ControllerSmart section](#controllersmart--advanced-features--live-tuning) above.

---

## Simulation Support

The simulation architecture (CTRE SimState, WPILib DCMotor models,
lifecycle, and position/velocity propagation) is documented in
[Library Architecture — Simulation Architecture](library-architecture.md#simulation-architecture).
Each sensor and controller section above notes its specific SimState
initialization.

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
- Abstract simulation methods (`setSimPosRotations`, `setSimVelRotations`,
  `simulationInit`, `simulationPeriodic`)

**EncoderFX** reads the TalonFX's internal rotor position and velocity
signals. Registers signals with CTREManager during `robotInit()`.
Uses latency compensation for accurate position reads. Simulation
writes go through the controller's `TalonFXSimState`.

**EncoderCANCoder** wraps a CTRE CANcoder for absolute position sensing.
Provides both boot position and absolute position signals. Supports
magnet offset configuration and direction inversion. Has its own
`CANcoderSimState` initialized in `create()` with orientation set
based on encoder direction.

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
Has a `Pigeon2SimState` for simulation — robot projects can set
simulated yaw via the sim state.

### Range Sensors

```text
TObj
└── RangeBase                   (abstract — distance with scale factor)
    └── RangeCTRE
        └── RangeCANrange       (CTRE CANrange time-of-flight)
```

**RangeBase** provides raw distance plus a configurable scale factor
for unit conversion. `getDist()` returns `raw × scale`.

**RangeCANrange** has a `CANrangeSimState` for simulation — robot
projects can set simulated distance values.

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
