<!-- markdownlint-disable MD013 -->
# Verification Plan Framework

Framework-agnostic test strategy for Team 271 Java projects. A concrete
project (library or season robot) fills in the specifics — per-layer
coverage targets, specific test-ID ranges, project-specific hooks — in
its own SVP and cites this framework.

> **Coverage note:** The targets discussed below follow a
> development-quality discipline inspired by DO-178C structural
> coverage. They are **not** a DO-178C certification claim.

## 1. Test Levels

Every project's test matrix **shall** cover four levels:

### 1.1 Unit tests

JUnit 5 Jupiter + JaCoCo coverage, with WPILib HAL simulation
(`HAL.initialize()` in `@BeforeAll`). Every test class that creates
vendor devices **shall** clear shared static state in `@BeforeEach`.

Unit tests verify:

- Object construction and initialization
- Configuration APIs (getters return what setters stored)
- Validation logic (null checks, bounded inputs)
- State-machine transitions
- Pure math (PID, gear ratios, unit conversions)
- Telemetry key registration

Unit tests cannot verify:

- Real vendor signal values (sim returns defaults)
- Firmware closed-loop response
- Actual motor output to hardware

### 1.2 Integration tests

A project-local test harness that exercises a full robot lifecycle
(`robotInit` → pre-cycle → mode-specific periodic → post-cycle →
telemetry) across multiple subsystems. Integration tests verify
cross-layer behavior (e.g., exception isolation in a multi-subsystem
scenario).

Integration-harness code **should** be excluded from coverage metrics
(it is the test harness, not production code).

### 1.3 Simulation tests

With HAL initialized, vendor devices expose functional sim states.
Simulation tests verify:

- Sim initialization paths
- Sim-output propagation through wrapper layers
- Supply-voltage updates each cycle

Simulation tests do **not** verify physics accuracy (that depends on
product-specific WPILib sim classes such as `ElevatorSim`,
`SingleJointedArmSim`).

### 1.4 Static analysis

| Tool | What it checks |
| ---- | -------------- |
| Spotless + Google Java Format (AOSP) | Formatting, import order |
| `javac -Xlint:all` | Unchecked, deprecation, serial, fallthrough warnings |
| Error Prone | Compile-time bug patterns |
| SpotBugs | Bytecode analysis (null deref, concurrency) |
| Checkstyle | Mechanizable subset of the coding standard |
| markdownlint-cli2 | Markdown rules (line length, heading hierarchy) |
| yamllint | YAML formatting |

A project that does not use one of these **shall** state so in its SVP.

## 2. Coverage Target Framework

Coverage targets **shall** be declared per layer (or per module) so
that different reliability needs are reflected by different thresholds.
Starting targets (adjust per project):

| Concern | Statement | Branch | Function |
| ------- | --------- | ------ | -------- |
| Safety-critical (PID, motion) | ≥ 85% | ≥ 75% | 100% |
| API surface (public contract) | ≥ 80% | ≥ 70% | 100% |
| Hardware-dependent (wrappers) | ≥ 65% | ≥ 55% | 100% |
| Vendor adapters | ≥ 70% | ≥ 60% | 100% |
| Utilities and math | ≥ 80% | ≥ 70% | 100% |

"Function" coverage means every public API method has at least one
direct test that calls it. Hardware-dependent behavior (real motor
output, real sensor signals) is not counted — the test verifies the
call path does not throw.

Projects **shall** state explicitly whether the coverage thresholds
are **enforced gates** (CI fails if missed) or **aspirational
targets** (report-only). Enforcement requires a
`jacocoTestCoverageVerification` task with per-module rules.

## 3. Pre-Merge Hook Pattern

Projects **should** install a suite of `.claude/hooks/*.sh` scripts
that fire on Edit/Write tool operations and function as pre-merge
gates locally. Hooks **shall** be advisory (exit 0) by default and
blocking only when they guard a safety rule that must not regress.

Canonical hook categories:

| Category | Purpose |
| -------- | ------- |
| Markdown lint | markdownlint-cli2 on `.md` files |
| Doc tunable check | No numeric tunable values in docs |
| Deleted-symbol refs | No references to symbols listed as removed |
| Design drift | Nudge when a behavior-defining class edit lacks a doc update |
| Java compile | Fast compile check after Java edit |
| Spotless | Format check after Java edit |
| Docs sweep | Broken links, stale paths, empty sections |

## 4. CI Pipeline Gate Structure

Every project **shall** have GitHub Actions CI that runs on every push
to `main` and every pull request. The gate set **shall** include:

| Gate | Command / Action |
| ---- | ---------------- |
| Spotless | `./gradlew spotlessCheck` |
| Java compile | `./gradlew compileJava compileTestJava` |
| Tests | `./gradlew test` |
| Javadoc | `./gradlew javadoc` |
| Build | `./gradlew build -x test` |
| Checkstyle | `./gradlew checkstyleMain checkstyleTest` |
| SpotBugs | `./gradlew spotbugsMain spotbugsTest` |
| Coverage report | `./gradlew jacocoTestReport` |
| Markdown lint | markdownlint-cli2 over docs and root |
| YAML lint | yamllint over workflow files |
| Docs sweep | `bash .claude/hooks/verify-docs.sh` |
| Shell lint | ShellCheck on `.claude/hooks/*.sh` |

Additional advisory workflows (AI review, vendordep-freshness,
dependency submission) **may** run in parallel but **shall not** block
merge.

## 5. Traceability

Every project's SVP **should** define a traceability convention between
requirements (`REQ-*`) and tests (`TEST-*`). The convention may be as
simple as a Javadoc comment on each test method that cites the
requirement it verifies. Projects **shall** be honest about the gap
between this convention and the current test code: until adopted
retroactively, a new convention applies prospectively to new or
refactored tests.
