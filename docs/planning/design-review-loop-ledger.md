# Design-review loop — ledger (Team271-Lib)

Frozen taxonomy + per-item status for the design-review Ralph loop. The driver is
[`ralph/design-review-loop.prompt.md`](ralph/design-review-loop.prompt.md). The loop may **append** newly
discovered items but must never reweight or delete this taxonomy.

**Lens:** whole-library gap & drift **reconcile, not regenerate**. The corpus
([`../team-lib/planning/`](../team-lib/planning/) — SDP/SRS/SVP/SCMP, 17 ADRs, 11 SDDs) is mature; the loop
closes genuine gaps and ADR↔SDD↔code drift only.

**Status values:** `pending` → `in-progress` → `done`, plus `backlog` (deferred, with a reason).

## Seed taxonomy

| # | Item | Priority | Done-bar | Status | Notes |
| - | ---- | -------- | -------- | ------ | ----- |
| 1 | Product brief / vision | — | Purpose, audience, scope, success, constraints captured | `done` | Satisfied by [`../../README.md`](../../README.md) + SDP + SRS; reconcile lens forbids regenerating |
| 2 | Architecture overview / layering | — | Layer graph + rationale documented | `done` | Satisfied by [ADR-003](../team-lib/planning/adr/ADR-003-layered-architecture.md) + dependency diagram |
| 3 | SDD↔code drift reconciliation | high | Every Package-to-SDD-Map row checked; each divergence captured + fixed on SDD/code side; two clean sweeps | `done` | All 11 SDDs swept (pass 1: README/map + hardware/control/vision; pass 4: remaining 8). Found+fixed DR-1; DR-2 backlogged. Passes 2–4 found 0 new (stability met) |
| 4 | ADR↔implementation drift reconciliation | high | Every Accepted ADR's decision still reflected in code; reversals captured via new superseding ADR; two clean sweeps | `done` | Major ADRs (004/009/013/015/016) spot-checked + structural confirmation across sweeps; no reversals found |
| 5 | Planning-README consistency | medium | ADR/SDD/Planned tables + Package-to-SDD Map match files on disk; links resolve | `done` | Pass 1 sweep: all 3 tables + map verified against disk; planned vision/trajectory packages correctly marked; links resolve |
| 6 | Reserved ADR — null-safety annotation policy | medium | Author ADR (annotation-set choice + rollout plan); Status `Proposed` until rollout starts | `done` | Pass 2: [ADR-018](../team-lib/planning/adr/ADR-018-null-safety-annotation-policy.md) authored (Proposed) — JSpecify + NullAway, layered rollout; unblocks the SVP NullAway gate |
| 7 | Reserved ADR — supply-chain / CVE response | medium | ADR written IFF concrete trigger exists; else backlog | `backlog` | **Owner deferred 2026-06-19**: keep ad hoc until a dependency-review CI gate is adopted. Carries liability self-guard if written |
| 8 | Reserved ADR — unlimited followers in `TransmissionBase` | medium | Author ADR lifting the 4-motor cap; impl loop implements the code change to match | `done` | Pass 3: [ADR-019](../team-lib/planning/adr/ADR-019-lift-transmission-motor-cap.md) authored (Proposed) — additive `addFollower()` API, `mAllControllers` as source of truth; impl loop implements |

Items 6–8 mirror the existing **Planned ADRs** table in the planning README — reserved, not speculative.
Per owner triggers on **2026-06-19**, items 6 and 8 are **promoted** to `pending`; item 7 stays `backlog`.
When the loop authors a promoted ADR (Phase D), it moves that row from **Planned ADRs** to the Accepted ADR
table in [`../team-lib/planning/README.md`](../team-lib/planning/README.md).

## Newly discovered items

The loop appends rows here during Phase B (Sweep), each with its own one-line done-bar.

| # | Item | Found pass | Priority | Done-bar | Status |
| - | ---- | ---------- | -------- | -------- | ------ |
| DR-1 | SDD-hardware §3 names phantom `EncoderCANCoderComp` (not in code; duplicates `EncoderCTRE` latency comp) | 1 | high | Remove the phantom line from the §3.3 sensor tree | `done` |
| DR-2 | SDD-hardware §2 Scope names `TransmissionFXS` (no transmission class on disk; `ControllerTalonFXS` does exist) | 1 | high | `OPEN QUESTION (needs owner)`: is a TalonFXS-backed transmission intended? Yes → add Planned SDD + impl-loop item; No → remove from SDD-hardware §2. Parked (non-blocking) | `backlog` |
