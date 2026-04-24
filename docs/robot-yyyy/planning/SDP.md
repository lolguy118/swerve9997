<!-- TEMPLATE FOR FORKED ROBOT PROJECTS -- scaffold file renamed in
     place to docs/<project>/planning/SDP.md by tools/init-robot.sh
     during project initialization. This banner is stripped by the
     init script. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Software Development Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-<PROJECT>-SDP |
| Revision | 0.1 |
| Date | YYYY-MM-DD |
| Status | Draft |

This document is the **<Project>** robot's specific development plan.
It builds on the shared planning framework (see §2 Applicable
Documents) and records the project's concrete pins, platform deltas,
milestones, and any deviations from inherited standards.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose and Scope

Defines when <Project> robot code is written, in what order, and
against what toolchain pin. Captures project-specific milestones and
any deviation from the library's development pins. Does not cover:

- Coding rules → [`../../common/coding-standard/Team271-Software-Coding-Standard.md`](../../common/coding-standard/Team271-Software-Coding-Standard.md)
  and [`../coding-standard.md`](../coding-standard.md)
- Test strategy → [SVP.md](SVP.md)
- Functional requirements → [SRS.md](SRS.md)
- Versioning and vendordep policy → [SCMP.md](SCMP.md)

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [`../../common/planning/development-plan.md`](../../common/planning/development-plan.md) | Shared toolchain framework, platform matrix, phase model |
| [`../../common/planning/configuration-management.md`](../../common/planning/configuration-management.md) | Deviation-tracking row format |
| [`../../team-lib/planning/SDP.md`](../../team-lib/planning/SDP.md) | Library's own SDP (project inherits pins unless overridden) |
| [SRS.md](SRS.md) | Project requirements |
| [SVP.md](SVP.md) | Project verification plan |
| [SCMP.md](SCMP.md) | Project configuration management |

## 3. Project Overview

> **Replace the placeholders below.**

- **Robot nickname**: `<Robot-Name>`
- **Season**: `<YYYY>`
- **Top-level package**: `com.team271.<project>`
- **Forked from**: Team271-Lib, tag recorded in [SCMP.md](SCMP.md) §4

Brief description (1-2 paragraphs) of what the robot does
mechanically, what mechanisms it has, and what game tasks it performs.

## 4. Development Environment

Unless overridden in §7, this project inherits every pin from
[`../../team-lib/planning/SDP.md`](../../team-lib/planning/SDP.md#4-development-environment-library-pins)
(Java version, Gradle version, WPILib version, Spotless/Checkstyle
tool versions). Record project-specific overrides here.

| Component | Source of truth | Project override (if any) |
| --------- | --------------- | ------------------------- |
| Java version | Team271-Lib SDP §4 | (none) |
| Gradle version | Team271-Lib SDP §4 | (none) |
| WPILib version | Team271-Lib SDP §4 | (none) |
| Fork origin tag | [SCMP.md](SCMP.md) §4 | (record fork tag) |

## 5. Platform Matrix

| Platform | Purpose | Used in CI? |
| -------- | ------- | ----------- |
| roboRIO 2 | Competition robot | Manual on-robot validation |
| Desktop sim (Windows/macOS/Linux) | Unit + simulation tests | Yes |

## 6. FRC Phase Milestones

Fill in dates per season.

| Phase | Target date | Deliverable |
| ----- | ----------- | ----------- |
| Kickoff / strategy | `<date>` | Game-strategy document, mechanism concepts |
| Build-season start | `<date>` | First on-robot drive |
| Code freeze (competition) | `<date>` | Tagged release `v<YYYY>.0.0` |
| Championship prep | `<date>` | Final season tag `v<YYYY>.N.P` |

## 7. Deviations from Inherited Standards

Record any deviation from common, library, or inherited-ADR decisions
here. Deviations require an approved pull request that cites the
inherited rule and a rationale.

| Inherited rule / pin | Project deviation | Reason | Approver | Date |
| -------------------- | ----------------- | ------ | -------- | ---- |
| (none yet) | | | | |
