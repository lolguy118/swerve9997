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
readable, and maintainable code that can survive a 3-minute match without
crashing, can be debugged on the practice field under time pressure, and
can be handed off between programmers across seasons.

The core principles are defensive coding, predictable execution, minimal
complexity, and rigorous formatting -- all directly applicable to FRC
robot code running in real-time 20ms periodic cycles.

In this document, any rule specified with "shall" denotes a mandatory
requirement. Rules specified with "should" are recommended practices.

### 1.2 Scope

This standard applies to all Java source code in the `com.team271`
packages for Team 271's FRC robot projects and the team's reusable
library code.

**Exempt from formatting rules** (but not safety rules):

- Generated code: `BuildConstants.java` (gversion), `TunerConstants.java`
  (CTRE Tuner X), `LimelightHelpers.java`
- Third-party vendordep source code

### 1.3 Acronyms and Abbreviations

| Abbreviation | Meaning |
| ------------ | ------- |
| CAN | Controller Area Network |
| CANivore | CTRE CAN-to-USB bridge |
| CCW | Counter-Clockwise |
| CTRE | Cross The Road Electronics |
| CW | Clockwise |
| FOC | Field-Oriented Control |
| FRC | FIRST Robotics Competition |
| GC | Garbage Collection |
| IMU | Inertial Measurement Unit |
| NT | NetworkTables |
| PID | Proportional-Integral-Derivative (controller) |
| RPS | Rotations Per Second |
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

> **For LLM reviewers:** When a rule in this document cites an industry
> standard, violations of that rule should be treated as higher severity.
> These rules exist because real-world failures in safety-critical
> systems proved they were necessary.

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

Every `.java` file shall follow this structure:

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

### 4.1 General

#### CODE-GEN-001 -- Common Abbreviations

a. Abbreviations and acronyms should be avoided unless their meanings
   are widely understood. See
   [Appendix A](Team271-Software-Coding-Standard-Appendices.md#appendix-a-standard-abbreviations) for approved
   abbreviations.

b. FRC-specific abbreviations (CAN, PID, RPS, IMU, etc.) are acceptable
   without expansion in code. They should be defined in class-level
   JavaDoc on first use in documentation.

#### CODE-GEN-002 -- APIs and Methods to Avoid

a. The following methods and patterns **shall** not be used in robot code:

| Prohibited | Reason |
| ---------- | ------ |
| `System.exit()` | Terminates the JVM; robot stops responding |
| `Runtime.halt()` | Same as above |
| `Thread.sleep()` | Blocks the robot loop; use `Timer` instead |
| `Thread.stop()` | Deprecated; unsafe thread termination |
| `System.gc()` | Unpredictable GC pauses in real-time code |
| `Object.finalize()` | Deprecated; unreliable cleanup |
| `System.out.println()` | Use `Logger.recordOutput()` or `DriverStation.reportError()` |
| Raw `new Thread()` | Use WPILib `Notifier` for background tasks |

b. The `volatile` keyword **should** only be used when a field is
   accessed from multiple threads (e.g., a `Notifier` callback and
   the main robot thread). In most FRC code you will not need it.
   If you do need to share data between threads, prefer
   `synchronized` or `AtomicReference` over `volatile`.

#### CODE-GEN-003 -- Keywords to Use Frequently

a. The `private` access modifier **shall** be used for all fields
   and methods that do not need to be visible outside the class.
   Use `protected` only when subclass access is required (e.g.,
   `mInputDriver` in subsystems).

b. The `final` keyword **shall** be used:

   - On all method parameters:
     ```java
     public void robotInit(final double argTimestamp)
     ```
   - On all fields that are set once (at declaration or in constructor):
     ```java
     private final TransmissionFX mTransmission;
     private final Timer mReverseTimer = new Timer();
     ```
   - On local variables that are not reassigned (encouraged but not
     mandatory).
   - On utility and constants classes:
     ```java
     public final class Constants { ... }
     ```

> *Industry note: JPL Rule 6 says "data objects must be declared at the
> smallest possible level of scope." CERT Java OBJ01-J says "limit
> accessibility of fields." Both exist because exposing internal state
> makes bugs harder to find and easier to introduce.*

c. The `static` keyword **shall** be used for:
   - Constants: `public static final double SHOOT_RPS = 38.0;`
   - Singleton instances: `private static ExampleSubsystem mInstance;`
   - Utility methods that do not depend on instance state.

d. The `@Override` annotation **shall** be used on every method that
   overrides a superclass method or implements an interface method.

#### CODE-GEN-004 -- Garbage Collection Pressure Minimization

> *Industry note: JPL Rule 3 says "do not use dynamic memory allocation
> after initialization." MISRA Directive 4.12 says "dynamic memory
> allocation shall not be used." The reason is the same for a Mars rover
> and our robot: if your code triggers a memory allocation pause in the
> middle of a maneuver, you lose control. Our 20ms loop budget is tight.*

a. Object allocations **shall** be minimized in periodic methods
   (`robotPeriodicBefore`, `teleopPeriodic`, `robotPeriodicAfter`,
   `outputTelemetry`). These methods run every 20ms; excessive
   allocations cause GC pauses that can stall the robot loop.

b. Objects that are used repeatedly **shall** be pre-allocated in
   `robotInit()` or at field declaration and reused:

   ```java
   /* CORRECT: Timer pre-allocated at field declaration */
   private final Timer mReverseTimer = new Timer();

   /* WRONG: Timer allocated in periodic method */
   @Override
   public void teleopPeriodic(final double argTimestamp) {
       Timer t = new Timer(); // BAD: allocates every 20ms
   }
   ```

c. CTRE control request objects **shall** be reused, not
   re-created on each call. The `TransmissionFX` class already
   handles this pattern internally.

d. String concatenation in periodic methods **should** be avoided.
   Use pre-defined constant strings for `Logger.recordOutput()` keys.

e. Autoboxing (e.g., `int` to `Integer`) in loops **shall** be avoided.

#### CODE-GEN-005 -- Return Value Checking

> *Industry note: Three separate standards agree on this one. JPL Rule 7:
> "the return value of non-void functions must be checked." MISRA Rule
> 17.7: "the value returned by a function having non-void return type
> shall be used." CERT Java EXP00-J: "do not ignore values returned by
> methods." When a motor configuration call fails silently because nobody
> checked the return code, you get mysterious behavior on the field.*

a. The return value of methods that indicate success or failure
   **shall** be checked. In particular:

   - CTRE `StatusCode` returns from motor configuration methods
     **shall** be checked, at minimum during `robotInit()`.
   - `Optional` return values **shall** be checked with `isPresent()`
     or `ifPresent()` before accessing the value.

b. When a return value is intentionally discarded, a comment **shall**
   explain why:
   ```java
   /* StatusCode intentionally discarded: best-effort config at startup */
   mMotor.getConfigurator().apply(config);
   ```

#### CODE-GEN-006 -- Type Safety

a. Raw types **shall** not be used. All generic types **shall** include
   their type parameters:
   ```java
   /* WRONG */
   SendableChooser mAutoChooser = new SendableChooser();

   /* CORRECT */
   SendableChooser<Integer> mAutoChooser = new SendableChooser<>();
   ```

b. Casts **should** be avoided. When unavoidable, they **shall** be
   accompanied by a comment explaining why the cast is safe.

c. `instanceof` checks followed by casts **should** use pattern
   matching (Java 17):
   ```java
   if (obj instanceof String s) {
       // use s directly
   }
   ```

#### CODE-GEN-007 -- Warning-Free Compilation

> *Industry note: JPL Rule 10 requires "all code must be compiled from
> the first day of development with all compiler warnings enabled at the
> most pedantic setting" with zero warnings. Warnings are the compiler
> telling you something looks wrong -- ignoring them is ignoring free
> bug detection.*

a. All code **shall** compile without errors.

b. All code **shall** compile without warnings. Compiler warnings
   indicate potential defects and **shall** be resolved before merging.

c. Spotless **shall** pass without modifications. Run `./gradlew
   spotlessCheck` before committing.

d. `@SuppressWarnings` **shall** only be used with a comment
   documenting the justification:
   ```java
   @SuppressWarnings("unchecked") // Safe: CTRE API returns raw SignalValue
   ```

#### CODE-GEN-008 -- External Input Validation

a. Controller inputs **shall** be validated before use. Deadbands,
   trigger thresholds, and input shaping are defined in
   `InputDriver.java` and **shall** be applied consistently.

b. Auto chooser values **shall** be validated. A `default` case or
   null check **shall** handle unexpected values.

c. CAN bus data **shall** be validated by checking `StatusCode`
   returns from CTRE signal refresh operations.

#### CODE-GEN-009 -- Public Method Argument Validation

a. Public methods that accept object parameters **should** validate
   that arguments are non-null before use, either through explicit
   null checks or through `Objects.requireNonNull()`.

#### CODE-GEN-010 -- Runtime Failure Minimization

a. Code **shall** be written to minimize runtime exceptions. The
   following categories **shall** be addressed:

   - **NullPointerException**: Check references before dereference
     when the value may be null (e.g., return values from external
     APIs, optional fields).
   - **ArrayIndexOutOfBoundsException**: Validate array indices
     against array length before access.
   - **IllegalStateException**: State machines **shall** handle all
     enum values in switch statements, including a `default` case.
   - **ArithmeticException**: Division operations **shall** check
     for zero divisors when the divisor comes from a variable.

#### CODE-GEN-011 -- Annotation Discipline

a. `@Override` **shall** always be present on overriding methods
   (CODE-GEN-003d).

b. `@SuppressWarnings` **shall** not be used without a documented
   justification comment (CODE-GEN-007d).

c. `@Deprecated` **should** include a `@deprecated` JavaDoc tag
   explaining the replacement.

#### CODE-GEN-012 -- Prohibited Standard Library Usage

a. `System.out.println()` and `System.err.println()` **shall** not
   be used for robot telemetry or error reporting. Use:
   - `Logger.recordOutput()` for telemetry data
   - `DriverStation.reportError()` for errors
   - `DriverStation.reportWarning()` for warnings
   - `Elastic.sendNotification()` for driver-visible notifications

b. `System.currentTimeMillis()` **shall** not be used for robot
   timing. Use `Timer.getFPGATimestamp()` which is synchronized
   with the FPGA clock and is monotonic.

#### CODE-GEN-013 -- Object Reuse in Real-Time Code

a. The following objects **shall** be pre-allocated and reused rather
   than created in periodic methods:

   - `Timer` objects
   - CTRE control requests (handled by `TransmissionFX`)
   - `Notification` objects for Elastic dashboard
   - Arrays and collections used for computation

b. Collections that grow dynamically (e.g., `ArrayList`, `HashMap`)
   **should** be pre-sized in `robotInit()` if their maximum size
   is known.

#### CODE-GEN-014 -- Exception Handling

> *Industry note: CERT Java ERR00-J says "do not suppress or ignore
> checked exceptions." Swallowing an exception hides the problem --
> the motor might not be configured, the path might not have loaded,
> and you will not know until the match starts.*

a. Periodic methods (`robotPeriodic`, `teleopPeriodic`,
   `autonomousPeriodic`, etc.) **shall not** throw exceptions. All
   exceptions **shall** be caught and reported via
   `DriverStation.reportError()`.

b. `catch (Throwable t)` and `catch (Exception e)` **shall** not be
   used broadly. Catch the most specific exception type possible.

c. Exceptions **shall** not be used for normal control flow. Use
   return values, enums, or state variables instead.

d. When calling external APIs that may throw (e.g., file I/O for
   path loading), the call **shall** be wrapped in a try-catch that
   logs the error and provides a safe fallback.

#### CODE-GEN-015 -- Null Safety

a. Public methods **should** document their null behavior in JavaDoc
   (`@param` descriptions should state whether null is permitted).

b. When null is a valid state (e.g., "no path loaded"), prefer
   `Optional<T>` over nullable references for return values.

c. When null indicates an error, prefer throwing
   `IllegalStateException` (as done in `getInstance()`) over
   returning null.

#### CODE-GEN-016 -- Singleton Pattern

a. All subsystems **shall** use the established singleton pattern
   with two `getInstance()` methods:

   ```java
   private static MySubsystem mInstance;

   public static MySubsystem getInstance(final TObj argParent) {
       if (mInstance == null) {
           mInstance = new MySubsystem(argParent);
       }
       return mInstance;
   }

   public static MySubsystem getInstance() {
       if (mInstance == null) {
           throw new IllegalStateException("MySubsystem not initialized");
       }
       return mInstance;
   }
   ```

b. The `getInstance(TObj)` overload creates the instance on first
   call. The no-argument `getInstance()` **shall** throw
   `IllegalStateException` if not yet initialized.

c. Subsystem singletons **shall** only be created in `Robot.robotInit()`
   and registered with `SubsystemManager.addSubsystem()`.

d. After creation, subsystem references **shall** be stored in
   `Globals.java` as `public static` fields so that other classes
   can access them without calling `getInstance()`:

   ```java
   /* In Robot.robotInit() */
   Globals.subsystemA = SubsystemA.getInstance(ntRobot);
   mSubsystemManager.addSubsystem(Globals.subsystemA);

   /* In Globals.java */
   public static SubsystemA subsystemA;
   public static SubsystemB subsystemB;
   public static SubsystemC subsystemC;
   public static SubsystemD subsystemD;
   public static InputDriver controllerDriver;
   ```

#### CODE-GEN-017 -- Object Equality

a. Object comparison **shall** use `.equals()`, not `==`, unless
   checking for null or intentional reference identity:

   ```java
   /* WRONG: compares references, not values */
   if (statusCode == StatusCode.OK) { ... }  // Actually OK for enums (see below)
   if (name == "TargetName") { ... }                // WRONG: use .equals()

   /* CORRECT */
   if (name.equals("Hub")) { ... }
   if ("Hub".equals(name)) { ... }  // null-safe idiom
   ```

b. Enum comparison with `==` is acceptable and preferred (enums are
   singletons in Java):

   ```java
   if (mControlState == ExampleControlState.IDLE) { ... } // OK
   ```

c. If a class overrides `equals()`, it **shall** also override
   `hashCode()` to maintain the general contract. Failing to do so
   causes broken behavior in `HashMap`, `HashSet`, and other
   hash-based collections.

#### CODE-GEN-018 -- Resource Management

a. Code that opens resources (files, streams, connections) **shall**
   use try-with-resources to guarantee cleanup:

   ```java
   try (BufferedReader reader = Files.newBufferedReader(path)) {
       // ...
   } catch (IOException e) {
       DriverStation.reportError("Failed to read: " + e.getMessage(), false);
   }
   ```

   This is particularly relevant for auto path loading from the
   filesystem.

#### CODE-GEN-019 -- Mutable Static Fields

a. Mutable `static` fields **shall** only be used for the singleton
   pattern (`mInstance`). Other shared mutable state **should** be
   passed explicitly through constructors or method parameters.

b. **Accepted exceptions** (documented here to avoid repeated review
   flags):
   - `Globals.java` fields — public static references to subsystems and
     `InputDriver`. These serve as a global registry initialized once in
     `Robot.robotInit()` and read thereafter. They use no `m` prefix
     because they are static, not instance fields (exception to
     CODE-VAR-001a).

---

### 4.2 Format

> **Note:** Most formatting rules are automatically enforced by Spotless
> (Google Java Format, AOSP variant). This section documents what the
> formatter does and identifies rules that require manual compliance.

#### CODE-FMT-001 -- Line Length

a. Line length is managed by Spotless `reflowLongStrings()`. Developers
   should write readable lines; the formatter will wrap as needed.

b. When manually wrapping, continuation lines **should** be indented
   by 8 spaces (double indent) from the original line.

#### CODE-FMT-002 -- Braces

> *Industry note: MISRA Rule 15.6 requires that the body of every
> if/else/while/for/switch be a compound statement (i.e., wrapped in
> braces). The Barr Group standard has the same rule. The reason: a
> one-liner without braces invites someone to add a second line that
> looks like it is inside the block but is not. This has caused
> real security vulnerabilities (look up Apple's "goto fail" bug).*

a. Braces **shall** surround all blocks following `if`, `else`,
   `while`, `do`, `for`, and `switch` statements, even when the
   block contains a single statement. This requires manual
   compliance; Spotless formats existing braces but does not add
   missing ones.

b. Brace style follows Google Java Format AOSP: opening brace on
   the same line as the statement, closing brace on its own line.

#### CODE-FMT-003 -- Parentheses

> *Industry note: MISRA Rule 12.1 says operator precedence should be
> made explicit with parentheses. Not everyone memorizes that `&&`
> binds tighter than `||`, or that `<<` binds tighter than `+`. Adding
> parentheses costs nothing and prevents misreading.*

a. Parentheses **shall** be used to clarify operator precedence when
   mixing different categories of operators (arithmetic with comparison,
   bitwise with logical, ternary within larger expressions):

   ```java
   /* CORRECT: explicit precedence for mixed operators */
   if ((flags & MASK) != 0) { ... }
   if ((a + b) > (c * d)) { ... }
   ```

   For simple comparisons joined by `&&` or `||`, extra parentheses
   are **recommended** but not required -- Java's precedence for
   comparison operators (`>`, `<`, `==`) binding tighter than logical
   operators (`&&`, `||`) is well-established:

   ```java
   /* Both are acceptable */
   if ((depth > 0) && (depth < MAX_DEPTH)) { ... }
   if (depth > 0 && depth < MAX_DEPTH) { ... }
   ```

#### CODE-FMT-004 -- Blank Lines and Section Comments

a. Each source line **shall** contain only one statement.

b. Blank lines **shall** separate logical blocks: between methods,
   before and after control structures, between declaration groups
   and executable code.

c. Section comments using block comment style **shall** be used to
   organize code within classes:

   ```java
   /*
    * Variables
    */

   /*
    * Robot Lifecycle
    */
   ```

d. Section comment text **shall** be concise (1-3 words).

#### CODE-FMT-005 -- Indentation

a. Each indentation level **shall** be 4 spaces (AOSP standard).
   Enforced by Spotless.

#### CODE-FMT-006 -- Tabs

a. Tab characters **shall** not appear in source files. Enforced
   by Spotless.

#### CODE-FMT-007 -- Line Endings

a. All source files **shall** use LF (0x0A) line endings. This is
   **not** automatically enforced by Spotless (the `endWithNewline()`
   setting only ensures a trailing newline character, not the line
   ending style). Enforce LF via `.gitattributes` (`* text=auto eol=lf`)
   or by adding `lineEndings('UNIX')` to the Spotless configuration.

#### CODE-FMT-008 -- Import Statements

a. Wildcard imports (`import foo.*`) **shall** not be used, with one
   documented exception: `import static edu.wpi.first.units.Units.*`
   is an established WPILib convention and is permitted.

b. Unused imports **shall** be removed. Spotless
   `removeUnusedImports()` enforces this automatically.

c. Import order is managed by Spotless. Do not manually reorder.

---

### 4.3 Modules and Files

#### CODE-MAF-001 -- Class and File Naming

a. Class names **shall** use PascalCase: `ExampleDrive`, `ExampleShooter`,
   `ExampleSubsystemConstants`.

b. Each `.java` file **shall** contain exactly one top-level public
   class. The file name **shall** match the class name.

c. Inner classes and enums **shall** also use PascalCase:
   `ExampleControlState`, `ExampleMode`.

d. Test classes **shall** be named `<ClassName>Test.java`.

#### CODE-MAF-002 -- Package Structure

a. Year-specific robot code **shall** be in
   `com.team271.frc<year>` (e.g., `com.team271.frc2026`).

b. Reusable library code **shall** be in `com.team271.lib` and its
   subpackages.

c. Subsystems **shall** be in the `subsystems` subpackage. Input
   handling **shall** be in `subsystems.Input`.

   **Exception:** the swerve drivetrain class implements the WPILib
   `edu.wpi.first.wpilibj2.command.Subsystem` interface (not the
   team271 `com.team271.lib.subsystem.Subsystem` base class) because
   it extends the CTRE-generated drivetrain class. It is owned by the drive subsystem
   and does not participate in the `SubsystemManager` lifecycle
   directly.

d. Package names **shall** be all lowercase with no underscores.

#### CODE-MAF-003 -- Import Rules

a. Absolute imports **shall** be used. No relative imports.

b. Static imports **should** be used for frequently referenced
   constants when it improves readability (e.g.,
   `import static com.team271.frc<year>.Constants.CAN_BUS_CANIVORE_SUBSYSTEMS`).

c. Each source file **shall** be free of unused imports (enforced
   by Spotless).

#### CODE-MAF-004 -- Constants Organization

a. Robot-wide constants **shall** be organized in `Constants.java`
   using nested `public static final class` inner classes grouped
   by subsystem or function:

   ```java
   public static final class ExampleSubsystemConstants { ... }
   public static final class ExampleSubsystemConstants { ... }
   public static final class AutoConstants { ... }
   ```

b. Each inner constants class **shall** have a `private` constructor
   to prevent instantiation.

c. Constants specific to a single subsystem **may** alternatively be
   defined as `private static final` fields within that subsystem
   class, but **should** be placed in `Constants.java` when they are
   referenced by multiple classes.

#### CODE-MAF-005 -- Generated Code

a. Generated files (`BuildConstants.java`, `TunerConstants.java`)
   **shall** not be manually edited. Changes to generated code
   should be made through the generating tool.
   Note: `BuildConstants.java` is generated by gversion into the
   `frc.robot` package (not `com.team271.frc<year>`) as configured
   in `build.gradle`.

b. Generated files are exempt from formatting rules in this standard
   but are still subject to safety rules (e.g., current limits in
   `TunerConstants.java` should be reviewed).

---

### 4.4 Methods

#### CODE-FUN-001 -- Method Naming Convention

a. Method names **shall** use camelCase: `robotInit()`,
   `teleopPeriodic()`, `outputTelemetry()`.

b. Boolean-returning methods **shall** use `is`, `has`, or `can`
   prefixes: `isZeroed()`, `isAtMaxVelocity()`, `hasTarget()`.
   (Note: the `Subsystem` base class method is `isZeroed()`;
   domain-specific aliases like `isHomed()` are acceptable.)

c. Getter methods **shall** use `get` prefix: `getAutoTimer()`,
   `getValue()`.

d. Factory methods **should** use `create` or `of` prefix.

e. Lifecycle methods **shall** match WPILib naming exactly:
   `robotInit`, `robotPeriodic`, `autonomousInit`,
   `autonomousPeriodic`, `teleopInit`, `teleopPeriodic`,
   `disabledInit`, `disabledPeriodic`.

f. Method names **shall** be descriptive of their purpose. Use verbs
   in method names (e.g., `configCurrentLimitStator()`,
   `runHoming()`, `runAgitation()`).

#### CODE-FUN-002 -- Method Discipline

a. A single return point at the end of a method is preferred but not
   mandatory. Early returns are acceptable for guard clauses:

   ```java
   public void teleopPeriodic(final double argTimestamp) {
       if (!isZeroed()) {
           return; // Guard clause: acceptable early return
       }
       // ... main logic
   }
   ```

b. Recursion **shall** not be used in any code that executes during
   periodic methods. All call graphs reachable from periodic methods
   **shall** be acyclic. The 20ms cycle budget cannot accommodate
   unbounded recursion.

   > *Industry note: JPL Rule 1 prohibits all recursion -- direct and
   > indirect. MISRA Rule 17.2 says "functions shall not call themselves,
   > either directly or indirectly." If your function calls itself and
   > the base case has a bug, the call stack grows until the program
   > crashes. On a spacecraft or a robot mid-match, that is game over.*

c. All method parameters **shall** be declared `final`:

   ```java
   public void setValue(final double argSpeed) { ... }
   ```

d. Method bodies **should** not exceed 80 lines of code, excluding
   blank lines and comments. Methods that exceed this limit
   **should** be decomposed into smaller helper methods.

   > *Industry note: JPL Rule 4 says no function should be longer than
   > about 60 lines (one printed page). Short methods are easier to
   > understand, test, and debug -- especially under time pressure at
   > a competition.*

e. Methods **should** not have more than 7 parameters. If more
   parameters are needed, consider a configuration object or builder
   pattern.

f. All private methods **shall** be declared `private`. All methods
   accessible only within the package **should** use package-private
   (no modifier) or `protected` as appropriate.

#### CODE-FUN-003 -- Utility Methods

a. Shared logic **shall** be extracted into utility methods rather
   than duplicated. Common utilities belong in `com.team271.lib`.

b. Utility classes **shall** be declared `final` with a `private`
   constructor to prevent instantiation.

#### CODE-FUN-004 -- Robot Lifecycle Contract

a. Subsystem lifecycle methods **shall** be called in this order by
   the `SubsystemManager`:

   ```text
   robotInit(argTimestamp)
   → robotPeriodicBefore(argTimestamp)    [read sensors]
   → <mode>Periodic(argTimestamp)         [state machine logic]
   → robotPeriodicAfter(argTimestamp)     [apply motor outputs]
   → outputTelemetry()                   [publish to NT/logs — no parameter]
   ```

b. Motor outputs **shall** only be commanded in `robotPeriodicAfter()`,
   never in `teleopPeriodic()` or `autonomousPeriodic()`. The periodic
   methods set *desired* state; `robotPeriodicAfter()` *applies* it.

c. Sensor reading **shall** be done in `robotPeriodicBefore()`, not
   in the mode-specific periodic methods.

#### CODE-FUN-005 -- State Machine Pattern

> *Industry note: DO-178C (the avionics software certification standard)
> emphasizes deterministic, traceable state management. The
> desired-state/actual-state pattern makes every transition explicit and
> auditable -- you can always answer "what state is the robot in and why
> did it get there?" This is the same pattern used in flight control
> software.*

a. Subsystems that use state machines **shall** maintain two state
   variables: `mControlState` (current) and `mDesiredControlState`
   (desired). The desired state is set in `teleopPeriodic()` or
   `autonomousPeriodic()`; the actual state is applied in
   `robotPeriodicAfter()`.

   ```java
   /* Set in teleopPeriodic */
   mDesiredControlState = ExampleControlState.INDEX;

   /* Applied in robotPeriodicAfter */
   mControlState = mDesiredControlState;
   switch (mControlState) {
       case INDEX:
           setValue(ExampleSubsystemConstants.EXAMPLE_SPEED);
           break;
       // ...
   }
   ```

b. State enums **shall** include all possible states including `IDLE`.

c. All state transitions **shall** be explicit. No state shall be
   unreachable.

#### CODE-FUN-006 -- Defensive Checks

> *Industry note: MISRA Rule 16.4 requires every switch statement to have
> a default label. Even if you think you have covered every enum value,
> someone might add a new value later and forget to update the switch.
> The default case catches that.*

a. Every `switch` statement on an enum **shall** include a `default`
   case that reports the unexpected value:

   ```java
   default:
       DriverStation.reportError(
           "Unexpected ExampleControlState: " + mControlState, false);
       break;
   ```

b. Public methods that accept object parameters **should** validate
   non-null at entry.

c. Array and list accesses with variable indices **shall** include
   bounds checks when the index comes from external input.

---

### 4.5 Variables

#### CODE-VAR-001 -- Variable Naming Convention

a. Instance fields (non-static, non-final) **shall** use the `m`
   prefix followed by camelCase:

   ```java
   private ExampleControlState mControlState = ExampleControlState.IDLE;
   private double mTimestamp = 0;
   private boolean mWasIndexing = false;
   ```

b. Method parameters **shall** use the `arg` prefix followed by
   camelCase:

   ```java
   public void robotInit(final double argTimestamp) { ... }
   public static ExampleSubsystem getInstance(final TObj argParent) { ... }
   ```

   **Note:** The team's `TObj` base class defines lifecycle methods
   with `argTimestamp` parameters. WPILib's own lifecycle methods
   (e.g., `robotPeriodic()`, `teleopPeriodic()`) take no parameters;
   the `argTimestamp` is a Team 271 convention passed through
   `SubsystemManager`.

c. Local variables **shall** use camelCase without prefix. Short-lived
   temporary variables **may** use the `tmp` prefix:

   ```java
   double tmpVoltage = mSpeed * MAX_VOLTAGE;
   ```

d. Fields that wrap NetworkTables entries (`NTEntry`, `NTTable`)
   **should** use the `nt` prefix:

   ```java
   private TRobot ntRobot;
   final NTEntry ntMMCruiseVel;
   ```

e. Static final constants **shall** use one of two naming
   conventions based on their purpose:

   - **UPPER_SNAKE_CASE** for operational and fixed constants
     (speeds, RPS targets, bus names, physical measurements,
     duty cycles):

     ```java
     public static final double SHOOT_RPS = 38.0;
     public static final String CAN_BUS_CANIVORE_A = "BusA";
     private static final int TELEMETRY_PERIOD = 5;
     ```

   - **`k` prefix + camelCase** (`kCamelCase`) for tunable
     hardware-configuration constants (PID gains, current limits,
     timing thresholds, homing parameters, path names):

     ```java
     public static final double kTranslationKp = 2.0;
     public static final int kExampleCurrentStatorLimit = 120;
     public static final double kHomingTimeoutSec = 4.0;
     public static final String kPathNameStation1 = "<year>PathName1";
     ```

   The `k` prefix signals "this value is likely to be tuned during
   testing" and groups tunable constants visually in autocomplete.

f. Boolean variables and methods **shall** use positive names
   (`isZeroed`, not `isNotZeroed`; `mEnabled`, not `mDisabled`).

g. No variable name **shall** be a single character except `i`, `j`,
   `k` in simple loop counters. Descriptive names are preferred even
   for loop variables (e.g., `channelIndex`, `moduleIndex`).

h. No variable **shall** shadow a variable from an enclosing scope.

#### CODE-VAR-002 -- Variable Initialization

> *Industry note: MISRA Rule 9.1 (a Mandatory rule -- their strongest
> category) says variables shall not be read before they are set. Reading
> an uninitialized variable is one of the oldest and most common bugs in
> software. Initializing at declaration eliminates the possibility.*

a. All instance fields **shall** be initialized at the point of
   declaration or in the constructor:

   ```java
   private ExampleControlState mControlState = ExampleControlState.IDLE;
   private double mSpeed = 0.0;
   private boolean mIsHomed = false;
   ```

b. Local variables **shall** be initialized before use. Prefer
   initialization at the point of declaration.

#### CODE-VAR-003 -- Type Conventions

a. Use `int` for general-purpose integers. Java `int` is always
   32-bit signed.

b. Use `double` for all floating-point values. This is the standard
   type for WPILib and CTRE APIs. Avoid `float` unless required by
   a specific API.

c. Use `boolean` for true/false values. Unlike C/C++, Java enforces
   this at the compiler level -- `if (1)` or `if (count)` will not
   compile. Use explicit comparisons: `if (count > 0)` instead.

d. Use `long` only for timestamps, large counters, or when required
   by an API.

e. Use enums for state machines and mode selection, never integer
   constants.

#### CODE-VAR-004 -- Floating-Point Comparison

a. Floating-point values **shall** not be compared using `==` or
   `!=`. Use epsilon-based comparison. While comparing against an
   assigned literal zero (e.g., `voltage == 0.0`) is technically
   safe in IEEE 754, we use epsilon comparison uniformly for
   consistency:

   ```java
   /* WRONG */
   if (voltage == 0.0) { ... }

   /* CORRECT */
   private static final double EPSILON = 1e-6;
   if (Math.abs(voltage) < EPSILON) { ... }
   ```

b. Be aware of `Double.NaN`, `Double.POSITIVE_INFINITY`, and
   `Double.NEGATIVE_INFINITY`. Check for these when processing
   sensor data that may produce invalid readings.

#### CODE-VAR-005 -- Enum Conventions

a. Enum values **shall** use UPPER_SNAKE_CASE:

   ```java
   public enum ExampleControlState {
       IDLE,
       SHOOT
   }
   ```

b. Enums used for state machines **shall** include a brief comment
   or JavaDoc describing each value's purpose:

   ```java
   public enum ExampleControlState {
       IDLE,
       INDEX,
       COAST_DOWN, // brief neutral coast before reversing
       REVERSE
   }
   ```

c. The `IDLE` state **should** be listed first in state machine enums.

d. Enum initialization: if explicit values are needed (rare in Java),
   either initialize only the first or initialize all.

#### CODE-VAR-006 -- Named Constants and Units

a. Physical units **shall** be documented in the constant name or in
   a comment:

   ```java
   public static final double WHEEL_RADIUS_M = 0.0508; // meters
   public static final double kHomingTimeoutSec = 2.0;
   public static final double MAX_SPEED_MPS = 4.5; // meters per second
   ```

b. Recommended unit suffixes: `_M` (meters), `_SEC` (seconds),
   `_MS` (milliseconds), `_DEG` (degrees), `_RAD` (radians),
   `_RPS` (rotations per second), `_MPS` (meters per second),
   `_V` (volts), `_A` (amps).

c. Hexadecimal constants (rare in FRC code) **shall** use lowercase
   `0x` prefix: `0xFF`.

#### CODE-VAR-007 -- Identifier Clarity

a. Identifiers **shall** not differ only by:
   - Case: `statusFlag` vs `StatusFlag`
   - Visually similar characters: `l` (lowercase L) vs `1` (digit),
     `O` (uppercase O) vs `0` (digit zero)

#### CODE-VAR-008 -- Magic Number Prohibition

a. Numeric constants other than `0`, `1`, `-1`, `0.0`, `1.0`, and
   `2.0` **shall** be replaced by named constants when used in
   executable code.

b. All tunable values (voltages, speeds, PID gains, current limits,
   timing windows) **shall** be defined as named constants, either
   in `Constants.java` or as `private static final` fields.

   ```java
   /* WRONG: magic numbers */
   if (channel > 31) {
       timeout = 5000;
   }

   /* CORRECT: named constants */
   if (channel > MAX_CHANNEL_INDEX) {
       timeout = DEFAULT_TIMEOUT_MS;
   }
   ```

#### CODE-INT-001 -- Shift Operator Constraints

> **Note:** Bit-shift operators are rare in FRC Java code. This rule
> is included for completeness.

a. If bit-shift operators (`<<`, `>>`) are used, the shift amount
   **shall** be non-negative and less than 32 (for `int`) or 64 (for
   `long`). Shifting by a negative number or by more than the bit
   width produces unpredictable results.

#### CODE-INT-002 -- Division by Zero Prevention

a. Before any division (`/`) or remainder (`%`) operation where the
   divisor is a variable, the divisor **shall** be checked for zero:

   ```java
   if (divisor != 0) {
       result = dividend / divisor;
   } else {
       result = 0; // or appropriate default
       DriverStation.reportError("Division by zero in ...", false);
   }
   ```

---

### 4.6 Control Structures

#### CODE-CTL-001 -- If-Else Statements

> *Industry note: MISRA Rule 13.4 says "the result of an assignment
> operator should not be used." Writing `if (x = getValue())` when you
> meant `if (x == getValue())` is an easy typo that compiles fine but
> does something completely different. Banning assignments in conditions
> eliminates this entire class of bug.*

a. Assignments **should** not be made within an `if` or `else if`
   condition expression. In Java, the compiler prevents most
   accidental assignment-in-condition bugs (it rejects non-boolean
   assignments in conditions), but `boolean` assignments like
   `if (found = search())` can still slip through.

b. Any `if` statement with an `else if` clause **should** end with
   an `else` clause.

c. Nested if-else statements **should** not be deeper than 4 levels.
   Use method extraction or switch statements to reduce complexity.

d. The ternary operator (`? :`) is acceptable for simple value
   selection but **should** be avoided for complex expressions or
   when either branch has side effects.

#### CODE-CTL-002 -- Switch Statements

a. All `switch` statements **shall** contain a `default` case
   (CODE-FUN-006a).

b. Fall-through between `case` labels **shall** not occur without
   an explicit `// fall through` comment.

c. Switch expressions using the `->` syntax are permitted and
   preferred for value computation:

   ```java
   double targetRps = switch (mShotMode) {
       case MODE_A -> ExampleSubsystemConstants.MODE_A_VALUE;
       case MODE_B -> ExampleSubsystemConstants.MODE_B_VALUE;
   };
   ```

d. Traditional `switch` with `break` **shall** be used for
   side-effectful cases (motor commands, state transitions).

#### CODE-CTL-003 -- Loops

a. Magic numbers **shall** not be used in loop bounds. Use named
   constants.

b. Loop variables in `for` statements **shall** not be modified
   inside the loop body. If the iteration pattern requires body
   modification, use a `while` loop instead.

c. All loops in periodic methods **shall** have a provable upper
   bound and **shall** terminate within the 20ms cycle budget.
   Unbounded loops (e.g., `while (true)`) **shall** not appear
   in any code reachable from periodic methods.

   > *Industry note: JPL Rule 2 says "all loops must have a fixed upper
   > bound" that a tool can verify statically. If a loop can run forever,
   > your robot loop stalls and the robot stops responding.*

d. Enhanced for-each loops are preferred when iterating collections:

   ```java
   for (Module module : mModules) {
       module.update();
   }
   ```

#### CODE-CTL-004 -- Array and List Access

a. Array index expressions **shall** not contain side effects.
   This pattern is uncommon in FRC Java but can appear in buffer
   management or manual array operations:

   ```java
   /* WRONG */
   buffer[idx++] = value;

   /* CORRECT */
   buffer[idx] = value;
   idx++;
   ```

b. When an array or list index is derived from external input,
   bounds **shall** be checked before access.

#### CODE-CTL-005 -- Side-Effect-Free Logging

a. Arguments to `Logger.recordOutput()` **shall** not contain side
   effects. Compute values before passing them to the logger.

b. Arguments to assertion methods **shall** be side-effect-free.

#### CODE-CTL-006 -- Logical Operator Short-Circuit

> *Industry note: MISRA Rule 13.5 says "the right hand operand of a
> logical && or || operator shall not contain persistent side effects."
> Because Java short-circuits these operators, the right side might not
> execute at all. If it has a side effect (like modifying a variable),
> your code behaves differently depending on the left side's value --
> a subtle and hard-to-debug inconsistency.*

a. The right-hand operand of `&&` and `||` operators **should** not
   contain side effects (method calls that modify state). Short-circuit
   evaluation may skip the right operand:

   ```java
   /* WRONG: resetSensor() may not execute if path is null */
   if ((path != null) && (resetSensor(path) == StatusCode.OK))

   /* CORRECT: separate the side effect */
   if (path != null) {
       StatusCode result = resetSensor(path);
       if (result == StatusCode.OK) {
           // ...
       }
   }
   ```

#### CODE-CTL-007 -- Enhanced Switch

a. Switch expressions (`->`) are preferred for value computation
   (no fall-through risk, exhaustive checking by compiler).

b. Traditional `switch` with `:` and `break` is required when cases
   have side effects (motor commands, state assignments, method calls).

c. When switching on an enum, the compiler verifies exhaustiveness
   for switch expressions. A `default` case is still required for
   traditional `switch` statements (CODE-FUN-006a).

#### CODE-CTL-008 -- Exception Control Flow

a. Exceptions **shall** not be used for normal control flow. Use
   return values, enums, or `Optional<T>` instead.

b. `catch` blocks **shall** catch the most specific exception type:

   ```java
   /* WRONG */
   catch (Exception e) { ... }

   /* CORRECT */
   catch (IOException e) { ... }
   ```

c. `catch (Throwable t)` **shall** not be used except in top-level
   robot lifecycle methods where preventing a crash is critical.

---

### 4.7 Comments

#### CODE-COM-001 -- Comment Formats

a. **JavaDoc** (`/** ... */`) **shall** be used for:
   - All public classes
   - All public methods
   - All public constants that are not self-documenting

   JavaDoc uses `@param`, `@return`, `@throws`, `@see`,
   `{@link}`, `{@code}` tags.

b. **Block comments** (`/* ... */`) **shall** be used for section
   separators within classes (see CODE-FMT-004c).

c. **Line comments** (`//`) **shall** be used for inline explanations
   within method bodies.

d. Comments **shall** not be used to disable blocks of code in
   released code. Use version control to track removed code.

   > *Industry note: MISRA Directive 4.4 says "sections of code should
   > not be commented out." Commented-out code rots fast -- it stops
   > compiling as the surrounding code changes, and readers cannot tell
   > if it was disabled on purpose or by accident. Git keeps the history;
   > delete the code and move on.*

#### CODE-COM-002 -- Comment Content

a. Every subsystem class **shall** have a class-level JavaDoc
   describing:
   - What mechanism the subsystem controls
   - Its control states
   - Its relationship to other subsystems

b. State machine enum values **shall** include a brief comment
   when the state name alone is not self-explanatory:

   ```java
   COAST_DOWN, // brief neutral coast before reversing to avoid current spike
   ```

c. Load-bearing ordering **shall** be documented with a `WARNING`
   comment:

   ```java
   /*
    * *** SUBSYSTEM ADD ORDER IS LOAD-BEARING ***
    * SubsystemA MUST be added before SubsystemB so that
    * SubsystemA.robotPeriodicBefore() updates its shared state
    * before SubsystemB reads it in the same cycle.
    */
   ```

d. `TODO:` comments **shall** include the author or a tracking issue:

   ```java
   // TODO(jane): Define subsystem motor API
   // TODO: #42 - Implement auto path for depot
   ```

e. Avoid explaining the obvious. Assume the reader knows Java. Focus
   comments on *why*, not *what*.

f. Magic-looking constants **shall** have a comment explaining their
   derivation:

   ```java
   /* 7.03:1 drive ratio, 4in wheel = 0.0508m radius */
   public static final double DRIVE_GEAR_RATIO = 7.03;
   ```

---

### 4.8 Debugging and Telemetry

#### CODE-BUG-001 -- Telemetry Discipline

a. All subsystems **shall** implement `outputTelemetry()` and publish
   state information via `Logger.recordOutput()`.

b. At minimum, each subsystem **shall** log:
   - Current control state
   - Motor output values (voltage, duty cycle, or velocity target)
   - Sensor readings used for control decisions

c. Telemetry key naming **shall** follow the pattern
   `"SubsystemName/ValueName"`:

   ```java
   Logger.recordOutput("ExampleSubsystem/ControlState", mControlState.toString());
   Logger.recordOutput("ExampleSubsystem/Speed", indexSpeed);
   ```

d. Telemetry **may** be rate-limited to reduce CAN bus load. The
   `TELEMETRY_PERIOD` pattern (publish every Nth cycle) is acceptable.

#### CODE-BUG-002 -- Dashboard Notifications

a. `Elastic.sendNotification()` **should** be used for significant
   events visible to the drive team:
   - Mode transitions
   - Homing completion
   - Error conditions

b. Notifications **should** include a display time
   (`withDisplayMilliseconds()` or the `displayTimeMillis` constructor
   parameter) to avoid cluttering the dashboard.

#### CODE-BUG-003 -- Error Reporting

a. `DriverStation.reportError()` **shall** be used for unexpected
   states (e.g., `default` case in enum switch).

b. `DriverStation.reportWarning()` **shall** be used for recoverable
   issues (e.g., CAN bus refresh failures).

c. Repeated warnings **shall** be rate-limited to avoid console spam.
   Use a "last status" pattern:

   ```java
   if (refreshStatus != mLastCANRefreshStatus) {
       DriverStation.reportWarning("CAN refresh: " + refreshStatus, false);
       mLastCANRefreshStatus = refreshStatus;
   }
   ```

#### CODE-BUG-004 -- Runtime Tunability

a. All configurable control values **shall** be tunable at runtime via
   SmartDashboard / NetworkTables using the `LoggedNTInput` +
   `checkTuning()` pattern. This includes:
   - PID gains (P, I, D, tolerance, deadband, I zone, output range)
   - Current limits (stator and supply enable/value)
   - Voltage limits (peak forward, peak reverse)
   - Motion Magic profiles (cruise velocity, acceleration, jerk)
   - Velocity targets (RPS/RPM setpoints per mode)
   - Duty cycle targets
   - Threshold values (velocity gating, jam detection, homing current)

b. Values that use the team library classes (`PIDBase`,
   `ControllerSmart`, `TransmissionFX`) **shall** inherit tunability
   automatically via the library's built-in `checkTuning()`. Subsystem
   code **shall not** re-implement tuning for values already exposed
   by the library.

c. Values defined as `static final` constants in `Constants.java` that
   are read once at initialization and not subsequently updatable
   **should** be migrated to `LoggedNTInput` fields in the subsystem
   class, using the constant as the default value. The `checkTuning()`
   method in the subsystem's `outputTelemetry()` applies dashboard
   changes.

d. Compile-time constants **shall** remain acceptable for values that
   are physically fixed (CAN IDs, gear ratios, wheel diameter),
   timing that is structurally load-bearing (auto move sequencing),
   and safety bounds that should not be adjustable during a match
   (absolute max current, emergency stop thresholds).

e. All `LoggedNTInput` fields **shall** be initialized with safe
   default values from the corresponding `Constants` class.

f. `checkTuning()` **shall** be called at the beginning of
   `outputTelemetry()`, ensuring tuning changes are applied at the
   end of the robot cycle (after control outputs, before the next
   sensor read).

---

## Companion Documents

The following companion documents are part of this standard:

| Document | Contents |
| -------- | -------- |
| [Team271-Software-Coding-Standard-Safety.md](Team271-Software-Coding-Standard-Safety.md) | §4.9 Safety Practices (CODE-SAF-*) |
| [Team271-Software-Coding-Standard-Compliance.md](Team271-Software-Coding-Standard-Compliance.md) | §5 Static Analysis and enforcement matrix |
| [Team271-Software-Coding-Standard-Templates.md](Team271-Software-Coding-Standard-Templates.md) | Appendix F (Subsystem Template) and Appendix G (Constants Template) |
| [Team271-Software-Coding-Standard-Java.md](Team271-Software-Coding-Standard-Java.md) | Appendix H (CTRE Phoenix 6 Usage Patterns) |
| [Team271-Software-Coding-Standard-Appendices.md](Team271-Software-Coding-Standard-Appendices.md) | Appendices A–E and I |
