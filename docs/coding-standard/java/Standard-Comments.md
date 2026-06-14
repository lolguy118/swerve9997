<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard — Comments

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Comments companion to [`Standard.md`](Standard.md). Contains
`CODE-COM-*` rules covering JavaDoc, block comments, inline
comments, and required content for non-obvious code.

---

## 4.7 Comments

<a id="code-com-001"></a>

### CODE-COM-001 -- Comment Formats (Source: MISRA Dir 4.4, Barr)

a. **JavaDoc** (`/** ... */`) **shall** be used for:

   - All public classes
   - All public methods
   - All public constants that are not self-documenting

   JavaDoc uses `@param`, `@return`, `@throws`, `@see`,
   `{@link}`, `{@code}` tags.

b. **Block comments** (`/* ... */`) **shall** be used for section
   separators within classes (see
   [CODE-FMT-004c](Standard-Format.md#code-fmt-004)).

c. **Line comments** (`//`) **shall** be used for inline explanations
   within method bodies.

d. Comments **shall not** be used to disable blocks of code in
   released code. Use version control to track removed code.

   > *Industry note: MISRA Directive 4.4 says "sections of code
   > should not be commented out." Commented-out code rots fast --
   > it stops compiling as the surrounding code changes, and
   > readers cannot tell if it was disabled on purpose or by
   > accident. Git keeps the history; delete the code and move on.*

<a id="code-com-002"></a>

### CODE-COM-002 -- Comment Content (Source: Barr, Team271-Lib)

a. Every class with non-trivial behavior **shall** have a
   class-level JavaDoc describing:

   - The responsibility the class owns
   - Any state machine it implements and the meaning of each state
   - Its collaboration with other classes (who calls it, what it
     depends on)

b. State machine enum values **shall** include a brief comment
   when the state name alone is not self-explanatory:

   ```java
   COAST_DOWN, // brief neutral coast before reversing to avoid current spike
   ```

c. Load-bearing ordering **shall** be documented with a `WARNING`
   comment. Wherever the correctness of the code depends on one
   step happening before another (registration order, publish
   order, subscription order, lifecycle order), call it out:

   ```java
   /*
    * *** MODULE REGISTRATION ORDER IS LOAD-BEARING ***
    * ModuleA MUST be registered before ModuleB so that
    * ModuleA's per-cycle update runs first and publishes the
    * shared state that ModuleB reads in the same cycle.
    */
   ```

d. `TODO:` comments **shall** include the author or a tracking
   issue:

   ```java
   // TODO(jane): Define module hardware-abstraction API
   // TODO: #42 - Implement fallback path
   ```

e. Avoid explaining the obvious. Assume the reader knows Java.
   Focus comments on *why*, not *what*.

f. Magic-looking constants **shall** have a comment explaining
   their derivation:

   ```java
   /* 7.03:1 reduction, 4 in output diameter -> 0.0508 m radius */
   public static final double OUTPUT_RADIUS_M = 0.0508;
   ```

---
