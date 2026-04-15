<!-- markdownlint-disable MD013 MD060 -->
# Documentation Index

> **Purpose:** Maps every library package to its design doc and
> provides a reading order for new contributors.

---

## Package-to-Doc Mapping

| Package | Key Classes | Design Doc | Coverage |
|---------|------------|-----------|----------|
| `auto/` | AutoMode, AutoMove, AutoMoveConditional, AutoMoveParallel, AutoMoveSequence, AutoMoveTimed | [Auto Design](auto-design.md) | Full |
| `control/pid/` | PIDBase, PIDSimple, PIDTrap, PIDWPI, PIDWPI_Trap, PIDFX | [Control System](control-system.md) | Full |
| `control/` | Balance | [Control System](control-system.md) | Full |
| `geometry/` | Pose2d, Rotation2d, Translation2d, Twist2d, State | [Geometry Package](geometry-package.md) | Full |
| `hardware/controllers/` | ControllerBase, ControllerSmart, ControllerTalonFX | [Hardware Abstraction](hardware-abstraction.md) | Full |
| `hardware/transmissions/` | TransmissionBase, TransmissionFX, Shifter, ShifterPneumatic | [Hardware Abstraction](hardware-abstraction.md) | Full |
| `hardware/sensors/encoders/` | EncoderBase, EncoderFX, EncoderCANCoder + Comp variants | [Hardware Abstraction](hardware-abstraction.md) | Full |
| `hardware/sensors/imu/` | IMUBase, IMUCTRE, IMUPigeon2 | [Hardware Abstraction](hardware-abstraction.md) | Full |
| `hardware/sensors/range/` | RangeBase, RangeCTRE, RangeCANrange | [Hardware Abstraction](hardware-abstraction.md) | Full |
| `hardware/sensors/switches/` | SwitchBase, SwitchFX, SwitchCANCoder | [Hardware Abstraction](hardware-abstraction.md) | Full |
| `hardware/Input/` | Input, InputXBox, InputPS4, Input8BitDuo, InputEnvisionPro | [Hardware Abstraction](hardware-abstraction.md) + [Input Shaping Guide](input-shaping-guide.md) | Full |
| `hardware/` | CANBus, CANDeviceID, CTREManager | [Library Architecture](library-architecture.md) | Full |
| `nt/` | NTTable, NTEntry, LoggedNTInput | [Library Architecture — Tuning Infrastructure](library-architecture.md#tuning-infrastructure) | Full |
| `subsystem/` | Subsystem, SubsystemManager | [Library Architecture](library-architecture.md) | Full |
| `sysid/` | Logger, LoggerGeneral | [SysID Workflow](sysid-workflow.md) | Full |
| `util/` | Alert, Util, DriveSignal, CSVWritable, Interpolable | [Utility Package](utility-package.md) | Full |
| `misc/` | Elastic | [Utility Package](utility-package.md) | Full |
| `wpilib/` | IterativeRobotBase, TimedRobot | [Library Architecture](library-architecture.md) | Partial |
| Root | TObj, TRobot, ConstantsLib | [Library Architecture](library-architecture.md) | Full |

---

## Cross-Cutting Concerns

| Topic | Authoritative Doc |
|-------|-------------------|
| Tuning infrastructure (LoggedNTInput, checkTuning) | [Library Architecture — Tuning Infrastructure](library-architecture.md#tuning-infrastructure) |
| Simulation architecture (SimState, DCMotor, lifecycle) | [Library Architecture — Simulation Architecture](library-architecture.md#simulation-architecture) |
| Simulation usage for robot projects | [Simulation Guide](simulation-guide.md) |
| Fault tolerance (exception isolation, CAN faults, timeouts) | [Fault Tolerance](fault-tolerance.md) |
| Testing patterns (HAL init, CTREManager cleanup, isolation) | [Testing Strategy](testing-strategy.md) |
| CAN management (bus tracking, signal refresh, timesync) | [Hardware Abstraction](hardware-abstraction.md) + [Library Architecture](library-architecture.md) |
| CTRE Phoenix 6 feature coverage (supported, planned, not implemented) | [Hardware Abstraction — CTRE Feature Coverage](hardware-abstraction.md#ctre-phoenix-6-feature-coverage) |
| CTRE fault coverage (monitored vs. unmonitored faults) | [Fault Tolerance — CTRE Fault Coverage](fault-tolerance.md#ctre-fault-coverage) |
| Phoenix 6 v26-specific features used | [Vendor Dependencies — v26 Features](vendor-dependencies.md#phoenix-6-v26-features-used) |
| Coding standard and safety rules | [Java Coding Standard](team271-java-coding-standard.md) |
| Vendordep management | [Vendor Dependencies](vendor-dependencies.md) |

---

## Reading Order for New Contributors

1. **[Development Setup](development-setup.md)** — Prerequisites, IDE
   setup, build commands, troubleshooting
2. **[Library Architecture](library-architecture.md)** — TObj hierarchy,
   subsystem lifecycle, managers, NT infrastructure, simulation
3. **[Hardware Abstraction](hardware-abstraction.md)** — Controllers,
   transmissions, sensors, input system
4. **[Fault Tolerance](fault-tolerance.md)** — Exception isolation,
   CAN fault handling, safe defaults
5. **Subsystem-specific doc** — whichever doc covers the area you're
   working on (auto, control, geometry, etc.)
6. **[Java Coding Standard](team271-java-coding-standard.md)** —
   Full standard (read at least Sections 3, 4.9, and 5.4 before
   your first PR)
