<!-- markdownlint-disable MD013 -->
# Team 271 — Coding Standard

This folder holds the **shared coding standard** for every Team 271
Java project — the library, robot projects, and any standalone tool
we write in Java. It's split across a core document and topical
companion files so you can read just the part you need.

> **Industry bridge.** Professional software teams publish a
> *coding standard* as a formal document, often with rule IDs (like
> `CODE-SAF-008`) so code review comments can cite the exact rule.
> This coding standard uses the same pattern, letting reviewers say
> "this violates CODE-GEN-004" instead of arguing about style.

## Start here

- [`Team271-Software-Coding-Standard.md`](Team271-Software-Coding-Standard.md)
  — the core document. Read this first; it indexes all the
  companion files.

Then open whichever companion matches what you're changing
(the filename tells you the scope, e.g. `-Safety.md` for
timeouts and fail-safe behavior).

## Files in this folder

| File | Scope |
| ---- | ----- |
| [`Team271-Software-Coding-Standard.md`](Team271-Software-Coding-Standard.md) | Core: §1 Introduction, §2 Programming Language, §3 Source-code presentation, §4 Coding Guidelines (router to companions) |
| [`-General.md`](Team271-Software-Coding-Standard-General.md) | `CODE-GEN-*` — keywords, annotations, type safety, exceptions, garbage collection, concurrency |
| [`-Format.md`](Team271-Software-Coding-Standard-Format.md) | `CODE-FMT-*` — braces, parentheses, blank lines, line endings, imports |
| [`-Modules.md`](Team271-Software-Coding-Standard-Modules.md) | `CODE-MAF-*` — file and package organization |
| [`-Methods.md`](Team271-Software-Coding-Standard-Methods.md) | `CODE-FUN-*` — method naming, lifecycle, state machines |
| [`-Variables.md`](Team271-Software-Coding-Standard-Variables.md) | `CODE-VAR-*` — variable naming, initialization, magic numbers |
| [`-Control.md`](Team271-Software-Coding-Standard-Control.md) | `CODE-CTL-*` — `if`, `switch`, loops |
| [`-Comments.md`](Team271-Software-Coding-Standard-Comments.md) | `CODE-COM-*` — Javadoc, block, inline comments |
| [`-Debug.md`](Team271-Software-Coding-Standard-Debug.md) | `CODE-BUG-*` — telemetry, driver notifications, runtime tunability |
| [`-Safety.md`](Team271-Software-Coding-Standard-Safety.md) | `CODE-SAF-*` — timeouts, fail-safe, CAN, brownout |
| [`-Appendices.md`](Team271-Software-Coding-Standard-Appendices.md) | Reference tables (`final` keyword guide, unit conventions, garbage collection, etc.) |
| [`-Compliance.md`](Team271-Software-Coding-Standard-Compliance.md) | §5 Static analysis + tooling + §5.4 Code review checklist; enforcement matrix |

## How to cite a rule

Every rule has an identifier like `CODE-CTL-002`. Cite the ID in
review comments and pull-request descriptions — the ID is stable
even if the surrounding wording changes. Sub-rules use a suffix
letter (`CODE-CTL-002a`).
