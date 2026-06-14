<!-- markdownlint-disable MD007 -->
# Team271-Lib FRC Coding Standard — Safety Practices

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Safety companion to the FRC overlay's [`Standard.md`](Standard.md).
Contains `CODE-SAF-*` rules covering motor safety, state-machine
discipline, autonomous safety, CAN-bus safety, disabled-mode
behaviour, brownout / fault tolerance, vision data validation,
sustained over-current protection, and CAN partition resilience.

These rules supplement the pure-Java rules in
[`../java/Standard.md`](../java/Standard.md), which remain
applicable. Several `CODE-SAF-*` rules are the FRC-specific
application of pure-Java defensive-coding rules
(`switch` defaults, named constants, input validation);
the underlying discipline lives in
[`../java/`](../java/) and is referenced inline.

---

## 4.10 Safety Practices

<a id="code-saf-001"></a>

### CODE-SAF-001 -- Input Validation (Source: Team271-Lib)

a. Controller inputs **shall** be validated through deadbands
   and input shaping before use. Robot projects **should**
   centralise this validation in a single driver-input class.

b. Auto chooser values **shall** be validated. The `default`
   case in auto selection **shall** select a safe "do nothing"
   mode:

   ```java
   AutoPaths result = AutoPaths.NOTHING;
   switch (mAutoChooser.getSelected()) {
       case 1:
           result = AutoPaths.PATH_NAME;
           break;
       // ...
       default:
           DriverStation.reportWarning("Unknown auto: " + selected, false);
           break;
   }
   return result;
   ```

c. CAN bus signal values **shall** be validated by checking
   the `StatusCode` from CTRE refresh operations.

<a id="code-saf-002"></a>

### CODE-SAF-002 -- Motor Safety (Source: Team271-Lib)

a. All motors **shall** have stator and supply current limits
   configured at robot initialization:

   ```java
   motor.configCurrentLimitStator(
       SubsystemConstants.kStatorLimitEnabled,
       SubsystemConstants.kStatorLimitAmps);
   motor.configCurrentLimitSupply(
       SubsystemConstants.kSupplyLimitEnabled,
       SubsystemConstants.kSupplyLimitAmps);
   ```

b. Mechanisms with physical travel limits **shall** use soft
   limits that are only enabled after homing completes.

c. Homing sequences **shall** have timeout protection to
   prevent indefinite stalling. On timeout, the subsystem
   **shall** fail safe: stop motors, restore default current
   limits, transition to IDLE, notify the driver via the
   project's driver-notification facility, and leave
   `mIsHomed = false` so closed-loop control stays disabled.

   ```java
   if (mHomingTimer.hasElapsed(SubsystemConstants.kHomingTimeoutSec)) {
       /* Homing timed out -- fail safe per project safety policy */
       mMotor.stop();
       mMotor.setCurrentLimitStator(
           true, SubsystemConstants.kDefaultStatorLimit);
       /* transition to the project's safe/idle state per its
          state-machine convention */
       transitionToSafeState();
       notify.send(new Notification(
           NotifyLevel.WARNING,
           "Homing Timeout", getName() + " homing timed out"));
       // leave mIsHomed = false so closed-loop stays disabled
   }
   ```

d. All voltage and duty-cycle commands **shall** be bounded
   to safe ranges. Library motor-controller wrappers enforce
   voltage limits internally; subsystem code **shall not**
   bypass these limits.

e. Motor neutral modes (brake vs coast) **shall** be
   explicitly configured at robot initialization based on the
   mechanism requirements.

<a id="code-saf-003"></a>

### CODE-SAF-003 -- State Machine Completeness (Source: MISRA Rule 16.4, Team271-Lib)

a. Every enum `switch` **shall** handle all values, including
   a `default` case. This is the FRC application of the
   broader defensive-check discipline in
   [`../java/Standard-Methods.md`](../java/Standard-Methods.md).

b. No state **shall** be unreachable. If a state exists in
   the enum, there **shall** be a transition path to it.

c. State transitions **shall** be documented (at minimum in
   the class-level JavaDoc or the subsystem's design doc —
   not in CLAUDE.md, which is a routing index).

<a id="code-saf-004"></a>

### CODE-SAF-004 -- Subsystem Coordination (Source: Team271-Lib)

a. Cross-subsystem dependencies **shall** be documented.
   When one subsystem reads state from another (e.g.,
   SubsystemB reads `SubsystemA.isReady()`), the dependency
   and its timing requirements **shall** be documented:

   ```java
   /*
    * *** SUBSYSTEM ADD ORDER IS LOAD-BEARING ***
    * SubsystemA MUST be added before SubsystemB ...
    */
   ```

b. Auto timing **shall** be expressed through move-based
   composition (`AutoSequence`, `AutoParallel`, `WaitMove`)
   in auto-mode constructors. Parallel moves **shall not**
   command the same subsystem.

c. Subsystem registration order **shall** respect data flow:

   - Input/controller subsystems **shall** be registered first
     so that other subsystems can read their state during the
     same cycle.
   - Subsystems that produce data consumed by other subsystems
     (e.g., a subsystem whose state gates another, such as a
     shooter gating a feeder) **shall** be registered before
     their consumers.
   - Actuator subsystems that depend on sensor data from other
     subsystems **shall** be registered after their data
     sources.

<a id="code-saf-005"></a>

### CODE-SAF-005 -- Autonomous Safety (Source: Team271-Lib)

a. All auto path loading **shall** handle missing path files
   gracefully (catch exceptions, log errors, fall back to
   "do nothing").

b. The auto-mode selector **shall** always include a "Do
   Nothing" option as the default.

c. Auto timing windows **should** account for paths taking
   longer than expected (add tolerance margins).

d. Alliance flipping **shall** be tested for both red and
   blue alliance positions.

<a id="code-saf-006"></a>

### CODE-SAF-006 -- CAN Bus Safety (Source: Team271-Lib)

a. CAN signal refresh **shall** use the project's bulk
   refresh API rather than individual signal refreshes, to
   minimise CAN bus traffic.

b. CTRE control requests **shall** use timesync:

   ```java
   .withUseTimesync(true).withUpdateFreqHz(0)
   ```

c. CAN refresh status codes **shall** be checked and error
   conditions **shall** be reported, but rate-limited to
   avoid console spam (CODE-BUG-003c).

d. CANivore bus names **shall** be defined as named
   constants in the project's shared constants artifact (see
   [`../java/Standard-Modules.md`](../java/Standard-Modules.md)),
   not hard-coded in subsystem files.

<a id="code-saf-007"></a>

### CODE-SAF-007 -- Disabled Mode Safety (Source: Team271-Lib)

a. `disabledInit()` **shall** stop all motors and reset
   subsystem states to safe defaults.

b. No motor commands **shall** be sent during disabled mode.
   The per-cycle "after" hook **should** check `isDisabled()`
   before applying outputs, or subsystem state machines
   should handle the disabled case.

c. Timers used for autonomous coordination **shall** be
   stopped and reset in `disabledInit()`.

<a id="code-saf-008"></a>

### CODE-SAF-008 -- Fault Tolerance (Source: Team271-Lib)

See the project's fault-tolerance reference for detailed
behavioral expectations under each failure scenario.

a. Subsystems **shall** check `hasResetOccurred()` (or
   equivalent sticky fault) on each motor in the per-cycle
   "before" hook and, on detection, re-apply all motor
   configuration and set `isZeroed` to `false`. The driver
   **shall** be notified via the driver-notification
   facility.

b. State machines **shall not** advance based on stale
   sensor readings during or immediately after a brownout.
   Velocity gates, position checks, and current thresholds
   **shall** re-validate after voltage recovery.

c. `robotInit()` **shall** be safe to call mid-match. It
   **shall not** assume the robot is in a known physical
   state.

d. `teleopInit()` **shall** preserve homing state
   (`isZeroed`) from a prior enable cycle when the
   subsystem's motor has not rebooted (detected per rule a),
   to avoid unnecessary re-homing after a transient comms
   loss.

e. Code **shall not** assume FMS / Driver Station data is
   available at `robotInit()` or during any one-shot
   initialization. The alliance color, driver station
   position, event name, and match time may be:

   - Absent at `robotInit()` — typical in simulation, bench
     testing, or early at a competition before the FMS
     attaches.
   - Arriving mid-cycle — the Driver Station can attach
     after the robot boots.
   - Changing after first read — alliance station assignment
     can change during practice / qualification rounds.

   Any logic that depends on these values **shall** either:

   - Read the value fresh on each use (e.g., per periodic
     cycle for alliance-dependent targeting), **or**
   - Cache only inside a Driver Station event listener (or
     an equivalent "DS attached" gated callback) and re-read
     when the attachment state changes.

   Alliance-dependent configuration (path mirroring, target
   pose selection, field-oriented drive heading offset)
   **shall** handle the "alliance not yet known" case —
   WPILib returns `Optional.empty()` from
   `DriverStation.getAlliance()` — with a defined safe
   behavior, not an unchecked `.get()`.

   Match-time logic **shall** treat
   `DriverStation.getMatchTime() <= 0` as "no match in
   progress" and **shall not** drive actuator commands from
   a negative or zero match time.

   > *Industry note: FMS data arrival is an asynchronous
   > event with no guaranteed timing relative to robot code
   > startup. The WPILib API shift to `Optional<Alliance>` /
   > `OptionalInt` in 2024 made the "not yet known" state
   > explicit in the type system; `getMatchTime()`'s
   > sentinel-return (-1) is the one remaining place where a
   > runtime check is still required.*

<a id="code-saf-009"></a>

### CODE-SAF-009 -- Vision Data Validation (Source: Team271-Lib)

a. Pose estimates from vision systems (Limelight,
   PhotonVision) **shall** be checked for staleness before
   use. If the timestamp of the latest measurement has not
   changed for more than a configurable number of cycles,
   the subsystem **shall** fall back to odometry-only or
   dead-reckoning.

b. Invalid or out-of-field pose data (NaN, coordinates
   outside field bounds) **shall** be rejected and logged,
   not silently applied.

c. Vision-dependent state transitions (e.g.,
   `ALIGNING_WITH_TARGET`) **shall** have a timeout that
   falls back to the previous state if no valid vision data
   is received within the timeout window.

<a id="code-saf-010"></a>

### CODE-SAF-010 -- Sustained Over-Current Protection (Source: Team271-Lib)

a. If a motor's torque current exceeds the named
   sustained-current threshold (see (b)) for the named
   sustained duration (also (b)), the subsystem **should**
   transition to a safe/idle state per the project's
   state-machine convention and notify the driver via the
   driver-notification facility.

   This software-level check is **not** redundant with the
   CTRE firmware's `StatorCurrentLimit` / `SupplyCurrentLimit`
   configs: the firmware limits cap peaks and short-term
   averages at the motor controller, but a mechanism jammed
   or overloaded just below the configured peak can still
   sustain damage over seconds. The sustained-current
   threshold is a **separate**, typically-lower value chosen
   per-subsystem based on its duty cycle and mechanical
   limits.

b. The sustained-current threshold and duration **shall** be
   named constants in the project's shared constants
   artifact (see
   [`../java/Standard-Modules.md`](../java/Standard-Modules.md)),
   not magic numbers.

<a id="code-saf-011"></a>

### CODE-SAF-011 -- CAN Bus Partition Resilience (Source: Team271-Lib)

a. Subsystems on one CANivore bus **shall not** block or
   stall subsystems on the other bus. If the primary bus
   fails, subsystem motors on the secondary bus **shall**
   continue to operate normally, and vice versa.

b. CAN signal refresh failures **should** be detected via
   `StatusCode` checks and logged, but **shall not** throw
   exceptions or halt the robot loop.

<a id="code-saf-012"></a>

### CODE-SAF-012 -- Waiting Operation Timeouts (Source: Team271-Lib)

a. Any operation that waits for a condition (current
   threshold, velocity target, position arrival, sensor
   signal, path completion) **shall** have a timeout that
   exits the waiting state. This prevents the robot from
   locking up if the expected condition never occurs (motor
   stall not detected, a flywheel never reaching speed, a
   path waypoint unreachable).

b. The timeout **shall** be a named constant in the
   subsystem's constants class, not a magic number.

c. On timeout, the subsystem **shall** fail safe: stop
   motors, restore default current limits, and transition to
   the safe/idle state.

d. On timeout, the subsystem **shall** send a driver
   notification so the failure is visible from the driver
   station.

e. The timeout and its fail-safe behavior **shall** be
   documented in the subsystem's design doc.

Homing sequences are the most common instance of this rule;
their specific timeout outcome is governed by
[CODE-SAF-002c](#code-saf-002). Vision-gated transitions are
governed by [CODE-SAF-009c](#code-saf-009).

---
