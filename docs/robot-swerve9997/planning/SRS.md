<!-- TEMPLATE FOR FORKED ROBOT PROJECTS -- scaffold file renamed in
     place to docs/<project>/planning/SRS.md by tools/init-robot.sh
     during project initialization. This banner is stripped by the
     init script. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Software Requirements Specification

| Field | Value |
| ----- | ----- |
| Document No. | TBD-<PROJECT>-SRS |
| Revision | 0.1 |
| Date | YYYY-MM-DD |
| Status | Draft |

This document captures **what the <Project> robot shall do** —
mechanism behaviors, operator interactions, autonomous routines, and
the non-functional constraints the code must honor. It is distinct
from [`../../team-lib/planning/SRS.md`](../../team-lib/planning/SRS.md),
which captures *library* requirements.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose and Scope

Defines the robot's externally observable behavior. Does not cover:

- How any requirement is implemented → each subsystem's SDD in
  [`sdd/`](sdd/)
- Test strategy for verifying each requirement → [SVP.md](SVP.md)
- Library-level requirements (already guaranteed by the library
  dependency) → [`../../team-lib/planning/SRS.md`](../../team-lib/planning/SRS.md)

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [`../../common/planning/README.md`](../../common/planning/README.md) | Normative keywords, framework conventions |
| [`../../team-lib/planning/SRS.md`](../../team-lib/planning/SRS.md) | Library requirements the project depends on |
| [SDP.md](SDP.md) | Project development plan (phases, pins) |
| [SVP.md](SVP.md) | How each requirement is verified |
| [adr/](adr/) | Project-scope architectural decisions |

## 3. Functional Requirements

Requirements tagged `[<PROJECT>-FN-NNN]`. Each requirement **shall**
be verifiable via a test, a demonstration, or a code review.

| ID | Requirement | Verification |
| -- | ----------- | ------------ |
| `[<PROJECT>-FN-001]` | (example) The robot **shall** score a game piece on the high goal within `<N>` seconds of operator command. | Field demo |

## 4. Non-Functional Requirements

Requirements tagged `[<PROJECT>-NF-NNN]`. Cover timing, reliability,
safety, telemetry, and operator-experience constraints.

| ID | Requirement | Verification |
| -- | ----------- | ------------ |
| `[<PROJECT>-NF-001]` | (example) The main robot loop **shall** complete within 20 ms on roboRIO 2 at every periodic tick. | Log analysis (AdvantageKit) |

## 5. Per-Subsystem Requirements

For each robot subsystem, add a subsection with its own requirement
prefix (e.g., `[DRIVE-NNN]`, `[SHOOTER-NNN]`, `[INTAKE-NNN]`) and a
requirements table scoped to that subsystem's API contract.

### 5.1 `<Subsystem>` — `[<SUBSYS>-NNN]`

| ID | Requirement | Verification |
| -- | ----------- | ------------ |
| `[<SUBSYS>-001]` | (example) | |

## 6. Traceability

Every requirement **shall** be traced to (a) the SDD section that
implements it and (b) the test case that verifies it.

| Requirement | Implementing SDD | Verifying test |
| ----------- | ---------------- | -------------- |
| `[<PROJECT>-FN-001]` | `sdd/SDD-<subsystem>.md` §N | `[<PROJECT>-TEST-NNN]` |
