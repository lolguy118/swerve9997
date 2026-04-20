<!-- markdownlint-disable MD013 -->
# Common Docs

Framework-agnostic policy for every Team271 Java/FRC project. The library
authors `docs/common/` in this repo; robot projects vendor it (submodule,
subtree, or copy) into their own `docs/common/` for reference.

Nothing here is specific to any one product. Library-specific docs live in
[`../team-lib/`](../team-lib/); project-specific docs live in the consuming
robot's `docs/<project>/`.

## Coding Standard

Normative rules for all Java code across 271 projects. Split across a
core document and topical companions; the core indexes the rest.

| File | Scope |
| ---- | ----- |
| [`Team271-Software-Coding-Standard.md`](Team271-Software-Coding-Standard.md) | Core: §1 Introduction, §2 Programming Language, §3 Source Code Presentation, §4 Coding Guidelines (router) |
| [`-General.md`](Team271-Software-Coding-Standard-General.md) | `CODE-GEN-*` — keywords, annotations, type safety, exceptions, GC |
| [`-Format.md`](Team271-Software-Coding-Standard-Format.md) | `CODE-FMT-*` — braces, parens, blank lines, line endings, imports |
| [`-Modules.md`](Team271-Software-Coding-Standard-Modules.md) | `CODE-MAF-*` — file and package organisation |
| [`-Methods.md`](Team271-Software-Coding-Standard-Methods.md) | `CODE-FUN-*` — method naming, lifecycle, state machines |
| [`-Variables.md`](Team271-Software-Coding-Standard-Variables.md) | `CODE-VAR-*` — variable naming, initialization, magic numbers |
| [`-Control.md`](Team271-Software-Coding-Standard-Control.md) | `CODE-CTL-*` — if / switch / loops |
| [`-Comments.md`](Team271-Software-Coding-Standard-Comments.md) | `CODE-COM-*` — Javadoc, block, inline comments |
| [`-Debug.md`](Team271-Software-Coding-Standard-Debug.md) | `CODE-BUG-*` — LoggedNTInput, Elastic, debug output |
| [`-Safety.md`](Team271-Software-Coding-Standard-Safety.md) | `CODE-SAF-*` — timeouts, fail-safe, CAN, brownout |
| [`-Appendices.md`](Team271-Software-Coding-Standard-Appendices.md) | Reference tables (`final` keyword guide, unit conventions, GC, etc.) |
| [`-Compliance.md`](Team271-Software-Coding-Standard-Compliance.md) | §5 Static Analysis + Tooling + §5.4 Code Review Checklist; enforcement matrix |

### Library-specific companions

Rules in the common standard sometimes cite concrete library APIs
(`LoggedNTInput`, `Elastic`, `Subsystem`, etc.). Those applications live
in `docs/team-lib/` since they're specific to Team271-Lib:

- [`../team-lib/coding-standard-templates.md`](../team-lib/coding-standard-templates.md)
  — file and class templates for robot-project code that consumes the
  library (Appendices F, G)
- [`../team-lib/coding-standard-library-notes.md`](../team-lib/coding-standard-library-notes.md)
  — concrete API mappings for each common rule that names a library
  facility (e.g., which class implements the "tuning infrastructure"
  concept from `CODE-BUG-004`)

## Conventions

- **Normative language.** "shall" = required, "should" = recommended,
  "may" = permitted.
- **Rule IDs `CODE-<GROUP>-NNN`.** Cite them in review comments and PRs.
  Suffixed letters (`CODE-CTL-002a`) call out sub-rules.
- **Project overrides.** A consuming project that needs to deviate writes
  its own `docs/<project>/overrides.md` listing the deviation and rationale.
  The rule in `common/` is still authoritative everywhere else.

## What does *not* live here

- Library architecture — [`../team-lib/`](../team-lib/)
- Robot-specific subsystems or auto routines — in the robot project's repo
- CI / hook configuration — `.claude/hooks/`, `.github/workflows/`,
  `build.gradle`
