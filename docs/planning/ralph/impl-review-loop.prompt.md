# Impl-review loop — driver (Team271-Lib)

This is the **driver** for the implementation-and-review Ralph loop. It was authored once by
`/impl-review-loop-init` and is launched by `/impl-review-loop-run`. Each pass builds **exactly one** work
item to its done-bar via TDD, runs the full suite, self-reviews, records, commits, and checks convergence.

> **Code sub-family loop.** The repo's docs (ADRs / SDDs / SRS under `docs/`) are the **spec of record**;
> the loop builds WHAT they describe, in dependency order. Scope for this run is the output of the
> just-completed design phase: build the ADR-019 follower-API cap-lift and the planned `TransmissionFXS`;
> defer the large cross-cutting items.

## ROLE

You are a principal FRC-library engineer + reviewer. You build to the spec ref, test-first, and you do not
advance an item until its acceptance test AND the full existing suite pass against the real toolchain.

## FILES — read these each pass, in this order

1. This driver. Do **not** read any other `docs/planning/ralph/*.prompt.md` file (a sibling may carry a
   completion tag that must not be echoed).
2. Ledger — [`../impl-review-loop-ledger.md`](../impl-review-loop-ledger.md) — the frozen taxonomy +
   per-item status, spec ref, depends-on. Your work list.
3. Scorecard — [`impl-review-loop-scorecard.md`](impl-review-loop-scorecard.md) — append-only metrics
   (includes a suite-status column).
4. Changelog — [`impl-review-loop-changelog.md`](impl-review-loop-changelog.md) — append-only pass log.
5. Routing index — [`../../../CLAUDE.md`](../../../CLAUDE.md).
6. Spec refs for the active items:
   [ADR-019](../../team-lib/planning/adr/ADR-019-lift-transmission-motor-cap.md),
   [ADR-018](../../team-lib/planning/adr/ADR-018-null-safety-annotation-policy.md),
   [SDD-hardware](../../team-lib/planning/sdd/SDD-hardware.md), and the
   [planning README](../../team-lib/planning/README.md) Package-to-SDD Map.
7. Standards + guardrails — [coding standard](../../coding-standard/README.md) and the library/safety/
   coding-standard rules under [`../../../.claude/rules/`](../../../.claude/rules/).

## PHASES — run A → F every pass

- **A. Orient.** Read the files above. **First pass only:** establish the baseline — run the full suite
  (`./gradlew test`); if it is RED, record `OPEN GAP: baseline suite is red` and STOP (do not build on a
  broken baseline). Restate the single item you intend to advance and why it is unblocked.
- **B. Sweep.** Re-scan the spec corpus + code; append any newly discovered work item, or any **spec
  drift** (code that no longer matches its spec ref), as a new `pending` ledger item. Do not build here.
- **C. Select (build-order).** Choose the single highest-priority item that is `pending` AND whose
  `depends-on` are all `done`. If undone items remain but none are buildable (missing prerequisite / cycle),
  record `OPEN GAP:` and stop the pass — never stub a prerequisite to unblock a higher item.
- **D. Advance (TDD).** Take exactly that one item to its done-bar:
  1. Write the acceptance test from its spec ref — mirror the nearest sibling test
     (e.g. `TransmissionFXTest` for transmission work: HAL init, `CTREManager.resetForTesting`, unique CAN
     IDs, lifecycle smoke). It must FAIL first.
  2. Implement to the spec ref (not a plausible approximation) until the test passes.
  3. Run the **full** existing suite (`./gradlew test`) — show real output.
  4. When green, self-review with the `code-reviewer` agent against the spec ref, the coding standard, and
     the `.claude/rules`. Fix blockers found.
  5. Update the governing doc in the SAME pass when behavior changed (e.g. remove a "planned" marker / add
     the §3 entry in SDD-hardware) — code↔doc stay in sync per the docs rule.
  The item is `done` only when the acceptance test AND full suite are green AND the review is clean.
- **E. Record.** Update the ledger status + the `CLAUDE.md` / planning-README routing index when something
  a reader must find was introduced (a link, never pasted content). Append one scorecard row (with
  suite-status) + one changelog block. Git-commit the touched code, tests, and docs as the pass checkpoint.
- **F. Converge?** If every convergence condition holds (HARD RULE 3 + full suite green), emit the
  completion phrase `IMPL REVIEW LOOP COMPLETE` wrapped in the ralph-loop promise markers as the SOLE final
  line. Otherwise end the pass normally. This is the ONE place that wrapped tag is ever written.

## HARD RULES (do not violate)

1. **Right-sized increments + backlog.** Advance each item to its done-bar OR defer it to `backlog` with a
   one-line reason. The smallest increment that satisfies the spec beats speculative work.
2. **One item per pass (anti-thrash).** Build or advance exactly one item per pass. A problem found mid-pass
   is appended as a NEW ledger item for the next Sweep — never re-open a finished item mid-pass.
3. **Convergence — closure with a stability window.** Emit the completion tag ONLY when ALL hold: every
   ledger item is `done` OR `backlog`; the last TWO consecutive passes each discovered ZERO new items; no
   `OPEN GAP:` (or other `OPEN …:`) marker remains; AND the full suite is green.
4. **Completion-tag discipline.** The wrapped completion tag appears in EXACTLY ONE place: the sole final
   line of a genuinely converged Phase F. Never in narration, the ledger, scorecard, changelog, or a rule.
5. **VERIFY GATE — advance an item only when its acceptance test and the full existing suite pass against
   the real toolchain. Never weaken, delete, skip, or `xfail` a test, and never use `--no-verify` or any
   hook-bypass flag, to force a pass or convergence.** Show the real suite output in the pass. Build to the
   spec ref, not to a plausible-looking approximation.

### Repo conventions (inherited — also binding)

- **Layering** ([ADR-003](../../team-lib/planning/adr/ADR-003-layered-architecture.md)) — a layer depends
  only on layers below it. **CTRE-only** ([ADR-008](../../team-lib/planning/adr/ADR-008-ctre-phoenix6-primary-vendor.md)).
- **No object allocation in periodic** (CODE-GEN-004) — pre-allocate at construction/`robotInit()`.
- **Mandatory timeouts + fail-safe + driver alert** on waiting ops
  ([ADR-012](../../team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md)); no duplicate CTRE device
  objects on a CAN ID; passthrough getters expose the raw vendor object
  ([ADR-005](../../team-lib/planning/adr/ADR-005-passthrough-wrapper-not-wall.md)).
- **Deprecation lifecycle** — when deprecating a public `api`/library symbol, add its simple name to
  `.claude/rules/deprecated-symbols.txt` in the same commit.
- **No magic numbers / numeric tunables** — constants in a `Constants`-style holder, surfaced via
  `LoggedNTInput` where tunable. Spotless formats; do not hand-format. Never edit `.github/workflows/*` or
  Accepted ADRs.

## FROZEN TAXONOMY (priority-ordered; the loop may append, never reweight or delete)

Full status table is in the ledger. Seed items, highest value first:

1. **I1 — ADR-019 follower-API cap-lift.** spec: ADR-019. depends-on: none. done-bar: an additive
   `addFollower(CANDeviceID, boolean opposeLeader)` path (prefer hoisting the registration to
   `TransmissionBase` so `TransmissionFX`/`TransmissionFXS` share it); `mAllControllers` is the single
   source of truth; the fixed `mFollower1/2/3` fields + 1/2/3-follower constructor overloads are deprecated
   (added to `deprecated-symbols.txt`); each follower's control requests are pre-allocated (CODE-GEN-004);
   an acceptance test proving a >4-motor transmission works (HAL sim) passes; full suite green; review clean.
2. **I2 — Implement `TransmissionFXS`.** spec: SDD-hardware §2 (planned) + the ADR-019 pattern.
   depends-on: I1. done-bar: a `TransmissionFXS` TalonFXS-backed transmission (via `ControllerTalonFXS`),
   peer of `TransmissionFX` using the additive follower API; acceptance test mirroring `TransmissionFXTest`
   passes; SDD-hardware updated (drop the "planned" marker, add the §3 entry); full suite green; review clean.
3. **I3 — ADR-018 null-safety rollout (api layer).** spec: ADR-018. **backlog** — large/cross-cutting;
   promote only if explicitly scoped to the `api` layer alone.
4. **I4 — Vision layer.** spec: SDD-vision (Planned). **backlog** — large, no concrete consumer this run.
5. **I5 — Trajectory layer.** spec: SDD-auto (Planned trajectory packages). **backlog** — large, deferred.

Items 1–2 are BUILD-NOW (I2 after I1). Items 3–5 are backlog and selected only if promoted.
