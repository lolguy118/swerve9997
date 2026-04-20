<!-- markdownlint-disable MD013 -->
# Common Docs

This folder holds the **shared policy** that applies to every
Team 271 Java / FRC (FIRST Robotics Competition) project — the
library, any season's robot code, and any standalone tool the team
writes. Anything in here is portable: it should apply unchanged to
every project.

> **Industry bridge.** Professional software teams ship a *coding
> standard* (how code must be written) separately from *design
> docs* (what's being built). This folder holds our coding standard;
> `team-lib/planning/` holds the design docs. Keeping them separate
> is what real-world projects do.

## Start here

- [`Team271-Software-Coding-Standard.md`](Team271-Software-Coding-Standard.md)
  — the core coding standard. Read this first.
- Then pick whichever companion file (below) matches what you're
  changing.

## Coding Standard

Normative rules for all Java code. Split across a core document and
topical companions; the core indexes the rest.

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
| [`-Debug.md`](Team271-Software-Coding-Standard-Debug.md) | `CODE-BUG-*` — telemetry, driver notifications, runtime tunability |
| [`-Safety.md`](Team271-Software-Coding-Standard-Safety.md) | `CODE-SAF-*` — timeouts, fail-safe, CAN, brownout |
| [`-Appendices.md`](Team271-Software-Coding-Standard-Appendices.md) | Reference tables (`final` keyword guide, unit conventions, GC, etc.) |
| [`-Compliance.md`](Team271-Software-Coding-Standard-Compliance.md) | §5 Static Analysis + Tooling + §5.4 Code Review Checklist; enforcement matrix |

## Conventions

- **Normative language.** "shall" = required, "should" = recommended,
  "may" = permitted.
- **Rule IDs `CODE-<GROUP>-NNN`.** Cite them in review comments and PRs.
  Suffixed letters (`CODE-CTL-002a`) call out sub-rules.
- **Abstract phrasing.** When a rule refers to a project facility (e.g.,
  "the project's driver-notification facility"), the consuming project
  is expected to document the concrete binding in its own docs tier.
- **Project overrides.** A consuming project that needs to deviate
  writes its own `overrides.md` listing the deviation and rationale.
  The rule here is still authoritative everywhere else.

## What does *not* live here

- Architecture, ADRs, SDDs for a particular product (library, robot)
- Concrete class names, vendor APIs, specific coverage thresholds,
  version numbers
- CI / hook / build configuration (`.claude/hooks/`,
  `.github/workflows/`, `build.gradle`)
