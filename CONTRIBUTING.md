# Contributing to Robot-2026-Comp

## Prerequisites

- **Java 17** (required by GradleRIO 2026)
- **WPILib 2026** VS Code extension (includes the Gradle wrapper and toolchain)
- Clone the repo and verify it builds: `./gradlew build`

## Before Every Commit

Every file you check in **must** pass a clean compile and format check.
Run these commands from the repo root before committing:

```bash
# 1. Auto-format all source files (Google Java Format, AOSP 4-space indent)
./gradlew spotlessApply

# 2. Verify formatting passes (catches anything spotlessApply couldn't fix)
./gradlew spotlessCheck

# 3. Full compile — must finish with zero errors and zero warnings
./gradlew build
```

> **Note:** Spotless runs automatically before compilation
> (`compileJava` depends on `spotlessApply`), so `./gradlew build` will
> format your code as a side effect. Running `spotlessApply` explicitly
> first is still recommended so you can review formatting changes in your
> diff before committing.

### What Spotless handles for you

- **Indentation:** 4 spaces (AOSP standard) — no tabs
- **Import order:** sorted automatically, unused imports removed
- **Trailing whitespace:** stripped
- **Line endings:** LF only (enforced by `.gitattributes`)
- **Long strings:** reflowed automatically

You should not need to manually format code. If Spotless reformats
something you want to keep as-is (e.g., an alignment table in
constants), use `// spotless:off` / `// spotless:on` markers sparingly.

## Coding Standard

The full coding standard lives in
[`docs/team271-java-coding-standard.md`](docs/team271-java-coding-standard.md).
Key sections:

- **Section 4** — Language rules (naming, safety, state machines, GC)
- **Section 5.1–5.3** — Tooling (Spotless, compiler warnings, JVM config)
- **Section 5.4** — Code review checklist (items that automated tools cannot catch)

Read at least Sections 4 and 5 before your first contribution.

## Code Review Checklist

Before opening a PR, verify the items in
[Section 5.4](docs/team271-java-coding-standard.md) of the coding
standard. These include state machine completeness, motor safety,
subsystem lifecycle ordering, naming conventions, and timeout protection.

For a comprehensive architecture reference and detailed review checklist,
see the [Code Review Prompt](docs/prompts/code-review-prompt-teamlib.md).

## Documentation Updates

If your code change affects any of the following, **update the
corresponding design doc in the same commit:**

- Subsystem state machines, control states, or shot modes
- Controller button bindings
- Cross-subsystem coordination (feeding chain, velocity gating, post-shoot sequence)
- Auto paths or auto timing
- Homing behavior, soft limits, or safety logic
- Telemetry keys

Design docs live in [`docs/`](docs/). See the Documentation Rules
section of [`CLAUDE.md`](CLAUDE.md) for which doc is authoritative for
each behavior.

## Commit Conventions

- Use imperative mood in commit messages (e.g., "add", "fix", "refactor")
- Prefix with a type when appropriate: `feat:`, `fix:`, `refactor:`, `docs:`
- Keep the first line under 72 characters
- Reference the subsystem or area in the message (e.g., `feat: add DEPOT shot mode to launcher`)

## Generated Files

The following files are generated and should not be manually formatted:

- `BuildConstants.java` (gversion plugin)
- `TunerConstants.java` (CTRE Tuner X export)
- `LimelightHelpers.java` (vendor library)

These are exempt from formatting rules but not from safety rules.
