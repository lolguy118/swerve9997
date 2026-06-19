# Impl-review loop — ledger (Team271-Lib)

Frozen taxonomy + per-item status for the implementation-review Ralph loop. The driver is
[`ralph/impl-review-loop.prompt.md`](ralph/impl-review-loop.prompt.md). The loop may **append** newly
discovered items (or spec drift) but must never reweight or delete this taxonomy.

**Scope (lens):** build the design-phase outputs — the ADR-019 follower-API cap-lift and the planned
`TransmissionFXS` — to spec-of-record; defer the large cross-cutting items. Code sub-family: TDD, full suite
green against the real `./gradlew` toolchain, self-review each pass (VERIFY GATE).

**Status values:** `pending` → `in-progress` → `done`, plus `backlog` (deferred, with a reason).

## Seed taxonomy

| # | Item | Priority | Spec ref | Depends-on | Done-bar | Status |
| - | ---- | -------- | -------- | ---------- | -------- | ------ |
| I1 | ADR-019 follower-API cap-lift | high | [ADR-019](../team-lib/planning/adr/ADR-019-lift-transmission-motor-cap.md) | — | Additive `addFollower` (registers via hoisted `TransmissionBase.registerFollower`); `mAllControllers` is source of truth; `mFollower1/2/3` + overloads `@Deprecated` (field-scoped, not in `deprecated-symbols.txt` which is api/-only); per-follower requests pre-allocated (CODE-GEN-004); >4-motor + dup-CAN-ID + null tests pass; full suite green; review fixes applied | `done` |
| I2 | Implement `TransmissionFXS` | high | [SDD-hardware](../team-lib/planning/sdd/SDD-hardware.md) §2 + ADR-019 pattern | I1 | TalonFXS-backed transmission (via `ControllerTalonFXS`), peer of `TransmissionFX` using the additive API; acceptance test mirroring `TransmissionFXTest` passes; SDD-hardware updated (drop "planned", add §3 entry); full suite green; review clean | `pending` |
| I3 | ADR-018 null-safety rollout (api layer) | backlog | [ADR-018](../team-lib/planning/adr/ADR-018-null-safety-annotation-policy.md) | — | `api/` packages `@NullMarked`; NullAway wired as a warning; suite green | `backlog` |
| I4 | Vision layer | backlog | [SDD-vision](../team-lib/planning/sdd/SDD-vision.md) (Planned) | api | Vendor-neutral vision API + a vendor impl per the SDD; suite green | `backlog` |
| I5 | Trajectory layer | backlog | [SDD-auto](../team-lib/planning/sdd/SDD-auto.md) (Planned) | auto | Trajectory follow API + PathPlanner/Choreo impls per the SDD; suite green | `backlog` |

Build order: I1 → I2 (I2 builds on the additive API so it never adopts the soon-deprecated overloads).
I3–I5 are backlog and selected only if promoted by the owner.

## Newly discovered items

The loop appends rows here during Phase B (Sweep) — new work items or spec drift, each with its own
spec ref, depends-on, and done-bar.

| # | Item | Found pass | Priority | Spec ref | Depends-on | Done-bar | Status |
| - | ---- | ---------- | -------- | -------- | ---------- | -------- | ------ |
