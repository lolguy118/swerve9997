# Common Planning Framework

This folder holds the **shared planning rules** used by every
Team 271 Java project — the library, each season's robot code, and
any standalone tool. These documents describe **how** to plan a
project; each specific project then writes its own plan documents
that follow these rules.

When you start a Team 271 project, you write four plan documents
alongside your code:

- **Software Development Plan (SDP)** — what gets built, in what
  order, against what toolchain.
- **Software Requirements Specification (SRS)** — what the software
  promises to the people and programs that use it.
- **Software Verification Plan (SVP)** — how you test and verify the
  software works.
- **Software Configuration Management Plan (SCMP)** — how versions,
  releases, and deviations are tracked.

Each of those project plans cites the rules in this folder rather
than repeating them.

## Start here

If you're new, read the framework documents in this order:

1. [`development-plan.md`](development-plan.md) — the SDP framework:
   development environment, platform matrix, and the four-phase
   model keyed to the FRC (FIRST Robotics Competition) season
   calendar.
2. [`verification-plan.md`](verification-plan.md) — the SVP
   framework: test levels, coverage-target tiers, pre-merge hooks,
   and continuous-integration (CI) gates.
3. [`configuration-management.md`](configuration-management.md) —
   the SCMP framework: Semantic Versioning (SemVer), branch model,
   vendor-dependency upgrade process, and deviation tracking.

The SRS has no shared framework file here because each project's
requirements are unique — every project writes its own SRS.

> **Heads up — changes cascade.** Because every Team 271 project
> inherits these rules, any edit here changes the contract each
> project's plans must follow. When you change a rule, also review
> every consuming project's SDP, SRS, SVP, and SCMP to make sure
> they still make sense, and call out the cascade in your pull
> request (PR) description so reviewers know to check each project.

## Normative Keywords

When you read these documents, a few words carry more weight than
they do in everyday English:

- **SHALL** / **SHALL NOT** — **must** do this / must **not** do
  this. No exceptions.
- **SHOULD** / **SHOULD NOT** — strongly recommended; depart only
  with a written reason.
- **MAY** — optional; do it or don't, your choice.

This convention comes from [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt),
an internet-standards document that defines how standards tell
absolute requirements apart from recommendations. Every plan
document (SDP, SRS, SVP, SCMP) inherits these meanings unless it
explicitly overrides them.

## Documents

| File | Covers |
| ---- | ------ |
| [`configuration-management.md`](configuration-management.md) | Semantic Versioning (SemVer) policy, branch model, vendor-dependency upgrade process, baseline tags, deviation tracking |
| [`development-plan.md`](development-plan.md) | Development environment baseline, platform matrix pattern, FRC (FIRST Robotics Competition) calendar-keyed phase model |
| [`verification-plan.md`](verification-plan.md) | Test levels (unit / integration / simulation / static), coverage-target framework, pre-merge hook pattern, continuous-integration (CI) gate structure |

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

- A specific project's requirements (always belongs in that project's SRS — Software Requirements Specification)
- A specific project's architecture (belongs in its SDDs — Software Design Descriptions)
- A specific project's ADRs — Architecture Decision Records (decisions are project-scoped)
- Concrete vendor versions, coverage numbers, workflow filenames
