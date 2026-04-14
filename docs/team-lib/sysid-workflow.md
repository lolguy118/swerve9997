<!-- markdownlint-disable MD013 MD060 -->
# System Identification Workflow

> **Scope:** This document covers the library's SysID logging
> infrastructure. The actual SysID test runs and gain extraction happen
> in each robot project — the library provides the data collection
> framework that robot projects wire into their mechanisms.

---

## Classes

### Logger — Base SysID Logger

Handles the data collection infrastructure:

- Pre-allocates a data vector (`DATA_VECTOR_SIZE = 36000`) sized for
  20 seconds at 200 Hz — avoids GC during data collection
- Manages test lifecycle via NetworkTables commands
- Runs data collection on a high-priority thread (`THREAD_PRIORITY = 15`)
- Supports two test types:
  - **Quasistatic** — ramps voltage slowly (V/s) to measure steady-state
  - **Dynamic** — applies a step voltage to measure transient response

### LoggerGeneral — General Mechanism Logger

Extends Logger for simple mechanisms (Arm, Elevator, Simple):

```java
void log(double timestamp, double voltage,
         double measuredPosition, double measuredVelocity)
```

Records 4 doubles per sample: timestamp, voltage, position, velocity.

---

## Running a SysID Test

### 1. Configure the Mechanism

In your robot code, create a `LoggerGeneral` and connect it to the
mechanism's transmission:

```java
LoggerGeneral sysidLogger = new LoggerGeneral();

// In testPeriodic():
double voltage = sysidLogger.getMotorVoltage();
transmission.setOutputVoltage(voltage);
sysidLogger.log(
    Timer.getFPGATimestamp(),
    voltage,
    transmission.getPosFX(),
    transmission.getVelFXRPS()
);
```

### 2. Set the Subsystem to SYSID Mode

Switch the subsystem's sensor mode so normal control logic doesn't
interfere:

```java
subsystem.setSensorMode(SensorMode.SYSID);
```

### 3. Run Tests via NetworkTables

The SysID tool (or a custom dashboard) sends commands via
NetworkTables to start/stop quasistatic and dynamic tests. The
Logger class handles these commands automatically.

### 4. Analyze Data

Export the collected data and analyze with WPILib's SysID tool to
extract kS (static friction), kV (velocity), and kA (acceleration)
constants.

### 5. Apply Gains

Use the extracted values to configure feedforward in your
`TransmissionFX`:

```java
transmission.configPIDFSlot(0, kP, kI, kD, kV, kS);
```

---

## Supported Mechanism Types

`LoggerGeneral.isWrongMechanism()` accepts:

| Mechanism | Use For |
|-----------|---------|
| `""` (empty) | Default / generic |
| `"Arm"` | Rotational arm mechanisms |
| `"Elevator"` | Linear elevator mechanisms |
| `"Simple"` | Single-motor mechanisms |

---

## Thread Priority

SysID logging runs at elevated thread priority to ensure consistent
sampling:

| Thread | Priority |
|--------|----------|
| SysID data collection | 15 |
| HAL thread | 40 |

This ensures data collection doesn't get preempted by lower-priority
tasks during characterization runs.
