# Team 271 Software Coding Standard (Retired)

> **Status: Retired - superseded 2026-06-14.** The coding standard now
> lives at [`../../coding-standard/`](../../coding-standard/):
> [`java/Standard.md`](../../coding-standard/java/Standard.md) (pure-Java
> rules plus the `CODE-LIB-*` library tier),
> [`frc/Standard.md`](../../coding-standard/frc/Standard.md) (the FRC
> overlay), and
> [`team271-lib-supplement.md`](../../coding-standard/team271-lib-supplement.md)
> (project values and library bindings). Rule IDs are unchanged, so bare
> `CODE-XXX-NNN` citations across the repo resolve against the new
> standard. The full retired text is in git history (last full revision:
> the commit preceding the 2026-06-14 consolidation).

## Why a file-split map, not an ID map

The consolidation adopts the project-template's standard, which keeps
the same `CODE-XXX-NNN` IDs but splits this single multi-chapter
document into language folders (`java/`, `frc/`). Old links **by chapter
file** therefore need the path remap below; the IDs themselves did not
change.

## Legacy chapter to new file

| Legacy chapter (this folder) | New file |
| --- | --- |
| `Team271-Software-Coding-Standard.md` (master) | [`java/Standard.md`](../../coding-standard/java/Standard.md) |
| `-General.md` (`CODE-GEN-*`) | [`java/Standard-General.md`](../../coding-standard/java/Standard-General.md) |
| `-Format.md` (`CODE-FMT-*`) | [`java/Standard-Format.md`](../../coding-standard/java/Standard-Format.md) |
| `-Modules.md` (`CODE-MAF-*`) | [`java/Standard-Modules.md`](../../coding-standard/java/Standard-Modules.md) |
| `-Methods.md` (`CODE-FUN-*`) | [`java/Standard-Methods.md`](../../coding-standard/java/Standard-Methods.md) |
| `-Variables.md` (`CODE-VAR-*`) | [`java/Standard-Variables.md`](../../coding-standard/java/Standard-Variables.md) |
| `-Control.md` (`CODE-CTL-*`) | [`java/Standard-Control.md`](../../coding-standard/java/Standard-Control.md) |
| `-Comments.md` (`CODE-COM-*`) | [`java/Standard-Comments.md`](../../coding-standard/java/Standard-Comments.md) |
| `-Debug.md` (`CODE-BUG-*`) | [`frc/Standard-Debug.md`](../../coding-standard/frc/Standard-Debug.md) |
| `-Safety.md` (`CODE-SAF-*`) | [`frc/Standard-Safety.md`](../../coding-standard/frc/Standard-Safety.md) |
| `-Appendices.md` | [`java/Standard-Appendices.md`](../../coding-standard/java/Standard-Appendices.md) |
| `-Compliance.md` (section 5 tooling) | [`java/Standard-Compliance.md`](../../coding-standard/java/Standard-Compliance.md) |

The library tier (`CODE-LIB-*`, formerly in
[`../../team-lib/coding-standard/`](../../team-lib/coding-standard/)) is
now [`java/Standard-Library.md`](../../coding-standard/java/Standard-Library.md),
with the Team271-Lib bindings in the
[supplement](../../coding-standard/team271-lib-supplement.md). The
`CODE-SEC-*` family, the formal `CODE-FRC-*` split, and several sub-rules
are additions in the template-derived standard.
