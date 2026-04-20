# Software Verification Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SVP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

> **Coverage note:** Coverage thresholds below are development-quality
> targets inspired by DO-178C structural coverage disciplines. They are
> **not** a DO-178C certification claim.

## 1. Purpose and Scope

This document defines how Team271-Lib requirements (captured in
[SRS.md](SRS.md)) are verified. It specifies the test framework, the
levels of testing performed, coverage thresholds by layer, CI pipeline
gates, and the role of the `.claude/hooks/` scripts as pre-merge
verification.

It does not cover robot-project testing — robot projects follow the
same patterns but add physics-model tests specific to each season's
mechanisms.

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [SRS.md](SRS.md) | Requirements being verified |
| [SDP.md](SDP.md) | Development phases and toolchain |
| [Team271-Software-Coding-Standard.md](../Team271-Software-Coding-Standard.md) | Coding rules |
| [ADR-009](adr/ADR-009-junit5-hal-simulation-tests.md) | Test framework decision |

## 3. Test Levels

### 3.1 Unit Tests

JUnit 5 Jupiter + JaCoCo coverage, with WPILib HAL simulation
(`HAL.initialize()` in `@BeforeAll`). Every test class that creates
CTRE devices calls `CTREManager.resetForTesting()` in `@BeforeEach` to
clear static state between tests.

Unit tests verify:

- Object construction and initialization
- Configuration APIs (getters return what setters stored)
- Validation logic (null checks, bounded inputs, bus validation)
- State-machine transitions
- PID math (`PIDSimple`, `PIDTrap`)
- Gear-ratio math
- Telemetry key registration

Unit tests cannot verify:

- Real CAN signal values (StatusSignals return defaults in sim)
- Firmware closed-loop response
- Real sensor readings or latency compensation
- Actual motor output voltage to hardware

### 3.2 Integration Tests

The `libtest/` package contains `Infrastructure` and `Superstructure`
harnesses that exercise a full robot lifecycle — `robotInit` →
`robotPeriodicBefore` → `robotPeriodicAfter` → `outputTelemetry` —
across multiple subsystems. These verify cross-layer behavior
(e.g., `SubsystemManager.forEachSafe()` actually isolates exceptions
in a multi-subsystem scenario).

`libtest/` is excluded from coverage metrics because it is the test
harness, not library code.

### 3.3 Simulation Tests

With HAL initialized, CTRE devices expose functional `SimState`
objects. Simulation tests verify:

- `simulationInit()` creates SimState instances
- `setSimPosRotations()` / `setSimVelRotations()` propagate without
  errors
- Supply voltage updates work under `simulationPeriodic()`
- Motor type and orientation are configured correctly
- `DCMotor` model is created with the correct motor count

Simulation tests do **not** verify physics accuracy (that depends on
robot-specific WPILib sim classes).

### 3.4 Static Analysis

| Tool | What It Checks | When |
| ---- | -------------- | ---- |
| Spotless + Google Java Format (AOSP) | Formatting, import order | `./gradlew spotlessCheck` |
| `javac -Xlint:all` | Unchecked, deprecation, serial, fallthrough warnings | `./gradlew compileJava` |
| markdownlint-cli2 | Markdown rules (140-char lines, heading hierarchy) | Hook + CI |

For the full list of `.claude/hooks/` pre-merge hooks (which are
tooling around the above plus doc-drift checks), see
[§6 Hooks as Pre-Merge Gates](#6-hooks-as-pre-merge-gates).

## 4. Coverage Requirements

> **Status:** The thresholds below are **targets**, not enforced
> gates. `./gradlew jacocoTestReport` generates the coverage report
> but does not fail on threshold violations. Enforcement would
> require adding a `jacocoTestCoverageVerification` task to
> `build.gradle` with per-module rules. Tracked as a follow-up.

Starting targets per layer (JaCoCo). The `libtest/` package is
excluded.

| Layer | Statement | Branch | Function |
| ----- | --------- | ------ | -------- |
| `api/` | ≥ 80% | ≥ 70% | 100% |
| `vendor/ctre/` | ≥ 70% | ≥ 60% | 100% |
| `hardware/` | ≥ 65% | ≥ 55% | 100% |
| `control/` | ≥ 85% | ≥ 75% | 100% |
| `subsystem/` | ≥ 75% | ≥ 65% | 100% |
| `auto/` | ≥ 80% | ≥ 70% | 100% |
| `sysid/` | ≥ 60% | ≥ 50% | 100% |
| `nt/` | ≥ 70% | ≥ 60% | 100% |
| `util/` | ≥ 80% | ≥ 70% | 100% |

Note: "Function" coverage means every public API method has at least
one direct test that calls it. Hardware-dependent behavior (real
motor output, real CAN signals) is not counted — the test verifies
the call path does not throw.

## 5. Per-Layer Test Requirements

| Layer | Test-ID Prefix | Notes |
| ----- | -------------- | ----- |
| `api/` | `TEST-API-NNN` | No HAL required (pure interfaces) |
| `vendor/ctre/` | `TEST-CTRE-NNN` | HAL init + CTREManager cleanup |
| `hardware/` | `TEST-HW-NNN` | HAL init + CTREManager cleanup; unique CAN IDs per test |
| `control/` | `TEST-CTL-NNN` | No HAL for pure PID variants; HAL for PIDFX |
| `subsystem/` | `TEST-SUB-NNN` | HAL if hardware-backed |
| `auto/` | `TEST-AUT-NNN` | HAL required |
| `sysid/` | `TEST-SID-NNN` | HAL required |
| `nt/` | `TEST-NT-NNN` | HAL required (NT4 init) |
| `util/` | `TEST-UTL-NNN` | No HAL except `LimelightHelpers` |

## 6. Hooks as Pre-Merge Gates

The `.claude/hooks/` scripts fire on Edit/Write tool operations (most
advisory, some blocking) and function as pre-merge gates. They can
also be run locally.

| Hook | What It Enforces | Local Run |
| ---- | ---------------- | --------- |
| `lint-markdown.sh` | markdownlint-cli2 rules on `.md` files | `markdownlint-cli2 docs/` |
| `check-doc-tunables.sh` | No numeric tunables in `docs/**` | (runs on Edit/Write) |
| `check-deleted-class-refs.sh` | No references to deprecated symbols | (runs on Edit/Write) |
| `check-design-drift.sh` | Code changes paired with doc updates | (runs on Edit/Write) |
| `check-java-compiles.sh` | Java compiles after each edit | `./gradlew compileJava` |
| `check-spotless.sh` | Spotless format check after Java edit (advisory) | `./gradlew spotlessCheck` |
| `verify-docs.sh` | Full docs sweep: broken links, stale paths, unresolved placeholders, empty SDD sections, markdownlint | `bash .claude/hooks/verify-docs.sh` |

## 7. CI Pipeline Gates

GitHub Actions CI runs on every push to `main` and every pull request
(ubuntu-24.04, JDK 17 Temurin). The authoritative workflow is
[`.github/workflows/ci.yml`](../../../.github/workflows/ci.yml);
the gates below mirror it.

| Gate | Command / Action | Workflow job |
| ---- | ---------------- | ------------ |
| Spotless | `./gradlew spotlessCheck` | `build` |
| Java compile | `./gradlew compileJava compileTestJava` | `build` |
| Error Prone | runs inline with `compileJava` (warnings only during rollout — see `build.gradle`) | `build` |
| Tests | `./gradlew test` | `build` |
| Javadoc | `./gradlew javadoc` | `build` |
| Build (non-test) | `./gradlew build -x test` | `build` |
| Checkstyle | `./gradlew checkstyleMain checkstyleTest` (invoked by `build`); config at `config/checkstyle/checkstyle.xml` | `build` |
| SpotBugs | `./gradlew spotbugsMain spotbugsTest` (invoked by `build`); `ignoreFailures=true` during rollout | `build` |
| Coverage report | `./gradlew jacocoTestReport` (thresholds per §4 are targets, not gates) | `build` |
| Coverage PR comment | `madrapps/jacoco-report@v1.7.1` (PRs only) | `build` |
| Markdown lint | `markdownlint-cli2 "**/*.md" "#build" "#.gradle"` | `lint-docs` |
| YAML lint | `yamllint .` | `lint-docs` |
| Docs sweep | `bash .claude/hooks/verify-docs.sh` (broken links, stale paths, empty SDD sections) | `lint-docs` |
| ShellCheck | `ludeeus/action-shellcheck` on `.claude/hooks/*.sh` | `shellcheck` |

Supporting workflows:

- [`.github/workflows/claude-code-review.yml`](../../../.github/workflows/claude-code-review.yml)
  — `anthropics/claude-code-action` runs an AI-assisted review pass on
  every PR. Advisory only, does not block merge.
- [`.github/workflows/dependency-submission.yml`](../../../.github/workflows/dependency-submission.yml)
  — `gradle/actions/dependency-submission` on every push to `main`.
  Populates GitHub's Dependency graph so vendordeps show up in
  Dependabot alerts.
- [`.github/workflows/vendordep-freshness.yml`](../../../.github/workflows/vendordep-freshness.yml)
  — weekly cron (Mondays 13:00 UTC) that fetches each vendordep
  `jsonUrl`, compares `version`, and opens/updates a tracking issue
  when any vendordep is behind upstream. See [SCMP §4](SCMP.md).

Hooks under `.claude/hooks/` run locally per edit; they catch a subset
of the same issues before CI sees them. See §6.

## 8. Traceability

The SRS §7 matrix lists every requirement with its SDD section and
an **expected** `TEST-*-NNN` identifier. The convention below is
aspirational — existing tests (1,389 `@Test` methods across 52 files
as of 2026-04-20) predate the convention and do not yet carry these
tags.

**Going forward:** new `[TEST-*]` test methods and significantly
refactored existing tests shall cite their `[REQ-*]` requirement in a
Javadoc comment above the method:

```java
/**
 * [TEST-CTRE-001] Verifies CTRE-001: CTREMotor implements ClosedLoopMotor.
 */
@Test
void testCTREMotorImplementsClosedLoopMotor() { ... }
```

PRs that add or modify a requirement must update the SRS §7 matrix in
the same commit. PRs that add tests matching an existing matrix row
should add the Javadoc citation.
