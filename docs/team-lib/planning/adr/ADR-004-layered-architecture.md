# ADR-004: Layered Architecture — api ← vendor/ctre ← hardware ← control ← subsystem ← auto

## Status

Accepted

## Date

2026-04-20

## Context

As the library has grown to cover motor control, sensor abstractions,
PID variants, state machines, and autonomous composition, the dependency
graph between packages has become an architectural question. Without a
rule for which packages can depend on which, the library drifts toward
a cycle-rich graph in which everything eventually depends on everything,
making refactors and test isolation painful.

## Decision

Team271-Lib is organized into six layers. Each layer may depend only on
layers below it. From bottom to top:

| Layer | Package | Depends On |
| ----- | ------- | ---------- |
| 1 | `api/` | (none — pure interfaces) |
| 2 | `vendor/ctre/` | `api/` + CTRE Phoenix 6 |
| 3 | `hardware/` | `vendor/ctre/`, `api/` |
| 4 | `control/` | `hardware/` |
| 5 | `subsystem/` | `control/`, `hardware/` |
| 6 | `auto/` | `subsystem/` |
| C | `nt/`, `sysid/`, `util/` | (cross-cutting; depend on none above) |

Cross-cutting packages may be used by any layer but may not depend on
any layer above themselves.
See [docs/internal/team271-lib-dependency-diagram.mmd](../../internal/team271-lib-dependency-diagram.mmd).

## Rationale

1. **Test isolation.** Lower layers can be tested without initializing
   upper layers. A PID test does not require a subsystem.
2. **Mental model.** Students can read the library bottom-up: first
   the neutral interfaces, then the CTRE realizations, then the
   lifecycle wrappers, then control, then subsystems, then autonomous.
3. **Change impact.** A change in `api/` requires updates in every
   layer above; a change in `auto/` never requires updates below.
   This maps directly to how the library is evolved.
4. **Vendor pluggability.** The api/ ← vendor/ctre/ split leaves a
   seam for a future WPILib or REV implementation, without forcing
   one to exist today (ADR-006).

## Consequences

**Easier:**

- Package imports clearly signal dependency direction.
- Refactors are scoped — a `vendor/ctre/` change cannot ripple to
  `api/`.
- Subsystem code does not depend on CTRE types directly; it uses
  `ClosedLoopMotor` (a library-provided abstraction) or goes through
  the hardware layer.

**Harder:**

- Cross-cutting needs (e.g., a util that wants subsystem-level data)
  must be designed carefully to avoid upward references.
- Contributors must sometimes refactor to move a feature to the right
  layer rather than expedient placement.

## Alternatives Considered

- **Feature-based packaging (no layers).** Rejected — cycles form
  quickly, and test isolation becomes impossible.
- **Fewer layers (merge hardware + control).** Rejected — control
  algorithms are independent of hardware specifics (see `PIDSimple`
  with no hardware dependency), and merging conflates concerns.

## References

- [SDD-team271-lib.md §2](../sdd/SDD-team271-lib.md)
- [docs/internal/team271-lib-dependency-diagram.mmd](../../internal/team271-lib-dependency-diagram.mmd)
- [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md)
