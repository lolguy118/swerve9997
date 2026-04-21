<!-- markdownlint-disable MD013 -->
# Common Docs

This folder holds the **shared policy** that applies to every
Team 271 Java / FRC (FIRST Robotics Competition) project — the
library, any season's robot code, and any standalone tool the team
writes. Anything in here is portable: it should apply unchanged to
every project.

> **Industry bridge.** Professional software teams ship a *coding
> standard* (how code must be written) separately from *design docs*
> (what's being built). This folder holds the coding standard and
> the shared planning framework; library-specific design lives in
> [`../team-lib/`](../team-lib/).

## Start here

- [`coding-standard/README.md`](coding-standard/README.md) — the
  Team 271 Java coding standard (core document + 11 companion files).
- [`planning/README.md`](planning/README.md) — the shared planning
  framework (development plan, verification plan, configuration
  management).
- [`guides/README.md`](guides/README.md) — generic developer guides
  (currently: development setup).

## What's in this folder

| Path | Scope |
| ---- | ----- |
| [`coding-standard/`](coding-standard/) | Normative rules for all Java code — core standard + topical companions, split so you can read only the parts you need |
| [`planning/`](planning/) | Shared planning framework — Software Development Plan, Software Verification Plan, Software Configuration Management Plan frameworks |
| [`guides/`](guides/) | Cross-project developer guides (development setup, etc.) |

## Conventions

- **Normative language.** `shall` = required, `should` = recommended,
  `may` = permitted. Full definitions in
  [`planning/README.md#normative-keywords`](planning/README.md#normative-keywords).
- **Rule IDs `CODE-<GROUP>-NNN`.** Cite them in review comments and
  pull requests (PRs). Suffixed letters (`CODE-CTL-002a`) call out
  sub-rules.
- **Abstract phrasing.** When a rule refers to a project facility
  (e.g., "the project's driver-notification facility"), the consuming
  project is expected to document the concrete binding in its own
  docs tier.
- **Project overrides.** A consuming project that needs to deviate
  writes its own `overrides.md` listing the deviation and rationale.
  The rule here is still authoritative everywhere else.

## What does *not* live here

- Architecture, ADRs, SDDs for a particular product (library, robot)
- Concrete class names, vendor APIs, specific coverage thresholds,
  version numbers
- Continuous-integration / hook / build configuration (`.claude/hooks/`,
  `.github/workflows/`, `build.gradle`)
