# Planning Documentation Index

Formal planning and design documents for Team271-Lib. They describe
**what** the library does (SRS), **how** it's built (SDP), **how**
it's verified (SVP), and **how** it's versioned (SCMP). The Software
Design Descriptions (SDDs) document the architecture of each layer;
the Architecture Decision Records (ADRs) capture the rationale behind
key decisions.

## Shared framework (common)

The shared planning framework (SemVer policy, FRC-calendar phase
model, test-level structure, CI gate pattern) lives under
[`../../common/planning/`](../../common/planning/). The documents in
this directory concretize those frameworks with library-specific
choices — they **reference** the common policy rather than duplicate
it.

## Document Map

| Document | Type | Purpose |
| -------- | ---- | ------- |
| [SDP.md](SDP.md) | Software Development Plan | Development phases, layer build order, platform matrix, milestones |
| [SRS.md](SRS.md) | Software Requirements Specification | API contracts and non-functional guarantees, per-layer |
| [SVP.md](SVP.md) | Software Verification Plan | Test strategy, coverage thresholds, CI gates, hook enforcement |
| [SCMP.md](SCMP.md) | Software Configuration Management Plan | Vendordep management, SemVer, branch workflow, deviation tracking |
| [sdd/](sdd/) | Software Design Descriptions | Module decomposition and API surface per architectural layer |
| [adr/](adr/) | Architecture Decision Records | Rationale behind key architectural choices |

## Package-to-SDD Map

| Source Package | SDD |
| -------------- | --- |
| `com.team271.lib` (root — TObj, Named, Lifecycle, TRobot, ConstantsLib) | [SDD-team271-lib.md](sdd/SDD-team271-lib.md) |
| `com.team271.lib.wpilib.*` (IterativeRobotBase, TimedRobot extensions) | [SDD-team271-lib.md](sdd/SDD-team271-lib.md) |
| `com.team271.lib.api.*` | [SDD-api.md](sdd/SDD-api.md) |
| `com.team271.lib.vendor.ctre.*`, `com.team271.lib.bridge.*` | [SDD-vendor-ctre.md](sdd/SDD-vendor-ctre.md) |
| `com.team271.lib.hardware.*` | [SDD-hardware.md](sdd/SDD-hardware.md) |
| `com.team271.lib.control.*` | [SDD-control.md](sdd/SDD-control.md) |
| `com.team271.lib.subsystem.*` | [SDD-subsystem.md](sdd/SDD-subsystem.md) |
| `com.team271.lib.auto.*` | [SDD-auto.md](sdd/SDD-auto.md) |
| `com.team271.lib.sysid.*` | [SDD-sysid.md](sdd/SDD-sysid.md) |
| `com.team271.lib.nt.*` | [SDD-nt.md](sdd/SDD-nt.md) |
| `com.team271.lib.util.*` | [SDD-util.md](sdd/SDD-util.md) |

## Architecture Decision Records

Each ADR records one architectural decision. Statuses:

- **Proposed** — under discussion; not yet binding.
- **Accepted** — binding. Contributors **shall** follow the decision.
- **Superseded** — replaced by a later ADR, which the Superseded ADR
  points at. Accepted ADRs are never deleted; they are Superseded.

| ADR | Title | Status |
| --- | ----- | ------ |
| [ADR-001](adr/ADR-001-team271-lib-standalone-library.md) | Team271-Lib as a Standalone Library | Accepted |
| [ADR-002](adr/ADR-002-java17-wpilib-gradlerio-toolchain.md) | Java 17 + WPILib + GradleRIO Toolchain | Accepted |
| [ADR-003](adr/ADR-003-passthrough-wrapper-not-wall.md) | Passthrough — Wrapper, Not Wall | Accepted |
| [ADR-004](adr/ADR-004-layered-architecture.md) | Layered Architecture | Accepted |
| [ADR-005](adr/ADR-005-composition-over-commands.md) | Composition over Commands | Accepted |
| [ADR-006](adr/ADR-006-ctre-phoenix6-primary-vendor.md) | CTRE Phoenix 6 as Primary Vendor | Accepted |
| [ADR-007](adr/ADR-007-centralized-can-refresh.md) | Centralized Bulk CAN Refresh | Accepted |
| [ADR-008](adr/ADR-008-logged-nt-input-backed-tuning.md) | LoggedNTInput-Backed Tuning | Accepted |
| [ADR-009](adr/ADR-009-junit5-hal-simulation-tests.md) | JUnit 5 + JaCoCo + HAL Simulation for Library Tests | Accepted |
| [ADR-010](adr/ADR-010-subsystem-exception-isolation.md) | Per-Subsystem Exception Isolation in SubsystemManager | Accepted |
| [ADR-011](adr/ADR-011-mandatory-timeouts-fail-safe.md) | Mandatory Timeouts with Fail-Safe + Driver Alert | Accepted |
| [ADR-012](adr/ADR-012-advantagekit-logging.md) | AdvantageKit for Telemetry and Replay Logging | Accepted |
| [ADR-013](adr/ADR-013-pathplanner-autonomous.md) | PathPlanner for Autonomous Path Following | Accepted |
| [ADR-014](adr/ADR-014-desired-to-actual-state-pattern.md) | Desired-to-Actual State Pattern in Subsystems | Accepted |
| [ADR-015](adr/ADR-015-explicit-instantiation-no-singletons.md) | Explicit Object Instantiation, No Singletons in Library Code | Accepted |

## Planned ADRs

> **Status: Planned — Not Yet Implemented.**

Reserved slots for decisions already foreseen but not yet authored.
Each **shall** be written before (or alongside) the first change
that depends on it — not speculatively.

| Planned ADR | Scope |
| ----------- | ----- |
| Null-safety annotation policy | Choice of annotation set (JSpecify, JetBrains, JSR-305) and rollout plan before NullAway becomes a CI gate |
| Supply-chain / CVE response | How the library triages OWASP / GitHub dependency-review findings and which severities trigger an out-of-phase hotfix |

## What These Documents Do NOT Cover

Deliberately excluded to prevent duplication with authoritative sources:

| Topic | Authoritative Source |
| ----- | -------------------- |
| Coding rules, naming, formatting | [`../../common/Team271-Software-Coding-Standard.md`](../../common/Team271-Software-Coding-Standard.md) |
| Shared planning policy (SemVer, phase model, CI framework) | [`../../common/planning/`](../../common/planning/) |
| AI-behavior guardrails (planning, safety, docs, coding-standard, team271-lib scopes) | [`../../../.claude/rules/`](../../../.claude/rules/) |
| Git workflow, PR process, commit rules | `../../../CONTRIBUTING.md` (repository root) |
| Vendordep versions | `../../../vendordeps/*.json`, `../../../build.gradle` |
| Robot-project requirements | Each robot project's own planning docs |

## Scope of Planning Documents

Planning documents own **what** each layer must do and **how** the library
is structured. Coding rules own **how** to write the code. Robot projects own
**what the robot shall do**.
