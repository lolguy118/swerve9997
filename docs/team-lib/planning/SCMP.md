# Software Configuration Management Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SCMP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

This plan records Team271-Lib's specific configuration management state
and cites the shared policy in
[`../../common/planning/configuration-management.md`](../../common/planning/configuration-management.md)
for the framework (SemVer, branch model, vendordep upgrade process,
baseline control, change control, deviation tracking).

## 1. Purpose

Library-specific state for the shared CM policy: current version,
authoritative sources, vendordep list, and deviations.

## 2. Authoritative CM Documents

| Document | CM Concern |
| -------- | ---------- |
| `../../../CONTRIBUTING.md` (repository root) | Branch naming, PR process, commit rules, linting workflow |
| `../../../build.gradle` | Library version, Gradle toolchain version |
| `../../../vendordeps/*.json` | Vendordep versions (Phoenix 6, WPILib, AdvantageKit, PathPlanner) |

## 3. Library Versioning

Team271-Lib follows the SemVer policy in the shared framework
(see [`configuration-management.md §1`](../../common/planning/configuration-management.md#1-versioning-semantic-versioning)).
Version format is `YYYY.MINOR.PATCH` with `YYYY` set to the current
FRC season year. Version is authoritative in `build.gradle`.

Tag events (per [SDP §8](SDP.md)):

- Offseason start → tag prior season final (e.g., `v2026.3.2`).
- Preseason API freeze → tag next-season MINOR=0 (e.g., `v2027.0.0`).
- Each competition hotfix → tag patch (e.g., `v2027.0.1`).

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

For the upgrade procedure itself, follow
[`configuration-management.md §4`](../../common/planning/configuration-management.md#4-vendor-dependency-management).

## 5. Baseline Control and Change Control

Team271-Lib follows the shared baseline and change-control rules in
[`configuration-management.md §2–§3`](../../common/planning/configuration-management.md#2-baseline-control).
The library additionally requires `/lib-review` output on any
non-trivial PR.

## 6. Deviation Tracking

Library-specific deviations from the shared coding standard are logged
in [SDP.md §9](SDP.md#9-deviations-from-scs). Format per
[`configuration-management.md §5`](../../common/planning/configuration-management.md#5-deviation-tracking).
