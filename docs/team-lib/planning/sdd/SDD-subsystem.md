# SDD: `com.team271.lib.subsystem` — Subsystem Base and Manager

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-SUBSYSTEM |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | `[SUB-001]`..`[SUB-005]` (SRS §4.5) |

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

## 1. Purpose

Provides the base class for all robot subsystems, the `SubsystemManager`
lifecycle coordinator, and the `StateMachine` utility. Defines exception
isolation (ADR-011) and the desired-to-actual state pattern (ADR-010).

## 2. Scope and Boundaries

This SDD covers:

- `Subsystem` — base class extending TObj; defines sensor-mode pattern,
  homing contract, per-subsystem telemetry
- `SubsystemManager` — singleton registry, lifecycle orchestration,
  exception-isolated per-subsystem iteration
- `StateMachine` — generic desired-to-actual enum helper with optional
  transition callbacks

## 3. Module Decomposition

### 3.1 `Subsystem`

`Subsystem extends TObj` and adds sensor-management fields plus the
homing contract.

**SensorMode state machine** — every subsystem tracks one of four
modes that gate sensor reads:

| Mode | Behavior |
| ---- | -------- |
| `SENSORED_AUTO` | Sensors enabled; subsystem runs in closed-loop mode, driving toward the desired state |
| `SENSORED_MANUAL` | Sensors enabled (used for limits, safety interlocks); subsystem runs in open-loop mode |
| `SENSORLESS` | Sensors disabled; open-loop control only |
| `SYSID` | System identification mode — sensor reads still active, control routed through SysID logger |

**Homing contract** — subsystems that require zeroing implement a
zero-to-hardware action plus a post-zero callback. A persisted
zeroed flag gates closed-loop operation so a subsystem cannot run
position control before homing completes. Per coding standard
§4.9c and ADR-012, any waiting homing sequence must have a named
timeout constant, a fail-safe action (stop motors, restore default
current limits, transition to IDLE), and an Elastic notification on
timeout.

**Lifecycle override points** — subsystems override the robot-init,
pre-periodic (read sensors, update actual state), post-periodic
(apply desired state to hardware), and telemetry-output hooks.
Telemetry output also runs the tuning-check pass that applies
dashboard changes.

### 3.2 `SubsystemManager`

Singleton registry. The robot project's main class registers each
subsystem during construction; registration order is load-bearing
and determines execution order per cycle.

**Lifecycle orchestration** — the manager broadcasts every lifecycle
phase to every registered subsystem, in order, under a single
exception-isolated iteration pass per phase. The canonical per-cycle
order is documented in §4 Data Flow.

**Exception isolation** — every subsystem call is wrapped in a
try/catch. On exception, the manager logs the error, sends a
throttled Elastic notification (throttle interval defined in
`ConstantsLib`, keyed per subsystem via a per-subsystem
last-notification timestamp), and continues iterating. One broken
subsystem does not stall the others. The robot-init phase is
explicitly **not** isolated — an init failure is fatal because the
robot cannot safely operate with a partially initialized subsystem.

### 3.3 `StateMachine`

Generic composition-based helper for the desired-to-actual pattern.
Subsystems use it as a field, not a base class — the subsystem
still extends `Subsystem`. `StateMachine<S extends Enum<S>>` stores
current state and desired state, publishes both to NT during
telemetry output, and exposes a transitioning predicate for gating
logic.

Transition callbacks (optional) are attached via a fluent builder:
one callback fires on exit from the previous state, one on entry to
the new state. Callbacks fire only when the state actually changes;
the exit callback runs before the current state is updated, the
entry callback runs after. Simple subsystems can continue to use
manual enum fields and switch statements — the helper is optional.

## 4. Data Flow

```text
// Per-cycle flow through a subsystem
operator input (teleop) / AutoMove (auto)
  → subsystem.setDesiredState(newState)           // enum assignment

SubsystemManager.robotPeriodicBefore()
  → forEachSafe(s -> s.robotPeriodicBefore(t))
    → subsystem reads sensors → updates actualState

SubsystemManager.<mode>Periodic()
  → forEachSafe(s -> s.teleopPeriodic(t))
    → subsystem state-machine logic (often a no-op if setters are in commands)

SubsystemManager.robotPeriodicAfter()
  → forEachSafe(s -> s.robotPeriodicAfter(t))
    → switch on desiredState → transmission.setOutputPosition(...) etc.
    → sm.transition(sm.getDesiredState())          // fires onExit/onEnter

SubsystemManager.outputTelemetry()
  → forEachSafe(s -> s.outputTelemetry())
    → checkTuning() (applies LoggedNTInput changes)
    → publish current/desired state, sensor values, output via NTEntry
  → Alert.outputTelemetry()                        // static: all groups
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Exception isolation in `forEachSafe()` | One broken subsystem must not crash the robot loop | [ADR-011](../adr/ADR-011-subsystem-exception-isolation.md) |
| Desired-to-actual separation | Decouples operator input from hardware; all reads finish before writes | [ADR-010](../adr/ADR-010-desired-to-actual-state-pattern.md) |
| Registration order is execution order | Load-bearing ordering documented in robot project | See §3.2 (`SubsystemManager`) above |
| Homing must have timeout + fail-safe + Elastic | Physical safety — robot must stop if homing hangs | [ADR-012](../adr/ADR-012-mandatory-timeouts-fail-safe.md) |
| `StateMachine` is a helper, not a base class | Keeps subsystem inheritance single-axis; simple subsystems skip it | See §3.3 above |
| `SubsystemManager` is a singleton | There is exactly one subsystem registry per robot | [ADR-004](../adr/ADR-004-explicit-instantiation-no-singletons.md) |
| `robotInit()` is not exception-isolated | Init failure is fatal; partial init is unsafe | See §6.1 |

## 6. Error Handling

### 6.1 Exception Isolation

`forEachSafe` catches any `Throwable` in a subsystem's lifecycle call,
logs the phase name plus exception to the DriverStation, and sends a
throttled Elastic `ERROR`-level notification. The subsystem list is
not modified — a subsystem that throws will be retried next cycle.

`robotInit` failure is the one phase that is **not** isolated.
Exceptions propagate to `Robot.robotInit()` and crash early, which is
the intended behavior — a partially initialized subsystem is worse
than a crashed robot (see [ADR-011](../adr/ADR-011-subsystem-exception-isolation.md)).

### 6.2 Error Notification Throttling

Each subsystem has its own `lastErrorNotificationTime` entry. A
persistent null-pointer bug produces one notification per throttle
interval (defined in `ConstantsLib`), not one per periodic cycle.
Without throttling, a sustained exception would flood the dashboard
with 50 notifications per second.

### 6.3 Safe Defaults

Library-level safe defaults that subsystems inherit:

- Motor output: guarded on `isConnected` — no command sent to
  disconnected devices.
- Input axes: return 0.0 when controller disconnected.
- Config failure: motor falls back to Phoenix 6 factory defaults
  (which include safe current limits).
- `StateMachine` transitions: callbacks fire only on actual state
  change; no-op for same-state transitions.

### 6.4 Homing Timeout

Each subsystem that homes must implement its own timeout
(`kHomingTimeoutSec` in that subsystem's `Constants`), fail-safe
action, and Elastic notification (per
[ADR-012](../adr/ADR-012-mandatory-timeouts-fail-safe.md)). The
library does not generic this pattern because the fail-safe action
is subsystem-specific.

### 6.5 StateMachine Exception Propagation

Exceptions thrown from `onEnter` or `onExit` callbacks propagate to
the enclosing subsystem's lifecycle call, and therefore through
`forEachSafe` (see §6.1). Callback authors do not need to catch
routine exceptions — the isolation covers them.

## 7. Platform Portability Notes

The subsystem layer contains no platform-specific code. Pure Java
behavior runs identically in unit tests, desktop simulation, and on
the RoboRIO. Hardware dependencies come from motor and sensor
wrappers used inside a subsystem (see
[SDD-hardware.md](SDD-hardware.md)).

Unit tests that exercise subsystem lifecycle typically:

1. Initialize HAL: `HAL.initialize(500, 0)`.
2. Reset CTRE state: `CTREManager.resetForTesting()`.
3. Clear any previous `SubsystemManager` registrations (see
   [SVP.md §Test Levels](../SVP.md#3-test-levels-library-specific-notes)).
4. Construct the subsystem and drive it manually by calling
   lifecycle methods directly.

## 8. Configuration

The library defines the lifecycle contract and the exception-isolation
policy; it does not define subsystem configuration. Each robot
project configures its own subsystems via:

- **`Constants` classes** — CAN IDs, gear ratios, homing timeouts,
  PID defaults, current limits, voltage limits.
- **`addSubsystem()` registration order** — explicit, declared once
  in the robot project's main class.
- **Sensor mode** — set on each subsystem at `robotInit()` based on
  the robot's default operating mode.
- **Homing constants** — per-subsystem `Constants.kHomingTimeoutSec`
  and `Constants.kDefaultStatorLimit` pairs.

## 9. Test Coverage Requirements

| Area | HAL Required | CTREManager Reset | Notes |
| ---- | ------------ | ----------------- | ----- |
| `Subsystem` lifecycle hooks | Yes | Yes if hardware used | Verify order of sensor read → state update → output |
| `SubsystemManager.forEachSafe` | Yes | Yes | Register a throwing subsystem; verify other subsystems still run |
| `SubsystemManager` throttling | No | No | Drive clock forward; verify 1 notification per 2s |
| `StateMachine` transitions | No | No | Pure state math; verify callbacks fire only on change |
| `StateMachine` telemetry | Yes (NT) | No | Verify NT publishing of current and desired state |

Test IDs: `[TEST-SUB-NNN]`. Existing tests live under
`src/test/java/com/team271/lib/subsystem/` (2 classes) — see
[SVP.md §Test Levels](../SVP.md#3-test-levels-library-specific-notes).
