---
description: Launch the design-review loop that /design-review-loop-init authored (a thin wrapper over the ralph-loop plugin; does not author it).
argument-hint: "[max-iterations] (default 40)"
---

# Launch the design-review loop for this repo

This command **launches** the definition-and-design loop that `/design-review-loop-init` already authored.
It does NOT author the loop — if the loop has not been authored yet, run `/design-review-loop-init` first.

## Step 1 — Precondition: the loop must already be authored

Check whether `docs/planning/ralph/design-review-loop.prompt.md` exists in this repo. If it does NOT exist,
STOP: tell the owner the design-review loop has not been authored yet and that they should run
`/design-review-loop-init` first. Do not launch anything.

## Step 2 — Launch the Ralph loop

If the driver exists, launch the loop by invoking the `ralph-loop:ralph-loop` skill. Use the owner's
argument as the iteration cap when they supplied a number, otherwise default to 40:

- prompt: `Define and design the product per docs/planning/ralph/design-review-loop.prompt.md`
- `--max-iterations`: `$ARGUMENTS` when the owner passed a number, else `40`
- `--completion-promise`: `DESIGN REVIEW LOOP COMPLETE`

That is, invoke the skill equivalent to this launch line (substitute the owner's iteration count for 40 if
they passed one):

```text
/ralph-loop:ralph-loop "Define and design the product per docs/planning/ralph/design-review-loop.prompt.md" --max-iterations 40 --completion-promise 'DESIGN REVIEW LOOP COMPLETE'
```

The loop then advances **one design artifact per pass** — interviewing where needed, drafting the brief,
ADRs, and design docs to their done-bars, parking genuine owner judgment calls, and updating the ledger,
scorecard, and changelog — and self-terminates when every taxonomy item is done or backlog, two consecutive
passes find no new decisions, and no owner question blocks an unbuilt foundational artifact. Stop it early
at any time with `/ralph-loop:cancel-ralph`.
