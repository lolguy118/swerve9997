<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/planning/SVP.md in the robot's own repository. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Software Verification Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-<PROJECT>-SVP |
| Revision | 0.1 |
| Date | YYYY-MM-DD |
| Status | Draft |

This document describes how the **<Project>** robot's code is
verified — what tests exist, what coverage is expected, and which
pre-merge and CI gates run. It concretizes the shared verification
framework (§2) with project-specific choices.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose and Scope

Defines test strategy, coverage targets, and CI gating for the
<Project> robot code. Covers:

- Test levels (unit, integration, on-robot, driver-practice)
- Coverage targets per subsystem
- CI pipeline gates

Does not cover:

- Shared verification framework (test-level taxonomy, coverage tier
  structure, CI gate pattern) → [`../../common/planning/verification-plan.md`](../../common/planning/verification-plan.md)
- Library's own test strategy → [`../../team-lib/planning/SVP.md`](../../team-lib/planning/SVP.md)
  (robot inherits library hooks when vendored)

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [`../../common/planning/verification-plan.md`](../../common/planning/verification-plan.md) | Shared test levels, coverage tiers, CI gate framework |
| [`../../team-lib/planning/SVP.md`](../../team-lib/planning/SVP.md) | Library test strategy + hook roster the project can pick up |
| [SRS.md](SRS.md) | Each requirement traces to a test case |

## 3. Test Levels

| Level | What it verifies | Where it runs |
| ----- | ---------------- | ------------- |
| Unit | Pure logic; state-machine transitions; math helpers | Desktop, fast |
| Integration (HAL sim) | Subsystem interacting with simulated hardware | Desktop, CI |
| On-robot | Real hardware, shop or practice field | Robot |
| Driver practice | Operator-facing behavior, game-piece handling | Practice field |

## 4. Coverage Targets

Per-subsystem coverage targets. Fill in the actual subsystems as they
are authored; remove the example row.

| Subsystem | Statement coverage | Branch coverage | Notes |
| --------- | ------------------ | --------------- | ----- |
| `<Subsystem>` | `<%>` | `<%>` | (e.g., HAL-sim only; no on-robot unit tests) |

## 5. CI Pipeline

Minimum CI gates for this project:

- Spotless (format) — `./gradlew spotlessCheck`
- Compile + unit + integration tests — `./gradlew test`
- Markdown lint — via `markdownlint` on `docs/**`

Projects that vendor `docs/team-lib/` alongside the library may pick
up the library's hook roster (see
[`../../team-lib/planning/SVP.md`](../../team-lib/planning/SVP.md#6-hooks-as-pre-merge-gates-library-roster));
doing so is recommended but not required.

## 6. Traceability

Test IDs follow the convention `[<PROJECT>-TEST-NNN]`. Every SRS
requirement **shall** be traced to at least one test ID here or in a
per-subsystem SDD §9 "Test Coverage Requirements" section.

| Test ID | Requirement(s) verified | Level | Location |
| ------- | ----------------------- | ----- | -------- |
| `[<PROJECT>-TEST-001]` | `[<PROJECT>-FN-001]` | Integration | `src/test/...` |
