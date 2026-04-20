# Software Configuration Management Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SCMP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

This plan records Team271-Lib's specific configuration management
state. It builds on the shared CM policy (see §2 Authoritative CM
Documents) and carries only library-specific deltas.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose

Library-specific state for the shared CM policy: current version,
authoritative sources, vendordep list, and deviations.

## 2. Authoritative CM Documents

| Document | CM Concern |
| -------- | ---------- |
| [`../../common/planning/configuration-management.md`](../../common/planning/configuration-management.md) | Shared policy: SemVer, branch model, vendordep upgrade process, baseline control, change control, deviation-tracking row format |
| `../../../CONTRIBUTING.md` (repository root) | Branch naming, PR process, commit rules, linting workflow |
| `../../../build.gradle` | Library version, Gradle toolchain version |
| `../../../vendordeps/*.json` | Vendordep versions (Phoenix 6, WPILib, AdvantageKit, PathPlanner) |

## 3. Library Versioning

`YYYY` in the version format is the current FRC season year; the
version number is authoritative in `build.gradle`.

Concrete tag events for the library's 2026 → 2027 cycle:

| Event | Tag |
| ----- | --- |
| Offseason start | `v2026.N.P` (prior-season final) |
| Offseason API freeze | `v2027.0.0` |
| Competition hotfix | `v2027.0.P` |

## 4. Vendordep Management (Team271-Lib specifics)

The [`.github/workflows/vendordep-freshness.yml`](../../../.github/workflows/vendordep-freshness.yml)
workflow implements the automated freshness tracking described in the
shared policy. It runs weekly (Mondays 13:00 UTC) and on manual
dispatch, fetches each vendordep `jsonUrl`, and opens/updates a
"Vendordep freshness check" issue when upstream moves ahead.

Currently tracked vendordeps:

- `Phoenix6-frc<year>-latest.json` (CTRE Phoenix 6)
- `AdvantageKit.json` (AdvantageKit)
- `PathplannerLib.json` (PathPlanner)
- `WPILibNewCommands.json` (WPILib commands)

The [`.github/workflows/dependency-submission.yml`](../../../.github/workflows/dependency-submission.yml)
workflow submits the Gradle dependency graph on every push to `main`.

The upgrade procedure itself is inherited from the shared policy
(see §2 Authoritative CM Documents).

## 5. Baseline Control and Change Control

Library-specific delta on top of the shared baseline / change-control
rules: every non-trivial PR **shall** carry `/lib-review` output
alongside the maintainer approval required by the framework.

## 6. Deviation Tracking

Library-specific deviations from the shared coding standard are
logged in [SDP.md §7](SDP.md#7-deviations-from-scs).
