# ADR-012: AdvantageKit for Telemetry and Replay Logging

## Status

Accepted

## Date

2026-04-20

## Context

Telemetry and logging are different concerns with overlapping
solutions:

- **Telemetry** — continuous publication of robot state to a dashboard
  during a match (driver-station, Shuffleboard, Elastic).
- **Logging** — recording state to disk for post-match analysis,
  debugging, and replay.

WPILib provides `DataLog` for on-disk logging and NetworkTables for
live telemetry. Mechanical Advantage's **AdvantageKit** bundles these
with additional features: a replay harness that re-runs robot code
against recorded inputs, structured `@AutoLogOutput` annotations, and
the `AdvantageScope` desktop log viewer.

## Decision

Team271-Lib uses AdvantageKit as its primary telemetry and logging
framework:

1. Every subsystem calls `Logger.recordOutput()` in its
   `outputTelemetry()` lifecycle hook.
2. All tunables use `LoggedNTInput`
   (see [ADR-008](ADR-008-logged-nt-input-backed-tuning.md)), which
   captures tunable reads in the log.
3. Match logs are captured as `.wpilog` files and loaded in
   AdvantageScope for post-match review.
4. `Logger.recordOutput()` publishes to NT4 too, so live dashboards
   (Elastic, Shuffleboard) work automatically.

## Rationale

1. **Log replay is transformative.** When a match goes wrong, replaying
   the log through the same robot code (on a laptop) pinpoints the
   root cause precisely. This is impossible with raw NT logging.
2. **Unified publish.** One `Logger.recordOutput()` call writes to
   both the log and NT4; no separate "log this AND publish this"
   code path.
3. **AdvantageScope.** The desktop viewer is actively maintained by
   Mechanical Advantage, integrates with Elastic, and supports 3D
   mechanism visualization.
4. **Phoenix 6 integration.** AdvantageKit + `SignalLogger` produce
   `.hoot` files on CANivore buses with timesync-aligned Phoenix
   signals, which AdvantageScope reads natively.

## Consequences

**Easier:**

- Post-match analysis is first-class.
- Tunable values are captured alongside everything else — replay is
  faithful (ADR-008 dovetail).
- Mechanism visualization via AdvantageScope aids driver debriefs.

**Harder:**

- Log size grows quickly; storage management is a robot-project
  concern.
- `Logger.recordOutput()` must be called from `outputTelemetry()`
  only, not from periodic control code (CODE-BUG rules).
- Replay fidelity depends on every input being logged — missing one
  breaks replay.

## Alternatives Considered

- **WPILib DataLog alone.** Rejected — no replay harness, no
  structured annotations.
- **Custom logging on top of NT.** Rejected — reinvents what
  AdvantageKit already provides, without the replay tool support.
- **No logging.** Rejected — post-match analysis is too valuable.

## References

- [SDD-team271-lib.md §Telemetry](../sdd/SDD-team271-lib.md)
- [SDD-sysid.md](../sdd/SDD-sysid.md)
- [ADR-008](ADR-008-logged-nt-input-backed-tuning.md)
- AdvantageKit docs: <https://docs.advantagekit.org/>
