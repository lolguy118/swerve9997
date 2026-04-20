<!-- markdownlint-disable MD007 MD013 MD031 MD032 MD041 -->
<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Methods

### CODE-FUN-001 -- Method Naming Convention

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

### CODE-FUN-002 -- Method Discipline

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

   > *Industry note: JPL Rule 4 recommends 60 lines (one printed page).
   > We allow up to 80 because robot periodic methods occasionally
   > contain unavoidable `switch`-case length for state-machine
   > handling. Exceeding 80 triggers PR review. Short methods are
   > still strongly preferred.*

e. Methods **should** not have more than 7 parameters. If more
   parameters are needed, consider a configuration object or builder
   pattern.

f. All private methods **shall** be declared `private`. All methods
   accessible only within the package **should** use package-private
   (no modifier) or `protected` as appropriate.

### CODE-FUN-003 -- Utility Methods

a. Shared logic **shall** be extracted into utility methods rather
   than duplicated. Common utilities belong in `com.team271.lib`.

b. Utility classes **shall** be declared `final` with a `private`
   constructor to prevent instantiation.

### CODE-FUN-004 -- Robot Lifecycle Contract

> **Anchor:** This rule codifies the lifecycle ordering underpinning
> the desired-to-actual state pattern in
> [ADR-014](planning/adr/ADR-014-desired-to-actual-state-pattern.md).

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

### CODE-FUN-005 -- State Machine Pattern

> **Anchor:** See
> [ADR-014](planning/adr/ADR-014-desired-to-actual-state-pattern.md)
> for the architectural decision, and
> [SDD-subsystem.md](planning/sdd/SDD-subsystem.md) for the library's
> `Subsystem` base-class implementation.
>
> *Industry note: DO-178C (the avionics software certification standard)
> emphasizes deterministic, traceable state management. The
> desired-state/actual-state pattern makes every transition explicit and
> auditable -- you can always answer "what state is the robot in and why
> did it get there?" This is the same pattern used in flight control
> software.*

a. **(Robot-project code.)** Subsystems that use state machines
   **shall** maintain two state variables: `mControlState` (current)
   and `mDesiredControlState` (desired). The desired state is set in
   `teleopPeriodic()` or `autonomousPeriodic()`; the actual state is
   applied in `robotPeriodicAfter()`. Library subsystems follow the
   same pattern with non-`m`-prefixed field names — see
   [SDD-subsystem.md](planning/sdd/SDD-subsystem.md).

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

### CODE-FUN-006 -- Defensive Checks

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
