# ADR-002: Java 17 + WPILib + GradleRIO Toolchain

## Status

Accepted

## Date

2026-04-20

## Context

The FRC ecosystem supports Java, C++, and Python for robot code, using
the WPILib framework and the GradleRIO build plugin for deploy and
simulation. The RoboRIO 2 runs a JDK bundled with the WPILib release,
setting a floor on the Java language version usable on the robot.

## Decision

Team271-Lib targets Java 17 as its language version. The build uses
Gradle with the GradleRIO plugin, matching the WPILib-distributed
toolchain. Code formatting is enforced by Spotless with Google Java
Format; dependencies are managed via vendordep JSON files under
`vendordeps/`.

## Rationale

1. **Java over C++.** Java has a gentler onboarding curve for high
   school students who may be writing their first production code.
   Garbage collection is an acceptable tradeoff in a 20 ms periodic
   loop with the discipline in `CODE-PERF` (no allocations in periodic
   methods).
2. **Java 17 over older Java.** Sealed types, pattern-matching switches,
   records, and text blocks make state-machine code, configuration
   objects, and diagnostic output substantially more concise than Java
   8. Java 17 is an LTS and matches the WPILib 2026 distribution.
3. **GradleRIO.** It is the official FRC build toolchain. Deviating
   would mean replicating deploy, simulation, and vendordep resolution
   from scratch.

## Consequences

**Easier:**

- Students can copy-paste example code from WPILib docs, CTRE examples,
  and AdvantageKit without translation.
- CI runs the same Gradle tasks as local development.
- Language features like records and enhanced switch make state
  machines clean.

**Harder:**

- Memory pressure matters: see `CODE-PERF` rules in the coding standard.
- JVM startup time is nonzero; the robot's first loop is sometimes
  slower than subsequent loops.
- Upgrading Java version is tied to WPILib's release schedule.

## Alternatives Considered

- **Kotlin.** Rejected — no WPILib-first-class support; adds learning
  burden beyond Java.
- **C++.** Rejected — steep learning curve for students who are
  writing their first production code.
- **Python.** Rejected — RobotPy exists but has a smaller ecosystem for
  Phoenix 6 features and runs slower in tight control loops.

## References

- [Team271-Software-Coding-Standard.md §2](../../Team271-Software-Coding-Standard.md)
- [SDP.md §4](../SDP.md)
- [SCMP.md §4](../SCMP.md)
