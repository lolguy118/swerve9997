# Team271-Lib

A reusable FRC framework for Team 271's season robots. Instead of
WPILib's command-based pattern, it builds on a state-machine model
with explicit lifecycle hooks, per-subsystem exception isolation (one
crash doesn't take the rest down), and batched CAN-signal reads.
Robot projects depend on a tagged release of the library; the library
itself stays season-agnostic.

- **Language / build:** Java 17, GradleRIO 2026
- **Hardware target:** CTRE Phoenix 6 on roboRIO 2
- **Telemetry:** AdvantageKit log + replay, NetworkTables dashboards
- **Autonomous:** PathPlanner + Choreo via `CommandBridge`

## What you actually get

Compared to a bare WPILib project, Team271-Lib saves you from
writing:

- **Lifecycle plumbing.** `TObj` gives every object automatic
  NetworkTables namespacing and ordered hooks (`robotInit`,
  `robotPeriodicBefore`, `disabledInit`, …). `SubsystemManager`
  orchestrates the tree in a fixed order — deterministic, not
  scheduler-dependent.
- **Exception isolation.** A subsystem that throws in its periodic
  method gets caught and logged; the rest of the robot keeps running.
- **Live-tunable control.** Five PID variants (software, profiled,
  hardware-onboard) share a `PIDBase` with gain tuning wired through
  `LoggedNTInput` — change a gain in Elastic, code picks it up
  without a redeploy, and replays still reproduce exactly
  ([ADR-015](docs/team-lib/planning/adr/ADR-015-logged-nt-input-backed-tuning.md)).
- **CAN efficiency.** One batched `StatusSignal` refresh per cycle
  via `HardwareManager`, not N refreshes scattered across subsystems
  ([ADR-009](docs/team-lib/planning/adr/ADR-009-centralized-can-refresh.md)).
- **Auto by composition.** Routines are built from `AutoMove` blocks
  (`Sequence`, `Parallel`, `Timed`, `Conditional`); timing emerges
  from composition instead of magic `waitSeconds` constants.
- **Fail-safe defaults.** Every waiting operation has a named
  timeout, notifies the driver via `Elastic` on miss, and restores
  safe state
  ([ADR-012](docs/team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md)).

## A subsystem, end to end

Library-side subsystems are constructed explicitly by a parent
(`TObj`); robot-project subsystems wrap the same pattern in a
singleton (see
[subsystem-template.md](docs/robot-yyyy/subsystem-template.md)).

```java
public class Infrastructure extends Subsystem {
    private IMUPigeon2 imu;

    public Infrastructure(final TObj argParent) {
        super(argParent, "Infrastructure");
    }

    @Override
    public void robotInit(final double argTimestamp) {
        imu = new IMUPigeon2(this, "Pigeon2", Constants.CAN.PIGEON2, 250.0);
        imu.robotInit(argTimestamp);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        if (imu != null) {
            imu.robotPeriodicBefore(argTimestamp);
        }
    }

    @Override
    public void outputTelemetry() {
        super.outputTelemetry();
        if (imu != null) {
            imu.outputTelemetry();
        }
    }
}
```

Because `Subsystem` extends `TObj`, NetworkTables paths
(`/Infrastructure/Pigeon2/…`), lifecycle ordering, and exception
isolation come for free. Full example with state modes and driver
alerts:
[Infrastructure.java](src/main/java/com/team271/libtest/subsystems/Infrastructure.java).

## Using the library in a robot project

Robot projects pin to a tagged release of Team271-Lib and vendor
`docs/common/` + `docs/team-lib/` alongside their own
`docs/<robot-name>/` tier. The versioning and publishing mechanism
is defined in
[SCMP §3](docs/team-lib/planning/SCMP.md#3-library-versioning);
the project-tier scaffold lives in
[docs/robot-yyyy/](docs/robot-yyyy/README.md).

## Working on the library itself

```bash
git clone https://github.com/Team271/Team271-Lib.git
cd Team271-Lib
./gradlew build
./gradlew test
./gradlew simulateJava   # desktop simulation
```

Requires Java 17 (bundled with the WPILib 2026 installer) and Git.
VS Code with the WPILib extension is recommended — full setup in
[docs/common/guides/development-setup.md](docs/common/guides/development-setup.md).

## Where to read next

- **New contributor** → [docs/team-lib/guides/start-here.md](docs/team-lib/guides/start-here.md)
- **Design overview** → [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
- **AI/IDE guardrails** → [CLAUDE.md](CLAUDE.md)
- **Contribution workflow** → [CONTRIBUTING.md](CONTRIBUTING.md)

## License

[TBD]
