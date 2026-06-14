<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard

| Field | Value |
| ----- | ----- |
| Document No | `SCS-Java` |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

## Revision History

| Revision | Date | Author | Description |
| -------- | ---- | ------ | ----------- |
| 0.1 | `2026-06-14` | — | Initial draft |

---

## 1. Introduction

### 1.1 Purpose

This document describes the software coding standard for the
Java codebases of `Team271-Lib`. It ensures that all
contributors write reliable, readable, and maintainable code
that holds up under load, can be debugged on short notice, and
can be handed off between contributors over the long lifetime
of a Java project.

The core principles are defensive coding, predictable execution,
minimal complexity, and rigorous formatting.

In this document, any rule specified with **shall** denotes a
mandatory requirement. Rules specified with **should** are
recommended practices.

### 1.2 Scope

This standard applies to all Java source code written for
`Team271-Lib`, covering both:

- **Reusable library code** (`com.example.lib.*` placeholder) —
  packaged for consumption by any downstream project.
- **Application-project code** (`com.example.app.*`
  placeholder) — the application that depends on the library.

The two share most rules. Where they differ, sections
explicitly note "library only" or "application projects may."
Rules specific to reusable library code are collected in
[`Standard-Library.md`](Standard-Library.md) (`CODE-LIB-*`).

**Exempt from all `CODE-*` rules** (with safety-critical rules
re-applied at human review):

- Generated source files — the generating tool owns them; manual
  edits are forbidden by
  [CODE-MAF-004](Standard-Modules.md#code-maf-004).
- Vendored third-party source imported unchanged from upstream
  releases — the vendor owns them; the project's exposure layer
  is the wrapper code that calls into the vendor source, and
  that wrapper code is in scope.

Tooling **shall** enforce this exemption at the Spotless,
Checkstyle, SpotBugs, and Error Prone layers — see the
consuming project's `build.gradle`, Checkstyle config, and
SpotBugs exclude config for the concrete exclusion patterns.

### 1.3 Terminology (Acronyms Used in This Document)

This glossary defines proper-name acronyms used throughout the
coding standard and its companion documents. For the separate
allowlist of short-form tokens permitted in *code identifiers*
(e.g., `cfg`, `idx`, `msg`), see
[Appendix A](Standard-Appendices.md#appendix-a-approved-identifier-abbreviations).

| Abbreviation | Meaning |
| ------------ | ------- |
| ADR | Architecture Decision Record |
| CERT | CERT Division of the Software Engineering Institute |
| CWE | Common Weakness Enumeration |
| GC | Garbage Collection |
| JPL | Jet Propulsion Laboratory |
| JVM | Java Virtual Machine |
| MISRA | Motor Industry Software Reliability Association |
| OWASP | Open Worldwide Application Security Project |
| OS | Operating System |
| PR | Pull Request |
| SCMP | Software Configuration Management Plan |
| SCS | Software Coding Standard (this document) |
| SDD | Software Design Description |
| SDP | Software Development Plan |
| SEI | Software Engineering Institute (Carnegie Mellon) |
| SemVer | Semantic Versioning |
| SQA | Software Quality Assurance |
| SRS | Software Requirements Specification |
| SVP | Software Verification Plan |

### 1.4 Applicable Documents

The following industry coding standards are normative references
for this document. [§1.5](#15-industry-standards-rationale)
below explains how each one shapes specific rules:

- [MISRA C:2025][misra-c] — Safety-critical C coding guidelines
  (concepts adapted to Java)
- [MISRA C++:2023][misra-cpp] — Safety-critical C++ coding
  guidelines (concepts adapted to Java)
- [SEI CERT Oracle Coding Standard for Java][cert-java] —
  Secure Java coding rules
- [Barr Group Embedded C Coding Standard (BARR-C:2018)][barr] — Embedded
  coding conventions (concepts adapted to Java)
- JPL Institutional Coding Standard for the C Programming
  Language (D-60411) — NASA/JPL rules for safety-critical code.
  This document does not link to JPL D-60411 directly; the
  rules are cited by name in the relevant rule headings.
- NASA/JPL Power of 10 — Gerard J. Holzmann, "The Power of 10:
  Rules for Developing Safety-Critical Code," IEEE Computer,
  vol. 39, no. 6, June 2006 (DOI 10.1109/MC.2006.212); the
  original 10 rules, later expanded into JPL D-60411. Cited by
  name in the relevant rule headings.
- DO-178C — Avionics software certification standard (process
  philosophy referenced by name in some industry-note callouts;
  no URL is included).
- [OWASP Top 10][owasp-top10] and
  [OWASP Cheat Sheet Series][owasp-cheats] — Web-application
  security baseline, used by
  [`Standard-Security.md`](Standard-Security.md).

[misra-c]: https://misra.org.uk/product/misra-c2025/
[misra-cpp]: https://misra.org.uk/product/misra-cpp2023/
[cert-java]: https://wiki.sei.cmu.edu/confluence/display/java/SEI+CERT+Oracle+Coding+Standard+for+Java
[barr]: https://barrgroup.com/embedded-c-coding-standard
[owasp-top10]: https://owasp.org/www-project-top-ten/
[owasp-cheats]: https://cheatsheetseries.owasp.org/

#### 1.4.1 Reference Documents

| Document | Description |
| -------- | ----------- |
| `SCMP` | Software Configuration Management Plan |
| `SDP` | Software Development Plan |
| `SRS` | Software Requirements Specification |
| `SVP` | Software Verification Plan |
| `SDD` | Software Design Description |

Any deviations to this document **shall** be noted in the
project's Software Development Plan (SDP).

#### 1.4.2 Companion Documents

This coding standard is split across several companion documents
to keep each one focused and manageable. The main document (this
file) routes by topic; each companion owns one rule prefix:

| Topic | Companion | Rule prefix |
| ----- | --------- | ----------- |
| General (keywords, annotations, type safety, exceptions, GC, concurrency) | [`Standard-General.md`](Standard-General.md) | `CODE-GEN-*` |
| Formatting (braces, parens, blank lines, line endings, imports) | [`Standard-Format.md`](Standard-Format.md) | `CODE-FMT-*` |
| Modules and Files (class/file naming, packages, constants, generated code) | [`Standard-Modules.md`](Standard-Modules.md) | `CODE-MAF-*` |
| Methods (naming, single-exit, defensive checks) | [`Standard-Methods.md`](Standard-Methods.md) | `CODE-FUN-*` |
| Variables (naming, init, types, magic numbers) | [`Standard-Variables.md`](Standard-Variables.md) | `CODE-VAR-*` |
| Control Structures (if/switch/loops) | [`Standard-Control.md`](Standard-Control.md) | `CODE-CTL-*` |
| Comments (JavaDoc, block, inline) | [`Standard-Comments.md`](Standard-Comments.md) | `CODE-COM-*` |
| Library Design (reusable-library patterns: DI, lifecycle, package isolation) | [`Standard-Library.md`](Standard-Library.md) | `CODE-LIB-*` |
| Security Coding Practices | [`Standard-Security.md`](Standard-Security.md) | `CODE-SEC-*` |

Supporting companions:

- [`Standard-Compliance.md`](Standard-Compliance.md) — §5 Static
  Analysis and Tooling (Spotless, compiler warnings, JVM,
  static analyzers) and the rule-to-tool enforcement matrix.
- [`Standard-Appendices.md`](Standard-Appendices.md) — Reference
  appendices A, B, C, and I (approved abbreviations, Java
  reserved words, `final` keyword guide, naming quick reference).

### 1.5 Industry Standards Rationale

Several rules in this document are inspired by coding standards
used in aerospace, automotive, and other safety-critical
industries. This standard does not claim compliance with any of
them — but many of the same problems apply to general-purpose
Java code: real-time loops, long-lived services, code that must
not crash under load. Where a rule aligns with an industry
standard, the rule heading cites the source so reviewers know
*why* the rule exists and that it is not just project opinion.

**MISRA C:2025 and MISRA C++:2023** — Originally written for
automotive software (anti-lock brakes, airbag controllers).
MISRA defines rules for writing C and C++ code that is
predictable and safe. Many of their rules about control flow,
switch statements, and side effects apply equally to Java.
When this standard cites MISRA, the concept transfers even
though the language does not.

**SEI CERT Oracle Coding Standard for Java** — Published by
Carnegie Mellon's Software Engineering Institute. Java-specific
rules for writing secure, reliable code. This is the most
directly applicable external standard.

**NASA/JPL "Power of Ten"** — Ten rules written by Gerard Holzmann at
NASA's Jet Propulsion Laboratory for code that runs on
spacecraft. Rules like "no recursion," "no dynamic allocation
after init," and "all loops must have a fixed upper bound"
apply directly to any tight real-time or per-cycle loop.

**DO-178C** — The standard used to certify software in
commercial aircraft. It is a process standard (it tells you
*how* to develop software, not specific coding rules), but its
emphasis on deterministic execution, traceable state machines,
and defensive coding directly influenced the structure of this
standard.

**Barr Group Embedded C Coding Standard** — A widely-used
industry coding standard for embedded systems. Its naming
conventions, mandatory braces, and module organization rules
influenced the formatting and naming choices here.

**OWASP Top 10 and OWASP Cheat Sheet Series** — The de facto
baseline for web-application security. Used in
[`Standard-Security.md`](Standard-Security.md) for the
threat-model framing of the `CODE-SEC-*` rules.

---

## 2. Language and Build

This standard targets **Java 17** as the minimum baseline.
Newer language features (Java 21 records, pattern matching for
`switch`, sealed types) **may** be used when the consuming
project's runtime and tooling support them; the project's
`build.gradle` records the actual target version.

Build-system specifics (Gradle plugin versions, dependency
manifests, CI pipeline) live in each consuming project's own
build files. Spotless, Checkstyle, SpotBugs, and Error Prone
configuration is documented in
[`Standard-Compliance.md`](Standard-Compliance.md).

---

## 3. Source Code Presentation

Each consuming project maintains its own source-code
presentation templates — file layouts, class-member ordering,
constants organization — aligned with its own architecture.
Normative rules that apply regardless of template (naming,
formatting, documentation) are in §4.

---

## 4. Coding Guidelines

Rules are organised by topic into the companion documents
listed in [§1.4.2](#142-companion-documents). Each companion
owns its own rule prefix; cite rules by ID (e.g.,
`CODE-GEN-004` or `CODE-VAR-001a`) in review comments and
commit messages — the ID is stable even if the surrounding
wording changes. Sub-rules use a single lowercase letter
suffix (`CODE-CTL-002a`).

| Topic | Companion | Rule prefix |
| ----- | --------- | ----------- |
| General | [`Standard-General.md`](Standard-General.md) | `CODE-GEN-*` |
| Formatting | [`Standard-Format.md`](Standard-Format.md) | `CODE-FMT-*` |
| Modules and Files | [`Standard-Modules.md`](Standard-Modules.md) | `CODE-MAF-*` |
| Methods | [`Standard-Methods.md`](Standard-Methods.md) | `CODE-FUN-*` |
| Variables | [`Standard-Variables.md`](Standard-Variables.md) | `CODE-VAR-*` |
| Control Structures | [`Standard-Control.md`](Standard-Control.md) | `CODE-CTL-*` |
| Comments | [`Standard-Comments.md`](Standard-Comments.md) | `CODE-COM-*` |
| Library Design (reusable-library patterns: DI, lifecycle, package isolation) | [`Standard-Library.md`](Standard-Library.md) | `CODE-LIB-*` |
| Security Coding Practices | [`Standard-Security.md`](Standard-Security.md) | `CODE-SEC-*` |

Supporting companions:

- [`Standard-Compliance.md`](Standard-Compliance.md) — §5 Static
  Analysis and Tooling plus the enforcement matrix.
- [`Standard-Appendices.md`](Standard-Appendices.md) —
  Appendices A, B, C, and I (approved abbreviations, Java
  reserved words, `final` keyword guide, naming quick
  reference).

---

## 5. Static Analysis and Tooling

See [`Standard-Compliance.md`](Standard-Compliance.md) — this
master document is a router; the tooling chapter lives in the
compliance companion so that the rule prefix layout in §4
and the enforcement strategy can be navigated independently.

<!-- markdownlint-enable MD007 -->
