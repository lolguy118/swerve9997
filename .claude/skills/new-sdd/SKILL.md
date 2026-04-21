---
name: new-sdd
description: Scaffold a new Software Design Description (SDD) for a Team271-Lib layer or module, with the 9-section template pre-filled per .claude/rules/planning.md. Use when a new library package needs its own SDD.
disable-model-invocation: true
argument-hint: "<package-name> (e.g. vision, auto, new-layer)"
allowed-tools: Bash(ls *)
---

# New SDD

Create a new Software Design Description for a Team271-Lib layer or
module.

**Arguments:** `$ARGUMENTS` — package/module name in kebab-case
(e.g. `vision`, `auto`, `new-layer`). Used as the filename suffix
(`SDD-$ARGUMENTS.md`) and as the `com.team271.lib.<package>`
reference in the header.

## Pre-computed values

- Today's date (YYYY-MM-DD): `!`date +%Y-%m-%d``

## Instructions for Claude

1. Create the file at
   `docs/team-lib/planning/sdd/SDD-$ARGUMENTS.md` using this
   template. Fill in the title and date; leave the other
   substantive sections as placeholders for the user.

    ```markdown
    # SDD: `com.team271.lib.<package>` — <Human-Readable Title>

    | Field | Value |
    | ----- | ----- |
    | Document No. | TBD-SDD-<PACKAGE-UPPER> |
    | Revision | 0.1 |
    | Date | YYYY-MM-DD |
    | Status | Draft |
    | Requirements Traced | `[<PREFIX>-NNN]`..`[<PREFIX>-NNN]` (SRS §X.Y) |

    The normative keywords SHALL, SHOULD, and MAY follow the
    convention defined in
    [`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

    ## 1. Purpose

    <What does this module provide to the library? What problem
    does it solve?>

    ## 2. Scope and Boundaries

    This SDD covers:

    - <Class or interface> — <one-line role>
    - <...>

    ## 3. Module Decomposition

    ### 3.1 <Subsection>

    <Describe classes, interfaces, and subpackages. Do NOT
    enumerate method signatures — that belongs in Javadoc or in
    a dedicated API-reference section like §6 Passthrough Getter
    Reference (per `.claude/rules/planning.md`).>

    ## 4. Data Flow

    <Show how data moves through the module in a typical cycle.
    Method names and invocation traces are acceptable here.>

    ## 5. Key Design Decisions

    | Decision | Rationale | Reference |
    | -------- | --------- | --------- |
    | <Decision> | <Why> | [ADR-NNN](../adr/ADR-NNN-....md) |

    ## 6. Error Handling

    <How does this module fail? What are the fail-safe behaviors?
    Apply ADR-011 timeout + fail-safe + alert rule wherever
    waits occur.>

    ## 7. Platform Portability Notes

    <Differences between RoboRIO runtime, desktop simulation, and
    unit tests. Note what HAL init is required.>

    ## 8. Configuration

    <What configuration surfaces does this module expose? Cite
    named constants, not numeric values (per docs.md rule).>

    ## 9. Test Coverage Requirements

    | Area | HAL Required | Notes |
    | ---- | ------------ | ----- |
    | <Area> | Yes/No | <Test approach> |

    Test IDs: `[TEST-<PREFIX>-NNN]`.
    ```

2. **Do not fill in substantive content.** Leave placeholders
   for the user to draft. Only fill in: title, date, and the
   filename.

3. Remind the user to:
    - Add a row to the "Package-to-SDD Map" in
      `docs/team-lib/planning/README.md` mapping the
      `com.team271.lib.<package>` package to this new SDD.
    - Add corresponding `[<PREFIX>-NNN]` requirements to
      `docs/team-lib/planning/SRS.md` §4.
    - Add `[TEST-<PREFIX>-*]` row(s) to the SRS §7 traceability
      matrix.
    - Update the header-table "Requirements Traced" row once
      the [PREFIX-NNN] IDs are finalized.
    - Per `.claude/rules/planning.md`, the nine section
      numbering and titles are fixed — do not add or remove
      sections.

4. Do not commit the new SDD — leave staging and commit to the
   user.
