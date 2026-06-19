# Design-review loop — changelog (Team271-Lib)

Append-only narrative log, one block per pass. The loop writes here in Phase E; do not edit past entries.

Format per block: `## Pass N — YYYY-MM-DD` heading, then the item advanced, what changed (files touched),
and any `OPEN QUESTION (needs owner)` raised or resolved.

## Pass 1 — 2026-06-19

- **Item advanced:** DR-1 — removed phantom class `EncoderCANCoderComp` from the SDD-hardware §3.3 sensor
  tree. It existed in no source file, was referenced nowhere else, and its stated purpose (latency
  compensation) is already a documented capability of the parent `EncoderCTRE`. Design-loop reconciliation
  is doc-side only (the loop does not write code), so the stale line was removed rather than implemented.
- **Files touched:** `docs/team-lib/planning/sdd/SDD-hardware.md` (removed the phantom line; bumped header
  Revision 0.1 → 0.2, Date → 2026-06-19), plus this loop's ledger / scorecard / changelog.
- **Sweep results (Phase B):** README ADR/SDD/Planned tables + Package-to-SDD Map — CLEAN; SDD-control §3 —
  CLEAN; SDD-vision — correctly marked Planned; ADR↔code — CLEAN (clean sweep 1 of 2 for item 4).
- **New items appended:** DR-1 (fixed this pass); DR-2 — SDD-hardware §2 Scope names `TransmissionFXS`,
  which has no transmission class on disk although `ControllerTalonFXS` does exist. Left `pending`; next
  pass must reconcile (mark Planned vs. remove) and likely raise an `OPEN QUESTION (needs owner)` on whether
  a TalonFXS-backed transmission is intended.
- **Convergence:** not met — items 3, 4, 6, 8, DR-2 still open. Pass ended normally.
