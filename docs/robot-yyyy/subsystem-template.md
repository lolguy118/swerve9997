<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to the robot's
     own docs folder as docs/<project>/subsystem-template.md (or
     treat as a live reference). The Java layout below is what each
     subsystem file under src/main/java/<your-package>/subsystems/
     should follow. -->
<!-- markdownlint-disable MD013 -->

# Subsystem Template

> **Rule:** Robot-project subsystems **shall** use this template
> pattern (singleton + lifecycle hooks + Globals registration).
> Library subsystems do **not** use singletons — see
> [ADR-004](../team-lib/planning/adr/ADR-004-explicit-instantiation-no-singletons.md)
> and [SDD-subsystem.md](../team-lib/planning/sdd/SDD-subsystem.md).

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

## Creation and registration

Subsystem singletons **shall** only be created in `Robot.robotInit()`
and registered with `SubsystemManager.addSubsystem()`:

```java
/* In Robot.robotInit() */
Globals.subsystemA = SubsystemA.getInstance(ntRobot);
mSubsystemManager.addSubsystem(Globals.subsystemA);
```

After creation, subsystem references **shall** be stored in
`Globals.java` as `public static` fields so that other classes can
access them without calling `getInstance()`:

```java
/* In Globals.java */
public static SubsystemA subsystemA;
public static SubsystemB subsystemB;
public static SubsystemC subsystemC;
public static SubsystemD subsystemD;
public static InputDriver controllerDriver;
```

Fields in `Globals.java` do not use the `m` prefix because they are
`static`, not instance fields (accepted exception to CODE-VAR-001a).

## Registration order

Registration order determines lifecycle call order. **This order is
load-bearing:**

- Input/controller subsystems **shall** be registered first so that
  other subsystems can read their state during the same cycle.
- Subsystems that produce data consumed by other subsystems (e.g.,
  a shooter gating a feeder) **shall** be registered before their
  consumers.
- Actuator subsystems that depend on sensor data from other
  subsystems **shall** be registered after their data sources.

See the robot-specific reference document (e.g.,
`docs/robot-<year>-reference.md`) for the current year's concrete
registration order.

## File organization

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

## Key elements demonstrated

1. Singleton with dual `getInstance()` — see the **Rule** blockquote above
2. State enum with all states including IDLE (CODE-&lt;PROJECT&gt;-009 in `coding-standard.md`)
3. `m` prefix on all instance fields (CODE-VAR-001a)
4. `arg` prefix on all parameters (CODE-VAR-001b)
5. `final` on all parameters and single-assignment fields (CODE-GEN-003b)
6. Motor configuration in `robotInit()` with current limits (CODE-SAF-002)
7. Desired state set in `teleopPeriodic()` (CODE-LIB-006b)
8. Outputs applied in `robotPeriodicAfter()` (CODE-LIB-006b)
9. Telemetry in `outputTelemetry()` — no parameters (CODE-BUG-001)
10. `default` case in all enum switches (CODE-FUN-006a)
11. Runtime tuning via `LoggedNTInput` + `checkTuning()` in `outputTelemetry()` (CODE-BUG-004)
