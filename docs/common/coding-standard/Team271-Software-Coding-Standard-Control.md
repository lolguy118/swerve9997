<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Control Structures

### CODE-CTL-001 -- If-Else Statements

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

d. The ternary operator (`? :`) **shall not** be used. Conditional
   value selection **shall** use an explicit `if`/`else` statement
   with a result variable (consistent with the single-return
   discipline in
   [CODE-FUN-002(a)](Team271-Software-Coding-Standard-Methods.md#code-fun-002----method-discipline)).
   For null-default selection, `Optional.ofNullable(x).orElse(default)`
   is permitted as a library-call alternative; it is not the
   `? :` operator.

   > *Industry note: MISRA-C 2012 Rule 14.1 says "the conditional
   > operator `?:` should not be used." The Barr Group Embedded C
   > Standard discourages it for the same reason: a ternary
   > compresses a decision into a single expression, making the
   > conditional harder to see at review and harder to set a
   > breakpoint on one branch. Explicit `if`/`else` keeps the
   > control-flow graph linear and debuggable.*

   ```java
   /* WRONG: ternary operator */
   final double nativePos =
       mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;

   /* CORRECT: explicit if/else with result variable */
   final double nativePos;
   if (mEncoder != null) {
       nativePos = mEncoder.mechanismToNative(argPosition);
   } else {
       nativePos = argPosition;
   }

   /* CORRECT: library-call alternative for null-default */
   final double nativePos = Optional.ofNullable(mEncoder)
       .map(e -> e.mechanismToNative(argPosition))
       .orElse(argPosition);
   ```

### CODE-CTL-002 -- Switch Statements

a. All `switch` statements **shall** contain a `default` case
   (CODE-FUN-004a).

b. Fall-through between `case` labels **shall** not occur without
   an explicit `// fall through` comment.

c. Switch expressions using the `->` syntax are permitted and
   preferred for value computation (the compiler verifies
   exhaustiveness for switch expressions over enums, eliminating
   the need for a `default` case in that form):

   ```java
   double targetRps = switch (mShotMode) {
       case MODE_A -> ExampleSubsystemConstants.MODE_A_VALUE;
       case MODE_B -> ExampleSubsystemConstants.MODE_B_VALUE;
   };
   ```

d. Traditional `switch` with `break` **shall** be used for
   side-effectful cases (motor commands, state transitions).
   Every non-`default` case **shall** terminate with `break`.
   `return` **shall not** be used to terminate a case (the
   single-return rule in
   [CODE-FUN-002(a)](Team271-Software-Coding-Standard-Methods.md#code-fun-002----method-discipline)
   places the method's only `return` at the tail, after the
   `switch`). `throw` is permitted for exception-based exit (same
   rule). Intentional fall-through follows CODE-CTL-002(b) — it
   **shall** be marked with a `// fall through` comment above the
   next case label.

   > *Industry note: MISRA Rule 16.3 says "an unconditional `break`
   > statement shall terminate every switch-clause." Unintentional
   > fall-through has caused real security vulnerabilities
   > (the `goto fail` / CVE-2014-0092 family of bugs). Combining
   > explicit `break` with the single-return rule also means the
   > control-flow graph of a method is always linear at the top
   > level — easier to read, test, and extend.*

   ```java
   /* CORRECT: every case terminated with break */
   switch (state) {
       case IDLE:
           stop();
           break;
       case RUN:
           applySpeed();
           break;
       default:
           DriverStation.reportError(
               "Unexpected state: " + state, false);
           break;
   }

   /* CORRECT: intentional fall-through, commented */
   switch (mode) {
       case WARMUP:
           primeHeater();
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
           primeHeater();
       case READY:
           enableIntake();
           break;
   }
   ```

### CODE-CTL-003 -- Loops

a. Magic numbers **shall** not be used in loop bounds. Use named
   constants.

b. Loop variables in `for` statements **shall** not be modified
   inside the loop body. If the iteration pattern requires body
   modification, use a `while` loop instead.

c. All loops in periodic methods **shall** have a provable upper
   bound and **shall** terminate within one loop cycle.
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

### CODE-CTL-004 -- Array and List Access

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

### CODE-CTL-005 -- Side-Effect-Free Logging

a. Arguments to `Logger.recordOutput()` **shall** not contain side
   effects. Compute values before passing them to the logger.

b. Arguments to assertion methods **shall** be side-effect-free.

### CODE-CTL-006 -- Logical Operator Short-Circuit

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

---
