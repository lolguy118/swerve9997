<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->
<!-- markdownlint-disable-file MD007 MD032 -->
<!-- reason: nested-list indentation in coding-rule examples (MD007);
     tight list spacing inside numbered rules (MD032). -->

## Comments

### CODE-COM-001 -- Comment Formats

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

### CODE-COM-002 -- Comment Content

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
