---
paths:
  - "docs/**"
---

# Rule: Documentation

Team271-Lib's design docs live under `docs/team-lib/` and are part of
the contract. Code changes that alter behavior require doc updates in
the same PR. Robot-project-specific docs live under `docs/<robot>/`
(separate repo); this rule applies only to library docs.

## Rules Claude must apply

- **No numeric tunables in docs.** PID gains, voltages, amps, RPS/RPM,
  duty cycles, timing windows, and thresholds belong in code
  (`Constants.java`, subsystem classes), not docs. In docs, reference
  the constant name (`kArmStatorLimit`) — never the value.
  - Allowed: datasheet values, physical dimensions, gear ratios, CAN
    IDs, rationale for *why* a value was chosen.
  - Enforced automatically by
    [`.claude/hooks/check-doc-tunables.sh`](../hooks/check-doc-tunables.sh).
- **Planned features must be clearly marked.** Any section describing
  unimplemented functionality must start with
  `> **Status: Planned — Not Yet Implemented.**`.
- **Telemetry keys must match `outputTelemetry()`.** When you add or
  remove a key in code, update the telemetry table in the
  corresponding design doc in the same change.
- **No cross-doc duplication.** One doc is authoritative per topic;
  others link to it. Never copy details between docs.
- **CLAUDE.md stays high-level.** Any section that grows past ~5 lines
  of detail should move to a design doc and be replaced with a link.
- **Prompt the user for doc updates** after code changes that alter
  subsystem behavior, state machines, control flow, controller
  bindings, homing, timeouts, or cross-subsystem coordination.
- **Cross-references use file-relative paths** (e.g.,
  `[ADR-009](../adr/ADR-009-centralized-can-refresh.md)`), not
  absolute repo paths.
- **Relative links must resolve.** Every `[text](path)` and
  `[text](#anchor)` must point to an existing file or in-document
  heading anchor. Broken **file** links are caught by
  [`verify-docs.sh`](../hooks/verify-docs.sh)'s `broken-link` check in
  CI; in-document **anchors** are not auto-checked, so verify those by
  hand. When you move or rename a file, grep for inbound links and
  update them in the same commit.
- **Common docs must stay self-contained.** Files under
  `docs/common/` **shall not** link to, or name specific artifacts
  in, `docs/team-lib/` or any `docs/<project>/` tier. `common/` is
  designed to vendor unchanged into any 271 Java project, including
  projects that don't ship `team-lib/`. Use abstract phrasing ("the
  project's safety policy") and let consumers bind concrete names
  in their own doc tier. See
  [`docs/common/README.md`](../../docs/common/README.md#portability)
  for rationale and examples. Enforced by
  [`.claude/hooks/check-common-tier-isolation.sh`](../hooks/check-common-tier-isolation.sh).
- **Max line length: 140 characters** (enforced by
  [`.claude/hooks/lint-markdown.sh`](../hooks/lint-markdown.sh));
  tables and URLs are exempt but keep them as short as possible.

## Authoritative docs

This rule file is the authoritative source for doc conventions. See
also:

- [`Standard-Compliance.md`](../../docs/coding-standard/java/Standard-Compliance.md)
  — enforcement matrix mapping each `CODE-*` rule (including doc
  rules) to its severity and check mechanism.
- [`docs/team-lib/planning/README.md`](../../docs/team-lib/planning/README.md)
  — map of every planning document.
