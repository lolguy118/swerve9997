# ADR-001: Team271-Lib as a Standalone Library, Separate from Robot Projects

## Status

Accepted

## Date

2026-04-20

## Context

Every FRC season, Team 271 builds a new robot project. Across seasons,
substantial portions of the code are reusable: motor wrappers, sensor
abstractions, PID variants, the subsystem lifecycle pattern, autonomous
move composition, the CAN refresh orchestration, and the telemetry stack.

Historically, this reusable code was copied from the previous season's
robot project and modified in-place. This created three problems: bug
fixes made in one robot project were lost in the next; architectural
improvements diverged; and onboarding new students meant reading
whichever robot-specific variant happened to be in the current season's
codebase.

## Decision

Team271-Lib lives in its own Git repository with its own Gradle build,
its own version number, and its own release cadence. Robot projects
consume it as a versioned dependency. The library has no awareness of
any specific robot's geometry, subsystems, or autonomous routines.

## Rationale

1. **Single source of truth.** Bug fixes and improvements flow from the
   library to all robot projects that upgrade to the new version.
2. **Independent versioning.** Robot projects can pin to a known-good
   library version during competition season and upgrade on their own
   schedule.
3. **Testability.** Library code is unit-tested independently
   (see [ADR-009](ADR-009-junit5-hal-simulation-tests.md)). A robot
   project that consumes the library as a published artifact does
   not pay the cost of running library tests (source-included
   consumption is an exception).
4. **Onboarding clarity.** New students learn the library once; the
   library does not change from season to season in ways that invalidate
   their learning.

## Consequences

**Easier:**

- Cross-season bug fixes propagate cleanly via version upgrade.
- Library refactors happen in one place, with one test suite.
- Documentation is stable across seasons.

**Harder:**

- A library change that breaks a robot project requires either a
  library MAJOR bump or a compatibility shim.
- Library maintainers must resist adding robot-specific logic, even
  when it would be expedient in the moment.
- Robot projects inherit library test requirements (HAL init,
  CTREManager cleanup).

## Alternatives Considered

- **Copy library files into each robot project.** Rejected — divergence
  across seasons has real cost and is the problem this ADR solves.
- **Embed the library as a Git submodule in each robot project.**
  Rejected — submodules surface the library's full test suite to the
  robot project's CI and blur the versioning boundary.

## References

- [SDD-team271-lib.md](../sdd/SDD-team271-lib.md)
- [ADR-004](ADR-004-layered-architecture.md)
- [SCMP.md §3 Library Versioning](../SCMP.md#3-library-versioning)
