<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/coding-standard.md in the robot's own repository.
     Replace <PROJECT> placeholders with the project name (season
     year, robot nickname, etc.). Delete any example rules that do
     not apply and add your own. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# <Project> Coding Standard

Document No: <project>-JCS\
Revision: Draft

---

## 1. Introduction

### 1.1 Purpose

Project-specific coding rules for the <Project> robot codebase.
This document extends the inherited standards (common + Team271-Lib)
with rules unique to this robot -- CAN-ID allocations, Globals
naming conventions, controller bindings, telemetry namespacing, and
any deviations from the common standard.

### 1.2 Rule Precedence

Rules apply in the following order of specificity (most specific
wins):

1. **This document** -- `CODE-<PROJECT>-NNN` (project-specific)
2. **Team271-Lib** -- templates and library-notes (library-consumer)
3. **Common** -- `CODE-GEN-*`, `CODE-VAR-*`, `CODE-FMT-*`, and
   other common companions (FRC-Java baseline)

A project rule may override an inherited rule **only if** it
explicitly cites the rule ID in §3 and documents a rationale.

### 1.3 Inherited Standards

Consuming projects typically vendor `docs/common/` and
`docs/team-lib/` from Team271-Lib. Update the paths below to match
your vendoring:

- **Common core:** `<vendored-path>/common/coding-standard/Team271-Software-Coding-Standard.md`
- **Common companions:** `<vendored-path>/common/coding-standard/Team271-Software-Coding-Standard-*.md`
- **Team271-Lib rules:** `<vendored-path>/team-lib/coding-standard/coding-standard-teamlib-rules.md`
- **Team271-Lib library-notes:** `<vendored-path>/team-lib/coding-standard/coding-standard-library-notes.md`
- **Project code templates** (copied alongside this file): `subsystem-template.md`, `constants-template.md`, `input-driver-template.md`

---

## 2. Project-Specific Rules

> **Remove or replace the examples below** with rules that actually
> apply to this project. Keep the `CODE-<PROJECT>-NNN` ID format;
> increment within each project. Sub-items use letters (`a`, `b`, `c`).

### CODE-<PROJECT>-001 -- CAN ID Assignment (example)

a. Motor-controller CAN IDs **shall** be assigned by subsystem
   range. Reserved ranges for this project:

   | Range | Subsystem |
   | ----- | --------- |
   | 1-5 | Drivetrain |
   | 6-10 | Shooter |
   | 11-15 | Elevator |
   | 16-20 | Intake |

b. CAN IDs 21-60 **shall** be reserved for sensors (CANcoder,
   CANdi, etc.) following the same per-subsystem grouping.

c. Unassigned ranges are available for new mechanisms; update this
   rule when a new subsystem is added.

### CODE-<PROJECT>-002 -- Globals Field Naming (example)

a. Fields in `Globals.java` **shall** use `camelCase` mechanism
   names (`shooter`, `elevator`, `intake`), **not** role names
   (`subsystem1`, `mainMechanism`).

b. Controller references **shall** be named `controller<Role>`
   (`controllerDriver`, `controllerOperator`).

### CODE-<PROJECT>-003 -- Controller Button Map (example)

a. The authoritative button-to-action map for this project lives
   in `docs/<project>/controller-map.md`. Any binding **shall** be
   documented there before the code ships.

b. Operator button A **shall** be reserved for manual override.
   Never bind it to a routine that could damage a mechanism.

### CODE-<PROJECT>-004 -- Telemetry Namespacing (example)

a. Telemetry keys **shall** be prefixed with the subsystem name in
   PascalCase followed by a forward slash
   (`Shooter/State`, `ElevatorHeightMeters`).

b. Nested components **shall** extend the prefix
   (`ShooterWrist/AngleDeg`), not invent new top-level names.

### CODE-<PROJECT>-005 -- Subsystem Singleton Pattern (example)

a. Every subsystem class in this project **shall** follow the dual
   `getInstance()` pattern demonstrated in
   [`subsystem-template.md`](subsystem-template.md);
   constructed once in `Robot.robotInit()`.

b. Subsystem references **shall** be stored in `Globals.java` using
   the naming convention from `CODE-<PROJECT>-002` above.

### CODE-<PROJECT>-006 -- Subsystem Registration Order (example)

a. Subsystems **shall** be registered with
   `SubsystemManager.addSubsystem()` in the order defined below.
   Registration order is load-bearing -- see
   [`subsystem-template.md#registration-order`](subsystem-template.md#registration-order)
   for the underlying rules.

b. The registration order for this project is:

   | Order | Subsystem | Reason |
   | ----- | --------- | ------ |
   | 1 | `InputDriver` (and any other input classes) | other subsystems read input state downstream |
   | 2 | Sensor-producing subsystems (vision, gyro) | provide data for consumers |
   | 3 | Drivetrain | primary actuator, consumes sensor data |
   | 4+ | Mechanism subsystems | adapt per project |

   Replace the table above with the project's actual ordering.

### CODE-<PROJECT>-007 -- Subsystem Package Layout (example)

Binds the common [CODE-MAF-002](../common/coding-standard/Team271-Software-Coding-Standard-Modules.md#code-maf-002----package-structure)
"project's coding standard fixes subpackage conventions" clause.

a. Robot-project subsystems **shall** live in the `subsystems`
   subpackage under the project's top-level package. Input-handling
   classes **shall** live in `subsystems.Input`.

b. **Exception — swerve drivetrain.** The swerve drivetrain class
   implements the WPILib
   `edu.wpi.first.wpilibj2.command.Subsystem` interface (rather
   than extending the Team271-Lib
   `com.team271.lib.subsystem.Subsystem` base class) because it
   extends the CTRE-generated drivetrain class. It is owned by the
   drive subsystem and does not participate in the
   `SubsystemManager` lifecycle directly. See
   [`subsystem-template.md`](subsystem-template.md) for the
   canonical subsystem pattern this exception deviates from.

### CODE-<PROJECT>-008 -- Constants Organization (example)

Binds the common [CODE-MAF-003](../common/coding-standard/Team271-Software-Coding-Standard-Modules.md#code-maf-003----constants-organization)
"project fixes the concrete shared-constants artifact" clause.

a. Robot-wide constants **shall** be organized in `Constants.java`
   using nested `public static final class` inner classes grouped
   by subsystem or function:

   ```java
   public static final class ExampleASubsystemConstants { ... }
   public static final class ExampleBSubsystemConstants { ... }
   public static final class AutoConstants { ... }
   ```

b. Each inner constants class **shall** have a `private`
   constructor to prevent instantiation (already required by
   CODE-MAF-003(b); restated here for the nested-class form).

c. Constants specific to a single subsystem **may** be declared as
   `private static final` fields on the subsystem class, but
   **should** be moved to `Constants.java` once they are referenced
   by more than one class.

See [`constants-template.md`](constants-template.md) for the
canonical scaffold.

### CODE-<PROJECT>-009 -- State Machine Pattern (example)

Governs subsystem-level state machines in robot-project code. The
library-side equivalent for subsystems written inside Team271-Lib is
[CODE-LIB-003](../team-lib/coding-standard/coding-standard-teamlib-rules.md#code-lib-003----desired-to-actual-state-pattern).

> *Industry note: DO-178C (the avionics software certification
> standard) emphasizes deterministic, traceable state management.
> The desired-state / actual-state pattern makes every transition
> explicit and auditable -- you can always answer "what state is the
> robot in and why did it get there?" This is the same pattern used
> in flight control software.*

a. Subsystems that use state machines **shall** maintain two state
   variables: `mControlState` (current) and `mDesiredControlState`
   (desired). The desired state **shall** be set in
   `teleopPeriodic()` or `autonomousPeriodic()`; the actual state
   **shall** be applied in `robotPeriodicAfter()`.

   ```java
   /* Set in teleopPeriodic */
   mDesiredControlState = ExampleControlState.INDEX;

   /* Applied in robotPeriodicAfter */
   mControlState = mDesiredControlState;
   switch (mControlState) {
       case INDEX:
           setValue(ExampleSubsystemConstants.EXAMPLE_SPEED);
           break;
       // ...
       default:
           DriverStation.reportError(
               "Unexpected ExampleControlState: " + mControlState, false);
           break;
   }
   ```

b. State enums **shall** include an `IDLE` value. `IDLE` **shall**
   be the safe default state and the target of timeout fail-safe
   transitions (per
   [CODE-SAF-002](../common/coding-standard/Team271-Software-Coding-Standard-Safety.md#code-saf-002----motor-safety)
   and
   [CODE-SAF-003](../common/coding-standard/Team271-Software-Coding-Standard-Safety.md#code-saf-003----state-machine-completeness)).

c. Reachability and default-case requirements for enum `switch`
   blocks are inherited from
   [CODE-SAF-003](../common/coding-standard/Team271-Software-Coding-Standard-Safety.md#code-saf-003----state-machine-completeness)
   and [CODE-FUN-004(a)](../common/coding-standard/Team271-Software-Coding-Standard-Methods.md#code-fun-004----defensive-checks);
   no additional project-level rule is needed.

See [`subsystem-template.md`](subsystem-template.md) for the
canonical scaffold.

---

## 3. Deviations from Inherited Standards

Common or Team271-Lib rules that this project explicitly deviates
from. Each row cites the rule, the deviation, and the rationale.
Deviations require an approved PR that updates this table.

| Inherited rule | Project deviation | Reason |
| -------------- | ----------------- | ------ |
| (none yet) | | |
