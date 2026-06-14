<!-- markdownlint-disable MD007 MD032 -->
# Team271-Lib Java Coding Standard — Variables

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Variables companion to [`Standard.md`](Standard.md). Contains
`CODE-VAR-*` rules covering variable naming, initialization,
type choice, enum conventions, named constants, magic-number
prohibition, and arithmetic-safety rules.

---

## 4.5 Variables

<a id="code-var-001"></a>

### CODE-VAR-001 -- Variable Naming Convention (Source: Barr, Team271-Lib)

a. Instance fields (non-static, non-final) **shall** use the
   `m` prefix. With a letter-only prefix such as
   `m`, capitalize the first word after the prefix; with a
   separator prefix such as `m_`, or with no prefix at all, keep
   plain camelCase:

   | Resolved prefix | Field declaration |
   | --------------- | ----------------- |
   | `m` | `private ControlMode mControlMode = ControlMode.IDLE;` |
   | `m_` | `private ControlMode m_controlMode = ControlMode.IDLE;` |
   | (none) | `private ControlMode controlMode = ControlMode.IDLE;` |

   This standard's instance-field prefix is `m`,
   set when the coding-standards overlay is applied. See
   [Appendix I](Standard-Appendices.md#appendix-i-naming-convention-quick-reference).

b. Method parameters **shall** use the `arg` prefix followed by
   camelCase:

   ```java
   public void setLevel(final double argLevel) { ... }
   public void configure(final Config argConfig) { ... }
   ```

c. Local variables **shall** use camelCase without prefix.
   Short-lived temporary variables **may** use the `tmp`
   prefix:

   ```java
   double tmpResult = mScale * MAX_VALUE;
   ```

d. Static final constants **shall** use one of two naming
   conventions based on their purpose:

   - **UPPER_SNAKE_CASE** for operational and fixed constants
     (port names, physical measurements, periods, duty-cycle
     limits):

     ```java
     public static final String BUS_NAME_PRIMARY = "BusA";
     public static final double WHEEL_RADIUS_M = 0.0508;
     private static final int TELEMETRY_PERIOD = 5;
     ```

   - **`k` prefix + camelCase** (`kCamelCase`) for tunable
     hardware-configuration constants (PID gains, current
     limits, timing thresholds, target setpoints):

     ```java
     public static final double kTranslationKp = 2.0;
     public static final int kCurrentStatorLimit = 120;
     public static final double kHomingTimeoutSec = 4.0;
     ```

   The `k` prefix signals "this value is likely to be tuned
   during testing" and groups tunable constants visually in
   autocomplete.

e. Boolean variables and methods **shall** use positive names
   (`isReady`, not `isNotReady`; `mEnabled`, not `mDisabled`).

f. No variable name **shall** be a single character except `i`,
   `j`, `k` in simple loop counters. Descriptive names are
   preferred even for loop variables (e.g., `channelIndex`,
   `moduleIndex`).

g. No variable **shall** shadow a variable from an enclosing
   scope.

<a id="code-var-002"></a>

### CODE-VAR-002 -- Variable Initialization (Source: MISRA Rule 9.1, Barr)

> *Industry note: MISRA Rule 9.1 (a Mandatory rule — their
> strongest category) says variables shall not be read before
> they are set. Reading an uninitialized variable is one of the
> oldest and most common bugs in software. Initializing at
> declaration eliminates the possibility.*

a. All instance fields **shall** be initialized at the point of
   declaration or in the constructor:

   ```java
   private ControlMode mControlMode = ControlMode.IDLE;
   private double mLevel = 0.0;
   private boolean mIsReady = false;
   ```

b. Local variables **shall** be initialized before use. Prefer
   initialization at the point of declaration.

<a id="code-var-003"></a>

### CODE-VAR-003 -- Type Conventions (Source: Team271-Lib)

a. Use `int` for general-purpose integers. Java `int` is
   always 32-bit signed.

b. Use `double` for all floating-point values. This is the
   standard type for most numerical APIs in Java. Avoid
   `float` unless required by a specific API.

c. Use `boolean` for true/false values. Unlike C/C++, Java
   enforces this at the compiler level — `if (1)` or
   `if (count)` will not compile. Use explicit comparisons:
   `if (count > 0)` instead.

d. Use `long` only for timestamps, large counters, or when
   required by an API.

e. Use enums for state machines and mode selection, never
   integer constants.

<a id="code-var-004"></a>

### CODE-VAR-004 -- Floating-Point Comparison (Source: Team271-Lib; CERT Java NUM07-J / NUM08-J for NaN/Infinity)

a. Floating-point values **shall not** be compared using `==`
   or `!=`. Use epsilon-based comparison. While comparing
   against an assigned literal zero (e.g., `level == 0.0`) is
   technically safe in IEEE 754, this standard uses epsilon
   comparison uniformly for consistency:

   ```java
   /* WRONG */
   if (level == 0.0) { ... }

   /* CORRECT */
   private static final double EPSILON = 1e-6;
   if (Math.abs(level) < EPSILON) { ... }
   ```

b. Be aware of `Double.NaN`, `Double.POSITIVE_INFINITY`, and
   `Double.NEGATIVE_INFINITY`. Check for these when processing
   sensor data that may produce invalid readings.

<a id="code-var-005"></a>

### CODE-VAR-005 -- Enum Conventions (Source: Team271-Lib)

a. Enum values **shall** use UPPER_SNAKE_CASE:

   ```java
   public enum ControlMode {
       IDLE,
       RUNNING
   }
   ```

b. Enums used for state machines **shall** include a brief
   comment or JavaDoc when the value's name is not
   self-explanatory (see
   [CODE-COM-002b](Standard-Comments.md#code-com-002)):

   ```java
   public enum ControlMode {
       IDLE,
       INDEX,
       COAST_DOWN, // brief neutral coast before reversing
       REVERSE
   }
   ```

c. The `IDLE` state **should** be listed first in state-machine
   enums.

d. Enum initialization: if explicit values are needed (rare in
   Java), either initialize only the first or initialize all.

<a id="code-var-006"></a>

### CODE-VAR-006 -- Named Constants and Physical Units (Source: Team271-Lib)

a. Physical units **shall** be documented in the constant name
   or in a comment:

   ```java
   public static final double WHEEL_RADIUS_M = 0.0508; // meters
   public static final double kTimeoutSec = 2.0;
   public static final double MAX_SPEED_MPS = 4.5; // meters per second
   ```

   Project-domain unit-suffix conventions (the exact
   abbreviations used for the project's physical quantities)
   belong in the project's coding-standard supplement —
   different domains use different short forms for the same
   unit, and the suffix list is short-lived information that
   shouldn't fork from the project that owns the units.

b. Hexadecimal constants **shall** use a lowercase `0x` prefix:
   `0xFF`.

<a id="code-var-007"></a>

### CODE-VAR-007 -- Identifier Clarity (Source: MISRA Dir 4.5)

a. Identifiers **shall not** differ only by:

   - Case: `statusFlag` vs `StatusFlag`
   - Visually similar characters: `l` (lowercase L) vs `1`
     (digit), `O` (uppercase O) vs `0` (digit zero)

<a id="code-var-008"></a>

### CODE-VAR-008 -- Magic Number Prohibition (Source: Barr)

a. Numeric constants other than `0`, `1`, `-1`, `0.0`, `1.0`,
   and `2.0` **shall** be replaced by named constants when
   used in executable code. `2.0` is permitted to support
   bisection and midpoint formulas (e.g., `(a + b) / 2.0`,
   `diameter / 2.0`) where naming the divisor would reduce
   rather than add readability; all other literals require a
   named constant.

b. All tunable values (limits, timing windows, thresholds)
   **shall** be defined as named constants — either in the
   project's shared constants artifact (see
   [CODE-MAF-003](Standard-Modules.md#code-maf-003))
   or as `private static final` fields on the class that uses
   them.

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

<a id="code-var-009"></a>

### CODE-VAR-009 -- Shift Operator Constraints (Source: CERT Java NUM14-J)

a. Shift amounts for `<<` and `>>` **shall** be non-negative
   and less than 32 (for `int`) or 64 (for `long`). Shifting
   by a negative number or by more than the bit width
   produces unpredictable results.

<a id="code-var-010"></a>

### CODE-VAR-010 -- Division by Zero Prevention (Source: CERT Java NUM02-J)

a. Before any division (`/`) or remainder (`%`) operation
   where the divisor is a variable, the divisor **shall** be
   checked for zero:

   ```java
   double result;
   if (divisor != 0) {
       result = dividend / divisor;
   } else {
       result = 0; // or appropriate default
       logger.error("Division by zero in computeRatio");
   }
   ```

---
