---
paths:
  - "src/main/java/com/team271/lib/**"
  - "src/test/java/com/team271/lib/**"
---

# Rule: Team271-Lib Library Code

Applies to library source under `src/main/java/com/team271/lib/**`.
Merges the former `hardware-abstraction.md` and `passthrough.md`
rules into one library-scoped file.

## Layering

Team271-Lib has six layers. Each layer may depend only on layers
below it ([ADR-003](../../docs/team-lib/planning/adr/ADR-003-layered-architecture.md)):

1. `api/` — vendor-neutral interfaces (Motor, Encoder, Gyro, Camera, etc.)
2. `vendor/ctre/`, `vendor/limelight/`, `vendor/photonvision/` — vendor implementations
3. `hardware/` — TObj-lifecycle wrappers (controllers, transmissions, sensors, input)
4. `control/` — PID variants, Balance, Feedforward
5. `subsystem/` — Subsystem base, SubsystemManager, StateMachine
6. `auto/` — autonomous move composition

Cross-cutting (`nt/`, `sysid/`, `util/`, `bridge/`) may be used by any
layer but depends on none above itself.

## Rules Claude must apply

- **CTRE-focused.** The only supported motor controllers and sensors
  are CTRE Phoenix 6 devices. No WPILib PWM motors; no REV. Do not
  speculate alternative vendor implementations — build them only when
  a concrete need exists
  ([ADR-008](../../docs/team-lib/planning/adr/ADR-008-ctre-phoenix6-primary-vendor.md)).
- **`CTREMotor` is the api/ bridge, not the `Controller*` classes.**
  Do not add `api/` interface implementations to `ControllerBase`,
  `ControllerSmart`, `ControllerTalonFX`, or `ControllerTalonFXS`.
- **Passthrough — wrapper, not wall** ([ADR-005](../../docs/team-lib/planning/adr/ADR-005-passthrough-wrapper-not-wall.md)).
  Every hardware wrapper exposes its underlying vendor object via a
  public getter returning the raw CTRE type (`TalonFX`, `CANcoder`,
  `Pigeon2`, `CANrange`). Never hide the raw object behind private
  access.
- **Prefer `HardwareManager.refreshAll()`** over
  `CTREManager.refreshAll()` in robot startup code — it is the
  forward-compatible entry point.
- **Bulk CAN refresh**
  ([ADR-009](../../docs/team-lib/planning/adr/ADR-009-centralized-can-refresh.md)).
  Register StatusSignals with `CTREManager` at `robotInit()` and let
  `HardwareManager.refreshAll()` do one bulk refresh per cycle. Do
  not call `signal.refresh()` inside periodic loops.
- **No duplicate CTRE device objects on the same CAN ID.** Construct
  each device once and pass the reference.
- **Desired-to-actual pattern** ([ADR-010](../../docs/team-lib/planning/adr/ADR-010-desired-to-actual-state-pattern.md)).
  Read sensors in `robotPeriodicBefore()`; act on desired state in
  `robotPeriodicAfter()`. Never apply operator input directly to
  hardware.
- **Every `TObj` implements** `robotInit`,
  `robotPeriodicBefore`, `robotPeriodicAfter`, `outputTelemetry`.
  Don't skip `outputTelemetry()` — it is how Alerts reach the
  dashboard.
- **New feature lives in api/ or CTREMotor?** Portable features
  (gains, current limits, continuous wrap, basic closed loop) go on
  `api/ClosedLoopMotor`. CTRE-only (Motion Magic, FOC, torque
  current, timesync) go on `CTREMotor` directly or via passthrough.
- **Vision is a separate vendor family** ([ADR-007](../../docs/team-lib/planning/adr/ADR-007-vendor-neutral-vision-abstraction.md)).
  Portable vision features (pose, target tx/ty, connection, stddev)
  go on `api/vision/Camera` / `PoseEstimate` / `TargetDetection`.
  Vendor-only features (Limelight pipeline switch, Photon
  `PhotonPoseEstimator` tuning) go on the vendor wrapper or via
  passthrough. Cameras pull — the library does not push estimates
  into a pose estimator. Empty `Optional` is the fail-safe; do not
  return stale or out-of-policy readings.
- **Deprecation lifecycle.** When deprecating a public symbol (class,
  method, field) in `api/`, add its simple name to
  [`../rules/deprecated-symbols.txt`](deprecated-symbols.txt) in the
  same commit. The
  [`check-deleted-class-refs.sh`](../hooks/check-deleted-class-refs.sh)
  hook then flags lingering doc references so they get cleaned up
  before the symbol is removed. Do the same *before* deletion: add to
  the list, wait one release, then remove the code.

## Authoritative docs

- [SDD-team271-lib.md](../../docs/team-lib/planning/sdd/SDD-team271-lib.md)
- [SDD-vendor-ctre.md](../../docs/team-lib/planning/sdd/SDD-vendor-ctre.md)
- [SDD-hardware.md](../../docs/team-lib/planning/sdd/SDD-hardware.md)
- [SDD-vision.md](../../docs/team-lib/planning/sdd/SDD-vision.md)
- ADRs
  [005](../../docs/team-lib/planning/adr/ADR-005-passthrough-wrapper-not-wall.md),
  [003](../../docs/team-lib/planning/adr/ADR-003-layered-architecture.md),
  [008](../../docs/team-lib/planning/adr/ADR-008-ctre-phoenix6-primary-vendor.md),
  [009](../../docs/team-lib/planning/adr/ADR-009-centralized-can-refresh.md),
  [010](../../docs/team-lib/planning/adr/ADR-010-desired-to-actual-state-pattern.md),
  [007](../../docs/team-lib/planning/adr/ADR-007-vendor-neutral-vision-abstraction.md)
