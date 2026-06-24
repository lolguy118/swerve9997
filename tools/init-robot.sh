#!/usr/bin/env bash
#
# init-robot.sh — bootstrap a forked Team271-Lib repo as a robot project.
#
# Usage:
#   ./tools/init-robot.sh <project-name> [java-package]
#
# Arguments:
#   <project-name>   kebab-case identifier (letters, digits, dashes),
#                    starts with a letter. Used as the docs/ folder
#                    name. Example: "robot-2026".
#   [java-package]   optional lowercase Java package segment that
#                    replaces "libtest". Defaults to <project-name>
#                    with dashes removed. Example: "robot2026".
#
# Effects:
#   - Renames docs/robot-yyyy/ -> docs/<project-name>/
#   - Renames src/main/java/com/team271/libtest -> .../<java-package>
#   - Rewrites "com.team271.swerve9997" references in .java + build.gradle
#   - Strips "<!-- TEMPLATE FOR ... -->" scaffold banners from the
#     renamed docs
#
# Prerequisites:
#   - Run from the repository root
#   - Clean working tree (commit or stash first)
#   - Bash 4+, GNU or BSD find/grep/sed (Git Bash on Windows works)
#
# Rationale: ADR-001 — Team271-Lib as a Standalone Library, Separate
# from Robot Projects. This script implements the fork-and-rename
# bootstrap that ADR-001 commits the library to shipping.

set -euo pipefail

# ============================ argument parsing ============================

usage() {
    cat <<'EOF'
init-robot.sh — bootstrap a forked Team271-Lib repo as a robot project.

Usage:
    ./tools/init-robot.sh <project-name> [java-package]

Arguments:
    <project-name>   kebab-case identifier (e.g., "robot-2026")
    [java-package]   Java package name (defaults to <project-name>
                     with dashes stripped)

See the header of tools/init-robot.sh for full details and effects.
EOF
    exit "${1:-64}"  # EX_USAGE
}

if [[ $# -lt 1 || "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
fi

PROJECT_NAME=$1
JAVA_PACKAGE=${2:-"${PROJECT_NAME//-/}"}

if [[ ! "$PROJECT_NAME" =~ ^[a-z][a-z0-9-]*$ ]]; then
    echo "error: project-name must match ^[a-z][a-z0-9-]*$ (got '$PROJECT_NAME')" >&2
    exit 64
fi

if [[ ! "$JAVA_PACKAGE" =~ ^[a-z][a-z0-9]*$ ]]; then
    echo "error: java-package must match ^[a-z][a-z0-9]*$ (got '$JAVA_PACKAGE')" >&2
    exit 64
fi

# ============================ preflight checks ============================

if [[ ! -f "build.gradle" || ! -d "src/main/java/com/team271" ]]; then
    echo "error: run this script from the repository root" >&2
    exit 1
fi

if [[ ! -d "src/main/java/com/team271/libtest" ]]; then
    echo "error: src/main/java/com/team271/libtest/ not found — already initialized?" >&2
    exit 1
fi

if [[ ! -d "docs/robot-yyyy" ]]; then
    echo "error: docs/robot-yyyy/ not found — already initialized?" >&2
    exit 1
fi

if [[ -e "docs/$PROJECT_NAME" ]]; then
    echo "error: docs/$PROJECT_NAME/ already exists; choose a different project-name" >&2
    exit 1
fi

if [[ -e "src/main/java/com/team271/$JAVA_PACKAGE" ]]; then
    echo "error: src/main/java/com/team271/$JAVA_PACKAGE/ already exists" >&2
    exit 1
fi

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    echo "error: not inside a Git working tree" >&2
    exit 1
fi

if ! git diff-index --quiet HEAD -- 2>/dev/null; then
    echo "error: working tree is not clean; commit or stash before running" >&2
    exit 1
fi

# ============================ helpers ============================

# Portable in-place sed (GNU sed and BSD sed differ on -i semantics).
sed_inplace() {
    local expr=$1
    local file=$2
    local tmp
    tmp=$(mktemp)
    sed "$expr" "$file" > "$tmp" && mv "$tmp" "$file"
}

# ============================ rename directories ============================

echo "==> Renaming docs/robot-yyyy -> docs/$PROJECT_NAME"
git mv docs/robot-yyyy "docs/$PROJECT_NAME"

echo "==> Renaming src/main/java/com/team271/libtest -> .../com/team271/$JAVA_PACKAGE"
git mv "src/main/java/com/team271/libtest" "src/main/java/com/team271/$JAVA_PACKAGE"

# ============================ rewrite package references ============================

echo "==> Rewriting 'com.team271.swerve9997' references in source files"
while IFS= read -r f; do
    sed_inplace "s|com\\.team271\\.libtest|com.team271.$JAVA_PACKAGE|g" "$f"
done < <(grep -rl 'com\.team271\.libtest' src build.gradle 2>/dev/null || true)

# ============================ strip scaffold banners ============================

echo "==> Stripping scaffold banners from docs/$PROJECT_NAME"
while IFS= read -r -d '' f; do
    if grep -q '<!-- TEMPLATE FOR' "$f"; then
        sed_inplace '/<!-- TEMPLATE FOR/,/-->/d' "$f"
    fi
done < <(find "docs/$PROJECT_NAME" -type f -name '*.md' -print0)

# ============================ summary ============================

cat <<EOF

Initialization complete.

What changed:
  - docs/robot-yyyy/                      -> docs/$PROJECT_NAME/
  - src/main/java/com/team271/libtest/    -> .../com/team271/$JAVA_PACKAGE/
  - com.team271.swerve9997 references in .java/.gradle rewritten
  - scaffold banners stripped from docs/$PROJECT_NAME/

Next steps:
  1. Review:          git status && git diff --stat
  2. Placeholders:    grep -rn '<[Pp]roject>\|<PROJECT>' docs/$PROJECT_NAME README.md CLAUDE.md CONTRIBUTING.md
  3. Repo-root docs:  rewrite README.md, CLAUDE.md, CONTRIBUTING.md
                      (they currently describe the library, not the robot)
  4. Verify build:    ./gradlew build
  5. Commit:          git add -A && git commit -m "chore: init $PROJECT_NAME from Team271-Lib"

EOF
