<!-- TEMPLATE FOR FORKED ROBOT PROJECTS -- scaffold file renamed in
     place to docs/<project>/planning/README.md by
     tools/init-robot.sh during project initialization. Replace
     <Project>/<PROJECT> placeholders in the body; this banner is
     stripped by the init script. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# <Project> — Planning and Design Documents

This folder holds the formal planning and design documents for the
**<Project>** robot. If you're new, this folder is where you learn
what the robot does, how it's built, and why it's built the way it is.

Each kind of document answers a different question:

- **What** the robot shall do → [SRS.md](SRS.md) — Software
  Requirements Specification
- **How** the robot code is developed → [SDP.md](SDP.md) — Software
  Development Plan
- **How** the robot is tested → [SVP.md](SVP.md) — Software
  Verification Plan
- **How** versions and releases are managed → [SCMP.md](SCMP.md) —
  Software Configuration Management Plan
- **How** each subsystem is designed → [sdd/](sdd/) — Software Design
  Descriptions (SDDs), one per subsystem
- **Why** we made big robot-scope choices → [adr/](adr/) —
  Architecture Decision Records (ADRs)

## Start here

If you're new to the robot code, read in this order:

1. [`../README.md`](../README.md) — this project's doc overview.
2. [`../coding-standard.md`](../coding-standard.md) — project-level
   coding rules (`CODE-<PROJECT>-*`) and deviations.
3. The SDD for whichever subsystem you're working on (see the
   Subsystem-to-SDD Map below).

## Shared framework (common)

The shared planning framework (SemVer policy, FRC-calendar phase
model, test-level structure, CI gate pattern) lives under
[`../../common/planning/`](../../common/planning/). The documents in
this directory concretize that framework with robot-specific choices
— they **reference** the common policy rather than duplicate it.

## Document Map

| Document | Type | Purpose |
| -------- | ---- | ------- |
| [SDP.md](SDP.md) | Software Development Plan | Project phases, milestones, toolchain pinning, deviations |
| [SRS.md](SRS.md) | Software Requirements Specification | Functional and non-functional requirements — what the robot shall do |
| [SVP.md](SVP.md) | Software Verification Plan | Test strategy for this robot, coverage targets, on-field validation |
| [SCMP.md](SCMP.md) | Software Configuration Management Plan | Robot version scheme, Team271-Lib fork-origin tag, deviation tracking |
| [sdd/](sdd/) | Software Design Descriptions | One SDD per robot subsystem (Drivebase, Shooter, Intake, Vision, ...) |
| [adr/](adr/) | Architecture Decision Records | Robot-scope decisions (mechanism choice, drive geometry, controller mapping rationale) |

## Subsystem-to-SDD Map

Every robot subsystem maps to one Software Design Description (SDD).
Fill this table in as subsystem SDDs are authored.

| Subsystem | Software Design Description |
| --------- | --------------------------- |
| `<ExampleSubsystem>` | `sdd/SDD-<example-subsystem>.md` |

## Inherited library decisions (informational)

The robot code inherits Team271-Lib from the fork, and several of
the library's architectural decisions govern patterns that the
robot code must follow. These are listed here so robot contributors
know what's already decided for them (via the inherited library
code) vs. what still needs a project-level decision. Each link
points into the inherited library planning tier.

- **[ADR-010 — Desired-to-Actual State Pattern](../../team-lib/planning/adr/ADR-010-desired-to-actual-state-pattern.md)**
  — every subsystem separates `mDesired*` from `m*` state; outputs
  apply in `robotPeriodicAfter()`. Shown concretely in
  [`../subsystem-template.md`](../subsystem-template.md) and bound
  to project code by `CODE-<PROJECT>-009`.
- **[ADR-012 — Mandatory Timeouts with Fail-Safe](../../team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md)**
  — every waiting operation has a named timeout constant, a
  fail-safe transition, and a driver notification. Normative form
  lives in
  [`CODE-SAF-002c`](../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md#code-saf-002----motor-safety)
  — robot code inherits it.
- **[ADR-013 — Composition over Commands](../../team-lib/planning/adr/ADR-013-composition-over-commands.md)**
  — robot autonomous routines use the library's `AutoMove`
  composition, not WPILib Command-Based. A project that wanted
  Command-Based would record it in §3 "Deviations from Inherited
  Standards" of [`../coding-standard.md`](../coding-standard.md).
- **[ADR-015 — LoggedNTInput-Backed Tuning](../../team-lib/planning/adr/ADR-015-logged-nt-input-backed-tuning.md)**
  — all tunable values live in code (`Constants.java`) and surface
  to the dashboard via `LoggedNTInput`. No numeric tunables in
  design docs.
- **[ADR-016 — AdvantageKit for Telemetry and Replay Logging](../../team-lib/planning/adr/ADR-016-advantagekit-logging.md)**
  — every subsystem emits telemetry via `Logger.recordOutput()`.
  Post-match `.wpilog` captures support replay.

If the robot needs to deviate from any inherited decision, record
the deviation in [SDP.md](SDP.md) §7 ("Deviations from Inherited
Standards") or in a project-scope ADR under [adr/](adr/).

## Planned ADRs

> **Status: Planned — Not Yet Implemented.**

Reserved slots for decisions already foreseen but not yet authored.

| Planned ADR | Scope |
| ----------- | ----- |
| (none yet) | |

## Planned SDDs

> **Status: Planned — Not Yet Implemented.**

| Planned SDD | Subsystem |
| ----------- | --------- |
| (none yet) | |

## What these documents do NOT cover

Deliberately excluded to prevent duplication with authoritative
sources:

| Topic | Authoritative Source |
| ----- | -------------------- |
| Coding rules, naming, formatting | [`../../common/coding-standard/Team271-Software-Coding-Standard.md`](../../common/coding-standard/Team271-Software-Coding-Standard.md) |
| Shared planning policy (SemVer, phase model, CI framework) | [`../../common/planning/`](../../common/planning/) |
| Library internals (CTREManager, SubsystemManager, AutoMove, ...) | [`../../team-lib/planning/`](../../team-lib/planning/) |
| Library-specific coding rules (`CODE-LIB-*`) | [`../../team-lib/coding-standard/coding-standard-teamlib-rules.md`](../../team-lib/coding-standard/coding-standard-teamlib-rules.md) |
| Project-level coding rules (`CODE-<PROJECT>-*`) | [`../coding-standard.md`](../coding-standard.md) |
| Git workflow, PR process | The robot project's own `CONTRIBUTING.md` |
