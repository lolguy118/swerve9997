# Architecture Decision Records (ADRs)

When Team271-Lib makes a **big architectural choice** — like "use
CTRE (Cross The Road Electronics) devices only" or "wrap vendor
objects instead of hiding them" — we write it down as an
**Architecture Decision Record (ADR)**.

Each ADR explains:

- **Context** — what problem we were trying to solve.
- **Decision** — what we decided to do.
- **Rationale** — why that decision, and not something else.
- **Consequences** — what the decision makes easier or harder.
- **Alternatives Considered** — other options we looked at and
  rejected, with reasons.

Reading the ADRs is the fastest way to understand **why** the
library is built the way it is.

## Start here

If you're new:

- The full ADR list (with status legend) is in
  [`../README.md`](../README.md#architecture-decision-records).
- Good first ADRs to read, in this order:
  1. [ADR-001: Team271-Lib as a Standalone Library, Separate from Robot Projects](ADR-001-team271-lib-standalone-library.md)
  2. [ADR-003: Layered Architecture — api ← vendor/* ← hardware ← control ← subsystem ← auto](ADR-003-layered-architecture.md)
  3. [ADR-005: Passthrough — Wrapper, Not Wall](ADR-005-passthrough-wrapper-not-wall.md)
  4. [ADR-008: CTRE Phoenix 6 as the Primary Motor/Sensor Vendor](ADR-008-ctre-phoenix6-primary-vendor.md)

## Permanence

ADRs are permanent. Once an ADR is **Accepted**, it is not deleted.
If a later decision reverses it, the original ADR is marked
**Superseded by ADR-XXX** and the new ADR is written alongside it.
This way the full decision history stays legible — you can always
read why the library was built the way it was at any point in time.

The normative keywords **SHALL**, **SHOULD**, and **MAY** follow
the convention defined in
[`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

## ADR Template

```markdown
# ADR-NNN: Short Decision Title

## Status

[Proposed | Accepted | Superseded by ADR-XXX]

## Date

YYYY-MM-DD

## Context

What is the problem or situation? What forces are at play?

## Decision

What was decided? One clear statement.

## Rationale

Why was this decision made? What evidence or constraints guided it?

## Consequences

What becomes easier? What becomes harder or constrained?

## Alternatives Considered

What other options were evaluated and why were they rejected?

## References

Links to SCS sections, relevant docs, or related ADRs.
```
