# Loop-authoring protocol (shared spine for the `*-review-loop-init` commands)

This document is the **shared authoring spine** that every `*-review-loop-init` slash command follows.
Each `init` command is **thin**: it links here, then supplies only its own **deltas**. Running an `init`
**once** authors a turnkey Ralph loop for THIS repo and then **stops** — it creates the loop's files; it
does NOT launch it (launching is a separate, explicit `*-review-loop-run` / `ralph-loop` invocation).

> **This file is reference material, not a slash command.** It lives outside `.claude/commands/` on
> purpose, so Claude Code does not register it as a `/command`. Do not move it under `commands/`.

## Two sub-families

The loops split into two families. **Each command carries its own sub-family gate as one of its deltas —
this protocol deliberately does not state the gates**, so the shared spine stays true for all loops:

- **Doc loops** (`doc-review-loop`, `design-review-loop`) — fill a documentation corpus from a goals /
  vision lens. Their delta adds the doc-discipline rule and a liability self-guard.
- **Code loops** (`impl-review-loop`, `test-review-loop`, `refactor-review-loop`,
  `migration-review-loop`) — change a code surface while keeping the suite green. Their delta adds a
  build-order rule and a real-toolchain verification gate.

A command's delta names the family it belongs to and embeds that family's gate verbatim.

## Step 1 — Gather inputs (the lens)

Establish the **lens** — the weighting the loop ranks every work item by — from the FIRST available
source, in this order:

1. **Inline.** If the owner passed the lens as `$ARGUMENTS`, use it verbatim.
2. **Pre-filled.** Else if the command's `<PLACEHOLDER>` block no longer holds the literal placeholder
   token (the owner edited it in), use that.
3. **Menu.** Else build the lens with the owner now. Do a QUICK recon (project type from `README`,
   manifests, top-level layout) so the suggestions are relevant, then call `AskUserQuestion` — one
   `multiSelect: true` question of ≤4 repo-tailored options (or up to ~3 grouped `multiSelect` questions
   if the repo spans more dimensions than fit four options). Keep labels terse; put the "why this matters
   here" in each description. The harness adds an "Other" free-text option automatically, so never spend a
   slot on one. If the owner picks nothing or cancels, fall back to asking open-endedly. Do NOT proceed
   lens-less — a wrong or missing lens mis-prioritizes the entire run.

Then **synthesize, echo, confirm.** Fold the picks (and any "Other" text) into a short, priority-ordered
lens statement; echo it back; ask the owner to confirm or reorder it (a `multiSelect` returns an
unordered set, but the loop weights by order, so the order must be settled here). Treat the confirmed
statement as the command's `<PLACEHOLDER>` for the rest of the command.

## Step 2 — Repo audit

Audit THIS repo yourself (you have read access). Establish, by looking — not assuming: the repo state and
project phase; what the relevant corpus/surface already covers; and the gaps the lens implies but the repo
lacks. Each command's delta says exactly what to audit (a doc corpus, a spec corpus + code, a test suite,
a debt surface, an old-pattern surface).

## Step 3 — Derive the frozen taxonomy

From the lens + audit, derive a **priority-ordered taxonomy** of work items, highest-value first, ordered
by the owner's weighting. Give each item a one-line **done-bar** (what makes it finished). Mark
scale-out / post-launch items as **backlog by default** so they never block the near term. Once written
into the driver this taxonomy is **frozen**: the loop may append newly discovered items but must never
reweight or delete it.

## Step 4 — Write the loop, then STOP

Create these four files; do NOT launch the loop:

1. **`docs/planning/ralph/<loop>.prompt.md`** — the loop **driver**. Structure it
   ROLE → FILES (read in order each pass) → PHASE A–F → HARD RULES → the frozen taxonomy/rubric block.
   Author the phases so each pass runs:
   - **A. Orient** — read the ledger, scorecard, changelog, and `CLAUDE.md` (the routing index); never
     read any `docs/planning/ralph/*.prompt.md` file beyond this driver (they may carry a completion tag
     that must not be echoed).
   - **B. Sweep** — *(command delta)* re-scan the corpus/surface + lens; append newly found work to the
     ledger.
   - **C. Select** — the single highest-priority item that is unfinished and whose prerequisites (if any)
     are met. Backlog items are not selected unless promoted.
   - **D. Advance** — *(command delta)* take exactly that one item to its done-bar.
   - **E. Record** — update the ledger status and the `CLAUDE.md` routing index when the item introduces
     something a reader must find; append one scorecard row + one changelog block; git-commit the touched
     files as the pass checkpoint.
   - **F. Converge?** — if every convergence condition holds, emit the loop's completion phrase wrapped in
     the ralph-loop promise markers as the SOLE final line; otherwise end the pass normally. This wrapped
     emission is the ONE place the wrapped tag is ever written.
2. **`docs/planning/<loop>-ledger.md`** — a stub table listing every taxonomy item with a **status** drawn
   from a small fixed set the command names (e.g. `pending` → in-progress → finished — where "finished" is
   the command's done word, such as `done` or `core-complete` — plus `backlog`), the per-item fields the
   command's taxonomy defines (at least priority and done-bar; code loops also carry a spec ref and a
   depends-on), and a "Newly discovered items" append area.
3. **`docs/planning/ralph/<loop>-scorecard.md`** — an append-only stub: one header row documenting its
   columns — at minimum pass, date, items finished, items backlog, open gaps, and new items this pass (code
   loops add a suite-status column) — plus the convergence definition; no data rows yet.
4. **`docs/planning/ralph/<loop>-changelog.md`** — an append-only stub: a title + a one-line format note;
   no pass entries yet.

Embed the universal HARD RULES below, plus the command's own sub-family rule(s), into the driver verbatim.

## Universal HARD RULES (write these into every driver)

1. **Right-sized increments + backlog.** Each item is advanced to its done-bar OR explicitly deferred to
   `backlog` with a one-line reason. Never pad to look complete — every artifact or line is surface to
   maintain, so the smallest increment that satisfies the item beats speculative work.
2. **One item per pass (anti-thrash).** Exactly one work item is created or advanced per pass: sweep,
   select the single highest-priority item, advance only it, record, commit. A problem found mid-pass is
   appended as a NEW ledger item in the next Sweep — never re-open a finished item mid-pass.
3. **Convergence — closure with a stability window.** Emit the completion tag ONLY when ALL hold: every
   ledger item is finished OR `backlog`; the last TWO consecutive passes each discovered ZERO new items;
   and no `OPEN GAP:` (or other `OPEN …:`) markers remain anywhere. A no-op pass (nothing left to safely
   advance) is valid and is what lets the two-pass stability window trigger.
4. **Completion-tag discipline.** The loop is launched with `--completion-promise '<PHRASE>'`; the
   ralph-loop Stop hook ends the loop the moment the assistant's final message contains that phrase
   wrapped in promise markers. That **wrapped** tag must appear in EXACTLY ONE place across the driver and
   any pass output: the sole final line of a genuinely converged pass (the Phase F emission). Write it
   nowhere else — not in narration, not in the ledger/scorecard/changelog, not in a rule. Naming the bare
   phrase to explain the rule is fine; only the wrapped form triggers the hook.

Inherit the host repo's conventions throughout: no cross-doc duplication (one authoritative doc per topic,
others link to it); `CLAUDE.md` stays a routing index (add a one-line entry, never paste content); relative
links resolve; obey the host's markdown line-length limit; never edit `.github/workflows/*` or Accepted
ADRs.

## Step 5 — Print the launch line, then stop

Print the exact one-line launch command for the owner (an `*-review-loop-run` invocation, or the
equivalent `/ralph-loop:ralph-loop "…" --max-iterations 40 --completion-promise '<PHRASE>'`), then tell
them the loop is written and committed-ready but is NOT running — they start it with that command and stop
it early with `/ralph-loop:cancel-ralph`.
