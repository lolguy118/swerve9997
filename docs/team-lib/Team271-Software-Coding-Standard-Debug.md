<!-- markdownlint-disable MD013 -->
<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Debugging and Telemetry

### CODE-BUG-001 -- Telemetry Discipline

a. All subsystems **shall** implement `outputTelemetry()` and publish
   state information via `Logger.recordOutput()`.

b. At minimum, each subsystem **shall** log:
   - Current control state
   - Motor output values (voltage, duty cycle, or velocity target)
   - Sensor readings used for control decisions

c. Telemetry key naming **shall** follow the pattern
   `"SubsystemName/ValueName"`:

   ```java
   Logger.recordOutput("ExampleSubsystem/ControlState", mControlState.toString());
   Logger.recordOutput("ExampleSubsystem/Speed", indexSpeed);
   ```

d. Telemetry **may** be rate-limited to reduce CAN bus load. The
   `TELEMETRY_PERIOD` pattern (publish every Nth cycle) is acceptable.

### CODE-BUG-002 -- Dashboard Notifications

a. `Elastic.sendNotification()` **should** be used for significant
   events visible to the drive team:
   - Mode transitions
   - Homing completion
   - Error conditions

b. Notifications **should** include a display time
   (`withDisplayMilliseconds()` or the `displayTimeMillis` constructor
   parameter) to avoid cluttering the dashboard.

### CODE-BUG-003 -- Error Reporting

a. `DriverStation.reportError()` **shall** be used for unexpected
   states (e.g., `default` case in enum switch).

b. `DriverStation.reportWarning()` **shall** be used for recoverable
   issues (e.g., CAN bus refresh failures).

c. Repeated warnings **shall** be rate-limited to avoid console spam.
   Use a "last status" pattern:

   ```java
   if (refreshStatus != mLastCANRefreshStatus) {
       DriverStation.reportWarning("CAN refresh: " + refreshStatus, false);
       mLastCANRefreshStatus = refreshStatus;
   }
   ```

### CODE-BUG-004 -- Runtime Tunability

a. All configurable control values **shall** be tunable at runtime via
   SmartDashboard / NetworkTables using the `LoggedNTInput` +
   `checkTuning()` pattern. This includes:
   - PID gains (P, I, D, tolerance, deadband, I zone, output range)
   - Current limits (stator and supply enable/value)
   - Voltage limits (peak forward, peak reverse)
   - Motion Magic profiles (cruise velocity, acceleration, jerk)
   - Velocity targets (RPS/RPM setpoints per mode)
   - Duty cycle targets
   - Threshold values (velocity gating, jam detection, homing current)

b. Values that use the team library classes (`PIDBase`,
   `ControllerSmart`, `TransmissionFX`) **shall** inherit tunability
   automatically via the library's built-in `checkTuning()`. Subsystem
   code **shall not** re-implement tuning for values already exposed
   by the library.

c. Values defined as `static final` constants in `Constants.java` that
   are read once at initialization and not subsequently updatable
   **should** be migrated to `LoggedNTInput` fields in the subsystem
   class, using the constant as the default value. The `checkTuning()`
   method in the subsystem's `outputTelemetry()` applies dashboard
   changes.

d. Compile-time constants **shall** remain acceptable for values that
   are physically fixed (CAN IDs, gear ratios, wheel diameter),
   timing that is structurally load-bearing (auto move sequencing),
   and safety bounds that should not be adjustable during a match
   (absolute max current, emergency stop thresholds).

e. All `LoggedNTInput` fields **shall** be initialized with safe
   default values from the corresponding `Constants` class.

f. `checkTuning()` **shall** be called at the beginning of
   `outputTelemetry()`, ensuring tuning changes are applied at the
   end of the robot cycle (after control outputs, before the next
   sensor read).

---
