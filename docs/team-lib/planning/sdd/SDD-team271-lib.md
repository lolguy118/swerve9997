# SDD: `com.team271.lib` — Root Library

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-ROOT |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | LIB-001 through LIB-NNN (SRS §3) |

## 1. Purpose

Describes the root library infrastructure: the TObj lifecycle contract,
Named, Lifecycle, TRobot, WPILib integration hooks, tuning infrastructure,
and simulation architecture. All other SDDs depend on the contracts defined
here.

## 2. Scope and Boundaries

**This SDD covers:**

- `TObj`, `Named`, `Lifecycle` base classes
- `TRobot` and WPILib `IterativeRobotBase`/`TimedRobot` integration
- Tuning infrastructure (`LoggedNTInput` pipeline)
- Simulation architecture (library responsibility only)
- `CTREManager` orchestration (bulk CAN refresh lifecycle)
- `HardwareManager` as the forward-compatible entry point for `refreshAll()`

**This SDD does not cover:**

- Motor/sensor implementations → [SDD-hardware.md](SDD-hardware.md)
- Subsystem state machines → [SDD-subsystem.md](SDD-subsystem.md)
- Specific PID implementations → [SDD-control.md](SDD-control.md)

## 3. Module Decomposition

### 3.1 `Lifecycle` and `Named`

Interfaces at the root of the library.

- **`Lifecycle`** — declares every lifecycle hook as a default no-op:
  `robotInit`, `robotPeriodicBefore`, `robotPeriodicAfter`,
  `outputTelemetry`, plus mode-specific init/periodic/exit pairs for
  disabled, autonomous, teleop, simulation, and test. All periodic
  hooks receive a `timestamp` (seconds); `outputTelemetry` takes no
  argument because it publishes cached values.
- **`Named`** — declares identity (`getName()`) and NetworkTables
  namespace (`getTable()`, `logKey()`). Used together with
  `Lifecycle` for objects that participate in the lifecycle without
  inheriting from `TObj`.

### 3.2 `TObj`

`TObj implements Lifecycle, Named` and is the standard base class
for library objects. It provides empty lifecycle implementations
(subclasses override as needed) and automatic NetworkTables
namespacing: passing a parent `TObj` to the constructor creates a
subtable such as `/Drivetrain/LeftTransmission/Leader`. The
namespace hierarchy requires no manual path construction and is the
foundation for how tunables and telemetry are organized.

### 3.3 `TRobot`

`TRobot` is the library-provided robot singleton that robot projects
extend. It hooks into WPILib `TimedRobot` (`IterativeRobotBase`) and
is the call site for `HardwareManager.refreshAll()`,
`SubsystemManager` lifecycle broadcasts, and crash-reporting
integration. Robot projects add their own subsystems inside
`TRobot.robotInit()` overrides.

### 3.4 `HardwareManager` and `CTREManager` (Refresh Entry Points)

- **`HardwareManager`** — the forward-compatible entry point for
  `refreshAll()`. Today it delegates to `CTREManager.refreshAll()`;
  in the future it will also refresh any `SignalRefreshable`
  non-CTRE devices. Robot startup code should call
  `HardwareManager.refreshAll()` per
  [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md).
- **`CTREManager`** — owns the complete StatusSignal registry,
  optimizes bus utilization, and performs the bulk refresh. Full
  details live in [SDD-hardware.md §CTREManager](SDD-hardware.md).

### 3.5 Tuning Infrastructure

The tuning pipeline uses three NT classes with distinct roles:

| Class | Direction | Change Detection | Use |
| ----- | --------- | ---------------- | --- |
| `NTTable` | — | — | Hierarchical namespace container |
| `NTEntry` | Output only | No | Telemetry publishing (also writes AdvantageKit log) |
| `LoggedNTInput` | Input + Output | Yes (`hasChanged()`) | Dashboard-tunable parameters |

`LoggedNTInput` is the primary tunable API; every read is also
logged to AdvantageKit so replays reproduce the exact value used on
each cycle. See [SDD-nt.md](SDD-nt.md) for the full pipeline.

The `checkTuning()` pattern is the canonical usage:

```java
@Override
public void outputTelemetry() {
    checkTuning();           // detect + apply dashboard changes
    super.outputTelemetry(); // parent telemetry
    // publish current values via NTEntry
}
```

Run once per cycle as part of
`SubsystemManager.outputTelemetry()`.

### 3.6 Simulation Architecture

The library provides two complementary simulation layers:

- **CTRE SimState (device level)** — every CTRE device has a
  SimState object (`TalonFXSimState`, `CANcoderSimState`,
  `Pigeon2SimState`, `CANrangeSimState`). These are initialized in
  the device wrapper's `simulationInit()` and updated each
  `simulationPeriodic()` with the current battery voltage.
- **WPILib DCMotor (physics level)** — `TransmissionBase` creates a
  `DCMotor` model matching the motor type and count (including
  followers). Robot projects use this model with
  `SingleJointedArmSim`, `ElevatorSim`, `FlywheelSim`, etc.

The library publishes infrastructure; robot projects implement the
physics. `transmission.setSimPosRotations()` propagates through the
entire stack (encoder sim, controller sim state, internal encoder
reads). See
[library-architecture.md §Simulation Architecture](SDD-team271-lib.md#simulation-architecture)
for the full capability matrix.

## 4. Data Flow

The canonical robot cycle flows through these steps in order:

```text
Robot.robotInit()                                 // once at startup
  → SubsystemManager.robotInit()
    → forEachSafe(s -> s.robotInit(t))            // not isolated — failure is fatal
      → each subsystem registers CTRE signals
  → CTREManager.init()                            // signal array build + bus optimization

Robot.robotPeriodic() @ 50 Hz                     // every loop
  → HardwareManager.refreshAll()                  // bulk CAN refresh
    → CTREManager.refreshAll()
      → BaseStatusSignal.refreshAll(signalArray)
  → SubsystemManager.robotPeriodicBefore()        // read sensors, update isConnected
  → SubsystemManager.<mode>Periodic()             // state machine logic
  → SubsystemManager.robotPeriodicAfter()         // apply commanded outputs
  → SubsystemManager.outputTelemetry()
    → each TObj publishes NTEntry + runs checkTuning()
    → Alert.outputTelemetry()                     // all alert groups to AdvantageKit

Robot.simulationPeriodic() @ 50 Hz (sim only)
  → each CTRE device updates SimState supply voltage
  → robot-specific physics models step via transmission.setSimPos/Vel
```

The separation of `robotPeriodicBefore` (reads) from
`robotPeriodicAfter` (writes) ensures all sensor reads finish
before any state decisions, and all state decisions finish before
hardware is commanded — eliminating single-cycle race conditions.

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Standalone library separate from robot projects | See ADR-001 | [ADR-001](../adr/ADR-001-team271-lib-standalone-library.md) |
| 6-layer architecture | See ADR-004 | [ADR-004](../adr/ADR-004-layered-architecture.md) |
| AdvantageKit for telemetry | See ADR-012 | [ADR-012](../adr/ADR-012-advantagekit-logging.md) |
| LoggedNTInput-backed tuning | See ADR-008 | [ADR-008](../adr/ADR-008-logged-nt-input-backed-tuning.md) |
| `TObj` instead of WPILib `SubsystemBase` | Finer-grained lifecycle hooks, exception isolation, sim hooks | [library-architecture.md §Why TObj](SDD-team271-lib.md#why-tobj-instead-of-wpilib-subsystem) |
| Singletons for managers (`SubsystemManager`, `CTREManager`) | Exactly one robot, one CAN system, one registry | [library-architecture.md §Why Singletons](SDD-team271-lib.md#why-singletons-for-managers) |
| Centralized bulk CAN refresh | Consistent timestamps, lower bus utilization | [ADR-007](../adr/ADR-007-centralized-can-refresh.md) |

## 6. Error Handling

The root library's fault-tolerance contract:

- **Per-subsystem exception isolation** — wrapped in
  `SubsystemManager.forEachSafe()`. One broken subsystem does not
  stop others. Error notifications are throttled per subsystem
  (throttle interval in `ConstantsLib`). See
  [SDD-subsystem.md](SDD-subsystem.md) and
  [fault-tolerance.md §Exception Isolation](SDD-subsystem.md#exception-isolation-subsystemmanager).
- **`robotInit()` failure is fatal** — not isolated, by design. A
  partially initialized robot is unsafe.
- **TRobot-level uncaught exceptions** — propagate through the
  WPILib `TimedRobot` loop. Robot projects can add a top-level
  uncaught exception handler that routes crash reports to Elastic.
- **Global notification throttling** — any library component that
  emits Elastic notifications from the periodic path applies the
  library's standard throttle interval. The canonical pattern is
  documented in
  [fault-tolerance.md §Error Notification Throttling](SDD-subsystem.md#error-notification-throttling).
- **Safe defaults** — every subsystem that can fail has a safe
  default output: motors guard on `isConnected`, inputs return 0.0
  when disconnected, PID output is clamped to `[minOutput, maxOutput]`,
  integral term bounded by `[iMin, iMax]` and zeroed outside
  `iZone`. Full inventory in
  [fault-tolerance.md §Safe Defaults](SDD-subsystem.md#safe-defaults).

## 7. Platform Portability Notes

The library targets two runtime environments:

- **RoboRIO 2 / WPILib 2026** — the production target. `TRobot`
  extends WPILib `TimedRobot` at the WPILib default loop period.
  CTRE Phoenix 6 operates on real CAN hardware.
- **Desktop simulation** — `./gradlew simulateJava`. WPILib HAL
  simulation backend provides NT4, Timer, and device lifecycle
  services. CTRE Phoenix 6 firmware simulation models device
  responses including a 1 kHz internal PID loop for TalonFX.

All library code is platform-neutral — no runtime platform
branches. The `robotInit()` / `simulationInit()` /
`simulationPeriodic()` split in the lifecycle contract is the
mechanism by which robot projects plug in platform-specific physics.

Unit tests (JUnit 5) run in a third context with HAL initialized
but no CTRE simulation tick — they exercise construction,
configuration, and pure-Java math. See
[testing-strategy.md §Desktop Simulation vs Unit Tests](../SVP.md#desktop-simulation-vs-unit-tests).

## 8. Configuration

The root library has no robot-side configuration surface of its
own. Configuration enters the system through:

- **Robot project `Constants`** — CAN IDs, gear ratios, PID
  defaults, homing timeouts, current limits, physical dimensions.
  The library never compiles numeric tunables into its own code.
- **`robotInit()` overrides** — robot projects construct their
  subsystems and call `SubsystemManager.addSubsystem()` in
  deterministic order.
- **Dashboard tunables** — adjustable at runtime via
  `LoggedNTInput`. Per [ADR-008](../adr/ADR-008-logged-nt-input-backed-tuning.md),
  every tunable has a safe default so the robot runs correctly
  without a dashboard connected.
- **Library constants (`ConstantsLib`)** — CAN retry counts, config
  timeouts, notification throttle intervals. These are named
  constants inside the library package, not exposed as tunables.

Per [.claude/rules/docs.md](../../../../.claude/rules/docs.md), design
documents reference constant names (`kHomingTimeoutSec`,
`CAN_RETRY_COUNT`) — never their numeric values.

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| `TObj` NT hierarchy | Yes (NT4) | Verify child table path construction |
| `Lifecycle` / `Named` defaults | No | Pure interface; test default no-ops compile |
| `TRobot` boot | Yes | Verify `SubsystemManager` wiring without crash |
| `HardwareManager.refreshAll` | Yes | Delegation verified by CTREManager tests |
| `libtest` integration harness | Yes | Full Infrastructure + Superstructure assemble |
| Crash-report routing | Yes | Optional; robot-project-specific |

HAL initialization and CTREManager reset requirements are documented
in [testing-strategy.md](../SVP.md).
The `libtest` harness assembles a minimal full-robot stack for
cross-layer smoke tests; it is excluded from JaCoCo coverage
metrics.

Test IDs: TEST-LIB-NNN plus cross-cutting TEST-* IDs from the
dependent SDDs.
