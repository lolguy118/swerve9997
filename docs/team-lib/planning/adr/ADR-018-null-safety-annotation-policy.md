# ADR-018: Null-Safety Annotation Policy — JSpecify with NullAway Enforcement

## Status

Accepted

## Date

2026-06-19

## Context

Java has no built-in compile-time null-safety: any reference may be `null`, and a
`NullPointerException` surfaces only at runtime. On a real robot that runtime is a
match, where an unhandled NPE in a subsystem is a fault-tolerance and safety concern
(see [ADR-011](ADR-011-subsystem-exception-isolation.md) and
[ADR-012](ADR-012-mandatory-timeouts-fail-safe.md)).

The library also wraps CTRE Phoenix 6 vendor objects behind a vendor-neutral API
([ADR-005](ADR-005-passthrough-wrapper-not-wall.md)), so the contract about *what may
be null* at that boundary is exactly the kind of thing a tool should enforce rather
than leave to prose.

The [Software Verification Plan](../SVP.md) already reserves a **Planned** NullAway
(Error Prone plugin) gate that is explicitly blocked on "the null-safety annotation
ADR first," and the build already runs Error Prone. Before that gate can be enabled,
two things must be decided: which annotation set the library uses, and how it is
rolled out. The planning README's Planned-ADRs slot frames the annotation-set choice
as JSpecify vs. JetBrains vs. JSR-305. This ADR makes that decision.

## Decision

Adopt **JSpecify** (`org.jspecify.annotations`) as the library's null-safety
annotation set, enforced at build time by **NullAway** running as an **Error Prone**
plugin (Error Prone is already configured in the build).

- Mark packages `@NullMarked` at the package level (`package-info.java`), so every
  reference is **non-null by default** and only `@Nullable` references are annotated.
- Roll out **layer by layer in [ADR-003](ADR-003-layered-architecture.md) dependency
  order** — `api` → `vendor` → `hardware` → `control` → `subsystem` → `auto` —
  starting with the `api` layer, the highest-value contract surface.
- Run NullAway as a **warning** during rollout; promote it to the **CI gate** the SVP
  reserves only once a layer is fully `@NullMarked` and clean.
- Treat unannotated vendor (Phoenix 6, WPILib) packages as NullAway "unannotated" so
  their absence of annotations does not produce false positives at the wrapper
  boundary.

This ADR is **Accepted**: the policy is binding. The rollout itself (annotating
packages, wiring NullAway as a warning, then promoting it to the CI gate) proceeds
incrementally and is tracked as implementation work.

## Rationale

- **JSpecify is the modern cross-vendor standard** (Google, JetBrains, Microsoft,
  Spring/Broadcom, Uber), at 1.0, with well-defined nullness semantics for generics
  and arrays — the areas where older annotation sets are ambiguous.
- **NullAway** is the de-facto build-time nullness checker and runs as an Error Prone
  plugin, so it adds **no new build-tool category** — it slots into tooling the
  library already uses — and it understands JSpecify annotations natively.
- **`@NullMarked` package default** means the common (non-null) case needs no
  annotation, keeping annotation surface — and therefore maintenance cost — minimal.
- **Layered rollout** lets each layer stabilize before the next inherits its
  guarantees, and hardens the vendor-neutral `api` contract first.
- It **directly unblocks** the SVP's reserved NullAway gate.

## Consequences

What becomes easier:

- A whole class of NPE bugs is caught at build time instead of mid-match.
- `@Nullable` contracts at the vendor-neutral API boundary become explicit and
  tool-checked, complementing the passthrough-wrapper design.
- Adoption is incremental and low-risk — a warning per layer, not a big-bang gate.

What becomes harder or constrained:

- Contributors must annotate `@Nullable` wherever `null` is a legitimate value, and
  learn JSpecify's semantics.
- The vendor boundary needs explicit handling (NullAway "unannotated" config and/or
  `@Nullable`/non-null declarations on wrapper methods) because Phoenix 6 types carry
  no nullness annotations.
- Until a layer is `@NullMarked`, NullAway offers no guarantee there — coverage is
  only as complete as the rollout.

## Alternatives Considered

- **JSR-305 (`javax.annotation`)** — rejected. The spec is dormant and unmaintained,
  its package collides with other artifacts under the Java Platform Module System
  (split-package), and its generics nullness semantics are ambiguous.
- **JetBrains annotations (`org.jetbrains.annotations`)** — rejected as the primary
  set. Excellent for IntelliJ inspections, but weaker and less portable for
  build-time generics/array nullness, and not the emerging cross-tool standard.
- **Checker Framework (Nullness Checker)** — rejected. The most rigorous option, but
  heavyweight (a separate type-checking compile pass and a steep learning curve) and
  overkill for a student-maintained FRC library; NullAway delivers most of the value
  at a fraction of the cost and complexity.
- **No annotation policy (status quo)** — rejected. Leaves NPE risk unmanaged on a
  real robot and permanently blocks the SVP's planned NullAway gate.

## References

- [ADR-002: Java 17 + WPILib + GradleRIO Toolchain](ADR-002-java17-wpilib-gradlerio-toolchain.md)
- [ADR-003: Layered Architecture](ADR-003-layered-architecture.md) — rollout order
- [ADR-005: Passthrough — Wrapper, Not Wall](ADR-005-passthrough-wrapper-not-wall.md)
  — annotating the vendor boundary
- [ADR-011: Per-Subsystem Exception Isolation](ADR-011-subsystem-exception-isolation.md)
- [SVP](../SVP.md) — the planned NullAway (Error Prone plugin) gate this ADR unblocks
- [Planning README](../README.md) — the Planned-ADR slot this ADR fills
- JSpecify — <https://jspecify.dev/> ; NullAway — <https://github.com/uber/NullAway>
