<!-- markdownlint-disable MD007 MD013 MD031 MD032 MD041 -->
<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## General

### CODE-GEN-001 -- Common Abbreviations

a. Abbreviations and acronyms should be avoided unless their meanings
   are widely understood. See
   [Appendix A](Team271-Software-Coding-Standard-Appendices.md#appendix-a-approved-identifier-abbreviations) for approved
   abbreviations.

b. FRC-specific abbreviations (CAN, PID, RPS, IMU, etc.) are acceptable
   without expansion in code. They should be defined in class-level
   JavaDoc on first use in documentation.

### CODE-GEN-002 -- APIs and Methods to Avoid

a. The following methods and patterns **shall** not be used in robot code:

| Prohibited | Reason |
| ---------- | ------ |
| `System.exit()` | Terminates the JVM; robot stops responding |
| `Runtime.halt()` | Same as above |
| `Thread.sleep()` | Blocks the robot loop; use `Timer` instead |
| `Thread.stop()` | Deprecated; unsafe thread termination |
| `System.gc()` | Unpredictable GC pauses in real-time code |
| `Object.finalize()` | Deprecated; unreliable cleanup |
| `System.out.println()` | Use `Logger.recordOutput()`, `DriverStation.reportError()`, `DriverStation.reportWarning()`, or `notify.send()` |
| `System.err.println()` | Same as `System.out.println()` above |
| `System.currentTimeMillis()` | Not monotonic, not FPGA-synchronized. Use `Timer.getFPGATimestamp()` |
| Raw `new Thread()` | Use WPILib `Notifier` for background tasks |
| `Math.random()` | Uses a synchronized shared `Random`; non-deterministic without a logged seed, which breaks AdvantageKit replay. Use `edu.wpi.first.math.MathUtil` helpers or `ThreadLocalRandom.current()` if randomness is actually needed. |

b. Reflection and dynamic class loading **shall not** be used in
   periodic methods. They defeat ahead-of-time optimization and can
   trigger unpredictable class-loading pauses mid-match. Init-time
   reflection is permitted (e.g., AdvantageKit metadata registration
   during `robotInit()`).

### CODE-GEN-003 -- Keywords to Use Frequently

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
     private final ExampleTransmission mTransmission;
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

### CODE-GEN-004 -- Garbage Collection Pressure Minimization

> *Industry note: JPL Rule 3 says "do not use dynamic memory allocation
> after initialization." MISRA Directive 4.12 says "dynamic memory
> allocation shall not be used." The reason is the same for a Mars rover
> and our robot: if your code triggers a memory allocation pause in the
> middle of a maneuver, you lose control. Our loop budget is tight.*

a. Object allocations **shall** be minimized in periodic methods.
   These methods run in a tight real-time loop; excessive
   allocations cause GC pauses that stall it.

b. Objects that are used repeatedly **shall** be pre-allocated in
   `robotInit()` or at field declaration and reused:

   ```java
   /* CORRECT: Timer pre-allocated at field declaration */
   private final Timer mReverseTimer = new Timer();

   /* WRONG: Timer allocated in periodic method */
   @Override
   public void teleopPeriodic() {
       Timer t = new Timer(); // BAD: allocates every cycle
   }
   ```

c. CTRE control request objects **shall** be reused, not
   re-created on each call. Declare them as `private final` fields
   and mutate their parameters on each use rather than constructing
   new instances in periodic methods.

d. String concatenation in periodic methods **should** be avoided.
   Use pre-defined constant strings for `Logger.recordOutput()` keys.

e. Autoboxing (e.g., `int` to `Integer`) in loops **shall** be avoided.

f. Driver-notification objects **should** be pre-allocated when used
   repeatedly (e.g., a recurring fault notification).

g. Collections that grow dynamically (e.g., `ArrayList`, `HashMap`)
   **should** be pre-sized in `robotInit()` if their maximum size
   is known.

h. Streams and lambda chains that allocate intermediate collections
   (e.g., `stream().filter(...).collect(toList())`, `.toArray()`)
   **shall not** be used in periodic methods. They are permitted in
   init code where one-time allocation cost is not a concern.

### CODE-GEN-005 -- Return Value Checking

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

### CODE-GEN-006 -- Type Safety

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

### CODE-GEN-007 -- Warning-Free Compilation

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

e. The `@Deprecated` annotation **should** include a `@deprecated`
   JavaDoc tag explaining the replacement.

### CODE-GEN-008 -- External Input Validation

a. **(Robot-project code.)** Controller inputs **shall** be validated
   before use. Deadbands,
   trigger thresholds, and input shaping are defined in
   `InputDriver.java` and **shall** be applied consistently.

b. Auto chooser values **shall** be validated. A `default` case or
   null check **shall** handle unexpected values.

c. CAN bus data **shall** be validated by checking `StatusCode`
   returns from CTRE signal refresh operations.

### CODE-GEN-009 -- Public Method Argument Validation

a. Public methods that accept object parameters **should** validate
   that arguments are non-null before use, either through explicit
   null checks or through `Objects.requireNonNull()`.

### CODE-GEN-010 -- Runtime Failure Minimization

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

### CODE-GEN-011 -- Exception Handling

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
   Exception: `catch (Throwable t)` is permitted only in top-level
   robot lifecycle methods (e.g., `Robot.robotPeriodic`) where
   preventing a crash is critical.

c. Exceptions **shall** not be used for normal control flow. Use
   return values, enums, or state variables instead.

d. When calling external APIs that may throw (e.g., file I/O for
   path loading), the call **shall** be wrapped in a try-catch that
   logs the error and provides a safe fallback.

### CODE-GEN-012 -- Null Safety

a. Public methods **should** document their null behavior in JavaDoc
   (`@param` descriptions should state whether null is permitted).

b. When null is a valid state (e.g., "no path loaded"), prefer
   `Optional<T>` over nullable references for return values.

c. When null indicates an error, prefer throwing
   `IllegalStateException` (as done in `getInstance()`) over
   returning null.

### CODE-GEN-013 -- Singleton Pattern (Robot Projects)

> **Scope:** This rule applies to **robot-project code** only.
> Reusable library subsystems do not use singletons — they are
> instantiated directly and passed by reference.

a. Robot-project subsystems **shall** use the established singleton pattern
   with two `getInstance()` methods:

   ```java
   private static MySubsystem mInstance;

   public static MySubsystem getInstance(final LifecycleBase argParent) {
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

b. The `getInstance(LifecycleBase)` overload creates the instance on first
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

### CODE-GEN-014 -- Object Equality

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

### CODE-GEN-015 -- Resource Management

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

### CODE-GEN-016 -- Mutable Static Fields

> **Scope:** Item `a` applies universally. The `Globals.java` exception
> in item `b` is a **robot-project** convention — reusable library code
> should not depend on it.

a. Mutable `static` fields **shall** only be used for the singleton
   pattern (`mInstance`). Other shared mutable state **should** be
   passed explicitly through constructors or method parameters.

b. **Accepted exceptions in robot-project code** (documented here to
   avoid repeated review flags):
   - `Globals.java` fields — public static references to subsystems and
     `InputDriver`. These serve as a global registry initialized once in
     `Robot.robotInit()` and read thereafter. They use no `m` prefix
     because they are static, not instance fields (exception to
     CODE-VAR-001a).

### CODE-GEN-017 -- Concurrency Keywords

> *Industry note: CERT Java rule LCK00-J says "use private final lock
> objects to synchronize classes that may interact with untrusted
> code," and the broader `java.util.concurrent` design philosophy
> (Goetz et al., *Java Concurrency in Practice*) is to prefer
> higher-level primitives over raw `synchronized` + `volatile`.
> Concurrency bugs are the hardest to reproduce and the hardest to
> debug after a match. The FRC answer is almost always to avoid
> sharing mutable state across threads in the first place — the main
> robot loop and any `Notifier` callback should communicate through
> immutable snapshots, not through locks.*

a. Most robot code is single-threaded (the main robot loop). The
   keywords in this rule only apply when a field is genuinely shared
   between the main thread and a background thread (e.g., a
   `Notifier` callback, a CTRE signal-refresh thread). If you are
   not sure whether you have cross-thread access, you probably do
   not, and you do not need these keywords.

b. The `volatile` keyword **should** only be used when a field is
   accessed from multiple threads. It guarantees visibility of
   writes across threads but does **not** provide atomicity for
   compound operations (e.g., `count++` is still unsafe on a
   `volatile int`).

c. When cross-thread sharing is required, prefer `AtomicReference`,
   `AtomicInteger`, or the other `java.util.concurrent.atomic`
   classes over `volatile`. These provide both visibility and atomic
   compound operations without the ceremony of `synchronized`.

d. `synchronized` blocks **shall** be kept short and **shall not**
   call out to methods that can block (I/O, `Thread.sleep`, CAN
   refresh, etc.). A `synchronized` block held on the main thread
   while a `Notifier` callback contends for the same monitor will
   stall the real-time loop.

---
