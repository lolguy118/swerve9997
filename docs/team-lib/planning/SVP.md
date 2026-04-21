# Software Verification Plan

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SVP |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

This document is Team271-Lib's specific verification plan. It builds
on the shared planning framework (see §2 Applicable Documents) and
records the library's concrete coverage targets, per-layer test
conventions, hook roster, and CI workflow details.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

> **Coverage note:** Coverage thresholds below are development-quality
> targets inspired by DO-178C structural coverage disciplines. They are
> **not** a DO-178C certification claim.

## 1. Purpose and Scope

How Team271-Lib requirements (captured in [SRS.md](SRS.md)) are
verified. Defines library-specific coverage targets, per-layer
test-ID conventions, the hook roster, and the concrete CI gate
workflow. Does not duplicate the shared framework.

Robot projects follow the same patterns but add physics-model tests
specific to each season's mechanisms.

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [`../../common/planning/verification-plan.md`](../../common/planning/verification-plan.md) | Shared test framework, coverage structure, CI gate pattern |
| [SRS.md](SRS.md) | Requirements being verified |
| [SDP.md](SDP.md) | Development phases and toolchain |
| [`../../common/coding-standard/Team271-Software-Coding-Standard.md`](../../common/coding-standard/Team271-Software-Coding-Standard.md) | Coding rules |
| [ADR-009](adr/ADR-009-junit5-hal-simulation-tests.md) | Test framework decision |

## 3. Test Levels (library-specific notes)

Team271-Lib follows the shared four-level test structure.
Library-specific notes:

- **Unit tests:** Every test class that creates CTRE devices calls
  `CTREManager.resetForTesting()` in `@BeforeEach` to clear static
  state between tests. Unit tests cover `PIDSimple`, `PIDTrap`,
  gear-ratio math, and telemetry key registration.
- **Integration tests:** The `libtest/` package contains
  `Infrastructure` and `Superstructure` harnesses that exercise the
  full robot lifecycle across multiple subsystems (verifying
  `SubsystemManager.forEachSafe()` isolation, for example).
  `libtest/` is excluded from coverage metrics because it is the test
  harness.
- **Simulation tests:** With HAL initialized, CTRE devices expose
  functional `SimState` objects. Tests verify `simulationInit()` /
  `setSimPosRotations()` / `setSimVelRotations()` paths and `DCMotor`
  model construction.

## 4. Coverage Targets (library-specific numbers)

> **Status:** The thresholds below are **targets**, not enforced
> gates. `./gradlew jacocoTestReport` generates the coverage report
> but does not fail on threshold violations. Enforcement would
> require adding a `jacocoTestCoverageVerification` task to
> `build.gradle` with per-module rules. Tracked as a follow-up.

Team271-Lib tiers coverage by architectural layer rather than by
concern (the framework permits either). Starting targets per layer
(JaCoCo). The `libtest/` package is excluded.

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

"Function" coverage means every public API method has at least one
direct test that calls it. Hardware-dependent behavior (real motor
output, real CAN signals) is not counted — the test verifies the call
path does not throw.

## 5. Per-Layer Test Requirements

| Layer | Test-ID Prefix | Notes |
| ----- | -------------- | ----- |
| `api/` | `[TEST-API-NNN]` | No HAL required (pure interfaces) |
| `vendor/ctre/` | `[TEST-CTRE-NNN]` | HAL init + CTREManager cleanup |
| `hardware/` | `[TEST-HW-NNN]` | HAL init + CTREManager cleanup; unique CAN IDs per test |
| `control/` | `[TEST-CTL-NNN]` | No HAL for pure PID variants; HAL for PIDFX |
| `subsystem/` | `[TEST-SUB-NNN]` | HAL if hardware-backed |
| `auto/` | `[TEST-AUT-NNN]` | HAL required |
| `sysid/` | `[TEST-SID-NNN]` | HAL required |
| `nt/` | `[TEST-NT-NNN]` | HAL required (NT4 init) |
| `util/` | `[TEST-UTL-NNN]` | No HAL except `LimelightHelpers` |

## 6. Hooks as Pre-Merge Gates (library roster)

Team271-Lib installs the following hooks under `.claude/hooks/`.
All are wired to `PostToolUse` in `.claude/settings.json`.

| Hook | What It Enforces | Local Run |
| ---- | ---------------- | --------- |
| `lint-markdown.sh` | markdownlint-cli2 rules on `.md` files | `markdownlint-cli2 docs/` |
| `lint-yaml.sh` | yamllint on `*.yml` / `*.yaml` files | `yamllint <file>` |
| `lint-shell.sh` | ShellCheck (severity=warning) on `*.sh` / `*.bash` files | `shellcheck --severity=warning <file>` |
| `check-doc-tunables.sh` | No numeric tunables in `docs/**` | (runs on Edit/Write) |
| `check-deleted-class-refs.sh` | No references to deprecated symbols | (runs on Edit/Write) |
| `check-design-drift.sh` | Code changes paired with doc updates | (runs on Edit/Write) |
| `check-java-compiles.sh` | Java compiles after each edit | `./gradlew compileJava` |
| `check-spotless.sh` | Spotless format check after Java edit (advisory) | `./gradlew spotlessCheck` |
| `check-checkstyle.sh` | Checkstyle violations after Java edit (advisory) | `./gradlew checkstyleMain` |
| `check-spotbugs.sh` | SpotBugs findings after Java edit (advisory; fail-soft during rollout) | `./gradlew spotbugsMain` |
| `check-javadoc.sh` | Javadoc doclint issues after Java edit (advisory) | `./gradlew javadoc` |
| `check-jacoco.sh` | Coverage report after Java edit — opt-in via `TEAM271_RUN_JACOCO_HOOK=1` because a full test run per edit is expensive | `./gradlew jacocoTestReport` |
| `verify-docs-hook.sh` | Wrapper that invokes `verify-docs.sh` when a doc is edited (advisory; CI is the authoritative gate) | `bash .claude/hooks/verify-docs.sh` |
| `verify-docs.sh` | Full docs sweep: broken links, stale paths, unresolved placeholders, empty SDD sections, markdownlint | `bash .claude/hooks/verify-docs.sh` |

## 7. CI Pipeline Gates (library-specific)

GitHub Actions CI runs on every push to `main` and every pull
request (ubuntu-24.04, JDK 17 Temurin). The authoritative workflow
is [`.github/workflows/ci.yml`](../../../.github/workflows/ci.yml);
the framework's canonical gate set is fully implemented. Only
library-specific additions and configuration are listed here:

| Gate | Library-specific detail | Workflow job |
| ---- | ----------------------- | ------------ |
| Error Prone | Runs inline with `compileJava` (warnings only during rollout — see `build.gradle`) | `build` |
| Coverage PR comment | `madrapps/jacoco-report@v1.7.1` posts the JaCoCo summary to PRs | `build` |
| Checkstyle | Config at `config/checkstyle/checkstyle.xml`; invoked by `build` | `build` |
| SpotBugs | `ignoreFailures=true` during rollout | `build` |
| Coverage report | Thresholds per §4 are targets, not enforced gates | `build` |
| Markdown lint | Glob `"**/*.md" "#build" "#.gradle"` | `lint-docs` |
| ShellCheck | `ludeeus/action-shellcheck` on `.claude/hooks/*.sh` | `shellcheck` |

Workflow jobs: `build`, `lint-docs`, `shellcheck`. See ci.yml for
exact job sequencing. Design-drift hooks (tunables, deleted refs,
doc-code pairing) run locally only; see §6.

Supporting workflows:

- [`.github/workflows/claude-code-review.yml`](../../../.github/workflows/claude-code-review.yml)
  — AI-assisted review pass on every PR. Advisory only.
- [`.github/workflows/dependency-submission.yml`](../../../.github/workflows/dependency-submission.yml)
  — dependency graph submission on every push to `main`.
- [`.github/workflows/vendordep-freshness.yml`](../../../.github/workflows/vendordep-freshness.yml)
  — weekly cron that compares each vendordep against upstream and
  opens a tracking issue when behind. See
  [SCMP §4](SCMP.md#4-vendordep-management-team271-lib-specifics).

### 7.1 Static-Analysis Adoption Status

The framework's canonical static-analysis list requires every
project to declare per-tool adoption. Current library state:

| Tool | Status | Notes |
| ---- | ------ | ----- |
| Spotless + Google Java Format | Adopted | `./gradlew spotlessCheck` |
| `javac -Xlint:all` | Adopted | `build.gradle` compile flags |
| `javac -Xdoclint:all,-missing` | Adopted (implicit) | Runs as part of `./gradlew javadoc`; consider making explicit in `build.gradle` |
| Error Prone | Adopted | Inline during `compileJava`; warnings-only during rollout |
| NullAway (Error Prone plugin) | Planned | Requires the null-safety annotation ADR first (see planning README Planned ADRs) |
| SpotBugs | Adopted | `ignoreFailures=true` during rollout |
| Checkstyle | Adopted | `config/checkstyle/checkstyle.xml` |
| markdownlint-cli2 | Adopted | CI job `lint-docs` |
| yamllint | Adopted | CI job `lint-docs` |
| ShellCheck | Adopted | CI job `shellcheck` |
| OWASP Dependency-Check / GitHub dependency review | Planned | GitHub dependency submission is wired; OWASP Gradle plugin not yet adopted. Supply-chain ADR pending (see planning README Planned ADRs) |

## 8. Traceability

The SRS §7 matrix is the library's traceability source. As of
2026-04-20, existing tests (1,389 `@Test` methods across 52 files)
predate the `[TEST-*]` / `[REQ-*]` convention and do not yet carry
the Javadoc citations.

Going forward — for new tests and significantly refactored existing
tests — the Javadoc citation format is:

```java
/**
 * [TEST-CTRE-001] Verifies [CTRE-001]: CTREMotor implements ClosedLoopMotor.
 */
@Test
void testCTREMotorImplementsClosedLoopMotor() { ... }
```

PRs that add or modify a requirement **shall** update the SRS §7
matrix in the same commit.
