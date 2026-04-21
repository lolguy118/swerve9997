---
paths:
  - "docs/team-lib/planning/**"
  - "docs/common/planning/**"
---

# Rule: Planning Documents

Applies to planning docs in two tiers:

- `docs/common/planning/` — shared framework (SemVer policy,
  phase model, verification framework).
- `docs/team-lib/planning/` — library-specific instances (SDP, SRS,
  SVP, SCMP, ADRs, SDDs) that cite the common framework.

## Rules Claude must apply

- **ADR format is fixed.** Use the template in
  [`adr/README.md`](../../docs/team-lib/planning/adr/README.md).
  Frontmatter: `# ADR-NNN: Title`, then `## Status`, `## Date`
  (YYYY-MM-DD), `## Context`, `## Decision`, `## Rationale`,
  `## Consequences`, `## Alternatives Considered`, `## References`.
- **ADR statuses are `Proposed | Accepted | Superseded by ADR-XXX`.**
  `Deprecated` is not a status Team271-Lib uses — a reversed
  decision becomes `Superseded`. Draft decisions are `Proposed`;
  binding decisions are `Accepted`.
- **SDD format is fixed.** Every SDD starts with a header table
  (Document No., Revision, Date, Status, Requirements Traced) and
  has nine sections: Purpose, Scope and Boundaries, Module
  Decomposition, Data Flow, Key Design Decisions, Error Handling,
  Platform Portability Notes, Configuration, Test Coverage
  Requirements.
- **SDD §2 Scope lists only what the SDD covers.** Routing to peer
  SDDs lives in the Package-to-SDD Map in
  [`docs/team-lib/planning/README.md`](../../docs/team-lib/planning/README.md);
  do not duplicate it in per-SDD "does not cover" lists.
- **SDD §3 Module Decomposition describes modules, not function
  signatures.** Describe classes, interfaces, and subpackages plus
  what they're responsible for. API-contract tables (method names +
  return types) belong in dedicated sections — §6 Passthrough
  Getter Reference, §7 Feature Coverage Matrix, etc. — when those
  sections exist. Method-name prose inside §3 drifts into
  Javadoc territory.
- **Requirement IDs `[PREFIX-NNN]`** are scoped. Allowed locations:
  (a) SRS requirement tables, (b) SVP traceability matrix, (c) each
  SDD's header-table "Requirements Traced" row, and (d) each SDD's
  §9 "Test Coverage Requirements" test-ID listing. Not allowed
  anywhere else — no IDs in SDD prose, guides, CLAUDE.md, coding
  standards, or code comments outside test-method Javadoc.
- **One ADR per decision.** If you find yourself writing an ADR with
  two decisions, split it. If you find yourself writing an ADR that
  duplicates an existing one, update the existing one instead.
- **ADRs are permanent once Accepted.** Do not delete or rewrite an
  accepted ADR. If a decision is reversed, mark it `Superseded by
  ADR-XXX` and write a new ADR.
- **Planned ADRs and SDDs live in the planning README.** Reserve
  the slot in the `Planned ADRs` / `Planned SDDs` tables of
  [`docs/team-lib/planning/README.md`](../../docs/team-lib/planning/README.md)
  with a `> **Status: Planned — Not Yet Implemented.**` marker.
  Don't author a speculative ADR or SDD with no concrete consumer;
  write them when a concrete need emerges.
- **SDD cross-references use file-relative paths** — sibling SDDs
  are `SDD-name.md`, ADRs are `../adr/ADR-NNN-title.md`. Section
  anchors use GitHub's standard form (lowercase, spaces → dashes,
  punctuation stripped).
- **No numeric tunables in SRS or SDDs.** Same rule as
  [`docs.md`](docs.md).

## Authoritative doc

[`docs/team-lib/planning/README.md`](../../docs/team-lib/planning/README.md)
lists every planning document. The ADR template is in
[`docs/team-lib/planning/adr/README.md`](../../docs/team-lib/planning/adr/README.md).
