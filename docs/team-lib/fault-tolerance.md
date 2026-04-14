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

`applyConfig()` retries up to `CAN_RETRY_COUNT` times with a 50 ms
timeout per attempt. If all retries fail:
- `isConfigured` is set to `false`
- The motor operates with default Phoenix 6 config (which includes
  safe current limits)

### Bus Validation for Followers

`ControllerBase.follow()` validates that leader and follower are on
the same CAN bus. Returns `ERROR_INVALID_BUS` if not. This prevents
silent failures where a follower on a different bus would not receive
timely updates.

---

## Timeout Protection Pattern

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
