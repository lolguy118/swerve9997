# ADR-017: Vendor-Neutral CAN Bus Abstraction

## Status

Accepted

## Date

2026-04-21

## Context

Team271-Lib currently has two distinct types named `CANBus`:

1. `com.ctre.phoenix6.CANBus` — CTRE's bus handle, accepted by
   Phoenix 6 v26+ device constructors.
2. `com.team271.lib.hardware.CANBus` — a library wrapper that adds bus
   type detection (RIO vs CANivore), utilization tracking, NT
   telemetry, and hoot-file logging.

Today, `com.team271.lib.hardware.CANDeviceID` holds the CTRE type, not
the library wrapper. That leaks a vendor type through a hardware-layer
composite key and means there is no canonical "bus reference" the rest
of the library can depend on. Three near-term features surface the
gap:

- **Bus utilization alerts** need a single place to observe the
  `busUtilization` signal and emit a driver alert when it exceeds a
  threshold. The wrapper already tracks utilization, but nothing
  enforces that a `CANDeviceID` resolves to a wrapper rather than a
  raw CTRE object.
- **Follower-bus validation** (leader and followers shall share a
  bus) needs a comparable bus identity at the hardware-wrapper
  boundary.
- **Multi-bus routing** (robots with a CANivore on the side of the
  RIO CAN bus) needs `CANDeviceID` to route devices to the right bus
  consistently.

The library is CTRE-focused per
[ADR-006](ADR-006-ctre-phoenix6-primary-vendor.md), and
[`.claude/rules/team271-lib.md`](../../../../.claude/rules/team271-lib.md)
forbids speculating alternative vendor implementations. However,
[ADR-016](ADR-016-vendor-neutral-vision-abstraction.md) already
established a precedent: `api/` interfaces are introduced when an
abstraction buys testability, layering cleanness, or symmetry — even
when the concrete vendor set is narrow.

## Decision

Introduce a vendor-neutral `api/CANBus` interface with a single CTRE
implementation, and route all library-internal references through it.

1. **Interface:** `com.team271.lib.api.CANBus` defines bus identity
   (`getName()`, `getType()`, `isSameBus(CANBus)`), utilization
   reporting (`getBusUtilization()`, `refresh()`), and the NT
   telemetry contract (`outputTelemetry()`). The `Type` enum remains
   `RIO | CANIVORE`; adding vendors or bus types is an ADR-superseding
   change.
2. **Implementation:** the existing wrapper moves to the CTRE vendor
   package per [ADR-004](ADR-004-layered-architecture.md) layering
   (canonical location: `com.team271.lib.vendor.ctre.CANBusCTRE`,
   matching the `IMUCTRE` / `RangeCTRE` naming pattern already in use
   under `hardware/sensors/`). It implements `api/CANBus` and provides
   a passthrough getter returning the raw `com.ctre.phoenix6.CANBus`
   per [ADR-003](ADR-003-passthrough-wrapper-not-wall.md).
3. **CANDeviceID:** holds an `api/CANBus` reference as its
   authoritative bus field. A passthrough getter exposes the raw
   CTRE `CANBus` for device constructors that require it. The raw
   `com.ctre.phoenix6.CANBus` is no longer a field type in
   `CANDeviceID`.
4. **Refresh ownership:** `CANBus.refresh()` is driven by
   `HardwareManager` (see
   [ADR-007](ADR-007-centralized-can-refresh.md)), not by individual
   subsystems. Utilization is a low-cadence signal (coarser than the
   periodic loop); refresh co-scheduling is left to `HardwareManager`
   design.
5. **Single-vendor commitment.** CTRE Phoenix 6 is the only planned
   implementation. Adding a second vendor (if one ever exists)
   **shall** go through a superseding ADR; `api/CANBus` **shall not**
   acquire unrelated vendor extensions speculatively.

## Rationale

1. **CANDeviceID stops leaking CTRE types.** Today the composite key
   at layer 3 (`hardware/`) imports a layer-2 vendor type. Moving
   `CANDeviceID` onto the `api/` interface aligns with the six-layer
   graph in [ADR-004](ADR-004-layered-architecture.md).
2. **Testability.** Mocking `api/CANBus` lets bus-utilization alert
   logic and follower-bus validation be tested without HAL sim or
   vendor stubs.
3. **Precedent.** `api/vision` in
   [ADR-016](ADR-016-vendor-neutral-vision-abstraction.md) already
   introduced an `api/` boundary for a narrow vendor set. The CAN-bus
   case is even narrower (one vendor, expected to stay one), but the
   same rationale — interface-driven testability and layering
   cleanness — applies.
4. **Name disambiguation.** Two `CANBus` types in the same project
   is a readability hazard. An `api/CANBus` interface with a
   `vendor/ctre/CANBusCTRE` impl names each role explicitly.
5. **Feature hooks already exist.** The wrapper tracks utilization
   and publishes NT telemetry today; an interface formalizes what is
   already implicit and enables the three triggering features without
   a rewrite.

## Consequences

**Easier:**

- `CANDeviceID` becomes a clean layer-3 type with no vendor imports.
- Bus utilization alerts and follower-bus validation can be
  unit-tested against mocks.
- Multi-bus routing is a property of `api/CANBus` identity
  (`isSameBus()`, `getName()`), not a CTRE-type comparison.
- Future refactor cost is bounded: the interface and impl are
  compile-time coupled to one vendor by design.

**Harder:**

- Callers that need the raw CTRE `CANBus` must go through the
  passthrough getter (consistent with
  [ADR-003](ADR-003-passthrough-wrapper-not-wall.md) but adds one
  hop).
- Two types share the name `CANBus`. Within the CTRE impl, the
  vendor type **shall** be referenced fully qualified, matching the
  existing pattern in the current wrapper.
- A small amount of design-inventory cost: an interface exists with
  only one implementor. The library explicitly accepts this cost in
  exchange for the benefits above.

## Alternatives Considered

- **Keep `CANBus` in `hardware/`, CTRE-specific, no interface.**
  Rejected — does not fix the `CANDeviceID` layering leak, and
  bus-utilization / follower-bus-validation tests would need HAL sim
  to isolate.
- **Move to `api/` without hedge (vendor-neutral, multi-vendor
  expected).** Rejected — conflicts with
  [ADR-006](ADR-006-ctre-phoenix6-primary-vendor.md) and the
  "no speculative vendor implementations" rule in
  [`.claude/rules/team271-lib.md`](../../../../.claude/rules/team271-lib.md).
- **Leave `CANDeviceID` holding the raw CTRE type; add utility
  methods on a sidecar class.** Rejected — splits bus identity across
  two types and does not enable mock-based testing of alert logic.

## References

- [ADR-003 — Passthrough: Wrapper, Not Wall](ADR-003-passthrough-wrapper-not-wall.md)
- [ADR-004 — Layered Architecture](ADR-004-layered-architecture.md)
- [ADR-006 — CTRE Phoenix 6 as Primary Vendor](ADR-006-ctre-phoenix6-primary-vendor.md)
- [ADR-007 — Centralized Bulk CAN Refresh](ADR-007-centralized-can-refresh.md)
- [ADR-016 — Vendor-Neutral Vision Abstraction](ADR-016-vendor-neutral-vision-abstraction.md)
- [SDD-hardware.md](../sdd/SDD-hardware.md)
- [SDD-vendor-ctre.md](../sdd/SDD-vendor-ctre.md)
