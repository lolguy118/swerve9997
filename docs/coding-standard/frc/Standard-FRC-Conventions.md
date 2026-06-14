<!-- markdownlint-disable MD007 MD032 -->
# Team271-Lib FRC Coding Standard — FRC-Specific Conventions

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

FRC-specific conventions companion to the FRC overlay's
[`Standard.md`](Standard.md). Contains the conventions that are
FRC-specific extensions of pure-Java rules — the rules
themselves are general (naming, units, file organisation), but
the *values* they fix (NetworkTables prefix, WPILib lifecycle
method names, FRC unit suffixes) are FRC-ecosystem facts that
do not belong in [`../java/`](../java/).

Each `CODE-FRC-*` rule below names the pure-Java rule it
extends so reviewers can navigate between layers.

---

## 4.11 FRC-Specific Conventions

<a id="code-frc-001"></a>

### CODE-FRC-001 -- NetworkTables Field Prefix (Source: Team271-Lib)

Extends the variable-naming rule in
[`../java/Standard-Variables.md`](../java/Standard-Variables.md).

a. Fields that wrap NetworkTables entries (`NtPublisher`,
   `NtTable`, project-defined `RobotObj` wrappers, raw
   `NetworkTableEntry`) **shall** use the `nt` prefix in
   place of the project's normal instance-field prefix:

   ```java
   private RobotObj ntRobot;
   private final NtPublisher ntMMCruiseVel;
   private final NetworkTableEntry ntDebugFlag;
   ```

   Rationale: NetworkTables-backed fields cross a process /
   driver-station boundary every cycle and behave differently
   from in-memory state — they can be observed and mutated by
   the dashboard, and their reads can lag the publish.
   Visually flagging them in code prevents subtle bugs where
   a developer mutates an `nt`-prefixed field expecting
   in-process semantics.

<a id="code-frc-002"></a>

### CODE-FRC-002 -- WPILib Lifecycle Method Names (Source: Team271-Lib)

Extends the method-naming rule in
[`../java/Standard-Methods.md`](../java/Standard-Methods.md).

a. Lifecycle methods **shall** match WPILib naming exactly:
   `robotInit`, `robotPeriodic`, `autonomousInit`,
   `autonomousPeriodic`, `teleopInit`, `teleopPeriodic`,
   `disabledInit`, `disabledPeriodic`, `testInit`,
   `testPeriodic`, `simulationInit`, `simulationPeriodic`.

b. Every override of a WPILib lifecycle method **shall** be
   annotated with `@Override`. This is a specific instance of
   the general `@Override` requirement in
   [`../java/Standard-General.md`](../java/Standard-General.md);
   the FRC-specific call-out exists because mis-spelled
   lifecycle methods (`teleopperiodic`, `teleopPeriodc`) are
   a common silent-failure mode — WPILib still compiles, the
   robot just never enters teleop logic.

   ```java
   /* CORRECT */
   @Override
   public void teleopPeriodic() { ... }

   /* WRONG: silent typo, never called */
   public void teleopPeriodc() { ... }
   ```

c. Subsystem-level lifecycle hooks defined by the project's
   own library (e.g., `robotPeriodicBefore`,
   `robotPeriodicAfter`, `outputTelemetry`) **shall** also
   carry `@Override` annotations when overridden in
   subclasses, for the same reason.

<a id="code-frc-003"></a>

### CODE-FRC-003 -- Unit Suffix Conventions (Source: Team271-Lib)

Extends the named-constant rule in
[`../java/Standard-Variables.md`](../java/Standard-Variables.md),
which mandates documenting physical units in constant names
or comments. This rule fixes the *specific* short-form
suffixes used across FRC code.

a. Constants representing physical quantities **shall** use
   one of the following suffixes:

   | Suffix | Unit |
   | ------ | ---- |
   | `_M`   | meters |
   | `_CM`  | centimeters |
   | `_IN`  | inches |
   | `_SEC` | seconds |
   | `_MS`  | milliseconds |
   | `_DEG` | degrees |
   | `_RAD` | radians |
   | `_RPS` | rotations per second |
   | `_RPM` | rotations per minute |
   | `_MPS` | meters per second |
   | `_V`   | volts |
   | `_A`   | amps |
   | `_HZ`  | hertz |
   | `_KG`  | kilograms |
   | `_N`   | newtons |

   ```java
   public static final double WHEEL_RADIUS_M = 0.0508;
   public static final double kHomingTimeoutSec = 2.0;
   public static final double MAX_SPEED_MPS = 4.5;
   ```

b. Constants whose values cross unit-system boundaries (for
   example, a target translated from inches to meters)
   **shall** carry the suffix of the unit they actually
   hold, not the unit they were specified in. Conversions
   **should** happen once at definition, never in per-cycle
   code.

<a id="code-frc-004"></a>

### CODE-FRC-004 -- Generated FRC Files (Source: Team271-Lib)

Extends the generated-code exemption in
[`../java/Standard-Modules.md`](../java/Standard-Modules.md).
This rule names the FRC-ecosystem generators whose output is
exempt from `CODE-*` rules.

a. The following generators' output **shall not** be
   manually edited:

   - The gversion plugin's build-stamp output (typically
     dropped into `frc.robot`).
   - The CTRE Tuner X swerve-config output.
   - Any vendor-maintained helper source imported unchanged
     from `vendordeps/**`.

   Concrete filenames vary by project and **shall** be
   listed in the consuming project's `.gitattributes`,
   Checkstyle / SpotBugs exclusion config, and
   `build.gradle` `excludedPaths` block. The placeholder
   `BuildConstants.java` in this standard's examples
   stands in for whichever filename the generator emits.

b. Generated files are exempt from all `CODE-*` rules
   *except* the `CODE-SAF-*` safety rules in
   [`Standard-Safety.md`](Standard-Safety.md). Safety
   concerns in generated content (current limits in a
   tuning output, CAN bus assignments) **shall** be human-
   reviewed even when the generator owns the file.

<a id="code-frc-005"></a>

### CODE-FRC-005 -- Subsystem Singleton Pattern (Source: Team271-Lib)

The generic mutable-static-field rule lives in
[`../java/Standard-General.md`](../java/Standard-General.md);
this rule defines the accepted singleton exception for
subsystem frameworks that manage one instance per mechanism.
The concrete parent/container type and any global-registry
class are project-library specifics — document them in the
project supplement.

a. Subsystems **shall** use a singleton pattern with two
   `getInstance()` methods: a creating overload taking the
   project library's parent/container type, and a fail-fast
   no-argument accessor:

   ```java
   private static MySubsystem mInstance;

   /* ParentObj stands in for the project library's parent type */
   public static MySubsystem getInstance(final ParentObj argParent) {
       if (mInstance == null) {
           mInstance = new MySubsystem(argParent);
       }
       return mInstance;
   }

   public static MySubsystem getInstance() {
       if (mInstance == null) {
           throw new IllegalStateException("MySubsystem not initialized");
       }
       return mInstance;
   }
   ```

b. The no-argument `getInstance()` **shall** throw
   `IllegalStateException` if the instance has not been
   created — never silently construct with missing context.

c. Subsystem singletons **shall** only be created during
   robot initialization and registered with the project's
   subsystem manager in the same place, so creation order is
   auditable in one method.

d. Projects **may** additionally store created references in
   a global registry class for cross-subsystem access. If
   they do, the registry **shall** be initialized once during
   robot initialization and documented as an accepted
   static-field exception in the project supplement.

<a id="code-frc-006"></a>

### CODE-FRC-006 -- Robot Lifecycle Contract (Source: Team271-Lib)

The lifecycle *method-name* conventions are
[CODE-FRC-002](#code-frc-002); this rule governs what each
phase of the per-cycle chain is allowed to do. Hook names
below follow the convention in CODE-FRC-002c — substitute the
project library's actual names.

a. Subsystem lifecycle hooks **shall** run in a fixed
   read-decide-apply-publish order each cycle:

   ```text
   robotInit(argTimestamp)
   -> robotPeriodicBefore(argTimestamp)    [read sensors]
   -> <mode>Periodic(argTimestamp)         [state machine logic]
   -> robotPeriodicAfter(argTimestamp)     [apply motor outputs]
   -> outputTelemetry()                    [publish to NT/logs]
   ```

b. Motor outputs **shall** only be commanded in the
   per-cycle "after" (apply) hook, never in `teleopPeriodic()`
   or `autonomousPeriodic()`. The mode-periodic methods set
   *desired* state; the apply hook *applies* it.

c. Sensor reading **shall** be done in the per-cycle
   "before" (read) hook, not in the mode-specific periodic
   methods.

d. Lifecycle timestamps **shall** come from WPILib's
   `Timer.getFPGATimestamp()` (FPGA-synchronized, monotonic),
   never `System.currentTimeMillis()` or `System.nanoTime()`.

<a id="code-frc-007"></a>

### CODE-FRC-007 -- State Machine Pattern (Source: Team271-Lib)

> *Industry note: DO-178C (the avionics software certification
> standard) emphasizes deterministic, traceable state
> management. The desired-state/actual-state pattern makes
> every transition explicit and auditable — you can always
> answer "what state is the robot in and why did it get
> there?" This is the same pattern used in flight control
> software.*

a. Subsystems that use state machines **shall** maintain two
   state variables: `mControlState` (current) and
   `mDesiredControlState` (desired). The desired state is set
   in `teleopPeriodic()` or `autonomousPeriodic()`; the actual
   state is applied in the per-cycle "after" hook
   ([CODE-FRC-006](#code-frc-006)):

   ```java
   /* Set in teleopPeriodic */
   mDesiredControlState = ExampleControlState.INDEX;

   /* Applied in the per-cycle "after" hook */
   mControlState = mDesiredControlState;
   switch (mControlState) {
       case INDEX:
           setValue(ExampleSubsystemConstants.EXAMPLE_SPEED);
           break;
       // ...
   }
   ```

b. State enums **shall** include all possible states
   including the safe/idle state (enum ordering convention:
   [`../java/Standard-Variables.md`](../java/Standard-Variables.md)).

c. All state transitions **shall** be explicit; no state
   shall be unreachable (completeness rule:
   [CODE-SAF-003](Standard-Safety.md#code-saf-003)).

---

## Reference Material

The sections below are *informational* — they support the
rules above with context, diagrams, and vendor-specific usage
patterns. They are not themselves numbered rules.

### FRC Robot Lifecycle Reference

```text
Power On -> robotInit()
         -> disabledInit() -> disabledPeriodic() [repeats]
         -> autonomousInit() -> autonomousPeriodic() [repeats for 15 sec]
         -> teleopInit() -> teleopPeriodic() [repeats for ~2:15]
         -> disabledInit() -> disabledPeriodic() [match ends]
```

Subsystem-level hooks (`robotPeriodicBefore`,
`robotPeriodicAfter`, `outputTelemetry`) fire inside the
WPILib lifecycle in an order fixed by the project's own
library — see the consuming project's CLAUDE.md for the
exact sequencing diagram.

### GC Pressure Minimization on the RoboRIO

The RoboRIO runs Java with a Serial GC (`-XX:+UseSerialGC`),
a stop-the-world collector chosen for its small memory
footprint on the RoboRIO's limited resources. SerialGC does
**not** support pause-time goals — `MaxGCPauseMillis` is a
G1GC / ZGC hint and has no effect here. See
[`../java/Standard-Compliance.md`](../java/Standard-Compliance.md)
for the JVM-config slot the consuming project fills in.

Because pause-time tuning is unavailable, GC pauses are
minimised by minimising allocations. The patterns below are
the FRC-specific application of the general per-cycle
allocation discipline in
[`../java/Standard-General.md`](../java/Standard-General.md).

**Pre-allocate at field declaration:**

```java
/* GOOD: allocated once */
private final Timer mReverseTimer = new Timer();

/* BAD: allocated every cycle */
Timer t = new Timer(); // in a periodic method
```

**Reuse CTRE control requests:**

```java
/* If you use the raw CTRE API, store requests as fields: */
private final VoltageOut mVoltageRequest = new VoltageOut(0);

/* In a per-cycle method: reuse the object. StatusCode is
 * intentionally discarded -- per-cycle control is best-effort
 * at 50 Hz, and transient failures self-correct on the next
 * cycle. See ../java/Standard-General.md for the discarded-
 * return-value discipline. */
mMotor.setControl(mVoltageRequest.withOutput(voltage));
```

**Avoid string concatenation in hot paths:**

```java
/* BAD: creates new String every cycle */
Logger.recordOutput("ExampleSubsystem/" + "Speed", speed);

/* GOOD: use a constant key */
private static final String LOG_KEY_SPEED = "ExampleSubsystem/Speed";
Logger.recordOutput(LOG_KEY_SPEED, speed);
```

**Avoid autoboxing in loops:**

```java
/* BAD: autoboxes int to Integer on every add */
List<Integer> values = new ArrayList<>();
for (int i = 0; i < count; i++) {
    values.add(i); // autoboxing
}

/* GOOD: primitive array */
int[] values = new int[count];
```

### CTRE Phoenix 6 Usage Patterns

#### Timesync

All CTRE control requests **shall** use timesync to
synchronise command execution with the CANivore clock:

```java
.withUseTimesync(true).withUpdateFreqHz(0)
```

`UpdateFreqHz(0)` means "send immediately, don't
auto-repeat." The subsystem code is responsible for sending
requests each cycle.

#### Bulk Signal Refresh

Use the project's bulk-refresh API (e.g.,
`hardwareMgr.refreshAll()`) at the start of each cycle to
refresh all registered CAN signals in a single bulk
operation. This is more efficient than refreshing signals
individually.

#### Control Request Reuse

Store control-request objects as fields and reuse them:

```java
private final VoltageOut mVoltageRequest =
    new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0);

/* In a per-cycle method: StatusCode is intentionally
 * discarded -- per-cycle control is best-effort. See
 * ../java/Standard-General.md for the discarded-return-value
 * discipline. */
mMotor.setControl(mVoltageRequest.withOutput(voltage));
```

#### Status Code Checking

Check `StatusCode` returns from configuration methods:

```java
StatusCode status = mMotor.getConfigurator().apply(config);
if (!status.isOK()) {
    DriverStation.reportError("Motor config failed: " + status, false);
}
```

<!-- markdownlint-enable MD007 MD032 -->
