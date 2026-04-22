# Robot Project Template (`robot-yyyy/`)

Scaffolding for a consuming robot project's `docs/<project>/` tier.
A robot project that depends on Team271-Lib copies the files in this
folder into `docs/<project>/` in its own repository — typically
`docs/robot-2026/`, `docs/robot-2027/`, etc., replacing `yyyy` with
the season year — and fills in the placeholders.

> This folder is a template, not a doc tier. Files here are not
> loaded, vendored, or linked from the library's own docs outside of
> this README. They exist so that a new robot project has a
> well-defined starting point for its project-level documentation.

## Contents

### Code templates (project root)

| File | Purpose |
| ---- | ------- |
| [`coding-standard.md`](coding-standard.md) | Project-level coding standard (`CODE-<PROJECT>-NNN` rules, deviations from inherited standards, rule-precedence hierarchy) |
| [`subsystem-template.md`](subsystem-template.md) | Java code template for a robot-project subsystem — singleton pattern, lifecycle hooks, Globals registration, file organization |
| [`constants-template.md`](constants-template.md) | Java code template for a robot-project `Constants.java` — CAN bus names, nested per-subsystem constant classes |
| [`input-driver-template.md`](input-driver-template.md) | Java code template for the operator-input class (`InputDriver`) — extends a library input base, connection-guarded semantic getters |

### Planning scaffolds ([`planning/`](planning/))

| File | Purpose |
| ---- | ------- |
| [`planning/README.md`](planning/README.md) | Planning-doc index, document map, subsystem-to-SDD map, inherited-library-decisions pointer |
| [`planning/SDP.md`](planning/SDP.md) | Project Software Development Plan template (phases, milestones, pin overrides, deviations) |
| [`planning/SRS.md`](planning/SRS.md) | Project Software Requirements Specification template (functional, non-functional, per-subsystem, traceability) |
| [`planning/SVP.md`](planning/SVP.md) | Project Software Verification Plan template (test levels, coverage targets, CI gates) |
| [`planning/SCMP.md`](planning/SCMP.md) | Project Software Configuration Management Plan template (versioning, library pinning, deviations) |
| [`planning/adr/README.md`](planning/adr/README.md) | ADR index + template for project-scope architectural decisions |
| [`planning/sdd/README.md`](planning/sdd/README.md) | SDD index + nine-section template pointer for per-subsystem design descriptions |

### Guides and prompts

| File | Purpose |
| ---- | ------- |
| [`guides/README.md`](guides/README.md) | Project guides index (onboarding, mechanism tuning, driver practice) |
| [`prompts/README.md`](prompts/README.md) | Optional project-scope AI review prompts (most projects don't need this) |

## How to use

1. Copy the entire contents of this folder (code templates, and the
   `planning/`, `guides/`, `prompts/` subdirectories) into the
   consuming robot's repo at `docs/<project>/` (where `<project>`
   is the robot's name, season year, or mnemonic — e.g.,
   `docs/nike-2026/`).
2. Replace `<Project>` / `<PROJECT>` placeholders with the project
   name.
3. Delete any example rules that do not apply; add project-specific
   rules using the `CODE-<PROJECT>-NNN` numbering.
4. Vendor `docs/common/` and `docs/team-lib/` alongside
   `docs/<project>/` per the
   [repo's docs layout](../README.md).
5. Fill in the planning templates (SDP, SRS, SVP, SCMP) with concrete
   project values. Keep the inherited-decisions section in
   `planning/README.md` up to date if the library version changes.
