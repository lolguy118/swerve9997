---
description: Launch the migration-review loop that /migration-review-loop-init authored (a thin wrapper over the ralph-loop plugin; does not author it).
argument-hint: "[max-iterations] (default 40)"
---

# Launch the migration-review loop for this repo

This command **launches** the codebase-migration loop that `/migration-review-loop-init` already authored.
It does NOT author the loop — if the loop has not been authored yet, run `/migration-review-loop-init`
first.

## Step 1 — Precondition: the loop must already be authored

Check whether `docs/planning/ralph/migration-review-loop.prompt.md` exists in this repo. If it does NOT
exist, STOP: tell the owner the migration-review loop has not been authored yet and that they should run
`/migration-review-loop-init` first. Do not launch anything.

## Step 2 — Launch the Ralph loop

If the driver exists, launch the loop by invoking the `ralph-loop:ralph-loop` skill. Use the owner's
argument as the iteration cap when they supplied a number, otherwise default to 40:

- prompt: `Run the migration per docs/planning/ralph/migration-review-loop.prompt.md`
- `--max-iterations`: `$ARGUMENTS` when the owner passed a number, else `40`
- `--completion-promise`: `MIGRATION REVIEW LOOP COMPLETE`

That is, invoke the skill equivalent to this launch line (substitute the owner's iteration count for 40 if
they passed one):

```text
/ralph-loop:ralph-loop "Run the migration per docs/planning/ralph/migration-review-loop.prompt.md" --max-iterations 40 --completion-promise 'MIGRATION REVIEW LOOP COMPLETE'
```

The loop then advances **one site per pass** — migrating it from the old pattern to the new one atomically,
running the full suite, and updating the ledger, scorecard, and changelog — and self-terminates when every
site is migrated or backlog, two consecutive passes find no new sites, the full suite is green, and a
repo-wide sweep finds zero residual old-pattern usages. Stop it early at any time with
`/ralph-loop:cancel-ralph`.
