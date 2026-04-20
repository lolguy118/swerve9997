# SDD: `com.team271.lib.api.vision` + `com.team271.lib.vendor.limelight` + `com.team271.lib.vendor.photonvision` — Vision

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-VISION |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | `[VIS-001]`..`[VIS-005]` (SRS §4.10) |

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

> **Status: Planned — Not Yet Implemented.** The vision packages
> described here do not yet exist in `src/main/java/`. This SDD
> documents the contract authored by
> [ADR-016](../adr/ADR-016-vendor-neutral-vision-abstraction.md);
> it is binding on the first change that introduces a
> `com.team271.lib.api.vision` class.

## 1. Purpose

Defines the vendor-neutral vision abstraction that allows robot
projects to consume AprilTag pose estimates and target detections
without depending on a specific camera vendor. Covers the `api/vision`
interfaces and the `vendor/limelight` and `vendor/photonvision`
implementations that ship in V1.

## 2. Scope and Boundaries

This SDD covers:

- `api/vision/Camera` — vendor-neutral vision surface
- `api/vision/PoseEstimate` — immutable pose-estimate record
- `api/vision/TargetDetection` — immutable target record
- `api/vision/PoseEstimateStrategy` — estimator-branch enum
- `vendor/limelight/LimelightCamera` — Limelight `Camera`
  implementation backed by
  [`util/LimelightHelpers`](SDD-util.md)
- `vendor/photonvision/PhotonCamera` — PhotonVision `Camera`
  implementation backed by the `photonlib` vendordep
- Passthrough getter inventory for the vision layer (§6)

## 3. Module Decomposition

### 3.1 `api/vision/Camera`

Vendor-neutral vision interface extending `Named`. Does not
extend `TObj` — lifecycle obligations live on the vendor
wrapper, matching the pattern used by `Motor`, `Encoder`, and
`Gyro` in `api/`.

| Method | Returns | Notes |
| ------ | ------- | ----- |
| `isConnected()` | `boolean` | False when the camera has produced no valid reading within a named staleness window |
| `getLatencySeconds()` | `double` | Camera-reported capture-to-NT latency; zero when unknown |
| `getLatestPoseEstimate()` | `Optional<PoseEstimate>` | Fresh-per-cycle; empty when stale, disconnected, or rejected by the vendor's pre-publish filters |
| `getLatestTarget()` | `Optional<TargetDetection>` | Best / primary target; empty when none visible |
| `getPoseStrategy()` | `PoseEstimateStrategy` | Identifies which estimator branch produced the last non-empty estimate |

### 3.2 `api/vision/PoseEstimate`

Immutable record carrying a single pose estimate.

| Field | Type | Meaning |
| ----- | ---- | ------- |
| `pose` | `Pose2d` | Field-relative robot pose |
| `timestampSeconds` | `double` | FPGA timestamp, suitable for `addVisionMeasurement` |
| `tagCount` | `int` | Number of tags contributing to the estimate |
| `avgTagDistanceMeters` | `double` | Mean tag distance at capture time |
| `ambiguity` | `double` | Single-tag ambiguity in `[0.0, 1.0]`; `NaN` when multi-tag |
| `recommendedStdDevs` | `Vector<N3>` | Vendor-computed default standard deviations `(x, y, theta)` |

Raw confidence signals and the recommended standard-deviation
vector are both present by design (ADR-016: S3): simple robots
use the recommended default; sophisticated robots derive their
own trust model from the raw signals.

### 3.3 `api/vision/TargetDetection`

Immutable record describing a single target.

| Field | Type | Meaning |
| ----- | ---- | ------- |
| `fiducialId` | `int` | AprilTag ID; `-1` for non-fiducial targets |
| `tx` | `double` | Horizontal offset from principal axis (degrees) |
| `ty` | `double` | Vertical offset from principal axis (degrees) |
| `area` | `double` | Target area in `[0.0, 100.0]` percent of image |
| `robotToTarget` | `Optional<Transform3d>` | Target-relative 3D transform; empty when the vendor pipeline is not producing one |
| `timestampSeconds` | `double` | FPGA timestamp |

### 3.4 `api/vision/PoseEstimateStrategy`

Closed enum identifying the estimator branch that produced the
most recent non-empty `PoseEstimate`.

| Value | Produced By |
| ----- | ----------- |
| `MEGATAG1` | Limelight Megatag1 |
| `MEGATAG2` | Limelight Megatag2 (gyro-fused) |
| `MULTI_TAG_PNP` | PhotonVision multi-tag PnP |
| `LOWEST_AMBIGUITY` | PhotonVision single-tag fallback |

### 3.5 `vendor/limelight/LimelightCamera`

`TObj` + `Camera` implementation for Limelight devices, backed
by the existing `util/LimelightHelpers` static surface.

| Method (beyond `Camera`) | Purpose |
| ------------------------ | ------- |
| `getLimelightName()` | Returns the NT table name for direct `LimelightHelpers` calls (passthrough) |
| `getRawMegatag2Estimate()` | Returns the last raw `LimelightHelpers.PoseEstimate` (passthrough) |
| `getRawFiducials()` | Returns the last raw `LimelightHelpers.LimelightTarget_Fiducial[]` (passthrough) |
| `setRobotOrientation(yawDeg)` | Feeds robot yaw into Megatag2 each cycle |
| `setPipeline(index)` | Switches the active Limelight pipeline |
| `setPriorityTagId(id)` | Biases single-tag selection; `-1` clears |

Construction takes an NT table name and a `LimelightCameraConfig`
carrying named tuning constants (min tag count, max average tag
distance, max ambiguity, distance-to-stddev coefficient, strategy,
whether to push robot orientation each cycle). Defaults live in
`LimelightCameraConfig` — never in this SDD (per
[`.claude/rules/docs.md`](../../../../.claude/rules/docs.md)).

The pre-periodic hook reads the Megatag estimate and fiducial
array **once per cycle** via `LimelightHelpers`, applies the
pre-publish rejection filter, and caches a `PoseEstimate` +
`TargetDetection` pair. The latest-accessor getters return those
cached values wrapped in `Optional`. The post-periodic hook is a
no-op unless `updateRobotOrientation` is enabled, in which case it
pushes the latest gyro yaw back to the Limelight.

### 3.6 `vendor/photonvision/PhotonCamera`

`TObj` + `Camera` implementation backed by `photonlib`.

| Method (beyond `Camera`) | Purpose |
| ------------------------ | ------- |
| `getRawCamera()` | Returns `org.photonvision.PhotonCamera` (passthrough) |
| `getRawEstimator()` | Returns the configured `PhotonPoseEstimator` (passthrough) |
| `getLatestResult()` | Returns the last `PhotonPipelineResult` (passthrough) |

Construction takes an NT name, a `PhotonCameraConfig`, an
`AprilTagFieldLayout`, and a `Transform3d robotToCamera`. The
asymmetry with `LimelightCamera` is intentional — PhotonVision
requires the field layout and camera-to-robot transform because
the consumer computes pose; Limelight computes pose internally.

The pre-periodic hook pulls the latest `PhotonPipelineResult`, runs
the configured `PhotonPoseEstimator` with
`MULTI_TAG_PNP_ON_COPROCESSOR` (fallback: `LOWEST_AMBIGUITY`),
applies the rejection filter, and caches a `PoseEstimate` +
`TargetDetection` pair. The strategy enum reflects whichever branch
produced the cached estimate.

## 4. Data Flow

```text
// Cycle ingest — robotPeriodicBefore (ADR-014 desired-to-actual)
subsystem.robotPeriodicBefore()
  → camera.robotPeriodicBefore()
    // LimelightCamera
    → LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(name)
    → LimelightHelpers.getRawFiducials(name)
    → apply LimelightCameraConfig filter → cache Optional<PoseEstimate>
                                          + Optional<TargetDetection>
    // PhotonCamera
    → photonCamera.getLatestResult()
    → poseEstimator.update(result)
    → apply PhotonCameraConfig filter → cache Optional<PoseEstimate>
                                      + Optional<TargetDetection>

// Consumption — robot project pulls and forwards (ADR-016: P1)
subsystem.robotPeriodicBefore()
  → camera.getLatestPoseEstimate().ifPresent(est ->
      poseEstimator.addVisionMeasurement(
          est.pose(), est.timestampSeconds(), est.recommendedStdDevs()))

// Target alignment — auto/teleop command
autoAlign.execute()
  → camera.getLatestTarget().ifPresent(t -> {
      // Close-range: prefer t.robotToTarget() when present
      // Far-range: tx/ty alignment is sufficient
      headingController.setGoal(-t.tx());
      driveSubsystem.setDesiredOmega(headingController.calculate());
    })
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| `Camera` extends `Named`, not `TObj` | Matches `Motor`/`Encoder`/`Gyro`; keeps `api/` lifecycle-free | [ADR-016](../adr/ADR-016-vendor-neutral-vision-abstraction.md) |
| Two-vendor V1 (Limelight + PhotonVision) | A one-vendor abstraction is not a real test of the contract | [ADR-016](../adr/ADR-016-vendor-neutral-vision-abstraction.md) |
| Pull via `Optional`, not pushed `Consumer` | Keeps library narrow; not every caller uses `SwerveDrivePoseEstimator` | [ADR-016](../adr/ADR-016-vendor-neutral-vision-abstraction.md) |
| Both raw signals and recommended stddev on `PoseEstimate` | Default for simple robots; escape hatch for tuned ones | [ADR-016](../adr/ADR-016-vendor-neutral-vision-abstraction.md) |
| Passthrough per vendor (ADR-003) | Preserves access to vendor-specific features not on `Camera` | [ADR-003](../adr/ADR-003-passthrough-wrapper-not-wall.md) |
| Limelight passthrough is NT-name + helper records | Limelight has no "raw object" like `TalonFX` | See §6 |
| Per-camera pre-periodic read | Matches desired-to-actual + bulk-refresh discipline | [ADR-014](../adr/ADR-014-desired-to-actual-state-pattern.md), [ADR-007](../adr/ADR-007-centralized-can-refresh.md) |
| Record allocation per cycle is acceptable | ~48 bytes × cameras × 50 Hz; CODE-GEN-004 targets unbounded allocation in hot loops, not small value records returned from cached state | [CODE-GEN-004](../../../common/Team271-Software-Coding-Standard-General.md) |

## 6. Passthrough Getter Reference

Every vision wrapper provides passthrough access to its vendor
surface per [ADR-003](../adr/ADR-003-passthrough-wrapper-not-wall.md).
Because Limelight has no "raw device object" comparable to a CTRE
`TalonFX`, its passthrough is the NT table name plus the
`LimelightHelpers` record types — direct `LimelightHelpers.*(name,
...)` calls are the escape hatch for Limelight-only features. A
future vendor with no Java library (e.g., Luma P1 at its current
maturity) is expected to follow the same pattern.

| Class | Method | Returns | Notes |
| ----- | ------ | ------- | ----- |
| `LimelightCamera` | `getLimelightName()` | `String` | NT table name; pair with direct `LimelightHelpers` calls |
| `LimelightCamera` | `getRawMegatag2Estimate()` | `LimelightHelpers.PoseEstimate` | Last raw Megatag2 record (nullable when stale) |
| `LimelightCamera` | `getRawFiducials()` | `LimelightHelpers.LimelightTarget_Fiducial[]` | Last raw fiducial array |
| `PhotonCamera` | `getRawCamera()` | `org.photonvision.PhotonCamera` | Raw photonlib camera object |
| `PhotonCamera` | `getRawEstimator()` | `PhotonPoseEstimator` | Configured pose estimator |
| `PhotonCamera` | `getLatestResult()` | `PhotonPipelineResult` | Last pipeline result |

Every new vision wrapper added under `vendor/<vendor>/` shall
provide at least one passthrough getter returning the raw vendor
surface. Returning a wrapped type from a passthrough getter
defeats the purpose.

## 7. Error Handling

Vision does not perform blocking waits, so the ADR-011 timeout
contract does not apply directly. The fail-safe equivalent is
**always return `Optional.empty()` rather than a stale or
out-of-policy estimate** — consumers fall back to odometry
without the library making the decision for them.

| Failure | Detection | Response |
| ------- | --------- | -------- |
| Camera disconnected (no NT update within staleness window) | Timestamp-delta check in `robotPeriodicBefore()` | `isConnected()` → false; persistent `Alert(ERROR, "Camera {name} disconnected")` in the "Cameras" group (ADR-012 logged via AdvantageKit) |
| Stale reading this cycle | No new timestamp since last read | `getLatestPoseEstimate()` returns `Optional.empty()`; no `Alert` (normal at rest) |
| Too few tags | `tagCount < config.minTagCount` | Rejected inside the camera; returns `Optional.empty()`; no `Alert` (policy filter, not fault) |
| Too far / too ambiguous | Distance or ambiguity exceeds configured threshold | Same as above |
| NT read / photonlib call throws | `try` / `catch` inside the camera | Throttled `Elastic` WARNING (at most one per two-second window); returns `Optional.empty()`; `isConnected()` is not flipped for a single transient throw |
| PhotonPoseEstimator throws | Same pattern | Same response |

All error paths record a telemetry key so the match log retains
evidence of rejection reasons (ADR-012). Input validation on
config constructors rejects negative thresholds, `NaN` values,
and missing field layouts with `IllegalArgumentException` —
fail-fast at construction rather than mid-match.

## 8. Platform Portability Notes

Both cameras work in simulation:

- **LimelightCamera** reads from NetworkTables; in sim, a test
  harness publishes fake `botpose_orb_wpiblue` and `rawfiducials`
  entries via `NetworkTablesJNI`. Production code and test code
  are identical.
- **PhotonCamera** uses `photonlib`'s `PhotonCameraSim` +
  `VisionSystemSim` to drive pipeline results from a simulated
  robot pose. A sim harness advances `VisionSystemSim.update`
  inside a sim test's per-tick callback.
- Neither path requires the RoboRIO HAL beyond NT initialization
  — tests run under `HAL.initialize(500, 0)` per
  [ADR-009](../adr/ADR-009-junit5-hal-simulation-tests.md).

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| `PoseEstimate`, `TargetDetection` records | No | Equality, accessor, `Optional` semantics |
| `PoseEstimateStrategy` enum | No | Round-trip name/ordinal sanity |
| `LimelightCamera` ingest | Yes (NT4) | Publish fake NT entries; verify filter, `Optional` semantics, strategy reported |
| `LimelightCamera` stddev formula | No | Pure arithmetic — test the coefficient directly |
| `PhotonCamera` ingest | Yes (NT4) | `PhotonCameraSim` in `@BeforeEach` driving simulated frames |
| Staleness → `isConnected()` + `Alert` | Yes (NT4) | Advance sim time past the staleness threshold |
| Rejection filter (tag count / distance / ambiguity) | Yes (NT4) | Drive marginal inputs; assert `Optional.empty()` |
| Passthrough getters non-null | Yes (NT4) | Construction-time sanity |

Test IDs: `[TEST-VIS-NNN]`. Tests shall use unique NT prefixes
per class to prevent cross-test pollution.

### Planned (Not Yet Implemented)

> **Status: Planned — Not Yet Implemented.**

The following items are explicitly out of scope for V1 and will
be introduced via their own ADRs when a concrete need arises:

- **Luma P1 `vendor/luma/LumaCamera` implementation.** Pending
  Luma API stabilization.
- **Neural / ML-based object detection.** Would add
  `api/vision/ObjectDetection` and a parallel `getLatestObject()`
  surface; requires a distinct ADR per ADR-016's "one decision
  per ADR" constraint.
- **`VisionSubsystem` with pushed `Consumer<PoseEstimate>`
  aggregation (P3).** Useful once multi-camera fusion becomes
  concrete; separate ADR before implementation.
- **`robotToTarget` on Limelight single-tag targets.** Plumbed
  via `Optional<Transform3d>` in the record today; the Limelight
  wrapper will populate it from `targetpose_robotspace` when the
  first consumer requests it.
