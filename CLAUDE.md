# Team 271 Library Code Review Prompt

Please comprehensively review our FRC robot library codebase. The codebase uses the latest version of WPILib and CTRE Phoenix 6. AdvantageKit and PathPlanner are available as dependencies for future integration. The `com.team271.libtest` package contains a test robot for validating the library in simulation and on real hardware.

---

## Task Instructions

When reviewing this codebase, you should:

1. **Review all Java source files** in `com.team271.lib` and `com.team271.libtest` for bugs, correctness, and adherence to the patterns documented below.
2. **Fix issues directly** — do not just report problems, apply corrections to the code.
3. **Remove debug artifacts** — delete commented-out `System.out.println` lines.
4. **Verify dependency versions** — check that vendordep JSONs in `vendordeps/` are up to date with their `jsonUrl` sources.
5. **Run validation** after changes:
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

### PID / Closed-Loop Changes
- [ ] PID gains set to correct slot (Slot 0, 1, or 2)
- [ ] `getCLError()` used for error, `getCLOutput()` used for output (not swapped)
- [ ] Feed-forward direction based on error sign, not output sign
- [ ] `setOutputRange()` called (note: was previously misspelled as `setOutputRage`)

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

### Code Quality
- [ ] No `==` for String comparison (use `.equals()`)
- [ ] No recursive calls missing `super.`
- [ ] No `final` fields being "reassigned" (check commented-out reassignment lines)
- [ ] Gear ratio conversions consistent (rotor-to-mechanism vs sensor-to-mechanism)
- [ ] Unit conversions correct (rotations vs radians, RPS vs RPM, meters vs inches)

---

## Stack

All dependencies must always use the **latest available version**. Versions are managed via vendordep JSON files in `vendordeps/` and `build.gradle`. When reviewing, verify vendordeps are up to date with their `jsonUrl` sources.

- **WPILib** — core FRC framework (GradleRIO, Java 17)
- **CTRE Phoenix 6** — motor controllers, sensors, CANivore + RIO CAN buses (vendordep: `Phoenix6-frc2026-latest.json`)
- **AdvantageKit** — dependency available, integration pending (vendordep: `AdvantageKit.json`; new code should adopt `Logger.recordOutput()` and IO interfaces)
- **PathPlanner** — dependency available, integration pending (vendordep: `PathplannerLib.json`; new autonomous code should adopt PathPlanner paths)
- **WPILib New Commands** — dependency available for command-based patterns (vendordep: `WPILibNewCommands.json`)

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
│   └── Balance.java (dead code from 2025 — DELETE THIS FILE)
├── geometry/ (custom implementations, not WPILib wrappers)
│   ├── Pose2d, Rotation2d, Translation2d, Twist2d, State
│   └── Interfaces: IPose2d, IRotation2d, ITranslation2d
├── nt/ (NTTable, NTEntry — NetworkTables pub/sub wrappers with value caching)
├── wpilib/ (IterativeRobotBase, TimedRobot — custom WPILib base classes)
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

### Existing Tests (`src/test/java/`)

- `com/team271/lib/control/pid/PIDSimpleTest.java` — PID unit tests
- `com/team271/lib/hardware/CANBusTest.java` — CAN bus type detection, constructors, equals/hashCode (68 tests)
- `com/team271/lib/hardware/CANDeviceIDTest.java` — device ID creation, isSameBus, equals/hashCode (50+ tests)
- `com/team271/lib/hardware/CTREManagerTest.java` — signal management, refresh, lifecycle

All tests use JUnit 5 (Jupiter) with `@BeforeAll` for HAL initialization (`HAL.initialize(500, 0)`).

---

## Libtest Architecture (`com.team271.libtest`)

> **NOTE: PLANNED IMPLEMENTATION — MOSTLY NOT YET CREATED**
>
> Currently only `Main.java` and `Robot.java` exist. The files listed below are the **planned architecture** for the test robot. When reviewing, focus on the two existing files and verify they follow the patterns below. Do NOT create the planned files unless explicitly asked.

### Existing Files

- **`Main.java`** — WPILib `RobotBase` entry point (`RobotBase.startRobot(Robot::new)`)
- **`Robot.java`** — extends `TimedRobot`, orchestrates lifecycle:
  - `robotInit()`: subsystems init → `mSubsystemManager.robotInit()` → `CTREManager.init()` (must be AFTER subsystem init)
  - `robotPeriodicBefore()`: `CTREManager.refreshAll()` → timestamp update → `mSubsystemManager.robotPeriodicBefore()`
  - `robotPeriodicAfter()`: `mSubsystemManager.robotPeriodicAfter()` → `mSubsystemManager.outputTelemetry()` → `CTREManager.outputTelemetry()`

### Planned Files (Not Yet Created)

```
libtest/
├── Main.java                    ← EXISTS
├── Robot.java                   ← EXISTS
├── Config.java                  ← PLANNED (Mode: REAL/SIM/REPLAY, RobotType enum)
├── Constants.java               ← PLANNED (CAN IDs, bus names, controller ports, physical dimensions)
├── Globals.java                 ← PLANNED (static singleton references to all subsystems)
├── subsystems/
│   ├── Infrastructure.java      ← PLANNED (PDH, gyro, teleop/auto mode tracking)
│   ├── Superstructure.java      ← PLANNED (high-level coordination, ROBOT_STATE enum)
│   └── Input/
│       ├── InputDriver.java     ← PLANNED (driver controller)
│       └── InputOp.java         ← PLANNED (operator controller)
└── auto/
    ├── auto_modes/Auto0.java    ← PLANNED (default autonomous mode)
    └── auto_moves/              ← PLANNED (autonomous movement actions)
```

### Planned Config Modes
- `Mode.REAL` — running on physical robot hardware
- `Mode.SIM` — running in WPILib simulation (uses `DriverStationSim`, simbot type)
- `Mode.REPLAY` — replaying from a log file (real robot types in sim environment)

### Planned Subsystem Init Order (in `Robot.robotInit()` — order matters)
1. InputDriver
2. InputOp
3. Infrastructure
4. EncoderTest
5. TransmissionTest
6. Superstructure
7. `mSubsystemManager.robotInit()` (calls robotInit on all subsystems)
8. `CTREManager.init()` (optimizes bus, builds signal arrays — must be AFTER subsystem init)

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
- Encoder/IMU configs use `ConstantsLib.CAN_LONG_TIMEOUT_MS` (100ms) timeout with the same 5 retries
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

## SysId / Characterization

The `sysid/` package provides system identification support:

- **`Logger.java`**: SmartDashboard-based data collection for quasistatic and dynamic tests. Collects voltage, position, and velocity data for feed-forward characterization.
- **`LoggerGeneral.java`**: CSV-based data logging extension for general characterization.
- **`SensorMode.SYSID`**: Triggers characterization mode in subsystems, allowing SysId voltage commands to override normal control.

---

## WPILib Required Patterns

### NetworkTables
- Use `NTEntry` wrapper for pub/sub — supports boolean, double, long, int, String
- NTEntry caches last published value to avoid redundant network traffic (uses `Util.epsilonEquals()` for doubles, `.equals()` for strings)
- All NT publishing happens in `outputTelemetry()` methods

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
