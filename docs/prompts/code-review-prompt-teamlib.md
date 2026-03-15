Please comprehensively review our FRC robot library codebase. The codebase uses the latest version of WPILib and CTRE Phoenix 6. AdvantageKit and PathPlanner are available as dependencies for future integration. The `com.team271.libtest` package contains a full test robot for validating the library in simulation and on real hardware.

# Team 271 Library Code Review Prompt

Use this document as context when reviewing changes to `com.team271.lib` and `com.team271.libtest`. It describes the library architecture, required patterns, and known pitfall categories so you can catch bugs specific to this codebase.

---

## Reference Documentation

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
│   │   ├── Pneumatic shifter support (DoubleSolenoid, gear ratios per gear)
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
│   │   └── PIDSimpleTest.java (only unit test in library)
│   └── Balance.java (dead code from 2025 — candidate for removal)
├── geometry/ (custom implementations, not WPILib wrappers)
│   ├── Pose2d, Rotation2d, Translation2d, Twist2d, State
│   └── Interfaces: IPose2d, IRotation2d, ITranslation2d
├── nt/ (NTTable, NTEntry — NetworkTables pub/sub wrappers with value caching)
├── wpilib/ (IterativeRobotBase, TimedRobot — custom WPILib base classes)
├── sysid/ (Logger, LoggerGeneral — SmartDashboard-based SysId characterization)
├── misc/ (Elastic — dashboard notifications)
├── auto/ (AutoMode, AutoMove, AutoMoveSingle, AutoMoveTimed)
└── util/ (Util, Alert, DriveSignal, CSVWritable, Interpolable)
```

### Key Design Patterns

1. **TObj Lifecycle**: Every component extends `TObj` and receives lifecycle callbacks: `robotInit()`, `robotPeriodicBefore()`, `robotPeriodicAfter()`, `autonomousInit/Periodic/Exit()`, `teleopInit/Periodic/Exit()`, `simulationInit/Periodic()`, etc.

2. **Subsystem State Machines**: Subsystems use a desired-state/actual-state pattern. Desired state is set in `teleopPeriodic()`/`autonomousPeriodic()`, then applied in `robotPeriodicAfter()`. State transitions (timer resets, etc.) happen at the boundary in `robotPeriodicAfter()`.

3. **Singleton Pattern**: All subsystems and the SubsystemManager use lazy-initialized singletons with `getInstance()`. Private constructors prevent external instantiation.

4. **SubsystemManager Ordering**: Subsystems are added in a specific order in `Robot.robotInit()`. The iteration order determines which subsystem's `robotPeriodicBefore()` runs first. This matters for cross-subsystem data dependencies (e.g., Launcher velocity must update before Index reads it).

5. **TransmissionFX Wraps TalonFX**: Motor commands go through TransmissionFX, which handles encoder integration, gear ratios, unit conversions, and timesync control requests. Do not create raw TalonFX objects for subsystem motors.

6. **Exception Isolation**: `SubsystemManager.forEachSafe()` wraps subsystem callbacks in try-catch to prevent one subsystem's exception from crashing the entire robot.

---

## Libtest Architecture (`com.team271.libtest`)

The libtest package is a **complete FRC robot implementation** used for testing library components in simulation and on real hardware. It is the deploy target (`Main.java` is the robot entry point in `build.gradle`).

```
libtest/
├── Main.java (WPILib RobotBase entry point)
├── Robot.java (extends TimedRobot — lifecycle orchestration)
├── Config.java (Mode: REAL/SIM/REPLAY, RobotType: ROBOT_2024C/ROBOT_2024P/ROBOT_SIMBOT)
├── Constants.java (CAN IDs, bus names, controller ports, physical dimensions)
├── Globals.java (static singleton references to all subsystems)
├── LimelightHelpers.java (Limelight vision processing utilities)
├── subsystems/
│   ├── Infrastructure.java (PDH, gyro, teleop/auto mode tracking)
│   ├── EncoderTest.java (encoder verification subsystem)
│   ├── TransmissionTest.java (motor/transmission testing subsystem)
│   ├── Superstructure.java (high-level coordination, ROBOT_STATE enum)
│   └── Input/
│       ├── InputDriver.java (driver controller)
│       └── InputOp.java (operator controller)
└── auto/
    ├── auto_modes/Auto0.java (default autonomous mode)
    └── auto_moves/ (autonomous movement actions)
```

### Libtest Key Details

**Subsystem Init Order** (in `Robot.robotInit()` — order matters):
1. InputDriver
2. InputOp
3. Infrastructure
4. EncoderTest
5. TransmissionTest
6. Superstructure
7. → `mSubsystemManager.robotInit()` (calls robotInit on all subsystems)
8. → `CTREManager.init()` (optimizes bus, builds signal arrays — must be AFTER subsystem init)

**Config Modes**:
- `Mode.REAL` — running on physical robot hardware
- `Mode.SIM` — running in WPILib simulation (uses `DriverStationSim`, `ROBOT_SIMBOT` type)
- `Mode.REPLAY` — replaying from a log file (real robot types in sim environment)

**CAN Bus Names** (from `Constants.java`):
- `"rio"` — RoboRIO built-in CAN bus
- `"271"` — CANivore CAN bus

**Robot Lifecycle Flow**:
1. `robotPeriodicBefore()`: `CTREManager.refreshAll()` → timestamp update → `mSubsystemManager.robotPeriodicBefore()`
2. `robotPeriodicAfter()`: `mSubsystemManager.robotPeriodicAfter()` → `mSubsystemManager.outputTelemetry()`

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
- Target update frequency: 250Hz for most signals

### Config Application
- Motor configs are applied via `ControllerTalonFX.applyConfig()` which retries up to 5 times with 50ms timeout per attempt
- Always call `applyConfigs()` after changing config fields — config objects are mutable but not applied until explicitly sent

### Latency Compensation
- Encoders (EncoderFXComp, EncoderCANCoderComp) use `BaseStatusSignal.getLatencyCompensatedValue(posSignal, velSignal)` for accurate position
- IMUPigeon2 uses latency compensation for yaw with yaw rate as reference

### Bus Optimization
- `ParentDevice.optimizeBusUtilizationForAll()` is called in `CTREManager.init()` to disable unused status frames
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
- `Config.getMode() == Mode.SIM` triggers simulation code paths
- `DriverStationSim.setAllianceStationId()` used to set alliance in sim (defaults to Blue1)
- WPILib simulation GUI enabled via `wpi.sim.addGui()` in `build.gradle`

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
- Supports `// spotless:off` / `// spotless:on` toggle comments for sections that must not be reformatted (e.g., lookup tables, ASCII art)

### .editorconfig
- IDE-agnostic formatting hints at the repository root
- Java: 4-space indent, 120-char line length, UTF-8, LF line endings
- JSON/YAML: 2-space indent
- Markdown: trailing whitespace preserved (intentional line breaks)

### Workflow
1. Write code normally in your IDE (most IDEs auto-apply `.editorconfig` rules)
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

## Known Bug Patterns to Watch For

These are specific bug categories that have been found in this codebase. Check every PR for these:

### 1. Wrong Variable in Assignment
The desired-state/actual-state pattern means there are pairs like `mDesiredState` and `mControlState`. Writing to the wrong one (e.g., `mControlState` instead of `mDesiredState` in a periodic method) causes the state to be silently overwritten in `robotPeriodicAfter()`.

**Check:** In `teleopPeriodic()` and `autonomousPeriodic()`, all state assignments should target the *desired* state variable, not the actual state variable.

### 2. else-if Chains for Independent Systems
When multiple independent subsystems (e.g., intake extension + intake rollers) need time-based control in autonomous, using `else if` means only one can be active per cycle. Independent systems need independent `if` blocks.

**Check:** Auto timing logic where multiple outputs need to overlap in time windows.

### 3. getCLError() vs getCLOutput() Confusion
`ControllerTalonFX` has both `getCLError()` (closed-loop tracking error) and `getCLOutput()` (closed-loop controller output). The TransmissionBase wrapper has the same methods. These are different values — error is the setpoint-minus-measurement, output is the PID controller's response.

**Check:** Any code using `getCLError()` to make decisions (like feed-forward direction) is actually reading error, not output.

### 4. Recursive Method Calls (Missing `super.`)
When overriding a method and wanting to call the parent implementation, forgetting `super.` causes infinite recursion. This is especially dangerous in `reset()`, `stop()`, and lifecycle methods.

**Check:** Any method that calls a method with the same name should use `super.` if targeting the parent.

### 5. `final` Fields in Mutable Config
If a config object (like `TrapezoidProfile.Constraints`) is declared `final`, it can't be reassigned. Methods that accept new config parameters must use the parameter directly rather than trying to reassign the field.

**Check:** Methods that take config parameters and appear to store them — verify the assignment actually works.

### 6. Debug Prints Left In Production
`System.out.println()` and `System.err.println()` in production code spam the DS console and can mask important warnings.

**Check:** No `System.out.println` or `System.err.println` in any production code path. Use `DriverStation.reportError/Warning()` or `NTEntry.publish()` for telemetry instead.

### 7. Signal Null Safety
CTRE StatusSignals can be null if the device doesn't exist or the signal wasn't registered. Always guard with:
```java
if (signal != null && signal.getStatus().isOK()) {
    value = signal.getValueAsDouble();
}
```
Never call `.getValue()` or `.getValueAsDouble()` without checking status first.

### 8. String Comparison with == Instead of .equals()
Java string interning means `==` sometimes works, but it's unreliable. All String comparisons must use `.equals()`.

**Check:** `equals()` implementations, caching comparisons, bus name comparisons.

### 9. Telemetry Publishing Wrong Value
NT telemetry entries sometimes publish the wrong getter (e.g., `tError.publish(getCLOutput())` when it should be `getCLError()`). The entry name suggests one thing but the value is another.

**Check:** Every `publish()` call — does the NT entry name match the value being published?

### 10. Missing Auto Path Cases
When adding new auto modes, ALL subsystems that participate in auto must have cases for the new path. Missing a subsystem means it sits idle during that auto.

**Check:** If a new `AutoPaths` enum value is added, search for all `switch`/`if` on `getAutoPath()` across every subsystem.

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
- [ ] New auto paths have cases in ALL participating subsystems (Drive, Intake, Index, Launcher)
- [ ] Timing windows don't use else-if for independent subsystems
- [ ] Auto timer read from `Drive.autoTimer.get()` (shared timer)
- [ ] Launcher velocity gate (`Launcher.isAtMaxVelocity()`) checked before indexing
- [ ] Alliance flipping handled for new paths

### PID / Closed-Loop Changes
- [ ] PID gains set to correct slot (Slot 0, 1, or 2)
- [ ] `getCLError()` used for error, `getCLOutput()` used for output (not swapped)
- [ ] Feed-forward direction based on error sign, not output sign
- [ ] `setOutputRange()` called (note: was previously misspelled as `setOutputRage`)

### Safety
- [ ] Homing sequences have timeout protection
- [ ] Soft limits only enforced after `isZeroed` is true
- [ ] Voltage limits prevent reverse drive where needed (e.g., launcher coast-only)
- [ ] No `System.out.println` / `System.err.println` in production paths
- [ ] Exception handling in periodic code logs and continues (doesn't crash robot)

### Null Safety
- [ ] StatusSignals checked for null AND `.getStatus().isOK()` before reading
- [ ] Optional values from WPILib (e.g., `DriverStation.getAlliance()`) have fallbacks
- [ ] Singleton access throws clear error if not initialized
- [ ] Controller disconnect handled gracefully (check `isConnected()`)

### Cross-Subsystem Dependencies
- [ ] SubsystemManager add order preserved (Launcher before Index)
- [ ] Static shared state (e.g., `Launcher.isAtMaxVelocity`) updated before consumers read it
- [ ] Shared timers (`Drive.autoTimer`) started/stopped in the correct subsystem

### Simulation Changes
- [ ] DCMotor model matches actual motor type in MotorBase
- [ ] Sim state updated every cycle in `simulationPeriodic()`
- [ ] Gear ratios applied correctly in sim (same as real hardware)
- [ ] `DriverStationSim` alliance set in sim mode
- [ ] New hardware classes implement `simulationInit()` and `simulationPeriodic()`

### Libtest / Test Robot Changes
- [ ] `Config.getMode()` correctly handles REAL/SIM/REPLAY paths
- [ ] New subsystems added to `Robot.robotInit()` in correct order
- [ ] `Globals` singleton references set before use
- [ ] Simulation methods (`simulationInit`/`simulationPeriodic`) update sim state for all hardware
- [ ] LimelightHelpers calls handle null/missing camera gracefully
- [ ] CAN IDs in `Constants.java` don't conflict (no duplicates per device type per bus)

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
- [ ] Line length ≤ 120 characters (`.editorconfig` recommendation)
- [ ] Run `./gradlew spotlessApply` before committing to auto-fix formatting

### Code Quality
- [ ] No `==` for String comparison (use `.equals()`)
- [ ] No recursive calls missing `super.`
- [ ] No `final` fields being "reassigned" (check commented-out reassignment lines)
- [ ] Gear ratio conversions consistent (rotor-to-mechanism vs sensor-to-mechanism)
- [ ] Unit conversions correct (rotations vs radians, RPS vs RPM, meters vs inches)
