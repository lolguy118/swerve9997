# Rule: Planning Documents

Applies to everything under `docs/team-lib/planning/`: SDP, SRS, SVP,
SCMP, ADRs, and SDDs.

## Rules Claude must apply

- **ADR format is fixed.** Use the template in
  [`adr/README.md`](../../docs/team-lib/planning/adr/README.md).
  Frontmatter: `# ADR-NNN: Title`, then `## Status`, `## Date`
  (YYYY-MM-DD), `## Context`, `## Decision`, `## Rationale`,
  `## Consequences`, `## Alternatives Considered`, `## References`.
- **SDD format is fixed.** Every SDD starts with a header table
  (Document No., Revision, Date, Status, Requirements Traced) and
  has nine sections: Purpose, Scope and Boundaries, Module
  Decomposition, Data Flow, Key Design Decisions, Error Handling,
  Platform Portability Notes, Configuration, Test Coverage
  Requirements.
- **Requirement IDs `[PREFIX-NNN]`** are used **only inside SRS
  tables and the SVP traceability matrix.** Never in SDDs, guides,
  CLAUDE.md, or code comments outside test methods.
- **One ADR per decision.** If you find yourself writing an ADR with
  two decisions, split it. If you find yourself writing an ADR that
  duplicates an existing one, update the existing one instead.
- **ADRs are permanent once Accepted.** Do not delete or rewrite an
  accepted ADR. If a decision is reversed, mark it `Superseded by
  ADR-XXX` and write a new ADR.
- **SDD cross-references use file-relative paths** — sibling SDDs
  are `SDD-name.md`, ADRs are `../adr/ADR-NNN-title.md`.
- **No numeric tunables in SRS or SDDs.** Same rule as
  [`docs.md`](docs.md).

## Authoritative doc

[`docs/team-lib/planning/README.md`](../../docs/team-lib/planning/README.md)
lists every planning document. The ADR template is in
[`docs/team-lib/planning/adr/README.md`](../../docs/team-lib/planning/adr/README.md).
