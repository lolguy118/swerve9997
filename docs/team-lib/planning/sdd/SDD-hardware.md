# SDD: `com.team271.lib.hardware` — Hardware Lifecycle Wrappers

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-HARDWARE |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | HW-001 through HW-NNN (SRS §4.3) |

## 1. Purpose

Provides TObj-lifecycle wrappers around CTRE devices: the controller
hierarchy, transmission (multi-motor) coordination, sensor abstractions,
and input devices. Also contains `CTREManager` (bulk refresh orchestration)
and `HardwareManager` (the forward-compatible refresh entry point).

## 2. Scope and Boundaries

**This SDD covers:**

- **Controllers:** `ControllerBase`, `ControllerSmart`, `ControllerTalonFX`,
  `ControllerTalonFXS` — CTRE-facing lifecycle wrappers
- **Transmissions:** `TransmissionBase`, `TransmissionFX`, `TransmissionFXS`
  — multi-motor coordination
- **Sensors:** encoder wrappers, IMU wrappers, range sensor wrappers,
  limit switch wrappers
- **Input:** `Input` base + joystick devices
- `CTREManager` — centralized CAN bus refresh, optimization, timesync, logging
- `HardwareManager` — forward-compatible `refreshAll()` entry point
- `CANBus`, `CANDeviceID` — CAN topology helpers
- `FaultMonitor` — sticky-fault tracking per device

**This SDD does not cover:**

- CTRE Phoenix 6 implementations → [SDD-vendor-ctre.md](SDD-vendor-ctre.md)
- api/ interfaces → [SDD-api.md](SDD-api.md)

## 3. Module Decomposition

### 3.1 Controller Hierarchy

```text
TObj
└── ControllerBase            identity, open-loop output, neutral mode,
    │                         direction, follower links, sim hooks
    └── ControllerSmart       current + voltage limits, ramping, PID slots,
        │                     gravity type, continuous wrap, software limits,
        │                     closed-loop position/velocity with slot selection
        ├── ControllerTalonFX  Phoenix 6 TalonFX: brushless, FOC, all modes
        └── ControllerTalonFXS Phoenix 6 TalonFXS: brushed motors, non-FOC subset

Value objects:
  CANDeviceID       bus name + device number
  CANBus            CAN bus wrapper (RIO or CANivore)
  CurrentLimitConfig   immutable stator + supply config
  PIDGains          kP/kI/kD/kV/kS/kG/kA (record)
  GravityType       ARM_COSINE, ELEVATOR_STATIC
```

The controller hierarchy is **CTRE-facing only** — these classes do
not implement `api/` interfaces. The vendor-neutral bridge is
`CTREMotor` in `vendor/ctre/` (see
[SDD-vendor-ctre.md](SDD-vendor-ctre.md)). `ControllerBase` provides
the enums (`ControllerType`, `ControllerStatus`, `NeutralState`,
`MotorDirection`) and abstract output methods. `ControllerSmart`
adds smart-controller features plus live tuning via `LoggedNTInput`
for current and voltage limits. `ControllerTalonFX` pre-allocates
its control request objects (`DutyCycleOut`, `VoltageOut`,
`TorqueCurrentFOC`, `PositionVoltage`, `VelocityVoltage`,
`NeutralOut`) and manages signal filtering via an
`EnumSet<Signals>`.

Each controller wraps **exactly one** CTRE device. Duplicate
`CANDeviceID` use within a robot is forbidden per
[.claude/rules/safety.md](../../../../.claude/rules/safety.md); pass the
existing controller reference instead of constructing a new one.

### 3.2 Transmission Architecture

```text
TObj
└── TransmissionBase        multi-motor (1 leader + up to 3 followers),
    │                       encoder selection, shifter support, gear ratios,
    │                       DCMotor model, unit conversion, input validation
    └── TransmissionFX      TalonFX-specific control mode matrix:
                            Position/Velocity × DutyCycle/Voltage/TorqueCurrent
                            + Motion Magic (static, dynamic, expo) variants
```

`TransmissionBase` manages up to four motors in a `LinkedHashSet`
(for deterministic iteration). Configuration calls
(`configCurrentLimitStator`, `configVoltagePeak`, `configRamp*`)
apply to **all** motors; PID, direction, and control-mode calls
apply to the **leader only**. Followers are established via
`ControllerBase.follow()` which validates that leader and follower
share the same CAN bus — cross-bus follow attempts return
`ERROR_INVALID_BUS` because the follower would otherwise sit idle
at zero output with no error indication.

The encoder subsystem uses an `EncoderAdapter` interface as the
single source of truth: `TransmissionBase` holds one adapter
reference, regardless of whether the backing encoder is an
`EncoderFX` (TalonFX integrated rotor) or `EncoderCANCoder`
(external CANcoder). Gear-ratio conversion is performed inside the
adapter via a `GearRatio` value object (immutable, validated to
reject zero ratios).

`TransmissionFX` pre-allocates all 20 control request objects at
construction to avoid GC pressure during match play, and stores
`timesyncApplicators` lambdas for bulk timesync reconfiguration.
All closed-loop output methods guard against NaN/Infinity inputs
via `hasInvalidInput()` before sending to hardware.

### 3.3 Sensor Abstractions

```text
TObj
├── EncoderBase                 position (rotations) + velocity (RPS)
│   └── EncoderCTRE             CTRE intermediate — refresh + latency compensation
│       ├── EncoderFX           TalonFX integrated rotor
│       ├── EncoderCANCoder     CANcoder absolute
│       └── EncoderCANCoderComp CANcoder with latency compensation
│
├── IMUBase                     yaw, roll, pitch, yaw rate
│   └── IMUCTRE
│       └── IMUPigeon2          CTRE Pigeon 2
│
├── RangeBase                   distance with scale factor
│   └── RangeCTRE
│       └── RangeCANrange       CTRE CANrange time-of-flight
│
└── SwitchBase                  triggered state + optional auto-zero
    ├── SwitchFX                TalonFX internal limit
    ├── SwitchCANCoder          CANcoder absolute position threshold
    └── SwitchDIO               RoboRIO DIO pin
```

Every sensor wrapper registers its StatusSignals with `CTREManager`
during `robotInit()` at the appropriate update frequency — see
§8.3 (StatusSignal Update Frequencies) below and `ConstantsLib` for
the concrete frequency constants. Each `robotPeriodicBefore()`
simply reads the cached value that `CTREManager.refreshAll()` has
already updated; individual `refresh()` calls on StatusSignals are
avoided because they defeat bulk refresh optimization. `IMUPigeon2`
uses `BaseStatusSignal.getLatencyCompensatedValue()` for yaw reads.
`IMUPigeon2` also owns a `FaultMonitor` tracking 6 sticky faults
(see [§3.7 FaultMonitor](SDD-hardware.md) above).

The encoder adapter pattern (`EncoderAdapter`, `FXEncoderAdapter`,
`CANCoderAdapter`) means adding a new encoder type requires only a
new adapter implementation — `TransmissionBase` code does not
change.

### 3.4 Input System

```text
TObj
└── Subsystem
    └── Input                   base gamepad abstraction
        ├── InputPS4
        ├── InputXBox
        ├── Input8BitDuo
        └── InputEnvisionPro
```

`Input` extends `Subsystem` (not just `TObj`) because it
participates in the SubsystemManager lifecycle — its
`robotPeriodicBefore` caches raw axis / button / POV values so all
downstream reads within a cycle see consistent values. It provides
edge detection (`getButtonPressed`, `getButtonReleased`) and
returns zero for all axes when the controller is disconnected
(safe default). Axis values are clamped to [-1.0, 1.0]; robot
project subclasses apply axis inversion per controller mapping.

Input shaping modes (`NONE`, `LINEAR`, `SOFT`, `SQUARED`, `CUBED`,
`AGGRESSIVE`, `MORE_AGGRESSIVE`, `DYNAMIC`) make joysticks less
sensitive near center. See
[input-shaping-guide.md](../../guides/input-shaping-guide.md)
for selection criteria.

### 3.5 `CTREManager` — Centralized CAN Refresh

Static singleton that owns all CTRE devices and StatusSignals.
Lifecycle:

1. **Registration** (during `robotInit()`) — each hardware wrapper
   calls `addSignalTalonFX(signal, freqHz)`, `addSignalCANCoder`,
   `addSignalPigeon`, or `addSignalCANrange`. The internal helper
   validates the signal's `StatusCode`, adds it to the global list,
   and sets its update frequency. `addSignalCANdi` exists for future
   CANdi support.
2. **Initialization** (`CTREManager.init()`) — converts the
   `ArrayList<StatusSignal<?>>` to a primitive array for efficient
   bulk refresh, runs `ParentDevice.optimizeBusUtilizationForAll()`
   per CAN bus, and starts `SignalLogger` auto-logging if any
   CANivore bus is registered (hoot files written to `/U/logs`).
3. **Refresh** (every cycle) — `refreshAll()` calls
   `BaseStatusSignal.refreshAll()` on the entire signal array in a
   single pass, tracks timestamps for `getDt()`, and on failure
   sends a throttled Elastic notification (throttle interval
   defined in `ConstantsLib`, global). Returns `StatusCode`.
4. **Teardown** — `stopLogging()` stops `SignalLogger` on CANivore
   buses. `resetForTesting()` clears all internal state for unit
   tests without reflection.

Devices are tracked in two structures: a global list and a
`LinkedHashMap<String, ArrayList<ParentDevice>>` keyed by bus name
for per-bus optimization. **All registration must complete before
`init()`** — signals added after `init()` are not included in the
refresh array.

### 3.6 `HardwareManager`

Forward-compatible entry point for `refreshAll()`. Today it
delegates to `CTREManager.refreshAll()`; in the future it will also
refresh non-CTRE devices that implement `SignalRefreshable`. Per
[.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md),
robot startup code should call `HardwareManager.refreshAll()` not
`CTREManager.refreshAll()` directly.

### 3.7 `FaultMonitor`

Reusable sticky-fault tracker used by each CTRE device wrapper.
Registers named fault signals with `CTREManager`, refreshes them
each cycle, publishes per-fault booleans plus a summary `Has Fault`
boolean to NetworkTables, and raises a persistent `Alert` in the
"Faults" group for each active fault. Per-device fault inventories
are listed in
§3.7 FaultMonitor above.

### 3.8 Passthrough Getter Reference

The table below lists hardware-layer passthrough getters. For
vendor-layer wrappers (`CTREMotor`, `CTREEncoder`, `CTREGyro`,
`CTRERangeSensor`), see
[SDD-vendor-ctre.md §6 Passthrough Getter Reference](SDD-vendor-ctre.md).
The two tables are complementary — this one covers
`Controller*`/`Transmission*` and direct sensor wrappers; the other
covers the api/-implementing `CTRE*` classes.

| Class | Method | Returns | Notes |
| ----- | ------ | ------- | ----- |
| `ControllerTalonFX` | `getTalonFX()` | `TalonFX` | Raw CTRE motor controller |
| `ControllerTalonFX` | `getConfig()` | `TalonFXConfiguration` | Deferred config; modify then `applyConfig()` |
| `ControllerTalonFX` | `getSimState()` | `TalonFXSimState` | Simulation proxy |
| `ControllerTalonFX` | `getConfigMM()` | `MotionMagicConfigs` | Shortcut into `config.MotionMagic` |
| `ControllerTalonFXS` | `getTalonFXS()` | `TalonFXS` | Raw brushed-motor controller |
| `ControllerTalonFXS` | `getConfig()` | `TalonFXSConfiguration` | Deferred config |
| `ControllerTalonFXS` | `getSimState()` | `TalonFXSSimState` | Simulation proxy |
| `TransmissionFX` | `getLeader()` | `TalonFX` | Leader's raw CTRE object |
| `TransmissionFX` | `getLeaderConfig()` | `TalonFXConfiguration` | Leader's deferred config |
| `TransmissionFX` | `getLeaderController()` | `ControllerTalonFX` | Leader's library wrapper |
| `TransmissionFX` | `getSimState()` | `TalonFXSimState` | Leader's simulation proxy |
| `TransmissionFX` | `getAllControllers()` | `Set<ControllerSmart>` | All motors (leader + followers) |
| `EncoderCANCoder` | `getCANcoder()` | `CANcoder` | Raw CANcoder device |
| `EncoderCANCoder` | `getConfig()` | `CANcoderConfiguration` | Deferred config |
| `EncoderCANCoder` | `getSimState()` | `CANcoderSimState` | Simulation proxy |
| `IMUPigeon2` | `getPigeon2()` | `Pigeon2` | Raw Pigeon2 IMU |
| `IMUPigeon2` | `getConfig()` | `Pigeon2Configuration` | Deferred config |
| `IMUPigeon2` | `getSimState()` | `Pigeon2SimState` | Simulation proxy |
| `RangeCANrange` | `getCANrange()` | `CANrange` | Raw CANrange sensor |
| `RangeCANrange` | `getConfig()` | `CANrangeConfiguration` | Deferred config |

## 4. Data Flow

```text
// Init — signal registration
Robot.robotInit()
  → SubsystemManager.robotInit()
    → forEachSafe(s -> s.robotInit(t))          // each subsystem registers signals
      → ControllerTalonFX.robotInit()
        → CTREManager.addSignalTalonFX(position, kTalonStatusFreqHz)
        → CTREManager.addSignalTalonFX(velocity, kTalonStatusFreqHz)
        → FaultMonitor.registerSignals()
      → EncoderCANCoder.create() → CTREManager.addSignalCANCoder(...)
      → IMUPigeon2.create() → CTREManager.addSignalPigeon(...)
  → CTREManager.init()                          // array build + bus optimization
    → ParentDevice.optimizeBusUtilizationForAll() per bus
    → SignalLogger.start() (if CANivore present)

// Periodic cycle
Robot.robotPeriodic() @ 50 Hz
  → HardwareManager.refreshAll()                // → CTREManager.refreshAll()
    → BaseStatusSignal.refreshAll(signalArray)  // one CAN round-trip
    → sensor cached values updated atomically
  → SubsystemManager.robotPeriodicBefore()
    → ControllerTalonFX.robotPeriodicBefore()
      → isConnected = talonFX.isConnected()
      → FaultMonitor.refresh()
    → encoder / IMU / range wrappers: no-op (cache already fresh)
  → SubsystemManager.<mode>Periodic()
  → SubsystemManager.robotPeriodicAfter()
    → TransmissionFX.setOutput*() → ControllerTalonFX → CTREMotor → TalonFX
      → guards on isConnected + hasInvalidInput
  → SubsystemManager.outputTelemetry()
    → each wrapper publishes via NTEntry + Logger
    → checkTuning() applies LoggedNTInput changes
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Centralized bulk refresh | Consistent timestamps, lower bus utilization, simpler latency compensation | [ADR-007](../adr/ADR-007-centralized-can-refresh.md) |
| `Controller*` classes do not implement api/ | Keeps controller hierarchy CTRE-focused; `CTREMotor` is the api/ bridge | [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md) |
| No duplicate CAN IDs | Construct each device once, pass reference | [.claude/rules/safety.md](../../../../.claude/rules/safety.md) |
| `HardwareManager` forward-compatible | Single entry point even if vendors expand | [SDD-vendor-ctre.md](SDD-vendor-ctre.md) |
| Encoder adapter pattern | Swap encoder types without touching TransmissionBase | See §3.3 above |
| Follower same-bus validation | Cross-bus followers silently sit at zero output; catch at construction | See §3.2 (Transmission Architecture) above |
| Pre-allocated control request objects | Avoid GC pressure during match play | [CODE-GEN-004](../../../common/Team271-Software-Coding-Standard-General.md) |
| NaN/Infinity input guards | Bad sensor readings should skip one cycle, not disable the subsystem | See §6 Error Handling above |

## 6. Error Handling

- **CAN disconnect** — `ControllerTalonFX.robotPeriodicBefore()`
  updates the `isConnected` flag from `talonFX.isConnected()` each
  cycle. Output methods guard on this flag — no control frames are
  sent to a disconnected device.
- **Signal refresh errors** — `CTREManager.refreshAll()` returns an
  aggregate `StatusCode`. Non-OK codes produce a throttled Elastic
  notification (throttle interval in `ConstantsLib`). The loop
  continues with stale cached values rather than aborting.
- **Config apply retries** — `ControllerTalonFX.applyConfig()`
  retries up to `CAN_CONFIG_APPLY_RETRIES` times with a per-attempt
  timeout (`CAN_CONFIG_APPLY_TIMEOUT_SEC`). Persistent failure sets
  `isConfigured = false`; the motor runs with Phoenix 6 factory
  defaults, which include safe current limits. Config error
  notifications are throttled via
  `lastConfigErrorNotificationTime`.
- **Follower bus mismatch** — `ControllerBase.follow()` returns
  `ERROR_INVALID_BUS` if leader and follower are on different CAN
  buses. Callers must check the return value; the library does not
  silently continue.
- **Input validation** — `TransmissionFX.hasInvalidInput()` checks
  every closed-loop parameter for NaN / Infinity / -Infinity. On
  detection it logs a throttled DriverStation warning, returns
  without sending a control request, and the motor holds its last
  command. `STRICT_VALIDATION` flag available for tests to promote
  warn-to-throw.
- **Current-limit propagation** — `TransmissionBase.configCurrentLimit*`
  applies to the full `allControllers` set, so followers cannot
  diverge from the leader. Per
  [.claude/rules/safety.md](../../../../.claude/rules/safety.md), this
  propagation is load-bearing and must not be removed.
- **Input disconnect** — `Input` tracks connection state, sends an
  Elastic notification on transition, and returns 0.0 for all axes
  while disconnected.
- **Fault monitoring** — each device's `FaultMonitor` raises a
  persistent `Alert` in the "Faults" group when a sticky fault
  activates; the alert shows in both the dashboard and the
  AdvantageKit log.

## 7. Platform Portability Notes

- **Desktop simulation** — CTRE devices create functional `SimState`
  objects when `HAL.initialize()` has been called. Position and
  velocity propagate from the leader TalonFX's `TalonFXSimState`
  into `EncoderFX` reads; external CANcoders and CANranges use
  their own sim states. The library's simulation-lifecycle plumbing
  (`simulationInit`, `simulationPeriodic`) is documented in
  [SDD-team271-lib.md §3.6 Simulation Architecture](SDD-team271-lib.md).
- **`StatusSignal` values** — in unit tests (as opposed to desktop
  sim), StatusSignals return default/zero values. Tests verify
  API wiring, not signal values.
- **Timesync** — available only on CANivore buses, not on the RIO
  CAN bus. `ControllerTalonFX.setControlUpdateFrequency()`
  switches between timesync mode (`UpdateFreqHz = 0`, `UseTimesync =
  true`) and standard mode. Robot projects that use only the RIO
  CAN bus must call `configTimesync(false, kStandardUpdateHz)`.
- **Platform detection** — the library itself does not branch on
  `RobotBase.isReal()`. Robot projects may do so (e.g., to set
  `TransmissionFX.STRICT_VALIDATION = true` in development builds).

## 8. Configuration

The hardware layer exposes three primary configuration surfaces:

### 8.1 `applyConfig` / `applyConfigs`

- `ControllerTalonFX.applyConfig()` — retries up to
  `CAN_CONFIG_APPLY_RETRIES` times; sets `isConfigured` on
  success/failure.
- `TransmissionBase.applyConfigs()` — applies to the leader only;
  followers inherit from their follow link. Called once during
  `robotInit()` after library and direct-CTRE configuration have
  both been staged via `getLeaderConfig()`.

Configuration is **`robotInit()`-only**. Config writes are large
CAN frames susceptible to collision; the retry loop can block up
to several tens of milliseconds, which is unsafe in periodic
methods. This is enforced by convention (not by an assert).

### 8.2 Live-Tunable Parameters

`ControllerSmart` exposes current and voltage limits as
`LoggedNTInput` tunables (`Tune/StatorEnable`,
`Tune/StatorLimit`, `Tune/SupplyEnable`, `Tune/SupplyLimit`,
`Tune/VoltagePeakFwd`, `Tune/VoltagePeakRev`).

`TransmissionFX` exposes Motion Magic parameters and PID gains as
tunables (`Tune/MMCruiseVel`, `Tune/MMAccel`, `Tune/MMJerk`,
`Tune/PID_kP` through `Tune/PID_kS`). Full inventory in
§8.2 above for the full list of tunable parameters.

Changes are detected in `checkTuning()` (called from
`outputTelemetry()`) and applied via `configPIDFSlot` or
`setMMConfig`, which issue single-signal CAN writes.

### 8.3 `StatusSignal` Update Frequencies

Per-signal update frequencies are set at registration time. Default
values are stored as named constants in `ConstantsLib`; the
canonical list lives alongside the constants themselves. CANrange uses a lower default frequency than the
TalonFX / CANcoder / Pigeon2 devices.

Follower motors can restrict their signal set via `Signals.NONE`
to reduce CAN traffic when the follower does not need individual
telemetry.

## 9. Test Coverage Requirements

| Area | HAL Required | CTREManager Reset | Notes |
| ---- | ------------ | ----------------- | ----- |
| `CANDeviceID` / `CANBus` | No | No | Pure value objects; verify equality and validation |
| `ControllerTalonFX` construction | Yes | Yes | `HAL.initialize(500, 0)` + `CTREManager.resetForTesting()` |
| Follower bus validation | Yes | Yes | Attempt cross-bus follow; verify `ERROR_INVALID_BUS` |
| `TransmissionFX` gear-ratio conversion | Yes | Yes | Verify `setOutputPosition` unscales via `GearRatio` |
| Sensor wrappers | Yes | Yes | Verify signal registration; no-op periodic reads |
| `CTREManager.refreshAll` | Yes | Yes | Single-signal refresh; verify `StatusCode` surface |
| `Input` axis clamping | Yes (NT) | No | Verify axes clamp to [-1.0, 1.0]; disconnect returns 0 |
| `FaultMonitor` | Yes | Yes | Simulate sticky fault; verify Alert raised |

All tests creating CTRE devices use unique CAN IDs within a class
and call `CTREManager.resetForTesting()` in `@BeforeEach`. See the
CTREManager cleanup pattern in
[SVP.md](../SVP.md#critical-pattern-ctremanager-static-state-cleanup).

Test IDs: TEST-HW-NNN.
