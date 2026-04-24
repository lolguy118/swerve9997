# Contributing to Team271

## Prerequisites

- **Java 17+** (required by GradleRIO)
- **WPILib** VS Code extension (includes the Gradle wrapper and toolchain)
- Clone the repo and verify it builds: `./gradlew build`

Full environment setup (WPILib installer, recommended extensions,
troubleshooting) lives in
[`docs/common/guides/development-setup.md`](docs/common/guides/development-setup.md).

## Before Every Commit

Every file you check in **must** pass a clean compile and format check.
Run these commands from the repo root before committing:

```bash
# 1. Auto-format all source files (Google Java Format, AOSP 4-space indent)
./gradlew spotlessApply

# 2. Verify formatting passes (catches anything spotlessApply couldn't fix)
./gradlew spotlessCheck

# 3. Full build — compiles with zero warnings, runs Spotless,
#    runs the JUnit 5 test suite, produces JaCoCo coverage
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
[`docs/common/coding-standard/Team271-Software-Coding-Standard.md`](docs/common/coding-standard/Team271-Software-Coding-Standard.md)
indexes the rest:

- Core — §1 Introduction, §2 Language and Build, §3 Source Code
  Presentation, §4 Coding Guidelines (router to companions)
- `-General.md`, `-Format.md`, `-Modules.md`, `-Methods.md`,
  `-Variables.md`, `-Control.md`, `-Comments.md`, `-Debug.md`,
  `-Safety.md` — the `CODE-*` rules by category
- [`-Appendices.md`](docs/common/coding-standard/Team271-Software-Coding-Standard-Appendices.md)
  — reference tables (final-keyword guide, unit conventions, GC, etc.)
- [`-Compliance.md`](docs/common/coding-standard/Team271-Software-Coding-Standard-Compliance.md)
  — §5 static analysis + tooling + §5.4 review checklist

Scaffolding for a forked robot project (subsystem, constants, and
input-driver code templates, plus a project-level coding-standard
template) lives under [`docs/robot-yyyy/`](docs/robot-yyyy/) and is
renamed in place to `docs/<project>/` by
[`tools/init-robot.sh`](tools/init-robot.sh) during project
initialization.

Read at least the core doc and `-Safety.md` before your first
contribution.

## Code Review Checklist

Before opening a PR, verify the items in
[`-Compliance.md` §5.4 Code Review Checklist](docs/common/coding-standard/Team271-Software-Coding-Standard-Compliance.md#54-code-review-checklist).
These include state machine completeness, motor safety, subsystem
lifecycle ordering, naming conventions, and timeout protection.

## Documentation Updates

If your code change affects any of the following, **update the
corresponding design doc in the same commit:**

- Subsystem lifecycle ordering, state machines, or control flow
- Homing behaviour, timeouts, or other fail-safe logic
  (see [ADR-012](docs/team-lib/planning/adr/ADR-012-mandatory-timeouts-fail-safe.md))
- Cross-subsystem coordination primitives (`StateMachine`,
  `SubsystemManager`)
- Auto composition primitives (`AutoMove`, `AutoMode`) or ordering
  semantics
- Public API surface under `com.team271.lib.api.*` — breaking
  changes trigger deprecation bookkeeping in
  [`.claude/rules/deprecated-symbols.txt`](.claude/rules/deprecated-symbols.txt)
- Telemetry keys published via `outputTelemetry()` — must match the
  SDD's telemetry table (enforced by
  [`.claude/rules/docs.md`](.claude/rules/docs.md))
- Input semantic-getter names exposed by `InputBase` subclasses

Design docs live in [`docs/team-lib/`](docs/team-lib/). See
[`.claude/rules/docs.md`](.claude/rules/docs.md) and
[`docs/team-lib/planning/README.md`](docs/team-lib/planning/README.md)
for the authoritative map of which doc owns each topic. The
[`/doc-sync-check`](.claude/commands/doc-sync-check.md) slash command
scans a branch for missing or stale doc updates.

## Commit Conventions

- Use imperative mood in commit messages (e.g., "add", "fix", "refactor")
- Prefix with a type when appropriate: `feat:`, `fix:`, `refactor:`, `docs:`
- Keep the first line under 72 characters
- Reference the subsystem or area in the message (e.g., `feat(control): add ProfiledPIDFOC variant`)

## Branches and Pull Requests

- Feature branches use `feat/<short-description>`; bug fixes use
  `fix/<short-description>`
  (see [common CM policy §2](docs/common/planning/configuration-management.md#2-baseline-control)).
- `main` is protected — no direct push, no force-push. Every change
  lands via pull request with at least one maintainer approval.
- CI gates listed in
  [SVP §7](docs/team-lib/planning/SVP.md#7-ci-pipeline-gates-library-specific)
  run on every PR (Spotless, compile + Error Prone, test, Javadoc,
  Checkstyle, SpotBugs, JaCoCo, markdownlint, yamllint, ShellCheck,
  `verify-docs.sh`). A PR may only merge when all gates are green.
- Non-trivial library changes **shall** carry
  [`/lib-review`](.claude/commands/lib-review.md) output alongside
  maintainer approval
  (see [SCMP §5](docs/team-lib/planning/SCMP.md#5-baseline-control-and-change-control)).
- Do **not** use `--no-verify` or equivalent hook-bypass flags — they
  are on the deny list in
  [`.claude/settings.json`](.claude/settings.json). If a hook fails,
  fix the underlying issue.

## Generated Files

The following files are generated or vendor-supplied and should not be
manually formatted:

- `BuildConstants.java` (gversion plugin)
- `LimelightHelpers.java` (vendor library, vendored under `util/`)

These are exempt from all `CODE-*` rules except `CODE-SAF-*`
(safety) — the full exemption is documented in
[the coding standard's §1.2 Scope](docs/common/coding-standard/Team271-Software-Coding-Standard.md#12-scope).
