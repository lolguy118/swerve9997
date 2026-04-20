# Software Development Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

## 1. Purpose and Scope

This document defines **when** Team271-Lib code is written, **in what
order**, and **on what toolchain**. It captures the development
lifecycle phases, platform matrix, layer build priority, and
milestones. It deliberately does not cover:

- Coding rules, naming, or formatting → see
  [Team271-Software-Coding-Standard.md](../../common/Team271-Software-Coding-Standard.md)
- Test strategy and coverage → see [SVP.md](SVP.md)
- API contracts or non-functional guarantees → see [SRS.md](SRS.md)
- Vendordep versioning or branch policy → see [SCMP.md](SCMP.md)

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [Team271-Software-Coding-Standard.md](../../common/Team271-Software-Coding-Standard.md) + companions | Normative coding rules |
| [SRS.md](SRS.md) | Library requirements |
| [SVP.md](SVP.md) | Verification plan |
| [SCMP.md](SCMP.md) | Configuration management |
| [ADR-002](adr/ADR-002-java17-wpilib-gradlerio-toolchain.md) | Toolchain decision |
| [ADR-004](adr/ADR-004-layered-architecture.md) | Layer order |

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

## 4. Development Environment

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

The authoritative version list is `vendordeps/*.json` and `build.gradle`.
This document references but does not duplicate them. See
[SCMP.md §4](SCMP.md) for upgrade process.

## 5. Platform Matrix

| Platform | Purpose | CI? |
| -------- | ------- | --- |
| roboRIO 2 | Competition target | No (hardware-only) |
| Windows 11 (desktop sim) | Primary developer platform | Yes |
| macOS (desktop sim) | Secondary developer platform | Yes |
| Linux (desktop sim) | Tertiary developer platform + CI runner (`ubuntu-24.04`) | Yes |

Desktop simulation requires WPILib HAL sim and Phoenix 6 sim. Tests
are runnable on all three desktop platforms. A GitHub Actions CI
pipeline runs on `ubuntu-24.04` for every push to `main` and every
pull request (see [SVP §7](SVP.md#7-ci-pipeline-gates)); local
pre-edit gates run via `.claude/hooks/`.

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

## 7. Development Phases (FRC-Calendar Keyed)

FRC seasons impose a clear rhythm on library development. Each phase
has entry gates, deliverables, and exit criteria. This document does
not set calendar dates — phases begin when entry conditions are met.

### 7.1 Offseason (May–September)

- **Entry:** competition season concluded.
- **Deliverables:** architectural ADRs, breaking API changes,
  library refactors, new layer features.
- **Exit:** API freeze declared (typically end of September).

### 7.2 Preseason (October–December)

- **Entry:** API freeze declared.
- **Deliverables:** new features that extend (never break) the API;
  documentation updates; integration tests against the upcoming
  robot project.
- **Exit:** build season starts; library MINOR version tagged.

### 7.3 Build Season (January–February)

- **Entry:** FRC kickoff.
- **Deliverables:** **stability only** — bug fixes, tuning, small
  additive features that do not break the API.
- **Exit:** first competition. No breaking changes permitted.

### 7.4 Competition Season (March–April)

- **Entry:** first competition.
- **Deliverables:** **hotfix only** — fixes to bugs discovered at
  competition. Each change requires `/lib-review` + ≥1 maintainer.
- **Exit:** championship concluded. Library PATCH tags applied
  per fix.

### 7.5 Postseason (May)

- **Entry:** championship concluded.
- **Deliverables:** retrospective, next-season major tag (e.g.,
  `v2027.0.0`), docs updates, ADR drafts for the next offseason.
- **Exit:** offseason begins.

## 8. Milestones

### 8.1 Version Tag Events

| Event | Action | Example |
| ----- | ------ | ------- |
| Offseason start | Tag prior season final | `v2026.N.P` |
| Preseason API freeze | Tag next-season MINOR=0 | `v2027.0.0` |
| Each competition hotfix | Tag patch | `v2027.0.P` |

### 8.2 Phase Transitions (no tag)

| Event | Action |
| ----- | ------ |
| Build season start | No-new-features rule takes effect |
| Postseason | Retrospective; ADR drafts for next offseason |

Version format: `YYYY.MINOR.PATCH`. See
[SCMP.md §3](SCMP.md) for semantics.

## 9. Deviations from SCS

Deviations from the Coding Standard are filed here. Each deviation
requires a rationale and a maintainer approval.

| Rule ID | File / Scope | Rationale | Approved By | Date |
| ------- | ------------ | --------- | ----------- | ---- |
| (none) | | | | |
