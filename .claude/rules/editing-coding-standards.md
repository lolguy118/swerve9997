---
paths:
  - "docs/coding-standard/**/*.md"
---

# Rule: Editing the Coding Standard

This rule fires when Claude edits a file inside `docs/coding-standard/`.
The standard was adopted from the project-template; these conventions
keep its rule IDs and citations stable. The advisory PostToolUse hook
[`check-coding-standards.sh`](../hooks/check-coding-standards.sh) flags
violations to stderr after each edit.

## Rules Claude must apply

- **Rule IDs are stable.** Never renumber an existing `CODE-XXX-NNN`.
  To retire a rule, mark its heading
  `DEPRECATED - superseded by CODE-YYY-MMM`; keep the ID and a short
  stub explaining the migration. Old PR comments and commits cite it.
- **Assign new IDs by scanning the family.** Before introducing a new
  rule, grep the same language folder for the highest existing `NNN`
  in that family and assign the next.
- **Verify before citing.** Before writing a `CODE-XXX-NNN` citation,
  confirm the rule exists in the target file by grepping the standard.
  Never write a citation from memory.
- **Sub-rules need parents.** Sub-rules use lowercase suffixes
  (`CODE-CTL-002a`, ...b, ...c) and require an existing parent rule with
  the same `XXX-NNN`.
- **Normative bodies required.** Every `### CODE-XXX-NNN` heading must
  be followed within 30 lines by `shall`, `should`, `shall not`, or
  `should not`. Non-normative material belongs in a Guidelines or
  Appendices section.
- **Anchors and cross-folder links.** Each rule heading in `java/` and
  `frc/` carries an explicit `<a id="code-xxx-nnn"></a>` line; cite by
  the bare-ID fragment `#code-xxx-nnn`. Links from `frc/` into `java/`
  use the relative path `../java/...#code-...`, not a bare uppercase ID
  (citation integrity is checked per language folder).
- **External standards: cite, don't paraphrase.** References to MISRA,
  JPL D-60411, Barr Group, SEI CERT, OWASP, WPILib, etc. must be links
  to the source under a domain listed in
  [`.claude/external-references.json`](../external-references.json).
  Do not paraphrase normative prose from those documents.
- **Vendor names are kept verbatim.** CTRE Phoenix 6, WPILib, REV,
  NavX, Limelight, PathPlanner, Choreo, AdvantageKit, PhotonVision are
  FRC-ecosystem facts and stay as-is.
- **Project-specific values live in the supplement.** Package roots,
  generated-file lists, approved abbreviations, the language-feature
  policy, and the concrete `CODE-LIB-*` library bindings go in
  [`docs/coding-standard/team271-lib-supplement.md`](../../docs/coding-standard/team271-lib-supplement.md),
  not patched into the generic rules, so future refreshes from the
  template stay clean merges.
- **Cross-file edits.** When you change a rule body, follow the
  citations: `grep -rn CODE-XXX-NNN docs/` and update any companions or
  consumers that paraphrase or extend the original wording.

## Authoritative doc

[`docs/coding-standard/README.md`](../../docs/coding-standard/README.md)
indexes the standard; the legacy chapter-to-new-file map is in the
[tombstone](../../docs/common/coding-standard/Team271-Software-Coding-Standard.md).
