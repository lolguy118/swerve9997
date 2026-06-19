# ADR-019: Lift the 4-Motor Transmission Cap — Variable-Arity Followers

## Status

Proposed

## Date

2026-06-19

## Context

`TransmissionBase` caps a transmission at **4 motors** (1 leader + 3 followers). The
cap is enforced by four named fields — `mLeader`, `mFollower1`, `mFollower2`,
`mFollower3` — and by `TransmissionFX`'s constructor overloads that accept exactly 1,
2, or 3 followers. The cap is **design-imposed, not vendor-imposed**: Phoenix 6 places
no limit on follower count.

Notably, the operational surface already scales. `mAllControllers` (a `LinkedHashSet`)
is the single collection every per-motor operation iterates — config apply, current
limits, neutral mode, `stop()`, telemetry, and simulation all walk that set, which is
unbounded. Only **construction and follower registration** are capped by the named
fields and the constructor overloads.

A concrete 2027 mechanism (owner-identified) needs more than four motors, which is the
trigger the reserved Planned-ADR slot named: "change when a concrete mechanism (e.g.,
6-wheel tank, exotic climber) needs it."

## Decision

Lift the 4-motor cap. Replace the fixed `mFollower1`/`mFollower2`/`mFollower3` fields
and the 1/2/3-follower constructor overloads with a **variable-arity, additive follower
API** — an `addFollower(CANDeviceID id, boolean opposeLeader)` method on
`TransmissionFX`, mirroring the library's existing additive hardware API
(`addEncoderFX`, `addCANCoder`, `addShifter`). `mAllControllers` remains the **single
source of truth** for all motors. Each follower's controller and its control-request
objects are **pre-allocated at construction/registration time**, never in a periodic
loop.

Implementation is **deferred to the implementation loop**; this ADR fixes the decision
and the constraints below. It is `Proposed` pending owner acceptance.

## Rationale

- The cap is artificial: Phoenix 6 supports arbitrary followers and `mAllControllers`
  is already unbounded, so most code needs no change.
- An additive `addFollower()` matches the existing builder-style hardware API and ends
  the combinatorial growth of follower constructor overloads (1, 2, 3, … N).
- Keeping `mAllControllers` as the single source of truth means config / stop /
  telemetry / simulation code is unchanged.
- Pre-allocating each follower's control requests at registration preserves
  CODE-GEN-004 (no object allocation in periodic methods) and the bulk-CAN-refresh
  registration model ([ADR-009](ADR-009-centralized-can-refresh.md)).

## Consequences

What becomes easier:

- 6-wheel tank drives, multi-motor climbers, and other >4-motor mechanisms become
  expressible; the constructor surface shrinks rather than grows.

What becomes harder or constrained:

- The implementation must migrate existing 1/2/3-follower call sites to `addFollower()`
  — a mechanical migration the implementation loop owns.
- The fixed `mFollower1`/`mFollower2`/`mFollower3` fields are deprecated; follow the
  deprecation lifecycle (add to `.claude/rules/deprecated-symbols.txt` before removal).
- Per-follower control requests must be pre-allocated at registration to honor
  CODE-GEN-004.
- [SDD-hardware](../sdd/SDD-hardware.md) §3 must document the additive API when the
  code ships.

## Alternatives Considered

- **Higher fixed cap** (e.g., `mFollower1`..`mFollower7`) — rejected. It only moves the
  arbitrary limit and worsens the named-field and constructor-overload sprawl.
- **Varargs constructor** (`TransmissionFX(…, CANDeviceID... followers)`) — rejected as
  the primary API: it cannot cleanly pair each follower with its own `opposeLeader`
  flag. It may later be added as a thin convenience over `addFollower()`.
- **Leave the cap (status quo)** — rejected: blocks the concrete 2027 mechanism.

## References

- [ADR-003: Layered Architecture](ADR-003-layered-architecture.md)
- [ADR-005: Passthrough — Wrapper, Not Wall](ADR-005-passthrough-wrapper-not-wall.md)
- [ADR-009: Centralized Bulk CAN Refresh](ADR-009-centralized-can-refresh.md)
- [SDD-hardware](../sdd/SDD-hardware.md) — transmission decomposition (update when code ships)
- [Planning README](../README.md) — the Planned-ADR slot this ADR fills
- Coding standard CODE-GEN-004 (no allocation in periodic) —
  [`../../coding-standard/README.md`](../../coding-standard/README.md)
