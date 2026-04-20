<!-- markdownlint-disable MD013 MD060 -->
# Testing Strategy

> **Scope:** This document covers Team271-Lib's test architecture and
> patterns. Robot projects follow the same patterns for their own tests
> but may add physics-based simulation tests that are robot-specific.

---

## Framework

- **JUnit 5** (Jupiter) — `junit-jupiter:5.10.1`
- **JaCoCo** — code coverage with HTML/XML/CSV reports
- **HAL simulation** — WPILib Hardware Abstraction Layer in sim mode
- **Auto-detection** — `junit.jupiter.extensions.autodetection.enabled = true`

### Running Tests

```bash
./gradlew test              # run all tests (produces JaCoCo report)
./gradlew jacocoTestReport  # generate coverage report (auto-runs after test)
```

Coverage reports are generated in `build/reports/jacoco/`. The `libtest`
package is excluded from coverage metrics.

---

## Test Structure

Tests mirror the main source tree:

```text
src/test/java/com/team271/lib/
├── auto/                    5 test classes
├── control/pid/             5 test classes (PIDSimple, PIDTrap, PIDWPI, PIDFX, Balance)
├── geometry/                4 test classes
├── hardware/
│   ├── controllers/         1 test class (ControllerTalonFX)
│   ├── sensors/encoders/    5 test classes
│   ├── sensors/imu/         1 test class (IMUPigeon2)
│   ├── sensors/range/       1 test class (RangeCANrange)
│   ├── sensors/switches/    2 test classes
│   ├── transmissions/       2 test classes
│   └── (root)              3 test classes (CANBus, CANDeviceID, CTREManager)
├── subsystem/               2 test classes
├── nt/                      3 test classes
├── sysid/                   2 test classes
├── util/                    6 test classes
└── (root)                   2 test classes (TObj, TRobot)
```

**Total: ~47 test classes covering ~71 main classes (66% class ratio).**

---

## Critical Pattern: HAL Initialization

**Every test class** that touches WPILib or CTRE hardware must initialize
the HAL before any test runs:

```java
@BeforeAll
static void initHAL() {
    HAL.initialize(500, 0);
}
```

Without this, any WPILib or Phoenix 6 object creation will fail with
native library errors. The `500` parameter is the timeout in milliseconds;
`0` is the mode.

This is a `@BeforeAll` (runs once per class), not `@BeforeEach`.

---

## Critical Pattern: CTREManager Static State Cleanup

`CTREManager` is a static singleton — its state persists across tests
within the same JVM. Every test class that creates CTRE devices **must**
reset CTREManager in `@BeforeEach`:

```java
@BeforeEach
void resetCTREManager() throws Exception {
    clearStaticField("buses");
    clearStaticField("devicesByBus");
    clearStaticField("devices");
    clearStaticField("signalsAll");
    setStaticField("signalsAllArray", null);
    setStaticField("prevRefreshTime", null);
    setStaticField("lastRefreshTime", null);
    setStaticField("lastErrorNotificationTime", 0.0);
}

private void clearStaticField(String fieldName) throws Exception {
    Field f = CTREManager.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    Object collection = f.get(null);
    if (collection instanceof java.util.Map) {
        ((java.util.Map<?, ?>) collection).clear();
    } else if (collection instanceof java.util.List) {
        ((java.util.List<?>) collection).clear();
    }
}

private void setStaticField(String fieldName, Object value) throws Exception {
    Field f = CTREManager.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    f.set(null, value);
}
```

This uses reflection because CTREManager's fields are private. Without
this cleanup, tests that register devices or signals will see stale
state from previous tests.

### Preferred: resetForTesting()

CTREManager provides a `resetForTesting()` method that clears all
internal state without reflection. Use this instead of the
reflection-based pattern above:

```java
@BeforeEach
void setup() {
    HAL.initialize(500, 0);
    CTREManager.resetForTesting();  // clears buses, devices, signals
}
```

This is simpler, less brittle, and does not require reflection.
The reflection-based pattern above remains valid but is no longer
necessary.

---

## Test Isolation Guidelines

### What to Reset

| Singleton | How to Reset | When |
|-----------|-------------|------|
| `CTREManager` | Reflection teardown (see above) | Any test creating CTRE devices |
| `SubsystemManager` | Clear subsystem list | Any test registering subsystems |
| `NTTable` / NetworkTables | Generally safe; HAL handles | Usually not needed |

### CAN ID Uniqueness

Each test should use unique CAN IDs to avoid device conflicts:

```java
@Test
void testMotorA() {
    ControllerTalonFX motor = new ControllerTalonFX(null, "A", new CANDeviceID(1), KRAKEN);
}

@Test
void testMotorB() {
    ControllerTalonFX motor = new ControllerTalonFX(null, "B", new CANDeviceID(2), KRAKEN);
}
```

### Shared Constants

Use `static final` fields for commonly reused test fixtures:

```java
private static final MotorBase KRAKEN = new MotorBase(MotorBase.MotorType.KRAKENX60);
```

---

## What to Test vs. What Hardware Tests

### Unit-Testable (test in simulation)

- Object construction and initialization
- Configuration API (setters/getters for limits, gains, etc.)
- Follower validation (same-bus enforcement)
- Gear ratio conversion math
- State machine transitions
- Telemetry key registration
- PID calculation math (PIDSimple, PIDTrap)
- Geometry transforms (Pose2d, Rotation2d, etc.)
- Input shaping math
- Auto move sequencing and completion logic

### Hardware-Dependent (cannot fully test in unit tests)

- Actual motor output (voltage/duty cycle to hardware)
- CAN signal values (StatusSignal reads return default/zero in sim)
- Latency compensation accuracy
- Real sensor readings
- Encoder position accuracy after gear ratio conversion
- Config application to real devices (timeouts, retries)

For hardware-dependent behavior, the tests verify that the API calls
don't throw exceptions and that the configuration is stored correctly.
Actual hardware validation happens during robot testing.

---

## Writing a New Test

### 1. Create the test class

```java
package com.team271.lib.hardware.sensors.encoders;

import static org.junit.jupiter.api.Assertions.*;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyNewSensorTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetState() throws Exception {
        // Reset CTREManager if creating CTRE devices
    }

    @Test
    void constructorCreatesValidObject() {
        // ...
    }
}
```

### 2. Test naming convention

Use descriptive method names that read as specifications:

```java
void constructorCreatesTalonFX()
void getConfigIsNotNull()
void followRejectsOppositeBus()
void setCurrentLimitStatorAppliesValue()
```

### 3. Suppress resource warnings

CTRE devices implement `AutoCloseable` but HAL manages their lifecycle
in simulation. Suppress the warning at class level:

```java
@SuppressWarnings("resource")
class ControllerTalonFXTest {
```

---

## Coverage Targets

JaCoCo reports are generated after every test run. The `libtest` package
is excluded from metrics (it's the test robot harness, not library code).

Focus coverage on:

- **Constructor paths** — verify objects initialize without errors
- **Configuration API** — verify getters return what setters store
- **Validation logic** — verify boundary checks, null guards, bus validation
- **State transitions** — verify sensor mode changes, completion flags
- **Math functions** — verify PID calculations, geometry transforms,
  unit conversions

---

## Simulation-Based Testing

### CTRE SimState in Unit Tests

When `HAL.initialize()` is called, CTRE devices create functional
SimState objects. You can use these in unit tests to verify
simulation wiring:

```java
@Test
void simulationInitSetsMotorType() {
    CANDeviceID id = new CANDeviceID(10);
    ControllerTalonFX controller =
        new ControllerTalonFX(null, "Sim", id, KRAKEN);

    controller.simulationInit(0.0);

    // SimState should now be initialized
    assertNotNull(controller.getSimState());
}

@Test
void setSimPosRotationsPropagates() {
    CANDeviceID id = new CANDeviceID(11);
    TransmissionFX transmission =
        new TransmissionFX(null, "Sim", Motor.kKrakenX60, id);
    transmission.simulationInit(0.0);

    // Set simulated position — should not throw
    transmission.setSimPosRotations(5.0);
    transmission.setSimVelRotations(1.0);
}
```

### What Simulation Tests Can Verify

- SimState objects are created during `simulationInit()`
- `setSimPosRotations()` / `setSimVelRotations()` propagate without errors
- Supply voltage updates don't throw during `simulationPeriodic()`
- Motor type and orientation are configured correctly
- DCMotor model is created with the correct motor count

### What Simulation Tests Cannot Verify

- Physics accuracy (depends on robot-specific WPILib sim classes)
- Closed-loop PID response (requires firmware simulation loop)
- CAN signal values (StatusSignals return defaults in unit tests)
- Real-world timing and latency compensation

### Desktop Simulation vs Unit Tests

| Aspect | Unit Tests (`./gradlew test`) | Desktop Sim (`./gradlew simulateJava`) |
|--------|-------------------------------|---------------------------------------|
| Speed | Fast (seconds) | Slow (requires GUI) |
| Physics | No physics models | Full WPILib physics models |
| Dashboard | No dashboard | Full NT + dashboard |
| Tuning | No `LoggedNTInput` changes | Live tuning via dashboard |
| Scope | Library correctness | Robot behavior validation |
| CI-friendly | Yes | No |

**Guideline:** Use unit tests for library correctness (construction,
config, math, state machines). Use desktop simulation for full-stack
validation with physics and dashboard tuning. Robot projects should
have both.

---

## Robot Project Testing

Robot projects follow the same patterns as the library, with additions:

- **Physics model tests** — verify that `simulationPeriodic()` produces
  expected position/velocity for known voltage inputs
- **Auto sequence tests** — verify that `AutoMode` compositions complete
  in expected order using simulated time
- **State machine integration tests** — verify cross-subsystem
  coordination using SubsystemManager lifecycle

Robot-specific tests live in the robot project's `src/test/` directory,
not in Team271-Lib.
