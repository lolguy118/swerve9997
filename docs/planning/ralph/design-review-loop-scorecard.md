# Design-review loop — scorecard (Team271-Lib)

Append-only, one row per pass. The loop writes here in Phase E; do not edit past rows.

**Convergence** (all must hold to emit the completion tag): every ledger item is `done` or `backlog`; the
last TWO consecutive passes each found ZERO new items; and no `OPEN QUESTION (needs owner)` blocks a
still-unbuilt foundational artifact.

| Pass | Date | Item advanced | Items done | Items backlog | Open questions | New items this pass |
| ---- | ---- | ------------- | ---------- | ------------- | -------------- | ------------------- |
| 1 | 2026-06-19 | DR-1: remove phantom `EncoderCANCoderComp` from SDD-hardware §3 | 4 | 1 | 0 | 2 (DR-1, DR-2) |
| 2 | 2026-06-19 | Item 6: author ADR-018 null-safety (JSpecify + NullAway); park DR-2 as OPEN QUESTION | 5 | 2 | 1 | 0 |
| 3 | 2026-06-19 | Item 8: author ADR-019 lift transmission cap (additive `addFollower()`) | 6 | 2 | 1 | 0 |
| 4 | 2026-06-19 | Items 3 & 4: comprehensive sweep of remaining 8 SDDs + ADR spot-check — all clean | 8 | 2 | 1 | 0 |

**Converged at pass 4:** all 10 ledger items are `done` (8) or `backlog` (2); passes 2–4 each found 0 new
items (stability window met); the one remaining `OPEN QUESTION` (DR-2) is on a non-blocking backlog item.
