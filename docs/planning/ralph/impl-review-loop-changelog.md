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

## Pass 2 — 2026-06-19

- **Item advanced:** I2 — implement `TransmissionFXS`. Spec ref: SDD-hardware §2 (was planned) + the
  ADR-019 additive-follower pattern. Depends-on I1 (satisfied), so it was built on `addFollower` from the
  start rather than the deprecated overloads.
- **TDD:** wrote `TransmissionFXSTest` (10 tests: construction, `addFollower` registration, >4 motors,
  null/dup guards, neutral mode, robotInit/outputTelemetry/stop/setOutputDuty smoke) first; red (class
  undefined) → implemented → green.
- **Code:** new `TransmissionFXS` — a **lean** TalonFXS peer of `TransmissionFX`: leader-only constructor
  (`ControllerTalonFXS`), `getLeaderController()`, `getLeader()` passthrough (ADR-005), and
  `addFollower(CANDeviceID, boolean)` (null + duplicate-CAN-ID guards; construct + `follow()` +
  `registerFollower`). Deliberately omits the TalonFX-only Motion Magic matrix and the `CTREMotor` wrapper
  (`CTREMotor` wraps `ControllerTalonFX` only) — smallest increment satisfying the spec. Config / outputs /
  sim / telemetry are inherited from `TransmissionBase`.
- **Docs:** SDD-hardware §2 + §3 tree now list `TransmissionFXS` as a real class (dropped the "planned"
  marker); rev 0.4 → 0.5.
- **Self-review (`lib-reviewer`):** found 1 Blocker — `addFollower` discarded the `follow()` status, so a
  cross-bus follower would register but sit idle with no driver alert (silent failure; regression vs.
  `ControllerTalonFX`'s follower constructor, which reports it). **Fixed:** capture the status and
  `DriverStation.reportError` on non-OK. Two nits (double-cast `getLeader`, unguarded constructor params)
  accepted as deliberate parity with the `TransmissionFX` peer.
- **Suite:** GREEN — `BUILD SUCCESSFUL`, all 10 new tests pass, no regressions.
- **Convergence:** **MET.** Build-now items I1 + I2 are `done`; I3 (null-safety rollout), I4 (vision), I5
  (trajectory) remain `backlog` (deferred per the run's scope). Passes 1 and 2 each found 0 new items
  (two-pass stability window). No `OPEN GAP:` markers. Full suite green. The loop emits its completion tag
  this pass.
