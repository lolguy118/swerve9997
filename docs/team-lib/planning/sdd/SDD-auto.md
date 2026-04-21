# SDD: `com.team271.lib.auto` — Autonomous Move Composition

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-AUTO |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | `[AUT-001]`..`[AUT-011]` (SRS §4.6) |

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

## 1. Purpose

Provides the autonomous move composition model: `AutoMode`, `AutoMove`,
and container types. Robot projects use this model to compose autonomous
routines from reusable moves. Trajectory-follower integration for
PathPlanner and Choreo is also described here
([ADR-014](../adr/ADR-014-trajectory-following-vendors.md)).

## 2. Scope and Boundaries

This SDD covers:

- `AutoMode` — top-level autonomous routine; owns the move sequence lifecycle
- `AutoMove` — abstract base for a single step in an autonomous routine
- `AutoMoveTimed` — move with a fixed time limit
- `AutoMoveConditional` — move that ends when a condition is met; **must have
  a timeout** (ADR-012)
- `AutoMoveSequence` — container: runs moves in sequence
- `AutoMoveParallel` — container: runs moves concurrently
- `WaitMove` — delay move
- Trajectory-follower integration — `TrajectoryFollower.follow(...)`
  produces an `AutoMove` that samples a `Trajectory` (PathPlanner or
  Choreo) each cycle ([ADR-014](../adr/ADR-014-trajectory-following-vendors.md))
- `CommandBridge.asAutoMove` — command-based escape hatch for
  PathPlanner AutoBuilder / event-marker entry points
  ([ADR-014](../adr/ADR-014-trajectory-following-vendors.md))

## 3. Module Decomposition

```text
AutoMode
  └─ List<AutoMove> moves
       ├─ AutoMoveTimed          duration-based completion
       ├─ AutoMoveConditional    event-driven; MANDATORY timeout
       ├─ AutoMoveSequence       sequential children (nests any AutoMove)
       ├─ AutoMoveParallel       simultaneous children (nests any AutoMove)
       └─ WaitMove               explicit delay
```

| Class | Responsibility |
| ----- | -------------- |
| `AutoMode` | Top-level sequencer. Holds the flat move list and a current-move index. Owns the overall elapsed-time tracker. Delegates pre-periodic, autonomous-periodic, and post-periodic calls to the current move; advances to the next move when the current move reports completion. Sends Elastic notifications on start and end. |
| `AutoMove` | Abstract base. Each move tracks its own elapsed time (no shared clock). Provides template methods for start, end, autonomous-periodic, and pre-periodic behaviors. A gate predicate returns true only while the move is running, within its time limit, and not yet complete. Move authors override hooks — they do not subclass deep. |
| `AutoMoveTimed` | Completes after a fixed duration (`length` seconds). Optional start delay and safety timeout. Completion is checked every pre-periodic cycle, regardless of the gate predicate. |
| `AutoMoveConditional` | Waits for a boolean supplier with a **required** timeout argument. On condition true, ends normally. On timeout, sends a WARNING Elastic notification (including the move name), terminates, and allows the parent to advance. |
| `AutoMoveSequence` | Container that runs children in declaration order. Each child must complete before the next starts. Advances during the post-periodic hook. |
| `AutoMoveParallel` | Container that starts all children simultaneously. Completes only when every child reports completion. Children keep independent timers and completion state. |
| `WaitMove` | Explicit delay move — thin specialization of `AutoMoveTimed` with no subsystem commands. Used inside `AutoMoveSequence` / `AutoMoveParallel` for timing offsets. |
| Trajectory follower | `api/trajectory/TrajectoryFollower.follow(Trajectory, poseSupplier, chassisSpeedsConsumer, timeoutSec)` returns an `AutoMove` that samples the trajectory each cycle and pushes `ChassisSpeeds` to the drivetrain consumer. Two vendor implementations ship: `vendor/pathplanner/PathPlannerFollower` and `vendor/choreo/ChoreoFollower`. The returned `AutoMove` enforces the mandatory-timeout rule via the `timeoutSec` argument (ADR-012). |
| `CommandBridge.asAutoMove` | Command-based escape hatch. Robot projects wrap PathPlanner `Command` objects (AutoBuilder output, `PathPlannerAuto` with event markers) via `CommandBridge` (see [SDD-vendor-ctre.md §3.3](SDD-vendor-ctre.md)). Retained alongside `TrajectoryFollower` because PathPlanner's command-based entry points have no direct equivalent in the follower contract. |

## 4. Data Flow

```text
// AutoMode lifecycle (per autonomous routine)
autonomousInit()
  → AutoMode.start()
    → reset timers, send Elastic INFO ("Auto started")
    → start first move (onStart → timer running)

autonomousPeriodic() @ 50 Hz
  → AutoMode.robotPeriodicBefore(dt)
    → currentMove.robotPeriodicBefore(dt)     // updates elapsed, checks completion
      → [if container] each child.robotPeriodicBefore(dt)
  → AutoMode.autonomousPeriodic(dt)
    → currentMove.autonomousPeriodic(dt)      // gated by canRun()
      → [if container] each active child.autonomousPeriodic(dt)
  → AutoMode.robotPeriodicAfter(dt)
    → currentMove.robotPeriodicAfter(dt)
      → [if container] advance children
    → if (currentMove.isComplete) AutoMode.nextMove()

autonomousExit()
  → AutoMode.end()
    → send Elastic INFO ("Auto complete")

// AutoMoveConditional timeout path
robotPeriodicBefore:
  if (elapsed > timeout):
    Elastic.sendNotification(WARNING, "Conditional timeout", moveName)
    this.end()               // invokes onEnd() → subsystem cleanup
    // parent container advances normally
```

The separation between `robotPeriodicBefore` (time / condition
checks) and `autonomousPeriodic` (subsystem commands) is captured
by the `AutoMove.canRun()` gate described in §3 above: timing
and completion updates fire every cycle via `robotPeriodicBefore`,
but only `autonomousPeriodic` is gated by `canRun()`.

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Composition over WPILib Commands | See ADR-013 | [ADR-013](../adr/ADR-013-composition-over-commands.md) |
| No shared state between moves | Moves are independent; avoid coupling bugs | SRS §4.6 |
| Mandatory timeout on conditional moves | Physical safety — path must end | [ADR-012](../adr/ADR-012-mandatory-timeouts-fail-safe.md) |
| Vendor-neutral trajectory abstraction (`api/trajectory/`) with PathPlanner + Choreo implementations | Two-vendor launch validates the abstraction; swap is a construction change, not a rewrite | [ADR-014](../adr/ADR-014-trajectory-following-vendors.md) |
| `CommandBridge.asAutoMove` retained alongside `TrajectoryFollower` | PathPlanner's AutoBuilder and event-marker entry points are command-shaped; bridge gives them a lifecycle-native wrapping without forcing them through the follower contract | [ADR-014](../adr/ADR-014-trajectory-following-vendors.md) |
| Independent timers per move | Makes nested composition work without cross-coupling | See §3 (AutoMove row) |
| Move author owns `onEnd()` cleanup | Library cannot know which subsystem commands a move issued | See §3 (AutoMove row) and §6 |

## 6. Error Handling

- **Exceptions inside a move** — `AutoMode` does not wrap move calls
  in try/catch. An exception in a move propagates to
  `Robot.autonomousPeriodic()`. The subsystem-level exception isolation
  in `SubsystemManager.forEachSafe()` still protects the rest of the
  robot (see [SDD-subsystem.md](SDD-subsystem.md)), but the current
  auto routine is interrupted. Moves are expected to be written
  defensively — subsystem reads that may return null should be guarded
  by the move itself.
- **Conditional timeout** — `AutoMoveConditional` fires a
  `WARNING`-level Elastic notification including the move name, then
  calls `end()`. The parent container advances to the next move so
  the auto routine completes rather than stalling.
- **`onEnd()` responsibility** — library code calls `onEnd()` on
  every termination path (normal completion, timeout, early
  termination from a parent container). The move author must undo
  any subsystem commands issued in `onStart()` or during periodic
  execution. Forgetting this leaves subsystems in the commanded
  state after the move ends.
- **Trajectory follower failure** — when a `TrajectoryFollower`-produced
  `AutoMove` is still running past its `timeoutSec`, the `AutoMove`
  stops the drivetrain (by pushing a zero `ChassisSpeeds`), fires an
  Elastic WARNING notification including the move name, and calls
  `setCompleted()` so the parent container advances. Sampling past
  `totalTimeSeconds()` returns `Optional.empty()` — the follower
  treats that as "at end", holds zero output, and completes on the
  next cycle.
- **PathPlanner command failure** — commands wrapped via
  `CommandBridge.asAutoMove` signal completion through WPILib's
  `Command.isFinished()`. If the underlying command throws, the
  exception surfaces through the `CommandBridge.asAutoMove` wrapper
  in the same way as any other `AutoMove` exception. The mandatory
  timeout provides an upper bound even when the command's completion
  logic fails.

## 7. Platform Portability Notes

The auto composition framework is platform-neutral pure Java — it
runs identically in unit tests, desktop simulation, and on the
RoboRIO. The only platform dependencies are:

- `Timer.getFPGATimestamp()` for elapsed-time tracking (available in
  both HAL sim and RoboRIO).
- `Elastic.sendNotification()` calls require NetworkTables; safe in
  both sim and on robot.
- Trajectory followers (`PathPlannerFollower`, `ChoreoFollower`) and
  command-bridged wrappers require WPILib kinematics classes, which
  are platform-neutral. Vendor implementations load trajectory files
  from the robot project's deploy directory; test doubles
  (`FakeTrajectory`) avoid the deploy dependency.

Unit tests can step the framework forward in simulated time by
calling the periodic methods directly with monotonically increasing
timestamps.

## 8. Configuration

- **Startup delay** — `AutoMode` constructor accepts a delay (seconds)
  before the first move starts. Defaults to zero.
- **Move timeouts** — per-move constructor argument; named constants
  per robot project's `Constants`. `AutoMoveConditional` rejects a
  timeout of zero (mandatory positive timeout).
- **Trajectory authoring** — paths / trajectories are defined in the
  vendor's GUI (PathPlanner editor, Choreo optimizer) and referenced
  by name in robot project code. Vendor choice is per-trajectory; a
  single robot project may mix both.
- **Alliance flipping** — handled per-move in robot project code via
  the chosen vendor's alliance-flipping helpers
  (`PathPlannerPath.flipPath()`, `Choreo` alliance API), not by the
  library.
- **Trajectory timeouts** — `TrajectoryFollower.follow(...)` accepts a
  named timeout constant from the robot project's `Constants`; magic
  numbers prohibited.

Auto routines are composed in each `AutoMode` constructor via
`addMove(...)`; there is no external configuration file. Robot
projects document their own auto routines in their own design docs.

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| `AutoMoveTimed` completion | Yes | `HAL.initialize(500, 0)` for timer; verify `length` and `timeout` paths |
| `AutoMoveConditional` normal / timeout | Yes | Verify timeout fires Elastic and calls `end()` |
| `AutoMoveSequence` child advancement | Yes | Nest timed + conditional; verify declaration order |
| `AutoMoveParallel` child completion | Yes | Verify parent completes only when all children report complete |
| `AutoMode` indexing and `nextMove` | Yes | Run through all moves in order; verify `currentMoveIdx` |
| `CommandBridge.asAutoMove` | Yes | Verify WPILib command ends correctly inside an AutoMode |
| `TrajectoryFollower` timeout path | Yes | Verify a follower past `timeoutSec` stops drivetrain, fires Elastic WARNING, and advances parent container |
| `PathPlannerFollower` sample + consume | Yes | Load a fixture path, verify `ChassisSpeeds` consumer receives sampled values and follower completes at `totalTimeSeconds()` |
| `ChoreoFollower` sample + consume | Yes | Load a fixture trajectory, verify `ChassisSpeeds` consumer receives sampled values and follower completes at `totalTimeSeconds()` |

Test IDs: `[TEST-AUT-NNN]`. Existing auto tests (5 classes) live
under `src/test/java/com/team271/lib/auto/` — see
[SVP.md §Test Levels](../SVP.md#3-test-levels-library-specific-notes).
