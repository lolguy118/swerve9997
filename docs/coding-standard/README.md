# Team271-Lib Coding Standard

The shared coding standard for Team271-Lib (and the robot projects that
fork it), adopted from the project-template. It is split into a
pure-Java layer, an FRC overlay, and a project supplement.

> **Industry bridge.** Like professional software teams, this standard
> publishes rule IDs (`CODE-GEN-001`, `CODE-SAF-008`, ...) so review
> comments can cite the exact rule instead of arguing about style.

## Layout

- [`java/Standard.md`](java/Standard.md) - pure-Java rules
  (`CODE-GEN/FMT/MAF/FUN/VAR/CTL/COM/SEC-*`) plus the reusable-library
  tier (`CODE-LIB-*` in
  [`java/Standard-Library.md`](java/Standard-Library.md)).
- [`frc/Standard.md`](frc/Standard.md) - FRC overlay
  (`CODE-BUG/SAF/FRC-*`): telemetry, robot safety, NetworkTables,
  lifecycle, and generated-file exemptions.
- [`team271-lib-supplement.md`](team271-lib-supplement.md) -
  Team271-Lib's project values (package roots, generated files,
  abbreviations, language policy) and the concrete `CODE-LIB-*` library
  bindings the generic tier delegates.

## How to cite a rule

Cite the stable ID (e.g. `CODE-CTL-002`, sub-rule `CODE-CTL-002a`) in
review comments and PR descriptions; the ID stays stable even when the
surrounding wording changes. Pure-Java rules are cited by their `java/`
location, FRC rules by their `frc/` location.

## Provenance

This is the project-template's genericized form of the standard
Team271-Lib originally authored - the template literally derives its
Java/FRC content from this repo. The legacy
[`../common/coding-standard/`](../common/coding-standard/)
`Team271-Software-Coding-Standard*.md` family is retired (see its
tombstone); rule IDs are unchanged, so bare `CODE-XXX-NNN` citations
across the repo now resolve here.
