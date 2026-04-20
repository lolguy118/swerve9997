#!/usr/bin/env bash
# Hook: PostToolUse — ./gradlew checkstyleMain/Test after Edit|Write
# of Java files. Advisory only (always exits 0). Mirrors CI Checkstyle.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

case "$REL_PATH" in
  src/main/java/*.java) TASK="checkstyleMain" ;;
  src/test/java/*.java) TASK="checkstyleTest" ;;
  *) exit 0 ;;
esac

command -v "$REPO_ROOT/gradlew" >/dev/null 2>&1 || [ -x "$REPO_ROOT/gradlew" ] || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 180 ./gradlew "$TASK" -q 2>&1) || true
VIOLATIONS=$(echo "$OUTPUT" | grep -E '\[(ERROR|WARN)\].*\.java:[0-9]+' || true)

if [ -n "$VIOLATIONS" ]; then
  echo "check-checkstyle: $TASK reported violations after edit to $REL_PATH"
  echo "$VIOLATIONS" | head -20
  echo "Run: ./gradlew $TASK"
fi

exit 0
