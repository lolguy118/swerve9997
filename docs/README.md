<!-- markdownlint-disable MD013 -->
# Docs Layout

This directory is organized in three tiers so Team271-Lib and every robot
project that consumes it share a common doc surface without forking or
copy-pasting policy.

```text
docs/
├── common/       ← applies to any Team271 Java/FRC project
├── team-lib/     ← specific to this library's architecture
└── <project>/    ← specific to the robot project using the library
                    (lives only in that robot's repo, not here)
```

## common/

Framework-agnostic policy. Anything here applies verbatim to every 271
project — the library, a season robot, a dev-tool, anything else we write
in Java.

Currently:

- [`common/`](common/) — coding standard (12 files: core + companions)
- [`common/planning/`](common/planning/) — shared planning framework
  (configuration-management policy, development-plan framework,
  verification-plan framework)

See [`common/README.md`](common/README.md) and
[`common/planning/README.md`](common/planning/README.md) for the indexes.

## team-lib/

Specific to the **library**. Planning docs (SDP, SRS, SVP, SCMP)
concretize the common planning framework with the library's specific
choices. ADRs record library-wide decisions. SDDs describe library
package internals. Guides walk through library architecture and
contributor setup. Prompts and internal diagrams support the library's
review and maintenance workflow.

See [`team-lib/planning/README.md`](team-lib/planning/README.md) for
the planning-doc map.

## `<project>/` (robot projects)

A robot project that depends on Team271-Lib creates its own
`docs/<project>/` directory in the robot's repo. Inside it belong:

- The robot's own SDP/SRS/SVP/SCMP
- The robot's ADRs (decisions specific to that season's mechanisms)
- The robot's SDDs (subsystems unique to that robot)
- The robot's guides
- Deviations from the common coding standard, if any

## How a robot project uses this folder

The robot project should see this folder's `common/` and `team-lib/` as
**reference-only**. Two practical options:

1. **Git submodule or subtree** — vendor the `docs/common/` and
   `docs/team-lib/` directories from this repo into the robot repo. Keeps
   them in sync with whatever version of the library the robot depends on.
2. **Copy on pinning** — copy the docs matching the library version the
   robot ships with. Simple but drifts over time; acceptable for short-lived
   projects.

Either way, the robot's own `docs/<project>/` is authored fresh in the
robot repo and is the only tier that gets edited there.

## Pointers

- Coding standard → [`common/`](common/)
- Library planning + design → [`team-lib/planning/`](team-lib/planning/)
- Library contributor guides → [`team-lib/guides/`](team-lib/guides/)
- Architecture diagram → [`team-lib/internal/team271-lib-dependency-diagram.mmd`](team-lib/internal/team271-lib-dependency-diagram.mmd)
- AI/LLM routing index → [`../CLAUDE.md`](../CLAUDE.md)
- Contributor workflow → [`../CONTRIBUTING.md`](../CONTRIBUTING.md)
