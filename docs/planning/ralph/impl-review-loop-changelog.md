# Impl-review loop — changelog (Team271-Lib)

Append-only narrative log, one block per pass. The loop writes here in Phase E; do not edit past entries.

Format per block: `## Pass N — YYYY-MM-DD` heading, then the item advanced, the spec ref it implements, what
changed (code / tests / docs touched), the real suite status, the self-review verdict, and any `OPEN GAP:`
raised or resolved.

## Pass 1 — 2026-06-19

- **Item advanced:** I1 — ADR-019 follower-API cap-lift. Spec ref:
  [ADR-019](../../team-lib/planning/adr/ADR-019-lift-transmission-motor-cap.md).
- **TDD:** wrote 3 acceptance tests first (`addFollowerRegistersWithAllControllers`,
  `addFollowerSupportsMoreThanFourMotors` — leader + 5 followers = 6 motors, `addFollowerNullIdThrows`);
  red (new API undefined) → implemented → green.
- **Code:** `TransmissionBase` — deprecated `mFollower1/2/3`; added hoisted `registerFollower(ControllerSmart)`
  (adds to `mAllControllers`, the single source of truth; populates the first three legacy fields for
  back-compat). `TransmissionFX` — deprecated the 1/2/3-follower constructor overloads; added
  `addFollower(CANDeviceID, boolean)` constructing a `ControllerTalonFX` from the leader's `MotorBase`.
  Control requests stay pre-allocated per controller (CODE-GEN-004 holds).
- **Self-review (`lib-reviewer`):** 2 Should-fix + 2 nits, all resolved this pass — (a) added a
  duplicate-CAN-ID guard in `addFollower` (safety rule) + a test; (b) updated SDD-hardware §3 (unbounded
  followers + additive API, ADR-019 ref; bumped rev 0.3→0.4) as ADR-019 mandates on code-ship; (c) added a
  back-compat field-population test; (d) cleaned the follower name. Confirmed `deprecated-symbols.txt`
  correctly NOT touched (it is api/-scoped; these are protected hardware fields).
- **Suite:** GREEN — `BUILD SUCCESSFUL`, all 5 new transmission tests pass, no regressions.
- **Convergence:** not met — I2 (`TransmissionFXS`) still `pending` (depends-on I1, now satisfied). Pass
  ended normally.
