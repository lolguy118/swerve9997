<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Appendix A: Approved Identifier Abbreviations

Short-form tokens permitted in code identifiers (variable, method, and
field names). This is an *allowlist for naming*, not a glossary — for
proper-name acronyms used in the standard's prose (ADR, CTRE, HAL,
WPILib, etc.), see
[§1.3 Terminology](Team271-Software-Coding-Standard.md#13-terminology-acronyms-used-in-this-document).

| Abbreviation | Meaning |
| ------------ | ------- |
| addr | address |
| arg | argument (method parameter prefix) |
| auto | autonomous |
| avg | average |
| buf | buffer |
| can | Controller Area Network |
| cfg | configuration |
| ch | channel |
| cmd | command |
| cnt | count |
| ctrl | control |
| def | default |
| deg | degree |
| dev | device |
| drv | driver |
| en | enable |
| err | error |
| ext | extension / extended |
| freq | frequency |
| hw | hardware |
| hz | hertz |
| idx | index |
| init | initialize |
| intf | interface |
| len | length |
| max | maximum |
| min | minimum |
| mod | module |
| mps | meters per second |
| msg | message |
| num | number |
| pid | proportional-integral-derivative |
| pos | position |
| pwr | power |
| rad | radian |
| req | request |
| rps | rotations per second |
| rx | receive |
| sec | second |
| sw | software |
| teleop | teleoperated |
| tmp | temporary |
| tx | transmit |
| val | value |
| vel | velocity |
| ver | version |

---

## Appendix B: Java Reserved Words

The following identifiers shall not be used as variable, method, or
class names:

`abstract`, `assert`, `boolean`, `break`, `byte`, `case`, `catch`,
`char`, `class`, `const`, `continue`, `default`, `do`, `double`,
`else`, `enum`, `extends`, `final`, `finally`, `float`, `for`,
`goto`, `if`, `implements`, `import`, `instanceof`, `int`,
`interface`, `long`, `native`, `new`, `package`, `private`,
`protected`, `public`, `return`, `short`, `static`,
`strictfp` (reserved but no-op since Java 17),
`super`, `switch`, `synchronized`, `this`, `throw`, `throws`,
`transient`, `try`, `void`, `volatile`, `while`

Also avoid: `var` (reserved type name), `record`, `sealed`,
`permits`, `yield` (context keywords), `true`, `false`, `null`
(literal values).

---

## Appendix C: `final` Keyword Usage Guide

### When to Use `final`

| Context | Requirement | Example |
| ------- | ----------- | ------- |
| Method parameters | **Shall** (mandatory) | `void foo(final int argValue)` |
| Fields set once | **Shall** (mandatory) | `private final Timer mTimer = new Timer();` |
| Constants | **Shall** (mandatory) | `static final double MAX_SPEED = 4.5` |
| Utility classes | **Shall** (mandatory) | `public final class Constants` |
| Constants inner classes | **Shall** (mandatory) | `public static final class CAN` |
| Local variables | **Should** (encouraged) | `final double voltage = speed * 12.0` |
| Methods | Rarely needed | Only to prevent override in specific cases |
| Classes (non-utility) | Rarely needed | Only for leaf classes that must not be extended |

### Why `final`?

- **Parameters**: Prevents accidental reassignment; makes intent clear.
- **Fields**: Enables the compiler to detect accidental mutation.
  Communicates "this value is set once and never changes."
- **Constants**: `static final` enables compile-time constant folding
  and communicates that the value is a true constant.
- **Utility classes**: Prevents meaningless subclassing.

---

## Appendix D: FRC Robot Lifecycle Reference

### Mode Transitions

```text
Power On → robotInit()
         → disabledInit() → disabledPeriodic() [repeats]
         → autonomousInit() → autonomousPeriodic() [repeats for 15 sec]
         → teleopInit() → teleopPeriodic() [repeats for ~2:15]
         → disabledInit() → disabledPeriodic() [match ends]
```

---

## Appendix E: GC Pressure Minimization Guide

### The Problem

The RoboRIO runs Java with a Serial GC (`-XX:+UseSerialGC`), a
stop-the-world collector chosen for its small memory footprint on
the RoboRIO's limited resources. SerialGC does **not** support
pause-time goals — `MaxGCPauseMillis` is a G1GC/ZGC hint and has
no effect here. See
[Team271-Software-Coding-Standard-Compliance.md §5.3](Team271-Software-Coding-Standard-Compliance.md)
for the actual JVM configuration.

Every object allocation in a periodic method creates garbage that
must eventually be collected. Excessive allocation can cause GC
pauses that exceed the loop budget, causing the robot to stutter or
brownout. Because we can't tune pause times, we minimize them by
minimizing allocations — the patterns below are the mechanism.

### Allocation-Free Patterns

**Pre-allocate objects at field declaration:**

```java
/* GOOD: allocated once */
private final Timer mReverseTimer = new Timer();

/* BAD: allocated every cycle */
Timer t = new Timer(); // in periodic method
```

**Reuse CTRE control requests:**

```java
/* ExampleTransmission already handles this internally.
 * If you use raw CTRE API, store requests as fields: */
private final VoltageOut mVoltageRequest = new VoltageOut(0);

/* In periodic: reuse the object. StatusCode is intentionally
 * discarded -- periodic control is best-effort at 50 Hz, and
 * transient failures self-correct on the next cycle
 * (CODE-GEN-005b). */
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

/* GOOD: use primitive arrays when possible */
int[] values = new int[count];
```

---

## Appendix H: CTRE Phoenix 6 Usage Patterns

### Timesync

All CTRE control requests **shall** use timesync to synchronize
command execution with the CANivore clock:

```java
.withUseTimesync(true).withUpdateFreqHz(0)
```

`UpdateFreqHz(0)` means "send immediately, don't auto-repeat."
The subsystem code is responsible for sending requests each cycle.

### Bulk Signal Refresh

Use `hardwareMgr.refreshAll()` at the start of each cycle to refresh
all registered CAN signals in a single bulk operation. This is more
efficient than refreshing signals individually.

### Control Request Reuse

Store control request objects as fields and reuse them:

```java
private final VoltageOut mVoltageRequest =
    new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0);

/* In robotPeriodicAfter: StatusCode is intentionally discarded --
 * periodic control is best-effort (CODE-GEN-005b). */
mMotor.setControl(mVoltageRequest.withOutput(voltage));
```

### Status Code Checking

Check `StatusCode` returns from configuration methods:

```java
StatusCode status = mMotor.getConfigurator().apply(config);
if (!status.isOK()) {
    DriverStation.reportError("Motor config failed: " + status, false);
}
```

---

---

## Appendix I: Naming Convention Quick Reference

| Context | Convention | Prefix | Example |
| ------- | ---------- | ------ | ------- |
| Classes | PascalCase | (none) | `ExampleDrive`, `ExampleShooter` |
| Inner classes | PascalCase | (none) | `ExampleSubsystemConstants` |
| Enums (type) | PascalCase | (none) | `ExampleControlState` |
| Enum values | UPPER_SNAKE_CASE | (none) | `IDLE`, `SHOOT`, `PATH_FOLLOWING` |
| Interfaces | PascalCase | (none) | `Interpolable` |
| Methods | camelCase | (none) | `robotInit()`, `isZeroed()` |
| Instance fields | camelCase | `m` | `mInstance`, `mControlState` |
| Operational constants | UPPER_SNAKE_CASE | (none) | `CAN_BUS_RIO`, `WHEEL_DIAMETER_M` |
| Tunable constants | camelCase | `k` | `kTranslationKp`, `kShooterRpsTarget` |
| Static mutable fields | camelCase | `m` | `mInstance` (singleton) |
| Method parameters | camelCase | `arg` | `argParent`, `argVoltage` |
| Local variables | camelCase | (none) | `speed`, `voltage` |
| Temporary locals | camelCase | `tmp` | `tmpStatusReturn` |
| NetworkTables fields | camelCase | `nt` | `ntRobot`, `ntMMCruiseVel` |
| Packages | lowercase | (none) | `com.example.app` |
| Boolean fields | camelCase | `mIs`/`mHas` | `mIsHomed`, `mHasTarget` |
| Boolean methods | camelCase | `is`/`has`/`can` | `isZeroed()`, `hasTarget()` |
