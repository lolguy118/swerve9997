<!-- markdownlint-disable MD007 -->
# Team271-Lib FRC Coding Standard

| Field | Value |
| ----- | ----- |
| Document No | `SCS-FRC` |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

## Revision History

| Revision | Date | Author | Description |
| -------- | ---- | ------ | ----------- |
| 0.1 | `2026-06-14` | - | Initial draft |

---

## 1. Introduction

### 1.1 Purpose

This document is the FRC (FIRST Robotics Competition) overlay
on top of the pure-Java coding standard. It adds the rule
families that only make sense inside the FRC ecosystem -
WPILib lifecycle, CTRE Phoenix 6 / REV / NavX driver
patterns, NetworkTables conventions, robot-safety practices
(motor / CAN / FMS / brownout / vision), and the replay-
faithful telemetry conventions used by AdvantageKit and
similar loggers.

The core principles inherited from
[`../java/Standard.md`](../java/Standard.md) - defensive
coding, predictable execution, minimal complexity, rigorous
formatting - apply unchanged. This overlay adds the
domain-specific practices a robot project also needs to
survive a 2:30 match and recover from an unplanned brownout
on the field.

In this document, any rule specified with **shall** denotes
a mandatory requirement. Rules specified with **should** are
recommended practices.

### 1.2 Scope

This overlay applies to all Java source in FRC robot
projects. It is **additive**: the pure-Java rules in
[`../java/`](../java/) are not duplicated here - they remain
in force, and this overlay only documents the FRC-specific
additions and extensions.

### 1.3 Precondition

The FRC overlay **requires** the pure-Java overlay
[`../java/`](../java/) to also be applied. The companion
files in this directory link into `../java/<companion>.md`
for shared topics; applying this overlay alone produces
broken relative links that the consumer's
`markdown-link-check` CI workflow will fail on.

The coding-standards stack's apply script enforces this
precondition (see the overlay's
[`README.md`](../../../README.md) for the apply procedure).

### 1.4 Applicable Documents

In addition to the industry standards cited by the pure-Java
overlay
([`../java/Standard.md` §1.4](../java/Standard.md#14-applicable-documents)),
the FRC overlay relies on vendor documentation maintained
outside this repo:

- [WPILib documentation][wpilib-docs] - robot framework,
  Subsystem / Command base classes, DriverStation API,
  Notifier, Timer
- CTRE Phoenix 6 documentation - TalonFX / TalonFXS /
  CANcoder / CANivore APIs; the project's chosen URL goes
  in the consuming project's `docs/reference-urls.md`
- REV Robotics documentation - SPARK MAX / SPARK Flex
  APIs; same arrangement
- PathPlanner, Choreo, AdvantageKit, PhotonVision,
  Limelight - vendor docs maintained per upstream release;
  links in the consuming project's `docs/reference-urls.md`

[wpilib-docs]: https://docs.wpilib.org/

The consuming project's `docs/reference-urls.md` is the
single authoritative index for external vendor / tool URLs
the FRC code depends on; this standard does not duplicate it.

---

## 2. Companion Documents

This overlay is split across three companions plus this
master. Each FRC-specific rule family owns one prefix:

| Topic | Companion | Rule prefix |
| ----- | --------- | ----------- |
| Debugging and Telemetry | [`Standard-Debug.md`](Standard-Debug.md) | `CODE-BUG-*` |
| Safety Practices | [`Standard-Safety.md`](Standard-Safety.md) | `CODE-SAF-*` |
| FRC-Specific Conventions (NetworkTables, lifecycle methods, unit suffixes, generated-file exemptions, singleton pattern, lifecycle contract, state-machine pattern, vendor usage patterns) | [`Standard-FRC-Conventions.md`](Standard-FRC-Conventions.md) | `CODE-FRC-*` |

---

## 3. Routing to Pure-Java Rules

For shared topics, this overlay routes back into the pure-Java
overlay. The IDs below are *not* re-defined here - applying
this overlay implies the pure-Java overlay is already in
place, and reviewers cite the pure-Java rule by its java/
location.

| Topic | Pure-Java companion |
| ----- | ------------------- |
| General (keywords, annotations, type safety, exceptions, GC, concurrency) | [`../java/Standard-General.md`](../java/Standard-General.md) |
| Formatting (braces, parens, blank lines, line endings, imports) | [`../java/Standard-Format.md`](../java/Standard-Format.md) |
| Modules and Files (class/file naming, packages, constants, generated code) | [`../java/Standard-Modules.md`](../java/Standard-Modules.md) |
| Methods (naming, single-exit, defensive checks) | [`../java/Standard-Methods.md`](../java/Standard-Methods.md) |
| Variables (naming, init, types, magic numbers) | [`../java/Standard-Variables.md`](../java/Standard-Variables.md) |
| Control Structures (if/switch/loops) | [`../java/Standard-Control.md`](../java/Standard-Control.md) |
| Comments (JavaDoc, block, inline) | [`../java/Standard-Comments.md`](../java/Standard-Comments.md) |
| Library Design (reusable-library patterns: DI, lifecycle, package isolation) | [`../java/Standard-Library.md`](../java/Standard-Library.md) |
| Security Coding Practices | [`../java/Standard-Security.md`](../java/Standard-Security.md) |
| Static Analysis and Tooling (Spotless, compiler warnings, JVM, enforcement matrix) | [`../java/Standard-Compliance.md`](../java/Standard-Compliance.md) |
| Reference Appendices (abbreviations, reserved words, `final` guide, naming quick-ref) | [`../java/Standard-Appendices.md`](../java/Standard-Appendices.md) |

---

## 4. FRC-Specific Additions

The three FRC companions add 23 rules across three prefixes:

- `CODE-BUG-*` - 4 rules in
  [`Standard-Debug.md`](Standard-Debug.md) covering telemetry
  publication, dashboard notifications, error reporting, and
  runtime tunability.
- `CODE-SAF-*` - 12 rules in
  [`Standard-Safety.md`](Standard-Safety.md) covering input
  validation, motor safety, state-machine completeness,
  subsystem coordination, autonomous safety, CAN bus safety,
  disabled-mode behaviour, fault tolerance (brownout / FMS /
  motor reset), vision-data validation, sustained
  over-current protection, CAN partition resilience, and
  waiting-operation timeouts.
- `CODE-FRC-*` - 7 rules in
  [`Standard-FRC-Conventions.md`](Standard-FRC-Conventions.md)
  covering the NetworkTables field prefix, WPILib lifecycle
  method names, FRC unit-suffix conventions, the FRC-specific
  generated-file exemption list, the subsystem singleton
  pattern, the robot lifecycle contract, and the
  desired/actual state-machine pattern.

The same companion also collects three FRC reference
appendices (robot-mode lifecycle, RoboRIO GC pressure
patterns, CTRE Phoenix 6 usage patterns) that are
informational support for the rules.

---

## 5. Code Review Additions for FRC Projects

These items extend the code-review checklist in
[`../java/Standard-Compliance.md`](../java/Standard-Compliance.md#54-code-review-checklist)
with FRC-specific items that cannot be caught by static
analysis:

- [ ] Motor safety: current limits configured, soft limits
      after homing, voltage bounds (CODE-SAF-002)
- [ ] Subsystem lifecycle: correct method ordering, outputs
      in the per-cycle "after" hook only (CODE-FRC-006)
- [ ] Telemetry coverage: key state variables logged
      (CODE-BUG-001)
- [ ] Cross-subsystem coordination: registration order
      correct, timing dependencies documented (CODE-SAF-004)
- [ ] Auto safety: missing-path handling, timing-window
      correctness, alliance-flip tested (CODE-SAF-005)
- [ ] Motor direction: `configDirection()` matches physical
      mechanism intent
- [ ] Resource cleanup: timers and sensor subscriptions
      cleaned up in mode-exit methods (CODE-SAF-007)
- [ ] Timeout protection: waiting operations have named
      timeout constants with fail-safe behaviour
      (CODE-SAF-012; homing specifics in CODE-SAF-002c)
- [ ] Fault tolerance: motor-reset detection, brownout
      recovery, mid-match `robotInit` / FMS-data handling,
      vision-staleness checks, sustained over-current
      detection (CODE-SAF-008, CODE-SAF-009, CODE-SAF-010)
- [ ] Runtime tunability: configurable values use the
      project's tunable-input pattern (CODE-BUG-004)
- [ ] NetworkTables fields use the `nt` prefix (CODE-FRC-001)
- [ ] Lifecycle method overrides are `@Override`-annotated
      with WPILib-exact spelling (CODE-FRC-002)
- [ ] Physical-quantity constants carry the documented unit
      suffix (CODE-FRC-003)

<!-- markdownlint-enable MD007 -->
