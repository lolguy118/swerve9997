<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to the robot's
     own src/main/java/<your-package>/Constants.java and fill in the
     real CAN IDs, bus names, and constant values for this robot. -->

# Constants Template

> **Scope:** This template is for **robot-project `Constants.java`**.
> Library-level constants live in `ConstantsLib.java` inside
> Team271-Lib itself, not here.

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
