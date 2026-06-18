---
description: Author a coverage-retrofit-and-hardening loop for this repo (writes the driver + ledger + records; does not launch it).
argument-hint: "[test scope] (optional)"
---

# Author the test-review loop for this repo

You are a principal test engineer. Running this command **once** authors a turnkey
coverage-retrofit-and-hardening loop for THIS repo, then stops. You CREATE the loop's files; you DO NOT
launch it — launching is a separate, explicit `/test-review-loop-run` (or `ralph-loop`) invocation.

This is a **code sub-family** loop. **Follow the shared authoring protocol in
[`../loop-authoring-protocol.md`](../loop-authoring-protocol.md)** (3-tier input precedence, repo audit,
frozen taxonomy, the four-file write, phases A/C/E/F, and the universal HARD RULES). This file supplies only
the **deltas** below. The loop raises test coverage on an EXISTING codebase one unit at a time; it does not
add features and does not change behavior.

## Delta 1 — the lens: risk-weighted test scope

`<TEST_SCOPE>`

The lens is the **risk weighting** the loop ranks units by — which modules/subsystems matter most, what
coverage bar counts as enough, and what to defer. On the menu path, call `AskUserQuestion` with ≤4
repo-tailored options seeded from the highest-risk areas (core domain logic, error/edge paths, recently
changed code, security-sensitive surfaces, public API). Synthesize, echo, and confirm a priority-ordered
scope before deriving the taxonomy.

## Delta 2 — taxonomy unit and audit

Audit the existing code + test suite: what is implemented, and what the suite already covers. The taxonomy
unit is **one untested or weakly-tested unit** (a module, class, or behavior). Order by the lens's risk
weighting; give each a done-bar (its public surface + the identified edge/failure cases are covered).

## Delta 3 — phase B (Sweep) and phase D (Advance)

- **B. Sweep** — re-scan the code + suite; append each untested/weak unit, and the specific edge and
  failure cases it needs, as ledger items.
- **D. Advance** — bring **exactly one unit per pass** to its done-bar by writing **characterization
  tests** that assert the code's **ACTUAL current behavior**, then run the full existing suite. Meaningful
  assertions only — never coverage theater (a test with no real assertion). Never rewrite a unit already at
  its done-bar.

**Boundary (this loop does not change behavior).** If a characterization test reveals behavior that looks
like a **bug**, do NOT fix it here — record it as `OPEN GAP: <describe>` for the owner / the
`impl-review-loop` to address, and assert the current behavior (or mark that one case `OPEN GAP:` rather
than baking a wrong expectation in). Changing code to "make a test pass" is out of scope.

## Delta 4 — gate (code sub-family): the VERIFY GATE

Embed this rule in the driver verbatim, as a HARD RULE after the universal four:

> **VERIFY GATE — advance a unit only when its new tests and the full existing suite pass against the real
> toolchain. Never weaken, delete, skip, or `xfail` a test, and never use `--no-verify` or any hook-bypass
> flag, to force a pass or convergence.** Show the real suite output in the pass.

## Delta 5 — convergence and the completion phrase

Convergence (per the protocol's universal rule 3) additionally requires that the **full suite is green**.
The driver's Phase-F emission uses the completion phrase `TEST REVIEW LOOP COMPLETE`.

## Then print the launch line and stop

Print this for the owner (one line), then tell them the loop is written but NOT running:

```text
/ralph-loop:ralph-loop "Retrofit and harden tests per docs/planning/ralph/test-review-loop.prompt.md" --max-iterations 40 --completion-promise 'TEST REVIEW LOOP COMPLETE'
```

End with: "The loop is written and committed-ready, but it is NOT running. Run `/test-review-loop-run`
(or the command above) to start it; stop it early with `/ralph-loop:cancel-ralph`."
