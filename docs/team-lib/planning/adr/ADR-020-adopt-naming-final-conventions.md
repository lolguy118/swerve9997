# ADR-020: Adopt Documented Naming & Final-Parameter Conventions via Phased Mechanical Migration

## Status

Proposed

## Date

2026-06-19

## Context

Growing the Checkstyle gate (the 2 → 12 rule expansion) surfaced a gap between the
coding standard and the existing library code. The standard mandates several naming
and keyword conventions that the code never consistently adopted:

- Instance fields **shall** use the `m` prefix
  ([CODE-VAR-001a](../../../coding-standard/java/Standard-Variables.md#code-var-001)).
- Method parameters **shall** use the `arg` prefix
  ([CODE-VAR-001b](../../../coding-standard/java/Standard-Variables.md#code-var-001)).
- The `final` keyword **shall** be used on all method parameters
  ([CODE-GEN-003b](../../../coding-standard/java/Standard-General.md#code-gen-003)).

A single exploratory Checkstyle pass (all candidate rules, `ignoreFailures`, one run)
measured the gap on `src/main`: `MemberName` (m-prefix) **487**, `ParameterName`
(arg-prefix) **279**, `FinalParameters` **163** violations — roughly 930 edits, plus
more in test sources. The conventions are partially adopted already (e.g.
`SubsystemManager` uses `mAllControllers` / `argLifecycle`), so the standard reflects
intent, not an accident — but the bulk of the code predates the convention.

The Checkstyle gate runs `maxWarnings=0` / `severity=error`, so a rule is clean-on-
arrival: it cannot be introduced with hundreds of outstanding violations. The three
rules above were therefore **deferred** from the gate-growth change. A separate,
deliberate effort is needed to close the gap. A fourth, related item —
[CODE-VAR-008](../../../coding-standard/java/Standard-Variables.md#code-var-008) "no
magic numbers" (`MagicNumber`, ~82 in main) — is *not* a mechanical rename and is
scoped out (see Alternatives).

## Decision

Adopt the documented field-naming, parameter-naming, and final-parameter conventions
in existing code via a **phased mechanical migration**, then promote each rule to the
Checkstyle gate once its phase is clean — rather than relaxing the standard or leaving
the conventions aspirational.

Execute through the repository's migration-review loop (`migration-review-loop-init`),
**one convention (one Checkstyle rule) per phase**, in ascending order of risk:

1. **`FinalParameters` (CODE-GEN-003b)** — purely additive `final` keyword; no rename.
2. **`MemberName` m-prefix (CODE-VAR-001a)** — instance-field rename with reference
   updates.
3. **`ParameterName` arg-prefix (CODE-VAR-001b)** — method-parameter rename.

Each phase: migrate the sites → confirm `./gradlew build` stays green (behavior
preserved) → add the corresponding Checkstyle rule at `maxWarnings=0` in the same PR →
update the [enforcement matrix](../../../coding-standard/java/Standard-Compliance.md).
This ADR fixes the decision and the phasing; the migrations themselves are deferred to
the migration loop.

## Rationale

- The conventions are already **normative** (sourced from Barr / MISRA in the coding
  standard) and **partially adopted**, so the standard describes the intended end
  state — the gap is technical debt, not a wrong rule.
- The changes are **mechanical and low-semantic-risk**: `final` on parameters is purely
  additive; field/parameter renames are IDE-automatable with full reference updates;
  `google-java-format` (Spotless) keeps the reformatted result stable, so the diff does
  not churn.
- **One rule per phase** keeps each PR reviewable and the gate honest — a rule is
  promoted only once its violation count is zero, mirroring the layer-by-layer rollout
  precedent in [ADR-018](ADR-018-null-safety-annotation-policy.md).
- Closing the gap **completes** the CODE-VAR-001 / CODE-GEN-003b enforcement that the
  Checkstyle gate-growth change had to leave on the table.

## Consequences

What becomes easier:

- The code matches its own standard; the three deferred Checkstyle rules can be
  promoted to gates, so the conventions are enforced going forward instead of eroding.
- New contributions are checked mechanically rather than relying on review to catch
  naming drift.

What becomes harder or constrained:

- The migrations touch nearly every source file. They **shall** be isolated, behavior-
  preserving passes (rename / keyword-only, no logic change) run through the migration
  loop, never bundled with feature work.
- Public-facing renames (e.g. protected fields visible to forking robot projects per
  [ADR-001](ADR-001-team271-lib-standalone-library.md)) follow the deprecation
  lifecycle in [`team271-lib.md`](../../../../.claude/rules/team271-lib.md) where they
  cross the library's extension surface.
- `MagicNumber` (CODE-VAR-008) is **out of scope** here; it needs semantic judgment
  (naming each constant, choosing its home) and is tracked as a separate effort.

## Alternatives Considered

- **Relax CODE-VAR-001 / CODE-GEN-003b to match the code** — rejected. It discards
  conventions the team adopted deliberately from Barr / MISRA; the standard would then
  describe nothing and the review checklist loses its anchor.
- **Leave the conventions aspirational** (documented, never enforced) — rejected. An
  unenforced rule rots, and the gap silently grows as new code is added.
- **One big-bang PR for all three conventions** — rejected. ~930 edits across three
  distinct conventions in a single PR is unreviewable and conflates additive (`final`)
  with rename changes; phase per convention instead.
- **Fold `MagicNumber` into the mechanical sweep** — rejected. Extracting magic numbers
  is not a mechanical rename; it requires naming and placement judgment, so it is a
  separate decision/effort.

## References

- [ADR-018: Null-Safety Annotation Policy](ADR-018-null-safety-annotation-policy.md)
  — precedent for phased, gate-clean enforcement rollout.
- [ADR-001: Team271-Lib as a Standalone Library](ADR-001-team271-lib-standalone-library.md)
  — the forking / extension surface that constrains public renames.
- Coding standard:
  [CODE-VAR-001](../../../coding-standard/java/Standard-Variables.md#code-var-001),
  [CODE-GEN-003](../../../coding-standard/java/Standard-General.md#code-gen-003),
  [CODE-VAR-008](../../../coding-standard/java/Standard-Variables.md#code-var-008).
- [Standard-Compliance.md](../../../coding-standard/java/Standard-Compliance.md)
  — enforcement matrix updated as each rule is promoted.
- [Planning README](../README.md) — ADR index.
