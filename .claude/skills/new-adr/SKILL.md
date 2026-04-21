---
name: new-adr
description: Scaffold a new Architecture Decision Record (ADR) for Team271-Lib, with next-sequential number and today's date pre-filled, following the template in docs/team-lib/planning/adr/README.md.
disable-model-invocation: true
argument-hint: "<short-kebab-title>"
allowed-tools: Bash(ls *) Bash(git status *)
---

# New ADR

Create a new Architecture Decision Record for Team271-Lib.

**Arguments:** `$ARGUMENTS` — short kebab-case title (e.g.
`null-safety-annotations`, `canbus-abstraction`). Used both as the
filename suffix and as a starting point for the document title.

## Pre-computed values

- Next ADR number: <!-- markdownlint-disable-next-line MD013 -->
  `!`ls docs/team-lib/planning/adr/ADR-*.md 2>/dev/null | sed -n 's/.*ADR-\([0-9]\{3\}\).*/\1/p' | sort -n | tail -1 | awk '{printf "%03d", $1+1}' ``
- Today's date (YYYY-MM-DD): `!`date +%Y-%m-%d``

## Instructions for Claude

1. Using the pre-computed values above, name the file
   `docs/team-lib/planning/adr/ADR-<NNN>-$ARGUMENTS.md` where
   `<NNN>` is the next ADR number shown above.

2. Create the file with this skeleton, filling in the `NNN`, the
   date, and a human-readable title derived from `$ARGUMENTS`:

    ```markdown
    # ADR-NNN: <Human-Readable Title>

    ## Status

    Proposed

    ## Date

    YYYY-MM-DD

    ## Context

    <What problem are we solving? What forces are at play?>

    ## Decision

    <What was decided? One clear statement.>

    ## Rationale

    1. <Why this decision, and not something else>
    2. <...>

    ## Consequences

    **Easier:**

    - <...>

    **Harder:**

    - <...>

    ## Alternatives Considered

    - **<Alternative>.** Rejected — <reason>.

    ## References

    - <Links to SDDs, other ADRs, external docs>
    ```

3. **Do not fill in the substantive content (Context, Decision,
   Rationale, Consequences, Alternatives).** Leave them as
   placeholders for the user to draft. Only fill in: title,
   status (`Proposed`), date, and filename.

4. Remind the user to:
    - Add a row to the "Planned ADRs" table in
      `docs/team-lib/planning/README.md` if this ADR is a
      replacement for a planned slot, or add a full row to the
      main ADR table once the status becomes `Accepted`.
    - Link the new ADR from any SDDs or rules files that it
      governs once it is `Accepted`.
    - Per `.claude/rules/planning.md`, ADRs are permanent once
      `Accepted`; status values are `Proposed | Accepted |
      Superseded by ADR-XXX` — no `Deprecated`.

5. Do not commit the new ADR — leave staging and commit to the
   user.
