<!-- markdownlint-disable MD007 -->
# Team271-Lib Coding Standard Supplement

| Field | Value |
| ----- | ----- |
| Supplements | [`java/Standard.md`](java/Standard.md), [`frc/Standard.md`](frc/Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Adopted |

The formal standard in [`java/`](java/) and [`frc/`](frc/) is written
generically and repeatedly delegates project-specific values to "the
consuming project's own coding standard" / "the project supplement".
This document is that supplement for Team271-Lib.

Because Team271-Lib is a **reusable library** (each season's robot
project forks it), this supplement emphasizes the library tier
(`CODE-LIB-*`) and the `com.team271.lib` package. The inverse
application-project patterns (subsystem singletons, a `Globals`
registry) are not library rules; they live in the `docs/robot-yyyy/`
scaffolding that forked robot projects inherit and are governed by
[CODE-FRC-005](frc/Standard-FRC-Conventions.md#code-frc-005).

Rules that delegate to this supplement: approved abbreviations
(CODE-GEN-001), mutable-static-field exceptions (CODE-GEN-015), import
exceptions (CODE-FMT-006), package values (CODE-MAF-002), generated
files (CODE-FRC-004), the singleton parent-type / registry concretes
(CODE-FRC-005), and the framework bindings for the library tier
(CODE-LIB-001..007).

## Package structure (resolves CODE-MAF-002)

- Reusable library code lives in `com.team271.lib` and its subpackages
  (`api`, `vendor`, `hardware`, `control`, `subsystem`, `auto`, `nt`,
  `sysid`, `util`, `bridge`, `wpilib`) per the six-layer architecture in
  [ADR-003](../team-lib/planning/adr/ADR-003-layered-architecture.md).
- The in-repo example/test consumer lives in `com.team271.libtest`; the
  `tools/init-robot.sh` fork script renames it to the robot's own
  package (`com.team271.<robot>`) when a season project forks this repo.
- Library code **shall not** be placed outside `com.team271.lib*`, and
  library code **shall not** import a consuming robot project's
  `Constants`
  ([CODE-LIB-007](java/Standard-Library.md#code-lib-007)).
- **Exception:** the WPILib launcher package `frc.robot` (`Main` /
  `Robot`) exists for GradleRIO and is the thin shim WPILib requires,
  not library code.

## Import exceptions (resolves CODE-FMT-006a)

- `import static edu.wpi.first.units.Units.*` is the one permitted
  wildcard import (established WPILib convention).
- Static imports of frequently-referenced library constants are
  permitted when they improve readability (e.g. a `ConstantsLib`
  member).

## Approved abbreviation additions (extends CODE-GEN-001 / Appendix A)

In addition to the standard's Appendix A allowlist, these FRC-ecosystem
short forms are approved identifiers: `auto`, `can`, `deg`, `mps`,
`pid`, `rad`, `rps`, `teleop`.

## Instance-field naming scope (refines CODE-VAR-001a)

[CODE-VAR-001a](java/Standard-Variables.md#code-var-001)'s `m`-prefix
applies to **private** non-static, non-final instance fields only.
Public and protected fields are part of the library's extension API —
robot projects fork this repo and subclass library types, reading
inherited protected/public fields
([ADR-001](../team-lib/planning/adr/ADR-001-team271-lib-standalone-library.md)).
Forcing the `m` prefix on them would rename published API and break
forks, so they **retain their published names**. Any future automated
`MemberName` enforcement (ADR-020) is therefore scoped to private fields;
the generic rule's broader wording yields to this binding for the library
tier.

## FRC terminology (acronyms used in the standard)

The generic acronym glossary (ADR, GC, JVM, the SDP / SRS / SVP / SCMP
plan IDs, etc.) lives in
[`java/Standard.md` §1.3](java/Standard.md#13-terminology-acronyms-used-in-this-document).
The FRC-ecosystem proper-name acronyms the standard and the
[`frc/`](frc/) overlay use in prose are defined here, since they are
vendor / framework facts the generic standard deliberately keeps out:

| Acronym | Meaning |
| ------- | ------- |
| CAN | Controller Area Network |
| CANivore | CTRE CAN-to-USB bridge |
| CCW / CW | Counter-Clockwise / Clockwise |
| CTRE | Cross The Road Electronics |
| FOC | Field-Oriented Control |
| FRC | FIRST Robotics Competition |
| FX / FXS | TalonFX (brushless) / TalonFXS (brushed) CTRE controllers |
| HAL | Hardware Abstraction Layer (WPILib) |
| IMU | Inertial Measurement Unit |
| NT | NetworkTables |
| PID | Proportional-Integral-Derivative (controller) |
| RPS | Rotations Per Second |
| WPILib | WPI Robotics Library |

## Generated files (resolves CODE-FRC-004a)

The concrete generated files this repo exempts (listed
`linguist-generated` in `.gitattributes`): `BuildConstants.java`
(gversion build stamp), `TunerConstants.java` (CTRE Tuner X swerve
config, in the `libtest` example), and `LimelightHelpers.java` (vendor
source). They are exempt per
[CODE-FRC-004](frc/Standard-FRC-Conventions.md#code-frc-004).

Accepted exemption: `TunerConstants.java` contains non-ASCII characters
(degree sign, modifier-letter capital T) emitted by Tuner X in comments;
they are accepted under the generated-file exemption and not hand-edited
out.

## Accepted static-field exceptions (resolves CODE-GEN-015)

Library code uses **dependency injection, not singletons**
([CODE-LIB-001](java/Standard-Library.md#code-lib-001)), so
`com.team271.lib` classes carry no `getInstance()` accessor or
static-instance field. The accepted exceptions in this repo are
therefore narrow:

- Framework managers that own a process-wide resource and are
  constructed once at `robotInit` (the CTRE signal registry behind
  `CTREManager` / `HardwareManager`, and `SubsystemManager`) hold their
  registrations in static collections by design; they are initialized
  once and never reassigned.
- The robot-project singleton pattern (a `mInstance` field +
  `getInstance()`) and an optional `Globals` registry are
  **application-side** exceptions documented in
  [`../robot-yyyy/`](../robot-yyyy/) under
  [CODE-FRC-005](frc/Standard-FRC-Conventions.md#code-frc-005); they do
  not apply to library code.

## Language feature policy (project application of java/ section 2)

Java 17 via GradleRIO (`build.gradle` source/target compatibility).

- **Permitted:** switch expressions (`->`), enhanced for-each, `var`
  when the type is obvious, records for data carriers, text blocks in
  tests / configuration.
- **Restricted in periodic methods** (GC pressure,
  [CODE-GEN-004](java/Standard-General.md#code-gen-004)): streams and
  lambdas that create intermediate collections, autoboxing in loops,
  string concatenation in hot paths.
- **Prohibited:** `Thread.sleep()` in robot code (use WPILib `Timer` /
  `Notifier`), `System.exit()` / `Runtime.halt()`, raw `Thread`
  creation, `System.gc()`, `Object.finalize()`.

## File organization and templates

Library components (`TObj` subclasses under `com.team271.lib`) follow
the desired-to-actual lifecycle layout below; robot-project subsystems
use the singleton layout in
[`../robot-yyyy/subsystem-template.md`](../robot-yyyy/subsystem-template.md).

A library component orders its members:

1. Constants / configuration (constructor parameters preferred over
   static config)
2. Vendor objects and their passthrough getters
   ([ADR-005](../team-lib/planning/adr/ADR-005-passthrough-wrapper-not-wall.md))
3. State: `mDesiredX` / `mX` pairs, timers, counters
4. Constructor (takes an owner / parent reference; registers signals
   with the CTRE manager)
5. `stop()` fail-safe
6. Lifecycle hooks in execution order: `robotInit`,
   `robotPeriodicBefore` (read sensors), `<mode>Periodic` (decide),
   `robotPeriodicAfter` (apply outputs), `outputTelemetry` (publish)
7. Private helpers

Library-wide constants live in `ConstantsLib.java` at the root of
`com.team271.lib`
([CODE-LIB-007](java/Standard-Library.md#code-lib-007)); per-component
constants stay private to their class.

## Library tier framework bindings (resolves CODE-LIB-001..007)

The generic library rules in
[`java/Standard-Library.md`](java/Standard-Library.md) delegate the
concrete facility to this supplement. Team271-Lib's facilities:

| Rule | Library facility |
| ---- | ---------------- |
| [CODE-LIB-001](java/Standard-Library.md#code-lib-001) Explicit instantiation | Constructors + DI via a `TObj argParent` parameter; no `getInstance()` on library classes. [ADR-004](../team-lib/planning/adr/ADR-004-explicit-instantiation-no-singletons.md) |
| [CODE-LIB-002](java/Standard-Library.md#code-lib-002) Centralized resource access | CTRE `StatusSignal`s registered with `CTREManager` at construction; one bulk `HardwareManager.refreshAll()` per cycle, never per-signal `refresh()` in periodic code. [ADR-009](../team-lib/planning/adr/ADR-009-centralized-can-refresh.md) |
| [CODE-LIB-003](java/Standard-Library.md#code-lib-003) Desired vs applied state | `mDesiredX` / `mX` pairs; sensors read in `robotPeriodicBefore()`, outputs applied in `robotPeriodicAfter()`. [ADR-010](../team-lib/planning/adr/ADR-010-desired-to-actual-state-pattern.md) |
| [CODE-LIB-004](java/Standard-Library.md#code-lib-004) Fault isolation | `SubsystemManager.forEachSafe()` catches and reports; reporting via `DriverStation.reportError()` + `Elastic.sendNotification(WARNING, ...)`, never `System.err` / silent swallow. [ADR-011](../team-lib/planning/adr/ADR-011-subsystem-exception-isolation.md) |
| [CODE-LIB-005](java/Standard-Library.md#code-lib-005) Bounded waits | A named timeout constant in the component's constants; on timeout stop motors, restore default current limits, transition to `IDLE`, and `Elastic.sendNotification(WARNING, ...)`. [ADR-012](../team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md) |
| [CODE-LIB-006](java/Standard-Library.md#code-lib-006) Deterministic lifecycle | The `TObj` contract driven by `SubsystemManager`: `robotInit` -> `robotPeriodicBefore` -> `<mode>Periodic` -> `robotPeriodicAfter` -> `outputTelemetry` (no parameter). [SDD-subsystem.md](../team-lib/planning/sdd/SDD-subsystem.md) |
| [CODE-LIB-007](java/Standard-Library.md#code-lib-007) Package / constants isolation | `com.team271.lib` package root; library-wide constants in `ConstantsLib.java`; library code never imports a robot project's `Constants`. |

Facilities that several common rules delegate to are concretized as:
`TObj.outputTelemetry()` + AdvantageKit `Logger.recordOutput` / `NTEntry`
(telemetry, CODE-BUG-001); `Elastic.sendNotification` (driver alerts,
CODE-BUG-002); `LoggedNTInput` + `checkTuning()` (replay-faithful
runtime tuning, CODE-BUG-004); and `DriverStation.reportError()` +
`Elastic` (error reporting, CODE-GEN-011). The full common-rule ->
library-API binding table lives in
[`../team-lib/coding-standard/coding-standard-library-notes.md`](../team-lib/coding-standard/coding-standard-library-notes.md).
