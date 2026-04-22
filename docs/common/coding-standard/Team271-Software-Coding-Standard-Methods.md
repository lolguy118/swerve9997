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

a. Every method **shall** have exactly one `return` statement,
   placed at the end of the method body. Early returns (including
   guard clauses) **shall not** be used. For methods that compute a
   value, assign to a local result variable and return it once at
   the bottom.

   **Permitted exits besides the tail `return`:**

   - `throw` statements for argument validation or error
     propagation (see
     [CODE-GEN-011](Team271-Software-Coding-Standard-General.md#code-gen-011----exception-handling)).
   - The implicit exit at the closing brace of a `void` method
     whose logic is structured so every path falls through.

   > *Industry note: MISRA Rule 15.5 says "A function should have a
   > single point of exit at the end." Single-exit methods are 
   > easier to read (one control-flow graph instead of branching
   > returns), easier to test (one breakpoint covers all paths),
   > and easier to extend with cleanup or logging later without
   > revisiting every return site.*

   ```java
   /* CORRECT: value-returning method, single return at end */
   public double computeDutyCycle(final double argInput) {
       double result = 0.0;
       if (isReady() && isWithinBounds(argInput)) {
           result = clamp(argInput * kScale);
       }
       return result;
   }

   /* CORRECT: void method, main logic inside the guard */
   public void teleopPeriodic() {
       if (isZeroed()) {
           // ... main logic
       }
   }

   /* WRONG: early return as guard clause */
   public void teleopPeriodic() {
       if (!isZeroed()) {
           return;
       }
       // ... main logic
   }
   ```

b. Recursion **shall** not be used in any code that executes during
   periodic methods. All call graphs reachable from periodic methods
   **shall** be acyclic. The real-time loop cannot accommodate
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
   than duplicated. Common utilities belong in `com.example.lib`.

b. Utility classes **shall** be declared `final` with a `private`
   constructor to prevent instantiation.

### CODE-FUN-006 -- Defensive Checks

**See also:**
[CODE-GEN-005](Team271-Software-Coding-Standard-General.md#code-gen-005----return-value-checking)
(return-value checking — `StatusCode`, `Optional`, etc.),
[CODE-GEN-012](Team271-Software-Coding-Standard-General.md#code-gen-012----null-safety)
(null-safety), and
[CODE-CTL-002](Team271-Software-Coding-Standard-Control.md#code-ctl-002----switch-statements)
(switch statements — termination, fall-through, arrow syntax).
This rule covers the remaining defensive patterns not handled by
those.

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

d. Method parameters **shall** use the `arg` prefix (per
   [CODE-VAR-001b](Team271-Software-Coding-Standard-Variables.md#code-var-001----variable-naming-convention))
   as a defensive naming discipline. The prefix prevents accidental
   shadowing of instance fields and makes caller-provided values
   visibly distinct in the method body, reducing the risk of silent
   bugs where a typo refers to `fieldName` instead of
   `argFieldName`.

e. Public methods that accept numeric parameters with a bounded
   valid range (voltages, duty cycles, percentages, angles within
   a finite sector, etc.) **shall** enforce the range at entry.
   Enforcement **shall** be one of:

   - Clamp to the valid range (when a caller error should be
     absorbed silently so the robot keeps running), or
   - Throw `IllegalArgumentException` with a descriptive message
     (when a caller error indicates a bug that must surface).

   The choice **shall** be documented in the method's JavaDoc so
   callers know which behavior to expect.

   > *Industry note: CERT NUM00-J and the Barr Group standard both
   > require bounded-input validation at API boundaries. Without
   > it, an out-of-range value propagates deep into motor firmware
   > before surfacing (if it ever does).*

   ```java
   /* CORRECT: clamp and continue */
   public void setDutyCycle(final double argDutyCycle) {
       final double clamped = Util.limit(argDutyCycle, -1.0, 1.0);
       mTransmission.set(clamped);
   }

   /* CORRECT: throw on invalid input */
   public void configRatio(final double argRatio) {
       if (argRatio <= 0.0) {
           throw new IllegalArgumentException(
               "argRatio must be > 0, got " + argRatio);
       }
       mRatio = argRatio;
   }
   ```

---
