# Third-Party Reference Documents

This folder holds licensed copies of external standards PDFs that
are cited in Team 271's coding standard and design docs. Keeping
local copies lets contributors look up the rules cited throughout
the coding-standard companion documents without each person needing
to purchase their own copy of a paywalled standard.

## Contents

| File | Publisher | Edition | Cited from |
| ---- | --------- | ------- | ---------- |
| [`MISRA-C-2025.pdf`](MISRA-C-2025.pdf) | The MISRA Consortium | MISRA C:2025 | [SCS §1.4](../common/coding-standard/Team271-Software-Coding-Standard.md#14-applicable-documents), [§1.5](../common/coding-standard/Team271-Software-Coding-Standard.md#15-industry-standard-references) |
| [`MISRA-CPP-2023.pdf`](MISRA-CPP-2023.pdf) | The MISRA Consortium | MISRA C++:2023 | [SCS §1.4](../common/coding-standard/Team271-Software-Coding-Standard.md#14-applicable-documents), [§1.5](../common/coding-standard/Team271-Software-Coding-Standard.md#15-industry-standard-references) |

Inline rule citations like "MISRA Rule 13.4" or "MISRA Directive 4.4"
also appear throughout the companion coding-standard files
([Comments](../common/coding-standard/Team271-Software-Coding-Standard-Comments.md),
[Control](../common/coding-standard/Team271-Software-Coding-Standard-Control.md),
[Format](../common/coding-standard/Team271-Software-Coding-Standard-Format.md),
[Methods](../common/coding-standard/Team271-Software-Coding-Standard-Methods.md),
[Variables](../common/coding-standard/Team271-Software-Coding-Standard-Variables.md),
[General](../common/coding-standard/Team271-Software-Coding-Standard-General.md));
rule numbers are edition-agnostic by convention, so they resolve
against whichever local PDF is relevant.

## License and Usage

These PDFs are copyrighted by their publisher and are included
under the team's licensed copy.

**Do not redistribute.** The files in this folder are for internal
Team 271 use only and must not be shared outside the team, copied
into other repositories, or published. Before making any repository
that vendors this folder public, delete the PDFs first.

## Maintenance

- When a new edition is published (e.g., a future MISRA C amendment),
  replace the PDF, update the version column in the table above, and
  bump the edition reference in
  [SCS §1.4](../common/coding-standard/Team271-Software-Coding-Standard.md#14-applicable-documents)
  and [SCS §1.5](../common/coding-standard/Team271-Software-Coding-Standard.md#15-industry-standard-references).
- Add new third-party PDFs only if they are cited normatively by the
  coding standard or a design doc. Ad-hoc reference material belongs
  elsewhere.
- PDFs are declared binary in
  [`.gitattributes`](../../.gitattributes) so Git does not attempt
  line-ending normalization on them.
