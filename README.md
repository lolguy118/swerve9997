# Team271-Lib

Team 271's reusable FRC robotics library. Java 17, built with
GradleRIO, targets CTRE Phoenix 6 hardware on the roboRIO 2. Robot
projects depend on this library as a versioned artifact; the library
has no awareness of any specific season's robot.

## Quick Start

```bash
git clone https://github.com/Team271/Team271-Lib.git
cd Team271-Lib
./gradlew build
./gradlew test
./gradlew simulateJava   # desktop simulation
```

Requires Java 17 (bundled with the WPILib 2026 installer) and Git.
Editor: VS Code with the WPILib extension recommended.

## What the library provides

- **Lifecycle primitives** — `TObj`, `Subsystem`, `SubsystemManager`,
  `TRobot`
- **Hardware wrappers** — CTRE Phoenix 6 motors, encoders, IMU, range
  sensors, limit switches (passthrough access to raw vendor objects)
- **Control** — five PID variants (`PIDSimple`, `PIDTrap`, `PIDWPI`,
  `PIDWPI_Trap`, `PIDFX`), feedforward, balance algorithm
- **Autonomous** — `AutoMode`, `AutoMove` composition,
  PathPlanner integration via `CommandBridge`
- **Telemetry** — AdvantageKit-backed logging, `LoggedNTInput` for
  replay-faithful dashboard tuning
- **System ID** — data capture for mechanism characterization
- **Utilities** — `Alert`, `Elastic` dashboard notifications,
  `DriveSignal`, `LimelightHelpers`, math helpers

## Where to read next

- **New contributor** → [docs/team-lib/guides/start-here.md](docs/team-lib/guides/start-here.md)
- **Design overview** → [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
- **AI/IDE guardrails** → [CLAUDE.md](CLAUDE.md)
- **Contribution workflow** → [CONTRIBUTING.md](CONTRIBUTING.md)

## License

[TBD]
