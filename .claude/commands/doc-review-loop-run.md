---
description: Launch the documentation-review loop that /doc-review-loop-init authored (a thin wrapper over the ralph-loop plugin; does not author it).
argument-hint: "[max-iterations] (default 40)"
---

# Launch the documentation-review loop for this repo

This command **launches** the gap-driven documentation-review loop that
`/doc-review-loop-init` already authored. It does NOT author the loop — if the
loop has not been authored yet, run `/doc-review-loop-init` first.

## Step 1 — Precondition: the loop must already be authored

Check whether `docs/planning/ralph/doc-review-loop.prompt.md` exists in this
repo. If it does NOT exist, STOP: tell the owner the documentation-review loop
has not been authored yet and that they should run `/doc-review-loop-init`
first. Do not launch anything.

## Step 2 — Launch the Ralph loop

If the driver exists, launch the loop by invoking the `ralph-loop:ralph-loop`
skill. Use the owner's argument as the iteration cap when they supplied a
number, otherwise default to 40:

- prompt: `Review the repo fresh and fill the doc corpus per docs/planning/ralph/doc-review-loop.prompt.md`
- `--max-iterations`: `$ARGUMENTS` when the owner passed a number, else `40`
- `--completion-promise`: `DOC REVIEW LOOP COMPLETE`

That is, invoke the skill equivalent to this launch line (substitute the owner's
iteration count for 40 if they passed one):

```text
/ralph-loop:ralph-loop "Review the repo fresh and fill the doc corpus per docs/planning/ralph/doc-review-loop.prompt.md" --max-iterations 40 --completion-promise 'DOC REVIEW LOOP COMPLETE'
```

The loop then advances **one document per pass** — updating the coverage ledger,
scorecard, and changelog — and self-terminates when every taxonomy item is
core-complete or backlog and two consecutive passes find no new gaps. Stop it
early at any time with `/ralph-loop:cancel-ralph`.
