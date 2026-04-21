# Software Requirements Specification

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SRS |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |

> **Scope note:** This SRS captures API contracts and non-functional
> guarantees for Team271-Lib as a reusable library. It does **not**
> capture robot-project requirements (e.g., "the robot shall score a
> note"). Those belong in each robot project's own SRS.
>
> Requirement IDs (`[LIB-NNN]`, `[API-NNN]`, etc.) are scoped. They
> appear only in: this SRS, the SVP traceability matrix, each SDD's
> header-table "Requirements Traced" row, and each SDD's §9
> "Test Coverage Requirements" test-ID listing. They are not copied
> into SDD prose, guides, CLAUDE.md, or code comments outside
> test-method Javadoc.
>
> Normative keywords (SHALL, SHOULD, MAY) follow the convention in
> [`../../common/planning/README.md`](../../common/planning/README.md#normative-keywords).

## 1. Purpose and Scope

This document captures what Team271-Lib guarantees to robot projects
that depend on it. It includes:

- **API contracts** — what the library's public types guarantee.
- **Non-functional guarantees** — timing, thread safety, fault
  behavior, exception isolation.
- **Cross-layer interface constraints** — the dependency rules that
  make the architecture tractable.

It excludes anything that is robot-specific.

## 2. Applicable Documents

| Document | Purpose |
| -------- | ------- |
| [Team271-Software-Coding-Standard.md](../../common/coding-standard/Team271-Software-Coding-Standard.md) | Normative coding rules |
| [SDP.md](SDP.md) | Development phases and toolchain |
| [SVP.md](SVP.md) | Verification strategy |
| [ADR-003](adr/ADR-003-passthrough-wrapper-not-wall.md) | Passthrough contract |
| [ADR-004](adr/ADR-004-layered-architecture.md) | Layering rules |
| [ADR-007](adr/ADR-007-centralized-can-refresh.md) | CAN refresh contract |
| [ADR-010](adr/ADR-010-subsystem-exception-isolation.md) | Exception isolation |
| [ADR-011](adr/ADR-011-mandatory-timeouts-fail-safe.md) | Timeout contract |
| [ADR-014](adr/ADR-014-desired-to-actual-state-pattern.md) | State pattern |
| [ADR-016](adr/ADR-016-vendor-neutral-vision-abstraction.md) | Vision abstraction |

## 3. Library-Level Requirements (`[LIB-NNN]`)

- **[LIB-001]** Every TObj subclass shall implement `robotInit()`,
  `robotPeriodicBefore()`, `robotPeriodicAfter()`, and
  `outputTelemetry()` lifecycle hooks.
- **[LIB-002]** `SubsystemManager.forEachSafe()` shall wrap each
  registered subsystem's periodic call in a try-catch such that an
  exception in one subsystem does not propagate to others
  (ADR-010).
- **[LIB-003]** `HardwareManager.refreshAll()` shall be called exactly
  once per robot periodic cycle (in `robotPeriodicBefore()`) and
  shall perform a single bulk refresh of all registered signals
  (ADR-007).
- **[LIB-004]** Library code shall not allocate objects in periodic
  methods (per CODE-GEN-004).
- **[LIB-005]** Library code shall not require robot-project classes
  to inherit from framework types beyond `Subsystem`, `AutoMove`, and
  their standard subclasses.

## 4. Per-Layer Requirements

### 4.1 API Layer (`[API-NNN]`)

- **[API-001]** The `Motor` interface shall provide duty-cycle, voltage,
  and stop commands; implementations shall clamp inputs to hardware
  limits.
- **[API-002]** The `ClosedLoopMotor` interface shall extend `Motor`
  with position and velocity closed-loop commands, gain configuration,
  and current-limit configuration.
- **[API-003]** The `Encoder` interface shall provide relative
  position and velocity readings in mechanism units.
- **[API-004]** The `AbsoluteEncoder` interface shall provide an
  absolute position reading with configurable zero offset.
- **[API-005]** The `Gyro` interface shall provide angle and rotation
  rate readings.
- **[API-006]** The `LimitSwitch` interface shall provide a debounced
  boolean reading.
- **[API-007]** The `RangeSensor` interface shall provide distance in
  meters.
- **[API-008]** `SignalRefreshable` implementations shall return
  bounded latency on `refresh()` (no blocking waits beyond the
  vendor's normal CAN read).
- **[API-009]** API interface methods shall document their unit
  conventions in Javadoc (rotations, radians, meters, seconds).

### 4.2 Vendor-CTRE Layer (`[CTRE-NNN]`)

- **[CTRE-001]** `CTREMotor` shall implement `ClosedLoopMotor`.
- **[CTRE-002]** Every CTRE wrapper shall expose its underlying
  vendor object via a public getter (ADR-003).
- **[CTRE-003]** Passthrough getters shall return raw CTRE types
  (`TalonFX`, `CANcoder`, `Pigeon2`, `CANrange`), not wrapped types.
- **[CTRE-004]** All CTRE StatusSignals used by the library shall be
  registered with `CTREManager` at `robotInit()`.
- **[CTRE-005]** `CTREManager.refreshAll()` shall provide
  timestamp-consistent signal values to all callers within a single
  robot cycle.
- **[CTRE-006]** `CommandBridge.asAutoMove()` shall require an
  explicit timeout argument (ADR-011); callers cannot construct one
  without it.

### 4.3 Hardware Layer (`[HW-NNN]`)

- **[HW-001]** The controller hierarchy shall be
  `ControllerBase → ControllerSmart → ControllerTalonFX/FXS`. The
  controllers shall not implement `api/` interfaces directly;
  `CTREMotor` is the api/ bridge.
- **[HW-002]** `TransmissionBase` subclasses shall apply current-limit,
  direction, and neutral-mode configuration to all motors in the
  transmission (leader + followers).
- **[HW-003]** `applyConfig()` on a `TransmissionBase` shall retry up
  to `CAN_RETRY_COUNT` times with a per-attempt timeout; on total
  failure, `isConfigured` shall be set to `false` and the motor shall
  operate with Phoenix 6 factory defaults.
- **[HW-004]** Two hardware wrappers shall not be constructed with the
  same `CANDeviceID`.
- **[HW-005]** `Transmission` follower validation shall verify that
  leader and follower are on the same physical CAN bus and return
  `ERROR_INVALID_BUS` otherwise.
- **[HW-006]** `Input` devices shall return `0.0` (or equivalent neutral
  value) when the controller is disconnected.
- **[HW-007]** `FaultMonitor` shall track CTRE sticky faults for
  every device it is attached to and surface active faults via
  `Alert.ERROR` + NT telemetry.

### 4.4 Control Layer (`[CTL-NNN]`)

- **[CTL-001]** All PID variants (`PIDSimple`, `PIDTrap`, `PIDWPI`,
  `PIDWPI_Trap`, `PIDFX`) shall share a common `PIDController`
  interface; swapping implementations shall require only constructor
  changes.
- **[CTL-002]** `PIDBase` shall clamp controller output to a
  configured `[minOutput, maxOutput]` range.
- **[CTL-003]** `PIDBase` shall bound integrator wind-up with
  configurable `[iMin, iMax]` and `iZone`.
- **[CTL-004]** Continuous-mode PID shall wrap error to the
  configured range on each call.
- **[CTL-005]** `atSetpoint()` shall return true only when the
  absolute error is within the configured tolerance.
- **[CTL-006]** When `PIDFX` initialization fails, the caller may
  reconstruct the controller with a software PID variant
  (`PIDSimple`, `PIDTrap`, `PIDWPI`, `PIDWPI_Trap`). The library
  does not automate this fallback; fallback policy is
  robot-project scoped (see SDD-control.md §Error Handling).

### 4.5 Subsystem Layer (`[SUB-NNN]`)

- **[SUB-001]** Every `Subsystem` shall separate desired state from
  actual state (ADR-014).
- **[SUB-002]** Sensors shall be read in `robotPeriodicBefore()`;
  actuation shall occur in `robotPeriodicAfter()`.
- **[SUB-003]** `SubsystemManager.forEachSafe()` shall log caught
  exceptions to the Driver Station and send throttled Elastic
  notifications; the per-subsystem throttle window is a named
  constant in `SubsystemManager` (see SDD-subsystem).
- **[SUB-004]** `SubsystemManager.robotInit()` shall **not** isolate
  exceptions — an init failure shall be fatal.
- **[SUB-005]** Homing sequences shall have a named timeout constant,
  a fail-safe action on timeout, and an Elastic alert on timeout
  (ADR-011).

### 4.6 Auto Layer (`[AUT-NNN]`)

- **[AUT-001]** `AutoMove` subclasses shall be independent — no shared
  state between moves.
- **[AUT-002]** `AutoMoveConditional` shall require an explicit
  timeout argument in its constructor.
- **[AUT-003]** `AutoMoveConditional` timeout shall trigger a
  fail-safe action and an Elastic WARNING notification.
- **[AUT-004]** `AutoMode.init()` shall initialize the first move;
  `AutoMode.periodic()` shall delegate to the currently executing
  move.
- **[AUT-005]** PathPlanner integration shall occur via
  `CommandBridge.asAutoMove()` (not by direct `CommandScheduler`
  invocation).

### 4.7 SysID Layer (`[SID-NNN]`)

- **[SID-001]** `Logger` and `LoggerGeneral` shall pre-allocate data
  vectors to a named default capacity (see SDD-sysid); no allocation
  shall occur after `robotInit()` returns.
- **[SID-002]** SysID logging threads shall run at priority 15 (below
  HAL's 40) to avoid starving the CAN bus.
- **[SID-003]** On vector overflow, SysID loggers shall stop recording
  and post an `Alert.WARNING`; previously recorded data shall remain
  valid.
- **[SID-004]** SysID output shall be AdvantageKit-compatible
  (`.wpilog` format).

### 4.8 NT Layer (`[NT-NNN]`)

- **[NT-001]** `LoggedNTInput` shall read from NetworkTables and
  simultaneously write to the AdvantageKit log on every `get()`
  (ADR-008).
- **[NT-002]** `LoggedNTInput` shall return the configured default
  when NetworkTables is disconnected.
- **[NT-003]** `NTTable` shall scope all entries under a configurable
  prefix (typically the subsystem name).

### 4.9 Util Layer (`[UTL-NNN]`)

- **[UTL-001]** `Alert` instances shall be persistent — once set,
  state shall remain until explicitly cleared.
- **[UTL-002]** Active `Alert` instances shall be output to
  AdvantageKit via `Logger.recordOutput()` in `outputTelemetry()`.
- **[UTL-003]** `Elastic.sendNotification()` shall be fire-and-forget;
  a send failure shall not throw to the caller.
- **[UTL-004]** `DriveSignal` shall be immutable.
- **[UTL-005]** `Util` math helpers shall be stateless and
  thread-safe.

### 4.10 Vision Layer (`[VIS-NNN]`)

- **[VIS-001]** The `Camera` interface shall expose the latest
  pose estimate via `Optional<PoseEstimate>`; an empty `Optional`
  shall indicate stale, disconnected, or out-of-policy readings.
- **[VIS-002]** The `Camera` interface shall expose the latest
  target via `Optional<TargetDetection>`, with an empty `Optional`
  when no target is visible.
- **[VIS-003]** `Camera` implementations shall report connection
  state via `isConnected()`; a stale reading within a single
  periodic cycle shall yield an empty `Optional` from the getters
  without flipping `isConnected()` to `false`.
- **[VIS-004]** `PoseEstimate` shall carry tag count, average
  tag distance, single-tag ambiguity (or `NaN` when multi-tag),
  and a vendor-computed recommended standard-deviation vector.
- **[VIS-005]** Each vendor camera wrapper shall expose its raw
  vendor surface via passthrough getters (ADR-003); for vendors
  without a raw device object (e.g., Limelight), the passthrough
  shall return the vendor-specific handle needed to call the
  vendor's helper API directly.

## 5. Cross-Layer Interface Requirements (`[INT-NNN]`)

- **[INT-001]** `api/` shall not depend on any other library package.
- **[INT-002]** `vendor/ctre/` shall depend only on `api/` and on
  CTRE Phoenix 6.
- **[INT-003]** `hardware/` shall depend only on `vendor/ctre/`,
  `api/`, and cross-cutting packages.
- **[INT-004]** `control/` shall depend only on `hardware/`, `api/`,
  and cross-cutting packages.
- **[INT-005]** `subsystem/` shall depend only on `control/`,
  `hardware/`, and cross-cutting packages.
- **[INT-006]** `auto/` shall depend only on `subsystem/` and
  cross-cutting packages.
- **[INT-007]** Cross-cutting packages (`nt/`, `sysid/`, `util/`)
  shall not depend on `hardware/`, `control/`, `subsystem/`, or
  `auto/`.
- **[INT-008]** `vendor/limelight/` shall depend only on
  `api/vision/`, `util/LimelightHelpers`, and WPILib types; it
  shall not depend on `hardware/`, `control/`, `subsystem/`, or
  `auto/`.
- **[INT-009]** `vendor/photonvision/` shall depend only on
  `api/vision/`, WPILib types, and the `photonlib` vendordep; it
  shall not depend on `hardware/`, `control/`, `subsystem/`, or
  `auto/`.

## 6. Non-Functional Constraints

Non-functional constraints binding on library code (no periodic
allocation, bounded loops, mandatory timeouts, `switch default`
branches, null-checks on public API, no `Thread.sleep()` in periodic,
no silent `InterruptedException`) are inherited from the Coding
Standard (see §2 Applicable Documents). The SRS does not re-list
them; any change to library-binding constraints happens there.

## 7. Traceability Matrix

The matrix below maps requirements to SDD sections and expected
test-case identifiers. Verification status lives in the test tree
and CI reports; update the test-case column as tests are added.

| Requirement | SDD Section | Test Case |
| ----------- | ----------- | --------- |
| `[LIB-001]` | SDD-team271-lib §3 | `[TEST-LIB-001]` |
| `[LIB-002]` | SDD-subsystem §3.2 | `[TEST-SUB-003]` |
| `[LIB-003]` | SDD-hardware §3.5 | `[TEST-HW-010]` |
| `[API-001..009]` | SDD-api §3 | `[TEST-API-*]` |
| `[CTRE-001]` | SDD-vendor-ctre §3 | `[TEST-CTRE-001]` |
| `[CTRE-002]`, `[CTRE-003]` | SDD-vendor-ctre §6 | `[TEST-CTRE-002]` |
| `[CTRE-004]`, `[CTRE-005]` | SDD-hardware §3.5 | `[TEST-HW-010]` |
| `[CTRE-006]` | SDD-vendor-ctre §3 | `[TEST-CTRE-005]` |
| `[HW-001..007]` | SDD-hardware §3 | `[TEST-HW-*]` |
| `[CTL-001..006]` | SDD-control §3 | `[TEST-CTL-*]` |
| `[SUB-001..006]` | SDD-subsystem §3 | `[TEST-SUB-*]` |
| `[AUT-001..005]` | SDD-auto §3 | `[TEST-AUT-*]` |
| `[SID-001..004]` | SDD-sysid §3 | `[TEST-SID-*]` |
| `[NT-001..003]` | SDD-nt §3 | `[TEST-NT-*]` |
| `[UTL-001..005]` | SDD-util §3 | `[TEST-UTL-*]` |
| `[VIS-001..005]` | SDD-vision §3 | `[TEST-VIS-*]` |
| `[INT-001..009]` | (package-info.java imports) | (static check) |
