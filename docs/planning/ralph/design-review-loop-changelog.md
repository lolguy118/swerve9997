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

## Pass 2 — 2026-06-19

- **Item advanced:** item 6 (null-safety annotation policy). Authored
  [ADR-018](../../team-lib/planning/adr/ADR-018-null-safety-annotation-policy.md) (Status: Proposed) —
  adopt **JSpecify** annotations enforced by **NullAway** (an Error Prone plugin; Error Prone is already
  Adopted per SVP), `@NullMarked` package default, rolled out layer by layer in ADR-003 order starting at
  `api`. The Phase-B sweep confirmed the concrete trigger: SVP reserves a Planned NullAway gate that
  required this ADR first.
- **Files touched:** new `docs/team-lib/planning/adr/ADR-018-null-safety-annotation-policy.md`; planning
  `README.md` (moved the row from Planned ADRs into the ADR table as Proposed); `SVP.md` (NullAway row now
  cites ADR-018); plus this loop's ledger / scorecard / changelog.
- **DR-2 parked:** raised `OPEN QUESTION (needs owner)` — is a TalonFXS-backed transmission intended?
  `ControllerTalonFXS` exists but no transmission class does. Set DR-2 to `backlog` (non-blocking); the
  owner decides at loop end (add a Planned SDD + impl-loop item, or remove the stale §2 reference).
- **Sweep results (Phase B):** no NEW drift beyond the already-known DR-2 → 0 new items this pass
  (stability window: 1 of 2). Item 4 ADR↔code: clean sweep 2 (no reversals found).
- **Convergence:** not met — item 8 (>4-follower ADR) still `pending`; item 3 awaits a final clean sweep.
  Pass ended normally.

## Pass 3 — 2026-06-19

- **Item advanced:** item 8 (unlimited followers). Authored
  [ADR-019](../../team-lib/planning/adr/ADR-019-lift-transmission-motor-cap.md) (Status: Proposed) — lift
  the 4-motor cap with a variable-arity, additive `addFollower(CANDeviceID, opposeLeader)` API. Grounded in
  the actual code: the cap is the four named fields (`mLeader`, `mFollower1`..`3`) + `TransmissionFX`'s
  1/2/3-follower constructor overloads, while `mAllControllers` (the iterated set) is already unbounded —
  so operational code already scales. Constraint captured: per-follower control requests stay pre-allocated
  (CODE-GEN-004). Implementation is deferred to the impl loop.
- **Files touched:** new `docs/team-lib/planning/adr/ADR-019-lift-transmission-motor-cap.md`; planning
  `README.md` (added to ADR table as Proposed; removed its Planned-ADR row); plus this loop's
  ledger / scorecard / changelog.
- **Sweep results (Phase B):** read `TransmissionBase` + `TransmissionFX` — code matches the SDD except the
  already-known DR-2; no new drift → 0 new items (stability window: 2 of 2).
- **Convergence:** NOT YET — items 3 & 4 (the drift-reconciliation engines) have not had every SDD / ADR
  row checked; pass 1 covered README/map + 3 SDDs only. Next pass runs a comprehensive sweep of the
  remaining SDDs and Accepted ADRs to satisfy their "every row checked" done-bar before any completion.
  Pass ended normally.

## Pass 4 — 2026-06-19

- **Items advanced:** items 3 & 4 (the drift-reconciliation engines), closed to `done`. A comprehensive
  read-only sweep covered the remaining 8 SDDs (team271-lib, api, vendor-ctre, subsystem, auto, sysid, nt,
  util) — every documented §3 class/interface exists in code; **0 phantoms, 0 undocumented gaps**. ADR
  spot-check: ADR-004 (no singletons), ADR-009 (centralized refresh), ADR-013 (composition over commands),
  ADR-015/016 (LoggedNTInput tuning) all CONFIRMED in code; no reversals.
- **Files touched:** this loop's ledger / scorecard / changelog only (no corpus changes — the sweep was
  clean).
- **Convergence:** **MET.** All 10 ledger items are `done` (1–6, 8, DR-1, plus 3 & 4) or `backlog` (7,
  DR-2). Passes 2, 3, and 4 each discovered 0 new items (two-pass stability window satisfied). The single
  remaining `OPEN QUESTION (needs owner)` — DR-2 (TalonFXS transmission?) — is on a non-blocking backlog
  item and does not block any foundational artifact. The loop emits its completion tag this pass.
