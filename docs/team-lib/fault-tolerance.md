<!-- markdownlint-disable MD013 MD060 -->
# Fault Tolerance Patterns

> **Scope:** This document covers library-level fault tolerance — the
> infrastructure that all robot projects inherit. Robot-specific fault
> scenarios (e.g., "what happens when the intake jams") belong in each
> robot project's own design docs. The [Library vs. Robot Responsibilities](#library-vs-robot-responsibilities)
> table at the end clarifies the boundary.

See coding standard Section 4.9 (CODE-SAF-008 through CODE-SAF-011)
for the rules these patterns implement.

---

## Exception Isolation (SubsystemManager)

The most fundamental fault tolerance pattern: one subsystem crash
must not bring down the entire robot.

**Why this matters:** Without isolation, a null pointer in one
subsystem's `robotPeriodicBefore()` would crash the entire robot loop.
In competition, this means losing control of the drivetrain because the
intake threw an exception. Exception isolation ensures that only the
faulting subsystem stops operating — the rest of the robot continues.

`SubsystemManager.forEachSafe()` wraps every subsystem lifecycle
call in a try/catch:

```text
for each subsystem:
    try { action.accept(subsystem); }
    catch (Exception e) {
        log error to DriverStation
        send Elastic notification (throttled to 1 per 2s per subsystem)
    }
```

**What this means for library consumers:**
- A null pointer in your intake subsystem won't stop the drivetrain
- Error notifications are throttled — the driver station won't flood
- The exception is logged with the phase name for debugging

**Exception to isolation:** `robotInit()` does **not** isolate — an
init failure is fatal because the robot cannot safely operate with
a partially initialized subsystem.

---

## CAN Bus Fault Handling

**Why this matters:** CAN bus wiring is subject to vibration, connector
wear, and brownout. A motor dropping off the CAN bus mid-match is not
hypothetical — it happens. Without connection monitoring, the library
would continue sending control requests to a device that cannot receive
them, wasting cycle time and masking the failure from the driver.

### Connection Monitoring (ControllerTalonFX)

Every `ControllerTalonFX.robotPeriodicBefore()` checks:

```java
isConnected = talonFX.isConnected();
```

Output methods guard on this flag:

```java
if (isConnected) {
    talonFX.setControl(motorOutV);
}
```

**What this means:** If a motor drops off the CAN bus, the library
stops sending commands to it. It does not throw exceptions or stall
the control loop.

### Signal Refresh Errors (CTREManager)

`CTREManager.refreshAll()` returns a `StatusCode`. On failure:
- The error is logged
- An Elastic notification is sent (throttled to 1 per 2 seconds)
- The robot loop continues — stale signal values are used

### Config Application Retries (ControllerTalonFX)

**Why retries are needed:** CAN config frames are larger than status
frames and more susceptible to collision. A single config write failing
is normal under high bus load. The retry loop amortizes this, but the
total blocking time (up to `CAN_RETRY_COUNT * CAN_TIMEOUT_MS`) is why
config must happen only in `robotInit()`, never in periodic methods.

`applyConfig()` retries up to `CAN_RETRY_COUNT` times with a 50 ms
timeout per attempt. If all retries fail:
- `isConfigured` is set to `false`
- The motor operates with default Phoenix 6 config (which includes
  safe current limits)

### Bus Validation for Followers

**Why this matters:** TalonFX follower mode requires leader and follower
on the same physical CAN bus. If they are on different buses (e.g., one
on RIO CAN, one on CANivore), the follower will not receive the leader's
frame — it will sit idle at zero output with no error indication. The
bus validation catches this at construction time.

`ControllerBase.follow()` validates that leader and follower are on
the same CAN bus. Returns `ERROR_INVALID_BUS` if not. Callers must
check this return value and report errors (see D4 in the code fix log).

---

## CTRE Fault Coverage (FaultMonitor)

**Why this matters:** Phoenix 6 devices expose dozens of fault signals
(under-voltage, hardware faults, thermal shutdowns, etc.). Monitoring
these faults gives the driver and pit crew immediate visibility into
hardware problems. Without monitoring, a motor in thermal shutdown
looks identical to a CAN disconnect from the driver's perspective.

### FaultMonitor Architecture

`FaultMonitor` is a reusable class that monitors CTRE sticky fault
signals. Each device creates a FaultMonitor in its constructor and
registers fault signals by name:

- **Registration:** `addFault(name, stickyFaultSignal, updateFreqHz)`
- **Signal registration:** `registerSignals()` called in `robotInit()`
- **Checking:** `refresh()` called in `robotPeriodicBefore()`
- **Telemetry:** `outputTelemetry()` publishes per-fault booleans and
  a summary `Has Fault` boolean to NetworkTables
- **Alerts:** Each fault creates a persistent `Alert` in the "Faults"
  group. Active faults appear on the dashboard automatically.

Sticky faults are used (not live faults) because they latch transient
events that might clear before the next read cycle.

### Monitored Faults by Device

| Device | Faults Monitored |
|--------|-----------------|
| **TalonFX** | BootDuringEnable, DeviceTemp, ProcTemp, Hardware, Undervoltage, BridgeBrownout |
| **CANCoder** | BootDuringEnable, Undervoltage, BadMagnet |
| **Pigeon 2** | BootDuringEnable, Hardware, Undervoltage, SaturatedMagnetometer, SaturatedAccelerometer, SaturatedGyroscope |

All sticky faults are cleared at device creation via
`clearStickyFaults()`. New faults that appear after initialization
indicate a real issue that occurred during operation.

---

## Timeout Protection Pattern

**Why this matters:** Any operation that blocks on a condition (current
spike, velocity target, sensor signal) can block indefinitely if the
expected condition never occurs. In competition, this means the robot
freezes mid-auto because the launcher never reached its target speed
due to a ball jam. Timeouts turn indefinite blocks into bounded waits
with driver notification.

Per coding standard 4.9c, any operation that waits for a condition
must have a timeout. The library provides `AutoMoveConditional` as
the standard implementation:

```java
new AutoMoveConditional(
    "LauncherReady",
    Launcher::isAtMaxVelocity,
    3.0  // timeout in seconds
);
```

On timeout:
- A WARNING notification is sent via Elastic
- The move ends (allowing the auto sequence to continue)
- The driver sees a notification on the dashboard

### Implementing Timeouts in Subsystems

For robot-specific subsystem timeouts (e.g., homing), follow this
pattern:

```java
// In Constants:
public static final double kHomingTimeoutSec = 2.0;

// In subsystem periodic:
if (state == State.HOMING) {
    if (homingTimer.hasElapsed(Constants.kHomingTimeoutSec)) {
        // Fail safe:
        transmission.stop();
        transmission.setCurrentLimitStator(true, kDefaultStatorLimit);
        state = State.IDLE;
        Elastic.sendNotification(
            new Elastic.Notification(Elastic.NotificationLevel.WARNING,
                "Homing Timeout", getName() + " homing timed out"));
    }
}
```

Requirements per coding standard:
1. Named timeout constant (not a magic number)
2. Fail safe: stop motors, restore default limits, go to IDLE
3. Driver notification via Elastic

---

## Error Notification Throttling

**Why this matters:** Without throttling, a persistent CAN error would
send 50 Elastic notifications per second (one per 20 ms cycle), flooding
the driver station and making it unusable. The 2-second throttle ensures
the driver sees the error without being overwhelmed.

Multiple components use time-based throttling to prevent flooding:

| Component | Throttle | Field |
|-----------|----------|-------|
| SubsystemManager | 2s per subsystem | `lastErrorNotificationTime` map |
| CTREManager | 2s global | `lastErrorNotificationTime` static |
| TransmissionBase | 2s for config errors | `lastConfigErrorNotificationTime` |

Pattern:

```java
double now = Timer.getFPGATimestamp();
if (now - lastErrorNotificationTime > 2.0) {
    Elastic.sendNotification(...);
    lastErrorNotificationTime = now;
}
```

---

## Sensor Fallback (TransmissionBase)

TransmissionBase supports dual encoders (FX + CANCoder). Position
queries automatically fall back:

1. If CANCoder is configured → use CANCoder position
2. If no CANCoder → use FX integrated encoder position

This provides resilience against a single encoder failure, though
both encoders failing would require robot-specific handling.

---

## Safe Defaults

The library establishes safe defaults throughout:

| Component | Safe Default |
|-----------|-------------|
| Input axes | Return 0.0 when controller disconnected |
| Motor output | Guards on `isConnected` — no output if disconnected |
| Config failure | Motor runs with Phoenix 6 factory defaults |
| Switch auto-zero | Disabled by default — must be explicitly enabled |
| PID output | Clamped to `[minOutput, maxOutput]` range |
| Integral wind-up | Bounded by `[iMin, iMax]` and `iZone` |
| TransmissionBase leader | Null-checked in `robotInit()` — logs error and returns safely |
| CTRE fault monitoring | FaultMonitor tracks sticky faults per device with Alerts and NT telemetry (see [CTRE Fault Coverage](#ctre-fault-coverage-faultmonitor)) |

---

## Library vs. Robot Responsibilities

| Concern | Library Handles | Robot Must Handle |
|---------|----------------|-------------------|
| Exception isolation | SubsystemManager wraps calls | Define recovery behavior per subsystem |
| CAN disconnect detection | `isConnected` flag tracking | Decide what to do when motors disconnect |
| Config retries | Automatic retry loop | Handle persistent config failure |
| Signal refresh errors | Throttled notifications | Determine if stale data is safe |
| Timeout infrastructure | `AutoMoveConditional`, timer utilities | Define timeout values, fail-safe actions |
| Input disconnect | Return 0.0, track connection state | Alert driver, switch to backup input |
| Brownout recovery | Not handled (robot-specific) | Detect and re-initialize as needed |
| CAN bus partition | Not handled (robot-specific) | Per CODE-SAF-011, handle gracefully |
