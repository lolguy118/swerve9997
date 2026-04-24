# Documentation Layout

This folder contains **all the documentation** for Team271-Lib.
Because every season's robot project is a **fork** of this repo
([ADR-001](team-lib/planning/adr/ADR-001-team271-lib-standalone-library.md)),
the `docs/` folder is organised in three tiers so each tier has a
clear role after the fork: shared rules stay authoritative,
library-specific docs become frozen reference material in the robot
repo, and a project-specific tier is created by renaming the
`robot-yyyy/` scaffold.

```text
docs/
├── common/          ← rules that apply to every Team 271 FRC Java project
├── team-lib/        ← specific to this library
├── robot-yyyy/      ← scaffold renamed in place when a fork becomes a robot project
└── <project>/       ← the renamed robot-yyyy/, post-fork (edited in the robot repo)
```

## Start here

If you're new, open these in order:

1. [`team-lib/guides/start-here.md`](team-lib/guides/start-here.md)
   — the onboarding guide for library contributors.
2. [`team-lib/planning/README.md`](team-lib/planning/README.md) —
   the library's planning and design-document index (Software
   Development Plan, Software Design Descriptions, Architecture
   Decision Records, etc.).
3. [`common/README.md`](common/README.md) — the coding standard
   (how to write Java for Team 271 projects).

## common/

FRC Java policy. Anything here applies verbatim to every 271 FRC
project — the library and any season's robot code. This tier
survives forking unchanged.

Currently:

- [`common/`](common/) — coding standard (12 files: core + companions)
- [`common/planning/`](common/planning/) — shared planning framework
  (configuration-management policy, development-plan framework,
  verification-plan framework)

See [`common/README.md`](common/README.md) and
[`common/planning/README.md`](common/planning/README.md) for the indexes.

## team-lib/

Specific to the **library**. Planning docs — Software Development
Plan (SDP), Software Requirements Specification (SRS), Software
Verification Plan (SVP), and Software Configuration Management Plan
(SCMP) — fill in the common planning framework with the library's
specific choices. Architecture Decision Records (ADRs) record
library-wide decisions. Software Design Descriptions (SDDs) describe
library package internals. Guides walk through library architecture
and contributor setup. Prompts and internal diagrams support the
library's review and maintenance workflow.

After a fork, this tier becomes **frozen reference material** in the
robot repo — it describes the library state at fork time. Any
library-code modifications a robot team makes during the season live
in the robot repo only; they do not propagate back to Team271-Lib
`main` unless explicitly upstreamed between seasons
(see [ADR-001 Consequences](team-lib/planning/adr/ADR-001-team271-lib-standalone-library.md#consequences)).

See [`team-lib/planning/README.md`](team-lib/planning/README.md) for
the planning-doc map.

## robot-yyyy/

Scaffolding that becomes the robot project's documentation tier
after the fork. An init script under `tools/` renames this directory
to `docs/<project>/` (e.g., `docs/robot-2026/`), renames the
`com.team271.libtest` Java package, and substitutes project-name
placeholders.

See [`robot-yyyy/README.md`](robot-yyyy/README.md) for contents and
the rename workflow.

## `<project>/` (robot projects, post-fork)

After the fork-and-rename, `docs/robot-yyyy/` becomes
`docs/<project>/` in the robot repo. This tier contains:

- The robot's own SDP, SRS, SVP, and SCMP
- The robot's ADRs (decisions specific to that season's mechanisms)
- The robot's SDDs (subsystems unique to that robot)
- The robot's guides
- The robot's project-level coding standard (based on
  [`robot-yyyy/coding-standard.md`](robot-yyyy/coding-standard.md)),
  covering `CODE-<PROJECT>-NNN` rules and any deviations from the
  inherited common / Team271-Lib standards

This is the only tier that gets actively edited in the robot repo;
`common/` and `team-lib/` are reference-only after the fork.

## Pointers

- Coding standard → [`common/`](common/)
- Library planning + design → [`team-lib/planning/`](team-lib/planning/)
- Library contributor guides → [`team-lib/guides/`](team-lib/guides/)
- Architecture diagram → [`team-lib/internal/team271-lib-dependency-diagram.mmd`](team-lib/internal/team271-lib-dependency-diagram.mmd)
- AI/LLM routing index → [`../CLAUDE.md`](../CLAUDE.md)
- Contributor workflow → [`../CONTRIBUTING.md`](../CONTRIBUTING.md)
