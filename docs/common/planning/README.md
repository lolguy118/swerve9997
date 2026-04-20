<!-- markdownlint-disable MD013 -->
# Common Planning Framework

Framework-agnostic planning policy that applies to every Team 271 Java
project — library, season robot, standalone tool. These documents
describe *how* to plan, not what any specific product's plan is.

Each project keeps its own concrete plans alongside its source (e.g.,
a library's `SDP.md` / `SRS.md` / `SVP.md` / `SCMP.md`) and cites the
policies here rather than re-stating them.

## Documents

| File | Covers |
| ---- | ------ |
| [`configuration-management.md`](configuration-management.md) | SemVer policy, branch model, vendordep upgrade process, baseline tags, deviation tracking |
| [`development-plan.md`](development-plan.md) | Development environment baseline, platform matrix pattern, FRC-calendar-keyed phase model |
| [`verification-plan.md`](verification-plan.md) | Test levels (unit / integration / sim / static), coverage-target framework, pre-merge hook pattern, CI gate structure |

## What does *not* live here

- A specific project's requirements (always belongs in that project's SRS)
- A specific project's architecture (belongs in its SDDs)
- A specific project's ADRs (decisions are project-scoped)
- Concrete vendor versions, coverage numbers, workflow filenames
