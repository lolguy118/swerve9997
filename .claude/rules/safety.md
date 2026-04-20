# Rule: Safety and Fault Tolerance

The robot is a real machine with real consequences. Every waiting or
blocking operation in the code must fail safe.

## Rules Claude must apply

- **All waiting operations must have a timeout.** Homing sequences,
  launcher spin-up waits, PathPlanner follow-to-waypoint, sensor-gated
  state transitions — all of them. No unbounded `while` loops on a
  sensor condition. See
  [ADR-011](../../docs/team-lib/planning/adr/ADR-011-mandatory-timeouts-fail-safe.md).
- **Timeout constants must be named.** Not magic numbers. Put them in
  the subsystem's `Constants` class.
- **On timeout, fail safe:** stop motors, restore default current
  limits, transition to `IDLE`.
- **On timeout, notify the driver** via `Elastic` so the driver knows
  something went wrong. No silent timeouts.
- **Document the timeout and its fail-safe behavior** in the
  subsystem's design doc.
- **Do not suggest `--no-verify`** or other hook-skipping behavior to
  work around safety checks. If a hook fails, investigate the root
  cause. Enforced automatically by the deny list in
  [`.claude/settings.json`](../settings.json) (`--no-verify`,
  `--no-gpg-sign`, `git commit -n`).
- **Do not introduce duplicate CTRE device objects** on the same CAN
  ID — construct each device once and pass the reference.

## Code-review items to call out

When reviewing library changes, flag:

- Unit inconsistencies (rotations vs radians, RPS vs RPM, meters vs
  inches)
- Deprecated Phoenix 6 API calls
- Missing or incorrect `StatusSignal` refresh patterns
- `setCurrentLimit*` paths that don't propagate to followers
- New waiting operations without timeout + fail-safe + driver alert
- Fault-tolerance regressions per
  [SDD-subsystem.md](../../docs/team-lib/planning/sdd/SDD-subsystem.md)
  and [SDD-hardware.md](../../docs/team-lib/planning/sdd/SDD-hardware.md)

## Authoritative docs

- [ADR-010](../../docs/team-lib/planning/adr/ADR-010-subsystem-exception-isolation.md)
  — exception isolation
- [ADR-011](../../docs/team-lib/planning/adr/ADR-011-mandatory-timeouts-fail-safe.md)
  — mandatory timeouts
- [Team271-Software-Coding-Standard-Safety.md](../../docs/team-lib/Team271-Software-Coding-Standard-Safety.md)
  — CODE-SAF-* rules
- [SDD-subsystem.md](../../docs/team-lib/planning/sdd/SDD-subsystem.md)
  — fault-tolerance patterns
