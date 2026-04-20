# ADR-003: Passthrough — Wrapper, Not Wall

## Status

Accepted

## Date

2026-04-20

## Context

Team271-Lib wraps CTRE Phoenix 6 devices, WPILib PID controllers, and
other vendor types to add lifecycle management, multi-motor coordination,
centralized CAN refresh, fault monitoring, and gear-ratio conversions.
There is a constant tension between the library's desire to provide a
clean, consistent API and the need to access vendor-specific features
that are either new, advanced, or rarely used.

Historically, wrapper libraries accrete over time: they start helpful,
then grow to hide the underlying object as a matter of philosophy, and
eventually block access to the very features the wrapper was designed
to enable.

## Decision

Every hardware wrapper in Team271-Lib exposes its underlying vendor
object via a public getter (e.g., `getTalonFX()`, `getCANcoder()`,
`getController()` for WPILib PID). The library never blocks access to a
vendor feature that has not yet been wrapped. When CTRE or WPILib
releases a new feature, callers can use it immediately via the
passthrough — no library update is required.

Concretely:

1. Store the vendor object as a `protected` field.
2. Provide a `public` getter returning the raw vendor type.
3. If the class owns a configuration object, provide `getConfig()`.
4. If the class owns simulation state, provide `getSimState()`.
5. Document every getter in the `SDD-vendor-ctre.md` passthrough
   reference table.

## Rationale

1. **Never block access.** A wrapper that hides the underlying object
   creates artificial dependency on the library's release cadence for
   every new vendor feature.
2. **Vendor docs stay useful.** Students can read CTRE's Phoenix 6
   documentation and apply the examples through the passthrough without
   hunting for a library equivalent.
3. **Migration safety.** When Phoenix 6 or WPILib introduces a new
   feature, robot projects can adopt it immediately; the library
   wraps it later if ≥2 callers need it or it crosses subsystem
   boundaries.

## Consequences

**Easier:**

- New vendor features are accessible immediately.
- Library updates don't need to keep pace with every Phoenix 6 release.
- Students learn the vendor APIs alongside the library.

**Harder:**

- Two paths for every operation (library convenience + passthrough) —
  contributors must choose the right one.
- The passthrough reference table in `SDD-vendor-ctre.md` must stay in
  sync with the wrappers.

## Alternatives Considered

- **Hide vendor types behind the library API.** Rejected — creates a
  waiting-on-library dependency for every new feature.
- **Expose vendor types only in a `.internal` package.** Rejected — adds
  ceremony without reducing the number of paths.

## References

- [SDD-vendor-ctre.md §Passthrough Reference](../sdd/SDD-vendor-ctre.md)
- [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md)
  (merges into `.claude/rules/team271-lib.md` in Phase 6)
- [ADR-006](ADR-006-ctre-phoenix6-primary-vendor.md)
