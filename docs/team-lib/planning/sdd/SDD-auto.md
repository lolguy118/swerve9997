# SDD: `com.team271.lib.auto` — Autonomous Move Composition

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-AUTO |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | AUT-001 through AUT-NNN (SRS §4.6) |

## 1. Purpose

Provides the autonomous move composition model: `AutoMode`, `AutoMove`,
and container types. Robot projects use this model to compose autonomous
routines from reusable moves. PathPlanner integration is also described
here (ADR-013).

## 2. Scope and Boundaries

**This SDD covers:**

- `AutoMode` — top-level autonomous routine; owns the move sequence lifecycle
- `AutoMove` — abstract base for a single step in an autonomous routine
- `AutoMoveTimed` — move with a fixed time limit
- `AutoMoveConditional` — move that ends when a condition is met; **must have
  a timeout** (ADR-011)
- `AutoMoveSequence` — container: runs moves in sequence
- `AutoMoveParallel` — container: runs moves concurrently
- `WaitMove` — delay move
- PathPlanner `AutoMoveFollow` integration bridge (ADR-013)

**This SDD does not cover:**

- Path definitions and tuning (robot-project responsibility)
- Subsystem state machines → [SDD-subsystem.md](SDD-subsystem.md)

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
| `AutoMode` | Top-level sequencer. Holds the flat move list and `currentMoveIdx`. Owns the overall `elapsedTimer`. Delegates `robotPeriodicBefore`, `autonomousPeriodic`, and `robotPeriodicAfter` to the current move; advances via `nextMove()` when the current move reports `isComplete`. Sends Elastic notifications on start and end. |
| `AutoMove` | Abstract base. Each move tracks its own `elapsedTimer` (no shared clock). Provides template methods `onStart()`, `onEnd()`, `autonomousPeriodic()`, and `robotPeriodicBefore()`. Gate method `canRun()` returns `isRunning && isWithinTimeLimit() && !isComplete`. Move authors override hooks — they do not subclass deep. |
| `AutoMoveTimed` | Completes after a fixed `length` seconds. Optional `delay` and safety `timeout`. Completion checked in `robotPeriodicBefore` (fires every cycle regardless of `canRun()`). |
| `AutoMoveConditional` | Waits for a `BooleanSupplier` with a **required** timeout argument. On condition true, ends normally. On timeout, sends a WARNING Elastic notification (including the move name), calls `end()`, and allows the parent to advance. |
| `AutoMoveSequence` | Container that runs children in declaration order. Each child must complete before the next starts. Advances via `advanceToNext()` in `robotPeriodicAfter`. |
| `AutoMoveParallel` | Container that starts all children simultaneously. Completes only when every child reports `isComplete`. Children keep independent timers and completion state. |
| `WaitMove` | Explicit delay move — thin specialization of `AutoMoveTimed` with no subsystem commands. Used inside `AutoMoveSequence` / `AutoMoveParallel` for timing offsets. |
| PathPlanner bridge | Robot projects wrap PathPlanner `Command` objects via `CommandBridge.asAutoMove(cmd, timeoutSec)` (see [SDD-vendor-ctre.md §CommandBridge](SDD-vendor-ctre.md)). The bridge enforces the mandatory-timeout rule by making the timeout a required constructor argument. |

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
| Composition over WPILib Commands | See ADR-005 | [ADR-005](../adr/ADR-005-composition-over-commands.md) |
| No shared state between moves | Moves are independent; avoid coupling bugs | SRS §4.6 |
| Mandatory timeout on conditional moves | Physical safety — path must end | [ADR-011](../adr/ADR-011-mandatory-timeouts-fail-safe.md) |
| PathPlanner integration via `CommandBridge` | Reuse proven path generation without re-architecting | [ADR-013](../adr/ADR-013-pathplanner-autonomous.md) |
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
- **PathPlanner failure** — wrapped commands signal completion via
  WPILib's `Command.isFinished()`. If the underlying command throws,
  the exception surfaces through the `CommandBridge.asAutoMove`
  wrapper in the same way as any other `AutoMove` exception. The
  mandatory timeout provides an upper bound even when the command's
  completion logic fails.

## 7. Platform Portability Notes

The auto composition framework is platform-neutral pure Java — it
runs identically in unit tests, desktop simulation, and on the
RoboRIO. The only platform dependencies are:

- `Timer.getFPGATimestamp()` for elapsed-time tracking (available in
  both HAL sim and RoboRIO).
- `Elastic.sendNotification()` calls require NetworkTables; safe in
  both sim and on robot.
- PathPlanner-wrapped commands require WPILib kinematics classes,
  which are platform-neutral.

Unit tests can step the framework forward in simulated time by
calling the periodic methods directly with monotonically increasing
timestamps.

## 8. Configuration

- **Startup delay** — `AutoMode` constructor accepts a delay (seconds)
  before the first move starts. Defaults to zero.
- **Move timeouts** — per-move constructor argument; named constants
  per robot project's `Constants`. `AutoMoveConditional` rejects a
  timeout of zero (mandatory positive timeout).
- **PathPlanner paths** — defined in the PathPlanner GUI; referenced
  by name in robot project code.
- **Alliance flipping** — handled per-move in robot project code via
  PathPlanner's alliance-flipping helpers, not by the library.

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

Test IDs: TEST-AUT-NNN. Existing auto tests (5 classes) live under
`src/test/java/com/team271/lib/auto/` — see
[SVP.md §Test Structure](../SVP.md#test-structure).
