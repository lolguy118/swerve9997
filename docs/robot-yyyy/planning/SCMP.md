<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/planning/SCMP.md in the robot's own repository. -->
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
state: version scheme, library pinning, baseline control, and any
deviations from the shared CM policy.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose

Project-specific state layered on top of the shared CM policy: which
library version the project pins, how the project is versioned, and
any deviations.

## 2. Authoritative CM Documents

| Document | CM Concern |
| -------- | ---------- |
| [`../../common/planning/configuration-management.md`](../../common/planning/configuration-management.md) | Shared policy: SemVer, branch model, deviation-tracking row format |
| `../../../CONTRIBUTING.md` (robot repo) | Branch naming, PR process, commit rules |
| `build.gradle` (robot repo) | Robot version, Gradle toolchain |
| `vendordeps/*.json` (robot repo) | Library + WPILib + vendor versions |

## 3. Project Versioning

Recommended pattern: `v<YYYY>.<MINOR>.<PATCH>`, where `<YYYY>` is the
season year. Example events:

| Event | Tag |
| ----- | --- |
| Season baseline (competition-ready code freeze) | `v<YYYY>.0.0` |
| Competition hotfix | `v<YYYY>.0.P` |
| Post-season cleanup / offseason tag | `v<YYYY>.N.P` |

## 4. Library Pinning

This project pins Team271-Lib via its vendordep JSON. Record the
pinned version here whenever it changes. A library upgrade is a
change event that **shall** be documented in §5.

| Field | Value |
| ----- | ----- |
| Pinned library version | `v<YYYY>.N.P` |
| Vendordep file | `vendordeps/Team271-Lib.json` (robot repo) |
| Last upgrade date | YYYY-MM-DD |

## 5. Deviation Tracking

Deviations from the shared CM policy, the library's SCMP, or any
inherited ADR are recorded here. Each deviation **shall** be approved
via pull request.

| Inherited rule | Project deviation | Reason | Approver | Date |
| -------------- | ----------------- | ------ | -------- | ---- |
| (none yet) | | | | |
