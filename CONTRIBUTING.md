# Contributing to Team271

## Prerequisites

- **Java 17+** (required by GradleRIO)
- **WPILib** VS Code extension (includes the Gradle wrapper and toolchain)
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

The coding standard is split across a core document and topical
companions. The core
[`docs/common/Team271-Software-Coding-Standard.md`](docs/common/Team271-Software-Coding-Standard.md)
indexes the rest:

- Core — §1 Introduction, §2 Programming Language, §3 Source Code
  Presentation, §4 Coding Guidelines (router to companions)
- `-General.md`, `-Format.md`, `-Modules.md`, `-Methods.md`,
  `-Variables.md`, `-Control.md`, `-Comments.md`, `-Debug.md`,
  `-Safety.md` — the `CODE-*` rules by category
- [`team-lib/coding-standard-templates.md`](docs/team-lib/coding-standard-templates.md)
  — file and class templates for robot-project code that consumes the library
- [`-Appendices.md`](docs/common/Team271-Software-Coding-Standard-Appendices.md)
  — reference tables (final-keyword guide, unit conventions, GC, etc.)
- [`-Compliance.md`](docs/common/Team271-Software-Coding-Standard-Compliance.md)
  — §5 static analysis + tooling + §5.4 review checklist

Read at least the core doc and `-Safety.md` before your first
contribution.

## Code Review Checklist

Before opening a PR, verify the items in
[`-Compliance.md` §5.4 Code Review Checklist](docs/common/Team271-Software-Coding-Standard-Compliance.md#54-code-review-checklist).
These include state machine completeness, motor safety, subsystem
lifecycle ordering, naming conventions, and timeout protection.

## Documentation Updates

If your code change affects any of the following, **update the
corresponding design doc in the same commit:**

- Subsystem state machines, control states, or shot modes
- Controller button bindings
- Cross-subsystem coordination (feeding chain, velocity gating, post-shoot sequence)
- Auto paths or auto timing
- Homing behavior, soft limits, or safety logic
- Telemetry keys

Design docs live in [`docs/team-lib/`](docs/team-lib/). See
[`.claude/rules/docs.md`](.claude/rules/docs.md) and
[`docs/team-lib/planning/README.md`](docs/team-lib/planning/README.md)
for the authoritative map of which doc owns each topic.

## Commit Conventions

- Use imperative mood in commit messages (e.g., "add", "fix", "refactor")
- Prefix with a type when appropriate: `feat:`, `fix:`, `refactor:`, `docs:`
- Keep the first line under 72 characters
- Reference the subsystem or area in the message (e.g., `feat(control): add ProfiledPIDFOC variant`)

## Generated Files

The following files are generated or vendor-supplied and should not be
manually formatted:

- `BuildConstants.java` (gversion plugin)
- `LimelightHelpers.java` (vendor library, vendored under `util/`)

These are exempt from formatting rules but not from safety rules.
