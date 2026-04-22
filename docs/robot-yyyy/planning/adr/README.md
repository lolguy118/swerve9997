<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/planning/adr/README.md in the robot's own
     repository. The ADR template is framework-level and can be
     reused verbatim. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Architecture Decision Records (ADRs) — <Project>

When the **<Project>** robot makes a **big architectural choice**
— like "use swerve vs. tank drive", "put the intake on the front or
back", or "bind the fast-shot routine to the left trigger" — we
write it down as an **Architecture Decision Record (ADR)**.

Project-scope ADRs live **here**. Library-scope ADRs (things like
"use CTRE Phoenix 6", "centralized CAN refresh", "no singletons in
library code") live in the library tier at
[`../../../team-lib/planning/adr/`](../../../team-lib/planning/adr/)
and are inherited by depending on the library.

Each ADR explains:

- **Context** — what problem we were trying to solve.
- **Decision** — what we decided to do.
- **Rationale** — why that decision, and not something else.
- **Consequences** — what the decision makes easier or harder.
- **Alternatives Considered** — other options we looked at and
  rejected, with reasons.

## Start here

- The library's ADRs (inherited decisions you should be aware of)
  are listed in [`../README.md`](../README.md#inherited-library-decisions-informational).
- Project-scope ADRs live in this folder. When there are none yet,
  this section stays empty.

## Permanence

ADRs are permanent. Once an ADR is **Accepted**, it is not deleted.
If a later decision reverses it, the original ADR is marked
**Superseded by ADR-XXX** and the new ADR is written alongside it.

The normative keywords **SHALL**, **SHOULD**, and **MAY** follow the
convention defined in
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

Links to library ADRs, SDDs, coding-standard sections, or other ADRs.
```

## Project ADR Index

| ADR | Title | Status |
| --- | ----- | ------ |
| (none yet) | | |
