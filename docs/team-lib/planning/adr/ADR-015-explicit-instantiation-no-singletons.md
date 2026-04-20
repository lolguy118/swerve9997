# ADR-015: Explicit Object Instantiation, No Singletons in Library Code

## Status

Accepted

## Date

2026-04-20

## Context

Two common patterns for organizing robot code:

- **Singletons / service locator.** Each subsystem is a singleton;
  global `Drivetrain.getInstance()` calls pull the reference anywhere.
  This was the WPILib Command-Based tutorial pattern for years.
- **Explicit instantiation and injection.** The robot project creates
  subsystem instances in `robotInit()` and passes them by reference
  to whoever needs them.

Singletons are seductive early on: no constructor arguments to thread
through, no need to pass references around. The cost shows up later:
unit tests can't create a fresh subsystem per test (singletons are
process-global), initialization order becomes implicit, and lifecycle
management spreads across multiple `getInstance()` call sites.

## Decision

**Robot-level objects** (subsystems, controllers, transmissions,
sensors, input devices) are instantiated explicitly by the robot
project and passed by reference to whoever needs them. The library
does not provide singleton accessors for these types.

**Library-level coordination utilities** (`CTREManager`,
`SubsystemManager`) are exceptions — they use static state for
coordination (signal registration, subsystem dispatch) but are
initialized exactly once per robot lifecycle and are not
service locators. They do not store user subsystems; they orchestrate
them.

Rule of thumb: **if a user would call `.getInstance()` to fetch
their own object back, it's a service locator, and it doesn't belong
in this library.**

## Rationale

1. **Testability.** Unit tests create fresh objects per test; with
   singletons, every test inherits the state of the previous one.
2. **Explicit dependencies.** A subsystem's constructor lists what it
   needs; a reader of the code can trace dependencies directly.
3. **Initialization order.** Explicit instantiation in `robotInit()`
   is read top-to-bottom; singleton initialization order is
   determined by whichever `getInstance()` is called first, which
   varies across runs.
4. **Library vs. robot responsibility.** Orchestration utilities
   (CTREManager, SubsystemManager) need static state to function —
   they coordinate across all devices / all subsystems. User-level
   code does not need static state; forcing it creates problems
   that explicit instantiation does not have.

## Consequences

**Easier:**

- Unit tests are fast and isolated.
- Constructor signatures document dependencies.
- Initialization order is a function of reading `robotInit()`.
- Refactoring is safe — removing a subsystem removes its constructor
  call, not a globally-referenced instance.

**Harder:**

- Robot projects must thread subsystem references through call sites.
  (Mitigation: a `RobotContainer` class or similar that holds
  references.)
- Porting Command-Based tutorial code to this library requires
  rewriting `getInstance()` calls.

## Alternatives Considered

- **Singleton subsystems (Command-Based default).** Rejected — the
  test isolation problem is severe and not easily solved.
- **Dependency injection framework (Guice, Dagger).** Rejected —
  adds heavyweight machinery for a codebase that has ~10 objects to
  wire up; `robotInit()` is clear enough.

## References

- [SDD-team271-lib.md §3.2 TObj](../sdd/SDD-team271-lib.md#32-tobj)
- [SDD-subsystem.md §3.2 SubsystemManager](../sdd/SDD-subsystem.md#32-subsystemmanager)
- [SDD-hardware.md §3.5 CTREManager](../sdd/SDD-hardware.md#35-ctremanager--centralized-can-refresh)
- [ADR-001](ADR-001-team271-lib-standalone-library.md)
