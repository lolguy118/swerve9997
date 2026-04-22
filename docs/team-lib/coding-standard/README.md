<!-- markdownlint-disable MD013 -->
# Library-Specific Coding-Standard Notes

This folder holds **library-specific extensions** to the shared
coding standard. The shared standard (in
[`../../common/coding-standard/`](../../common/coding-standard/))
covers rules that apply to every Team 271 Java project; the files
here translate those generic rules into concrete library patterns
and supply ready-to-copy templates for robot projects.

> **Industry bridge.** Real software projects often keep a single
> shared coding standard *and* a project-specific "style guide" or
> "conventions" doc that shows how to apply the shared rules to
> the project's own classes and patterns. That's what these files
> are — the library's style guide layered on top of the shared
> standard.

## Contents

| File | Scope |
| ---- | ----- |
| [`coding-standard-teamlib-rules.md`](coding-standard-teamlib-rules.md) | Team271-Lib-specific coding rules (`CODE-LIB-NNN`) for library source code — architectural patterns from ADRs expressed as enforceable rules |
| [`coding-standard-library-notes.md`](coding-standard-library-notes.md) | How the generic Team 271 coding-standard rules bind to specific library classes (`TObj`, `SubsystemManager`, `CTREManager`, etc.) |

Robot-project code templates (subsystem layout, constants organization, input-driver pattern) live in
[`../../project-template/`](../../project-template/) alongside the project-level coding-standard template — they are consumer-facing scaffolding, not library content.
