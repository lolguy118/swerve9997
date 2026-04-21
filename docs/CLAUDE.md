<!-- markdownlint-disable MD013 -->
# CLAUDE.md — `docs/`

> **Auto-loaded by Claude Code** when a session's working directory
> is under `docs/`. Adds documentation-scoped guardrails on top of
> the root `CLAUDE.md`.

See [`README.md`](README.md) for the three-tier docs layout
(common / team-lib / robot-project) and
[`team-lib/planning/README.md`](team-lib/planning/README.md) for
the library's planning-document map.

## Documentation-scoped rules

@.claude/rules/docs.md
@.claude/rules/planning.md

## Where the other rules live

Code-side rules stay with the root `CLAUDE.md` so they're active
for every session:

- [`.claude/rules/coding-standard.md`](../.claude/rules/coding-standard.md)
  — Java coding standard quick-recall.
- [`.claude/rules/team271-lib.md`](../.claude/rules/team271-lib.md)
  — library-specific layering, passthrough, CAN-refresh rules.
- [`.claude/rules/safety.md`](../.claude/rules/safety.md)
  — timeouts, fail-safe, driver-alert rules.

If you're editing a doc that *cites* one of those rules, the
authoritative text still lives at its root location — no need to
re-read it from here.
