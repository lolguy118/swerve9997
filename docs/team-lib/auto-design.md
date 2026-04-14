<!-- markdownlint-disable MD013 MD060 -->
# Auto System Architecture

This document describes the autonomous routine framework in Team271-Lib.
The library provides composable building blocks for constructing complex
autonomous sequences without shared timers or tight coupling to subsystems.

---

## Core Concepts

Autonomous routines are built by composing **moves** — small, self-contained
units of work. Moves can be nested inside containers that control execution
order (sequential, parallel, conditional). An **AutoMode** manages the
top-level move sequence for a single autonomous routine.

### Key Principles

- **Composition over inheritance** — complex timing comes from nesting
  moves, not from subclassing
- **Independent timers** — each move tracks its own elapsed time
- **No shared state** — moves command subsystems directly; subsystems
  do not poll a shared timer
- **Mandatory timeouts** — conditional waits must have timeout protection
  per coding standard 4.9c

---

## Class Hierarchy

```text
AutoMode (top-level sequencer — one per auto routine)
  └─ List<AutoMove> moves
       ├─ AutoMoveSingle        (leaf / placeholder)
       ├─ AutoMoveTimed         (duration-based completion)
       ├─ AutoMoveConditional   (event-driven completion)
       ├─ AutoMoveSequence      (sequential children)
       │   └─ List<AutoMove>    (any move type, including containers)
       └─ AutoMoveParallel      (simultaneous children)
           └─ List<AutoMove>    (any move type, including containers)
```

Containers (Sequence, Parallel) accept any `AutoMove`, so they can be
nested arbitrarily to create complex overlapping timing patterns.

---

## AutoMode — Top-Level Sequencer

`AutoMode` manages a flat list of moves and executes them one at a time.
It is the entry point that the robot's `Superstructure` (or equivalent)
selects and runs during autonomous.

### Lifecycle

```text
autonomousInit()
  → start()
    → reset timers
    → start first move
    → send Elastic notification

autonomousPeriodic()          (called every 20 ms)
  → robotPeriodicBefore()     — update timing, delegate to current move
  → autonomousPeriodic()      — delegate to current move if canRun()
  → robotPeriodicAfter()      — check move completion, advance

autonomousExit()
  → end()
    → stop execution
    → send completion notification
```

### Key Fields

| Field | Type | Purpose |
|-------|------|---------|
| `moves` | `List<AutoMove>` | Ordered queue of moves |
| `currentMoveIdx` | `long` | Index of the active move |
| `delay` | `double` | Startup delay (seconds) before first move |
| `elapsedTimer` | `Timer` | Overall auto mode elapsed time |

### Move Advancement

When the current move completes (`isComplete == true`), `nextMove()`
advances to the next move in the list. If the list is exhausted,
`end()` is called and the auto mode completes.

---

## AutoMove — Abstract Base

Every move extends `AutoMove`. It provides:

- **Independent timer** — each move has its own `elapsedTimer`
- **Delay support** — optional delay before execution starts
- **Template methods** — `onStart()` and `onEnd()` hooks for subclasses
- **Gated execution** — `canRun()` returns true only while running,
  within time limit, and not complete

### Key Methods

| Method | Purpose |
|--------|---------|
| `start()` | Sets running flag, starts timer, calls `onStart()` |
| `end()` | Calls `onEnd()`, marks complete, logs telemetry |
| `canRun()` | Gate: `isRunning && isWithinTimeLimit() && !isComplete` |
| `onStart()` | Override hook — empty by default |
| `onEnd()` | Override hook — empty by default |
| `autonomousPeriodic()` | Override for move behavior — empty by default |
| `robotPeriodicBefore()` | Updates elapsed time, logs telemetry |

### Implementing a Custom Move

Robot projects extend `AutoMove` (or `AutoMoveSingle`) and override
`onStart()`, `autonomousPeriodic()`, and `onEnd()` to command subsystems:

```java
public class ShootMove extends AutoMoveSingle {
    public ShootMove(double argDelay) {
        super(argDelay);
    }

    @Override
    protected void onStart() {
        Globals.launcher.setAutoShoot(true);
    }

    @Override
    public void autonomousPeriodic(double argTimestamp) {
        // Check completion condition
        if (Globals.launcher.hasShot()) {
            end();
        }
    }

    @Override
    protected void onEnd() {
        Globals.launcher.setAutoShoot(false);
    }
}
```

---

## Move Types

### AutoMoveSingle — Leaf Node

A minimal concrete `AutoMove` with no additional behavior. Used as:
- A base class for simple robot-specific moves
- A placeholder / no-op in sequences

### AutoMoveTimed — Duration-Based

Completes after a specified duration. Checks elapsed time in
`robotPeriodicBefore()` (fires every cycle, not gated by `canRun()`).

| Parameter | Purpose |
|-----------|---------|
| `length` | Primary duration (seconds) |
| `delay` | Delay before start (seconds) |
| `timeout` | Safety timeout (seconds, 0 = disabled) |

Termination priority: if `timeout > 0.01` and elapsed > timeout, ends
immediately. Otherwise ends when elapsed > length.

### AutoMoveConditional — Event-Driven

Waits for a `BooleanSupplier` condition to become true, with a mandatory
timeout. Checks condition in `robotPeriodicBefore()` every cycle.

```java
new AutoMoveConditional(
    "LauncherReady",
    Launcher::isAtMaxVelocity,
    3.0  // timeout seconds — required
);
```

On timeout: sends a WARNING notification via Elastic and ends the move.
This follows the coding standard's requirement that all waiting operations
have timeout protection.

### AutoMoveSequence — Sequential Composition

Executes children one at a time in order. Each child must complete before
the next starts. The sequence completes when all children finish.

```java
new AutoMoveSequence(
    new DriveToPositionMove(pose1),
    new ShootMove(0.0),
    new DriveToPositionMove(pose2)
);
```

Internally manages a `currentIdx` and `current` reference, advancing via
`advanceToNext()` when the active child completes.

### AutoMoveParallel — Simultaneous Execution

Starts all children simultaneously. Completes only when **all** children
have finished. Each child maintains independent timing and completion state.

```java
new AutoMoveParallel(
    new DrivePathMove(path),
    new AutoMoveSequence(
        new AutoMoveTimed(1.0),  // wait 1 second
        new ShootMove(0.0)       // then shoot while still driving
    )
);
```

---

## Composition Patterns

### Pattern: Overlapping Actions

Use `AutoMoveParallel` to run actions simultaneously. Nest an
`AutoMoveSequence` inside to delay one action relative to another:

```java
// Drive path while shooting after a 1-second delay
new AutoMoveParallel(
    new DrivePathMove(path),
    new AutoMoveSequence(
        new AutoMoveTimed(1.0),
        new ShootMove(0.0)
    )
);
```

### Pattern: Conditional Gate

Use `AutoMoveConditional` to wait for a subsystem to reach a state
before proceeding:

```java
new AutoMoveSequence(
    new SpinUpLauncherMove(),
    new AutoMoveConditional("AtSpeed", Launcher::isReady, 2.0),
    new FeedAndShootMove()
);
```

### Pattern: Timed Segment

Use `AutoMoveTimed` for fixed-duration actions (intake, wait, coast):

```java
new AutoMoveSequence(
    new IntakeMove(),
    new AutoMoveTimed(0.5),  // brief pause
    new ShootMove(0.0)
);
```

---

## Periodic Delegation Chain

The delegation flow ensures every move in the tree receives periodic
updates, regardless of nesting depth:

```text
AutoMode.robotPeriodicBefore(dt)
  → currentMove.robotPeriodicBefore(dt)
    → [if container] each child.robotPeriodicBefore(dt)

AutoMode.autonomousPeriodic()
  → currentMove.autonomousPeriodic(dt)
    → [if container] each child.autonomousPeriodic(dt)

AutoMode.robotPeriodicAfter(dt)
  → currentMove.robotPeriodicAfter(dt)
    → [if container] check child completion, advance/end
  → check currentMove completion → nextMove()
```

### Where State Checks Happen

- **AutoMoveTimed** and **AutoMoveConditional** check completion in
  `robotPeriodicBefore()` — this fires every cycle regardless of
  `canRun()` state
- **AutoMoveSequence** and **AutoMoveParallel** check child completion
  in `robotPeriodicAfter()` — after children have had a chance to act
- **AutoMode** checks current move completion in `robotPeriodicAfter()`
  and advances

---

## Telemetry

All auto classes publish telemetry under the `Auto/` NetworkTables prefix:

| Class | Key Pattern | Values |
|-------|-------------|--------|
| AutoMode | `Auto/` | Running, Complete, CurrentTime, MoveIndex, CurrentMoveName |
| AutoMove | `Auto/Moves/{name}/` | Running, Complete, ElapsedTime |
| AutoMoveSequence | (above) + | ChildIndex, ChildName |
| AutoMoveParallel | (above) + | CompletedChildren, TotalChildren, ActiveChildren |

---

## Building a New Auto Routine

1. Create a class extending `AutoMode`
2. In the constructor, compose moves using `addMove()`
3. Register the mode with the robot's auto selector

```java
public class TwoPieceAuto extends AutoMode {
    public TwoPieceAuto() {
        super(0.0);  // no startup delay

        addMove(new AutoMoveParallel(
            new DrivePathMove(Paths.kStartToFirstPiece),
            new IntakeMove()
        ));

        addMove(new AutoMoveSequence(
            new SpinUpMove(),
            new AutoMoveConditional("Ready", Launcher::isReady, 2.0),
            new ShootMove(0.0)
        ));

        addMove(new DrivePathMove(Paths.kFirstPieceToSecond));

        addMove(new AutoMoveParallel(
            new IntakeMove(),
            new AutoMoveTimed(1.5)
        ));

        addMove(new ShootMove(0.0));
    }
}
```
