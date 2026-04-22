<!-- Library-specific companion to the Team 271 Software Coding Standard.
     Templates here are for robot-project code that consumes Team271-Lib
     (TObj, Subsystem, LoggedNTInput, etc.). For the framework-agnostic
     standard itself, see ../common/coding-standard/Team271-Software-Coding-Standard.md. -->
<!-- markdownlint-disable-file MD041 -->

# Coding Standard — Library Templates

> **Scope:** These templates are for **robot-project code** that consumes
> Team271-Lib. Library subsystems themselves follow the pattern in
> [SDD-subsystem.md](../planning/sdd/SDD-subsystem.md) and do **not** use
> singletons (see
> [ADR-004](../planning/adr/ADR-004-explicit-instantiation-no-singletons.md)).
> Library-level constants live in `ConstantsLib.java`, not in the
> `Constants.java` shown in Appendix G.

## Appendix F: Subsystem Template

Every robot-project `.java` subsystem file shall follow this structure:

```java
package com.example.app;

import com.example.lib.LifecycleBase;
import edu.wpi.first.wpilibj.Timer;
import org.littletonrobotics.junction.Logger;

/**
 * Brief description of the class purpose.
 *
 * <p>Additional details about behavior, dependencies, or usage.
 */
public class ExampleSubsystem extends Subsystem {

    /*
     * Singleton
     */
    private static ExampleSubsystem mInstance;

    public static ExampleSubsystem getInstance(final LifecycleBase argParent) {
        if (mInstance == null) {
            mInstance = new ExampleSubsystem(argParent);
        }
        return mInstance;
    }

    public static ExampleSubsystem getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("ExampleSubsystem not initialized");
        }
        return mInstance;
    }

    /*
     * Enums
     */
    public enum ExampleControlState {
        IDLE,
        ACTIVE
    }

    /*
     * Constants
     */
    private static final double MAX_VOLTAGE = 12.0;

    /*
     * Other Singletons
     */
    protected final InputDriver mInputDriver;

    /*
     * Variables
     */
    private ExampleControlState mControlState = ExampleControlState.IDLE;
    private ExampleControlState mDesiredControlState = ExampleControlState.IDLE;

    /*
     * Motors
     */
    private final ExampleTransmission mTransmission;

    /*
     * Constructor
     */
    public ExampleSubsystem(final LifecycleBase argParent) {
        super(argParent, "ExampleSubsystem");
        mInputDriver = InputDriver.getInstance();
        // ...
    }

    /*
     * Stop Robot
     */
    public void stop() {
        mTransmission.stop();
    }

    /*
     * Robot Lifecycle
     */
    @Override
    public void robotInit(final double argTimestamp) {
        // Configure motors, current limits, etc.
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        // Read sensors
    }

    @Override
    public void teleopPeriodic(final double argTimestamp) {
        // Set desired state based on inputs
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
        // Apply outputs based on desired state
    }

    @Override
    public void outputTelemetry() {
        Logger.recordOutput("ExampleSubsystem/State", mControlState.toString());
    }
}
```

### File organization

Subsystem files shall contain sections in this order, each preceded
by a block comment:

1. `/* Singleton */` -- `mInstance` field and `getInstance()` methods
2. `/* Enums */` -- Control state enums and mode enums
3. `/* Constants */` -- Class-level constants (if not in `Constants.java`)
4. `/* Other Singletons */` -- References to other subsystems
5. `/* Variables */` -- State variables, timers, counters
6. `/* Motors */` -- `ExampleTransmission` declarations
7. `/* Constructor */`
8. `/* Stop Robot */` -- `stop()` method
9. `/* Robot Lifecycle */` -- Lifecycle methods in execution order
10. Private helper methods

### Key elements demonstrated

1. Singleton with dual `getInstance()` (CODE-GEN-013)
2. State enum with all states including IDLE (CODE-FUN-005)
3. `m` prefix on all instance fields (CODE-VAR-001a)
4. `arg` prefix on all parameters (CODE-VAR-001b)
5. `final` on all parameters and single-assignment fields (CODE-GEN-003b)
6. Motor configuration in `robotInit()` with current limits (CODE-SAF-002)
7. Desired state set in `teleopPeriodic()` (CODE-FUN-004b)
8. Outputs applied in `robotPeriodicAfter()` (CODE-FUN-004b)
9. Telemetry in `outputTelemetry()` — no parameters (CODE-BUG-001)
10. `default` case in all enum switches (CODE-FUN-006a)
11. Runtime tuning via `LoggedNTInput` + `checkTuning()` in `outputTelemetry()` (CODE-BUG-004)

---

## Appendix G: Constants Template

```java
public final class Constants {

    /* Robot-wide constants */
    public static final double CANCODER_BOOT_ALLOWANCE_SECS = 10.0;

    /* CAN Bus Names — update per robot */
    public static final String CAN_BUS_RIO = "rio";
    public static final String CAN_BUS_CANIVORE_1 = "CANivore1";
    public static final String CAN_BUS_CANIVORE_2 = "CANivore2";

    public static final class CAN {
        /* Group by subsystem, one CANDeviceID per motor/sensor */
        public static final CANDeviceID EXAMPLE_MOTOR =
                new CANDeviceID(1, CAN_BUS_CANIVORE_1);
        // ...

        private CAN() {}
    }

    /**
     * One inner class per subsystem.  Group constants by category:
     * speeds/voltages, timing, motor configuration.
     *
     * Constants here provide default values.  Configurable values
     * (speeds, voltages, PID gains, thresholds) should also be
     * exposed as LoggedNTInput fields in the subsystem for runtime
     * dashboard tuning (CODE-BUG-004).
     */
    public static final class ExampleSubsystemConstants {
        /* Speeds / Duty Cycles */
        public static final double IDLE_SPEED = 0.0;
        public static final double FORWARD_SPEED = 1.0;
        public static final double REVERSE_SPEED = -0.5;

        /* Timing */
        public static final double COAST_DOWN_SEC = 0.1;
        public static final double ACTION_DURATION_SEC = 0.3;

        /* Motor Configuration */
        public static final boolean kStatorLimitEnabled = true;
        public static final int kCurrentStatorLimit = 120;
        public static final boolean kSupplyLimitEnabled = true;
        public static final int kCurrentSupplyLimit = 80;

        private ExampleSubsystemConstants() {}
    }

    public static final class Controller {
        public static final int DRIVER_PORT = 0;
        public static final int OPERATOR_PORT = 1;

        private Controller() {}
    }

    private Constants() {}
}
```

See the robot-specific reference document (e.g., `docs/robot-<year>-reference.md`)
for the current year's actual CAN IDs, bus names, and constant values.

---
