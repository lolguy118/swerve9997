# CLAUDE.md
<!-- markdownlint-disable MD013 -->
> **AI maintenance rule:** This file is a routing index for AI/LLM
> context only. It must NOT contain design information — only
> references to authoritative documents.
>
> Do NOT duplicate content from:
>
> - `CONTRIBUTING.md` (root) — workflow, PRs, commits, linting
> - `docs/team-lib/Team271-Software-Coding-Standard*.md` — coding rules
> - `docs/team-lib/planning/` — SDP, SRS, SVP, SCMP, SDDs, ADRs
> - `docs/team-lib/guides/` — contributor tutorials
> - `.claude/rules/` — path-scoped AI guardrails
>
> If new information needs a home, add it to the appropriate
> authoritative document above, then reference it here.

This file provides routing context to Claude Code. Detailed rules
live in the documents it references.

## Project Overview

Team 271's reusable FRC library. See `README.md` for the project
overview. Library docs live under `docs/team-lib/` so a robot project
consuming this library can keep its own docs at `docs/<robot-name>/`
without path collision.

## Architecture

See [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
for the planning-doc map and
[docs/team-lib/internal/team271-lib-dependency-diagram.mmd](docs/team-lib/internal/team271-lib-dependency-diagram.mmd)
for the six-layer graph. The layering decision is
[ADR-004](docs/team-lib/planning/adr/ADR-004-layered-architecture.md).

## Authoritative References

- **Coding standard:**
  [`docs/team-lib/Team271-Software-Coding-Standard.md`](docs/team-lib/Team271-Software-Coding-Standard.md)
  — normative rules; companion docs for
  [Java](docs/team-lib/Team271-Software-Coding-Standard-Java.md),
  [safety](docs/team-lib/Team271-Software-Coding-Standard-Safety.md),
  [templates](docs/team-lib/Team271-Software-Coding-Standard-Templates.md),
  [appendices](docs/team-lib/Team271-Software-Coding-Standard-Appendices.md),
  [compliance](docs/team-lib/Team271-Software-Coding-Standard-Compliance.md).
- **Planning:** [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
  — SDP, SRS, SVP, SCMP, ADRs, SDDs.
- **Guides:** [docs/team-lib/guides/start-here.md](docs/team-lib/guides/start-here.md)
  is the first read for new contributors.
- **Library conventions:** [CONTRIBUTING.md](CONTRIBUTING.md).
- **AI guardrails:** `.claude/rules/` — path-scoped rules auto-loaded
  when working in each subdirectory.

## Language

See
[`docs/team-lib/Team271-Software-Coding-Standard.md` §2](docs/team-lib/Team271-Software-Coding-Standard.md)
and [`docs/team-lib/planning/SDP.md` §4](docs/team-lib/planning/SDP.md)
for the full environment specification.
Toolchain decision: [ADR-002](docs/team-lib/planning/adr/ADR-002-java17-wpilib-gradlerio-toolchain.md).

## Build System

Gradle + GradleRIO. See `build.gradle`; version policy in
[`docs/team-lib/planning/SCMP.md` §3](docs/team-lib/planning/SCMP.md).

## CI

GitHub Actions (planned). Current pre-merge gates: Gradle
`compileJava`, `spotlessCheck`, `test`, `jacocoTestReport`, plus
markdownlint. See [`docs/team-lib/planning/SVP.md` §7](docs/team-lib/planning/SVP.md).

## Platform Support

See [`docs/team-lib/planning/SDP.md` §5](docs/team-lib/planning/SDP.md)
for the RoboRIO + desktop-sim matrix.

## Vendor Dependencies

See [`docs/team-lib/planning/SCMP.md` §4](docs/team-lib/planning/SCMP.md)
and [ADR-006](docs/team-lib/planning/adr/ADR-006-ctre-phoenix6-primary-vendor.md).
`vendordeps/*.json` is authoritative.

## Reference URLs (External)

### CTRE Phoenix 6

- Docs: <https://v6.docs.ctr-electronics.com/en/stable/index.html>
- Java API: <https://api.ctr-electronics.com/phoenix6/stable/java/>
- Examples: <https://github.com/CrossTheRoadElec/Phoenix6-Examples/tree/main/java>
- Tuner X: <https://v6.docs.ctr-electronics.com/en/stable/docs/tuner/index.html>

### WPILib

- Docs: <https://docs.wpilib.org/en/stable/>
- Java API: <https://github.wpilib.org/allwpilib/docs/release/java/index.html>
- Source: <https://github.com/wpilibsuite/allwpilib>
- GradleRIO: <https://github.com/wpilibsuite/GradleRIO>

### Auto / Path Following

- PathPlanner: <https://pathplanner.dev/home.html>

### Logging & Telemetry

- AdvantageKit: <https://docs.advantagekit.org/>
- AdvantageScope: <https://docs.advantagescope.org/>
- Elastic: <https://frc-elastic.gitbook.io/docs>

### Vision

- Limelight: <https://limelightlib-wpijava-reference.limelightvision.io/>
- PhotonVision: <https://docs.photonvision.org/en/latest/>
- Luma P1: <https://docs.luma.vision/p1/>

### Testing & Tooling

- JUnit 5: <https://junit.org/junit5/docs/current/user-guide/>
- Google Java Format: <https://github.com/google/google-java-format>
- Spotless: <https://github.com/diffplug/spotless/tree/main/plugin-gradle>

## Claude Rules

@.claude/rules/coding-standard.md
@.claude/rules/docs.md
@.claude/rules/planning.md
@.claude/rules/team271-lib.md
@.claude/rules/safety.md
