---
description: Author a codebase-migration loop for this repo — old pattern to new, one site per pass (writes the driver + ledger + records; does not launch it).
argument-hint: "[migration target: from X to Y]"
---

# Author the migration-review loop for this repo

You are a principal engineer running a large mechanical migration. Running this command **once** authors a
turnkey migration loop for THIS repo, then stops. You CREATE the loop's files; you DO NOT launch it —
launching is a separate, explicit `/migration-review-loop-run` (or `ralph-loop`) invocation.

This is a **code sub-family** loop. **Follow the shared authoring protocol in
[`../loop-authoring-protocol.md`](../loop-authoring-protocol.md)** (3-tier input precedence, repo audit,
frozen taxonomy, the four-file write, phases A/C/E/F, and the universal HARD RULES). This file supplies only
the **deltas** below. The loop moves the codebase from an old pattern to a new one, one site at a time,
keeping the suite green and converging only when no trace of the old pattern remains.

## Delta 1 — the lens: the migration target (from X to Y)

`<MIGRATION_TARGET>`

The lens is the **specific migration**: the old pattern (X) and the new pattern (Y) — a dependency bump, a
framework or language-version move, an API replacement, or rolling a new standard across the codebase.
Because the target is usually specific, the **inline `$ARGUMENTS` / pre-filled** path is primary; on the
menu path, call `AskUserQuestion` offering common migration types (dependency upgrade, framework swap,
language-version bump, API replacement, standard rollout) plus the "Other" free-text the harness adds.
Synthesize, echo, and confirm the exact from-X-to-Y target before deriving the taxonomy.

## Delta 2 — taxonomy unit and audit

Audit the codebase for every **site** that uses the old pattern (X). The taxonomy unit is **one migration
site (or a cohesive batch)**. Order with shared infrastructure / shims first via `depends-on` (a site that
needs a new helper lists it as a prerequisite). Give each a done-bar (the site is fully on Y, the suite is
green, and no X remains in it).

## Delta 3 — phase B (Sweep) and phase D (Advance)

- **B. Sweep** — re-scan for old-pattern (X) sites across the repo; append each as a ledger item.
- **D. Advance** — migrate **exactly one site per pass** to the new pattern, **atomically**: never leave a
  site half-migrated in a state that breaks the build between passes (introduce a compatibility shim first
  as its own item if needed). Run the full suite. Never rewrite a site already migrated.

## Delta 4 — gate (code sub-family): the VERIFY GATE

Embed this rule in the driver verbatim, as a HARD RULE after the universal four:

> **VERIFY GATE — advance a site only when the full existing suite passes against the real toolchain after
> the migration. Never weaken, delete, skip, or `xfail` a test, and never use `--no-verify` or any
> hook-bypass flag, to force a pass or convergence.** Show the real suite output in the pass.

## Delta 5 — convergence: a clean zero-residual sweep

Convergence (per the protocol's universal rule 3) additionally requires that the **full suite is green**
AND a **repo-wide sweep finds zero residual old-pattern (X) usages** — an empty ledger is not enough; the
loop must prove X is gone (e.g. a grep for the old pattern returns nothing outside intentionally-excluded
paths). The driver's Phase-F emission uses the completion phrase `MIGRATION REVIEW LOOP COMPLETE`.

## Then print the launch line and stop

Print this for the owner (one line), then tell them the loop is written but NOT running:

```text
/ralph-loop:ralph-loop "Run the migration per docs/planning/ralph/migration-review-loop.prompt.md" --max-iterations 40 --completion-promise 'MIGRATION REVIEW LOOP COMPLETE'
```

End with: "The loop is written and committed-ready, but it is NOT running. Run `/migration-review-loop-run`
(or the command above) to start it; stop it early with `/ralph-loop:cancel-ralph`."
