---
description: Author a spec-driven implementation-and-review loop for this repo (writes the driver + ledger + records; does not launch it).
argument-hint: "[impl scope] (optional)"
---

# Author the implementation-review loop for this repo

You are a principal engineer and reviewer. Running this command **once** authors a turnkey
implementation-and-review loop for THIS repo, then stops. You CREATE the loop's files; you DO NOT launch
it — launching is a separate, explicit `/impl-review-loop-run` (or `ralph-loop`) invocation.

This is a **code sub-family** loop. **Follow the shared authoring protocol in
[`../loop-authoring-protocol.md`](../loop-authoring-protocol.md)** (3-tier input precedence, repo audit,
frozen taxonomy, the four-file write, phases A/C/E/F, and the universal HARD RULES). This file supplies only
the **deltas** below. The repo's docs (ADRs / specs / design) are the spec of record — the loop builds WHAT
they describe, in dependency order, reviewing each increment as it lands.

## Delta 1 — the lens: implementation scope

`<IMPL_SCOPE>`

The lens is the **scope** — the weighting AND boundary (build now vs. defer to backlog) the loop ranks every
work item by. On the menu path, do a quick recon of the spec corpus, then call `AskUserQuestion` with ≤4
candidate work-areas the specs imply (capability / subsystem names adapted to THIS repo; or up to ~3 grouped
`multiSelect` questions if the specs span more subsystems than fit four options). Synthesize, echo, and
confirm a scope statement that names what to BUILD NOW (priority-ordered) and what to DEFER before deriving
the taxonomy.

## Delta 2 — taxonomy unit and audit

Audit this repo: which behaviors the docs (ADRs / specs / design under `docs/`) specify and where; what is
already implemented and tested; and the gaps and drift (specified behaviors the code lacks, code that no
longer matches its spec). The taxonomy unit is **one work item**; give each:

- a **priority** (its weight under the scope);
- a **spec ref** — the doc / section / ADR it implements, so the build is traceable and a reviewer can check
  code against intent;
- a **depends-on** — the items that must be `done` first (derive from the docs);
- a one-line **done-bar** — the acceptance criterion.

Mark scale-out / post-MVP items as backlog by default.

## Delta 3 — phase B (Sweep) and phase D (Advance)

- **B. Sweep** — re-scan the spec corpus + code; append any newly discovered work item, or any **spec
  drift** (code that no longer matches its spec ref), as a new `pending` ledger item.
- **C. Select (build-order refinement)** — choose the single highest-priority item that is `pending` AND
  whose `depends-on` are all `done`. If undone items remain but none are buildable (cycle or missing
  prerequisite), record `OPEN GAP:` and stop the pass — never stub the missing prerequisite to unblock a
  higher-priority item.
- **D. Advance** — take **exactly one item per pass** to its done-bar via TDD: write the acceptance test
  from its spec ref (failing first), implement until it passes, run the **full** existing suite; when green,
  run a self-review (the `code-reviewer` agent) against the spec ref, the coding standard, and the Claude
  rules. The item is `done` only when the suite is green AND the review is clean. Never rewrite a `done`
  item.

## Delta 4 — gate (code sub-family): the VERIFY GATE

Embed this rule in the driver verbatim, as a HARD RULE after the universal four:

> **VERIFY GATE — advance an item only when its acceptance test and the full existing suite pass against the
> real toolchain. Never weaken, delete, skip, or `xfail` a test, and never use `--no-verify` or any
> hook-bypass flag, to force a pass or convergence.** Show the real suite output in the pass.

Build to the spec ref, not to a plausible-looking approximation.

## Delta 5 — convergence and the completion phrase

Convergence (per the protocol's universal rule 3) additionally requires that the **full suite is green**.
The driver's Phase-F emission uses the completion phrase `IMPL REVIEW LOOP COMPLETE`.

## Then print the launch line and stop

Print this for the owner (one line), then tell them the loop is written but NOT running:

```text
/ralph-loop:ralph-loop "Develop and review the implementation per docs/planning/ralph/impl-review-loop.prompt.md" --max-iterations 40 --completion-promise 'IMPL REVIEW LOOP COMPLETE'
```

End with: "The loop is written and committed-ready, but it is NOT running. Run `/impl-review-loop-run`
(or the command above) to start it; stop it early with `/ralph-loop:cancel-ralph`."
