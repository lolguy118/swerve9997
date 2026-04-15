<!-- markdownlint-disable MD013 MD060 -->
# Start Here — Team271-Lib Contributor Guide

> **Scope:** A 5-minute orientation for new contributors. Covers what
> this library is, how its pieces fit together, and where to go next.
> Does not duplicate content from other docs — each section points you
> to the authoritative reference for details.

---

## What Is Team271-Lib?

Team271-Lib is a reusable FRC robot framework built on WPILib, CTRE Phoenix 6, and AdvantageKit. Robot projects depend on it but live in separate repositories — the library provides building blocks (lifecycle management, hardware wrappers, control algorithms, autonomous composition), and each robot project assembles them into a specific robot with its own subsystems, constants, and auto paths.

The library uses a **state-machine approach** instead of WPILib's command-based framework. This gives finer-grained lifecycle control: explicit before/after hooks, deterministic execution ordering within each cycle, and per-subsystem exception isolation. If one subsystem crashes, the rest keep running.

See [Library Architecture](library-architecture.md) for the full rationale and design decisions.

---

## The 30-Second Mental Model

Every piece of code in this library lives in one of five layers. Higher layers depend on lower ones:

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

- **Foundation** — `TObj` is the universal base class. It gives every object lifecycle hooks (`robotInit`, `robotPeriodicBefore`, etc.) and automatic NetworkTables namespacing. `CTREManager` batches all CAN signal reads into one efficient call per cycle.
- **Hardware** — Wrappers around physical devices. `TransmissionFX` manages one or more TalonFX motors as a unit. Encoders, IMUs, and gamepads each have a base class with device-specific implementations.
- **Control** — Five PID variants (software, profiled, and hardware-onboard) plus the `Balance` algorithm, all sharing a common `PIDBase` interface with live dashboard tuning.
- **Subsystem** — `Subsystem` extends `TObj` and adds sensor modes, homing, and exception isolation. `SubsystemManager` orchestrates all subsystem lifecycle calls in a fixed order.
- **Auto** — Autonomous routines are composed from `AutoMove` building blocks using sequence, parallel, timed, and conditional containers. No timing constants — timing emerges from composition.

See [Library Architecture](library-architecture.md), [Hardware Abstraction](hardware-abstraction.md), [Control System](control-system.md), [Auto Design](auto-design.md).

---

## The Robot Loop — How Everything Executes

Every 20 ms, the robot runs this exact sequence. Understanding it is the single most important thing for a new contributor:

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
- If a subsystem throws an exception, `SubsystemManager` catches it and continues with the next subsystem — the robot keeps running.

See [Library Architecture — SubsystemManager](library-architecture.md#subsystemmanager--lifecycle-orchestrator), [Fault Tolerance](fault-tolerance.md).

---

## Critical Rules Before You Touch Code

These rules exist because robots run in 20 ms real-time loops at competition. Violating them can cause lockups, CAN bus overloads, or loss of control.

| # | Rule | Why | Reference |
|---|------|-----|-----------|
| 1 | All waiting operations must have timeouts | A missing timeout locks the robot if the expected condition never arrives (motor stall, sensor failure, unreachable waypoint) | [CODE-SAF-008](team271-java-coding-standard.md), [Fault Tolerance](fault-tolerance.md) |
| 2 | Set desired state in `<mode>Periodic()`, apply outputs in `robotPeriodicAfter()` | Mixing decision and actuation in the same phase causes cross-subsystem race conditions | [State Machine Pattern](library-architecture.md#state-machine-pattern) |
| 3 | Every `switch` on an enum must handle all cases including `default` | An unhandled state silently does nothing — dangerous on a 150 lb robot | [CODE-SAF-003](team271-java-coding-standard.md) |
| 4 | Register all CTRE signals before `CTREManager.init()` | Signals added after init are never included in the bulk refresh | [CTREManager](library-architecture.md#ctremanager--centralized-can-refresh) |
| 5 | No tunable values in docs or CLAUDE.md | Constants in code are the single source of truth; docs reference constant names, not numbers | [Documentation Rules](../../CLAUDE.md) |
| 6 | All configurable values must be dashboard-tunable via `LoggedNTInput` | Enables field-side tuning without redeploying code | [CODE-BUG-004](team271-java-coding-standard.md), [Tuning Infrastructure](library-architecture.md#tuning-infrastructure) |

See [Java Coding Standard — Section 4.9](team271-java-coding-standard.md) for the complete safety rules.

---

## Guided Reading Order

Read in this order. Each doc builds on the previous one.

1. **You are here** — this guide gives you the mental model.
2. **[Development Setup](development-setup.md)** — Get the code building before reading further. You will want to reference actual source files as you read the architecture docs.
3. **[Library Architecture](library-architecture.md)** — The core document. Covers `TObj`, `SubsystemManager`, `CTREManager`, NetworkTables tuning, and simulation infrastructure. Read this end-to-end.
4. **[Hardware Abstraction](hardware-abstraction.md)** — How motors, transmissions, encoders, IMUs, and gamepads are wrapped. Read this when you need to understand what a `TransmissionFX` or `ControllerTalonFX` does.
5. **[Fault Tolerance](fault-tolerance.md)** — Exception isolation, CAN fault handling, timeout patterns. Short but critical — read before writing any subsystem code.
6. **[Java Coding Standard](team271-java-coding-standard.md)** — At minimum, read Section 3 (naming), Section 4.9 (safety), and Section 5.4 (review checklist) before your first PR. The rest is reference.
7. **Area-specific docs** — pick based on what you are working on:

| Working on... | Read |
|---------------|------|
| Autonomous routines | [Auto Design](auto-design.md) |
| PID tuning or control | [Control System](control-system.md) |
| Simulation | [Simulation Guide](simulation-guide.md) |
| System identification | [SysID Workflow](sysid-workflow.md) |
| Joystick curves | [Input Shaping Guide](input-shaping-guide.md) |
| Vendordep upgrades | [Vendor Dependencies](vendor-dependencies.md) |
| Geometry / math | [Geometry Package](geometry-package.md) |
| Alerts / utilities | [Utility Package](utility-package.md) |

8. **[Documentation Index](documentation-index.md)** — The master mapping from package to doc. Use as a lookup table when you encounter unfamiliar code.

---

## Quick Reference

| Question | Answer |
|----------|--------|
| How do I build? | [Development Setup](development-setup.md) |
| What is `TObj`? | [Library Architecture — TObj](library-architecture.md#tobj--base-class) |
| How does the robot loop work? | [Library Architecture — SubsystemManager](library-architecture.md#subsystemmanager--lifecycle-orchestrator) |
| How do I add a motor or sensor? | [Hardware Abstraction](hardware-abstraction.md) |
| How do I make a value tunable? | [Library Architecture — Tuning Infrastructure](library-architecture.md#tuning-infrastructure) |
| What are the safety rules? | [Coding Standard — Section 4.9](team271-java-coding-standard.md) |
| How do I write an auto routine? | [Auto Design](auto-design.md) |
| Which package does class X belong to? | [Documentation Index](documentation-index.md) |
| How do I run tests? | [Testing Strategy](testing-strategy.md) |
