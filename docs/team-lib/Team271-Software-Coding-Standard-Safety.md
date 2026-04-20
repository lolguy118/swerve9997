<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

### 4.9 Safety Practices

> *Industry note: The safety rules in this section reflect the philosophy
> of DO-178C, which requires that safety-critical software be
> deterministic, defensive, and traceable. DO-178C is the standard used
> to certify the software in every commercial airplane you have ever
> flown on. We are not building flight software, but the same mindset
> applies: the robot must not do something unexpected during a match.*

#### CODE-SAF-001 -- Input Validation

a. Controller inputs **shall** be validated through deadbands and
   input shaping before use. The `InputDriver` class centralizes
   this validation.

b. Auto chooser values **shall** be validated. The `default` case
   in auto selection **shall** select a safe "do nothing" mode:

   ```java
   switch (mAutoChooser.getSelected()) {
       case 1: return AutoPaths.PATH_NAME;
       // ...
       default:
           DriverStation.reportWarning("Unknown auto: " + selected, false);
           return AutoPaths.NOTHING;
   }
   ```

c. CAN bus signal values **shall** be validated by checking the
   `StatusCode` from CTRE refresh operations.

#### CODE-SAF-002 -- Motor Safety

a. All motors **shall** have stator and supply current limits
   configured in `robotInit()`:

   ```java
   transmission.configCurrentLimitStator(
       ExampleSubsystemConstants.kExampleStatorLimitEnabled,
       ExampleSubsystemConstants.kExampleCurrentStatorLimit);   // int widens to double
   transmission.configCurrentLimitSupply(
       ExampleSubsystemConstants.kExampleSupplyLimitEnabled,
       ExampleSubsystemConstants.kExampleCurrentSupplyLimit);   // int widens to double
   ```

b. Mechanisms with physical travel limits **shall** use soft limits
   that are only enabled after homing completes.

c. Homing sequences **shall** have timeout protection to prevent
   indefinite stalling:

   ```java
   if (mHomingTimer.hasElapsed(ExampleSubsystemConstants.kHomingTimeoutSec)) {
       /* Homing timed out -- assume position and continue */
       DriverStation.reportError("Subsystem homing timed out", false);
       mIsHomed = true;
   }
   ```

d. All voltage and duty-cycle commands **shall** be bounded to safe
   ranges. The `TransmissionFX` class enforces voltage limits
   internally; subsystem code **shall** not bypass these limits.

e. Motor neutral modes (brake vs coast) **shall** be explicitly
   configured in `robotInit()` based on the mechanism requirements.

#### CODE-SAF-003 -- State Machine Completeness

a. Every enum `switch` **shall** handle all values including a
   `default` case (CODE-FUN-006a).

b. No state **shall** be unreachable. If a state exists in the enum,
   there **shall** be a transition path to it.

c. State transitions **shall** be documented (at minimum in the
   class-level JavaDoc or CLAUDE.md).

#### CODE-SAF-004 -- Subsystem Coordination

a. Cross-subsystem dependencies **shall** be documented. When one
   subsystem reads state from another (e.g., SubsystemB reads
   `SubsystemA.isReady()`), the dependency and its timing
   requirements **shall** be documented:

   ```java
   /*
    * *** SUBSYSTEM ADD ORDER IS LOAD-BEARING ***
    * SubsystemA MUST be added before SubsystemB ...
    */
   ```

b. Auto timing **shall** be expressed through move-based composition
   (`AutoMoveSequence`, `AutoMoveParallel`, `WaitMove`) in auto mode
   constructors. Parallel moves **shall** not command the same subsystem.

#### CODE-SAF-005 -- Autonomous Safety

a. All auto path loading **shall** handle missing path files
   gracefully (catch exceptions, log errors, fall back to "do nothing").

b. The auto mode selector **shall** always include a "Do Nothing"
   option as the default.

c. Auto timing windows **should** account for paths taking longer
   than expected (add tolerance margins).

d. Alliance flipping **shall** be tested for both red and blue
   alliance positions.

#### CODE-SAF-006 -- CAN Bus Safety

a. CAN signal refresh **shall** use `CTREManager.refreshAll()` for
   bulk refresh rather than individual signal refreshes, to minimize
   CAN bus traffic.

b. CTRE control requests **shall** use timesync:
   ```java
   .withUseTimesync(true).withUpdateFreqHz(0)
   ```

c. CAN refresh status codes **shall** be checked and error conditions
   **shall** be reported, but rate-limited to avoid console spam
   (CODE-BUG-003c).

d. CANivore bus names **shall** be defined as constants in
   `Constants.java`, not hard-coded in subsystem files.

#### CODE-SAF-007 -- Disabled Mode Safety

a. `disabledInit()` **shall** stop all motors and reset subsystem
   states to safe defaults.

b. No motor commands **shall** be sent during disabled mode. The
   `robotPeriodicAfter()` implementation **should** check
   `isDisabled()` before applying outputs, or subsystem state
   machines should handle the disabled case.

c. Timers used for autonomous coordination **shall** be stopped
   and reset in `disabledInit()`.

#### CODE-SAF-008 -- Fault Tolerance

See [Fault Tolerance](robot-<year>-reference.md#fault-tolerance) for
detailed behavioral expectations under each failure scenario.

a. Subsystems **shall** check `hasResetOccurred()` (or equivalent
   sticky fault) on each motor in `robotPeriodicBefore()` and, on
   detection, re-apply all motor configuration and set `isZeroed`
   to `false`. The driver **shall** be notified via Elastic.

b. State machines **shall** not advance based on stale sensor readings
   during or immediately after a brownout. Velocity gates, position
   checks, and current thresholds **shall** re-validate after voltage
   recovery.

c. `robotInit()` **shall** be safe to call mid-match. It **shall** not
   assume the robot is in a known physical state.

d. `teleopInit()` **shall** preserve homing state (`isZeroed`) from a
   prior enable cycle when the subsystem's motor has not rebooted
   (detected per rule a), to avoid unnecessary re-homing after a
   transient comms loss.

#### CODE-SAF-009 -- Vision Data Validation

a. Pose estimates from vision systems (Limelight, PhotonVision) **shall**
   be checked for staleness before use. If the timestamp of the latest
   measurement has not changed for more than a configurable number of
   cycles, the subsystem **shall** fall back to odometry-only or
   dead-reckoning.

b. Invalid or out-of-field pose data (NaN, coordinates outside field
   bounds) **shall** be rejected and logged, not silently applied.

c. Vision-dependent state transitions (e.g., ALIGNING_WITH_HUB)
   **shall** have a timeout that falls back to the previous state if
   no valid vision data is received within the timeout window.

#### CODE-SAF-010 -- Sustained Over-Current Protection

a. If a motor's torque current exceeds its configured supply current
   limit for a sustained period (defined by a named constant), the
   subsystem **should** transition to IDLE and notify the driver via
   Elastic. This protects against motor jam scenarios where the hardware
   current limit alone may not prevent mechanism damage.

b. The sustained-current threshold and duration **shall** be named
   constants in the subsystem's Constants class, not magic numbers.

#### CODE-SAF-011 -- CAN Bus Partition Resilience

a. Subsystems on one CANivore bus **shall not** block or stall
   subsystems on the other bus. If the primary bus fails, subsystem
   motors on `"Subsystems"` **shall** continue to operate normally,
   and vice versa.

b. CAN signal refresh failures **should** be detected via
   `StatusCode` checks and logged, but **shall not** throw exceptions
   or halt the robot loop.

---
