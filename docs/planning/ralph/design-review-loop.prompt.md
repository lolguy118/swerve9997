# Design-review loop — driver (Team271-Lib)

This is the **driver** for the definition-and-design Ralph loop. It was authored once by
`/design-review-loop-init` and is launched by `/design-review-loop-run`. Each pass advances **exactly one**
design artifact to its done-bar, records the result, commits, and checks for convergence.

> This loop is scoped to a **mature** repo. Its lens is **whole-library gap & drift reconcile, NOT
> regenerate**: Team271-Lib already has SDP/SRS/SVP/SCMP, 17 Accepted ADRs, and 11 SDDs covering every
> layer. The loop's job is to find and close *genuine* design gaps and ADR↔SDD↔code drift — never to
> re-author or duplicate an artifact that is already at its done-bar.

## ROLE

You are a principal FRC-library architect doing a design-reconciliation pass. You protect an existing,
working design corpus. You add only what is missing or has drifted; you regenerate nothing.

## FILES — read these each pass, in this order

1. This driver (you are reading it). Do **not** read any other `docs/planning/ralph/*.prompt.md` file — a
   sibling driver may carry a completion tag that must never be echoed.
2. Ledger — [`../design-review-loop-ledger.md`](../design-review-loop-ledger.md) — the frozen taxonomy +
   per-item status. This is your work list.
3. Scorecard — [`design-review-loop-scorecard.md`](design-review-loop-scorecard.md) — append-only metrics.
4. Changelog — [`design-review-loop-changelog.md`](design-review-loop-changelog.md) — append-only pass log.
5. Routing index — [`../../../CLAUDE.md`](../../../CLAUDE.md) — the repo's authoritative-doc router.
6. Planning map — [`../../team-lib/planning/README.md`](../../team-lib/planning/README.md) — the ADR table,
   the **Planned ADRs** / **Planned SDDs** tables, and the **Package-to-SDD Map**.
7. Doc + planning guardrails — [`../../../.claude/rules/planning.md`](../../../.claude/rules/planning.md) and
   [`../../../.claude/rules/docs.md`](../../../.claude/rules/docs.md) — ADR/SDD format, requirement-ID
   scoping, no-duplication, no-numeric-tunables, 140-char line limit.

## PHASES — run A → F every pass

- **A. Orient.** Read the files above. Restate the single highest-priority unfinished item you intend to
  advance and why it is unblocked.
- **B. Sweep.** Re-scan the design corpus against the code it governs:
  - For each row of the **Package-to-SDD Map**, compare the SDD's Module Decomposition, telemetry table,
    and documented behavior against the actual package. Any divergence (new/renamed/removed class,
    telemetry key mismatch, behavior the SDD no longer describes) is **drift** — append it as a NEW ledger
    item with its own one-line done-bar.
  - Confirm every major architectural decision embodied in the code has an Accepted ADR. A decision present
    in code with no ADR is a gap — append it.
  - Confirm the planning-README tables (ADR list, Planned ADRs/SDDs, Package-to-SDD Map) match the files on
    disk under `adr/` and `sdd/`. A table/file mismatch is a gap — append it.
  - Append each finding to the ledger's "Newly discovered items" area. Do not fix anything in this phase.
- **C. Select.** Pick the single highest-priority item that is `pending`, unblocked, and not `backlog`.
  A reserved-decision item is selectable ONLY if its concrete trigger now exists (see taxonomy); otherwise
  it stays `backlog`.
- **D. Advance.** Take that one item to its done-bar, obeying every HARD RULE below:
  - Drift item → fix it on the **SDD or code** side, or by writing a **new** ADR. Never edit an Accepted
    ADR (see HARD RULES). Keep the SDD's fixed nine-section format and the ADR's fixed template.
  - Reserved-decision item with a now-real trigger → author the ADR under
    [`../../team-lib/planning/adr/`](../../team-lib/planning/adr/) using the next sequential number and the
    template in [`../../team-lib/planning/adr/README.md`](../../team-lib/planning/adr/README.md); move its
    row from **Planned ADRs** to the Accepted ADR table in the planning README; add an SDD only if code
    ships with it.
  - A genuine product/owner judgment call you cannot derive → do **not** invent it. Record it in the ledger
    as `OPEN QUESTION (needs owner): …`, then either advance a different unblocked item or end the pass.
  - Never re-author an artifact already at its done-bar.
- **E. Record.** Update the item's ledger status; if the change introduces something a reader must find,
  add a one-line entry to the relevant routing index (`CLAUDE.md` / planning `README.md`) — a link, never
  pasted content. Append one scorecard row and one changelog block. Then git-commit the touched files as
  this pass's checkpoint.
- **F. Converge?** If — and only if — every convergence condition holds (see HARD RULE 3 plus the
  design-loop addition), emit the completion phrase `DESIGN REVIEW LOOP COMPLETE` wrapped in the ralph-loop
  promise markers as the SOLE final line of your message. Otherwise end the pass normally. This is the ONE
  place that wrapped tag is ever written.

## HARD RULES (do not violate)

1. **Right-sized increments + backlog.** Advance each item to its done-bar OR defer it to `backlog` with a
   one-line reason. Never pad to look complete — every doc is surface to maintain.
2. **One item per pass (anti-thrash).** Create or advance exactly one item per pass. A problem found
   mid-pass is appended as a NEW ledger item for the next Sweep — never re-open a finished item mid-pass.
3. **Convergence — closure with a stability window.** Emit the completion tag ONLY when ALL hold: every
   ledger item is `done` OR `backlog`; the last TWO consecutive passes each discovered ZERO new items; and
   no `OPEN GAP:` / `OPEN QUESTION (needs owner)` marker remains that blocks a still-unbuilt foundational
   artifact. A no-op pass (nothing safe to advance) is valid and is what triggers the two-pass window.
4. **Completion-tag discipline.** The wrapped completion tag appears in EXACTLY ONE place: the sole final
   line of a genuinely converged Phase F. Never in narration, the ledger, scorecard, changelog, or a rule.
   Naming the bare phrase to explain this rule is fine; only the wrapped form triggers the Stop hook.
5. **Liability self-guard (doc sub-family).** Every legal / financial / compliance / tax / business-model
   artifact this loop emits MUST begin, right after its H1, with this blockquote, verbatim:
   > **DRAFT — NOT legal or financial advice. Generated by an automated loop; requires professional and
   > human review before any reliance.**
   Such artifacts must also state their key assumptions explicitly. (For this repo this can apply to the
   reserved supply-chain / CVE-response ADR if it makes any compliance claim.)

### Repo conventions (inherited — also binding)

- **Accepted ADRs are permanent.** Never edit, rewrite, or delete an Accepted ADR. A reversed decision
  becomes `Superseded by ADR-XXX` and is captured in a NEW ADR written alongside it.
- **No speculative ADRs/SDDs.** Author a reserved ADR/SDD only when a concrete consumer/trigger exists
  (per [`../../team-lib/planning/README.md`](../../team-lib/planning/README.md)). Absent a trigger, the
  slot stays `backlog` with an `OPEN QUESTION (needs owner)`.
- **No cross-doc duplication.** One authoritative doc per topic; others link to it. `CLAUDE.md` stays a
  routing index — add a one-line link, never pasted content.
- **Fixed formats.** ADRs use the `adr/README.md` template; SDDs use the fixed nine-section template.
  Requirement IDs `[PREFIX-NNN]` only in the locations `planning.md` allows.
- **No numeric tunables in docs** — reference the constant name, never the value.
- **Relative links must resolve**; cross-references use file-relative paths. Max line length **140 chars**.
- **Never edit** `.github/workflows/*`.

## FROZEN TAXONOMY (priority-ordered; the loop may append, never reweight or delete)

The full status table lives in the ledger. Summary of the seed items, highest value first:

1. **Product brief / vision** — done-bar: purpose, audience, scope, success, constraints captured.
   **Already satisfied** by [`../../../README.md`](../../../README.md), the SDP, and the SRS — mark `done`;
   do NOT regenerate.
2. **Architecture overview / layering** — done-bar: the layer graph and its rationale are documented.
   **Already satisfied** by ADR-003 + the dependency diagram — mark `done`.
3. **SDD↔code drift reconciliation** (recurring) — done-bar: every Package-to-SDD-Map row checked, each
   divergence captured as a discrete item and fixed on the SDD/code side; two clean sweeps in a row.
4. **ADR↔implementation drift reconciliation** (recurring) — done-bar: every Accepted ADR's decision is
   still reflected in code; any reversal captured via a NEW superseding ADR; two clean sweeps in a row.
5. **Planning-README consistency** — done-bar: the ADR/SDD/Planned tables and Package-to-SDD Map match the
   files on disk; links resolve.
6. **Reserved ADR — null-safety annotation policy** — done-bar: ADR written IFF a concrete trigger exists
   (e.g., NullAway being adopted as a CI gate / an annotation rollout starting); else `backlog` +
   `OPEN QUESTION (needs owner)`.
7. **Reserved ADR — supply-chain / CVE response** — done-bar: ADR written IFF a concrete trigger exists
   (adopting a dependency-review gate / defining hotfix-severity policy); else `backlog` + OPEN QUESTION.
   If written, it carries the liability-self-guard blockquote.
8. **Reserved ADR — unlimited followers in `TransmissionBase`** — done-bar: ADR written IFF a concrete
   mechanism needs >4 motors; else `backlog` + OPEN QUESTION.

Items 6–8 are the repo's existing **Planned ADRs**; they start `backlog` and are promoted only on a real
trigger. Items 3–5 are the live reconciliation engine. Items 1–2 are `done` on day one by design.
