# ADR-015: LoggedNTInput-Backed Tuning — No Magic Numbers in Docs

## Status

Accepted

## Date

2026-04-20

## Context

Robot code depends on many tunable values: PID gains, current limits,
velocity targets, timing windows, soft-limit positions. These change
regularly during development and mechanism tuning. Every team has seen
the failure mode where a tunable value is written in docs, forgotten
in code, tuned on the field, and then the docs drift out of sync with
reality within days.

Separately, AdvantageKit enables log replay — a recorded match log can
be fed back through the same robot code to diagnose problems after the
fact. For replay to be faithful, every input the robot used during the
match — including tunable values read from the dashboard — must be
captured in the log at the time it was used.

## Decision

All tunable parameters live in code, typically in a subsystem's
`Constants` class. They are surfaced to the dashboard via
`LoggedNTInput`, which reads a value from NetworkTables and
simultaneously writes the same value to the AdvantageKit log on each
read. Documentation references **constant names** (e.g.,
`kArmStatorLimit`), never numeric values.

The `.claude/hooks/check-doc-tunables.sh` hook enforces this rule
automatically on every doc edit.

## Rationale

1. **Single source of truth.** Code is authoritative. Docs describe
   the rule, never the value.
2. **Replay faithfulness.** Every tunable read is captured in the
   log, so replaying the log produces the same control output the
   robot produced live.
3. **Dashboard tuning still works.** `LoggedNTInput` reads from NT4,
   so tuning via the dashboard behaves normally.
4. **Enforceable.** The hook catches violations before commit.

## Consequences

**Easier:**

- Docs never rot with respect to tuning values.
- Match replay diagnoses tuning mistakes accurately.
- `git blame` on a constant immediately shows who set its value.

**Harder:**

- A student searching docs for a number will not find it; they must
  know the constant name. (Mitigation: every tunable mentioned in
  docs includes the file path of the constant.)
- `LoggedNTInput` has a small per-read overhead (the log write).
  Acceptable in 20 ms loops.

## Alternatives Considered

- **Put values in docs.** Rejected — guaranteed to rot; every team
  that tries this regrets it within a season.
- **`NTEntry` without AdvantageKit logging.** Rejected — breaks
  replay fidelity.
- **`@Tunable` annotation on fields, reflection-driven registration.**
  Rejected — reflection in periodic code is a nonstarter; the explicit
  `LoggedNTInput` wrapper is clearer.

## References

- [SDD-nt.md §3.4 LoggedNTInput](../sdd/SDD-nt.md#34-loggedntinput)
- [SDD-team271-lib.md §3.5 Tuning Infrastructure](../sdd/SDD-team271-lib.md#35-tuning-infrastructure)
- [.claude/hooks/check-doc-tunables.sh](../../../../.claude/hooks/check-doc-tunables.sh)
- [ADR-016](ADR-016-advantagekit-logging.md)
