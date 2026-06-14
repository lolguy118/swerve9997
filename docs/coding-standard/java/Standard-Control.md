<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard — Control Structures

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Control structures companion to [`Standard.md`](Standard.md).
Contains `CODE-CTL-*` rules covering `if`/`else`, `switch`,
loops, array access, logging arguments, and logical-operator
short-circuit safety.

---

## 4.6 Control Structures

<a id="code-ctl-001"></a>

### CODE-CTL-001 -- If-Else Statements (Source: MISRA Rule 13.4 / 15.7, Barr)

> *Industry note: MISRA Rule 13.4 says "the result of an
> assignment operator should not be used." Writing
> `if (x = getValue())` when you meant `if (x == getValue())`
> is an easy typo that compiles fine but does something
> completely different. Banning assignments in conditions
> eliminates this entire class of bug.*

a. Assignments **should not** be made within an `if` or
   `else if` condition expression. In Java, the compiler
   prevents most accidental assignment-in-condition bugs (it
   rejects non-boolean assignments in conditions), but
   `boolean` assignments like `if (found = search())` can
   still slip through.

b. Any `if` statement with an `else if` clause **should** end
   with an `else` clause.

c. Nested if-else statements **should not** be deeper than 4
   levels. Use method extraction or switch statements to
   reduce complexity.

d. The ternary operator (`? :`) **shall not** be used.
   Conditional value selection **shall** use an explicit
   `if`/`else` statement with a result variable (consistent
   with the single-return discipline in
   [CODE-FUN-002a](Standard-Methods.md#code-fun-002)).
   For null-default selection,
   `Optional.ofNullable(x).orElse(default)` is permitted as a
   library-call alternative; it is not the `? :` operator.

   > *Industry note: neither MISRA C nor the Barr Group
   > Embedded C Standard prohibits the conditional operator
   > `?:` -- Barr only governs whitespace around `?` and `:`
   > and uses the ternary in its own example code. Banning it
   > here is a project convention: a ternary compresses a
   > decision into a single expression, making the conditional
   > harder to see at review and harder to set a breakpoint on
   > one branch. Explicit `if`/`else` keeps the control-flow
   > graph linear and debuggable.*

   ```java
   /* WRONG: ternary operator */
   final double scaled =
       mScale != null ? mScale.apply(argInput) : argInput;

   /* CORRECT: explicit if/else with result variable */
   final double scaled;
   if (mScale != null) {
       scaled = mScale.apply(argInput);
   } else {
       scaled = argInput;
   }

   /* CORRECT: library-call alternative for null-default */
   final double scaled = Optional.ofNullable(mScale)
       .map(s -> s.apply(argInput))
       .orElse(argInput);
   ```

<a id="code-ctl-002"></a>

### CODE-CTL-002 -- Switch Statements (Source: MISRA Rule 16.3 / 16.4)

a. All `switch` statements **shall** contain a `default` case
   (see also
   [CODE-FUN-004a](Standard-Methods.md#code-fun-004)).

b. Fall-through between `case` labels **shall not** occur
   without an explicit `// fall through` comment.

c. Switch expressions using the `->` syntax are permitted and
   preferred for value computation (the compiler verifies
   exhaustiveness for switch expressions over enums,
   eliminating the need for a `default` case in that form):

   ```java
   double targetValue = switch (mMode) {
       case MODE_A -> Config.MODE_A_VALUE;
       case MODE_B -> Config.MODE_B_VALUE;
   };
   ```

d. Traditional `switch` with `break` **shall** be used for
   side-effectful cases (state transitions, command emission).
   Every non-`default` case **shall** terminate with `break`.
   `return` **shall not** be used to terminate a case (the
   single-return rule in
   [CODE-FUN-002a](Standard-Methods.md#code-fun-002)
   places the method's only `return` at the tail, after the
   `switch`). `throw` is permitted for exception-based exit
   (same rule). Intentional fall-through follows
   CODE-CTL-002b — it **shall** be marked with a
   `// fall through` comment above the next case label.

   > *Industry note: MISRA Rule 16.3 requires every
   > switch-clause to be appropriately terminated — by a
   > break, continue, goto, return, or a _Noreturn-qualified
   > function call — so control does not silently fall through.
   > Rule 16.3 itself permits `return` as a terminator; this
   > standard is stricter and prohibits `return` inside a case
   > (clause d) to keep the method's only `return` at the tail
   > per CODE-FUN-002a.
   > Unintentional fall-through has caused real security
   > vulnerabilities (the `goto fail` / CVE-2014-0092 family
   > of bugs). Combining explicit `break` with the
   > single-return rule also means the control-flow graph of
   > a method is always linear at the top level — easier to
   > read, test, and extend.*

   ```java
   /* CORRECT: every case terminated with break */
   switch (state) {
       case IDLE:
           stop();
           break;
       case RUN:
           applyLevel();
           break;
       default:
           logger.error("Unexpected state: " + state);
           break;
   }

   /* CORRECT: intentional fall-through, commented */
   switch (mode) {
       case WARMUP:
           primeBuffer();
           // fall through
       case READY:
           enableIntake();
           break;
       default:
           break;
   }

   /* WRONG: return inside a case (violates CODE-FUN-002a) */
   switch (state) {
       case IDLE:
           return 0.0;
       // ...
   }

   /* WRONG: unmarked fall-through */
   switch (mode) {
       case WARMUP:
           primeBuffer();
       case READY:
           enableIntake();
           break;
   }
   ```

<a id="code-ctl-003"></a>

### CODE-CTL-003 -- Loops (Source: Power of Ten Rule 2, MISRA Rule 14.2)

a. Magic numbers **shall not** be used in loop bounds. Use
   named constants.

b. Loop variables in `for` statements **shall not** be modified
   inside the loop body. If the iteration pattern requires
   body modification, use a `while` loop instead.

c. All loops inside a real-time or per-cycle method **shall**
   have a provable upper bound and **shall** terminate within
   one cycle. Unbounded loops (e.g., `while (true)`) **shall
   not** appear in any code reachable from a per-cycle method.

   > *Industry note: Power of Ten Rule 2 says "all loops must have a
   > fixed upper bound" that a tool can verify statically. If
   > a loop can run forever, the real-time loop stalls and the
   > system stops responding.*

d. Enhanced for-each loops are preferred when iterating
   collections:

   ```java
   for (Module module : mModules) {
       module.update();
   }
   ```

<a id="code-ctl-004"></a>

### CODE-CTL-004 -- Array and List Access (Source: MISRA Rule 13.2)

a. Array index expressions **shall not** contain side effects.
   This pattern is uncommon in Java but can appear in buffer
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

<a id="code-ctl-005"></a>

### CODE-CTL-005 -- Side-Effect-Free Logging (Source: Team271-Lib — applies MISRA Rule 13.5's side-effect discipline to logging and assertion arguments)

a. Arguments to the project's logging / event-reporting calls
   **shall not** contain side effects. Compute values before
   passing them to the logger.

b. Arguments to assertion methods **shall** be
   side-effect-free.

<a id="code-ctl-006"></a>

### CODE-CTL-006 -- Logical Operator Short-Circuit (Source: MISRA Rule 13.5)

> *Industry note: MISRA Rule 13.5 says "the right hand operand
> of a logical && or || operator shall not contain persistent
> side effects." Because Java short-circuits these operators,
> the right side might not execute at all. If it has a side
> effect (like modifying a variable), your code behaves
> differently depending on the left side's value — a subtle
> and hard-to-debug inconsistency.*

a. The right-hand operand of `&&` and `||` operators **should
   not** contain side effects (method calls that modify
   state). Short-circuit evaluation may skip the right
   operand:

   ```java
   /* WRONG: resetSensor() may not execute if path is null */
   if ((path != null) && (resetSensor(path) == Status.OK))

   /* CORRECT: separate the side effect */
   if (path != null) {
       Status result = resetSensor(path);
       if (result == Status.OK) {
           // ...
       }
   }
   ```

---
