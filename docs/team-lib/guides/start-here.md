# Start Here — Team271-Lib Contributor Guide

> **Scope:** A 5-minute orientation for new contributors. Covers what
> this library is, how its pieces fit together, and where to go next.
> Does not duplicate content from other docs — each section points you
> to the authoritative reference for details.

---

## What Is Team271-Lib?

Team271-Lib is a reusable FRC robot framework built on WPILib, CTRE
Phoenix 6, and AdvantageKit. Each season's robot project is a
**fork** of this repository at a chosen season tag (see
[ADR-001](../planning/adr/ADR-001-team271-lib-standalone-library.md))
— the library provides building blocks (lifecycle management,
hardware wrappers, control algorithms, autonomous composition), and
each forked robot project assembles them into a specific robot with
its own subsystems, constants, and auto paths.

The library uses a **state-machine approach** instead of WPILib's
command-based framework. This gives finer-grained lifecycle control:
explicit before/after hooks, deterministic execution ordering within
each cycle, and per-subsystem exception isolation. If one subsystem
crashes, the rest keep running.

See [Library Architecture](../planning/sdd/SDD-team271-lib.md) for the full rationale and design decisions.

---

## The 30-Second Mental Model

Every piece of code in this library lives in one of six layers (plus
cross-cutting utilities). Higher layers depend only on layers below them.

> **Note:** The diagram below is a **simplified five-layer view** for
> orientation. It collapses the vendor-neutral `api/` interfaces and
> CTRE `vendor/ctre/` implementations into "Foundation." The rigorous
> six-layer model (api ← vendor/ctre ← hardware ← control ← subsystem
> ← auto) is defined in
> [ADR-003](../planning/adr/ADR-003-layered-architecture.md).

```text
┌─────────────────────────────────────────────────────┐
│  Auto Layer          AutoMode, AutoMove              │
│                      AutoMoveSequence / Parallel     │
├─────────────────────────────────────────────────────┤
│  Subsystem Layer     Subsystem, SubsystemManager     │
│                      SensorMode, homing pattern      │
├─────────────────────────────────────────────────────┤
│  Control Layer       PID variants, Balance           │
├─────────────────────────────────────────────────────┤
│  Hardware Layer      TransmissionFX, ControllerFX    │
│                      Encoders, IMU, Input            │
├─────────────────────────────────────────────────────┤
│  Foundation          TObj lifecycle + NT hierarchy    │
│                      CTREManager, NTEntry / NTInput   │
└─────────────────────────────────────────────────────┘
```

- **Foundation** — `TObj` is the universal base class. It gives every
  object lifecycle hooks (`robotInit`, `robotPeriodicBefore`, etc.) and
  automatic NetworkTables namespacing. `CTREManager` batches all CAN
  signal reads into one efficient call per cycle.
- **Hardware** — Wrappers around physical devices. `TransmissionFX`
  manages one or more TalonFX motors as a unit. Encoders, IMUs, and
  gamepads each have a base class with device-specific implementations.
- **Control** — Five PID variants (software, profiled, and
  hardware-onboard) plus the `Balance` algorithm, all sharing a common
  `PIDBase` interface with live dashboard tuning.
- **Subsystem** — `Subsystem` extends `TObj` and adds sensor modes,
  homing, and exception isolation. `SubsystemManager` orchestrates all
  subsystem lifecycle calls in a fixed order.
- **Auto** — Autonomous routines are composed from `AutoMove` building
  blocks using sequence, parallel, timed, and conditional containers.
  No timing constants — timing emerges from composition.

See [Library Architecture](../planning/sdd/SDD-team271-lib.md),
[Hardware Abstraction](../planning/sdd/SDD-hardware.md),
[Control System](../planning/sdd/SDD-control.md),
[Auto Design](../planning/sdd/SDD-auto.md).

---

## The Robot Loop — How Everything Executes

Every 20 ms, the robot runs this exact sequence. Understanding it is the
single most important thing for a new contributor:

```text
Every 20 ms:
┌───────────────────────────────────────────┐
│  CTREManager.refreshAll()                 │  ← bulk CAN read (all sensors at once)
├───────────────────────────────────────────┤
│  SubsystemManager.robotPeriodicBefore()   │  ← each subsystem reads its sensors
├───────────────────────────────────────────┤
│  <mode>Periodic()                         │  ← state machine logic sets desired state
├───────────────────────────────────────────┤
│  SubsystemManager.robotPeriodicAfter()    │  ← each subsystem applies outputs to hardware
├───────────────────────────────────────────┤
│  SubsystemManager.outputTelemetry()       │  ← publish to NetworkTables + check tuning
└───────────────────────────────────────────┘
```

This **read → decide → act → report** ordering guarantees:

- All sensor data is consistent within a cycle (no stale reads mid-decision).
- All decisions complete before any hardware commands fire (no cross-subsystem race conditions).
- If a subsystem throws an exception, `SubsystemManager` catches it and
  continues with the next subsystem — the robot keeps running.

See [SDD-subsystem §3.2 SubsystemManager](../planning/sdd/SDD-subsystem.md),
[SDD-subsystem §6 Error Handling](../planning/sdd/SDD-subsystem.md).

---

## Critical Rules Before You Touch Code

These rules exist because robots run in 20 ms real-time loops at
competition. Violating them can cause lockups, CAN bus overloads, or
loss of control.

| # | Rule | Why | Reference |
| --- | ------ | ----- | ----------- |
| 1 | All waiting operations must have timeouts | A missing timeout locks the robot if the expected condition never arrives (motor stall, sensor failure, unreachable waypoint) | [CODE-SAF-008](../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md), [SDD-subsystem §6.4](../planning/sdd/SDD-subsystem.md) |
| 2 | Set desired state in `<mode>Periodic()`, apply outputs in `robotPeriodicAfter()` | Mixing decision and actuation in the same phase causes cross-subsystem race conditions | [ADR-010](../planning/adr/ADR-010-desired-to-actual-state-pattern.md), [SDD-subsystem §3.1](../planning/sdd/SDD-subsystem.md) |
| 3 | Every `switch` on an enum must handle all cases including `default` | An unhandled state silently does nothing — dangerous on a 150 lb robot | [CODE-CTL rules](../../common/coding-standard/Team271-Software-Coding-Standard-Control.md) |
| 4 | Register all CTRE signals before `CTREManager.init()` | Signals added after init are never included in the bulk refresh | [SDD-hardware §3.5 CTREManager](../planning/sdd/SDD-hardware.md) |
| 5 | No tunable values in docs or CLAUDE.md | Constants in code are the single source of truth; docs reference constant names, not numbers | [ADR-015](../planning/adr/ADR-015-logged-nt-input-backed-tuning.md), [`.claude/rules/docs.md`](../../../.claude/rules/docs.md) |
| 6 | All configurable values must be dashboard-tunable via `LoggedNTInput` | Enables field-side tuning without redeploying code | [CODE-BUG-004](../../common/coding-standard/Team271-Software-Coding-Standard-Debug.md), [SDD-nt.md](../planning/sdd/SDD-nt.md) |
| 7 | Every hardware wrapper must expose its underlying vendor object via a getter | The library is additive — it wraps but never blocks access to CTRE/WPILib features | [Passthrough Design](../planning/sdd/SDD-vendor-ctre.md) |

See
[Team271-Software-Coding-Standard-Safety.md](../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md)
for the complete safety rules.

---

## Guided Reading Order

Read in this order. Each doc builds on the previous one.

1. **You are here** — this guide gives you the mental model.
2. **[Development Setup](../../common/guides/development-setup.md)** —
   Get the code building before reading further. You will want to
   reference actual source files as you read the architecture docs.
3. **[Library Architecture](../planning/sdd/SDD-team271-lib.md)** — The
   core document. Covers `TObj`, `SubsystemManager`, `CTREManager`,
   NetworkTables tuning, and simulation infrastructure. Read this
   end-to-end.
4. **[Hardware Abstraction](../planning/sdd/SDD-hardware.md)** — How
   motors, transmissions, encoders, IMUs, and gamepads are wrapped.
   Read this when you need to understand what a `TransmissionFX` or
   `ControllerTalonFX` does.
5. **[Fault Tolerance](../planning/sdd/SDD-subsystem.md)** — Exception
   isolation, CAN fault handling, timeout patterns. Short but critical —
   read before writing any subsystem code.
6. **[Java Coding Standard](../../common/coding-standard/Team271-Software-Coding-Standard.md)**
   — At minimum, read Section 3 (naming),
   [`-Safety.md`](../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md),
   and [`-Compliance.md` §5.4](../../common/coding-standard/Team271-Software-Coding-Standard-Compliance.md)
   (review checklist) before your first PR. The rest is reference.
7. **Area-specific docs** — pick based on what you are working on:

    | Working on... | Read |
    | --------------- | ------ |
    | Autonomous routines | [Auto Design](../planning/sdd/SDD-auto.md) |
    | PID tuning or control | [Control System](../planning/sdd/SDD-control.md) |
    | Simulation | [Simulation Guide](simulation-guide.md) |
    | System identification | [SysID Workflow](sysid-workflow.md) |
    | Joystick curves | [Input Shaping Guide](input-shaping-guide.md) |
    | Vendordep upgrades | [Vendor Dependencies](../planning/SCMP.md) |
    | Alerts / utilities | [Utility Package](../planning/sdd/SDD-util.md) |

8. **[Documentation Index](../planning/README.md)** — The master mapping
   from package to doc. Use as a lookup table when you encounter
   unfamiliar code.

---

## Quick Reference

| Question | Answer |
| ---------- | -------- |
| How do I build? | [Development Setup](../../common/guides/development-setup.md) |
| What is `TObj`? | [SDD-team271-lib §3.2 TObj](../planning/sdd/SDD-team271-lib.md) |
| How does the robot loop work? | [SDD-subsystem §3.2 SubsystemManager](../planning/sdd/SDD-subsystem.md) |
| How do I add a motor or sensor? | [Hardware Abstraction](../planning/sdd/SDD-hardware.md) |
| How do I make a value tunable? | [SDD-team271-lib §3.5 Tuning Infrastructure](../planning/sdd/SDD-team271-lib.md), [SDD-nt.md](../planning/sdd/SDD-nt.md) |
| What are the safety rules? | [`-Safety.md`](../../common/coding-standard/Team271-Software-Coding-Standard-Safety.md) |
| How do I write an auto routine? | [Auto Design](../planning/sdd/SDD-auto.md) |
| Which package does class X belong to? | [Documentation Index](../planning/README.md) |
| How do I run tests? | [SVP.md (Verification Plan)](../planning/SVP.md) |
