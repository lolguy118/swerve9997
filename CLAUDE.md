# CLAUDE.md

> **AI maintenance rule:** This file is a routing index for AI/LLM
> context only. It must NOT contain design information — only
> references to authoritative documents.
>
> Do NOT duplicate content from:
>
> - `CONTRIBUTING.md` (root) — workflow, PRs, commits, linting
> - `docs/coding-standard/` — coding rules
> - `docs/common/planning/` — shared planning framework
> - `docs/team-lib/planning/` — library SDP, SRS, SVP, SCMP, SDDs, ADRs
> - `docs/team-lib/guides/` — contributor tutorials
> - `docs/team-lib/coding-standard/` — library-API bindings for the common rules (the `CODE-LIB-*` rules now live in the coding standard)
> - `docs/robot-yyyy/` — scaffolding renamed in place when a robot
>   project forks this repo (code templates, project coding-standard,
>   and planning scaffolds — SDP/SRS/SVP/SCMP plus ADR, SDD, guides,
>   and prompts indexes; renamed to `docs/robot-2026/`,
>   `docs/robot-2027/`, etc. by the `tools/` init script)
> - `.claude/rules/` — path-scoped AI guardrails
>
> If new information needs a home, add it to the appropriate
> authoritative document above, then reference it here.

## Project Overview

Team 271's reusable FRC library. See [`README.md`](README.md) for the
project overview and quick start. Docs are organised in three tiers —
common to every 271 project, library-specific, project-specific — see
[`docs/README.md`](docs/README.md) for the layout. Each season's
robot project is a **fork** of this repo: the init script under
`tools/` renames `docs/robot-yyyy/` to `docs/<robot-name>/` and the
`com.team271.swerve9997` package to the robot's own package; `common/`
and `team-lib/` stay in place as inherited reference material
([ADR-001](docs/team-lib/planning/adr/ADR-001-team271-lib-standalone-library.md)).

## Architecture

See [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
for the planning-doc map and
[docs/team-lib/internal/team271-lib-dependency-diagram.mmd](docs/team-lib/internal/team271-lib-dependency-diagram.mmd)
for the six-layer graph. The layering decision is
[ADR-003](docs/team-lib/planning/adr/ADR-003-layered-architecture.md).

## Authoritative References

- **Coding standard:** [`docs/coding-standard/README.md`](docs/coding-standard/README.md)
  indexes the Java + FRC standard
  ([`java/Standard.md`](docs/coding-standard/java/Standard.md),
  [`frc/Standard.md`](docs/coding-standard/frc/Standard.md), plus the
  `CODE-LIB-*` library tier) and the project
  [`supplement`](docs/coding-standard/team271-lib-supplement.md). It is
  adopted from the project-template; the legacy
  [`docs/common/coding-standard/`](docs/common/coding-standard/) is
  retired. Library-API bindings live in
  [`docs/team-lib/coding-standard/`](docs/team-lib/coding-standard/).
- **Planning framework (common):** [docs/common/planning/README.md](docs/common/planning/README.md)
  — shared SemVer / phase-model / verification-framework policy.
- **Planning (library-specific):** [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
  — SDP, SRS, SVP, SCMP, plus subfolder indexes for
  [ADRs](docs/team-lib/planning/adr/README.md) and
  [SDDs](docs/team-lib/planning/sdd/README.md).
- **Guides:** common developer setup lives in
  [docs/common/guides/development-setup.md](docs/common/guides/development-setup.md);
  library-contributor guides in
  [docs/team-lib/guides/](docs/team-lib/guides/), with
  [start-here.md](docs/team-lib/guides/start-here.md) as the first
  read for new library contributors.
- **Library conventions:** [CONTRIBUTING.md](CONTRIBUTING.md).
- **AI guardrails:** [`.claude/rules/`](.claude/rules/) — rules
  auto-discovered by Claude Code, each self-scoped via `paths:`
  frontmatter (see [Claude Rules](#claude-rules) below).
- **AI-assisted tooling:**
  - [`/lib-review`](.claude/commands/lib-review.md) — slash command.
    Runs a branch-level library code review, delegating to the
    `lib-reviewer` agent for a Blocker / Should-fix / Nits punch list.
  - [`/doc-sync-check`](.claude/commands/doc-sync-check.md) — slash
    command. Checks that code changes on the current branch have
    matching design-doc updates and flags stale references to
    deleted symbols.
  - [`/doc-review-loop-init`](.claude/commands/doc-review-loop-init.md) —
    slash command. Authors (does not launch) a gap-driven
    documentation-review loop; fill its `<OWNER_GOALS>` first, or delete it.
  - [`/doc-review-loop-run`](.claude/commands/doc-review-loop-run.md) —
    slash command. Launches the authored loop (optional `[max-iterations]`,
    default 40; needs the `ralph-loop` plugin).
  - [`/impl-review-loop-init`](.claude/commands/impl-review-loop-init.md) —
    slash command. Authors (does not launch) a spec-driven
    implementation-review loop; fill its `<IMPL_SCOPE>` first, or delete it.
  - [`/impl-review-loop-run`](.claude/commands/impl-review-loop-run.md) —
    slash command. Launches the authored loop (optional `[max-iterations]`,
    default 40; needs the `ralph-loop` plugin).
  - [`lib-reviewer`](.claude/agents/lib-reviewer.md) — subagent.
    Applies the full review checklist from
    [`docs/team-lib/prompts/code-review-prompt-teamlib.md`](docs/team-lib/prompts/code-review-prompt-teamlib.md)
    against a file list or branch diff.
  - [`sim-test-writer`](.claude/agents/sim-test-writer.md) —
    subagent. Generates a JUnit 5 + WPILib HAL simulation test
    scaffold for one target class, mirroring the conventions of
    the nearest sibling test (HAL init, `CTREManager.resetForTesting`,
    unique CAN IDs, lifecycle smoke tests). Used after adding a new
    subsystem, hardware wrapper, or controller.
  - [`frc-docs` MCP server](.mcp.json) — documentation search across
    WPILib, CTRE Phoenix, REV, Redux, and PhotonVision via
    [first-agentic-csa](https://github.com/ramalamadingdong/agentic-csa).
    Usage policy in
    [`.claude/rules/frc-docs-mcp.md`](.claude/rules/frc-docs-mcp.md);
    requires `uv` installed locally (see
    [`development-setup.md`](docs/common/guides/development-setup.md)).
- **Pre-merge enforcement:** local hooks under `.claude/hooks/` are
  bound by [`.claude/settings.json`](.claude/settings.json); the
  authoritative roster with triggers, cold-start costs, and opt-in
  env vars is
  [SVP §6](docs/team-lib/planning/SVP.md#6-hooks-as-pre-merge-gates-library-roster).
- **Language + toolchain:** Java 17 + GradleRIO. Details in
  [SCS §2](docs/coding-standard/java/Standard.md#2-language-and-build) and
  [SDP §4](docs/team-lib/planning/SDP.md#4-development-environment-library-pins). Decision:
  [ADR-002](docs/team-lib/planning/adr/ADR-002-java17-wpilib-gradlerio-toolchain.md).
- **Build system:** Gradle + GradleRIO. See `build.gradle`; version
  policy in [SCMP §3](docs/team-lib/planning/SCMP.md#3-library-versioning).
- **CI:** workflows under [`.github/workflows/`](.github/workflows/);
  authoritative gate roster in
  [SVP §7](docs/team-lib/planning/SVP.md#7-ci-pipeline-gates-library-specific).
- **Platform support:** RoboRIO 2 + desktop sim on Windows/macOS/Linux.
  Matrix in [SDP §5](docs/team-lib/planning/SDP.md#5-platform-matrix-library-specific-deltas).
- **Vendor dependencies:** CTRE Phoenix 6, WPILib, AdvantageKit,
  PathPlanner. Process in
  [SCMP §4](docs/team-lib/planning/SCMP.md#4-vendordep-management-team271-lib-specifics);
  vendor decision in
  [ADR-008](docs/team-lib/planning/adr/ADR-008-ctre-phoenix6-primary-vendor.md).
  `vendordeps/*.json` is authoritative.

## Reference URLs

External vendor and tool documentation links live in
[`docs/reference-urls.md`](docs/reference-urls.md) — keeps this
file focused on project routing and pushes the URL list one click
away.

## Claude Rules

Rules live in [`.claude/rules/`](.claude/rules/) and are
auto-discovered by Claude Code. Each rule declares its own
`paths:` frontmatter, so Claude loads each rule only when reading
files inside its declared scope.

### Skills

Skills (user-invocable slash-command workflows) live in
[`.claude/skills/`](.claude/skills/) — one folder per skill holding a
`SKILL.md`: [`new-adr`](.claude/skills/new-adr/SKILL.md),
[`new-sdd`](.claude/skills/new-sdd/SKILL.md), and
[`new-subsystem`](.claude/skills/new-subsystem/SKILL.md).
