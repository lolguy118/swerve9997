# ADR-001: Team271-Lib as a Standalone Library, Separate from Robot Projects

## Status

Accepted

## Date

2026-04-23

## Context

Every FRC season, Team 271 builds a new robot project. Across seasons,
substantial portions of the code are reusable: motor wrappers, sensor
abstractions, PID variants, the subsystem lifecycle pattern, autonomous
move composition, the CAN refresh orchestration, and the telemetry stack.

Historically, reusable code was copied from the previous season's
robot project and modified in-place. This created three problems: bug
fixes made in one robot project were lost in the next; architectural
improvements diverged; and onboarding new students meant reading
whichever robot-specific variant happened to be in the current season's
codebase.

Team271-Lib consolidates that reusable surface into a single repository
with its own tests, design documents, and coding standards so that one
authoritative baseline exists at the start of every season.

## Decision

Team271-Lib lives in its own Git repository with its own Gradle build,
its own tag history, and its own release cadence. Each season's robot
project is created by **forking or copying this repository at a chosen
tag**, then renaming the `docs/robot-yyyy/` scaffolding and the
`com.team271.libtest` package to project-specific names (an init
script under `tools/` automates the mechanical rename steps).
Team271-Lib is not published as a Maven artifact and robot projects
do not pull library updates after the initial fork.

The library has no awareness of any specific robot's geometry,
subsystems, or autonomous routines.

## Rationale

1. **Single authoritative starting point.** Every season's robot
   begins from a reviewed, tagged baseline of Team271-Lib. The library
   remains the one place where subsystem lifecycle, CAN orchestration,
   PID variants, and auto composition are designed and tested.
2. **Simple bootstrap.** Forking a working repo is faster and less
   error-prone than standing up a new Gradle project that pulls a
   Maven-published artifact. No publishing infrastructure to maintain,
   no vendordep JSON to author, no coordination of library releases
   against robot build milestones.
3. **Full access during the season.** A robot team that needs to
   modify library internals mid-season — to work around a bug, add a
   one-off hook, or experiment with a new control strategy — can do
   so directly in the robot's own repo without waiting on an upstream
   release.
4. **Onboarding clarity.** New students learn one codebase — the
   forked repo — not a library plus a separate consumer project that
   depends on it. Design docs, tests, coding standards, and working
   examples all ship in the same repository.

## Consequences

**Easier:**

- Bootstrapping a new robot project is a `git clone` plus a rename
  script — no Maven publishing, no vendordep wiring, no submodule
  or subtree management.
- A robot team has complete autonomy over library code for the
  duration of the season.
- The `libtest/` package doubles as a reference-implementation robot
  that is built and tested alongside library changes, so the library
  never accumulates features that lack at least one consumer.

**Harder — accepted costs:**

- **Cross-season divergence is expected.** Fixes made inside a robot
  repo during the season do not automatically reach the next season's
  fork. Shared fixes must be manually ported back to Team271-Lib
  `main` between seasons, or the same bug can re-appear when next
  season forks from a stale tag. The team accepts this cost as the
  price of simpler bootstrapping and solo-season development.
- **No runtime upgrade path.** Once a robot project forks, it does
  not pull library updates. A mid-season library bug fix lands by
  editing the affected file in the robot repo directly, not by
  bumping a dependency version.
- **Divergent commit histories.** Team271-Lib `main` and each robot
  repo accumulate independent commit histories after the fork.
  Tooling that asks "which library version does this robot use?"
  answers only by the tag the robot was forked from, not by a
  pinned artifact.
- **Library tests ship with each robot repo.** A forked robot repo
  inherits Team271-Lib's test suite and CI configuration. Robot
  teams may prune or disable tests they do not care about, but the
  default is to keep them so regressions surface before a match.

## Alternatives Considered

- **Maven-published versioned artifact.** Rejected — requires
  publishing infrastructure (GitHub Packages, JitPack, or similar),
  vendordep JSON authoring, and per-release coordination with robot
  projects. The ergonomic cost outweighs the upgrade-path benefit at
  FRC-season cadence, especially given that competition weeks often
  run with degraded network access.
- **Git submodule or subtree in each robot project.** Rejected —
  submodules complicate competition-week Git operations (offline
  laptops, shallow clones, merge conflicts on shared files) and
  duplicate the library's test surface inside each robot's CI.
- **In-place code copy from the previous season's robot.** Rejected —
  this was the historical pattern and produced the divergence and
  onboarding problems that motivate Team271-Lib in the first place.
  The fork-at-tag model retains a single authoritative lineage for
  the library even though each season's robot diverges after its
  fork.

## References

- [SDD-team271-lib.md](../sdd/SDD-team271-lib.md)
- [ADR-003](ADR-003-layered-architecture.md)
- [SCMP.md §3 Library Versioning](../SCMP.md#3-library-versioning)
