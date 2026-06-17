---
description: Author a gap-driven documentation-review loop for this repo (writes the driver + ledger + records; does not launch it).
argument-hint: "(no args)"
---

# Author the documentation-review loop for this repo

You are a principal operator and technical editor. Running this command **once**
authors a turnkey documentation-review loop for THIS repo, then stops. You
CREATE the loop's files; you DO NOT launch it — launching is a separate,
explicit `ralph-loop` invocation.

The loop you author is a Ralph loop: a **frozen rubric**, **one-doc-per-pass**
review that fills this repo's business/project documentation to a right-sized
core and queues the rest as a prioritized backlog.

## Step 1 — Gather inputs

**Owner goals.** Read `<OWNER_GOALS>` below. If it still holds the literal
placeholder token, STOP and ask the owner for their goals before writing
anything — the goals are the lens the loop weights every document by, and a
wrong lens mis-prioritizes the entire corpus.

<OWNER_GOALS>

**Repo audit.** Audit THIS repo yourself (you have read access). Establish, by
looking — not assuming:

- repo state: rough doc count, whether there is production code, the project phase;
- the existing doc corpus: which topics are already well covered;
- the gaps: which documents the owner's goals imply but the repo lacks.

## Step 2 — Derive the taxonomy

From the goals + audit, derive a **goal-weighted, priority-ordered document
taxonomy**: the documents the owner needs, highest-value first, ordered strictly
by the owner's weighting (for a typical product, that might run operations →
revenue → growth → liability → tech execution → scale — but derive it from THESE
goals). Give each item a one-line **core-complete bar** — what the doc must
answer to count as done. Mark post-launch / scale-out items as **backlog by
default**; they must not block the near term. Once written into the driver this
taxonomy is **frozen**: the loop may append newly discovered gaps but must never
reweight or delete it.

## Step 3 — Write the loop, then STOP

Create these files; do NOT launch the loop:

1. **`docs/planning/ralph/doc-review-loop.prompt.md`** — the loop **driver**.
   Structure it ROLE → FILES (read in order each pass) → PHASE A–F → HARD RULES
   → the frozen taxonomy/rubric block. Author the phases so each pass:
   - **A. Orient** — read the ledger, scorecard, changelog, and `CLAUDE.md`
     (the routing index); never read any `docs/planning/ralph/*.prompt.md` file
     beyond this driver (they may carry a completion tag that must not be echoed).
   - **B. Sweep** — re-scan the corpus + goals; append any newly found gaps to
     `coverage-ledger.md`.
   - **C. Select** — the single highest-priority item that is `missing` or weak
     (backlog items are not selected unless promoted).
   - **D. Advance** — create or advance exactly that one doc to its
     core-complete bar. Never rewrite a doc already core-complete (touching a
     healthy doc is a regression).
   - **E. Record** — update the `CLAUDE.md` routing index (one line for the new
     doc) and the ledger status; append one scorecard row + one changelog
     block; git-commit the touched files as the pass checkpoint.
   - **F. Converge?** — if every convergence condition holds, emit
     `<promise>DOC REVIEW LOOP COMPLETE</promise>` as the sole final line (this is
     the one place the wrapped tag is ever written); otherwise end the pass
     normally.

   Embed the HARD RULES below into the driver verbatim.

2. **`docs/planning/coverage-ledger.md`** — a stub table listing every taxonomy
   item with status `missing` (status values: `missing` | `drafting` |
   `core-complete` | `backlog`), plus a "Newly discovered gaps" append area.

3. **`docs/planning/ralph/doc-review-loop-scorecard.md`** — an append-only stub:
   one header row documenting the columns (pass, date, docs core-complete, docs
   backlog, open gaps, new gaps this pass) and the convergence definition; no
   data rows yet.

4. **`docs/planning/ralph/doc-review-loop-changelog.md`** — an append-only stub:
   a title and a one-line format note; no pass entries yet.

Then print the launch command and a short run/stop note (Step 4), and stop.

## HARD RULES (write these into the driver verbatim)

1. **Right-sized core + backlog.** Each item is advanced to its core-complete
   bar OR explicitly deferred to `backlog` with a one-line reason. Never pad
   prose to look complete — every doc is drift surface to maintain, so fewer
   load-bearing docs beat exhaustive coverage. Core-complete = answers its key
   questions with concrete numbers/mechanics/owners, has no `OPEN GAP:` markers,
   and a downstream reader could act on it without further research.

2. **One doc per pass (anti-thrash).** Exactly one document is created or
   advanced per pass: sweep, select the single highest-priority item, advance
   only it, record, commit.

3. **Convergence — gap-closure.** Emit the completion tag ONLY when ALL hold:
   every ledger item is `core-complete` OR `backlog`; the last TWO consecutive
   passes each discovered ZERO new gaps; and no `OPEN GAP:` markers remain
   anywhere. A no-op pass (nothing left to safely advance) is valid and is what
   lets the two-pass stability window trigger.

4. **Doc discipline (inherit the host's rules).** No cross-doc duplication — one
   authoritative doc per topic, others link to it. `CLAUDE.md` stays a routing
   index: adding a doc means adding a one-line routing entry, never pasting
   content; if the repo has no `CLAUDE.md`, create a minimal routing-index one
   (a title + an `## Authoritative References` list) before the first entry.
   Relative links must resolve. Obey the host's markdown line-length
   limit and planned-section markers. Never edit `.github/workflows/*` or
   Accepted ADRs. New business docs go under `docs/business/` (create it if
   absent); planning docs under `docs/planning/`.

5. **Liability self-guard.** Every legal / financial / compliance / tax /
   insurance / ToS / DPA document the loop emits MUST begin, right after its H1,
   with this blockquote, verbatim:

   > **DRAFT — NOT legal or financial advice. Generated by an automated loop; requires
   > professional and human review before any reliance.**

   Such documents must also state their key assumptions explicitly so a human
   reviewer can check them. A loop that emits authoritative-sounding-but-wrong
   guidance manufactures liability; this gate is how it reduces liability.

6. **Completion-tag discipline.** The loop is launched with
   `--completion-promise 'DOC REVIEW LOOP COMPLETE'`; the Ralph Stop hook ends
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
/ralph-loop:ralph-loop "Review the repo fresh and fill the doc corpus per docs/planning/ralph/doc-review-loop.prompt.md" --max-iterations 40 --completion-promise 'DOC REVIEW LOOP COMPLETE'
```

End with: "The loop is written and committed-ready, but it is NOT running. Run
the command above to start it; stop it early with `/ralph-loop:cancel-ralph`."
