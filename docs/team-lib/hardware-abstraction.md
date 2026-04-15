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

- **Current limits:** stator (output-side) and supply (input-side).
  Supply limits have two overloads: simple (enable + limit) and
  time-based (`setCurrentLimitSupply(limit, time, lowerLimit)`) which
  reduces to `lowerLimit` if current exceeds `limit` for `time`
  seconds — useful for allowing brief current spikes during
  acceleration while protecting sustained draw
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

**Follower signal optimization:** Use `Signals.NONE` for follower
motors that do not need individual telemetry. This prevents the
follower from registering any StatusSignals with CTREManager, reducing
CAN bus traffic. The follower still receives control frames from the
leader — signal filtering only affects status reporting.

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

**Slot 0 hardcoding:** All control requests at the ControllerTalonFX
level use `Slot = 0`. TransmissionFX also hardcodes Slot 0 for all
its control requests. To use different PID gains, reconfigure Slot 0
at runtime via `setPIDFSlot()` or `checkTuning()`. See the
[Not Yet Implemented](#phoenix-6-features-not-yet-implemented)
section for planned runtime slot selection.

**Controller vs. TransmissionFX control scope:** ControllerTalonFX
exposes `setOutputPosition()` and `setOutputVelocity()` using only
the Voltage output type (`PositionVoltage`, `VelocityVoltage`).
For DutyCycle, TorqueCurrent, and all Motion Magic variants, use
TransmissionFX which provides the
[full control mode matrix](#supported-control-modes).

**Timesync configuration:** `setControlUpdateFrequency()` switches
between timesync mode (for CANivore buses) and standard update mode
(for RIO CAN). In timesync mode, all control request objects are set
to `UseTimesync = true` and `UpdateFreqHz = 0`. In standard mode,
they use the specified update frequency without timesync.

**Config application:** `applyConfig()` retries up to
`CAN_CONFIG_APPLY_RETRIES` (3) times with `CAN_CONFIG_APPLY_TIMEOUT_SEC`
(20 ms) per attempt — 60 ms worst case, within the 20 ms loop budget.
Sets `isConfigured` flag on success/failure.

**Connection tracking:** `robotPeriodicBefore()` checks
`talonFX.isConnected()` every cycle to detect CAN disconnection.

### Accessing Underlying Hardware

Every wrapper class exposes its underlying CTRE object via public
getters. This is the **Passthrough Principle** — the library adds
value without blocking access. See
[Passthrough Design](passthrough-design.md) for the full reference.

**Controller passthrough:**

```java
// Library convenience API
controller.setNeutralMode(NeutralState.BRAKE);
controller.setCurrentLimitStator(true, 80);
controller.applyConfig();

// Direct CTRE access — for features the library doesn't wrap
TalonFX talon = controller.getTalonFX();
TalonFXConfiguration config = controller.getConfig();
config.Audio.BeepOnBoot = false;
config.SoftwareLimitSwitch.ForwardSoftLimitEnable = true;
controller.applyConfig();  // library's multi-retry apply
```

**Transmission passthrough:**

```java
// Library convenience — applies to all motors
transmission.configCurrentLimitStator(true, 80);

// Direct CTRE access — for leader-specific config
TalonFX leader = transmission.getLeader();
TalonFXConfiguration leaderConfig = transmission.getLeaderConfig();
leaderConfig.Slot0.kG = 0.3;
leaderConfig.Slot0.GravityType = GravityTypeValue.Arm_Cosine;
transmission.applyConfigs();

// Use a control mode the library doesn't wrap
leader.setControl(new Follower(otherID, false));
```

---

## CTRE Phoenix 6 Feature Coverage

This section is the **authoritative reference** for which Phoenix 6
features the library supports. Other docs link here rather than
duplicating feature lists.

### Supported Devices

| Device | Wrapper Class | Sim State | Registered Signals |
|--------|--------------|-----------|-------------------|
| TalonFX | `ControllerTalonFX` | `TalonFXSimState` | 9 signal types via `Signals` enum (see [Signal Filtering](#controllertalonfx--phoenix-6-implementation)) |
| CANCoder | `EncoderCANCoder` | `CANcoderSimState` | Position, velocity, absolute position, position-since-boot |
| Pigeon 2 | `IMUPigeon2` | `Pigeon2SimState` | Yaw, roll, pitch, yaw rate (yaw uses latency compensation) |
| CANrange | `RangeCANrange` | `CANrangeSimState` | Distance (FOV configurable) |

> **Status: Planned — Not Yet Implemented.**
>
> **CANdi** — `CTREManager.addSignalCANdi()` exists for signal
> registration, but no device wrapper class has been created.
>
> **TalonFXS** — `ControllerType.TALONFXS` exists in the enum, but
> no controller implementation has been created.

### Supported Control Modes

All control requests use timesync (`UseTimesync = true`,
`UpdateFreqHz = 0`) for CANivore frame synchronization.

| Category | Control Request | Available In | Output Type |
|----------|----------------|-------------|-------------|
| **Open Loop** | `DutyCycleOut` | ControllerTalonFX, TransmissionFX | Duty cycle [-1, 1] |
| | `VoltageOut` | ControllerTalonFX, TransmissionFX | Voltage |
| | `TorqueCurrentFOC` | ControllerTalonFX, TransmissionFX | Torque current (amps) |
| **Position** | `PositionVoltage` | ControllerTalonFX, TransmissionFX | Voltage + feedforward |
| | `PositionDutyCycle` | TransmissionFX | Duty cycle + feedforward |
| | `PositionTorqueCurrentFOC` | TransmissionFX | Torque current + feedforward |
| **Velocity** | `VelocityVoltage` | ControllerTalonFX, TransmissionFX | Voltage + feedforward |
| | `VelocityDutyCycle` | TransmissionFX | Duty cycle + feedforward |
| | `VelocityTorqueCurrentFOC` | TransmissionFX | Torque current + feedforward |
| **Motion Magic Position** | `MotionMagicDutyCycle` | TransmissionFX | Trapezoidal profile, duty cycle |
| | `MotionMagicVoltage` | TransmissionFX | Trapezoidal profile, voltage |
| | `MotionMagicTorqueCurrentFOC` | TransmissionFX | Trapezoidal profile, torque current |
| **Motion Magic Velocity** | `MotionMagicVelocityDutyCycle` | TransmissionFX | Velocity trapezoidal, duty cycle |
| | `MotionMagicVelocityVoltage` | TransmissionFX | Velocity trapezoidal, voltage |
| | `MotionMagicVelocityTorqueCurrentFOC` | TransmissionFX | Velocity trapezoidal, torque current |
| **Motion Magic Expo** | `MotionMagicExpoDutyCycle` | TransmissionFX | Exponential (S-curve), duty cycle |
| | `MotionMagicExpoVoltage` | TransmissionFX | Exponential (S-curve), voltage |
| | `MotionMagicExpoTorqueCurrentFOC` | TransmissionFX | Exponential (S-curve), torque current |
| **Dynamic Motion Magic** | `DynamicMotionMagicDutyCycle` | TransmissionFX | Real-time vel/accel/jerk, duty cycle |
| | `DynamicMotionMagicVoltage` | TransmissionFX | Real-time vel/accel/jerk, voltage |
| | `DynamicMotionMagicTorqueCurrentFOC` | TransmissionFX | Real-time vel/accel/jerk, torque current |
| **Follower** | `Follower` | ControllerTalonFX | Aligned or opposed via `MotorAlignmentValue` |
| **Neutral** | `NeutralOut` | ControllerTalonFX, TransmissionFX | Brake stop |

**Note:** ControllerTalonFX exposes `PositionVoltage` and
`VelocityVoltage` directly. For other position/velocity output types
(DutyCycle, TorqueCurrent) and all Motion Magic variants, use
TransmissionFX which provides the full control mode matrix.

### Supported Configuration

| Config Area | Phoenix 6 Config Class | Library Method(s) | Notes |
|------------|----------------------|-------------------|-------|
| Current limits (stator) | `CurrentLimitsConfigs` | `setCurrentLimitStator()` | Enable/disable + limit amps |
| Current limits (supply) | `CurrentLimitsConfigs` | `setCurrentLimitSupply()` | Simple or time-based with lower limit |
| Voltage limits | `VoltageConfigs` | `setVoltagePeak()` | Peak fwd/rev voltage + supply time constant |
| Open-loop ramping | `OpenLoopRampsConfigs` | `setRampOpenLoopDuty/Voltage/Torque()` | Seconds from 0 to full output |
| Closed-loop ramping | `ClosedLoopRampsConfigs` | `setRampClosedLoopDuty/Voltage/Torque()` | Seconds from 0 to full output |
| PID gains (Slot 0/1/2) | `Slot0Configs` etc. | `setPSlot()`, `setISlot()`, `setDSlot()`, `setPIDFSlot()` | kP, kI, kD, kV, kS per slot |
| Neutral mode | `MotorOutputConfigs` | `setNeutralMode()` | Brake or Coast |
| Motor direction | `MotorOutputConfigs` | `setDirection()` | CW or CCW positive |
| Control timesync | `MotorOutputConfigs` | `setControlUpdateFrequency()` | Timesync freq Hz or standard update freq |
| Motion Magic params | `MotionMagicConfigs` | `setMMConfig()` | Cruise velocity, acceleration, jerk |
| Hardware limit switches | `HardwareLimitSwitchConfigs` | Via `SwitchFX` creation | NO/NC, enable, auto-zero position |
| Feedback sensor | `FeedbackConfigs` | `TransmissionFX.shift()` | RotorToSensorRatio for gear changes |

### Supported TalonFX Signals

| `Signals` Flag | StatusSignal | Update Freq | Purpose |
|----------------|-------------|-------------|---------|
| `SUPPLY_VOLT` | `getSupplyVoltage()` | 250 Hz | Battery voltage at controller |
| `SUPPLY_CURRENT` | `getSupplyCurrent()` | 250 Hz | Current draw from battery |
| `OUTPUT_DUTY` | `getDutyCycle()` | 250 Hz | Applied duty cycle |
| `OUTPUT_VOLT` | `getMotorVoltage()` | 250 Hz | Applied voltage |
| `OUTPUT_TORQUE_CURRENT` | `getTorqueCurrent()` | 250 Hz | Applied torque current |
| `LIMIT_SW_FWD` | (via `SwitchFX`) | 250 Hz | Forward limit switch state |
| `LIMIT_SW_REV` | (via `SwitchFX`) | 250 Hz | Reverse limit switch state |
| `CLOSED_LOOP_ERROR` | `getClosedLoopError()` | 250 Hz | Hardware PID error |
| `CLOSED_LOOP_OUTPUT` | `getClosedLoopOutput()` | 250 Hz | Hardware PID output |
| (always) | `getMotorOutputStatus()` | 250 Hz | Motor state (Motoring, Braking, etc.) |
| (always) | `getStickyFault_BootDuringEnable()` | 250 Hz | Boot-during-enable fault |

Other device signal frequencies: CANCoder 250 Hz, Pigeon2 250 Hz,
CANrange 100 Hz.

### Phoenix 6 Features Not Yet Implemented

> **Status: Planned — Not Yet Implemented.**
>
> The following Phoenix 6 v26 features are available in the CTRE API
> but not yet wrapped by the library. Each represents a potential
> future enhancement.

| Feature | Phoenix 6 API | Impact | Notes |
|---------|--------------|--------|-------|
| **kG gravity feedforward** | `Slot0Configs.kG`, `GravityTypeValue` | High — arm/elevator feedforward at 1 kHz | `setPIDFSlot()` accepts P, I, D, V, S but not G |
| **ContinuousWrap** | `ClosedLoopGeneralConfigs.ContinuousWrap` | High — swerve azimuth, turrets | Enables hardware error wrapping for continuous rotation |
| **Software limit switches** | `SoftwareLimitSwitchConfigs` | Medium — position-based safety limits | Only hardware limit switches are currently supported |
| **Runtime slot selection** | Control request `.Slot` field | Medium — multi-profile PID | All control requests hardcode `.Slot = 0` |
| **Device temperature** | `getDeviceTemp()` | Medium — thermal protection | Signal exists but not registered or published |
| **Acceleration signal** | `getAcceleration()` | Low — diagnostics | Signal exists but not registered |
| **Expanded fault monitoring** | `getFault_*()`, `getStickyFault_*()` | High — driver visibility | Only `BootDuringEnable` monitored; see [Fault Tolerance — CTRE Fault Coverage](fault-tolerance.md#ctre-fault-coverage) |
| **Fault telemetry** | Fault signals → NT | High — dashboard diagnostics | No faults published to NetworkTables |
| **Differential control** | `DifferentialControl` modes | Low — coupled motors | Beyond simple Follower synchronization |
| **Orchestra** | `Orchestra` class | Low — entertainment | Music playback through motors |
| **Audio/beep configs** | `AudioConfigs` | Low — operator feedback | Startup/error beep configuration |
| **CANdi device** | `CANdi` class | Medium — current distribution | `addSignalCANdi()` ready in CTREManager |
| **TalonFXS controller** | `TalonFXS` class | Medium — brushed motor control | `ControllerType.TALONFXS` enum exists |

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

**Encoder system (EncoderAdapter — single source of truth):**

| Encoder Type | Source | Purpose |
|-------------|--------|---------|
| `EncoderFX` | TalonFX integrated rotor | Always available, high-speed |
| `EncoderCANCoder` | External CANcoder | Absolute position, mechanism-side |

The `EncoderAdapter` interface is the **single source of truth** for
all encoder access. TransmissionBase holds one `EncoderAdapter encoder`
field. When both FX and CANCoder are configured, CANCoder takes priority.

To access the underlying encoder when needed, use typed downcasts:

```java
Optional<EncoderFX> fx = transmission.getEncoderFX();
Optional<EncoderCANCoder> cancoder = transmission.getEncoderCANCoder();

// Access raw CANcoder for advanced features
cancoder.ifPresent(enc -> {
    CANcoder raw = enc.getCANcoder();
    // use raw CTRE CANcoder directly
});
```

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
- `ShifterState`: GEAR_NONE (uninitialized), GEAR_1, GEAR_2
- `shift(GEAR_NONE)` is rejected with a warning — GEAR_NONE is not
  a valid shift target
- Per-gear sensor ratios (`sensorRatioGear1`, `sensorRatioGear2`)
- `NO_SOLENOID_CHANNEL` sentinel for `addShifter()` when no solenoid
  is configured

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

All control requests default to timesync (`UpdateFreqHz = 0`) and
`Slot = 0`. Use `configTimesync(false, 250.0)` to disable timesync
for robots using the RIO CAN bus instead of a CANivore.

**Control request management:** TransmissionFX pre-allocates all 23
control request objects at construction to avoid GC pressure during
match play. These are stored in both named fields (for direct use)
and an `allRequests` array (for bulk operations). The
`configTimesync()` method loops over `allRequests` to apply
timesync settings uniformly, rather than updating each field
individually.

For control modes not wrapped by TransmissionFX, use the passthrough:

```java
// Use a CTRE control mode the library doesn't wrap
var req = new StrictFollower(otherMotorID);
transmission.getLeader().setControl(req);
```

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
        ├── EncoderCANCoder     (CTRE CANcoder absolute encoder)
        └── EncoderCANCoderComp (CANCoder with latency compensation)

EncoderAdapter (interface)        ← strategy interface for unified access
├── FXEncoderAdapter              (wraps EncoderFX + GearRatio)
└── CANCoderAdapter               (wraps EncoderCANCoder + GearRatio)
```

#### Encoder Adapter Pattern

`TransmissionBase` holds a single `EncoderAdapter encoder` reference
instead of separate `encFX` and `encCANCoder` fields. The adapter
applies gear ratio conversions internally, so callers get mechanism
output units directly:

- `encoder.getPosition()` — position in mechanism output units
- `encoder.getVelocity()` — velocity in mechanism output units/sec
- `encoder.mechanismToNative(units)` — convert for closed-loop setpoints
- `encoder.getAbsolutePosition()` — absolute position (CANCoder only; FX returns 0)
- `encoder.updateGearRatio(newRatio)` — swap ratio on gear shift

The adapter is created automatically by `addEncoderFX()` or
`addCANCoder()`. Adding a new encoder type requires only implementing
`EncoderAdapter` — no changes to TransmissionBase or TransmissionFX.

#### GearRatio Value Object

`GearRatio` is an immutable validated object replacing four raw doubles:

- `rotorToMechanism` — rotor rotations to mechanism rotations (FX path)
- `sensorRelToMechanism` — sensor rotations to mechanism (CANCoder path)
- `sensorAbsToMechanism` — absolute sensor to mechanism (CANCoder abs)
- `mechanismToUnits` — mechanism rotations to output units

Provides named conversion methods (`rotorToOutput`, `outputToRotor`,
`sensorRelToOutput`, `outputToSensorRel`, `sensorAbsToOutput`) and
builder methods for gear shifts (`withRotorToMechanism`). Validates
that no ratio is zero at construction time.

#### Encoder Classes

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
`getHeading()` returning a `Rotation2d` from the cached yaw value.

**IMUPigeon2** registers 4 signals with CTREManager (yaw, roll, pitch,
yaw rate). `robotPeriodicBefore()` calls `refresh()` each cycle to
update cached values. Yaw uses latency compensation via
`BaseStatusSignal.getLatencyCompensatedValue()`. `getHeading()` uses
the latency-compensated yaw — it does not bypass the refresh path.
Has a `Pigeon2SimState` for simulation and a `FaultMonitor` tracking
6 sticky faults.

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
- `getTriggered()` — abstract, all subclasses must implement
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
- Edge detection: `getButtonPressed()` / `getButtonReleased()` for
  single-cycle transitions, `getButton()` for held state
- Connection detection with Elastic notification on disconnect
- Returns 0 for all axes when disconnected (safe default)
- Axes clamped to [-1.0, 1.0]; raw values passed without inversion
  (robot-specific subclasses apply axis inversion per controller mapping)

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
