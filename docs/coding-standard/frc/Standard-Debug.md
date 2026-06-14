<!-- markdownlint-disable MD007 MD032 -->
# Team271-Lib FRC Coding Standard - Debugging and Telemetry

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Debugging companion to the FRC overlay's [`Standard.md`](Standard.md).
Contains `CODE-BUG-*` rules covering telemetry publication,
driver-station notifications, error reporting, and runtime
tunability.

These rules supplement the pure-Java rules in
[`../java/Standard.md`](../java/Standard.md), which remain
applicable - `CODE-BUG-*` is FRC-specific because it depends on
the WPILib telemetry and DriverStation facilities and the
team's chosen replay-capable logger.

---

## 4.8 Debugging and Telemetry

<a id="code-bug-001"></a>

### CODE-BUG-001 -- Telemetry Discipline (Source: Team271-Lib)

a. Every subsystem **shall** implement the project's telemetry
   publishing method and publish state information via a
   replay-capable logging facility (e.g., AdvantageKit's
   `Logger.recordOutput`, WPILib's `DataLog`, or the project's
   own wrapper around either).

b. At minimum, each subsystem **shall** log:

   - Current control state
   - Motor output values (voltage, duty cycle, or velocity
     target)
   - Sensor readings used for control decisions

c. Telemetry key naming **shall** follow the pattern
   `"SubsystemName/ValueName"`:

   ```text
   ExampleSubsystem/ControlState
   ExampleSubsystem/Speed
   ```

d. Telemetry **may** be rate-limited to reduce CAN bus load.
   The "publish every Nth cycle" pattern is acceptable.

<a id="code-bug-002"></a>

### CODE-BUG-002 -- Dashboard Notifications (Source: Team271-Lib)

a. The project's driver-notification facility **should** be
   used for significant events visible to the drive team:

   - Mode transitions
   - Homing completion
   - Error conditions

b. Notifications **should** include a display-time argument to
   avoid cluttering the dashboard.

<a id="code-bug-003"></a>

### CODE-BUG-003 -- Error Reporting (Source: Team271-Lib)

a. `DriverStation.reportError()` **shall** be used for
   unexpected states (e.g., the `default` case in an enum
   `switch`).

b. `DriverStation.reportWarning()` **shall** be used for
   recoverable issues (e.g., CAN bus refresh failures).

c. Repeated warnings **shall** be rate-limited to avoid
   console spam. Use a "last status" pattern:

   ```java
   if (refreshStatus != mLastCANRefreshStatus) {
       DriverStation.reportWarning("CAN refresh: " + refreshStatus, false);
       mLastCANRefreshStatus = refreshStatus;
   }
   ```

<a id="code-bug-004"></a>

### CODE-BUG-004 -- Runtime Tunability (Source: Team271-Lib)

a. All configurable control values **shall** be tunable at
   runtime via the project's replay-faithful tuning mechanism.
   This includes:

   - PID gains (P, I, D, tolerance, deadband, I zone, output
     range)
   - Current limits (stator and supply enable/value)
   - Voltage limits (peak forward, peak reverse)
   - Motion Magic profiles (cruise velocity, acceleration,
     jerk)
   - Velocity targets (RPS/RPM setpoints per mode)
   - Duty cycle targets
   - Threshold values (velocity gating, jam detection, homing
     current)

b. Values already tunable through a project-supplied library
   class **shall** inherit tunability automatically from that
   class. Subsystem code **shall not** re-implement tuning for
   values already exposed there.

c. Values defined as `static final` constants in the project's
   `Constants` class that are read once at initialization and
   not subsequently updatable **should** be migrated to
   runtime-tunable fields, using the constant as the default
   value. The project's per-cycle tuning hook applies dashboard
   changes.

d. Compile-time constants **shall** remain acceptable for
   values that are physically fixed (CAN IDs, gear ratios,
   wheel diameter), timing that is structurally load-bearing
   (auto move sequencing), and safety bounds that should not
   be adjustable during a match (absolute max current,
   emergency-stop thresholds).

e. All runtime-tunable fields **shall** be initialized with
   safe default values from the corresponding `Constants`
   class.

f. The per-cycle tuning hook **shall** be called at the
   beginning of the telemetry-publish phase, ensuring tuning
   changes are applied at the end of the robot cycle (after
   control outputs, before the next sensor read).

---
