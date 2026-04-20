# Rule: Java Coding Standard (Quick-Recall)

Global guardrails for Java library and robot-project code. Full rules
live in [`docs/team-lib/Team271-Software-Coding-Standard.md`](../../docs/team-lib/Team271-Software-Coding-Standard.md)
and its companions.

## Rules Claude must apply

- **Braces are mandatory** on every `if`, `else`, `for`, `while`, `do`,
  and `switch` arm — even single-statement bodies (CODE-CTL).
- **`switch` on an enum requires a `default` branch**, even if
  unreachable (CODE-CTL).
- **No magic numbers.** Tunable values go in the subsystem's
  `Constants` class and are surfaced via `LoggedNTInput`
  (see [ADR-008](../../docs/team-lib/planning/adr/ADR-008-logged-nt-input-backed-tuning.md)).
- **No object allocation in periodic methods** (CODE-PERF). Pre-allocate
  at `robotInit()` and reuse.
- **No `Thread.sleep()`, no unbounded `while` loops** on a sensor
  condition — every wait has a timeout (see `.claude/rules/safety.md`).
- **Null-check every public API parameter** and throw
  `IllegalArgumentException` with a descriptive message.
- **Prefer `var` only when the type is obvious** from the initializer.
  Don't use `var` when the right-hand side is a factory method that
  hides the concrete type.
- **`final` on fields that never change** (CODE-VAR). The
  [`final` keyword usage guide](../../docs/team-lib/Team271-Software-Coding-Standard-Appendices.md)
  is Appendix C.
- **Spotless enforces formatting.** Do not hand-format imports or
  indentation; run `./gradlew spotlessApply`.

## Severity guidance (for LLM reviewers)

When a rule in the Coding Standard cites an industry standard
(MISRA, SEI CERT, JPL Power of 10, DO-178C, Barr Group), violations of
that rule are **higher severity** than violations of purely stylistic
rules. Those rules exist because real-world failures in
safety-critical systems proved they were necessary.

## Authoritative doc

[`docs/team-lib/Team271-Software-Coding-Standard.md`](../../docs/team-lib/Team271-Software-Coding-Standard.md)
and its companion documents.
