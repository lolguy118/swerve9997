# Rule: Passthrough — Wrapper, Not Wall

Every hardware wrapper in Team271-Lib exposes its underlying vendor
object (CTRE, WPILib) via a public getter. The library is **additive**.

## Rules Claude must apply

- When adding a new wrapper (around a motor, encoder, IMU, etc.),
  include a `getUnderlying<Vendor>Object()` method that returns the
  raw vendor type. Do not hide it behind private access.
- When adding a new convenience method, do **not** remove or deprecate
  the raw-object getter. The library must never block access to a new
  CTRE or WPILib feature that hasn't been wrapped yet.
- When the user needs a feature that isn't wrapped, prefer the
  passthrough getter over adding a new wrapper method — unless the
  feature genuinely belongs in the library (used by ≥2 callers, or
  crosses subsystem boundaries).
- Return *raw* vendor types from passthrough getters, not wrapped
  types. The whole point is direct access.

## Authoritative doc

[docs/team-lib/passthrough-design.md](../../docs/team-lib/architecture/passthrough-design.md)
lists every passthrough getter in the library and the philosophy
behind the design.
