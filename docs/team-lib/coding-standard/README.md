# Library-Specific Coding-Standard Notes

This folder holds **library-specific notes** layered on the shared
coding standard. The shared standard now lives in
[`../../coding-standard/`](../../coding-standard/) (adopted from the
project-template); the reusable-library rules once duplicated here
(`CODE-LIB-*`) are now part of that standard. The remaining file maps
the generic rules to concrete library classes.

> **Industry bridge.** Real software projects often keep a single
> shared coding standard *and* a project-specific "style guide" or
> "conventions" doc that shows how to apply the shared rules to
> the project's own classes and patterns. That's what these files
> are — the library's style guide layered on top of the shared
> standard.

## Contents

| File | Scope |
| ---- | ----- |
| [`coding-standard-teamlib-rules.md`](coding-standard-teamlib-rules.md) | **Retired** — the `CODE-LIB-*` rules now live in [`../../coding-standard/java/Standard-Library.md`](../../coding-standard/java/Standard-Library.md) plus the [supplement](../../coding-standard/team271-lib-supplement.md) |
| [`coding-standard-library-notes.md`](coding-standard-library-notes.md) | How the generic Team 271 coding-standard rules bind to specific library classes (`TObj`, `SubsystemManager`, `CTREManager`, etc.) |

Robot-project code templates (subsystem layout, constants organization,
input-driver pattern) live in
[`../../robot-yyyy/`](../../robot-yyyy/) alongside the project-level
coding-standard template — they are consumer-facing scaffolding, not
library content.
