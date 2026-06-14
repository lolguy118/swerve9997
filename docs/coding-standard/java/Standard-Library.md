<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard - Library Design

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Library-design companion to [`Standard.md`](Standard.md). Contains
`CODE-LIB-*` rules governing **reusable library code** - the layer
packaged for consumption by downstream application projects (see the
library-vs-application split in [`Standard.md` §1.2](Standard.md#12-scope)).

These rules apply only to source under the project's dedicated library
package root - the consuming project's coding-standard supplement fixes
the concrete value; `com.example.lib` is used as the placeholder below.
Application-project code uses the *inverse* of several of these patterns
(for example a singleton or global registry for shared services is
acceptable in an application but not in library code); those
application-side rules live in the project's own standard and, for FRC
projects, in the [`../frc/`](../frc/) overlay.

> **Framework bindings live in the project supplement.** Every rule
> below is framework-agnostic. The concrete library facilities that
> satisfy it (the component lifecycle base type, the centralized I/O
> manager, the error/notification facility, the tuning mechanism) are
> named in the consuming project's coding-standard supplement - keeping
> them out of this file is what makes the rules portable across
> projects.

---

## 4.8 Library Design

<a id="code-lib-001"></a>

### CODE-LIB-001 -- Explicit Instantiation (Source: Team271-Lib)

a. Reusable library classes (services, hardware wrappers, controllers,
   and other components intended for downstream consumption) **shall
   not** use the singleton pattern. Instances **shall** be created
   through constructors and supplied to collaborators by dependency
   injection - typically an owner/parent reference passed as a
   constructor parameter.

b. Library classes **shall not** expose a static `getInstance()`
   accessor or store their own instance in a mutable static field.
   A consumer obtains a library instance by constructing it directly
   and, if it wants global access, holding the reference in its own
   application-level registry.

The inverse rule - that an application project **may** use a
singleton/global-registry pattern for its own components - belongs in
the consuming project's standard, which keeps this companion
framework-neutral. See also the mutable-static-field discipline in
[CODE-GEN-015](Standard-General.md#code-gen-015).

<a id="code-lib-002"></a>

### CODE-LIB-002 -- Centralized Resource Access (Source: Team271-Lib)

a. Library code **shall** route access to a shared external resource
   (a hardware bus, an I/O subsystem, a connection pool) through a
   single centralized manager rather than having each component acquire
   or poll the resource ad hoc.

b. The centralized manager **should** perform the shared work once per
   cycle - one batched read/refresh - and hand the cached result to
   components, so per-component code does not repeat the cost.

c. New library components **shall** register their resource needs with
   the manager at construction time rather than reaching the resource
   directly from periodic or hot-path code.

The concrete manager (for FRC projects, a centralized CAN-signal
refresh) is named in the project supplement.

<a id="code-lib-003"></a>

### CODE-LIB-003 -- Separation of Desired and Applied State (Source: Team271-Lib)

a. Library components **shall** separate *desired* state (set by
   callers and decision logic) from *applied* state (pushed to the
   underlying resource). A single setter **shall not** both record
   intent and actuate hardware in one call.

b. Inputs **shall** be read at one defined point in the component's
   cycle and outputs applied at another defined point, so that all
   logic within a cycle observes a consistent snapshot and external
   effects are issued once, in a predictable place.

The cycle phases that bind "read" and "apply" are defined by
[CODE-LIB-006](#code-lib-006); their concrete method names are named in
the project supplement.

<a id="code-lib-004"></a>

### CODE-LIB-004 -- Fault Isolation from the Host Loop (Source: Team271-Lib)

a. A library component's periodic or callback methods **shall not** let
   an exception propagate into the host application's main loop. The
   library **shall** provide a safe-iteration mechanism that invokes
   each component, catches exceptions, and reports them, so one
   component's failure does not stop the others.

b. When library code catches an exception for reporting rather than
   recovery, it **shall** route it to the project's error-reporting
   facility (see [CODE-GEN-011](Standard-General.md#code-gen-011)), not
   `System.err` or a silent swallow.

<a id="code-lib-005"></a>

### CODE-LIB-005 -- Bounded Waits with Fail-Safe (Source: Team271-Lib)

a. Every library operation that waits for a condition (a threshold, a
   target value, an arrival, a completion signal) **shall** be bounded
   by a named timeout constant - never a magic number and never an
   unbounded wait.

b. On timeout, the component **shall** transition to a defined
   fail-safe outcome (stop the output, restore safe defaults, enter an
   idle/safe state) rather than continuing to wait or leaving the
   resource in an indeterminate state.

c. A timeout **shall not** be silent: it **shall** be reported through
   the project's error / operator-notification facility.

d. The timeout and its fail-safe behavior **shall** be documented in
   the component's design description.

This is the library-tier statement of bounded execution; it shares
intent with the runtime-failure-minimization rule
[CODE-GEN-010](Standard-General.md#code-gen-010). FRC robot projects
additionally apply the robot-safety timeout rules supplied by the
[`../frc/`](../frc/) overlay.

<a id="code-lib-006"></a>

### CODE-LIB-006 -- Deterministic Component Lifecycle (Source: Team271-Lib)

a. Library components **shall** implement a documented lifecycle
   contract whose phases run in a fixed order each cycle: initialize,
   read inputs, compute/decide, apply outputs, publish telemetry.

b. External effects (outputs to the resource) **shall** be issued only
   in the apply phase, never in the input-read or compute phases, so
   the order of side effects within a cycle is deterministic.

c. Input reads **shall** happen only in the read phase, so every
   component observes a consistent snapshot for the remainder of the
   cycle.

The concrete method names and the manager that drives them in order are
defined by the framework and named in the project supplement.

<a id="code-lib-007"></a>

### CODE-LIB-007 -- Library Package and Constants Isolation (Source: Team271-Lib)

a. Library source **shall** reside under a dedicated library package
   root (placeholder `com.example.lib`), distinct from any
   application-project package. Application code **shall not** be placed
   under the library root.

b. Library-wide constants **shall** be defined in a library-level
   constants holder at the root of the library package and **shall**
   follow the shared-constants discipline in
   [CODE-MAF-003](Standard-Modules.md#code-maf-003) (single shared
   location, `private` constructor on static-only holders).

c. Library code **shall not** import the consuming application's
   configuration or `Constants` class. Values needed at construction
   time **shall** be supplied through constructor parameters or setter
   calls, preserving the library package's isolation from any single
   consumer.

---
