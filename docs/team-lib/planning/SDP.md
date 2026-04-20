# Software Development Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

This document is Team271-Lib's specific development plan. It builds
on the shared planning framework (see §2 Applicable Documents) and
records the library's concrete pins, platform deltas, layer build
order, and deviations.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose and Scope

Defines when Team271-Lib code is written, in what order, and against
what toolchain pin. Captures the library-specific layer build priority
and milestones. Does not cover:

- Coding rules → [coding-standard core](../../common/Team271-Software-Coding-Standard.md)
- Test strategy → [SVP.md](SVP.md)
- API contracts → [SRS.md](SRS.md)
- Vendordep / versioning policy → [SCMP.md](SCMP.md)

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [`../../common/planning/development-plan.md`](../../common/planning/development-plan.md) | Shared toolchain, platform matrix, phase model, milestones, deviations table format source |
| [`../../common/planning/configuration-management.md`](../../common/planning/configuration-management.md) | Versioning, vendordep upgrade, deviation-tracking row format |
| [`../../common/Team271-Software-Coding-Standard.md`](../../common/Team271-Software-Coding-Standard.md) + companions | Normative coding rules |
| [SRS.md](SRS.md) | Library requirements |
| [SVP.md](SVP.md) | Verification plan |
| [SCMP.md](SCMP.md) | Configuration management |
| [ADR-001](adr/ADR-001-team271-lib-standalone-library.md) | Library packaging and consumption model |
| [ADR-002](adr/ADR-002-java17-wpilib-gradlerio-toolchain.md) | Toolchain decision |
| [ADR-004](adr/ADR-004-layered-architecture.md) | Layer order |
| [ADR-006](adr/ADR-006-ctre-phoenix6-primary-vendor.md) | CTRE Phoenix 6 as primary vendor |
| [ADR-009](adr/ADR-009-junit5-hal-simulation-tests.md) | JUnit 5 + HAL simulation test framework |

## 3. Project Overview

Team271-Lib is Team 271's reusable FRC library: lifecycle primitives,
motor/sensor wrappers, PID variants, a state-machine-based subsystem
pattern, autonomous move composition, and telemetry. It is consumed as
a versioned dependency by each year's robot project
(see [ADR-001](adr/ADR-001-team271-lib-standalone-library.md)).

The code is organized into six layers with strict dependency rules
([ADR-004](adr/ADR-004-layered-architecture.md)). The dependency
graph is drawn in
[team271-lib-dependency-diagram.mmd](../internal/team271-lib-dependency-diagram.mmd).

## 4. Development Environment (library pins)

Concrete pins for this library's toolchain (component categories
inherited from the framework):

| Component | Version | Source |
| --------- | ------- | ------ |
| Java | 17 | WPILib-bundled JDK |
| Gradle | Per GradleRIO 2026 | `gradle/wrapper/gradle-wrapper.properties` |
| WPILib | Per 2026 release | WPILib installer |
| Spotless | Per build.gradle | Gradle plugin |
| JaCoCo | Per build.gradle | Gradle plugin |
| Phoenix 6 | Per `vendordeps/Phoenix6-frc2026-latest.json` | Vendordep |
| AdvantageKit | Per `vendordeps/AdvantageKit.json` | Vendordep |
| PathPlanner | Per `vendordeps/PathplannerLib.json` | Vendordep |
| markdownlint-cli2 | Latest | npm / developer install |

The authoritative version list is `vendordeps/*.json` and
`build.gradle`. This document references but does not duplicate them.
See [SCMP.md §4](SCMP.md#4-vendordep-management-team271-lib-specifics)
for upgrade process.

## 5. Platform Matrix (library-specific deltas)

Team271-Lib targets every platform named in the framework.
Library-specific facts:

- Desktop sim **shall** initialize both WPILib HAL sim and Phoenix 6
  sim; the library's CTRE wrappers depend on vendor sim.
- CI runs on `ubuntu-24.04` (GitHub Actions default) for every push
  to `main` and every pull request
  (see [SVP §7](SVP.md#7-ci-pipeline-gates-library-workflow)).
- Local pre-edit gates run via `.claude/hooks/`.
- Hardware target is roboRIO 2 only; the library does not target
  roboRIO 1.

## 6. Layer Build Priority

When building from scratch, each layer must be present and tested
before the next layer above it begins. Layers may depend only on
layers below them ([ADR-004](adr/ADR-004-layered-architecture.md)).

| Priority | Layer | Depends On | Why First |
| -------- | ----- | ---------- | --------- |
| 1 | `api/` | (none — pure interfaces) | Defines the vendor-neutral contract |
| 2 | `vendor/ctre/` | `api/` + CTRE Phoenix 6 | Provides the sole motor/sensor realization |
| 3 | `hardware/` | `vendor/ctre/`, `api/` | TObj-lifecycle wrappers + CAN orchestration |
| 4 | `control/` | `hardware/` | PID depends on motors + sensors |
| 5 | `subsystem/` | `control/`, `hardware/` | Subsystems coordinate control + hardware |
| 6 | `auto/` | `subsystem/` | Autonomous composes subsystem commands |
| C | `sysid/`, `nt/`, `util/` | (cross-cutting) | Used by any layer; depend on none above |

## 7. Deviations from SCS

Library-specific deviations from the shared coding standard. Table
format inherited from the framework (see §2 Applicable Documents).

| Rule ID | File / Scope | Rationale | Approved By | Date |
| ------- | ------------ | --------- | ----------- | ---- |
| (none) | | | | |
