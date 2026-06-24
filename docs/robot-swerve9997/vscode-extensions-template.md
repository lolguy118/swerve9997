<!-- TEMPLATE FOR FORKED ROBOT PROJECTS -- copy the JSON block below
     into .vscode/extensions.json at the robot project's repository
     root after forking. Commit the file so every contributor gets
     the same VS Code extension prompt when opening the project.
     This banner is stripped by tools/init-robot.sh. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# VS Code Extensions — <Project>

Team 271 robot projects use the same editor extension set as
Team271-Lib itself, so contributors moving between the library and a
season robot don't reconfigure their editor. Project-specific
additions (robot-unique linters, language packs, etc.) get appended
to the `recommendations` array.

> **Scope:** this template is for `<project-root>/.vscode/extensions.json`
> at the robot project's repository root, **not** the library's own
> [`../../.vscode/extensions.json`](../../.vscode/extensions.json).
> The library's file is authoritative for the library; this template
> is the recommended starting point for a robot project.

## How to use

1. Create the `.vscode/` directory at the robot project's repository
   root if it doesn't exist.
2. Copy the JSON block below into `.vscode/extensions.json`.
3. Add any robot-specific extensions to `recommendations` as needed
   (PathPlanner, game-specific linters, etc.).
4. Commit the file so every contributor sees the same prompt.

## Contents

```json
{
  "recommendations": [
    "anthropic.claude-code",
    "vscjava.vscode-gradle",
    "redhat.java",
    "naco-siren.gradle-language",
    "richardwillis.vscode-spotless-gradle",
    "ryanluker.vscode-coverage-gutters"
  ]
}
```

## Extension reference

| Extension | ID | Purpose |
| --------- | -- | ------- |
| **Claude Code** | `anthropic.claude-code` | AI-assisted development in the editor; backs the project's slash commands |
| **Gradle for Java** | `vscjava.vscode-gradle` | Gradle task runner, build visualization, and dependency inspection |
| **Language Support for Java™** | `redhat.java` | Java language server — IntelliSense, navigation, refactoring |
| **Gradle Language Support** | `naco-siren.gradle-language` | Syntax highlighting for `build.gradle` |
| **Spotless Gradle** | `richardwillis.vscode-spotless-gradle` | Apply Spotless formatting from the editor without dropping to the terminal |
| **Coverage Gutters** | `ryanluker.vscode-coverage-gutters` | Show JaCoCo line coverage in the editor gutter after `./gradlew test` |

## Companion `settings.json`

The extensions above expect a few editor settings to be set (JUnit
launcher, JaCoCo report path, Java completion preferences). A
project-tier [`settings.json`](../../.vscode/settings.json) template
is not yet scaffolded — for now, copy the library's
[`.vscode/settings.json`](../../.vscode/settings.json) as a starting
point and trim or extend to taste.

## Keeping in sync

When Team271-Lib's [`.vscode/extensions.json`](../../.vscode/extensions.json)
changes, update the JSON block above so the scaffold stays current.
The library's file is the source of truth for the shared baseline;
projects are free to add more.

See also
[`../common/guides/development-setup.md`](../common/guides/development-setup.md#recommended-extensions)
for the same extension roster inside the shared developer-setup
guide.
