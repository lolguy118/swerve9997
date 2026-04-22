<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/planning/sdd/README.md in the robot's own
     repository. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Software Design Descriptions (SDDs) — <Project>

A **Software Design Description (SDD)** explains how one subsystem of
the <Project> robot is put together on the inside — what classes
exist, what each one is responsible for, how data flows through, how
errors are handled. One SDD per subsystem.

If the Software Requirements Specification (SRS) says *what* the
robot promises, an SDD says *how* we make good on that promise for
one subsystem at a time.

> **Industry bridge.** SDDs are a standard software-engineering
> artifact described by IEEE Std 1016. Every SDD follows a fixed
> nine-section template (Purpose, Scope and Boundaries, Module
> Decomposition, Data Flow, Key Design Decisions, Error Handling,
> Platform Portability Notes, Configuration, Test Coverage
> Requirements).

## Start here

List the robot's subsystems below as their SDDs are authored.
Library-internal SDDs (the base `Subsystem` class, `SubsystemManager`,
`AutoMove`, `CTREManager`, etc.) are already authored in the library
tier — see
[`../../../team-lib/planning/sdd/README.md`](../../../team-lib/planning/sdd/README.md).

## SDD Template

Each SDD **shall** start with the same header table and nine-section
structure used by library SDDs. The simplest way to start is to copy
the skeleton from an existing library SDD (e.g.,
[`../../../team-lib/planning/sdd/SDD-control.md`](../../../team-lib/planning/sdd/SDD-control.md))
and rewrite its contents for the robot subsystem.

Section titles, in order:

1. Purpose
2. Scope and Boundaries
3. Module Decomposition
4. Data Flow
5. Key Design Decisions
6. Error Handling
7. Platform Portability Notes
8. Configuration
9. Test Coverage Requirements

## SDD Inventory

Fill this table in as subsystem SDDs are authored. Replace the
example row.

| File | Subsystem |
| ---- | --------- |
| `SDD-<subsystem>.md` | `<Subsystem>` (example placeholder) |
