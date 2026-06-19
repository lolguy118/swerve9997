# Team271-Lib — Planning and Design Documents

This folder holds the formal planning and design documents for
**Team271-Lib**, Team 271's reusable FRC (FIRST Robotics Competition)
library. If you're new, this folder is where you learn what the
library does, how it's built, and why it's built the way it is.

Each kind of document answers a different question:

- **What** the library promises → [SRS.md](SRS.md) — Software
  Requirements Specification
- **How** the library is built → [SDP.md](SDP.md) — Software
  Development Plan
- **How** the library is tested → [SVP.md](SVP.md) — Software
  Verification Plan
- **How** versions and releases are managed → [SCMP.md](SCMP.md) —
  Software Configuration Management Plan
- **How** each layer is designed → [sdd/](sdd/) — Software Design
  Descriptions (SDDs)
- **Why** we made big architectural choices → [adr/](adr/) —
  Architecture Decision Records (ADRs)

## Start here

If you're new to the library, read in this order:

1. [`../README.md`](../README.md) — the `docs/team-lib/` overview.
2. [`../guides/start-here.md`](../guides/start-here.md) — the
   onboarding guide for contributors.
3. [ADR-003 Layered Architecture](adr/ADR-003-layered-architecture.md)
   — the six-layer picture that everything else depends on.
4. The Software Design Description (SDD) for whatever you're
   working on:
    - Motors and sensors → [SDD-api.md](sdd/SDD-api.md) +
      [SDD-vendor-ctre.md](sdd/SDD-vendor-ctre.md) +
      [SDD-hardware.md](sdd/SDD-hardware.md)
    - Closed-loop control (PID, feedforward) →
      [SDD-control.md](sdd/SDD-control.md)
    - State-machine-based subsystems →
      [SDD-subsystem.md](sdd/SDD-subsystem.md)
    - Autonomous routines → [SDD-auto.md](sdd/SDD-auto.md)
    - Vision (camera pose estimates, AprilTags) →
      [SDD-vision.md](sdd/SDD-vision.md)

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

Every Java source package in the library maps to one Software Design
Description (SDD):

| Source Package | Software Design Description |
| -------------- | --------------------------- |
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
| `com.team271.lib.api.vision.*`, `com.team271.lib.vendor.limelight.*`, `com.team271.lib.vendor.photonvision.*` | [SDD-vision.md](sdd/SDD-vision.md) |
| `com.team271.lib.api.trajectory.*`, `com.team271.lib.vendor.pathplanner.*`, `com.team271.lib.vendor.choreo.*` | [SDD-auto.md](sdd/SDD-auto.md) |

## Architecture Decision Records

Each Architecture Decision Record (ADR) captures one architectural
decision — its context, what was decided, why, and what it makes
easier or harder. Reading the ADRs is the fastest way to understand
why the library is built the way it is.

Statuses an ADR can have:

- **Proposed** — under discussion; not yet binding.
- **Accepted** — binding. Contributors **shall** follow the decision.
- **Superseded** — replaced by a later ADR, which the Superseded
  ADR points at. Accepted ADRs are never deleted; they are marked
  Superseded instead.

| ADR | Title | Status |
| --- | ----- | ------ |
| [ADR-001](adr/ADR-001-team271-lib-standalone-library.md) | Team271-Lib as a Standalone Library, Separate from Robot Projects | Accepted |
| [ADR-002](adr/ADR-002-java17-wpilib-gradlerio-toolchain.md) | Java 17 + WPILib + GradleRIO Toolchain | Accepted |
| [ADR-003](adr/ADR-003-layered-architecture.md) | Layered Architecture — api ← vendor/* ← hardware ← control ← subsystem ← auto | Accepted |
| [ADR-004](adr/ADR-004-explicit-instantiation-no-singletons.md) | Explicit Object Instantiation, No Singletons in Library Code | Accepted |
| [ADR-005](adr/ADR-005-passthrough-wrapper-not-wall.md) | Passthrough — Wrapper, Not Wall | Accepted |
| [ADR-006](adr/ADR-006-can-bus-abstraction.md) | Vendor-Neutral CAN Bus Abstraction | Accepted |
| [ADR-007](adr/ADR-007-vendor-neutral-vision-abstraction.md) | Vendor-Neutral Vision Abstraction in `api/vision/` | Accepted |
| [ADR-008](adr/ADR-008-ctre-phoenix6-primary-vendor.md) | CTRE Phoenix 6 as the Primary Motor/Sensor Vendor | Accepted |
| [ADR-009](adr/ADR-009-centralized-can-refresh.md) | Centralized Bulk CAN Refresh via CTREManager / HardwareManager | Accepted |
| [ADR-010](adr/ADR-010-desired-to-actual-state-pattern.md) | Desired-to-Actual State Pattern in Subsystems | Accepted |
| [ADR-011](adr/ADR-011-subsystem-exception-isolation.md) | Per-Subsystem Exception Isolation in SubsystemManager.forEachSafe() | Accepted |
| [ADR-012](adr/ADR-012-mandatory-timeouts-fail-safe.md) | Mandatory Timeouts with Fail-Safe + Driver Alert on All Waiting Operations | Accepted |
| [ADR-013](adr/ADR-013-composition-over-commands.md) | Composition over Commands — State-Machine + AutoMove Instead of WPILib Command-Based | Accepted |
| [ADR-014](adr/ADR-014-trajectory-following-vendors.md) | Trajectory-Following Vendors — PathPlanner and Choreo | Accepted |
| [ADR-015](adr/ADR-015-logged-nt-input-backed-tuning.md) | LoggedNTInput-Backed Tuning — No Magic Numbers in Docs | Accepted |
| [ADR-016](adr/ADR-016-advantagekit-logging.md) | AdvantageKit for Telemetry and Replay Logging | Accepted |
| [ADR-017](adr/ADR-017-junit5-hal-simulation-tests.md) | JUnit 5 + JaCoCo + HAL Simulation for Library Tests | Accepted |
| [ADR-018](adr/ADR-018-null-safety-annotation-policy.md) | Null-Safety Annotation Policy — JSpecify with NullAway Enforcement | Proposed |

## Planned ADRs

> **Status: Planned — Not Yet Implemented.**

Reserved slots for decisions already foreseen but not yet authored.
Each **shall** be written before (or alongside) the first change
that depends on it — not speculatively.

| Planned ADR | Scope |
| ----------- | ----- |
| Supply-chain / CVE response | How the library triages OWASP / GitHub dependency-review findings and which severities trigger an out-of-phase hotfix |
| Unlimited followers in `TransmissionBase` | Whether to lift the current 4-motor cap (1 leader + followers). Phoenix 6 itself imposes no limit; the cap is design-imposed and tied to pre-allocated control-request arrays. Change when a concrete mechanism (e.g., 6-wheel tank, exotic climber) needs it |

## Planned SDDs

> **Status: Planned — Not Yet Implemented.**

Reserved slots for design documents that don't yet exist. Each
**shall** be authored alongside (or after) its enabling ADR, when
the corresponding code ships.

None currently planned.

## What These Documents Do NOT Cover

Deliberately excluded to prevent duplication with authoritative sources:

| Topic | Authoritative Source |
| ----- | -------------------- |
| Coding rules, naming, formatting | [`../../coding-standard/README.md`](../../coding-standard/README.md) |
| Shared planning policy (SemVer, phase model, CI framework) | [`../../common/planning/`](../../common/planning/) |
| AI-behavior guardrails (planning, safety, docs, coding-standard, team271-lib scopes) | [`../../../.claude/rules/`](../../../.claude/rules/) |
| Git workflow, PR process, commit rules | `../../../CONTRIBUTING.md` (repository root) |
| Vendordep versions | `../../../vendordeps/*.json`, `../../../build.gradle` |
| Robot-project requirements | Each robot project's own planning docs |

## Scope of Planning Documents

Planning documents own **what** each layer must do and **how** the library
is structured. Coding rules own **how** to write the code. Robot projects own
**what the robot shall do**.
