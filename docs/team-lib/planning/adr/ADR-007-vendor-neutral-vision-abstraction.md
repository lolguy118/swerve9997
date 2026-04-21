# ADR-007: Vendor-Neutral Vision Abstraction in `api/vision/`

## Status

Accepted

## Date

2026-04-20

## Context

Team 271 robots rely on vision for AprilTag-based pose estimation and
target-relative alignment. The FRC ecosystem offers several camera
vendors — Limelight, PhotonVision, Luma P1 — each with its own Java
API shape:

- **Limelight** publishes results over NetworkTables and ships a
  team-vendored `LimelightHelpers.java` static-helper surface. It has
  no "raw device object" in the CTRE sense.
- **PhotonVision** ships a real Java library (`photonlib`) with
  `PhotonCamera`, `PhotonPoseEstimator`, and `PhotonPipelineResult`
  objects the consumer drives directly.
- **Luma P1** is a newer, less stable API that the team expects to
  adopt after its surface settles.

To date the library has no vision abstraction. `util/LimelightHelpers`
exists as a vendor-provided raw helper, but robot code that consumes
it is Limelight-specific: swapping to PhotonVision requires rewriting
every call site. The lack of a shared contract also blocks sharing
test doubles across vendors.

ADR-008 established CTRE Phoenix 6 as the primary motor and
CAN-sensor vendor. Cameras are a separate vendor ecosystem that CTRE
does not address at all — no Phoenix 6 device produces a vision
pose estimate — so ADR-008's "CTRE is primary" rule does not apply
and does not need to be revised. This ADR scopes vision as a
separate `api/` family that sits alongside (not inside) the CTRE
abstractions.

## Decision

Introduce a vendor-neutral vision abstraction at
`com.team271.lib.api.vision` with vendor implementations under
`com.team271.lib.vendor.<vendor>`, mirroring the existing
`api/motor` + `vendor/ctre` pattern:

- `api/vision/Camera` — interface extending `Named` with
  `isConnected()`, `getLatencySeconds()`,
  `getLatestPoseEstimate()`, `getLatestTarget()`, and
  `getPoseStrategy()`.
- `api/vision/PoseEstimate` — immutable record carrying pose,
  FPGA timestamp, tag count, average tag distance, single-tag
  ambiguity, and a recommended standard-deviation vector.
- `api/vision/TargetDetection` — immutable record carrying
  fiducial id, tx, ty, area, an optional target-relative 3D
  transform, and FPGA timestamp.
- `api/vision/PoseEstimateStrategy` — enum naming the estimator
  branch that produced a given `PoseEstimate` (Megatag1, Megatag2,
  multi-tag PnP, lowest-ambiguity fallback).

V1 ships two vendor implementations:

- `vendor/limelight/LimelightCamera` — a `TObj` built on the
  existing `util/LimelightHelpers`.
- `vendor/photonvision/PhotonCamera` — a `TObj` built on the
  `photonlib` vendordep.

Luma P1 and ML-based object detection are deferred; each will be
introduced via its own ADR when a concrete robot-project need
arises.

Camera measurements reach consumers via a **pull** pattern: the
robot project polls `camera.getLatestPoseEstimate()` each cycle and
forwards the result to its own pose estimator
(`SwerveDrivePoseEstimator.addVisionMeasurement`) — the library does
not own or push into a pose estimator.

`PoseEstimate` carries **both** raw confidence signals (`tagCount`,
`avgTagDistanceMeters`, `ambiguity`) **and** a vendor-computed
`recommendedStdDevs` vector. Robot projects may use the default or
derive their own trust model from the raw signals.

## Rationale

1. **Two-vendor launch is the real abstraction test.** Shipping
   only one vendor risks baking that vendor's assumptions into
   the interface. Limelight (NT-JSON, internal pose computation)
   and PhotonVision (library objects, consumer-driven
   `PhotonPoseEstimator`) differ enough that anything `Camera` can
   express cleanly across both has a realistic chance of holding
   for Luma later.
2. **Pull over push keeps the library narrow.** A pushed
   `Consumer<PoseEstimate>` would couple the vision layer to
   WPILib's pose-estimator ergonomics. Not every consumer wants
   that — auto-align commands want the latest target, not a stream
   — and `Optional`-return semantics already model "no valid
   reading this cycle" cleanly.
3. **S3 (raw signals + recommended stddev) matches the library's
   passthrough philosophy (ADR-005).** Simple robots use the
   recommended default; sophisticated robots read the raw signals
   and build their own trust model. Carrying both fields costs a
   few bytes per record and avoids a breaking change when the
   first consumer wants to tune trust.
4. **`Camera` extends `Named`, not `TObj`.** This matches `Motor`,
   `Encoder`, and `Gyro` in `api/`: the interface stays free of
   lifecycle obligations, and the vendor wrapper owns the `TObj`
   contract. Tests can provide lightweight doubles without faking
   the lifecycle.
5. **The target-relative 3D transform sits alongside tx/ty.**
   Both vendors expose it (`botpose_targetspace`, PhotonVision
   `Transform3d`), and robust "drive to tag" commands that align
   at close range need it. Making it `Optional<Transform3d>`
   keeps the contract honest when a vendor pipeline is not
   configured to produce one.

## Consequences

**Easier:**

- Robot-project control code depends on `Camera`, not a specific
  vendor — swapping Limelight for PhotonVision on a given bot
  becomes a construction change, not a rewrite.
- Test doubles (`FakeCamera`) can stand in for either vendor and
  live in `libtest`.
- PhotonVision enters the library under the same passthrough
  rule as CTRE (ADR-005): `getRawCamera()` / `getRawEstimator()`
  exposes the vendor surface when callers need features the
  `Camera` interface does not express.

**Harder:**

- Both vendor APIs must be kept in sync with the `Camera`
  contract. When one vendor adds a feature (e.g., a new Megatag
  mode), the library must either expose it through
  `Camera`/`PoseEstimate`, through the vendor wrapper's own
  methods, or through the raw passthrough.
- Adding `photonlib` introduces a new vendordep; SCMP §4
  vendordep management now covers a non-CTRE vendor.
- `LimelightCamera`'s passthrough has no single "raw object" the
  way `CTREMotor.getTalonFX()` does; its passthrough surface is
  the Limelight name string plus `LimelightHelpers` record types.
  The asymmetry needs one paragraph of explanation in
  `SDD-vision.md` §5.
- `PhotonCamera` and `LimelightCamera` constructors are
  asymmetric — PhotonVision requires an `AprilTagFieldLayout` and
  a `Transform3d robotToCamera`; Limelight does not. This is a
  property of the vendor APIs, not the abstraction, and will
  not be hidden.

## Alternatives Considered

- **New top-level `vision/` layer parallel to `hardware/`.**
  Rejected — it would add a second layering style alongside the
  existing `api/`+`vendor/` split (ADR-003), fragmenting the
  architecture for no gain.
- **Cross-cutting `vision/` module at the `sysid/`/`nt/` tier.**
  Rejected — mixes abstraction and implementation in one
  directory and loses the `api/`-vs-`vendor/` separation that
  already pays off for motors.
- **Ship Limelight-only in V1.** Rejected — a one-vendor
  abstraction is not a real test; the interface would almost
  certainly need breaking changes the first time PhotonVision
  landed.
- **Ship all three vendors (including Luma) in V1.** Rejected —
  Luma's API is still evolving; committing the library
  contract around it now risks breaking changes when Luma
  stabilizes.
- **Push via `VisionSubsystem` that owns a registered
  `Consumer<PoseEstimate>` (P3).** Rejected for V1 — opinionated
  toward `SwerveDrivePoseEstimator`; premature when every known
  caller already polls in `robotPeriodicBefore()`. Can be added
  later as a separate ADR without breaking the pull contract.
- **Expose only a recommended stddev, no raw signals (S2).**
  Rejected — forces a breaking change the first time a consumer
  needs to tune trust; the three extra record fields are cheap.
- **Expose only raw signals, no recommended stddev (S1).**
  Rejected — makes simple robots carry trust-model logic they
  do not need.

## References

- [SDD-vision.md](../sdd/SDD-vision.md)
- [SDD-api.md](../sdd/SDD-api.md)
- [SDD-util.md §3.4 LimelightHelpers](../sdd/SDD-util.md#34-limelighthelpers)
- [ADR-005](ADR-005-passthrough-wrapper-not-wall.md) — passthrough contract
- [ADR-003](ADR-003-layered-architecture.md) — layered architecture
- [ADR-008](ADR-008-ctre-phoenix6-primary-vendor.md) — motor/sensor
  vendor scope (cameras are out of scope for ADR-008)
- [SCMP.md §4 Vendordep Management](../SCMP.md#4-vendordep-management-team271-lib-specifics)
- [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md)
