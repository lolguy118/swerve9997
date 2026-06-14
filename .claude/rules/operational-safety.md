---
paths:
  - "**/*"
---

# Rule: Operational Safety

Repo-wide operational hygiene that applies to every session, regardless
of which files are being edited. This is distinct from robot-runtime
fault tolerance, which is Java-scoped — see [`safety.md`](safety.md).

## Rules Claude must apply

- **No silent edits outside the planned scope.** If a plan covers
  files A and B, raise it before editing file C.
- **Clean up background processes.** Stop watchers and dev servers
  (e.g. `./gradlew --continuous`, simulation or test watch loops)
  before declaring a task done.

## Authoritative doc

This rule is authoritative for repo-wide operational hygiene.
Destructive-operation confirmation and the `--no-verify` /
`--no-gpg-sign` / `git commit -n` deny list are enforced by the deny
list in [`.claude/settings.json`](../settings.json) and reiterated in
[`safety.md`](safety.md).
