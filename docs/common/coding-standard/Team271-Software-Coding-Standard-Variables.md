<!-- markdownlint-disable MD007 MD013 MD031 MD032 MD041 -->
<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Variables

### CODE-VAR-001 -- Variable Naming Convention

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
   public static ExampleSubsystem getInstance(final LifecycleBase argParent) { ... }
   ```

   **Note:** The team's `LifecycleBase` base class defines lifecycle methods
   with `argTimestamp` parameters. WPILib's own lifecycle methods
   (e.g., `robotPeriodic()`, `teleopPeriodic()`) take no parameters;
   the `argTimestamp` is a Team 271 convention passed through
   `SubsystemManager`.

c. Local variables **shall** use camelCase without prefix. Short-lived
   temporary variables **may** use the `tmp` prefix:

   ```java
   double tmpVoltage = mSpeed * MAX_VOLTAGE;
   ```

d. Fields that wrap NetworkTables entries (`NtPublisher`, `NtTable`)
   **should** use the `nt` prefix:

   ```java
   private RobotObj ntRobot;
   final NtPublisher ntMMCruiseVel;
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

### CODE-VAR-002 -- Variable Initialization

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

### CODE-VAR-003 -- Type Conventions

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

### CODE-VAR-004 -- Floating-Point Comparison

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

### CODE-VAR-005 -- Enum Conventions

a. Enum values **shall** use UPPER_SNAKE_CASE:

   ```java
   public enum ExampleControlState {
       IDLE,
       SHOOT
   }
   ```

b. Enums used for state machines **shall** include a brief comment
   or JavaDoc when the value's name is not self-explanatory (see
   [`CODE-COM-002b`](Team271-Software-Coding-Standard-Comments.md)):

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

### CODE-VAR-006 -- Named Constants and Units

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

### CODE-VAR-007 -- Identifier Clarity

a. Identifiers **shall** not differ only by:
   - Case: `statusFlag` vs `StatusFlag`
   - Visually similar characters: `l` (lowercase L) vs `1` (digit),
     `O` (uppercase O) vs `0` (digit zero)

### CODE-VAR-008 -- Magic Number Prohibition

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

### CODE-VAR-009 -- Shift Operator Constraints

a. Shift amounts for `<<` and `>>` **shall** be non-negative and
   less than 32 (for `int`) or 64 (for `long`). Shifting by a
   negative number or by more than the bit width produces
   unpredictable results.

### CODE-VAR-010 -- Division by Zero Prevention

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
