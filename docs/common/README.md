<!-- markdownlint-disable MD013 -->
# Common Docs

This folder holds the **shared policy** that applies to every
Team 271 FRC (FIRST Robotics Competition) Java project — the
library and any season's robot code. Anything in here is portable:
it should apply unchanged to every FRC project the team ships.

> **Industry bridge.** Professional software teams ship a *coding
> standard* (how code must be written) separately from *design docs*
> (what's being built). This folder holds the coding standard and
> the shared planning framework; product-specific design lives in
> the consuming project's own doc tier.

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
| [`coding-standard/`](coding-standard/) | Normative rules for all FRC Java code — core standard + topical companions, split so you can read only the parts you need |
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

## Portability

Common docs **shall** be self-contained. They do not link to — or
name specific artifacts in — the `team-lib/` or `<project>/` tiers.
This keeps `common/` usable verbatim in any consuming FRC project,
including robot projects that don't vendor `team-lib/`.

Applied in practice:

- **No cross-tier hyperlinks.** Links must resolve within `common/`
  or out to sibling top-level paths (`../CLAUDE.md`,
  `../CONTRIBUTING.md`).
- **No bare artifact references** such as "per ADR-012" or "see
  SDD-manipulator." Those names are library- or project-specific.
- **Abstract phrasing.** Say "per the project's safety-shutdown
  policy" and let the consuming project bind that term to its own
  ADR / SDD / code in its own doc tier.

## What does *not* live here

- Architecture, ADRs, SDDs for a particular product (library, robot)
- Concrete class names, vendor APIs, specific coverage thresholds,
  version numbers
- Continuous-integration / hook / build configuration (`.claude/hooks/`,
  `.github/workflows/`, `build.gradle`)
