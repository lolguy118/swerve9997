---
description: Launch the implementation-review loop that /impl-review-loop-init authored (a thin wrapper over the ralph-loop plugin; does not author it).
argument-hint: "[max-iterations] (default 40)"
---

# Launch the implementation-review loop for this repo

This command **launches** the spec-driven implementation-and-review loop that
`/impl-review-loop-init` already authored. It does NOT author the loop — if the
loop has not been authored yet, run `/impl-review-loop-init` first.

## Step 1 — Precondition: the loop must already be authored

Check whether `docs/planning/ralph/impl-review-loop.prompt.md` exists in this
repo. If it does NOT exist, STOP: tell the owner the implementation-review loop
has not been authored yet and that they should run `/impl-review-loop-init`
first. Do not launch anything.

## Step 2 — Launch the Ralph loop

If the driver exists, launch the loop by invoking the `ralph-loop:ralph-loop`
skill. Use the owner's argument as the iteration cap when they supplied a
number, otherwise default to 40:

- prompt: `Develop and review the implementation per docs/planning/ralph/impl-review-loop.prompt.md`
- `--max-iterations`: `$ARGUMENTS` when the owner passed a number, else `40`
- `--completion-promise`: `IMPL REVIEW LOOP COMPLETE`

That is, invoke the skill equivalent to this launch line (substitute the owner's
iteration count for 40 if they passed one):

```text
/ralph-loop:ralph-loop "Develop and review the implementation per docs/planning/ralph/impl-review-loop.prompt.md" --max-iterations 40 --completion-promise 'IMPL REVIEW LOOP COMPLETE'
```

The loop then advances **one work item per pass** — building it to its done-bar
via TDD, reviewing the increment, and updating the ledger, scorecard, and
changelog — and self-terminates when every taxonomy item is done or backlog, two
consecutive passes find no new items or drift, and the full suite is green. Stop
it early at any time with `/ralph-loop:cancel-ralph`.
