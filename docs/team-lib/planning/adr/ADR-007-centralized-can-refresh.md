# ADR-007: Centralized Bulk CAN Refresh via CTREManager / HardwareManager

## Status

Accepted

## Date

2026-04-20

## Context

Phoenix 6 status signals (position, velocity, voltage, current, fault
bits, etc.) arrive asynchronously on the CAN bus and must be refreshed
before use. Naively, each caller refreshes the signals it needs:

```java
// Scattered across subsystems:
motor.getPosition().refresh();
motor.getVelocity().refresh();
```

This pattern has three problems in a 20 ms periodic loop: (1) each
refresh blocks on CAN read-back, (2) signals read at different points
in the cycle have inconsistent timestamps, and (3) no single place
knows whether a signal was registered.

## Decision

All CTRE StatusSignals used by the library are registered with
`CTREManager` at initialization time. `HardwareManager.refreshAll()` is
called once per robot periodic cycle (in `robotPeriodicBefore()`) and
delegates to `CTREManager.refreshAll()`, which performs a single bulk
refresh for every registered signal. Individual `signal.refresh()`
calls in periodic loops are not used.

`HardwareManager` is the forward-compatible entry point: it currently
delegates to `CTREManager` but can later orchestrate additional
non-CTRE `SignalRefreshable` sources (e.g., a WPILib sensor bridge).

## Rationale

1. **Consistent timestamps.** A bulk refresh gives all signals the
   same timestamp, making pose estimation and control math meaningful.
2. **Single CAN round-trip.** Phoenix 6's bulk refresh uses one wait
   cycle instead of one per signal, reducing periodic overhead.
3. **Registration visibility.** Every subsystem registers its signals
   at `robotInit()` time. The library knows the full set of signals
   before the first periodic loop.
4. **Fault handling in one place.** `CTREManager.refreshAll()` returns
   a `StatusCode`; errors are logged once with throttled Elastic
   notification rather than once per caller.
5. **Forward compatibility via HardwareManager.** A future WPILib
   sensor bridge can implement `SignalRefreshable` and register without
   changing any callers.

## Consequences

**Easier:**

- Subsystem periodic code is short: it reads values that are already
  refreshed.
- Signal registration bugs surface at init, not at first use.
- Telemetry timestamps are internally consistent.

**Harder:**

- Signals must be registered before use. Forgetting to register is a
  silent bug (zero values).
- Adding a new signal requires touching `CTREManager` signal lists in
  addition to the device wrapper.
- Tests that create CTRE devices must reset `CTREManager` static state
  between runs (see [ADR-009](ADR-009-junit5-hal-simulation-tests.md)).

## Alternatives Considered

- **Per-caller refresh.** Rejected — inconsistent timestamps and per-
  signal CAN round-trips cost more than the centralization does.
- **Per-subsystem refresh.** Rejected — still has the per-subsystem
  round-trip problem; does not solve the timestamp consistency issue
  for cross-subsystem math.

## References

- [SDD-hardware.md §3.5 CTREManager](../sdd/SDD-hardware.md#35-ctremanager--centralized-can-refresh)
- [SDD-team271-lib.md §4 Data Flow](../sdd/SDD-team271-lib.md#4-data-flow)
- [SDD-hardware.md §6 Error Handling](../sdd/SDD-hardware.md#6-error-handling)
- [ADR-006](ADR-006-ctre-phoenix6-primary-vendor.md)
