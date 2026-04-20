<!-- markdownlint-disable MD013 -->
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

d. The ternary operator (`? :`) is acceptable for simple value
   selection but **should** be avoided for complex expressions or
   when either branch has side effects.

### CODE-CTL-002 -- Switch Statements

a. All `switch` statements **shall** contain a `default` case
   (CODE-FUN-006a).

b. Fall-through between `case` labels **shall** not occur without
   an explicit `// fall through` comment.

c. Switch expressions using the `->` syntax are permitted and
   preferred for value computation:

   ```java
   double targetRps = switch (mShotMode) {
       case MODE_A -> ExampleSubsystemConstants.MODE_A_VALUE;
       case MODE_B -> ExampleSubsystemConstants.MODE_B_VALUE;
   };
   ```

d. Traditional `switch` with `break` **shall** be used for
   side-effectful cases (motor commands, state transitions).

### CODE-CTL-003 -- Loops

a. Magic numbers **shall** not be used in loop bounds. Use named
   constants.

b. Loop variables in `for` statements **shall** not be modified
   inside the loop body. If the iteration pattern requires body
   modification, use a `while` loop instead.

c. All loops in periodic methods **shall** have a provable upper
   bound and **shall** terminate within the 20ms cycle budget.
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

### CODE-CTL-007 -- Enhanced Switch

a. Switch expressions (`->`) are preferred for value computation
   (no fall-through risk, exhaustive checking by compiler).

b. Traditional `switch` with `:` and `break` is required when cases
   have side effects (motor commands, state assignments, method calls).

c. When switching on an enum, the compiler verifies exhaustiveness
   for switch expressions. A `default` case is still required for
   traditional `switch` statements (CODE-FUN-006a).

### CODE-CTL-008 -- Exception Control Flow

a. Exceptions **shall** not be used for normal control flow. Use
   return values, enums, or `Optional<T>` instead.

b. `catch` blocks **shall** catch the most specific exception type:

   ```java
   /* WRONG */
   catch (Exception e) { ... }

   /* CORRECT */
   catch (IOException e) { ... }
   ```

c. `catch (Throwable t)` **shall** not be used except in top-level
   robot lifecycle methods where preventing a crash is critical.

---
