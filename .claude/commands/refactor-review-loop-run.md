---
description: Launch the refactor-review loop that /refactor-review-loop-init authored (a thin wrapper over the ralph-loop plugin; does not author it).
argument-hint: "[max-iterations] (default 40)"
---

# Launch the refactor-review loop for this repo

This command **launches** the behavior-preserving tech-debt-paydown loop that `/refactor-review-loop-init`
already authored. It does NOT author the loop — if the loop has not been authored yet, run
`/refactor-review-loop-init` first.

## Step 1 — Precondition: the loop must already be authored

Check whether `docs/planning/ralph/refactor-review-loop.prompt.md` exists in this repo. If it does NOT
exist, STOP: tell the owner the refactor-review loop has not been authored yet and that they should run
`/refactor-review-loop-init` first. Do not launch anything.

## Step 2 — Launch the Ralph loop

If the driver exists, launch the loop by invoking the `ralph-loop:ralph-loop` skill. Use the owner's
argument as the iteration cap when they supplied a number, otherwise default to 40:

- prompt: `Pay down tech debt per docs/planning/ralph/refactor-review-loop.prompt.md`
- `--max-iterations`: `$ARGUMENTS` when the owner passed a number, else `40`
- `--completion-promise`: `REFACTOR REVIEW LOOP COMPLETE`

That is, invoke the skill equivalent to this launch line (substitute the owner's iteration count for 40 if
they passed one):

```text
/ralph-loop:ralph-loop "Pay down tech debt per docs/planning/ralph/refactor-review-loop.prompt.md" --max-iterations 40 --completion-promise 'REFACTOR REVIEW LOOP COMPLETE'
```

The loop then advances **one refactor target per pass** — behavior-preserving, with the full suite green
before and after — and updates the ledger, scorecard, and changelog. Uncovered targets are deferred with
`OPEN GAP: needs coverage` (run the test-review loop first). It self-terminates when every target is
resolved or backlog, two consecutive passes find no new targets, and the full suite is green. Stop it early
at any time with `/ralph-loop:cancel-ralph`.
