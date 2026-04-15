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

## CTRE Fault Coverage

**Why this matters:** Phoenix 6 devices expose dozens of fault signals
(supply over-voltage, under-voltage, hardware faults, thermal
shutdowns, etc.). Monitoring these faults gives the driver and pit
crew immediate visibility into hardware problems. Without monitoring,
a motor in thermal shutdown looks identical to a CAN disconnect from
the driver's perspective.

### Currently Monitored

| Device | Fault | How Checked | Cleared |
|--------|-------|-------------|---------|
| TalonFX | `getStickyFault_BootDuringEnable()` | Registered at 250 Hz via CTREManager | `clearStickyFaults()` at device creation |

This single fault detects when a motor controller rebooted while the
robot was enabled — a strong indicator of a brownout or wiring issue.

### Not Yet Monitored

The following Phoenix 6 fault categories are available in the API
but not yet tracked by the library:

| Category | Example Faults | Why It Matters |
|----------|---------------|----------------|
| **Supply voltage** | `Fault_SupplyOverV`, `Fault_SupplyUnstable` | Detects brownout or voltage regulator issues |
| **Hardware faults** | `Fault_Hardware`, `StickyFault_Hardware` | Detects internal device failure |
| **Thermal** | `Fault_DeviceTemp`, `Fault_ProcTemp` | Detects overheating before thermal shutdown |
| **Limit switch** | `Fault_ForwardHardLimit`, `Fault_ReverseHardLimit` | Confirms limit switch engagement |
| **Sensor** | `Fault_RemoteSensorInvalid` | Detects CANCoder communication loss |
| **Bridge faults** | `Fault_BridgeBrownout`, `StickyFault_BridgeBrownout` | Detects motor driver brownout |

### Fault Telemetry Gap

No fault status is currently published to NetworkTables. When the
library expands fault monitoring, each device should publish its
active faults to an NT subtable for dashboard visibility. This would
allow the driver to see "Left Drive Motor: SupplyUnstable" on Elastic
rather than inferring faults from motor behavior.

See the [Not Yet Implemented](hardware-abstraction.md#phoenix-6-features-not-yet-implemented)
table in Hardware Abstraction for the full planned feature list.

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
| CTRE fault monitoring | Only `BootDuringEnable` sticky fault checked; other faults pass silently (see [CTRE Fault Coverage](#ctre-fault-coverage)) |

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
