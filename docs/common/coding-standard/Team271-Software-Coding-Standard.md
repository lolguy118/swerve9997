<!-- markdownlint-disable MD007 MD013 MD031 MD032 -->
# Team 271 Java Coding Standard

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

This standard applies to all Java source code written by the team,
covering both:

- **Reusable library code** (`com.example.lib.*` placeholder) —
  packaged for consumption by any downstream project.
- **Robot-project code** (`com.example.app.*` placeholder) — the
  season robot's application that depends on the library.

The two share most rules. Where they differ, sections explicitly note
"library only" or "robot projects may." Notable example: subsystem
instantiation — reusable library code uses explicit instantiation and
no singletons; robot-project subsystems commonly use the singleton
pattern shown in §3.1.

**Exempt from formatting rules** (but not safety rules):

- Generated code: `BuildConstants.java` (gversion), `TunerConstants.java`
  (CTRE Tuner X), `LimelightHelpers.java`
- Third-party vendordep source code

### 1.3 Terminology (Acronyms Used in This Document)

This glossary defines proper-name acronyms used throughout the coding
standard and its companion documents. For the separate allowlist of
short-form tokens permitted in *code identifiers* (e.g., `cfg`, `idx`,
`msg`), see
[Appendix A](Team271-Software-Coding-Standard-Appendices.md#appendix-a-approved-identifier-abbreviations).

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
| WPILib | WPI Robotics Library |

### 1.4 Applicable Documents

For vendor and tool documentation (WPILib, CTRE Phoenix 6, PathPlanner,
Choreo, AdvantageKit, Limelight, PhotonVision, Elastic, Spotless, etc.),
see [`docs/reference-urls.md`](../../reference-urls.md). That file is the
single authoritative index for external vendor/tool URLs.

The following industry coding standards are normative references for
this document. §1.5 below explains how each one shapes specific rules:

- [MISRA C:2025][misra-c] -- Safety-critical C coding guidelines (concepts adapted to Java). Local [PDF][misra-c-pdf]
- [MISRA C++:2023][misra-cpp] -- Safety-critical C++ coding guidelines (concepts adapted to Java). Local [PDF][misra-cpp-pdf]
- [SEI CERT Java Coding Standard][cert-java] -- Secure Java coding rules
- [JPL "Power of 10" Rules][jpl-power10] -- NASA/JPL rules for safety-critical code
- [DO-178C][do178c] -- Avionics software certification standard (process philosophy)
- [Barr Group Embedded C Coding Standard][barr] -- Embedded coding conventions

[misra-c]: https://misra.org.uk/misra-c/
[misra-c-pdf]: ../../third-party/MISRA-C-2025.pdf
[misra-cpp]: https://misra.org.uk/misra-cpp/
[misra-cpp-pdf]: ../../third-party/MISRA-CPP-2023.pdf
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

**MISRA C:2025 and MISRA C++:2023** -- Originally written for
automotive software (think anti-lock brakes and airbag controllers).
MISRA defines rules for writing C and C++ code that is predictable and
safe. MISRA C:2025 is the current edition of the C guidelines
(superseding MISRA C:2023); MISRA C++:2023 is the current edition of
the C++ guidelines (targeting C++17 and superseding MISRA C++:2008).
Many of their rules about control flow, switch statements, and side
effects apply equally to Java. When we cite MISRA, the concept
transfers even though the language does not -- the rule numbers are
edition-agnostic by convention, so citations like "MISRA Rule 13.4" or
"MISRA Directive 4.4" in the companion documents refer to the same
guidance regardless of which edition originated the rule.

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

Java feature and API guidance lives in the General companion:

- [CODE-GEN-002](Team271-Software-Coding-Standard-General.md#code-gen-002----apis-and-methods-to-avoid)
  — banned APIs and patterns (`Thread.sleep`, `System.exit`,
  reflection in periodic methods, etc.).
- [CODE-GEN-004](Team271-Software-Coding-Standard-General.md#code-gen-004----garbage-collection-pressure-minimization)
  — allocation and GC-pressure rules (streams, autoboxing, string
  concatenation, and similar patterns to avoid in the 20 ms loop).
- [CODE-GEN-017](Team271-Software-Coding-Standard-General.md#code-gen-017----concurrency-keywords)
  — concurrency keywords (`volatile`, `synchronized`,
  `java.util.concurrent.atomic`).

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
> (the application package). It uses the singleton pattern commonly
> used in FRC robot code. Reusable library code does **not** use
> singletons for subsystems — library authors should omit the
> `/* Singleton */` block and `getInstance()` methods.

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

### 3.2 Subsystem File Organization

Subsystem files shall contain sections in this order, each preceded by
a block comment:

1. `/* Singleton */` -- `mInstance` field and `getInstance()` methods
2. `/* Enums */` -- Control state enums and mode enums
3. `/* Constants */` -- Class-level constants (if not in `Constants.java`)
4. `/* Other Singletons */` -- References to other subsystems
5. `/* Variables */` -- State variables, timers, counters
6. `/* Motors */` -- `ExampleTransmission` declarations
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

Concrete project-specific applications of these rules (file templates,
API bindings) live alongside the consuming project's source, not here.
