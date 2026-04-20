# Rule: Documentation

Team271-Lib's design docs live in `docs/team-lib/` and are part of the
contract. Code changes that alter behavior require doc updates in the
same PR.

## Rules Claude must apply

- **No numeric tunables in docs.** PID gains, voltages, amps, RPS/RPM,
  duty cycles, timing windows, and thresholds belong in code
  (`Constants.java`, subsystem classes), not docs. In docs, reference
  the constant name (`kArmStatorLimit`) — never the value.
  - Allowed: datasheet values, physical dimensions, gear ratios, CAN
    IDs, rationale for *why* a value was chosen.
- **Planned features must be clearly marked.** Any section describing
  unimplemented functionality must start with
  `> **Status: Planned — Not Yet Implemented.**`
- **Telemetry keys must match `outputTelemetry()`.** When you add or
  remove a key in code, update the telemetry table in the
  corresponding design doc in the same change.
- **No cross-doc duplication.** One doc is authoritative per topic;
  others link to it. Never copy details between docs.
- **CLAUDE.md stays high-level.** Any section that grows past ~5 lines
  of detail should move to a design doc and be replaced with a link.
- **Prompt the user for doc updates** after code changes that alter
  subsystem behavior, state machines, control flow, controller
  bindings, homing, timeouts, or cross-subsystem coordination.

## Authoritative doc

The "Documentation Rules" section of
[CLAUDE.md](../../CLAUDE.md). When the user asks whether a doc update
is needed, use that checklist.
