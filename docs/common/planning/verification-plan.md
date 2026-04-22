# Verification Plan Framework

Framework-agnostic test strategy for Team 271 Java projects. A concrete
project (library or season robot) fills in the specifics — per-layer
coverage targets, specific test-ID ranges, project-specific hooks — in
its own SVP and cites this framework.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in [`README.md`](README.md#normative-keywords).

> **Coverage note:** The tiers discussed below follow a
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
| `javac -Xdoclint:all,-missing` | Javadoc validity on public APIs — broken `@link`, invalid HTML, malformed tags |
| Error Prone | Compile-time bug patterns |
| NullAway (Error Prone plugin) | Null-safety enforcement on annotated packages |
| SpotBugs | Bytecode analysis (null deref, concurrency) |
| Checkstyle | Mechanizable subset of the coding standard |
| markdownlint-cli2 | Markdown rules (line length, heading hierarchy) |
| yamllint | YAML formatting |
| ShellCheck | Shell-script static analysis (e.g., `.claude/hooks/*.sh`) |
| OWASP Dependency-Check / GitHub dependency review | Known CVEs in declared and transitive dependencies |

A project that does not use one of these **shall** state so in its SVP.

## 2. Coverage Target Framework

Coverage targets **shall** be declared per layer (or per module) so
that different reliability needs are reflected by different thresholds.
Each project's SVP **shall** define concrete percentages for each tier
below; the framework names the tiers, not the numbers.

| Tier | Typical scope |
| ---- | ------------- |
| Safety-critical | PID, motion profiles, fail-safe timers — anything whose regression risks hardware or driver safety |
| API surface | Public contract of a library or service layer |
| Hardware-dependent | Wrappers around vendor devices and HAL — tested against sim, not real hardware |
| Vendor adapters | Thin translation layer between an `api/` interface and a specific vendor SDK |
| Utilities and math | Pure computation — unit conversions, filters, geometry |

Each tier's SVP entry **shall** declare a statement-coverage
percentage, a branch-coverage percentage, and a function-coverage
requirement. "Function" coverage means every public API method has at
least one direct test that calls it. Hardware-dependent behavior
(real motor output, real sensor signals) is not counted — the test
verifies the call path does not throw.

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

Every project's SVP **should** define a traceability convention
between requirements and tests. Requirement and test identifiers
**shall** use the bracketed form `[PREFIX-NNN]` — for example
`[REQ-042]`, `[TEST-137]`. Each test method **should** cite the
requirement it verifies in its Javadoc.

Projects **shall** define where requirement IDs are permitted to
appear (typically: SRS requirement tables, SVP traceability matrix,
and scoped SDD sections) and **shall not** let IDs leak into prose or
code comments outside those scoped locations.

Projects **shall** be honest about the gap between this convention
and existing test code: until adopted retroactively, a new convention
applies prospectively to new or refactored tests.
