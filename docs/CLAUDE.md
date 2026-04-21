# docs/CLAUDE.md

> **AI maintenance rule:** This file is a doc-side routing index for
> AI/LLM context only. It must NOT contain design information — only
> references to authoritative documents.
>
> Do NOT duplicate content from:
>
> - [`README.md`](README.md) — doc tier layout (common / team-lib / project)
> - [`common/coding-standard/`](common/coding-standard/) — coding rules
> - [`common/planning/`](common/planning/) — shared planning framework
> - [`team-lib/planning/`](team-lib/planning/) — library SDP, SRS, SVP, SCMP, SDDs, ADRs
> - [`team-lib/guides/`](team-lib/guides/) — contributor tutorials
> - [`team-lib/coding-standard/`](team-lib/coding-standard/) — library-specific templates and bindings
> - [`../.claude/rules/`](../.claude/rules/) — path-scoped AI guardrails
>
> If new doc-side context is needed, add it to the appropriate
> authoritative document above, then reference it here.

## Scope

This CLAUDE.md activates when sessions are rooted inside `docs/`.
It is the doc-side counterpart to the repo-root
[`../CLAUDE.md`](../CLAUDE.md), which handles code-side routing.
Both files exist so that rules under
[`../.claude/rules/`](../.claude/rules/) load regardless of whether
the session's entry point is code or docs.

## Authoritative References (doc-side)

- **Layout:** [`README.md`](README.md) — three-tier structure
  (`common/`, `team-lib/`, `<project>/`) and how robot projects
  consume this folder.
- **Planning (common):** [`common/planning/README.md`](common/planning/README.md)
  — SemVer, phase model, verification framework.
- **Planning (library):** [`team-lib/planning/README.md`](team-lib/planning/README.md)
  — SDP, SRS, SVP, SCMP, plus
  [ADRs](team-lib/planning/adr/README.md) and
  [SDDs](team-lib/planning/sdd/README.md).
- **Coding standard:** [`common/coding-standard/README.md`](common/coding-standard/README.md)
  — core + eleven companions. Library bindings in
  [`team-lib/coding-standard/`](team-lib/coding-standard/).
- **Contributor onboarding:** [`team-lib/guides/start-here.md`](team-lib/guides/start-here.md).
- **External URLs:** [`reference-urls.md`](reference-urls.md) —
  vendor docs (WPILib, CTRE, PathPlanner, etc.).
- **Review prompts:** [`team-lib/prompts/`](team-lib/prompts/) —
  full prompt loaded by the [`lib-reviewer`](../.claude/agents/lib-reviewer.md)
  subagent.

## Doc-Side AI Guardrails (auto-loaded by scope)

Rules under [`../.claude/rules/`](../.claude/rules/) declare their
own `paths:` frontmatter and auto-activate when Claude reads
matching files. The rules scoped to doc work:

- [`../.claude/rules/docs.md`](../.claude/rules/docs.md) —
  no numeric tunables in docs, planned-feature markers, telemetry
  parity, cross-reference style, 140-char line limit.
- [`../.claude/rules/planning.md`](../.claude/rules/planning.md) —
  ADR / SDD format, requirement IDs, section templates.
- [`../.claude/rules/safety.md`](../.claude/rules/safety.md) —
  loads when docs describe subsystem behavior or safety invariants.

## Pre-Merge Doc Enforcement

> **Doc-side subset.** The full hook roster — including the Java and
> opt-in hooks that don't fire on doc edits — is authoritative in
> [SVP §6](team-lib/planning/SVP.md#6-hooks-as-pre-merge-gates-library-roster).
> The entries below are the hooks that trigger on `docs/**` edits,
> listed here for doc-session onboarding.

Doc edits trigger these hooks (see also
[`../.claude/settings.json`](../.claude/settings.json)):

- [`lint-markdown.sh`](../.claude/hooks/lint-markdown.sh) —
  markdownlint.
- [`check-doc-tunables.sh`](../.claude/hooks/check-doc-tunables.sh)
  — flag numeric constants in design docs.
- [`check-deleted-class-refs.sh`](../.claude/hooks/check-deleted-class-refs.sh)
  — warn on references to deprecated symbols listed in
  [`../.claude/rules/deprecated-symbols.txt`](../.claude/rules/deprecated-symbols.txt).
- [`check-design-drift.sh`](../.claude/hooks/check-design-drift.sh)
  — nudge doc updates when behavior-defining code changes.
- [`verify-docs-hook.sh`](../.claude/hooks/verify-docs-hook.sh) —
  advisory wrapper around the authoritative
  [`verify-docs.sh`](../.claude/hooks/verify-docs.sh) sweep (broken
  links, stale paths, TBDs, empty sections).
