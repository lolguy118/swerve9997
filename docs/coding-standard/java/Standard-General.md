<!-- markdownlint-disable MD007 MD031 MD032 -->
# Team271-Lib Java Coding Standard — General

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

General companion to [`Standard.md`](Standard.md). Contains
`CODE-GEN-*` rules covering keyword usage, annotations, prohibited
APIs, type safety, exception handling, GC pressure, and
concurrency.

---

## 4.1 General

<a id="code-gen-001"></a>

### CODE-GEN-001 -- Common Abbreviations (Source: Barr)

a. Abbreviations and acronyms **should** be avoided unless their
   meanings are widely understood. See
   [Appendix A](Standard-Appendices.md#appendix-a-approved-identifier-abbreviations)
   for the approved list of short-form tokens permitted in
   identifiers.

b. Domain-specific abbreviations established by the project's
   problem domain (hardware bus names, protocol names, common
   physical quantities) are acceptable without expansion in code.
   They **should** be defined in class-level JavaDoc on first use
   in documentation. The project's own coding-standard
   supplement enumerates the accepted set.

<a id="code-gen-002"></a>

### CODE-GEN-002 -- APIs and Methods to Avoid (Source: Team271-Lib)

a. The following methods and patterns **shall not** be used in
   production code:

   | Prohibited | Reason |
   | ---------- | ------ |
   | `System.exit()` | Terminates the JVM; the application stops responding |
   | `Runtime.halt()` | Same as above, without shutdown hooks |
   | `Thread.sleep()` | Blocks the calling thread; use a scheduled callback or timer abstraction instead |
   | `Thread.stop()` | Deprecated; unsafe thread termination |
   | `System.gc()` | Unpredictable GC pauses in real-time / latency-sensitive code |
   | `Object.finalize()` | Deprecated; unreliable cleanup. Use try-with-resources (CODE-GEN-014) |
   | `System.out.println()` | Use the project's structured logger / event-reporting facility |
   | `System.err.println()` | Same as `System.out.println()` |
   | `System.currentTimeMillis()` | Not monotonic; affected by wall-clock adjustments. Use `System.nanoTime()` or a project-supplied monotonic-clock API |
   | Raw `new Thread()` | Use the project's executor / scheduled-callback abstraction so threads are owned by a known pool with a known lifecycle |
   | `Math.random()` | Uses a synchronized shared `Random`; non-deterministic without a logged seed, which breaks any replay / determinism story. Use `ThreadLocalRandom.current()` or a seeded `Random` if randomness is actually required |

b. Reflection and dynamic class loading **shall not** be used in
   code that runs in a tight real-time or latency-sensitive loop.
   They defeat ahead-of-time optimization and can trigger
   unpredictable class-loading pauses. Initialization-time
   reflection is permitted (e.g., framework metadata registration
   during application startup).

<a id="code-gen-003"></a>

### CODE-GEN-003 -- Keywords to Use Frequently (Source: Barr)

a. The `private` access modifier **shall** be used for all fields
   and methods that do not need to be visible outside the class.
   Use `protected` only when subclass access is genuinely
   required.

b. The `final` keyword **shall** be used:

   - On all method parameters:

     ```java
     public void setLevel(final int argLevel)
     ```

   - On all fields that are set once (at declaration or in the
     constructor):

     ```java
     private final ScheduledExecutorService mScheduler;
     private final Watchdog mWatchdog = new Watchdog();
     ```

   - On local variables that are not reassigned (encouraged but
     not mandatory).
   - On utility and constants classes:

     ```java
     public final class StringUtil { ... }
     ```

   See
   [Appendix C](Standard-Appendices.md#appendix-c-final-keyword-usage-guide)
   for the complete `final` usage table.

   > *Industry note: Power of Ten Rule 6 says "data objects must be
   > declared at the smallest possible level of scope." CERT
   > Java OBJ01-J says "limit accessibility of fields." Both
   > exist because exposing internal state makes bugs harder to
   > find and easier to introduce.*

c. The `static` keyword **shall** be used for:

   - Constants: `public static final double WHEEL_RADIUS_M = 0.0508;`
   - Single-instance references held at class level (when the
     project's pattern defines such a usage).
   - Utility methods that do not depend on instance state.

d. The `@Override` annotation **shall** be used on every method
   that overrides a superclass method or implements an interface
   method. Spotless does not add missing `@Override`; Error Prone's
   `MissingOverride` check is the recommended automated
   enforcer.

<a id="code-gen-004"></a>

### CODE-GEN-004 -- Garbage Collection Pressure Minimization (Source: Power of Ten Rule 3, MISRA Dir 4.12)

> *Industry note: Power of Ten Rule 3 says "do not use dynamic memory
> allocation after initialization." MISRA Directive 4.12 says
> "dynamic memory allocation shall not be used." The reason
> transfers to Java: if allocations in a tight loop trigger a GC
> pause that exceeds the loop budget, the loop stalls and the
> system loses responsiveness.*

a. Object allocations **shall** be minimized in code that runs
   in a tight real-time or per-cycle loop. Excessive allocations
   cause GC pauses that stall such loops.

b. Objects that are used repeatedly **shall** be pre-allocated
   at initialization time or at field declaration and reused:

   ```java
   /* CORRECT: Watchdog pre-allocated at field declaration */
   private final Watchdog mWatchdog = new Watchdog();

   /* WRONG: Watchdog allocated in a per-cycle method */
   public void onCycle() {
       Watchdog w = new Watchdog(); // BAD: allocates every cycle
   }
   ```

c. Command, request, or message objects sent on every cycle
   **shall** be reused. Declare them as `private final` fields
   and mutate their parameters on each use rather than
   constructing new instances per cycle.

d. String concatenation in per-cycle methods **should** be
   avoided. Pre-compute constant keys once and reuse them:

   ```java
   /* WRONG: new String every cycle */
   logger.record("Service/" + "Speed", speed);

   /* CORRECT: constant key */
   private static final String LOG_KEY_SPEED = "Service/Speed";
   logger.record(LOG_KEY_SPEED, speed);
   ```

e. Autoboxing (e.g., `int` to `Integer`) in loops **shall** be
   avoided.

f. Repeatedly emitted notification or event objects **should** be
   pre-allocated when their payload is structurally constant
   between emissions.

g. Collections that grow dynamically (`ArrayList`, `HashMap`)
   **should** be pre-sized at initialization if their maximum
   size is known.

h. Streams and lambda chains that allocate intermediate
   collections (e.g., `stream().filter(...).collect(toList())`,
   `.toArray()`) **shall not** be used in per-cycle methods. They
   are permitted in initialization code where one-time allocation
   cost is not a concern.

<a id="code-gen-005"></a>

### CODE-GEN-005 -- Return Value Checking (Source: Power of Ten Rule 7, MISRA Rule 17.7, CERT Java EXP00-J)

> *Industry note: Power of Ten Rule 7: "the return value of non-void
> functions must be checked." MISRA Rule 17.7: "the value
> returned by a function having non-void return type shall be
> used." CERT Java EXP00-J: "do not ignore values returned by
> methods." When a configuration call fails silently because
> nobody checked the return code, you get mysterious behavior
> later, at the worst time.*

a. The return value of methods that indicate success or failure
   **shall** be checked. In particular:

   - Status-code returns from configuration or RPC calls
     **shall** be checked, at minimum during initialization.
   - `Optional` return values **shall** be checked with
     `isPresent()` or `ifPresent()` before accessing the value.

b. When a return value is intentionally discarded, a comment
   **shall** explain why:

   ```java
   /* Status intentionally discarded: best-effort config at startup;
    * transient failures self-correct on the next cycle. */
   mClient.applyConfig(config);
   ```

<a id="code-gen-006"></a>

### CODE-GEN-006 -- Type Safety (Source: Team271-Lib)

a. Raw types **shall not** be used. All generic types **shall**
   include their type parameters:

   ```java
   /* WRONG */
   List items = new ArrayList();

   /* CORRECT */
   List<String> items = new ArrayList<>();
   ```

b. Casts **should** be avoided. When unavoidable, they **shall**
   be accompanied by a comment explaining why the cast is safe.

c. `instanceof` checks followed by casts **should** use pattern
   matching. Pattern matching fuses the type check and the cast,
   eliminates the redundant type name, and scopes the bound
   variable to the branch where it is valid:

   ```java
   /* AVOID -- separate cast, redundant type naming, typo risk */
   if (obj instanceof String) {
       String s = (String) obj;
       // use s
   }

   /* PREFER -- pattern matching */
   if (obj instanceof String s) {
       // use s directly
   }
   ```

<a id="code-gen-007"></a>

### CODE-GEN-007 -- Warning-Free Compilation (Source: Power of Ten Rule 10)

> *Industry note: Power of Ten Rule 10 requires "all code must be
> compiled from the first day of development with all compiler
> warnings enabled at the most pedantic setting" with zero
> warnings. Warnings are the compiler telling you something
> looks wrong — ignoring them is ignoring free bug detection.*

a. All code **shall** compile without errors.

b. All code **shall** compile without warnings. Compiler
   warnings indicate potential defects and **shall** be resolved
   before merging.

c. Spotless **shall** pass without modifications. Run
   `./gradlew spotlessCheck` before committing.

d. `@SuppressWarnings` **shall** only be used with a comment
   documenting the justification:

   ```java
   @SuppressWarnings("unchecked") // Safe: vendor API returns raw value
   ```

e. The `@Deprecated` annotation **should** include a
   `@deprecated` JavaDoc tag explaining the replacement.

f. Compilation **shall** be invoked with all warning categories
   enabled. For `javac`, this means `-Xlint:all`; default
   compiler settings suppress many categories and render (b)
   unverifiable. Warnings **should** be failed at the build
   level (via `-Werror` or an equivalent static-analyser gate)
   so warning drift cannot accumulate silently between reviews.
   Project-specific enforcer configuration (Error Prone,
   SpotBugs, Checkstyle) is recorded in
   [`Standard-Compliance.md`](Standard-Compliance.md).

<a id="code-gen-008"></a>

### CODE-GEN-008 -- External Input Validation (Source: MISRA Dir 4.14)

External inputs **shall** be validated before use. "External"
includes operator inputs, configuration / option-selection
values, vendor-library return codes, and any data received from
out-of-process sources.

a. Operator-input validation — deadbands, threshold checks,
   shape filtering — **shall** be applied consistently across
   the codebase, typically by routing all raw input through a
   single project-defined input-handling class.

b. Mode-selection and option-selection values (auto-complete
   menus, configuration dropdowns, RPC enum parameters)
   **shall** have a `default` case or null-handling path for
   unexpected values.

c. Return codes from vendor libraries **shall** be checked at
   the point of call. Project-specific patterns for the chosen
   vendor libraries are recorded in the consuming project's own
   docs.

<a id="code-gen-009"></a>

### CODE-GEN-009 -- Public Method Argument Validation (Source: CERT Java MET00-J)

a. Public methods that accept object parameters **should**
   validate that arguments are non-null before use, either
   through explicit null checks or through
   `Objects.requireNonNull()`.

<a id="code-gen-010"></a>

### CODE-GEN-010 -- Runtime Failure Minimization (Source: MISRA Dir 4.1)

a. Code **shall** be written to minimize runtime exceptions.
   The following categories **shall** be addressed:

   - **NullPointerException:** Check references before
     dereference when the value may be null (e.g., return
     values from external APIs, optional fields).
   - **ArrayIndexOutOfBoundsException:** Validate array indices
     against array length before access.
   - **IllegalStateException:** State machines **shall** handle
     all enum values in switch statements, including a `default`
     case.
   - **ArithmeticException:** Division operations **shall**
     check for zero divisors when the divisor comes from a
     variable. See
     [CODE-VAR-010](Standard-Variables.md#code-var-010).

<a id="code-gen-011"></a>

### CODE-GEN-011 -- Exception Handling (Source: CERT Java ERR00-J / ERR08-J)

> *Industry note: CERT Java ERR00-J says "do not suppress or
> ignore checked exceptions." Swallowing an exception hides the
> problem — the resource might not be initialized, the file
> might not have loaded, and you will not know until the
> failure surfaces in production.*

a. Methods that run on every cycle of a real-time loop
   **shall not** throw exceptions. All exceptions **shall** be
   caught and reported through the project's error-reporting
   facility.

b. `catch (Throwable t)` and `catch (Exception e)` **shall not**
   be used broadly. Catch the most specific exception type
   possible. Exception: `catch (Throwable t)` is permitted only
   in top-level lifecycle methods where preventing a crash is
   critical and the catch site re-reports the failure through a
   structured channel.

c. Exceptions **shall not** be used for normal control flow.
   Use return values, enums, or state variables instead.

d. When calling external APIs that may throw (e.g., file I/O,
   network I/O), the call **shall** be wrapped in a try-catch
   that logs the error and provides a safe fallback.

<a id="code-gen-012"></a>

### CODE-GEN-012 -- Null Safety (Source: Team271-Lib)

a. Public methods **should** document their null behavior in
   JavaDoc (`@param` descriptions should state whether null is
   permitted).

b. When null is a valid state (e.g., "no value loaded"), prefer
   `Optional<T>` over nullable references for return values.

c. When null indicates an error, prefer throwing
   `IllegalStateException` with a descriptive message over
   returning null.

<a id="code-gen-013"></a>

### CODE-GEN-013 -- Object Equality (Source: CERT Java EXP50-J / MET09-J)

a. Object comparison **shall** use `.equals()`, not `==`,
   unless checking for null or intentional reference identity:

   ```java
   /* WRONG: compares references, not values */
   if (name == "Hub") { ... }

   /* CORRECT */
   if (name.equals("Hub")) { ... }
   if ("Hub".equals(name)) { ... }  // null-safe idiom
   ```

b. Enum comparison with `==` is acceptable and preferred (enums
   are singletons in Java):

   ```java
   if (mControlMode == ControlMode.IDLE) { ... } // OK
   ```

c. If a class overrides `equals()`, it **shall** also override
   `hashCode()` to maintain the general contract. Failing to do
   so causes broken behaviour in `HashMap`, `HashSet`, and
   other hash-based collections.

<a id="code-gen-014"></a>

### CODE-GEN-014 -- Resource Management (Source: CERT Java FIO04-J)

a. Code that opens resources (files, streams, network
   connections) **shall** use try-with-resources to guarantee
   cleanup:

   ```java
   try (BufferedReader reader = Files.newBufferedReader(path)) {
       // ...
   } catch (IOException e) {
       logger.error("Failed to read: " + e.getMessage(), e);
   }
   ```

<a id="code-gen-015"></a>

### CODE-GEN-015 -- Mutable Static Fields (Source: CERT Java OBJ10-J)

a. Mutable `static` fields **shall** be avoided. Shared mutable
   state **should** be passed explicitly through constructors
   or method parameters. Project-specific exceptions (e.g.,
   intentional singleton instance references) are documented in
   the consuming project's own coding standard.

<a id="code-gen-016"></a>

### CODE-GEN-016 -- Concurrency Keywords (Source: CERT Java LCK00-J)

> *Industry note: CERT Java LCK00-J says "use private final
> lock objects to synchronize classes that may interact with
> untrusted code," and the broader `java.util.concurrent` design
> philosophy (Goetz et al., *Java Concurrency in Practice*) is
> to prefer higher-level primitives over raw `synchronized` +
> `volatile`. Concurrency bugs are the hardest to reproduce and
> the hardest to debug after the fact. The first answer should
> be to avoid sharing mutable state across threads in the first
> place — communicate through immutable snapshots, not through
> locks.*

a. The concurrency keywords in this rule only apply when a
   field is genuinely shared between a primary thread and a
   background thread (a scheduled callback, a worker pool, a
   vendor-provided refresh thread). If you are not sure whether
   you have cross-thread access, you probably do not, and you
   do not need these keywords.

b. The `volatile` keyword **should** only be used when a field
   is accessed from multiple threads. It guarantees visibility
   of writes across threads but does **not** provide atomicity
   for compound operations (e.g., `count++` is still unsafe on
   a `volatile int`).

c. When cross-thread sharing is required, prefer
   `AtomicReference`, `AtomicInteger`, or the other
   `java.util.concurrent.atomic` classes over `volatile`. These
   provide both visibility and atomic compound operations
   without the ceremony of `synchronized`.

d. `synchronized` blocks **shall** be kept short and **shall
   not** call out to methods that can block (I/O,
   `Thread.sleep`, vendor RPC calls, etc.). A `synchronized`
   block held on the primary thread while a background callback
   contends for the same monitor will stall the real-time loop.

---
