---
description: Author a behavior-preserving tech-debt-paydown loop for this repo (writes the driver + ledger + records; does not launch it).
argument-hint: "[refactor scope] (optional)"
---

# Author the refactor-review loop for this repo

You are a principal engineer. Running this command **once** authors a turnkey behavior-preserving
tech-debt-paydown loop for THIS repo, then stops. You CREATE the loop's files; you DO NOT launch it —
launching is a separate, explicit `/refactor-review-loop-run` (or `ralph-loop`) invocation.

This is a **code sub-family** loop. **Follow the shared authoring protocol in
[`../loop-authoring-protocol.md`](../loop-authoring-protocol.md)** (3-tier input precedence, repo audit,
frozen taxonomy, the four-file write, phases A/C/E/F, and the universal HARD RULES). This file supplies only
the **deltas** below. The loop improves the structure of existing code one target per pass **without
changing behavior**; the suite stays green throughout.

## Delta 1 — the lens: which debt, where

`<REFACTOR_SCOPE>`

The lens is the **kind of debt to target and where** — complexity / long functions, duplication, tangled
boundaries, unclear naming, dead code — weighted across which areas matter most, and what to defer. On the
menu path, call `AskUserQuestion` with ≤4 repo-tailored options seeded from that space. Synthesize, echo,
and confirm a priority-ordered scope before deriving the taxonomy.

## Delta 2 — taxonomy unit and audit

Audit the existing code for debt targets and, critically, for **which code is covered by tests**. The
taxonomy unit is **one refactor target** (a god-file to split, a duplication to dedupe, a boundary to
clarify, dead code to remove). Order by the lens; give each a done-bar (the target is restructured, all
callers updated, the suite green, no behavior change).

## Delta 3 — phase B (Sweep) and phase D (Advance)

- **B. Sweep** — re-scan for debt targets; append each as a ledger item.
- **D. Advance** — perform **exactly one refactor per pass**, **behavior-preserving**: the full suite must
  be green BEFORE the change and green AFTER it. Never rewrite a target already at its done-bar.

**Safety-net rule (chains to the test loop).** Never refactor code that lacks a passing test covering the
behavior you are about to move. If the target is not covered, do NOT refactor blind — record
`OPEN GAP: needs coverage — run /test-review-loop-init first` and select a different target (or end the
pass). **Scope rule.** A change that requires altering behavior is out of scope — record it as a finding
for the `impl-review-loop` rather than doing it here.

## Delta 4 — gate (code sub-family): the VERIFY GATE

Embed this rule in the driver verbatim, as a HARD RULE after the universal four:

> **VERIFY GATE — advance a refactor only when the full existing suite passes against the real toolchain
> both before and after the change (proving behavior is preserved). Never weaken, delete, skip, or `xfail`
> a test, and never use `--no-verify` or any hook-bypass flag, to force a pass or convergence.** Show the
> real suite output in the pass.

## Delta 5 — convergence and the completion phrase

Convergence (per the protocol's universal rule 3) additionally requires that the **full suite is green**.
The driver's Phase-F emission uses the completion phrase `REFACTOR REVIEW LOOP COMPLETE`.

## Then print the launch line and stop

Print this for the owner (one line), then tell them the loop is written but NOT running:

```text
/ralph-loop:ralph-loop "Pay down tech debt per docs/planning/ralph/refactor-review-loop.prompt.md" --max-iterations 40 --completion-promise 'REFACTOR REVIEW LOOP COMPLETE'
```

End with: "The loop is written and committed-ready, but it is NOT running. Run `/refactor-review-loop-run`
(or the command above) to start it; stop it early with `/ralph-loop:cancel-ralph`."
