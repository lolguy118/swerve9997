<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard - Methods

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Methods companion to [`Standard.md`](Standard.md). Contains
`CODE-FUN-*` rules covering method naming, single-exit discipline,
recursion, parameter declaration, and defensive checks at method
boundaries.

---

## 4.4 Methods

<a id="code-fun-001"></a>

### CODE-FUN-001 -- Method Naming Convention (Source: Barr, Team271-Lib)

a. Method names **shall** use camelCase (`computeOffset()`,
   `restartScheduler()`).

b. Boolean-returning methods **shall** use `is`, `has`, or `can`
   prefixes: `isReady()`, `isAtMaxRate()`, `hasData()`,
   `canRetry()`.

c. Getter methods **shall** use the `get` prefix: `getTimer()`,
   `getValue()`.

d. Factory methods **should** use the `create` or `of` prefix.

e. Method names **shall** be descriptive of their purpose. Use
   verbs in method names (e.g., `configureLimits()`,
   `runHandshake()`).

<a id="code-fun-002"></a>

### CODE-FUN-002 -- Method Discipline (Source: MISRA Rule 15.5 / 17.2, Power of Ten Rules 1 / 4)

a. Every method **shall** have exactly one `return` statement,
   placed at the end of the method body. Early returns
   (including guard clauses) **shall not** be used. For methods
   that compute a value, assign to a local result variable and
   return it once at the bottom.

   **Permitted exits besides the tail `return`:**

   - `throw` statements for argument validation or error
     propagation (see
     [CODE-GEN-011](Standard-General.md#code-gen-011)).
   - The implicit exit at the closing brace of a `void` method
     whose logic is structured so every path falls through.

   > *Industry note: MISRA Rule 15.5 says "A function should
   > have a single point of exit at the end." Single-exit
   > methods are easier to read (one control-flow graph instead
   > of branching returns), easier to test (one breakpoint
   > covers all paths), and easier to extend with cleanup or
   > logging later without revisiting every return site.
   > (MISRA C:2025 disapplies its single-exit guideline
   > Rule 15.5; single exit is retained here as a project
   > convention.)*

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
   public void onCycle() {
       if (isReady()) {
           // ... main logic
       }
   }

   /* WRONG: early return as guard clause */
   public void onCycle() {
       if (!isReady()) {
           return;
       }
       // ... main logic
   }
   ```

b. Recursion **shall not** be used in any code that executes
   inside a real-time or per-cycle loop. All call graphs
   reachable from such loops **shall** be acyclic. A real-time
   loop cannot accommodate unbounded recursion.

   > *Industry note: Power of Ten Rule 1 prohibits all recursion -
   > direct and indirect. MISRA Rule 17.2 says "functions shall
   > not call themselves, either directly or indirectly." If
   > your function calls itself and the base case has a bug,
   > the call stack grows until the program crashes.*

c. All method parameters **shall** be declared `final`:

   ```java
   public void setLevel(final double argLevel) { ... }
   ```

d. Method bodies **should not** exceed 80 lines of code,
   excluding blank lines and comments. Methods that exceed this
   limit **should** be decomposed into smaller helper methods.

   > *Industry note: Power of Ten Rule 4 recommends 60 lines (one
   > printed page). This standard allows up to 80 because
   > per-cycle methods occasionally contain unavoidable
   > `switch`-case length for state-machine handling.
   > Exceeding 80 triggers PR review. Short methods are still
   > strongly preferred.*

e. Methods **should not** have more than 7 parameters. If more
   parameters are needed, consider a configuration object or
   builder pattern.

f. All private methods **shall** be declared `private`. Methods
   accessible only within the package **should** use
   package-private (no modifier) or `protected` as appropriate.

<a id="code-fun-003"></a>

### CODE-FUN-003 -- Utility Methods (Source: Team271-Lib)

a. Shared logic **shall** be extracted into utility methods
   rather than duplicated. Common utilities belong in the
   project's shared library package (e.g., `com.example.lib`).

b. Utility classes **shall** be declared `final` with a
   `private` constructor to prevent instantiation:

   ```java
   public final class StringUtil {
       private StringUtil() {
           // utility class, no instantiation
       }

       public static String reverse(final String argInput) { ... }
   }
   ```

<a id="code-fun-004"></a>

### CODE-FUN-004 -- Defensive Checks (Source: MISRA Rule 16.4, Barr, CERT Java MET00-J)

**See also:**
[CODE-GEN-005](Standard-General.md#code-gen-005)
(return-value checking - status codes, `Optional`),
[CODE-GEN-012](Standard-General.md#code-gen-012)
(null-safety), and
[CODE-CTL-002](Standard-Control.md#code-ctl-002)
(switch statements - termination, fall-through, arrow syntax).
This rule covers the remaining defensive patterns not handled by
those.

> *Industry note: MISRA Rule 16.4 requires every switch
> statement to have a default label. Even if you think you have
> covered every enum value, someone might add a new value later
> and forget to update the switch. The default case catches
> that.*

a. Every `switch` statement on an enum **shall** include a
   `default` case that reports the unexpected value via the
   project's structured logger:

   ```java
   default:
       logger.error(
           "Unexpected ControlMode: " + mControlMode);
       break;
   ```

b. Public methods that accept object parameters **should**
   validate non-null at entry.

c. Array and list accesses with variable indices **shall**
   include bounds checks when the index comes from external
   input.

d. Method parameters **shall** use the `arg` prefix (per
   [CODE-VAR-001b](Standard-Variables.md#code-var-001))
   as a defensive naming discipline. The prefix prevents
   accidental shadowing of instance fields and makes
   caller-provided values visibly distinct in the method body,
   reducing the risk of silent bugs where a typo refers to
   `fieldName` instead of `argFieldName`.

e. Public methods that accept numeric parameters with a bounded
   valid range (voltages, duty cycles, percentages, angles
   within a finite sector, etc.) **shall** enforce the range
   at entry. Enforcement **shall** be one of:

   - Clamp to the valid range (when a caller error should be
     absorbed silently so the system keeps running), or
   - Throw `IllegalArgumentException` with a descriptive
     message (when a caller error indicates a bug that must
     surface).

   The choice **shall** be documented in the method's JavaDoc
   so callers know which behavior to expect.

   > *Industry note: CERT MET00-J requires bounded-input
   > validation at API boundaries. Without it, an out-of-range
   > value propagates deep into the callee before surfacing
   > (if it ever does).*

   ```java
   /* CORRECT: clamp and continue */
   public void setDutyCycle(final double argDutyCycle) {
       final double clamped = clampDouble(argDutyCycle, -1.0, 1.0);
       mOutput.set(clamped);
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
