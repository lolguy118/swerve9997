<!-- markdownlint-disable MD013 MD060 -->
# Documentation Index

> **Purpose:** Maps every library package to its design doc and
> provides a reading order for new contributors.

---

## Package-to-Doc Mapping

| Package | Key Classes | Design Doc | Coverage |
|---------|------------|-----------|----------|
| `auto/` | AutoMode, AutoMove, AutoMoveConditional, AutoMoveParallel, AutoMoveSequence, AutoMoveTimed | [Auto Design](../control/auto-design.md) | Full |
| `control/pid/` | PIDBase, PIDSimple, PIDTrap, PIDWPI, PIDWPI_Trap, PIDFX | [Control System](../control/control-system.md) | Full |
| `control/` | Balance, HardwarePIDController | [Control System](../control/control-system.md) + [Vendor Abstraction Guide](../architecture/vendor-abstraction-guide.md) | Full |
| `hardware/controllers/` | ControllerBase, ControllerSmart, ControllerTalonFX, ControllerTalonFXS | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/motors/` | MotorBase | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/transmissions/` | TransmissionBase, TransmissionFX, Shifter, ShifterPneumatic | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/sensors/encoders/` | EncoderBase, EncoderFX, EncoderCANCoder + Comp variants | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/sensors/imu/` | IMUBase, IMUCTRE, IMUPigeon2 | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/sensors/range/` | RangeBase, RangeCTRE, RangeCANrange | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/sensors/switches/` | SwitchBase, SwitchFX, SwitchCANCoder | [Hardware Abstraction](../architecture/hardware-abstraction.md) | Full |
| `hardware/Input/` | Input, InputXBox, InputPS4, Input8BitDuo, InputEnvisionPro | [Hardware Abstraction](../architecture/hardware-abstraction.md) + [Input Shaping Guide](../guides/input-shaping-guide.md) | Full |
| `api/motor/` | Motor, ClosedLoopMotor, MotorCapabilities, NeutralMode, FollowStatus | [Vendor Abstraction Guide](../architecture/vendor-abstraction-guide.md) | Full |
| `api/sensor/` | Encoder, AbsoluteEncoder, Gyro, LimitSwitch, RangeSensor | [Vendor Abstraction Guide](../architecture/vendor-abstraction-guide.md) | Full |
| `vendor/ctre/` | CTREMotor, CTREEncoder, CTREAbsoluteEncoder, CTREGyro, CTRELimitSwitch, CTRERangeSensor | [Vendor Abstraction Guide](../architecture/vendor-abstraction-guide.md) | Full |
| `bridge/` | CommandBridge | [Vendor Abstraction Guide](../architecture/vendor-abstraction-guide.md) | Full |
| `hardware/` | CANBus, CANDeviceID, CTREManager, HardwareManager | [Library Architecture](../architecture/library-architecture.md) | Full |
| `nt/` | NTTable, NTEntry, LoggedNTInput | [Library Architecture — Tuning Infrastructure](../architecture/library-architecture.md#tuning-infrastructure) | Full |
| `subsystem/` | Subsystem, SubsystemManager, StateMachine | [Library Architecture](../architecture/library-architecture.md) | Full |
| `sysid/` | Logger, LoggerGeneral | [SysID Workflow](../guides/sysid-workflow.md) | Full |
| `util/` | Alert, Elastic, LimelightHelpers, Util, DriveSignal, CSVWritable, Interpolable | [Utility Package](utility-package.md) | Full |
| `wpilib/` | IterativeRobotBase, TimedRobot | [Library Architecture](../architecture/library-architecture.md) | Partial |
| Root | Lifecycle, Named, TObj, TRobot, ConstantsLib | [Library Architecture](../architecture/library-architecture.md) | Full |

### Removed Packages

| Former Package | Replacement | Decision Record |
|----------------|-------------|-----------------|
| `geometry/` | `edu.wpi.first.math.geometry` (Rotation2d, Translation2d, Pose2d, Twist2d) | [Geometry Package — Removed](geometry-package.md) |

---

## Cross-Cutting Concerns

| Topic | Authoritative Doc |
|-------|-------------------|
| Tuning infrastructure (LoggedNTInput, checkTuning) | [Library Architecture — Tuning Infrastructure](../architecture/library-architecture.md#tuning-infrastructure) |
| Simulation architecture (SimState, DCMotor, lifecycle) | [Library Architecture — Simulation Architecture](../architecture/library-architecture.md#simulation-architecture) |
| Simulation usage for robot projects | [Simulation Guide](../guides/simulation-guide.md) |
| Fault tolerance (exception isolation, CAN faults, timeouts) | [Fault Tolerance](../quality/fault-tolerance.md) |
| Testing patterns (HAL init, CTREManager cleanup, isolation) | [Testing Strategy](../quality/testing-strategy.md) |
| CAN management (bus tracking, signal refresh, timesync) | [Hardware Abstraction](../architecture/hardware-abstraction.md) + [Library Architecture](../architecture/library-architecture.md) |
| CTRE Phoenix 6 feature coverage (supported, planned, not implemented) | [Hardware Abstraction — CTRE Feature Coverage](../architecture/hardware-abstraction.md#ctre-phoenix-6-feature-coverage) |
| CTRE fault coverage (monitored vs. unmonitored faults) | [Fault Tolerance — CTRE Fault Coverage](../quality/fault-tolerance.md#ctre-fault-coverage-faultmonitor) |
| Phoenix 6 v26-specific features used | [Vendor Dependencies — v26 Features](vendor-dependencies.md#phoenix-6-v26-features-used) |
| Coding standard and safety rules | [Java Coding Standard](../quality/team271-java-coding-standard.md) |
| Vendordep management | [Vendor Dependencies](vendor-dependencies.md) |
| Passthrough design (wrapper, not wall philosophy) | [Passthrough Design](../architecture/passthrough-design.md) |

---

## Reading Order for New Contributors

1. **[Start Here](../guides/start-here.md)** — 5-minute orientation, mental
   models, and critical rules
2. **[Development Setup](../guides/development-setup.md)** — Prerequisites, IDE
   setup, build commands, troubleshooting
3. **[Library Architecture](../architecture/library-architecture.md)** — TObj hierarchy,
   subsystem lifecycle, managers, NT infrastructure, simulation
4. **[Hardware Abstraction](../architecture/hardware-abstraction.md)** — Controllers,
   transmissions, sensors, input system
5. **[Fault Tolerance](../quality/fault-tolerance.md)** — Exception isolation,
   CAN fault handling, safe defaults
6. **Subsystem-specific doc** — whichever doc covers the area you're
   working on (auto, control, geometry, etc.)
7. **[Java Coding Standard](../quality/team271-java-coding-standard.md)** —
   Full standard (read at least Sections 3, 4.9, and 5.4 before
   your first PR)
