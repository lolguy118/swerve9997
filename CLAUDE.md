<!-- markdownlint-disable MD013 MD060 -->
# Team271-Lib

Team 271's reusable FRC robot library. The codebase uses WPILib, CTRE Phoenix 6, PathPlanner, and AdvantageKit for logging.

## Reference Documentation

- Phoenix 6 Docs: <https://v6.docs.ctr-electronics.com/en/stable/index.html>
- Phoenix 6 Java API: <https://api.ctr-electronics.com/phoenix6/stable/java/>
- Phoenix 6 Examples: <https://github.com/CrossTheRoadElec/Phoenix6-Examples/tree/main/java>
- WPILib Docs: <https://docs.wpilib.org/en/stable/>
- WPILib Java API: <https://github.wpilib.org/allwpilib/docs/release/java/index.html>
- WPILib Source: <https://github.com/wpilibsuite/allwpilib>
- PathPlanner: <https://pathplanner.dev/home.html>
- PathPlanner GitHub: <https://github.com/mjansen4857/pathplanner>
- AdvantageKit Docs: <https://docs.advantagekit.org/>
- AdvantageKit Source: <https://github.com/Mechanical-Advantage/AdvantageKit>
- Elastic Dashboard Docs: <https://frc-elastic.gitbook.io/docs>
- Elastic Dashboard Source: <https://github.com/Gold872/elastic_dashboard>

## Design Documents

- [Start Here](docs/team-lib/start-here.md) — new contributor orientation, mental models, and guided reading order
- [Java Coding Standard](docs/team-lib/team271-java-coding-standard.md) — formatting, safety rules, naming conventions, state machine patterns
- [Contributing Guide](CONTRIBUTING.md) — pre-commit checks, build verification, commit conventions
- [Library Architecture](docs/team-lib/library-architecture.md) — TObj hierarchy, subsystem lifecycle, hardware abstraction stack
- [Auto Design](docs/team-lib/auto-design.md) — move composition, AutoMode lifecycle, sequencing patterns
- [Hardware Abstraction](docs/team-lib/hardware-abstraction.md) — controllers, transmissions, sensors, input system
- [Control System](docs/team-lib/control-system.md) — PID variants, selection criteria, Balance algorithm
- [Testing Strategy](docs/team-lib/testing-strategy.md) — HAL initialization, test isolation, coverage patterns
- [Fault Tolerance](docs/team-lib/fault-tolerance.md) — library-level fault patterns, timeout protection, recovery
- [Code Review Prompt](docs/prompts/code-review-prompt-teamlib.md) — architecture reference and review checklist
- [Development Setup](docs/team-lib/development-setup.md) — getting started guide for new contributors
- [SysID Workflow](docs/team-lib/sysid-workflow.md) — system identification and characterization
- [Vendor Dependencies](docs/team-lib/vendor-dependencies.md) — vendordep management and upgrades
- [Geometry Package](docs/team-lib/geometry-package.md) — 2D pose and transformation utilities
- [Utility Package](docs/team-lib/utility-package.md) — Alert, Elastic notifications, DriveSignal, math utilities
- [Input Shaping Guide](docs/team-lib/input-shaping-guide.md) — joystick input curve selection and comparison
- [Simulation Guide](docs/team-lib/simulation-guide.md) — robot project physics simulation implementation
- [Documentation Index](docs/team-lib/documentation-index.md) — package-to-doc mapping and reading order

---

## Software Architecture

### Framework

- Robot extends LoggedRobot (AdvantageKit) — logs to /U/logs on RoboRIO + NT4
- All subsystems extend com.team271.lib.subsystem.Subsystem (our team library base class)
- Singleton pattern with SubsystemManager lifecycle orchestration
- State machine pattern: desired state set in teleopPeriodic/autonomousPeriodic, applied in robotPeriodicAfter
- TransmissionFX wraps TalonFX motors (voltage, position, velocity, Motion Magic control)
- Subsystem base class provides SensorMode (SENSORED_AUTO, SENSORED_MANUAL, SENSORLESS, SYSID), isZeroed for homing, and sensorsZero() override
- CTREManager singleton centralizes CAN signal refresh at 250 Hz
- All CTRE control requests use timesync (UpdateFreqHz=0)

### Lifecycle Flow

See `docs/team-lib/team271-java-coding-standard.md` Appendix D for the full lifecycle reference.

```text
Robot.robotPeriodic()
  → CTREManager.refreshAll() — bulk CAN signal refresh
  → SubsystemManager.robotPeriodicBefore() — read sensors
  → <mode>Periodic() — state machine logic
  → SubsystemManager.robotPeriodicAfter() — apply outputs
  → SubsystemManager.outputTelemetry() — publish NT
```

---

## Constants Reference

All tunable values (voltages, speeds, current limits, timing windows, thresholds) live in code, not in this document. All configurable values must be dashboard-tunable at runtime per [CODE-BUG-004](docs/team-lib/team271-java-coding-standard.md).

---

## Auto Coordination (move-based composition)

Autonomous routines are built from composable `AutoMove` building blocks (see [Auto Design](docs/team-lib/auto-design.md)). `Superstructure` selects and runs the active `AutoMode`, which sequences moves. Each move commands subsystems directly via `Globals.*.setAuto*()` methods — subsystems do not poll a shared timer. Timing is expressed through `AutoMoveParallel`, `AutoMoveSequence`, and `WaitMove` composition rather than timing-window constants.

---

## Documentation Rules

### Code Changes That Affect Design Require Doc Updates

When making code changes that alter subsystem behavior, state machines, control flow, controller bindings, or cross-subsystem coordination, **ask whether the design docs need updating before considering the change complete.** The design docs describe *how the robot works* — if the robot now works differently, the docs are wrong until updated.

Examples of code changes that require a doc update prompt:

- Adding, removing, or renaming a control state or shot mode
- Changing controller button bindings or trigger thresholds
- Adding or removing a subsystem, motor, or sensor
- Changing the feeding chain, velocity gating logic, or post-shoot sequence
- Adding a new auto path or changing auto coordination
- Changing homing behavior, soft limits, or safety logic
- Adding or removing telemetry keys

When in doubt, ask. A 30-second doc update now prevents a 30-minute debugging session later when someone reads stale docs.

### No Tunable Values in Docs

Design documents and CLAUDE.md must **never** contain hard-coded tunable values (PID gains, voltages, current limits, duty cycles, RPS/RPM targets, timing windows, thresholds, soft limits). The source of truth for all tunables is the code (`Constants.java`, `TunerConstants.java`, subsystem classes).

**In docs, reference constant names instead of numbers:**

- Tables: use the constant name in the value column (e.g., `kIntakeVoltage`, `P_LOOP_KP`)
- Prose: use qualitative descriptions + constant references (e.g., "drives slowly at homing voltage (`kHomingVoltage`)")
- Auto timing: timing is expressed in the auto mode constructors via move composition — never duplicate timing values

**What CAN appear in docs:**

- Datasheet values (motor free speed, kV, stall torque) — these come from the manufacturer
- Physical dimensions (gear ratios, wheel diameter, roller diameter, track width) — these come from mechanical design
- Physics derivations using datasheet values (max surface speed, max roller RPS)
- CAN IDs — hardware reference
- Design rationale explaining *why* a value was chosen (but reference the constant, not the number)

When writing or updating design docs, always check that you are not introducing new numeric tunables.

### Planned Features Must Be Clearly Marked

Any doc section describing functionality that is **not yet implemented** must begin with:

> **Status: Planned — Not Yet Implemented.**

Remove the callout when the feature is implemented. This prevents someone from assuming a feature works because it appears in a design document. If an enum value exists in code but is not yet functional (e.g., a `ShotMode` with placeholder constants), note that in both the code comment and the design doc.

### No Cross-Doc Content Duplication

When multiple design docs describe the same cross-subsystem behavior, **one doc is the authoritative source** and the others link to it. Do not copy details between docs.

### CLAUDE.md Stays High-Level

CLAUDE.md provides the **overview** and links to design docs for details. Do not expand CLAUDE.md sections to match design doc depth. If a CLAUDE.md section grows past ~5 lines of detail, move the content to the appropriate design doc and replace it with a link.

### Telemetry Keys Must Match `outputTelemetry()`

Each design doc has a Telemetry table listing published keys. When adding or removing keys in a subsystem's `outputTelemetry()` method, update the telemetry table in the corresponding design doc in the same change. Do not add telemetry keys to code without documenting them.

### All Waiting Operations Must Have Timeouts

Any operation that waits for a condition (current threshold, velocity target, position arrival, sensor signal, path completion) **must** have a timeout that exits the waiting state and alerts the driver. This prevents the robot from locking up if the expected condition never occurs (e.g., motor stall not detected, launcher never reaching speed, path waypoint unreachable).

Requirements:

- The timeout constant must be a named constant in the subsystem's Constants class, not a magic number
- On timeout, the subsystem must **fail safe**: stop motors, restore default current limits, transition to IDLE
- On timeout, send a driver notification via Elastic so the driver knows something went wrong
- The timeout and its fail-safe behavior must be documented in the subsystem's design doc

Applies to: homing sequences (coding standard 4.9c), launcher spin-up waits, path following waypoint arrival, and any future sensor-based gating or blocking state.

---

## Code Review

See `docs/team-lib/team271-java-coding-standard.md` for the full coding standard, safety rules (Section 4.9), and code review checklist (Section 5.4).

### Library Review Items

These apply to all projects using Team271-Lib:

- Unit consistency (rotations vs radians, RPS vs RPM, meters vs inches)
- Correct Phoenix 6 API patterns, no deprecated calls
- No duplicate device objects on the same CAN ID
- Proper use of timesync and latency compensation
- StatusSignal refresh patterns correct
- Sensor enable/disable propagating current limits correctly
- CAN refresh rate sustainable under load
- All operations that wait for a condition have timeout protection with driver notification (coding standard 4.9c)
- Fault tolerance scenarios handled per [Fault Tolerance](docs/team-lib/fault-tolerance.md) and coding standard Section 4.9 (CODE-SAF-008–011)

---
---

## Project-Specific Configuration

> **Everything below this line is specific to this project / season.**
> The sections above are generic to Team271-Lib and can be shared
> across robot projects. When copying this CLAUDE.md to a new robot
> project, keep everything above and replace everything below with
> your project-specific details.

### Current Versions

| Dependency | Version |
|-----------|---------|
| Phoenix 6 | 26.1.3 |
| AdvantageKit | 26.0.2 |
| PathplannerLib | 2026.1.2 |
| GradleRIO | 2026.2.1 |
| Java | 17 |

### Project-Specific Review Items

- Verify Phoenix 6 v26.1.3 API patterns specifically — check release notes for any breaking changes from prior versions
