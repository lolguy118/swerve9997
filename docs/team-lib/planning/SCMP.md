# Software Configuration Management Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SCMP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

> This document is deliberately brief. It identifies the authoritative
> CM sources and summarises the policies they define. Do not duplicate
> content from those sources here.

## 1. Purpose

This plan summarizes Team271-Lib's configuration management:
versioning, vendordep upgrades, branch policy, and deviation
tracking. The authoritative CM sources are listed in §2.

## 2. Authoritative CM Documents

| Document | CM Concern |
| -------- | ---------- |
| `../../CONTRIBUTING.md` (repository root) | Branch naming, PR process, commit rules, linting workflow |
| `../../build.gradle` | Library version, Gradle toolchain version |
| `../../vendordeps/*.json` | Vendordep versions (Phoenix 6, WPILib, AdvantageKit, PathPlanner) |

## 3. Library Versioning

Team271-Lib uses Semantic Versioning (SemVer):

- **MAJOR** — breaking API change (rare; only during offseason)
- **MINOR** — new API surface (typically once per season)
- **PATCH** — bug fix (any phase)

Version format: `YYYY.MINOR.PATCH` (season year prefix). Version is set
in `build.gradle`. Git tags are applied at season boundaries per
[SDP §8.1](SDP.md): the prior season's final tag (e.g., `v2026.3.2`) is
applied at offseason start; the next-season MINOR=0 tag (e.g.,
`v2027.0.0`) is applied at preseason API freeze.

## 4. Vendordep Management

Upgrading a vendordep requires:

1. Update `vendordeps/*.json` to new version.
2. Run `./gradlew compileJava` — fix any API breaks.
3. Run `./gradlew test` — fix any test failures.
4. If the vendor changes fundamentally (new CTRE release, new WPILib
   major), write a new ADR (see [ADR-006](adr/ADR-006-ctre-phoenix6-primary-vendor.md)).
5. Merge via normal PR process.

## 5. Baseline Control

- `main` branch **should** be protected on GitHub (no direct push,
  no force-push). Verify this is actually configured on the remote.
- Season-boundary baselines are Git tags on `main` (signed).
- Feature branches: `feat/short-description` or `fix/short-description`.

## 6. Change Control

- All changes enter via PR on a feature branch.
- `/lib-review` command is expected on any non-trivial change.
- All `.claude/hooks/` must pass before merge approval is granted.
- At least one maintainer must approve the PR.

## 7. Deviation Tracking

Deviations from the coding standard are logged in [SDP.md §9](SDP.md).

Format:

| Rule ID | Rationale | Approval Date |
| ------- | --------- | ------------- |
| CODE-XXX-NNN | Why deviation is acceptable | YYYY-MM-DD |
