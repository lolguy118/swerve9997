<!-- markdownlint-disable MD013 -->
# Common Planning Framework

Framework-agnostic planning policy that applies to every Team 271 Java
project — library, season robot, standalone tool. These documents
describe *how* to plan, not what any specific product's plan is.

Each project keeps its own concrete plans alongside its source — a
Software Development Plan (SDP), Software Requirements Specification
(SRS), Software Verification Plan (SVP), and Configuration Management
Plan (SCMP) — and cites the policies here rather than re-stating them.

> **Change cascade.** Edits to any document in this folder change the
> contract every consuming project inherits. Each change **shall** be
> paired with a review of every consuming project's SDP, SRS, SVP, and
> SCMP before merge, and **should** note the cascade in its PR
> description.

## Normative Keywords

These documents use **SHALL**, **SHOULD**, and **MAY** per
[RFC 2119](https://www.ietf.org/rfc/rfc2119.txt):

- **SHALL** / **SHALL NOT** — absolute requirement.
- **SHOULD** / **SHOULD NOT** — recommended; depart only with
  documented rationale.
- **MAY** — optional.

Each consuming project's SDP, SRS, SVP, and SCMP inherits these
definitions unless it explicitly overrides them.

## Documents

| File | Covers |
| ---- | ------ |
| [`configuration-management.md`](configuration-management.md) | SemVer policy, branch model, vendordep upgrade process, baseline tags, deviation tracking |
| [`development-plan.md`](development-plan.md) | Development environment baseline, platform matrix pattern, FRC-calendar-keyed phase model |
| [`verification-plan.md`](verification-plan.md) | Test levels (unit / integration / sim / static), coverage-target framework, pre-merge hook pattern, CI gate structure |

## Planned Documents

> **Status: Planned — Not Yet Implemented.**

The following common-planning topics are reserved for future documents.
Each **shall** be authored when a consuming project has a concrete need
for it — not speculatively.

| Topic | Scope |
| ----- | ----- |
| Observability | Logging, telemetry-key conventions, AdvantageKit and Elastic integration shared across Team 271 Java projects |
| Documentation policy | Shared markdown rules (line length, tunable ban, cross-ref style, planned-feature marker) currently encoded in `.claude/rules/docs.md` |
| Supply chain | Vendor-trust policy, dependency-graph submission, sensitive-value handling, vendordep-freshness cadence |

## What does *not* live here

- A specific project's requirements (always belongs in that project's SRS)
- A specific project's architecture (belongs in its SDDs)
- A specific project's ADRs (decisions are project-scoped)
- Concrete vendor versions, coverage numbers, workflow filenames
