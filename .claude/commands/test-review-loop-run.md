---
description: Launch the test-review loop that /test-review-loop-init authored (a thin wrapper over the ralph-loop plugin; does not author it).
argument-hint: "[max-iterations] (default 40)"
---

# Launch the test-review loop for this repo

This command **launches** the coverage-retrofit-and-hardening loop that `/test-review-loop-init` already
authored. It does NOT author the loop — if the loop has not been authored yet, run `/test-review-loop-init`
first.

## Step 1 — Precondition: the loop must already be authored

Check whether `docs/planning/ralph/test-review-loop.prompt.md` exists in this repo. If it does NOT exist,
STOP: tell the owner the test-review loop has not been authored yet and that they should run
`/test-review-loop-init` first. Do not launch anything.

## Step 2 — Launch the Ralph loop

If the driver exists, launch the loop by invoking the `ralph-loop:ralph-loop` skill. Use the owner's
argument as the iteration cap when they supplied a number, otherwise default to 40:

- prompt: `Retrofit and harden tests per docs/planning/ralph/test-review-loop.prompt.md`
- `--max-iterations`: `$ARGUMENTS` when the owner passed a number, else `40`
- `--completion-promise`: `TEST REVIEW LOOP COMPLETE`

That is, invoke the skill equivalent to this launch line (substitute the owner's iteration count for 40 if
they passed one):

```text
/ralph-loop:ralph-loop "Retrofit and harden tests per docs/planning/ralph/test-review-loop.prompt.md" --max-iterations 40 --completion-promise 'TEST REVIEW LOOP COMPLETE'
```

The loop then advances **one unit per pass** — writing characterization tests for its public surface and
edge/failure cases, running the full suite, and updating the ledger, scorecard, and changelog — and
self-terminates when every unit is covered or backlog, two consecutive passes find no new units, and the
full suite is green. Suspected bugs are recorded as `OPEN GAP:` for the impl loop, not fixed here. Stop it
early at any time with `/ralph-loop:cancel-ralph`.
