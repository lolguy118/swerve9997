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

| File | Purpose |
| ---- | ------- |
| [`coding-standard.md`](coding-standard.md) | Project-level coding standard (`CODE-<PROJECT>-NNN` rules, deviations from inherited standards, rule-precedence hierarchy) |
| [`subsystem-template.md`](subsystem-template.md) | Java code template for a robot-project subsystem — singleton pattern, lifecycle hooks, Globals registration, file organization |
| [`constants-template.md`](constants-template.md) | Java code template for a robot-project `Constants.java` — CAN bus names, nested per-subsystem constant classes |
| [`input-driver-template.md`](input-driver-template.md) | Java code template for the operator-input class (`InputDriver`) — extends a library input base, connection-guarded semantic getters |

## How to use

1. Copy the files in this folder into the consuming robot's repo at
   `docs/<project>/` (where `<project>` is the robot's name, season
   year, or mnemonic — e.g., `docs/nike-2026/`).
2. Replace `<Project>` / `<PROJECT>` placeholders with the project
   name.
3. Delete any example rules that do not apply; add project-specific
   rules using the `CODE-<PROJECT>-NNN` numbering.
4. Vendor `docs/common/` and `docs/team-lib/` alongside
   `docs/<project>/` per the
   [repo's docs layout](../README.md).

## Future additions

Additional project-tier scaffolds (empty SDP/SRS/SVP/SCMP stubs,
ADR/SDD index templates, guide placeholders) will land here as they
are needed. Extend the Contents table above when adding files.
