# Formatting & Linting Plan

## Status: Implemented

## Overview

Automated code formatting and linting for the Team 271 library, enforced via Gradle build tooling and IDE-agnostic configuration.

## Components

### 1. Spotless (Gradle Plugin)
- **Plugin:** `com.diffplug.spotless` v7.0.4
- **Formatter:** google-java-format with AOSP variant (4-space indentation)
- **Scope:** All Java files in `src/**/*.java`
- **Features:**
  - `removeUnusedImports()` — strips dead imports
  - `trimTrailingWhitespace()` — removes trailing spaces
  - `endWithNewline()` — ensures final newline
  - `toggleOffOn()` — respects `// spotless:off` / `// spotless:on` for opt-out sections
  - `.reflowLongStrings()` — reformats long string literals

### 2. .editorconfig
- IDE-agnostic formatting hints consumed by VSCode, IntelliJ, Eclipse
- Java: 4-space indent, 120-char max line length, UTF-8, LF endings
- JSON/YAML: 2-space indent
- Markdown: trailing whitespace preserved

### 3. JUnit 5 Testing
- Test directory: `src/test/java/`
- Framework: JUnit Jupiter 5.10.1
- HAL initialization pattern for WPILib/Phoenix 6 sim tests
- Static state cleanup via reflection for singleton classes

## Commands

| Command | Purpose |
|---------|---------|
| `./gradlew spotlessCheck` | Verify formatting (fails build on violations) |
| `./gradlew spotlessApply` | Auto-fix all formatting violations |
| `./gradlew test` | Run all JUnit tests |
| `./gradlew spotlessApply test` | Format then test (recommended pre-commit) |

## Developer Workflow

1. Write code in IDE (`.editorconfig` provides baseline formatting)
2. Run `./gradlew spotlessApply` before committing
3. Run `./gradlew test` to verify tests pass
4. Commit — formatting is clean, tests are green

## Future Considerations

- **CI Integration:** Add `spotlessCheck` and `test` as required checks in GitHub Actions
- **Pre-commit Hook:** Could add a Git pre-commit hook that runs `spotlessCheck`
- **Checkstyle:** Could add Checkstyle for additional static analysis rules beyond formatting (naming conventions, Javadoc requirements, etc.) — evaluate if Spotless alone is sufficient first
- **Test Coverage Reporting:** Could add JaCoCo for coverage reporting in CI
