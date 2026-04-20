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

## Project Overview

Team 271's reusable FRC library. See [`README.md`](README.md) for the
project overview and quick start. Library docs live under
`docs/team-lib/` so a robot project consuming this library can keep
its own docs at `docs/<robot-name>/` without path collision.

## Architecture

See [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
for the planning-doc map and
[docs/team-lib/internal/team271-lib-dependency-diagram.mmd](docs/team-lib/internal/team271-lib-dependency-diagram.mmd)
for the six-layer graph. The layering decision is
[ADR-004](docs/team-lib/planning/adr/ADR-004-layered-architecture.md).

## Authoritative References

- **Coding standard:** [`docs/team-lib/Team271-Software-Coding-Standard.md`](docs/team-lib/Team271-Software-Coding-Standard.md)
  — normative rules (§1–§3 inline). §4 Coding Guidelines is split into
  eight companion files (General, Format, Modules, Methods, Variables,
  Control, Comments, Debug). Also: `-Safety`, `-Compliance`,
  `-Templates`, `-Appendices`. See the "Companion Documents" section
  of the core file for the full index.
- **Planning:** [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
  — SDP, SRS, SVP, SCMP, ADRs, SDDs.
- **Guides:** [docs/team-lib/guides/start-here.md](docs/team-lib/guides/start-here.md)
  is the first read for new contributors.
- **Library conventions:** [CONTRIBUTING.md](CONTRIBUTING.md).
- **AI guardrails:** `.claude/rules/` — path-scoped rules auto-loaded
  when working in each subdirectory.
- **Pre-merge enforcement:** `.claude/hooks/` — markdownlint, doc-tunable
  check, deleted-class refs, design-drift, Java compile, Spotless.
- **Language + toolchain:** Java 17 + GradleRIO. Details in
  [SCS §2](docs/team-lib/Team271-Software-Coding-Standard.md) and
  [SDP §4](docs/team-lib/planning/SDP.md). Decision:
  [ADR-002](docs/team-lib/planning/adr/ADR-002-java17-wpilib-gradlerio-toolchain.md).
- **Build system:** Gradle + GradleRIO. See `build.gradle`; version
  policy in [SCMP §3](docs/team-lib/planning/SCMP.md).
- **CI:** GitHub Actions live. `.github/workflows/ci.yml` gates PRs on
  Spotless, compile + Error Prone, test, Javadoc, Checkstyle, SpotBugs
  (fail-soft during rollout), JaCoCo coverage (with PR comment), build,
  markdownlint, yamllint, `verify-docs.sh`, and ShellCheck of
  `.claude/hooks/` — ubuntu-24.04 / JDK 17. Supporting workflows:
  `claude-code-review.yml` (advisory AI review),
  `dependency-submission.yml` (supply-chain graph on push to `main`),
  `vendordep-freshness.yml` (weekly upstream version check). Local
  pre-edit gates additionally run through `.claude/hooks/`.
  See [SVP §7](docs/team-lib/planning/SVP.md).
- **Platform support:** RoboRIO 2 + desktop sim on Windows/macOS/Linux.
  Matrix in [SDP §5](docs/team-lib/planning/SDP.md).
- **Vendor dependencies:** CTRE Phoenix 6, WPILib, AdvantageKit,
  PathPlanner. Process in
  [SCMP §4](docs/team-lib/planning/SCMP.md); vendor decision in
  [ADR-006](docs/team-lib/planning/adr/ADR-006-ctre-phoenix6-primary-vendor.md).
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
