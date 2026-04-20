#!/usr/bin/env bash
# Hook: PostToolUse — run spotlessCheck after Edit|Write of Java files.
# Advisory only (always exits 0). Reports format violations if any.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

case "$REL_PATH" in
  src/main/java/*.java|src/test/java/*.java) ;;
  *) exit 0 ;;
esac

command -v "$REPO_ROOT/gradlew" >/dev/null 2>&1 || [ -x "$REPO_ROOT/gradlew" ] || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 180 ./gradlew spotlessCheck -q 2>&1) || true
VIOLATIONS=$(echo "$OUTPUT" | grep -E '^(> Task :spotless|  .*\.java$|\s+@@|The following files had format violations)' || true)

if [ -n "$VIOLATIONS" ]; then
  echo "check-spotless: spotlessCheck reported format violations after edit to $REL_PATH"
  echo "$VIOLATIONS" | head -20
  echo "Run: ./gradlew spotlessApply"
fi

exit 0
