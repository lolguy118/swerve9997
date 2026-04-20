# Software Development Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

This document is Team271-Lib's specific development plan. The shared
framework — toolchain baseline, platform matrix pattern, FRC-calendar
phase model, milestone-event structure — lives in
[`../../common/planning/development-plan.md`](../../common/planning/development-plan.md).
This file records the library's specifics.

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
| [`../../common/planning/development-plan.md`](../../common/planning/development-plan.md) | Shared toolchain, platform matrix, phase model |
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

Team271-Lib follows the shared toolchain baseline
(see [`development-plan.md §1`](../../common/planning/development-plan.md#1-development-environment-baseline)).
Concrete pins for this library:

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

## 5. Platform Matrix (library-specific rows)

Team271-Lib follows the shared platform-matrix pattern
(see [`development-plan.md §2`](../../common/planning/development-plan.md#2-platform-matrix-pattern)).
The library's concrete matrix:

| Platform | Purpose | CI? |
| -------- | ------- | --- |
| roboRIO 2 | Competition target | No (hardware-only) |
| Windows 11 (desktop sim) | Primary developer platform | Yes |
| macOS (desktop sim) | Secondary developer platform | Yes |
| Linux (desktop sim) | Tertiary developer platform + CI runner (`ubuntu-24.04`) | Yes |

Desktop simulation requires WPILib HAL sim and Phoenix 6 sim. Tests
are runnable on all three desktop platforms. GitHub Actions runs on
`ubuntu-24.04` for every push to `main` and every pull request
(see [SVP §7](SVP.md#7-ci-pipeline-gates-library-workflow)); local pre-edit gates run
via `.claude/hooks/`.

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

## 7. Development Phases

Team271-Lib adopts the shared four-phase FRC-calendar model as-is
(see [`development-plan.md §3`](../../common/planning/development-plan.md#3-frc-calendar-phase-model)).
Offseason contains the internal API-freeze milestone at which the
next-season MINOR is tagged.

## 8. Milestones

### 8.1 Version Tag Events

| Event | Action | Example |
| ----- | ------ | ------- |
| Offseason start | Tag prior season final | `v2026.N.P` |
| Offseason API freeze | Tag next-season MINOR=0 | `v2027.0.0` |
| Each competition hotfix | Tag patch | `v2027.0.P` |

### 8.2 Phase Transitions (no tag)

| Event | Action |
| ----- | ------ |
| Build season start | No-new-features rule takes effect |
| Postseason | Retrospective; ADR drafts for next offseason |

Version format: `YYYY.MINOR.PATCH`. See
[SCMP.md §3](SCMP.md#3-library-versioning) for semantics.

## 9. Deviations from SCS

Library-specific deviations from the shared coding standard. Format
per
[`configuration-management.md §5`](../../common/planning/configuration-management.md#5-deviation-tracking).

| Rule ID | File / Scope | Rationale | Approved By | Date |
| ------- | ------------ | --------- | ----------- | ---- |
| (none) | | | | |
