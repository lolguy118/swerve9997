# Software Design Descriptions (SDDs)

A **Software Design Description (SDD)** explains how one piece of
the library is put together on the inside — what classes exist,
what each one is responsible for, how data flows through, how
errors are handled. Each SDD covers exactly one architectural
layer or module of Team271-Lib.

If the Software Requirements Specification (SRS) says *what* the
library promises, an SDD says *how* we make good on that promise.

> **Industry bridge.** SDDs are a standard software-engineering
> artifact described by IEEE Std 1016 *Standard for Information
> Technology — Systems Design — Software Design Descriptions*.
> Every SDD in this folder follows a fixed nine-section template
> (Purpose, Scope, Module Decomposition, Data Flow, Key Design
> Decisions, Error Handling, Platform Portability, Configuration,
> Test Coverage Requirements) modeled on that standard.

## Start here

The SDD you'll open depends on what you're working on. See the
[Package-to-SDD Map](../README.md#package-to-sdd-map) in the
planning README for the authoritative mapping.

Good first SDDs to read, in order:

1. [SDD-team271-lib.md](SDD-team271-lib.md) — root-library
   infrastructure (`TObj`, lifecycle hooks, robot loop).
2. [SDD-api.md](SDD-api.md) — the vendor-neutral interface layer.
3. One layer-specific SDD based on what you're touching (hardware,
   control, subsystem, auto, vision).

## SDD inventory

| File | Layer it covers |
| ---- | --------------- |
| [SDD-team271-lib.md](SDD-team271-lib.md) | Root library (lifecycle, TObj, robot loop) |
| [SDD-api.md](SDD-api.md) | Vendor-neutral interfaces (Motor, Encoder, Gyro, etc.) |
| [SDD-vendor-ctre.md](SDD-vendor-ctre.md) | CTRE (Cross The Road Electronics) Phoenix 6 implementations |
| [SDD-hardware.md](SDD-hardware.md) | Controllers, transmissions, sensor wrappers, CAN refresh |
| [SDD-control.md](SDD-control.md) | PID variants, feedforwards, balance algorithm |
| [SDD-subsystem.md](SDD-subsystem.md) | Subsystem base class, SubsystemManager, StateMachine |
| [SDD-auto.md](SDD-auto.md) | Autonomous-move composition (AutoMode, AutoMove, sequences) |
| [SDD-sysid.md](SDD-sysid.md) | SysID (System Identification) data capture |
| [SDD-nt.md](SDD-nt.md) | NetworkTables primitives, logging, dashboard landscape |
| [SDD-util.md](SDD-util.md) | Alerts, Elastic notifications, math utilities |
| [SDD-vision.md](SDD-vision.md) | Camera pose estimates, target detections, AprilTag integration |
