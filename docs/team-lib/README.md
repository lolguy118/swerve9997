# Team271-Lib — Library Documentation

This folder holds **everything specific to Team271-Lib itself** —
how it's designed, how it's tested, how it's versioned, and how to
get started contributing to it. Rules that apply to *any* Team 271
project live one level up in [`../common/`](../common/); rules
specific to a *robot project* live in that robot's own repository.

> **Industry bridge.** Professional software libraries ship with a
> predictable set of documents: requirements, design, verification,
> and configuration management. This folder follows that same shape
> so you can read Team271-Lib the way you'd read any industrial
> codebase.

## Start here

If you're new, open these in order:

1. [`guides/start-here.md`](guides/start-here.md) — five-minute
   orientation for first-season contributors.
2. [`../common/guides/development-setup.md`](../common/guides/development-setup.md) —
   get Java 17, WPILib, and Gradle installed so you can run the
   library's tests locally.
3. [`planning/README.md`](planning/README.md) — the planning and
   design-document index: what each document does and a recommended
   reading order.

## What's in this folder

| Path | What it holds |
| ---- | ------------- |
| [`planning/`](planning/) | Software Development Plan, Software Requirements Specification, Software Verification Plan, Software Configuration Management Plan, Architecture Decision Records (ADRs), and Software Design Descriptions (SDDs) |
| [`guides/`](guides/) | Step-by-step tutorials for contributors (setup, simulation, system identification, input shaping) |
| [`internal/`](internal/) | Reference artifacts that support the planning docs (for example, the architecture dependency diagram) |
| [`prompts/`](prompts/) | AI-assisted review prompts used during code review |
| [`coding-standard/`](coding-standard/) | Library-specific notes and templates that bind the generic Team271 coding standard to concrete library classes and robot-project patterns |
