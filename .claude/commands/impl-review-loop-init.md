---
description: Author a spec-driven implementation-and-review loop for this repo (writes the driver + ledger + records; does not launch it).
argument-hint: "(no args)"
---

# Author the implementation-review loop for this repo

You are a principal engineer and reviewer. Running this command **once** authors
a turnkey implementation-and-review loop for THIS repo, then stops. You CREATE
the loop's files; you DO NOT launch it — launching is a separate, explicit
`ralph-loop` invocation.

The loop you author is a Ralph loop: a **frozen rubric**, **one-item-per-pass**
build that develops the repo's code to match its documented specs and reviews
each increment as it lands. The repo's docs (ADRs / specs / design) are the
spec of record — the loop builds WHAT they describe, in dependency order, and
converges when every in-scope work item is implemented, reviewed, and green.

## Step 1 — Gather inputs

**Implementation scope.** Read `<IMPL_SCOPE>` below. If it still holds the
literal placeholder token, STOP and ask the owner for their scope before writing
anything — the scope is the lens the loop weights and bounds every work item by
(what to build now vs. defer to backlog), and a wrong lens mis-prioritizes the
entire build.

<IMPL_SCOPE>

**Repo audit.** Audit THIS repo yourself (you have read access). Establish, by
looking — not assuming:

- the spec corpus: which behaviors the docs (ADRs / specs / design under `docs/`)
  actually specify, and where each is stated;
- the existing code & tests: what is already implemented, and what the test
  suite already covers;
- the gaps and drift: which specified behaviors the code lacks, and where
  existing code no longer matches the spec it was meant to implement.

## Step 2 — Derive the taxonomy

From the scope + audit, derive a **goal-weighted, dependency-ordered work-item
taxonomy**: the units of implementation the docs imply, highest-value first,
ordered so that nothing is selected before its prerequisites. Give each item:

- a **priority** (its weight under the owner's scope);
- a **spec ref** — the doc / section / ADR it implements, so the build is
  traceable to the docs and a reviewer can check code against intent;
- a **depends-on** — the items that must be `done` first (derive from the docs;
  a feature that needs an unbuilt foundation lists it here);
- a one-line **done-bar** — the acceptance criterion that says it is finished.

Mark scale-out / post-MVP items as **backlog by default**; they must not block
the near term. Once written into the driver this taxonomy is **frozen**: the
loop may append newly discovered items but must never reweight or delete it.

## Step 3 — Write the loop, then STOP

Create these files; you DO NOT launch the loop:

1. **`docs/planning/ralph/impl-review-loop.prompt.md`** — the loop **driver**.
   Structure it ROLE → FILES (read in order each pass) → PHASE A–F → HARD RULES
   → the frozen taxonomy/rubric block. Author the phases so each pass:
   - **A. Orient** — read the ledger, scorecard, changelog, and `CLAUDE.md`
     (the routing index); never read any `docs/planning/ralph/*.prompt.md` file
     beyond this driver (they may carry a completion tag that must not be echoed).
   - **B. Sweep** — re-scan the spec corpus + code; append any newly discovered
     work item, or any *spec drift* (code that no longer matches its spec ref),
     to `impl-ledger.md` as a new `pending` item.
   - **C. Select** — the single highest-priority item that is `pending` or
     `in-progress` AND whose `depends-on` are all `done`. Backlog items are not
     selected unless promoted. If undone items remain but none are buildable
     (cycle or missing prerequisite), record `OPEN GAP:` and stop the pass —
     never stub the missing prerequisite to unblock a higher-priority item.
   - **D. Advance** — take exactly that one item to its done-bar via TDD: write
     the acceptance test derived from its spec ref (failing first), implement
     until it passes, then run the **full** existing suite. When green, run a
     self-review (the `code-reviewer` agent) against the spec ref, the coding
     standard, and the Claude rules. The item is `done` only when the suite is
     green AND the review is clean. Never rewrite an item already `done`.
   - **E. Record** — update the `impl-ledger.md` status (and the `CLAUDE.md`
     routing index if the item introduces a module/doc a reader must find);
     append one scorecard row + one changelog block; git-commit the touched code
     and tests as the pass checkpoint.
   - **F. Converge?** — if every convergence condition holds, emit
     `<promise>IMPL REVIEW LOOP COMPLETE</promise>` as the sole final line (this
     is the one place the wrapped tag is ever written); otherwise end the pass
     normally.

   Embed the HARD RULES below into the driver verbatim.

2. **`docs/planning/impl-ledger.md`** — a stub table listing every taxonomy item
   with columns `Item | Priority | Spec ref | depends-on | Status | Done-bar`,
   status `pending` for all (status values: `pending` | `in-progress` | `done` |
   `backlog`), plus a "Newly discovered items" append area.

3. **`docs/planning/ralph/impl-review-loop-scorecard.md`** — an append-only stub:
   one header row documenting the columns (pass, date, items done, items backlog,
   open gaps, new items this pass, suite status) and the convergence definition;
   no data rows yet.

4. **`docs/planning/ralph/impl-review-loop-changelog.md`** — an append-only stub:
   a title and a one-line format note; no pass entries yet.

Then print the launch command and a short run/stop note (Step 4), and stop.

## HARD RULES (write these into the driver verbatim)

1. **Right-sized increments + backlog.** Each item is advanced to its done-bar
   OR explicitly deferred to `backlog` with a one-line reason. Never pad the
   codebase to look complete — every line is surface to maintain, so the
   smallest increment that satisfies the spec ref beats speculative scaffolding.
   Done = the acceptance test derived from its spec ref passes, the full suite
   is green, the code passes self-review against the standard + rules, and no
   `OPEN GAP:` markers remain on it.

2. **One item per pass (anti-thrash).** Exactly one work item is created or
   advanced per pass: sweep, select the single highest-priority buildable item,
   advance only it, record, commit. A reviewer that finds a separate problem
   appends it as a NEW ledger item in the next Sweep — it never re-opens a `done`
   item mid-pass.

3. **Convergence — closure.** Emit the completion tag ONLY when ALL hold: every
   ledger item is `done` OR `backlog`; the last TWO consecutive passes each
   discovered ZERO new items or drift; no `OPEN GAP:` markers remain anywhere;
   and the full suite is green. A no-op pass (nothing left to safely advance) is
   valid and is what lets the two-pass stability window trigger.

4. **Build-order discipline (traceable to the docs).** Select only items whose
   `depends-on` prerequisites are all `done`; never stub or fake a missing
   prerequisite to unblock a higher-priority item — if nothing is buildable,
   flag `OPEN GAP:` and stop. Every item names the spec ref it implements; build
   to that spec, not to a plausible-looking approximation. Inherit the host's
   rules: no cross-doc duplication, `CLAUDE.md` stays a routing index, relative
   links resolve, obey the markdown line-length limit, never edit
   `.github/workflows/*` or Accepted ADRs.

5. **VERIFY GATE.** Every implementation pass MUST satisfy this, verbatim:

   > **VERIFY GATE — advance an item only when its acceptance test and the full
   > existing suite pass against the real toolchain. Never weaken, delete, skip,
   > or `xfail` a test, and never use `--no-verify` or any hook-bypass flag, to
   > force a pass or convergence.**

   A loop that green-washes tests manufactures false confidence the way
   authoritative-but-wrong docs manufacture liability; this gate is how it earns
   convergence instead of faking it. Show the real suite output in the pass.

6. **Completion-tag discipline.** The loop is launched with
   `--completion-promise 'IMPL REVIEW LOOP COMPLETE'`; the Ralph Stop hook ends
   the loop the moment the assistant's final message contains that phrase wrapped
   in promise markers — i.e. `<promise>…</promise>` around the phrase. That
   **wrapped** tag must appear in EXACTLY ONE place across this driver and any
   pass's output: the sole final line of a genuinely converged pass (the Phase F
   emission). Write it nowhere else — not in narration, not in the
   ledger/scorecard/changelog, not in this rule. Naming the bare phrase to
   explain the rule (as this sentence does) is fine; only the
   `<promise>…</promise>`-wrapped form triggers the hook.

## Step 4 — Print this for the owner

Print the launch command exactly (one line), then tell the owner the loop is
written but NOT running:

```text
/ralph-loop:ralph-loop "Develop and review the implementation per docs/planning/ralph/impl-review-loop.prompt.md" --max-iterations 40 --completion-promise 'IMPL REVIEW LOOP COMPLETE'
```

End with: "The loop is written and committed-ready, but it is NOT running. Run
the command above to start it; stop it early with `/ralph-loop:cancel-ralph`."
