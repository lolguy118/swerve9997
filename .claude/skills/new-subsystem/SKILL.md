---
name: new-subsystem
description: Walk a contributor through creating a new Team271-Lib Subsystem that follows every library invariant — desired-to-actual state pattern (ADR-010), lifecycle hooks, homing with mandatory timeout + fail-safe + driver alert (ADR-012), telemetry, and unit-test scaffolding. Use ONLY when working inside a downstream robot project that depends on Team271-Lib, not when editing Team271-Lib itself. The skill writes subsystem code under `<RobotProject>/src/main/java/frc/robot/subsystems/`, a path that only exists in robot repos.
disable-model-invocation: true
argument-hint: "<SubsystemName> (PascalCase, e.g. Arm, Elevator, Shooter)"
---

# New Subsystem

Scaffold a new robot-project subsystem following all Team271-Lib
invariants.

**Arguments:** `$ARGUMENTS` — PascalCase class name (e.g. `Arm`,
`Elevator`, `Shooter`). The subsystem file becomes
`<RobotProject>/src/main/java/frc/robot/subsystems/$ARGUMENTS.java`
(the exact path is robot-project-specific; confirm with the user
before writing).

## Instructions for Claude

### 1. Confirm intent with the user before writing anything

Ask which robot project the subsystem belongs to, confirm the target
directory, and get a one-sentence purpose. Example prompt:

> "You want a new `$ARGUMENTS` subsystem. Confirm:
>
> - Target file path (robot project root)?
> - One-sentence purpose (what does $ARGUMENTS do)?
> - Does it need homing? (i.e., does a state need to zero against a
>   limit switch before closed-loop operation?)"

### 2. Read the library invariants

Before writing code, cross-reference these design docs so the
scaffold is compliant:

- [`docs/team-lib/planning/sdd/SDD-subsystem.md`](../../../docs/team-lib/planning/sdd/SDD-subsystem.md)
  for the base class contract, lifecycle order, and sensor-mode
  enum.
- [`docs/team-lib/planning/adr/ADR-010-desired-to-actual-state-pattern.md`](../../../docs/team-lib/planning/adr/ADR-010-desired-to-actual-state-pattern.md)
  for desired-vs-actual separation.
- [`docs/team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md`](../../../docs/team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md)
  for homing-timeout requirements.
- [`docs/robot-yyyy/subsystem-template.md`](../../../docs/robot-yyyy/subsystem-template.md)
  for the authoritative robot-project subsystem template.

### 3. Scaffold structure

Create a subsystem class that extends `com.team271.lib.subsystem.Subsystem`
and includes all of the following, in this order:

1. **Desired-state and actual-state enums.** Separate. Enum for
   desired (operator intent) and enum for actual (sensor-derived).
   Per ADR-010, they are different types so callers can't confuse
   them.

2. **`Constants` inner class** with named constants for:
    - CAN device IDs used by this subsystem
    - Current limits (stator + supply)
    - PID gains (or leave as tunables via `LoggedNTInput`)
    - Homing timeout seconds (`kHomingTimeoutSec`) — required if
      the subsystem homes (per ADR-012)
    - Setpoint-tolerance thresholds

3. **Lifecycle method overrides**, in order:
    - `robotInit()` — construct vendor objects, register signals
      with `CTREManager`, apply `TransmissionBase.applyConfigs()`,
      register `LoggedNTInput` tunables, instantiate homing
      `Alert` instances.
    - `robotPeriodicBefore(double t)` — read sensors, update
      `actualState` enum.
    - `robotPeriodicAfter(double t)` — switch on `desiredState`,
      issue motor commands via
      `transmission.setOutputPosition(...)` or equivalent. **Do
      not apply operator input directly to hardware** (ADR-010).
    - `outputTelemetry()` — publish current/desired/actual state
      via `NTEntry`, call `checkTuning()` from `Subsystem`.

4. **Homing sequence** (only if the subsystem homes):
    - `private boolean isZeroed = false;` field.
    - `private final Alert homingTimeoutAlert = new Alert("Homing", "$ARGUMENTS homing timed out", Alert.AlertType.ERROR);`
    - A homing state in the desired-state enum.
    - Inside `robotPeriodicAfter`, when in the homing state:
        - Check `elapsedTimer.get() > Constants.kHomingTimeoutSec`.
        - On timeout: stop motors, restore default current limits,
          transition `desiredState` to `IDLE`, call
          `homingTimeoutAlert.set(true)`, log to DriverStation.

5. **State-machine exhaustiveness.** The `switch` on
   `desiredState` must have every enum constant handled **and**
   a `default` branch (per coding-standard §CODE-CTL).

### 4. Create a matching unit test

Create `<RobotProject>/src/test/java/frc/robot/subsystems/<Arguments>Test.java`
with at minimum:

- `@BeforeAll` calling `HAL.initialize(500, 0)`.
- `@BeforeEach` calling `CTREManager.resetForTesting()` and unique
  CAN IDs per test class.
- Tests for:
  - Construction with valid/invalid constants (null check).
  - Desired-state setter stores the value; doesn't immediately
    actuate.
  - State-machine transitions (e.g. `IDLE` → `HOMING` →
    `READY`).
  - Homing timeout fires the Alert and resets to `IDLE`.

### 5. Update docs

Remind the user:

- This is a robot-project subsystem. Document it in the robot
  project's SDDs (not in `docs/team-lib/planning/sdd/`, which is
  library-only).
- If a new pattern emerges that's library-worthy, propose an ADR
  via `/new-adr` first.

### 6. Invoke verification

After scaffolding, suggest to the user:

```bash
./gradlew compileJava
./gradlew test --tests "*$ArgumentsTest*"
./gradlew spotlessApply
```

Do **not** commit. Leave staging to the user.
