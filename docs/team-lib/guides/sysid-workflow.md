# System Identification Workflow

> **Scope:** This document covers the library's SysID logging
> infrastructure. The actual SysID test runs and gain extraction happen
> in each robot project — the library provides the data collection
> framework that robot projects wire into their mechanisms.

---

## Classes

### Class Hierarchy

```text
Logger              (base — data vector, NT commands, thread priority, voltage computation)
└── LoggerGeneral   (concrete — 4-double logging for simple mechanisms)
```

### Logger — Base SysID Logger

Handles the data collection infrastructure:

- Pre-allocates a data vector (`DATA_VECTOR_SIZE = 36000`) — avoids GC
  during data collection (see [Data Vector Sizing](#data-vector-sizing))
- Manages test lifecycle via NetworkTables commands
- Runs data collection on a high-priority thread (`THREAD_PRIORITY = 15`,
  see [Thread Priority](#thread-priority))
- Supports two test types:
  - **Quasistatic** — ramps voltage linearly (`voltageCommand * elapsed`)
    to measure steady-state characteristics
  - **Dynamic** — applies a constant step voltage to measure transient
    response

### LoggerGeneral — General Mechanism Logger

Extends Logger for simple mechanisms (Arm, Elevator, Simple):

```java
void log(double timestamp, double voltage,
         double measuredPosition, double measuredVelocity)
```

Records 4 doubles per sample: timestamp, voltage, position, velocity.
Calls `updateData()` on the base class to compute the current motor
voltage before recording. Silently stops recording if the data vector
reaches capacity (`DATA_VECTOR_SIZE`), and sets the `SysId/Overflow`
flag for the SysID tool to detect.

---

## Architecture Details

### Data Vector Sizing

`DATA_VECTOR_SIZE = 36000` is calculated as:

- 20 seconds of test data (maximum recommended characterization run)
- 200 samples/second (50 Hz robot loop × 4 doubles per sample)
- 9 doubles per sample at maximum (original design margin)
- Rounded up for safety margin

For `LoggerGeneral` (4 doubles/sample), the effective capacity is
36000 / 4 = 9000 samples = 45 seconds at 200 Hz — well beyond the
20-second target. Pre-allocation is critical because `ArrayList`
resizing triggers garbage collection, which introduces timing jitter
that corrupts the voltage-vs-velocity relationship SysID needs.

### Thread Priority

| Thread | Priority | Real-Time | Purpose |
| -------- | ---------- | ----------- | --------- |
| SysID data collection | 15 | Yes (non-sim) | Consistent sampling timing |
| HAL thread | 40 | Yes | Robot loop timing |
| Default Java threads | 5 | No | General application logic |

`updateThreadPriority()` sets both the HAL and current thread to
real-time priority via `Threads.setCurrentThreadPriority()`. In
simulation mode, priority setting is skipped (not supported). If
priority setting fails on real hardware, the method throws
`IllegalArgumentException` to fail loudly rather than silently
collecting bad data with inconsistent timing.

### NT Command Protocol

The SysID tool communicates with the Logger via SmartDashboard keys:

| NT Key | Type | Direction | Purpose |
| -------- | ------ | ----------- | --------- |
| `SysIdTest` | String | Tool → Robot | Mechanism name (empty = default) |
| `SysIdTestType` | String | Tool → Robot | `"Quasistatic"` or `"Dynamic"` |
| `SysIdRotate` | Boolean | Tool → Robot | Spin test for drivetrains |
| `SysIdVoltageCommand` | Double | Tool → Robot | Voltage (V) or ramp rate (V/s) |
| `SysIdAckNumber` | Double | Bidirectional | Handshake for data receipt |

Logger reads these in `initLogger()` at the start of each test run.

### Data Encoding and Transmission

`sendData()` serializes the data vector for the SysID tool:

1. Joins all doubles as comma-separated values
2. Prepends a test descriptor: `"fast-forward"`, `"fast-backward"`,
   `"slow-forward"`, or `"slow-backward"` (fast = Dynamic, slow = Quasistatic)
3. Publishes via `Logger.recordOutput("SysId/Telemetry", descriptor + ";" + data)`
4. Increments `ackNum` and publishes it — the SysID tool increments its
   own ack number when it has received the data, and `clearWhenReceived()`
   clears the telemetry string once the handshake completes

### Data Flow

```text
SysID Tool                              Robot
─────────                               ─────
  │  NT: SysIdTest, SysIdTestType,       │
  │      SysIdVoltageCommand, SysIdRotate│
  │ ──────────────────────────────────→  │
  │                                      │ initLogger() reads commands
  │                                      │ updateData() computes motorVoltage
  │                                      │   Quasistatic: V = cmd × elapsed
  │                                      │   Dynamic:     V = cmd
  │                                      │
  │                                      │ Robot project:
  │                                      │   voltage = logger.getMotorVoltage()
  │                                      │   transmission.setOutputVoltage(voltage)
  │                                      │   logger.log(timestamp, voltage, pos, vel)
  │                                      │
  │                                      │ sendData() → encodes + publishes
  │  NT: SysId/Telemetry (CSV data)      │
  │ ←──────────────────────────────────  │
  │  NT: SysId/AckNumber (handshake)     │
  │ ──────────────────────────────────→  │
  │                                      │ clearWhenReceived() clears telemetry
```

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
| ----------- | --------- |
| `""` (empty) | Default / generic |
| `"Arm"` | Rotational arm mechanisms |
| `"Elevator"` | Linear elevator mechanisms |
| `"Simple"` | Single-motor mechanisms |

For thread priority details, see
[Architecture Details — Thread Priority](#thread-priority) above.
