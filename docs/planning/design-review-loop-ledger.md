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
| 3 | SDD↔code drift reconciliation | high | Every Package-to-SDD-Map row checked; each divergence captured + fixed on SDD/code side; two clean sweeps | `pending` | Recurring engine; see driver Phase B |
| 4 | ADR↔implementation drift reconciliation | high | Every Accepted ADR's decision still reflected in code; reversals captured via new superseding ADR; two clean sweeps | `pending` | Recurring engine; never edit an Accepted ADR |
| 5 | Planning-README consistency | medium | ADR/SDD/Planned tables + Package-to-SDD Map match files on disk; links resolve | `pending` | Cross-check [`../team-lib/planning/README.md`](../team-lib/planning/README.md) |
| 6 | Reserved ADR — null-safety annotation policy | medium | Author ADR (annotation-set choice + rollout plan); Status `Proposed` until rollout starts | `pending` | **Promoted by owner 2026-06-19**: intent to adopt null-safety annotations / NullAway |
| 7 | Reserved ADR — supply-chain / CVE response | medium | ADR written IFF concrete trigger exists; else backlog | `backlog` | **Owner deferred 2026-06-19**: keep ad hoc until a dependency-review CI gate is adopted. Carries liability self-guard if written |
| 8 | Reserved ADR — unlimited followers in `TransmissionBase` | medium | Author ADR lifting the 4-motor cap; impl loop implements the code change to match | `pending` | **Promoted by owner 2026-06-19**: concrete >4-motor mechanism planned for 2027 |

Items 6–8 mirror the existing **Planned ADRs** table in the planning README — reserved, not speculative.
Per owner triggers on **2026-06-19**, items 6 and 8 are **promoted** to `pending`; item 7 stays `backlog`.
When the loop authors a promoted ADR (Phase D), it moves that row from **Planned ADRs** to the Accepted ADR
table in [`../team-lib/planning/README.md`](../team-lib/planning/README.md).

## Newly discovered items

_None yet. The loop appends rows here during Phase B (Sweep), each with its own one-line done-bar._

| # | Item | Found pass | Priority | Done-bar | Status |
| - | ---- | ---------- | -------- | -------- | ------ |
