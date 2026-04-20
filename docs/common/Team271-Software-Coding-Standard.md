<!-- markdownlint-disable MD007 MD013 MD031 MD032 -->
# Team 271 Java Coding Standard

> **Library applications:** Rules in this chapter sometimes name Team271-Lib
> classes as concrete examples (e.g., `TObj`, `Subsystem`, `LoggedNTInput`).
> The rule itself is framework-agnostic; the concrete library binding lives
> in [`team-lib/coding-standard-library-notes.md`](../team-lib/coding-standard-library-notes.md).

Document No: 271-JCS\
Revision: Draft\
Date of Release: (see revision history)

## Revision History

| Revision | Date | Author | Description |
| -------- | ---- | ------ | ----------- |
| Draft | (initial) | Team 271 | Initial draft |

---

## 1. Introduction

### 1.1 Purpose

This document describes the software coding standard for Team 271's FRC
robot Java codebase. It ensures that all team members write reliable,
readable, and maintainable code that can survive a 2:30 match without
crashing, can be debugged on the practice field under time pressure, and
can be handed off between programmers across seasons.

The core principles are defensive coding, predictable execution, minimal
complexity, and rigorous formatting -- all directly applicable to FRC
robot code running in real-time 20ms periodic cycles.

In this document, any rule specified with "shall" denotes a mandatory
requirement. Rules specified with "should" are recommended practices.

### 1.2 Scope

This standard applies to all Java source code in the `com.team271`
packages, covering both:

- **Library code** (`com.team271.lib.*`) in this repository.
- **Robot-project code** (`com.team271.frc<year>.*`) in each season's
  downstream repository that depends on this library.

The two share most rules. Where they differ, sections explicitly note
"library only" or "robot projects may." Notable example: subsystem
instantiation — library code uses explicit instantiation and no
singletons (see
[ADR-015](../team-lib/planning/adr/ADR-015-explicit-instantiation-no-singletons.md));
robot-project subsystems commonly use the singleton pattern shown in
§3.1.

**Exempt from formatting rules** (but not safety rules):

- Generated code: `BuildConstants.java` (gversion), `TunerConstants.java`
  (CTRE Tuner X), `LimelightHelpers.java`
- Third-party vendordep source code

### 1.3 Acronyms and Abbreviations

| Abbreviation | Meaning |
| ------------ | ------- |
| ADR | Architecture Decision Record |
| CAN | Controller Area Network |
| CANivore | CTRE CAN-to-USB bridge |
| CCW | Counter-Clockwise |
| CTRE | Cross The Road Electronics |
| CW | Clockwise |
| FOC | Field-Oriented Control |
| FRC | FIRST Robotics Competition |
| FX | TalonFX (CTRE brushless motor controller) |
| FXS | TalonFXS (CTRE brushed motor controller) |
| GC | Garbage Collection |
| HAL | Hardware Abstraction Layer (WPILib) |
| IMU | Inertial Measurement Unit |
| JVM | Java Virtual Machine |
| NT | NetworkTables |
| PID | Proportional-Integral-Derivative (controller) |
| RPS | Rotations Per Second |
| SCMP | Software Configuration Management Plan |
| SCS | Software Coding Standard (this document) |
| SDD | Software Design Description |
| SDP | Software Development Plan |
| SemVer | Semantic Versioning |
| SRS | Software Requirements Specification |
| SVP | Software Verification Plan |
| TObj | Team 271 base object (library lifecycle root class) |
| WPILib | WPI Robotics Library |

### 1.4 Applicable Documents

- [WPILib Documentation][wpilib-docs] -- Java API and robot programming guide
- [WPILib Java API][wpilib-api]
- [CTRE Phoenix 6 Documentation][phoenix6-docs] -- Motor controller and sensor API
- [CTRE Phoenix 6 Java API][phoenix6-api]
- [AdvantageKit Documentation][akit-docs] -- Logging and replay framework
- [PathPlanner Documentation][pathplanner-docs] -- Autonomous path following
- [Google Java Style Guide][google-java] -- Basis for Spotless formatter configuration
- [Elastic Dashboard Documentation][elastic-docs] -- FRC dashboard
- [MISRA C:2023][misra] -- Safety-critical C coding guidelines (concepts adapted to Java)
- [SEI CERT Java Coding Standard][cert-java] -- Secure Java coding rules
- [JPL "Power of 10" Rules][jpl-power10] -- NASA/JPL rules for safety-critical code
- [DO-178C][do178c] -- Avionics software certification standard (process philosophy)
- [Barr Group Embedded C Coding Standard][barr] -- Embedded coding conventions

[wpilib-docs]: https://docs.wpilib.org/en/stable/
[wpilib-api]: https://github.wpilib.org/allwpilib/docs/release/java/index.html
[phoenix6-docs]: https://v6.docs.ctr-electronics.com/en/stable/index.html
[phoenix6-api]: https://api.ctr-electronics.com/phoenix6/stable/java/
[akit-docs]: https://docs.advantagekit.org/
[pathplanner-docs]: https://pathplanner.dev/home.html
[google-java]: https://google.github.io/styleguide/javaguide.html
[elastic-docs]: https://frc-elastic.gitbook.io/docs
[misra]: https://misra.org.uk/misra-c/
[cert-java]: https://wiki.sei.cmu.edu/confluence/display/java/
[jpl-power10]: https://en.wikipedia.org/wiki/The_Power_of_10:_Rules_for_Developing_Safety-Critical_Code
[do178c]: https://en.wikipedia.org/wiki/DO-178C
[barr]: https://barrgroup.com/embedded-systems/books/embedded-c-coding-standard

### 1.5 Industry Standard References

Several rules in this document are inspired by coding standards used in
aerospace, automotive, and other safety-critical industries. We are not
claiming compliance with any of these standards -- we are a high school
robotics team, not building flight software. But many of the same
problems apply: our code runs in real-time loops, controls physical
mechanisms, and must not crash during a match. Where our rules align
with an industry standard, we call it out so you know *why* the rule
exists and that it is not just our opinion.

**MISRA C:2023** -- Originally written for automotive software (think
anti-lock brakes and airbag controllers). MISRA defines rules for
writing C code that is predictable and safe. Many of its rules about
control flow, switch statements, and side effects apply equally to
Java. When we cite MISRA, the concept transfers even though the
language does not.

**SEI CERT Java Coding Standard** -- Published by Carnegie Mellon's
Software Engineering Institute. These are Java-specific rules for
writing secure, reliable code. This is the most directly applicable
standard since we also write Java.

**JPL "Power of 10"** -- Ten rules written by Gerard Holzmann at
NASA's Jet Propulsion Laboratory for code that runs on spacecraft.
If your Mars rover's code crashes, there is nobody to reboot it.
Rules like "no recursion," "no dynamic allocation after init," and
"all loops must have a fixed upper bound" apply directly to our
20ms robot loop.

**DO-178C** -- The standard used to certify software in commercial
aircraft. It is a process standard (it tells you *how* to develop
software, not specific coding rules), but its emphasis on
deterministic execution, traceable state machines, and defensive
coding directly influenced our subsystem architecture.

**Barr Group Embedded C Coding Standard** -- A widely-used industry
coding standard for embedded systems. Its naming conventions,
mandatory braces, and module organization rules influenced our own
formatting and naming choices.

---

## 2. Programming Language

### 2.1 Java

The primary language shall be Java 17, as configured in `build.gradle`:

```groovy
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

**Permitted Java features:**

- Switch expressions (`->` syntax)
- Enhanced for-each loops
- `var` for local type inference (when the type is obvious from context)
- Records (for data-carrier classes)
- Text blocks (for multi-line strings in tests or configuration)

**Restricted features** (avoid in periodic methods due to GC pressure):

- Streams and lambdas that create intermediate collections
- Autoboxing/unboxing in loops
- String concatenation in hot paths (use `Logger.recordOutput()` keys as constants)
- Reflection and dynamic class loading

**Prohibited:**

- `Thread.sleep()` in robot code (use WPILib `Timer` or `Notifier`)
- `System.exit()` and `Runtime.halt()`
- Raw `Thread` creation (use WPILib `Notifier` if needed)
- `System.gc()` (let the JVM manage garbage collection)
- `Object.finalize()` (deprecated and unreliable)

### 2.2 Build System

The project uses Gradle with GradleRIO and the following plugins:

- `edu.wpi.first.GradleRIO` -- FRC build toolchain
- `com.diffplug.spotless` -- Automatic code formatting
- `com.peterabeles.gversion` -- Build version metadata
- `jacoco` -- Code coverage

The build also defines an `eventDeploy` task that automatically
commits all changes when deploying from an `event*` branch. This
ensures every competition deploy is captured in version control.

Spotless runs automatically before compilation
(`project.compileJava.dependsOn(spotlessApply)`). All code shall pass
`spotlessCheck` without manual intervention.

---

## 3. Source Code Presentation Standards

### 3.1 Java Source File Template

> **Scope note:** This template is for **robot-project subsystems**
> (the `com.team271.frc<year>.*` package). It uses the singleton
> pattern commonly used in FRC robot code. Library code
> (`com.team271.lib.*`) does **not** use singletons for subsystems —
> see [ADR-015](../team-lib/planning/adr/ADR-015-explicit-instantiation-no-singletons.md).
> Library authors should omit the `/* Singleton */` block and
> `getInstance()` methods.

Every robot-project `.java` subsystem file shall follow this structure:

```java
package com.team271.frc<year>;

import com.team271.lib.TObj;
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

    public static ExampleSubsystem getInstance(final TObj argParent) {
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
    private final TransmissionFX mTransmission;

    /*
     * Constructor
     */
    public ExampleSubsystem(final TObj argParent) {
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

### 3.2 Subsystem File Organization

Subsystem files shall contain sections in this order, each preceded by
a block comment:

1. `/* Singleton */` -- `mInstance` field and `getInstance()` methods
2. `/* Enums */` -- Control state enums and mode enums
3. `/* Constants */` -- Class-level constants (if not in `Constants.java`)
4. `/* Other Singletons */` -- References to other subsystems
5. `/* Variables */` -- State variables, timers, counters
6. `/* Motors */` -- `TransmissionFX` declarations
7. `/* Constructor */`
8. `/* Stop Robot */` -- `stop()` method
9. `/* Robot Lifecycle */` -- Lifecycle methods in execution order:
   `robotInit`, `robotPeriodicBefore`, `disabledInit`,
   `autonomousInit`, `autonomousPeriodic`, `teleopInit`,
   `teleopPeriodic`, `robotPeriodicAfter`, `outputTelemetry`
   (Note: `outputTelemetry()` takes no parameters; all others
   take `final double argTimestamp`. Additional lifecycle methods
   include `autonomousExit`, `teleopExit`, `disabledExit`,
   `simulationInit`, `simulationPeriodic`, `testInit`,
   `testPeriodic`, and `testExit`.)
10. Private helper methods

### 3.3 Constants File Organization

Constants shall be organized using nested `public static final class`
inner classes within `Constants.java`:

```java
public final class Constants {

    /* CAN Bus Names */
    public static final String CAN_BUS_RIO = "rio";
    public static final String CAN_BUS_CANIVORE_A = "BusA";

    public static final class CAN {
        public static final CANDeviceID INDEXER_LEADER =
                new CANDeviceID(5, CAN_BUS_CANIVORE_SUBSYSTEMS);
        // ...
        private CAN() {}
    }

    public static final class ExampleSubsystemConstants {
        public static final double EXAMPLE_SPEED = 1.0;
        public static final double IDLE_SPEED = 0.0;
        // ...
        private ExampleSubsystemConstants() {}
    }

    private Constants() {}
}
```

Each inner class shall have a `private` constructor to prevent
instantiation.

---

## 4. Coding Guidelines

§4 is split across eight companion documents, each covering one
subsection of the coding guidelines:

| Section | Companion | Former |
| ------- | --------- | ------ |
| General | [`-General.md`](Team271-Software-Coding-Standard-General.md) | §4.1 |
| Format | [`-Format.md`](Team271-Software-Coding-Standard-Format.md) | §4.2 |
| Modules and Files | [`-Modules.md`](Team271-Software-Coding-Standard-Modules.md) | §4.3 |
| Methods | [`-Methods.md`](Team271-Software-Coding-Standard-Methods.md) | §4.4 |
| Variables | [`-Variables.md`](Team271-Software-Coding-Standard-Variables.md) | §4.5 |
| Control Structures | [`-Control.md`](Team271-Software-Coding-Standard-Control.md) | §4.6 |
| Comments | [`-Comments.md`](Team271-Software-Coding-Standard-Comments.md) | §4.7 |
| Debugging and Telemetry | [`-Debug.md`](Team271-Software-Coding-Standard-Debug.md) | §4.8 |

Safety Practices (formerly §4.9) now lives standalone in
[`-Safety.md`](Team271-Software-Coding-Standard-Safety.md).

---

## Companion Documents

The following companion documents are part of this standard:

| Document | Contents |
| -------- | -------- |
| [`-General.md`](Team271-Software-Coding-Standard-General.md) | §4.1 General coding guidelines |
| [`-Format.md`](Team271-Software-Coding-Standard-Format.md) | §4.2 Formatting rules |
| [`-Modules.md`](Team271-Software-Coding-Standard-Modules.md) | §4.3 Modules and files |
| [`-Methods.md`](Team271-Software-Coding-Standard-Methods.md) | §4.4 Methods |
| [`-Variables.md`](Team271-Software-Coding-Standard-Variables.md) | §4.5 Variables |
| [`-Control.md`](Team271-Software-Coding-Standard-Control.md) | §4.6 Control structures |
| [`-Comments.md`](Team271-Software-Coding-Standard-Comments.md) | §4.7 Comments |
| [`-Debug.md`](Team271-Software-Coding-Standard-Debug.md) | §4.8 Debugging and telemetry |
| [`-Safety.md`](Team271-Software-Coding-Standard-Safety.md) | Safety Practices (CODE-SAF-*) |
| [`-Compliance.md`](Team271-Software-Coding-Standard-Compliance.md) | §5 Static Analysis and enforcement matrix |
| [`-Appendices.md`](Team271-Software-Coding-Standard-Appendices.md) | Appendices A–E, H, I |

### Library-specific companions (in `docs/team-lib/`)

| File | Content |
| ---- | ------- |
| [`coding-standard-templates.md`](../team-lib/coding-standard-templates.md) | Appendix F (Subsystem) + Appendix G (Constants) — templates for robot-project code consuming the library |
| [`coding-standard-library-notes.md`](../team-lib/coding-standard-library-notes.md) | Concrete library-API applications of common rules (e.g., `LoggedNTInput` for `CODE-BUG-004`, `Elastic` for `CODE-SAF-004`) |
