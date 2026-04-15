<!-- markdownlint-disable MD013 MD060 -->
# Utility & Misc Package

> **Scope:** This document covers the `util/` and `misc/` packages —
> shared utilities used throughout the library. These include the Alert
> system, Elastic Dashboard integration, drive signal helpers, and math
> utilities.

---

## Alert — Persistent Driver Notifications

`Alert` provides persistent, categorized alerts with active/inactive
state management. Unlike Elastic notifications (fire-and-forget),
Alerts have persistent state — they remain active until explicitly
deactivated.

### Alert Types

| Type | Severity | Use When |
|------|----------|----------|
| `ERROR` | High | Hardware failure, safety fault, operation failure |
| `WARNING` | Medium | Degraded operation, timeout, unexpected condition |
| `INFO` | Low | State transitions, mode changes, informational |

### Usage

```java
// Create an alert (default "Alerts" group)
Alert canAlert = new Alert("CAN bus disconnected", AlertType.ERROR);

// Activate when the condition is detected
canAlert.set(true);

// Deactivate when resolved
canAlert.set(false);
```

### Behavior

- **On activation:** Logs to DriverStation and sends an Elastic
  notification at the corresponding severity level
- **On deactivation:** Clears internal state silently
- **Removal:** `alert.remove()` deregisters the alert from its group
- **Groups:** Alerts are organized by group name (default: `"Alerts"`).
  Groups are auto-created on first use via a thread-safe
  `ConcurrentHashMap`
- **Telemetry:** `Alert.outputTelemetry()` publishes all active alerts
  to AdvantageKit as string arrays grouped by type (called by
  `SubsystemManager`)

### Difference from Elastic Notifications

| Feature | Alert | Elastic.sendNotification() |
|---------|-------|---------------------------|
| State | Persistent (active/inactive) | Fire-and-forget |
| Telemetry | Logged to AK as arrays | Not logged |
| Grouping | By named group | None |
| Dashboard | Appears in alert widget | Appears as popup |

---

## Elastic — Dashboard Notifications

`Elastic` provides fire-and-forget notifications to the Elastic
Dashboard via NetworkTables. Published to `/Elastic/RobotNotifications`
as JSON.

### Sending Notifications

```java
Elastic.sendNotification(
    new Elastic.Notification(
        Elastic.Notification.NotificationLevel.WARNING,
        "Homing Timeout",
        "Elevator homing timed out after 2.0s"));
```

### Notification Builder (Fluent API)

```java
Elastic.sendNotification(
    new Elastic.Notification()
        .withLevel(NotificationLevel.ERROR)
        .withTitle("CAN Error")
        .withDescription("Motor disconnected: Left Drive")
        .withDisplaySeconds(5.0)
        .withWidth(400));
```

### Notification Levels

| Level | Use For |
|-------|---------|
| `INFO` | Mode changes, auto start/complete, state transitions |
| `WARNING` | Timeouts, degraded operation, homing failures |
| `ERROR` | CAN disconnect, config failure, subsystem exception |

### Tab Selection

```java
Elastic.selectTab("Autonomous");   // by name
Elastic.selectTab(2);              // by index
```

### Implementation Details

- Serialization via Jackson `ObjectMapper`
- On serialization error, reports to DriverStation (does not throw)
- Default display time: 3000 ms
- Default width: 350, default height: -1 (auto-infer)

---

## DriveSignal — Differential Drive Command

Immutable pair of (left, right) motor outputs for differential drive.
Fields are `private final`; motor values are clamped to [-1.0, 1.0] on
construction.

### Constants

| Constant | Left | Right | Brake |
|----------|------|-------|-------|
| `NEUTRAL` | 0.0 | 0.0 | false |
| `BRAKE` | 0.0 | 0.0 | true |

### Key Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `getLeft()` | `double` | Left motor output [-1.0, 1.0] |
| `getRight()` | `double` | Right motor output [-1.0, 1.0] |
| `getBrakeMode()` | `boolean` | Whether brake mode is active |
| `normalize()` | `DriveSignal` | Scales outputs so `max(|left|, |right|) = 1.0` |

---

## Util — Math Utilities

Static utility class for common math operations.

### Constants

| Constant | Value | Purpose |
|----------|-------|---------|
| `kEpsilon` | `1e-12` | Floating-point comparison tolerance |

### Comparison & Range

| Method | Description |
|--------|-------------|
| `epsilonEquals(a, b)` | Double comparison within `kEpsilon` |
| `epsilonEquals(a, b, epsilon)` | Double comparison within custom epsilon |
| `inRange(v, max)` | Checks `|v| < max` |
| `inRange(v, min, max)` | Checks `min < v < max` |
| `allCloseTo(list, value, epsilon)` | All list elements within epsilon of value |

### Clamping & Mapping

| Method | Description |
|--------|-------------|
| `limit(v, max)` | Clamps to `[-max, max]` |
| `limit(v, min, max)` | Clamps to `[min, max]` |
| `interpolate(a, b, x)` | Linear interpolation: `a + (b - a) * clamp(x)` |
| `reMap(input, inStart, inEnd, outStart, outEnd)` | Linear range remapping |

### Joystick Utilities

| Method | Description |
|--------|-------------|
| `handleDeadzone(value, deadband)` | 1D deadzone with linear rescaling to [0, 1] |
| `handleDeadzone_Radial(out, x, y, low, high)` | 2D radial deadzone for stick pairs |
| `convertTrigger(value)` | Converts trigger [-1, 1] to [0, 1]: `(v + 1) / 2` |

### Other

| Method | Description |
|--------|-------------|
| `joinStrings(delim, list)` | *Deprecated* — use `String.join()` |
| `getMACAddress()` | Retrieves first non-loopback MAC address |

> **Note:** The `CSVWritable` and `Interpolable` interfaces were removed
> along with the custom geometry package. See
> [Geometry Package](geometry-package.md) for rationale.
