<!-- markdownlint-disable MD007 MD013 MD031 MD032 -->
# Team 271 Java Coding Standard

Document No: 271-JCS\
Revision: Draft

---

## 1. Introduction

### 1.1 Purpose

This document describes the software coding standard for Team 271's FRC
robot Java codebase. It ensures that all team members write reliable,
readable, and maintainable code that can survive a 2:30 match without
crashing, can be debugged on the practice field under time pressure, and
can be handed off between programmers across seasons.

The core principles are defensive coding, predictable execution, minimal
complexity, and rigorous formatting -- all directly applicable to FRC
robot code running in a tight real-time loop.

In this document, any rule specified with "shall" denotes a mandatory
requirement. Rules specified with "should" are recommended practices.

### 1.2 Scope

This standard applies to all Java source code written by the team,
covering both:

- **Reusable library code** (`com.example.lib.*` placeholder) —
  packaged for consumption by any downstream project.
- **Robot-project code** (`com.example.app.*` placeholder) — the
  season robot's application that depends on the library.

The two share most rules. Where they differ, sections explicitly note
"library only" or "robot projects may." Notable example: subsystem
instantiation — reusable library code uses explicit instantiation and
no singletons; robot-project subsystems commonly use the singleton
pattern documented in the consuming project's own templates.

**Exempt from formatting rules** (but not safety rules):

- Generated code: `BuildConstants.java` (gversion), `TunerConstants.java`
  (CTRE Tuner X), `LimelightHelpers.java`
- Third-party vendordep source code

### 1.3 Terminology (Acronyms Used in This Document)

This glossary defines proper-name acronyms used throughout the coding
standard and its companion documents. For the separate allowlist of
short-form tokens permitted in *code identifiers* (e.g., `cfg`, `idx`,
`msg`), see
[Appendix A](Team271-Software-Coding-Standard-Appendices.md#appendix-a-approved-identifier-abbreviations).

| Abbreviation | Meaning |
| ------------ | ------- |
| ADR | Architecture Decision Record |
| CAN | Controller Area Network |
| CANivore | CTRE CAN-to-USB bridge |
| CCW | Counter-Clockwise |
| CTRE | Cross The Road Electronics |
| CW | Clockwise |
| FOC | Field-Oriented Control |
| FRC | FIRST Robotics Competition |
| FX | TalonFX (CTRE brushless motor controller) |
| FXS | TalonFXS (CTRE brushed motor controller) |
| GC | Garbage Collection |
| HAL | Hardware Abstraction Layer (WPILib) |
| IMU | Inertial Measurement Unit |
| JVM | Java Virtual Machine |
| NT | NetworkTables |
| PID | Proportional-Integral-Derivative (controller) |
| RPS | Rotations Per Second |
| SCMP | Software Configuration Management Plan |
| SCS | Software Coding Standard (this document) |
| SDD | Software Design Description |
| SDP | Software Development Plan |
| SemVer | Semantic Versioning |
| SRS | Software Requirements Specification |
| SVP | Software Verification Plan |
| WPILib | WPI Robotics Library |

### 1.4 Applicable Documents

For vendor and tool documentation (WPILib, CTRE Phoenix 6, PathPlanner,
Choreo, AdvantageKit, Limelight, PhotonVision, Elastic, Spotless, etc.),
see [`docs/reference-urls.md`](../../reference-urls.md). That file is the
single authoritative index for external vendor/tool URLs.

The following industry coding standards are normative references for
this document. §1.5 below explains how each one shapes specific rules:

- [MISRA C:2025][misra-c] -- Safety-critical C coding guidelines (concepts adapted to Java). Local [PDF][misra-c-pdf]
- [MISRA C++:2023][misra-cpp] -- Safety-critical C++ coding guidelines (concepts adapted to Java). Local [PDF][misra-cpp-pdf]
- [SEI CERT Java Coding Standard][cert-java] -- Secure Java coding rules
- [JPL "Power of 10" Rules][jpl-power10] -- NASA/JPL rules for safety-critical code
- [DO-178C][do178c] -- Avionics software certification standard (process philosophy)
- [Barr Group Embedded C Coding Standard][barr] -- Embedded coding conventions

[misra-c]: https://misra.org.uk/misra-c/
[misra-c-pdf]: ../../third-party/MISRA-C-2025.pdf
[misra-cpp]: https://misra.org.uk/misra-cpp/
[misra-cpp-pdf]: ../../third-party/MISRA-CPP-2023.pdf
[cert-java]: https://wiki.sei.cmu.edu/confluence/display/java/
[jpl-power10]: https://en.wikipedia.org/wiki/The_Power_of_10:_Rules_for_Developing_Safety-Critical_Code
[do178c]: https://en.wikipedia.org/wiki/DO-178C
[barr]: https://barrgroup.com/embedded-systems/books/embedded-c-coding-standard

### 1.5 Industry Standard References

Several rules in this document are inspired by coding standards used in
aerospace, automotive, and other safety-critical industries. We are not
claiming compliance with any of these standards -- we are a high school
robotics team, not building flight software. But many of the same
problems apply: our code runs in real-time loops, controls physical
mechanisms, and must not crash during a match. Where our rules align
with an industry standard, we call it out so you know *why* the rule
exists and that it is not just our opinion.

**MISRA C:2025 and MISRA C++:2023** -- Originally written for
automotive software (think anti-lock brakes and airbag controllers).
MISRA defines rules for writing C and C++ code that is predictable and
safe. MISRA C:2025 is the current edition of the C guidelines
(superseding MISRA C:2023); MISRA C++:2023 is the current edition of
the C++ guidelines (targeting C++17 and superseding MISRA C++:2008).
Many of their rules about control flow, switch statements, and side
effects apply equally to Java. When we cite MISRA, the concept
transfers even though the language does not -- the rule numbers are
edition-agnostic by convention, so citations like "MISRA Rule 13.4" or
"MISRA Directive 4.4" in the companion documents refer to the same
guidance regardless of which edition originated the rule.

**SEI CERT Java Coding Standard** -- Published by Carnegie Mellon's
Software Engineering Institute. These are Java-specific rules for
writing secure, reliable code. This is the most directly applicable
standard since we also write Java.

**JPL "Power of 10"** -- Ten rules written by Gerard Holzmann at
NASA's Jet Propulsion Laboratory for code that runs on spacecraft.
If your Mars rover's code crashes, there is nobody to reboot it.
Rules like "no recursion," "no dynamic allocation after init," and
"all loops must have a fixed upper bound" apply directly to our
tight real-time loop.

**DO-178C** -- The standard used to certify software in commercial
aircraft. It is a process standard (it tells you *how* to develop
software, not specific coding rules), but its emphasis on
deterministic execution, traceable state machines, and defensive
coding directly influenced our subsystem architecture.

**Barr Group Embedded C Coding Standard** -- A widely-used industry
coding standard for embedded systems. Its naming conventions,
mandatory braces, and module organization rules influenced our own
formatting and naming choices.

---

## 2. Language and Build

This standard targets Java 17. Build-system specifics (Gradle
plugins, WPILib toolchain, continuous-integration pipeline) are
configured in each consuming project's own build files. The
coding-standard index in [README.md](README.md) organizes the rule
companions by topic (General, Format, Control, Methods, Variables,
Comments, Debug, Safety, Compliance, Appendices).

---

## 3. Source Code Presentation

Each consuming project maintains its own source-code presentation
templates — file layouts, class-member ordering, constants
organization — aligned with its own architecture. Normative rules
that apply regardless of template (naming, formatting,
documentation, etc.) are in §4.

---

## 4. Coding Guidelines

Rules are organized by topic into companion documents. Each companion
owns its own rule prefix; cite rules by ID (e.g., `CODE-GEN-004` or
`CODE-VAR-001a`) in review comments and commits.

| Topic | Companion | Rule prefix |
| ----- | --------- | ----------- |
| General | [`-General.md`](Team271-Software-Coding-Standard-General.md) | `CODE-GEN-*` |
| Formatting | [`-Format.md`](Team271-Software-Coding-Standard-Format.md) | `CODE-FMT-*` |
| Modules and Files | [`-Modules.md`](Team271-Software-Coding-Standard-Modules.md) | `CODE-MAF-*` |
| Methods | [`-Methods.md`](Team271-Software-Coding-Standard-Methods.md) | `CODE-FUN-*` |
| Variables | [`-Variables.md`](Team271-Software-Coding-Standard-Variables.md) | `CODE-VAR-*` |
| Control Structures | [`-Control.md`](Team271-Software-Coding-Standard-Control.md) | `CODE-CTL-*` |
| Comments | [`-Comments.md`](Team271-Software-Coding-Standard-Comments.md) | `CODE-COM-*` |
| Debugging and Telemetry | [`-Debug.md`](Team271-Software-Coding-Standard-Debug.md) | `CODE-BUG-*` |
| Safety Practices | [`-Safety.md`](Team271-Software-Coding-Standard-Safety.md) | `CODE-SAF-*` |

Supporting companions:

- [`-Compliance.md`](Team271-Software-Coding-Standard-Compliance.md)
  — §5 Static Analysis and enforcement matrix.
- [`-Appendices.md`](Team271-Software-Coding-Standard-Appendices.md)
  — Appendices A–E, H, I (approved abbreviations, Java keyword list,
  rule-deviation process, etc.).
