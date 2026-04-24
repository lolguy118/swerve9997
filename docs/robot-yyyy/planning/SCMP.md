<!-- TEMPLATE FOR FORKED ROBOT PROJECTS -- scaffold file renamed in
     place to docs/<project>/planning/SCMP.md by tools/init-robot.sh
     during project initialization. Record the Team271-Lib tag the
     project was forked from in §3 Versioning. This banner is
     stripped by the init script. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Software Configuration Management Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-<PROJECT>-SCMP |
| Revision | 0.1 |
| Date | YYYY-MM-DD |
| Status | Draft |

This plan records **<Project>**'s specific configuration-management
state: version scheme, Team271-Lib fork-origin tag, baseline
control, and any deviations from the shared CM policy.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose

Project-specific state layered on top of the shared CM policy: the
Team271-Lib tag this project was forked from, how the project is
versioned, and any deviations.

## 2. Authoritative CM Documents

| Document | CM Concern |
| -------- | ---------- |
| [`../../common/planning/configuration-management.md`](../../common/planning/configuration-management.md) | Shared policy: SemVer, branch model, deviation-tracking row format |
| `../../../CONTRIBUTING.md` (robot repo) | Branch naming, PR process, commit rules |
| `build.gradle` (robot repo) | Robot version, Gradle toolchain |
| `vendordeps/*.json` (robot repo) | WPILib + vendor versions (Phoenix 6, AdvantageKit, PathPlanner, Choreo) |

## 3. Project Versioning

Recommended pattern: `v<YYYY>.<MINOR>.<PATCH>`, where `<YYYY>` is the
season year. Example events:

| Event | Tag |
| ----- | --- |
| Season baseline (competition-ready code freeze) | `v<YYYY>.0.0` |
| Competition hotfix | `v<YYYY>.0.P` |
| Post-season cleanup / offseason tag | `v<YYYY>.N.P` |

## 4. Fork Origin

This project was forked from Team271-Lib at the tag recorded below.
Library code lives **directly in this repo** after the fork — it is
not tracked as a versioned dependency
(see [ADR-001](../../team-lib/planning/adr/ADR-001-team271-lib-standalone-library.md)).
Record the origin tag so future maintainers know which library
baseline the project started from.

| Field | Value |
| ----- | ----- |
| Team271-Lib fork tag | `v<YYYY>.N.P` |
| Fork date | YYYY-MM-DD |

Mid-season library fixes made in this repo do **not** auto-propagate
back to Team271-Lib `main`. A fix worth sharing forward is upstreamed
by opening a PR against Team271-Lib (typically between seasons).

## 5. Deviation Tracking

Deviations from the shared CM policy, the library's SCMP, or any
inherited ADR are recorded here. Each deviation **shall** be approved
via pull request.

| Inherited rule | Project deviation | Reason | Approver | Date |
| -------------- | ----------------- | ------ | -------- | ---- |
| (none yet) | | | | |
