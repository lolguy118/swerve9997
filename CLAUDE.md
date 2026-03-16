# Team 271 Library Code Review Prompt

Please comprehensively review our FRC robot library codebase. The codebase uses the latest version of WPILib and CTRE Phoenix 6. AdvantageKit and PathPlanner are available as dependencies for future integration. The `com.team271.libtest` package contains a test robot for validating the library in simulation and on real hardware.

---

## Task Instructions

When reviewing this codebase, you should:

1. **Review all Java source files** in `com.team271.lib` and `com.team271.libtest` for bugs, correctness, and adherence to the patterns documented below.
2. **Fix issues directly** — do not just report problems, apply corrections to the code.
3. **Verify dependency versions** — check that vendordep JSONs in `vendordeps/` are up to date with their `jsonUrl` sources.
4. **Run validation** after changes:
   - `./gradlew spotlessApply` (auto-format)
   - `./gradlew spotlessCheck` (verify formatting)
   - `./gradlew test` (all JUnit tests must pass)
   - `./gradlew build` (full build must succeed)

For each issue found, briefly explain the bug category (referencing the Known Bug Patterns below if applicable) and the fix applied.

---

## Review Checklist

### Motor & Hardware Changes
- [ ] Current limits set for both stator AND supply (both must be enabled)
- [ ] Neutral mode appropriate (BRAKE for arms/elevators, COAST for launchers/flywheels)
- [ ] Config changes followed by `applyConfigs()` call
- [ ] New StatusSignals registered with CTREManager at appropriate frequency
- [ ] Control requests use timesync (`withUseTimesync(true).withUpdateFreqHz(0)`)
- [ ] Follower motors on same CAN bus as leader
- [ ] No duplicate TalonFX/CANcoder objects for same CAN ID
- [ ] No negative CAN IDs

### State Machine Changes
- [ ] Desired state set in periodic methods, applied in `robotPeriodicAfter()`
- [ ] State transition logic (timer resets, flag changes) happens at the desired-to-actual boundary
- [ ] All enum values handled in switch (no missing cases, default logs error)
- [ ] Timers reset on state transitions, stopped on disable

### Autonomous Changes
- [ ] New auto paths have cases in ALL participating subsystems
- [ ] Timing windows don't use else-if for independent subsystems
- [ ] Shared auto timer read consistently across subsystems
- [ ] Velocity/state gates checked before dependent actions (e.g., confirm motor at speed before feeding)
- [ ] Alliance flipping handled for new paths

### Safety
- [ ] Homing sequences have timeout protection
- [ ] Soft limits only enforced after `isZeroed` is true
- [ ] Voltage limits prevent reverse drive where needed
- [ ] No `System.out.println` / `System.err.println` in production paths
- [ ] Exception handling in periodic code logs and continues (doesn't crash robot)

### Null Safety
- [ ] StatusSignals checked for null AND `.getStatus().isOK()` before reading
- [ ] Optional values from WPILib (e.g., `DriverStation.getAlliance()`) have fallbacks
- [ ] Singleton access throws clear error if not initialized
- [ ] Controller disconnect handled gracefully (check `isConnected()`)

### Cross-Subsystem Dependencies
- [ ] SubsystemManager add order preserved (consumers after producers)
- [ ] Static shared state updated before consumers read it
- [ ] Shared timers started/stopped in the correct subsystem

### Simulation Changes
- [ ] DCMotor model matches actual motor type in MotorBase
- [ ] Sim state updated every cycle in `simulationPeriodic()`
- [ ] Gear ratios applied correctly in sim (same as real hardware)
- [ ] New hardware classes implement `simulationInit()` and `simulationPeriodic()`

### Testing
- [ ] `./gradlew test` passes (all JUnit tests green)
- [ ] New utility/infrastructure classes have corresponding test classes in `src/test/java/`
- [ ] Tests use `@BeforeAll` for HAL initialization, `@BeforeEach` for state reset
- [ ] Static state (singletons, static managers) cleaned up between tests
- [ ] New branches in existing code covered by updated tests
- [ ] No test depends on execution order (each test is independent)

### Dependencies
- [ ] All vendordep JSONs are the latest version (check `jsonUrl` sources)
- [ ] `build.gradle` GradleRIO plugin version is latest for the FRC season
- [ ] No hardcoded dependency versions in `build.gradle` that conflict with vendordep versions
- [ ] JUnit version is current

### Formatting & Linting
- [ ] `./gradlew spotlessCheck` passes (no formatting violations)
- [ ] No unused imports (Spotless `removeUnusedImports()` enforces this)
- [ ] No trailing whitespace
- [ ] All files end with a newline
- [ ] 4-space indentation (AOSP style via google-java-format)
- [ ] Line length <= 120 characters (`.editorconfig` recommendation)

### Logging & Telemetry Changes
- [ ] Tunable parameters use `LoggedNTInput` with `hasChanged()` + `checkTuning()` in `outputTelemetry()`
- [ ] New error conditions send `Elastic.sendNotification()` at appropriate severity
- [ ] High-frequency Elastic notifications are rate-limited
- [ ] State-change notifications fire only on transition (not every cycle)
- [ ] Direct `Logger.recordOutput()` only for non-TObj classes (e.g., Balance, AutoMode)

### Code Quality
- [ ] No `==` for String comparison (use `.equals()`)
- [ ] No recursive calls missing `super.`
- [ ] No `final` fields being "reassigned" (check commented-out reassignment lines)
- [ ] Gear ratio conversions consistent (rotor-to-mechanism vs sensor-to-mechanism)
- [ ] Unit conversions correct (rotations vs radians, RPS vs RPM, meters vs inches)

### Real-Time Constraints
- [ ] No blocking I/O, `Thread.sleep()`, or unbounded loops in periodic methods
- [ ] CAN config operations (`applyConfig()`, `applyConfigs()`) only in `robotInit()`, never in periodic methods
- [ ] New periodic work fits within 20ms cycle budget (profile if uncertain)
- [ ] No large object allocations or unbounded collection growth in periodic paths
- [ ] High-frequency error notifications are rate-limited (2s minimum interval)
- [ ] Control requests use timesync pattern (`withUseTimesync(true).withUpdateFreqHz(0)`)
- [ ] Latency-compensated encoder variants (`Comp`) used where position accuracy matters
- [ ] No `System.out.println` / `System.err.println` — blocking I/O that steals cycle time; use `Logger.recordOutput()` or `DriverStation.reportWarning()`
- [ ] New StatusSignals registered at 250 Hz unless a different rate is justified

---

## Stack

All dependencies must always use the **latest available version**. Versions are managed via vendordep JSON files in `vendordeps/` and `build.gradle`. When reviewing, verify vendordeps are up to date with their `jsonUrl` sources.

- **WPILib** — core FRC framework (GradleRIO, Java 17)
- **CTRE Phoenix 6** — motor controllers, sensors, CANivore + RIO CAN buses (vendordep: `Phoenix6-frc2026-latest.json`)
- **AdvantageKit** — integrated for all logging and telemetry (vendordep: `AdvantageKit.json`; all output goes through `Logger.recordOutput()`)
- **PathPlanner** — dependency available, integration pending (vendordep: `PathplannerLib.json`; new autonomous code should adopt PathPlanner paths)
- **WPILib New Commands** — dependency available for command-based patterns (vendordep: `WPILibNewCommands.json`)
- **Elastic Dashboard** — integrated for live driver notifications (no vendordep; uses `com.team271.lib.misc.Elastic` with Jackson JSON serialization)

---

## Library Architecture (`com.team271.lib`)

```
ConstantsLib (library-wide constants: CAN_RETRY_COUNT, CAN_TIMEOUT_MS, CAN_LONG_TIMEOUT_MS, NT_UPDATE_MS)
TObj (base class — name, NTTable, lifecycle hooks)
├── TRobot (root robot object)
├── Subsystem (sensor modes, isZeroed, lifecycle)
│   └── SubsystemManager (singleton, forEachSafe exception isolation)
├── hardware/
│   ├── CTREManager (centralized CAN signal refresh at 250Hz, dt tracking)
│   ├── CANDeviceID (device number + bus name composite key)
│   ├── CANBus (CANBusType: RIO/CANIVORE, utilization tracking, hoot file logging)
│   ├── controllers/
│   │   ├── ControllerBase (abstract — duty cycle, voltage, follower, simulation)
│   │   ├── → ControllerSmart (abstract — stator/supply current limits, voltage peaks, ramp rates)
│   │   ├── → → ControllerTalonFX (Phoenix 6 — timesync, TorqueCurrentFOC, TalonFXSimState)
│   │   └── Leader/follower pattern, PID slots 0-2
│   ├── transmissions/
│   │   ├── TransmissionBase → TransmissionFX
│   │   ├── Voltage, duty, position, velocity, Motion Magic control modes
│   │   ├── Encoder integration: internal FX or external CANCoder
│   │   ├── Shifter interface → ShifterPneumatic (DoubleSolenoid, gear ratios per gear)
│   │   └── DCMotor physics models for simulation
│   ├── sensors/
│   │   ├── encoders/ (EncoderBase → EncoderCTRE → EncoderFX, EncoderCANCoder)
│   │   │   └── Comp variants (EncoderFXComp, EncoderCANCoderComp) add latency compensation
│   │   ├── imu/ (IMUBase → IMUCTRE → IMUPigeon2)
│   │   ├── range/ (RangeBase → RangeCTRE → RangeCANrange)
│   │   └── switches/ (SwitchBase → SwitchFX, SwitchCANCoder)
│   ├── Input/ (Input → Input8BitDuo, InputXBox, InputPS4, InputEnvisionPro)
│   │   └── Input shaping: LINEAR, SOFT, SQUARED, CUBED, AGGRESSIVE, MORE_AGGRESSIVE, DYNAMIC
│   └── motors/MotorBase (Falcon500, KrakenX60, KrakenX44, CTRE_Minion, NEO, NEO550, NEO_Vortex)
├── control/
│   ├── pid/ (PIDBase → PIDSimple, PIDTrap, PIDWPI, PIDWPI_Trap, PIDFX)
├── geometry/ (custom implementations, not WPILib wrappers)
│   ├── Pose2d, Rotation2d, Translation2d, Twist2d, State
│   └── Interfaces: IPose2d, IRotation2d, ITranslation2d
├── nt/ (NTTable, NTEntry, LoggedNTInput — logging + tuning via AK Logger)
├── wpilib/ (IterativeRobotBase, TimedRobot — custom WPILib base classes + AK lifecycle)
├── sysid/ (Logger, LoggerGeneral — SmartDashboard-based SysId characterization)
├── misc/ (Elastic — dashboard notifications via Elastic.Notification)
├── auto/ (AutoMode, AutoMove, AutoMoveSingle, AutoMoveTimed)
└── util/ (Util, Alert, DriveSignal, CSVWritable, Interpolable)
```

### Key Design Patterns

1. **TObj Lifecycle**: Every component extends `TObj` and receives lifecycle callbacks: `robotInit()`, `robotPeriodicBefore()`, `robotPeriodicAfter()`, `autonomousInit/Periodic/Exit()`, `teleopInit/Periodic/Exit()`, `simulationInit/Periodic()`, etc.

2. **Subsystem State Machines**: Subsystems use a desired-state/actual-state pattern. Desired state is set in `teleopPeriodic()`/`autonomousPeriodic()`, then applied in `robotPeriodicAfter()`. State transitions (timer resets, etc.) happen at the boundary in `robotPeriodicAfter()`.

3. **SubsystemManager Singleton**: `SubsystemManager` uses a lazy-initialized singleton with `getInstance()` and a private constructor. Individual subsystems are NOT singletons — they are instantiated normally in `Robot.robotInit()` and registered with `SubsystemManager.addSubsystem()`.

4. **SubsystemManager Ordering**: Subsystems are added in a specific order in `Robot.robotInit()`. The iteration order determines which subsystem's `robotPeriodicBefore()` runs first. This matters for cross-subsystem data dependencies (producers must be added before consumers).

5. **TransmissionFX Wraps TalonFX**: Motor commands go through TransmissionFX, which handles encoder integration, gear ratios, unit conversions, and timesync control requests. Do not create raw TalonFX objects for subsystem motors.

6. **Exception Isolation**: `SubsystemManager.forEachSafe()` wraps subsystem callbacks in try-catch to prevent one subsystem's exception from crashing the entire robot. Note: `robotInit()` rethrows exceptions because init must succeed.

---

## Real-Time System Constraints

FRC robot code runs under hard real-time constraints. Every subsystem callback shares a fixed time budget per cycle. Violating timing constraints causes loop overruns, degraded control performance, and potentially unsafe robot behavior.

### Timing Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│  1 kHz   │ TalonFX hardware PID (PIDFX) — runs onboard motor   │
│  1 kHz+  │ CTRE hoot logging — raw CAN frames (CANivore only)  │
│  250 Hz  │ CAN signal refresh — StatusSignals via CTREManager   │
│  250 Hz  │ Timesync — CANivore frame synchronization            │
│  50 Hz   │ Robot loop — all subsystem callbacks (20ms budget)   │
│  50 Hz   │ Software PID (PIDWPI, PIDSimple, etc.)               │
│  10 Hz   │ NetworkTables publishing (NTEntry, 100ms default)    │
└─────────────────────────────────────────────────────────────────┘
```

### Robot Loop (20ms / 50 Hz)

- **Period**: `TimedRobot.kDefaultPeriod = 0.02` seconds, enforced by HAL Notifier via FPGA clock
- **Watchdog**: `IterativeRobotBase` initializes a Watchdog that reports when any cycle exceeds 20ms
- **Constraint**: All code in `robotPeriodicBefore()`, mode-specific periodic methods, `robotPeriodicAfter()`, and `outputTelemetry()` must complete within this budget combined

### Execution Order Within a Single Cycle

```
1. LoggerBridge.periodicBeforeUser()          ← AK timestamp capture
2. CTREManager.refreshAll()                   ← batched CAN signal read (250 Hz signals)
3. Timestamp update (Timer.getFPGATimestamp)
4. SubsystemManager.robotPeriodicBefore()     ← sensor reads, state prep
5. Mode-specific periodic                     ← autonomousPeriodic / teleopPeriodic / etc.
6. SubsystemManager.robotPeriodicAfter()      ← state machines apply desired → actual
7. SubsystemManager.outputTelemetry()         ← NTEntry.publish(), checkTuning(), Alert output
8. CTREManager.outputTelemetry()              ← CAN bus telemetry
9. LoggerBridge.periodicAfterUser()           ← AK log flush
10. NetworkTableInstance.flushLocal()          ← NT data push (if enabled)
```

Steps 4-8 run through `forEachSafe()` — a single subsystem throwing an exception will not block others.

### CAN Bus Timing

| Parameter | Value | Constant / Location |
|-----------|-------|---------------------|
| Signal update frequency | 250 Hz (4ms) | `ControllerTalonFX` lines 54-101, `EncoderCTRE`, `IMUCTRE`, etc. |
| Timesync frequency | 250 Hz | `ControllerTalonFX:185` — `ControlTimesyncFreqHz = 250.0` |
| Short timeout (runtime) | 10 ms | `ConstantsLib.CAN_TIMEOUT_MS` |
| Long timeout (config) | 100 ms | `ConstantsLib.CAN_LONG_TIMEOUT_MS` |
| Config retry count | 5 | `ConstantsLib.CAN_RETRY_COUNT` |
| Hoot logging rate | 1 kHz+ | `CTREManager.init()` — CANivore buses only |

- `BaseStatusSignal.refreshAll()` is called once per cycle for batched efficiency
- `optimizeBusUtilizationForAll()` disables unused status frames to prevent bus saturation
- CAN config operations (retries × timeout = up to 500ms) must only happen in `robotInit()`, never in periodic methods

### Control Loop Frequencies

- **Hardware PID (PIDFX)**: Runs at **1 kHz** onboard the TalonFX. Software sends setpoints via timesync control requests; the motor controller closes the loop internally. This is why PIDFX delegates entirely to hardware — software cannot match 1 kHz.
- **Software PID (PIDWPI, PIDSimple, PIDTrap)**: Runs at **50 Hz** (robot loop rate, `0.02s` period). Suitable for mechanisms where hardware PID is not available or for outer control loops.
- **Timesync requirement**: When `UseTimesync = true`, `UpdateFreqHz` must be `0` (CTRE requirement). All control requests in `ControllerTalonFX` and `TransmissionFX` follow this pattern.

### Latency Compensation

CAN frames arrive with transport delay. Latency compensation uses velocity to extrapolate position to the current time:
- **EncoderFXComp / EncoderCANCoderComp**: `BaseStatusSignal.getLatencyCompensatedValue(posSignal, velSignal)` for accurate mechanism position
- **IMUPigeon2**: Yaw latency-compensated using yaw rate as the reference signal
- Use `Comp` encoder variants when position accuracy matters (e.g., odometry, closed-loop position)

### Thread Priorities

| Thread | Priority | Real-time | Notes |
|--------|----------|-----------|-------|
| HAL Notifier (robot loop) | System-managed | Yes | FPGA-timed, highest priority |
| HAL thread (SysId) | 40 | Yes | `sysid/Logger.java:60` |
| User thread (SysId) | 15 | Yes | `sysid/Logger.java:59` |
| NetworkTables | Default | No | Background publishing |

Thread priorities are only set in real mode (non-simulation). The main robot loop runs synchronously on the Notifier thread — all subsystem callbacks execute sequentially in registration order.

### Exception Isolation & Rate Limiting

- **`forEachSafe()`** (`SubsystemManager`): Wraps each subsystem callback in try-catch. One subsystem throwing does not prevent others from running. Errors are reported via `DriverStation.reportError()`.
- **`robotInit()` rethrows**: Initialization failures must crash loudly — a subsystem that cannot initialize should not run.
- **Error notification rate limit**: 2.0 seconds per subsystem (`SubsystemManager`) and per CAN refresh failure (`CTREManager`). Prevents flooding Elastic Dashboard during sustained failures.

### Timing Budget Guidelines

**DO in periodic methods:**
- Read cached sensor values (already refreshed by `CTREManager`)
- Set control requests (lightweight CAN writes)
- Update state machines with simple logic
- Call `NTEntry.publish()` / `Logger.recordOutput()` for telemetry

**DO NOT in periodic methods:**
- Call `applyConfig()` or any CAN config operation (up to 500ms blocking)
- Perform blocking I/O (file reads, network calls)
- Allocate large objects or create unbounded collections
- Run unbounded loops or recursive algorithms
- Call `Thread.sleep()` or any blocking wait

**Measuring overruns:**
- The Watchdog automatically logs when a cycle exceeds 20ms via `DriverStation.reportWarning()`
- AdvantageKit logs cycle timing — check `LoggedRobot/FullCycleMs` in AdvantageScope
- CTRE `CTREManager.getDt()` tracks time between CAN refreshes for drift detection

---

## Libtest Architecture (`com.team271.libtest`)

### Files

- **`Main.java`** — WPILib `RobotBase` entry point (`RobotBase.startRobot(Robot::new)`)
- **`Robot.java`** — extends `TimedRobot`, orchestrates lifecycle:
  - Constructor: AK Logger setup (metadata, data receivers, `Logger.start()`) — must happen before any `Logger.recordOutput()` calls
  - `robotInit()`: subsystems init → `mSubsystemManager.robotInit()` → `CTREManager.init()` (must be AFTER subsystem init)
  - `robotPeriodicBefore()`: `CTREManager.refreshAll()` → timestamp update → `mSubsystemManager.robotPeriodicBefore()`
  - `robotPeriodicAfter()`: `mSubsystemManager.robotPeriodicAfter()` → `mSubsystemManager.outputTelemetry()` → `CTREManager.outputTelemetry()`

```
libtest/
├── Main.java                    (WPILib entry point)
├── Robot.java                   (TimedRobot lifecycle orchestrator)
├── Config.java                  (Mode: REAL/SIM/REPLAY, RobotType enum)
├── Constants.java               (CAN IDs, bus names, controller ports, physical dimensions)
├── Globals.java                 (static singleton references to all subsystems)
├── subsystems/
│   ├── Infrastructure.java      (PDH, gyro, teleop/auto mode tracking)
│   ├── Superstructure.java      (high-level coordination, ROBOT_STATE enum)
│   └── Input/
│       ├── InputDriver.java     (driver controller)
│       └── InputOp.java         (operator controller)
└── auto/
    ├── auto_modes/Auto0.java    (default autonomous mode)
    └── auto_moves/              (autonomous movement actions)
```

### Config Modes
- `Mode.REAL` — running on physical robot hardware
- `Mode.SIM` — running in WPILib simulation (uses `DriverStationSim`, simbot type)
- `Mode.REPLAY` — replaying from a log file (real robot types in sim environment)

### Subsystem Init Order (in `Robot.robotInit()` — order matters)
1. InputDriver
2. InputOp
3. Infrastructure
4. Superstructure
5. `mSubsystemManager.robotInit()` (calls robotInit on all subsystems)
6. `CTREManager.init()` (optimizes bus, builds signal arrays — must be AFTER subsystem init)

---

## CTRE Phoenix 6 Required Patterns

### Timesync
All control requests MUST use timesync on CANivore buses:
```java
new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0)
```
When `UseTimesync = true`, `UpdateFreqHz` MUST be `0` (CTRE requirement). The ControllerTalonFX config also sets `ControlTimesyncFreqHz = 250.0`.

### Signal Refresh
- All StatusSignals are registered with `CTREManager` during `robotInit()` via `addSignal()`, `addSignalTalonFX()`, `addSignalCANCoder()`, `addSignalPigeon()`, `addSignalCANrange()`
- `CTREManager.refreshAll()` is called once per cycle in `Robot.robotPeriodicBefore()` using `BaseStatusSignal.refreshAll()` for batched efficiency
- CTREManager tracks `lastRefreshTime` and `prevRefreshTime` for `getDt()` calculations
- Individual signal reads must check `signal != null && signal.getStatus().isOK()` before reading values
- `addSignalInternal()` validates signals on registration — only adds signals with OK status
- Target update frequency: 250Hz for most signals

### Config Application
- Motor configs are applied via `ControllerTalonFX.applyConfig()` which retries up to 5 times (`ConstantsLib.CAN_RETRY_COUNT`) with 50ms timeout per attempt
- CTRE configs use `ConstantsLib.CAN_LONG_TIMEOUT_MS` (100ms) timeout with the same 5 retries
- Always call `applyConfigs()` after changing config fields — config objects are mutable but not applied until explicitly sent

### Latency Compensation
- Encoders (EncoderFXComp, EncoderCANCoderComp) use `BaseStatusSignal.getLatencyCompensatedValue(posSignal, velSignal)` for accurate position
- IMUPigeon2 uses latency compensation for yaw with yaw rate as reference

### Bus Optimization
- `ParentDevice.optimizeBusUtilizationForAll()` is called per-bus in `CTREManager.init()` to disable unused status frames
- Do not manually set status frame periods — let the optimizer handle it

### Follower Motors
- Created via `ControllerTalonFX` constructor with leader reference and oppose flag
- Follower and leader MUST be on the same CAN bus
- The `follow()` method uses `StrictFollower` control request with `MotorAlignmentValue`

---

## Simulation Patterns

Simulation support is integrated throughout all hardware classes, not in a separate package.

### Hardware Simulation Methods
- **Controllers**: `setSimVelRotations()`, `setSimPosRotations()` — set simulated encoder feedback
- **Encoders**: `simulationInit()`, `simulationPeriodic()` — update sim state each cycle
- **Transmissions**: build `DCMotor` physics models from `MotorBase` motor type (Falcon500, KrakenX60, etc.)
- **IMU / Range sensors**: `simulationInit()`, `simulationPeriodic()` — update simulated sensor values
- **TalonFX**: `TalonFXSimState` via `getSimState()` — CTRE's built-in physics simulation

### Robot-Level Simulation
- `simulationInit()` and `simulationPeriodic()` lifecycle hooks in `TObj` hierarchy
- WPILib simulation GUI enabled via `wpi.sim.addGui()` in `build.gradle`
- `DriverStationSim.setAllianceStationId()` used to set alliance in sim

---

## Logging Architecture

Data flows through three parallel systems:

```
┌──────────────────────────────────────────────────────────────────┐
│                    LOGGING (Robot → Logs/Dashboard)               │
│                                                                  │
│  NTEntry.publish() ──→ Logger.recordOutput()                     │
│                         ├──→ .wpilog (AdvantageScope replay)     │
│                         └──→ NT4Publisher → NT → Elastic/Scope   │
│                                                                  │
│  CTRE SignalLogger ──→ .hoot files (raw CAN at 1kHz+, CANivore) │
│                                                                  │
│  Alert.set() ──→ Logger.recordOutput() + Elastic notification    │
├──────────────────────────────────────────────────────────────────┤
│                    TUNING (Dashboard → Robot)                    │
│                                                                  │
│  LoggedNTInput ──→ NT subscriber (reads operator changes)        │
│                    + Logger.recordOutput() (records to AK log)   │
│                    + NT publisher (sets default for dashboard)    │
└──────────────────────────────────────────────────────────────────┘
```

### AdvantageKit Integration

#### Logger Lifecycle
- `IterativeRobotBase.loopFunc()` calls `LoggerBridge.periodicBeforeUser()` at start, `LoggerBridge.periodicAfterUser()` after simulation
- `LoggerBridge` (`org.littletonrobotics.junction.LoggerBridge`) is a bridge class that exposes AK's package-private `Logger.periodicBeforeUser/AfterUser` methods for use in custom robot base classes that cannot extend `LoggedRobot`
- `Robot()` constructor configures Logger before any subsystem init: metadata → data receivers → `Logger.start()`

#### Config Modes and Logger Setup
```java
switch (Config.getMode()) {
    case REAL:  WPILOGWriter + NT4Publisher  // .wpilog on USB + live NT
    case SIM:   NT4Publisher                 // live NT only
    case REPLAY: WPILOGReader → WPILOGWriter // replay from log, write sim output
}
```
- `Config.FORCE_REPLAY = true` enables replay mode in simulation (reads from existing .wpilog)

#### NTEntry (Output Only)
- `publish()` calls `Logger.recordOutput(logPath, value)` — does NOT write to NetworkTables directly
- AK Logger writes to .wpilog AND publishes via NT4Publisher, so data appears in both AdvantageScope and NT dashboards
- `logPath` is built from `table.getPath() + "/" + topicName` in the constructor
- `get*()` methods still read from NT subscriber (for input from other systems)
- When `table` is null, `logPath` is null and `publish()` is a no-op

#### LoggedNTInput (Tuning Input)
- Maintains NT publisher (dashboard sees the field with default value) AND NT subscriber (reads operator changes)
- Every `getDbl()`/`getBool()`/`getLong()`/`getString()` call also records to AK Logger for replay fidelity
- `hasChanged()` compares current subscriber value to last-read value — returns true when operator changes a value on the dashboard
- Default values are stored regardless of whether table is null (supports null-table fallback)
- Constructors: `LoggedNTInput(NTTable, String name, double/boolean/long/String defaultValue)`

### CTRE Hoot Logging
- `CTREManager.init()` starts `SignalLogger` only if a CANivore bus exists (`isCANivore()` check)
- Hoot logging does NOT work on the RIO CAN bus — CANivore required
- `.hoot` files capture raw CAN frames at 1kHz+; AdvantageScope reads them natively
- `CTREManager.stopLogging()` stops SignalLogger (also CANivore-guarded)

### Review Checklist — Logging
- [ ] New `NTEntry` fields use `logPath`-based output (automatic via constructor)
- [ ] New tunable parameters use `LoggedNTInput` with `hasChanged()` + `checkTuning()` pattern
- [ ] `outputTelemetry()` calls `checkTuning()` before publishing
- [ ] No `SmartDashboard.putNumber/putString` — use `Logger.recordOutput()` or `NTEntry.publish()`
- [ ] No `System.out.println` — use `Logger.recordOutput()` for data, `DriverStation.reportWarning` for errors
- [ ] Direct `Logger.recordOutput()` OK for classes that don't extend TObj (e.g., Balance, AutoMode)

---

## Dashboard Tuning

### Tunable Parameters (via LoggedNTInput)

The `checkTuning()` pattern reads `LoggedNTInput` values in `outputTelemetry()` and applies changes via existing setter methods:

#### PID (per PIDBase instance)
- `Tune P`, `Tune I`, `Tune D` → `setP()`, `setI()`, `setD()`
- `Tune Pos Tol` → `setTolerance()`
- `Tune P Deadband` → `setPDeadband()`
- `Tune I Zone` → `setIZone()`
- `Tune Output Min`, `Tune Output Max` → `setOutputRange()`
- PIDFX: `setP()`/`setI()`/`setD()` overrides also call `controller.setPSlot(0, val)` etc. to propagate to TalonFX hardware PID

#### Motor Current/Voltage (per ControllerSmart)
- `Tune Stator Enable`, `Tune Stator Limit` → `setCurrentLimitStator()`
- `Tune Supply Enable`, `Tune Supply Limit` → `setCurrentLimitSupply()`
- `Tune Voltage Peak Fwd`, `Tune Voltage Peak Rev` → `setVoltagePeak()`

#### Motion Magic (per TransmissionFX)
- `Tune MM Cruise Vel`, `Tune MM Accel`, `Tune MM Jerk` → `setMMConfig()`

#### Balance
- `Tune Speed Slow`, `Tune Speed Fast`, `Tune On Charge Deg`, `Tune Level Deg`, `Tune Debounce Time` → direct field updates

### Review Checklist — Tuning
- [ ] New tunable parameters initialized with safe defaults
- [ ] `checkTuning()` called at start of `outputTelemetry()` (end of robot cycle)
- [ ] PIDFX hardware sync: `setP/I/D` overrides propagate to TalonFX via `controller.setPSlot()`
- [ ] No direct field mutation for tuning — always use setter methods (they may trigger config re-apply)

---

## Elastic Dashboard Notifications

`Elastic.sendNotification()` sends live popup notifications to Elastic Dashboard via NetworkTables.

### Notification Events
| Event | Level | Title | Source File |
|-------|-------|-------|-------------|
| Autonomous started | INFO | Mode Change | `Infrastructure.java` |
| Teleop started | INFO | Mode Change | `Infrastructure.java` |
| Robot disabled | WARNING | Mode Change | `Infrastructure.java` |
| State transition | INFO | Superstructure | `Superstructure.java` |
| Subsystem exception | ERROR | Subsystem Error | `SubsystemManager.java` |
| CAN signal refresh fail | ERROR | CAN Error | `CTREManager.java` (2s rate limit) |
| Config apply failure | ERROR | Config Failed | `TransmissionBase.java` |
| Auto started | INFO | Auto | `AutoMode.java` |
| Auto complete | INFO | Auto | `AutoMode.java` |
| Alert activated (ERROR) | ERROR | Alert | `Alert.java` |
| Alert activated (WARNING) | WARNING | Alert | `Alert.java` |
| Alert activated (INFO) | INFO | Alert | `Alert.java` |
| Controller disconnected | WARNING | Controller | `Input.java` |

### Review Checklist — Elastic
- [ ] New error conditions send Elastic notifications with appropriate severity level
- [ ] High-frequency events are rate-limited (e.g., CAN errors use 2s rate limit)
- [ ] Notification title is short and descriptive (shown as popup heading)
- [ ] State transitions fire only on actual transition, not every cycle

---

## Alert System

`Alert.java` provides prioritized, grouped alerts that integrate with both AK Logger and Elastic Dashboard:

- `Alert.set(true)` → logs to AK + sends Elastic notification + reports to DriverStation
- `Alert.outputTelemetry()` → logs active alert string arrays per group to AK (called from `SubsystemManager.outputTelemetry()`)
- AlertType maps to Elastic NotificationLevel: `ERROR` → `ERROR`, `WARNING` → `WARNING`, `INFO` → `INFO`

---

## SysId / Characterization

The `sysid/` package provides system identification support:

- **`Logger.java`**: Data collection for quasistatic and dynamic tests. Outputs via `Logger.recordOutput()` (AdvantageKit). Reads voltage commands from SmartDashboard (input from SysId tool). Collects voltage, position, and velocity data for feed-forward characterization.
- **`LoggerGeneral.java`**: CSV-based data logging extension for general characterization.
- **`SensorMode.SYSID`**: Triggers characterization mode in subsystems, allowing SysId voltage commands to override normal control.

---

## WPILib Required Patterns

### NetworkTables / Logging
- **`NTEntry`** — output-only wrapper. `publish()` calls `Logger.recordOutput()` (AdvantageKit), NOT NetworkTables directly. Subscribes to NT for input reading via `get*()` methods.
- **`LoggedNTInput`** — tuning input wrapper. Maintains both a NT publisher (sets default for dashboard visibility) and NT subscriber (reads operator changes). Every `getDbl()`/`getBool()`/etc. call also records to AK Logger. Supports `hasChanged()` to detect dashboard edits.
- All telemetry output happens in `outputTelemetry()` methods via `NTEntry.publish()` → AK Logger
- All tuning input happens via `LoggedNTInput` checked in `outputTelemetry()` → `checkTuning()` pattern

### Units
- CTRE Phoenix 6 uses the WPILib Units library (e.g., `Rotations`, `MetersPerSecond`, `Amps`)
- Internal library convention: positions in rotations, velocities in RPS, distances in meters
- Gear ratios: `rotorToMechanism`, `sensorRelToMechanism`, `sensorAbsToMechanism`, `mechanismToUnits`

### Subsystem Sensor Modes
- `SensorMode` enum: `SENSORED_AUTO`, `SENSORED_MANUAL`, `SENSORLESS`, `SYSID`
- `sensorsZero()` in the base class sets `isZeroed = false` to mark the start of a zeroing process — subclasses must set `isZeroed = true` after completing their homing sequence
- Sensor mode changes may trigger config changes (e.g., different current limits in sensorless mode)

---

## Formatting & Linting

Code formatting is enforced automatically via **Spotless** (Gradle plugin) and **`.editorconfig`**.

### Spotless (google-java-format, AOSP style)
- Configured in `build.gradle` under the `spotless { }` block
- Uses **google-java-format** with the **AOSP** variant (4-space indentation)
- Enforces: consistent formatting, removed unused imports, no trailing whitespace, final newline
- **Check:** `./gradlew spotlessCheck` — fails the build if any file has violations
- **Fix:** `./gradlew spotlessApply` — auto-formats all Java source files in-place
- Supports `// spotless:off` / `// spotless:on` toggle comments for sections that must not be reformatted

### .editorconfig
- IDE-agnostic formatting hints at the repository root
- Java: 4-space indent, 120-char line length, UTF-8, LF line endings
- JSON/YAML: 2-space indent
- Markdown: trailing whitespace preserved (intentional line breaks)

### Workflow
1. Write code normally in your IDE
2. Before committing, run `./gradlew spotlessApply` to auto-fix formatting
3. CI (if configured) runs `./gradlew spotlessCheck` — PRs with violations will fail

### JUnit Testing
- Test source directory: `src/test/java/`
- Framework: JUnit 5 (Jupiter) — configured in `build.gradle`
- Run tests: `./gradlew test`
- HAL initialization: Tests that use WPILib or CTRE Phoenix 6 classes must call `HAL.initialize(500, 0)` in a `@BeforeAll` method
- Static state: Classes like `CTREManager` that use static state require `@BeforeEach` cleanup between tests (via reflection if necessary)
- Sim devices: `TalonFX`, `CANcoder`, `Pigeon2`, etc. create simulated device instances in the HAL — no real hardware needed
- Test coverage: Target 100% method and branch coverage for utility/infrastructure classes (`CANBus`, `CANDeviceID`, `CTREManager`)

### Existing Tests (`src/test/java/`)

46 test classes spanning the full library:

- **`control/`**: `BalanceTest`, `PIDSimpleTest`, `PIDTrapTest`, `PIDWPITest`, `PIDWPITrapTest`, `PIDFXTest`
- **`hardware/`**: `CANBusTest` (68 tests), `CANDeviceIDTest` (50+ tests), `CTREManagerTest`, `MotorBaseTest`, `ControllerTalonFXTest`
- **`hardware/sensors/`**: `EncoderFXTest`, `EncoderFXCompTest`, `EncoderCANCoderTest`, `EncoderCANCoderCompTest`, `IMUPigeon2Test`, `RangeCANrangeTest`, `SwitchFXTest`, `SwitchCANCoderTest`
- **`hardware/transmissions/`**: `TransmissionFXTest`, `ShifterPneumaticTest`
- **`hardware/Input/`**: `InputXBoxTest`, `InputPS4Test`, `Input8BitDuoTest`, `InputEnvisionProTest`
- **`geometry/`**: `Translation2dTest`, `Rotation2dTest`, `Pose2dTest`, `Twist2dTest`
- **`nt/`**: `NTTableTest`, `NTEntryTest`, `LoggedNTInputTest`
- **`auto/`**: `AutoModeTest`, `AutoMoveTest`, `AutoMoveSingleTest`, `AutoMoveTimedTest`
- **`subsystem/`**: `SubsystemTest`, `SubsystemManagerTest`
- **`sysid/`**: `LoggerTest`, `LoggerGeneralTest`
- **`misc/`**: `ElasticTest`
- **`util/`**: `UtilTest`, `DriveSignalTest`, `AlertTest`
- **Root**: `ConstantsLibTest`, `TObjTest`, `TRobotTest`

---

## Reference Documentation

Use these references to verify API usage and look up correct patterns. Fetch documentation as needed using available tools.

- Phoenix 6 Docs: https://v6.docs.ctr-electronics.com/en/stable/index.html
- Phoenix 6 Java API: https://api.ctr-electronics.com/phoenix6/stable/java/
- Phoenix 6 Examples: https://github.com/CrossTheRoadElec/Phoenix6-Examples/tree/main/java
- WPILib Docs: https://docs.wpilib.org/en/stable/
- WPILib Java API: https://github.wpilib.org/allwpilib/docs/release/java/index.html
- WPILib Source: https://github.com/wpilibsuite/allwpilib
- PathPlanner: https://pathplanner.dev/home.html
- PathPlanner GitHub: https://github.com/mjansen4857/pathplanner
- AdvantageKit Docs: https://docs.advantagekit.org/
- AdvantageKit Source: https://github.com/Mechanical-Advantage/AdvantageKit
- Elastic Dashboard Docs: https://frc-elastic.gitbook.io/docs
- Elastic Dashboard Source: https://github.com/Gold872/elastic_dashboard
