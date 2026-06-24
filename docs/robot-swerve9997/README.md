# Robot Project Template (`robot-yyyy/`)

Scaffolding that becomes a robot project's `docs/<project>/`
documentation tier after the fork-and-rename bootstrap. Each file
here has a `<!-- TEMPLATE FOR ... -->` banner at the top that the
init script strips during initialization.

> This folder is not loaded, vendored, or linked from the library's
> own docs outside of this README. It exists so that every new robot
> project has a well-defined starting point for its project-level
> documentation — the init script renames this directory to
> `docs/<project>/` in the forked repo.

The consumption-model rationale (why fork rather than depend on a
published artifact) is recorded in
[ADR-001](../team-lib/planning/adr/ADR-001-team271-lib-standalone-library.md).

## Contents

### Project-root templates

| File | Purpose |
| ---- | ------- |
| [`coding-standard.md`](coding-standard.md) | Project-level coding standard (`CODE-<PROJECT>-NNN` rules, deviations from inherited standards, rule-precedence hierarchy) |
| [`subsystem-template.md`](subsystem-template.md) | Java code template for a robot-project subsystem — singleton pattern, lifecycle hooks, Globals registration, file organization |
| [`constants-template.md`](constants-template.md) | Java code template for a robot-project `Constants.java` — CAN bus names, nested per-subsystem constant classes |
| [`input-driver-template.md`](input-driver-template.md) | Java code template for the operator-input class (`InputDriver`) — extends a library input base, connection-guarded semantic getters |
| [`vscode-extensions-template.md`](vscode-extensions-template.md) | Editor-tooling template — recommended VS Code extensions for the robot project's `.vscode/extensions.json` |

### Planning scaffolds ([`planning/`](planning/))

| File | Purpose |
| ---- | ------- |
| [`planning/README.md`](planning/README.md) | Planning-doc index, document map, subsystem-to-SDD map, inherited-library-decisions pointer |
| [`planning/SDP.md`](planning/SDP.md) | Project Software Development Plan template (phases, milestones, pin overrides, deviations) |
| [`planning/SRS.md`](planning/SRS.md) | Project Software Requirements Specification template (functional, non-functional, per-subsystem, traceability) |
| [`planning/SVP.md`](planning/SVP.md) | Project Software Verification Plan template (test levels, coverage targets, CI gates) |
| [`planning/SCMP.md`](planning/SCMP.md) | Project Software Configuration Management Plan template (project versioning, fork tag record, deviations) |
| [`planning/adr/README.md`](planning/adr/README.md) | ADR index + template for project-scope architectural decisions |
| [`planning/sdd/README.md`](planning/sdd/README.md) | SDD index + nine-section template pointer for per-subsystem design descriptions |

### Guides and prompts

| File | Purpose |
| ---- | ------- |
| [`guides/README.md`](guides/README.md) | Project guides index (onboarding, mechanism tuning, driver practice) |
| [`prompts/README.md`](prompts/README.md) | Optional project-scope AI review prompts (most projects don't need this) |

## Fork-and-rename workflow

1. **Fork Team271-Lib** at the chosen season tag into a new
   repository. The simplest path is "Use this template" on GitHub
   followed by a local clone; `git clone` + `git remote remove origin`
   also works.

2. **Run the init script** from the new repo's root:

   ```bash
   ./tools/init-robot.sh robot-2026
   ```

   This renames `docs/robot-yyyy/` to `docs/robot-2026/`, renames
   the `com.team271.swerve9997` Java package to `com.team271.robot2026`
   (dashes stripped by default — pass a second argument to override),
   updates `ROBOT_MAIN_CLASS` in `build.gradle`, and strips the
   scaffold banners from the renamed docs. The script refuses to run
   on an unclean working tree — commit or stash first.

3. **Fill in placeholders** that remain after the rename:

   ```bash
   grep -rn '<[Pp]roject>\|<PROJECT>' docs/robot-2026 README.md CLAUDE.md CONTRIBUTING.md
   ```

4. **Record the fork's origin tag** in `docs/robot-2026/planning/SCMP.md`
   so future maintainers know which library baseline the project
   started from (see
   [library SCMP §3](../team-lib/planning/SCMP.md#3-library-versioning)).

5. **Tailor the project coding standard.** Delete example
   `CODE-<PROJECT>-NNN` rules that do not apply and add your own,
   keeping the numbering convention documented in
   [`coding-standard.md`](coding-standard.md).

6. **Rewrite the repo-root docs.** `README.md`, `CLAUDE.md`, and
   `CONTRIBUTING.md` at the repo root currently describe
   Team271-Lib, not your robot — rewrite them to describe the robot
   project.

7. **Verify the build** (`./gradlew build`) and commit the
   initialization as a single `chore: init <project>` commit so the
   rename diff is easy to review.
