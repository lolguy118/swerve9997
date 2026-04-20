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
| Spotless + Google Java Format | Formatting, import order | `./gradlew spotlessCheck` |
| `javac -Xlint` | Unchecked, deprecation, serial warnings | `./gradlew compileJava` |
| markdownlint-cli2 | Markdown rules (140-char lines, heading hierarchy) | Hook + CI |
| `.claude/hooks/check-doc-tunables.sh` | No numeric tunables in docs | Hook |
| `.claude/hooks/check-deleted-class-refs.sh` | No references to deprecated classes | Hook |
| `.claude/hooks/check-design-drift.sh` | Code changes paired with doc updates | Hook |

## 4. Coverage Requirements

JaCoCo thresholds per layer. Starting targets; rise each season. The
`libtest/` package is excluded.

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

The `.claude/hooks/` scripts fire on every Edit/Write operation and
function as pre-merge gates. They can also be run locally.

| Hook | What It Enforces | Local Run |
| ---- | ---------------- | --------- |
| `lint-markdown.sh` | markdownlint-cli2 rules on `.md` files | `markdownlint-cli2 docs/` |
| `check-doc-tunables.sh` | No numeric tunables in `docs/**` | (runs on Edit/Write) |
| `check-deleted-class-refs.sh` | No references to deprecated symbols | (runs on Edit/Write) |
| `check-design-drift.sh` | Code changes paired with doc updates | (runs on Edit/Write) |
| `check-java-compiles.sh` | Java compiles after each edit | `./gradlew compileJava` |

## 7. CI Pipeline Gates

All gates must pass before a PR can merge. CI runs these in order;
failure of any gate blocks merge.

| Gate | Command | Required |
| ---- | ------- | -------- |
| Java compile | `./gradlew compileJava compileTestJava` | Yes |
| Spotless | `./gradlew spotlessCheck` | Yes |
| Tests | `./gradlew test` | Yes |
| Coverage | `./gradlew jacocoTestReport` (enforced thresholds per §4) | Yes |
| Markdown lint | `markdownlint-cli2 docs/ CLAUDE.md` | Yes |

## 8. Traceability

Every `[TEST-*]` test method cites its `[REQ-*]` requirement in a
Javadoc comment at the test method:

```java
/**
 * [TEST-CTRE-001] Verifies CTRE-001: CTREMotor implements ClosedLoopMotor.
 */
@Test
void testCTREMotorImplementsClosedLoopMotor() { ... }
```

The SRS §7 traceability matrix lists every requirement with its
SDD section and test case. PRs that add or modify requirements
must update the matrix.
