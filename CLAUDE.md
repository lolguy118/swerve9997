# CLAUDE.md

> **AI maintenance rule:** This file is a routing index for AI/LLM
> context only. It must NOT contain design information — only
> references to authoritative documents.
>
> Do NOT duplicate content from:
>
> - `CONTRIBUTING.md` (root) — workflow, PRs, commits, linting
> - `docs/common/coding-standard/Team271-Software-Coding-Standard*.md` — coding rules
> - `docs/common/planning/` — shared planning framework
> - `docs/team-lib/planning/` — library SDP, SRS, SVP, SCMP, SDDs, ADRs
> - `docs/team-lib/guides/` — contributor tutorials
> - `docs/team-lib/coding-standard/coding-standard-teamlib-rules.md` — library-specific `CODE-LIB-*` rules
> - `docs/team-lib/coding-standard/coding-standard-library-notes.md` — library bindings for common rules
> - `docs/robot-yyyy/` — scaffolding for consuming robot projects
>   (code templates, project coding-standard, and planning scaffolds
>   — SDP/SRS/SVP/SCMP plus ADR, SDD, guides, and prompts indexes;
>   copy to `docs/robot-2026/`, `docs/robot-2027/`, etc.)
> - `.claude/rules/` — path-scoped AI guardrails
>
> If new information needs a home, add it to the appropriate
> authoritative document above, then reference it here.

## Project Overview

Team 271's reusable FRC library. See [`README.md`](README.md) for the
project overview and quick start. Docs are organised in three tiers —
common to every 271 project, library-specific, project-specific — see
[`docs/README.md`](docs/README.md) for the layout. A robot project
consuming this library vendors `docs/common/` and `docs/team-lib/` and
adds its own `docs/<robot-name>/` without path collision.

## Architecture

See [docs/team-lib/planning/README.md](docs/team-lib/planning/README.md)
for the planning-doc map and
[docs/team-lib/internal/team271-lib-dependency-diagram.mmd](docs/team-lib/internal/team271-lib-dependency-diagram.mmd)
for the six-layer graph. The layering decision is
[ADR-003](docs/team-lib/planning/adr/ADR-003-layered-architecture.md).

## Authoritative References

- **Coding standard:** [`docs/common/coding-standard/README.md`](docs/common/coding-standard/README.md)
  indexes the core document
  [`Team271-Software-Coding-Standard.md`](docs/common/coding-standard/Team271-Software-Coding-Standard.md)
  and eleven topical companions (General, Format, Modules, Methods,
  Variables, Control, Comments, Debug, Safety, Compliance,
  Appendices). Library-specific templates and notes live in
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
  - [`lib-reviewer`](.claude/agents/lib-reviewer.md) — subagent.
    Applies the full review checklist from
    [`docs/team-lib/prompts/code-review-prompt-teamlib.md`](docs/team-lib/prompts/code-review-prompt-teamlib.md)
    against a file list or branch diff.
- **Pre-merge enforcement:** `.claude/hooks/` runs markdown / YAML /
  shell linters, a batched Java compile + Spotless + Checkstyle step,
  and a docs-sweep wrapper; opt-in Java hooks (SpotBugs, Javadoc,
  JaCoCo) activate with `TEAM271_RUN_SPOTBUGS_HOOK=1`,
  `TEAM271_RUN_JAVADOC_HOOK=1`, `TEAM271_RUN_JACOCO_HOOK=1`.
  Authoritative roster with triggers and cold-start costs in
  [SVP §6](docs/team-lib/planning/SVP.md#6-hooks-as-pre-merge-gates-library-roster).
- **Language + toolchain:** Java 17 + GradleRIO. Details in
  [SCS §2](docs/common/coding-standard/Team271-Software-Coding-Standard.md#2-language-and-build) and
  [SDP §4](docs/team-lib/planning/SDP.md#4-development-environment-library-pins). Decision:
  [ADR-002](docs/team-lib/planning/adr/ADR-002-java17-wpilib-gradlerio-toolchain.md).
- **Build system:** Gradle + GradleRIO. See `build.gradle`; version
  policy in [SCMP §3](docs/team-lib/planning/SCMP.md#3-library-versioning).
- **CI:** GitHub Actions live. `.github/workflows/ci.yml` gates PRs on
  Spotless, compile + Error Prone, test, Javadoc, Checkstyle, SpotBugs
  (fail-soft during rollout), JaCoCo coverage (with PR comment), build,
  markdownlint, yamllint, `verify-docs.sh`, and ShellCheck of
  `.claude/hooks/` — ubuntu-24.04 / JDK 17. Supporting workflows:
  `claude-code-review.yml` (advisory AI review),
  `dependency-submission.yml` (supply-chain graph on push to `main`),
  `vendordep-freshness.yml` (weekly upstream version check). Local
  pre-edit gates additionally run through `.claude/hooks/`.
  See [SVP §7](docs/team-lib/planning/SVP.md#7-ci-pipeline-gates-library-specific).
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
