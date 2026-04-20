# CLAUDE.md<!-- markdownlint-disable MD013 MD060 -->

> **AI maintenance rule:** This file is a routing index for
> AI/LLM context only. It must NOT contain design information
> — only references to authoritative documents.
>
> Do NOT duplicate content from:
>
> - `CONTRIBUTING.md` (root) — workflow, PRs, commits, linting
> - `docs/team-lib/reference/CONTRIBUTING.md` — library conventions,
>   naming, architecture, dependency graph
> - `docs/team-lib/quality/team271-java-coding-standard.md` — coding rules
>   and companion documents
> - `docs/planning/` — SDP, SRS, SVP, SCMP, SDDs, ADRs
> - `.claude/rules/` — path-scoped AI guardrails
>
> If new information needs a home, add it to the appropriate
> authoritative document above, then reference it here.

This file provides routing context to Claude Code. Detailed rules
live in the documents it references.

## Project Overview

Team 271's reusable FRC robot library. The codebase uses WPILib, CTRE Phoenix 6, PathPlanner, and AdvantageKit for logging.

## Reference Documentation

### CTRE Phoenix 6 (primary motor/sensor vendor)

- Phoenix 6 Docs: <https://v6.docs.ctr-electronics.com/en/stable/index.html>
- Phoenix 6 Java API: <https://api.ctr-electronics.com/phoenix6/stable/java/>
- Phoenix 6 Examples: <https://github.com/CrossTheRoadElec/Phoenix6-Examples/tree/main/java>
- Phoenix Tuner X: <https://v6.docs.ctr-electronics.com/en/stable/docs/tuner/index.html>

### WPILib

- WPILib Docs: <https://docs.wpilib.org/en/stable/>
- WPILib Java API: <https://github.wpilib.org/allwpilib/docs/release/java/index.html>
- WPILib Source: <https://github.com/wpilibsuite/allwpilib>
- GradleRIO: <https://github.com/wpilibsuite/GradleRIO>

### Auto / Path Following

- PathPlanner Docs: <https://pathplanner.dev/home.html>
- PathPlanner Source: <https://github.com/mjansen4857/pathplanner>

### Logging & Telemetry

- AdvantageKit Docs: <https://docs.advantagekit.org/>
- AdvantageKit Source: <https://github.com/Mechanical-Advantage/AdvantageKit>
- AdvantageScope Docs: <https://docs.advantagescope.org/>
- AdvantageScope Source: <https://github.com/Mechanical-Advantage/AdvantageScope>
- Elastic Dashboard Docs: <https://frc-elastic.gitbook.io/docs>
- Elastic Dashboard Source: <https://github.com/Gold872/elastic_dashboard>

### Vision

- Limelight Java API: <https://limelightlib-wpijava-reference.limelightvision.io/>
- Limelight Source: <https://github.com/LimelightVision/limelightlib-wpijava>
- PhotonVision Docs: <https://docs.photonvision.org/en/latest/>
- PhotonVision Javadocs: <https://javadocs.photonvision.org/>
- PhotonVision Source: <https://github.com/PhotonVision/photonvision/>
- Luma P1 Docs: <https://docs.luma.vision/p1/>

### Testing & Tooling

- JUnit 5 User Guide: <https://junit.org/junit5/docs/current/user-guide/>
- Google Java Format: <https://github.com/google/google-java-format>
- Spotless Gradle: <https://github.com/diffplug/spotless/tree/main/plugin-gradle>

## Architecture

## Authoritative References

- [Start Here](docs/team-lib/guides/start-here.md) — new contributor orientation, mental models, and guided reading order
- [Java Coding Standard](docs/team-lib/quality/team271-java-coding-standard.md) — formatting, safety rules, naming conventions, state machine patterns
- [Contributing Guide](CONTRIBUTING.md) — pre-commit checks, build verification, commit conventions
- [Library Architecture](docs/team-lib/architecture/library-architecture.md) — 6-layer architecture, TObj hierarchy, subsystem lifecycle, hardware abstraction stack
- [Vendor Abstraction Guide](docs/team-lib/architecture/vendor-abstraction-guide.md) — vendor-neutral interfaces, CTREMotor passthrough, HardwareManager, CommandBridge
- [Auto Design](docs/team-lib/control/auto-design.md) — move composition, AutoMode lifecycle, sequencing patterns
- [Hardware Abstraction](docs/team-lib/architecture/hardware-abstraction.md) — controllers, transmissions, sensors, input system
- [Passthrough Design](docs/team-lib/architecture/passthrough-design.md) — wrapper-not-wall philosophy, underlying object access
- [Control System](docs/team-lib/control/control-system.md) — PID variants, selection criteria, Balance algorithm
- [Testing Strategy](docs/team-lib/quality/testing-strategy.md) — HAL initialization, test isolation, coverage patterns
- [Fault Tolerance](docs/team-lib/quality/fault-tolerance.md) — library-level fault patterns, timeout protection, recovery
- [Code Review Prompt](docs/prompts/code-review-prompt-teamlib.md) — architecture reference and review checklist
- [Development Setup](docs/team-lib/guides/development-setup.md) — getting started guide for new contributors
- [SysID Workflow](docs/team-lib/guides/sysid-workflow.md) — system identification and characterization
- [Vendor Dependencies](docs/team-lib/reference/vendor-dependencies.md) — vendordep management and upgrades
- [Geometry Package — Removed](docs/team-lib/reference/geometry-package.md) — decision record; use `edu.wpi.first.math.geometry`
- [Utility Package](docs/team-lib/reference/utility-package.md) — Alert, Elastic notifications, DriveSignal, math utilities
- [Input Shaping Guide](docs/team-lib/guides/input-shaping-guide.md) — joystick input curve selection and comparison
- [Simulation Guide](docs/team-lib/guides/simulation-guide.md) — robot project physics simulation implementation
- [Documentation Index](docs/team-lib/reference/documentation-index.md) — package-to-doc mapping and reading order

## Claude Rules

Short path-scoped guardrails loaded on every session. Each file
points back to the authoritative design doc.

@.claude/rules/hardware-abstraction.md
@.claude/rules/docs.md
@.claude/rules/safety.md
@.claude/rules/passthrough.md

## Language

See [`docs/team-lib/quality/team271-java-coding-standard.md` §2](docs/ERSK-Software-Coding-Standard.md#2-programming-language)

## Project-Specific Configuration

> **Everything below this line is specific to this project / season.**
> The sections above are generic to Team271-Lib and can be shared
> across robot projects. When copying this CLAUDE.md to a new robot
> project, keep everything above and replace everything below with
> your project-specific details.
