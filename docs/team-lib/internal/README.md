<!-- markdownlint-disable MD013 -->
# Internal Reference Artifacts

This folder holds **supporting artifacts** for the library's
planning and design documents — things like architecture diagrams
and internal-only reference material. They aren't policy themselves;
they support the docs in [`../planning/`](../planning/).

> **Industry bridge.** Real-world software teams keep diagrams
> alongside the code in a *diagram-as-code* format so the diagram
> lives in version control and evolves with the codebase. We use
> [Mermaid](https://mermaid.js.org/) (a text-based diagram format
> GitHub renders automatically) for the same reason.

## Contents

| File | What it shows |
| ---- | ------------- |
| [`team271-lib-dependency-diagram.mmd`](team271-lib-dependency-diagram.mmd) | The six-layer architecture in diagram form — cited by [ADR-004 Layered Architecture](../planning/adr/ADR-004-layered-architecture.md). Includes the vision vendors ([ADR-016](../planning/adr/ADR-016-vendor-neutral-vision-abstraction.md)) and the CAN-bus abstraction ([ADR-017](../planning/adr/ADR-017-can-bus-abstraction.md)). |
| [`team271-lib-package-sdd-map.mmd`](team271-lib-package-sdd-map.mmd) | Which Software Design Description (SDD) covers each Java source package — visual form of the [Package-to-SDD Map](../planning/README.md#package-to-sdd-map). |
| [`team271-lib-periodic-flow-diagram.mmd`](team271-lib-periodic-flow-diagram.mmd) | Sequence of one robot cycle: bulk CAN refresh ([ADR-007](../planning/adr/ADR-007-centralized-can-refresh.md)), desired-to-actual reconciliation ([ADR-014](../planning/adr/ADR-014-desired-to-actual-state-pattern.md)), and per-subsystem exception isolation ([ADR-010](../planning/adr/ADR-010-subsystem-exception-isolation.md)). |
| [`team271-lib-automove-composition-diagram.mmd`](team271-lib-automove-composition-diagram.mmd) | Class hierarchy of the autonomous-move type system and the `CommandBridge` adapter — cited by [ADR-005](../planning/adr/ADR-005-composition-over-commands.md) and [ADR-013](../planning/adr/ADR-013-trajectory-following-vendors.md). |
| [`team271-lib-api-vendor-map-diagram.mmd`](team271-lib-api-vendor-map-diagram.mmd) | Which vendor class implements which `api/` interface — visualises [ADR-006](../planning/adr/ADR-006-ctre-phoenix6-primary-vendor.md), [ADR-016](../planning/adr/ADR-016-vendor-neutral-vision-abstraction.md), and [ADR-017](../planning/adr/ADR-017-can-bus-abstraction.md). |
