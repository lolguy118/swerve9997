#!/usr/bin/env bash
# Hook: PostToolUse — batched Java checks after Edit|Write of .java.
# Runs compile + spotlessCheck + checkstyleMain/Test in ONE gradle
# invocation so the JVM cold-starts once instead of three times.
# Advisory only (always exits 0). Reports any issues found.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  src/main/java/*.java) COMPILE_TASK="compileJava";   CHECKSTYLE_TASK="checkstyleMain" ;;
  src/test/java/*.java) COMPILE_TASK="compileTestJava"; CHECKSTYLE_TASK="checkstyleTest" ;;
  *) exit 0 ;;
esac

[ -x "$REPO_ROOT/gradlew" ] || exit 0

# One invocation, three tasks. `--continue` so a spotless failure doesn't
# hide a checkstyle one, and vice versa.
OUTPUT=$(cd "$REPO_ROOT" && timeout 240 ./gradlew -q --continue \
  "$COMPILE_TASK" spotlessCheck "$CHECKSTYLE_TASK" 2>&1) || true

COMPILE_ERRORS=$(printf '%s\n' "$OUTPUT" | grep -E '^\S+\.java:[0-9]+: error:' || true)
SPOTLESS_VIOLATIONS=$(printf '%s\n' "$OUTPUT" \
  | grep -E '^(> Task :spotless|  .*\.java$|\s+@@|The following files had format violations)' || true)
CHECKSTYLE_VIOLATIONS=$(printf '%s\n' "$OUTPUT" \
  | grep -E '\[(ERROR|WARN)\].*\.java:[0-9]+' || true)

found_any=0
if [ -n "$COMPILE_ERRORS" ]; then
  echo "check-java: $COMPILE_TASK reported errors after edit to $REL_PATH"
  printf '%s\n' "$COMPILE_ERRORS" | head -20
  found_any=1
fi
if [ -n "$SPOTLESS_VIOLATIONS" ]; then
  [ "$found_any" = 1 ] && echo ""
  echo "check-java: spotlessCheck reported format violations after edit to $REL_PATH"
  printf '%s\n' "$SPOTLESS_VIOLATIONS" | head -20
  echo "Run: ./gradlew spotlessApply"
  found_any=1
fi
if [ -n "$CHECKSTYLE_VIOLATIONS" ]; then
  [ "$found_any" = 1 ] && echo ""
  echo "check-java: $CHECKSTYLE_TASK reported violations after edit to $REL_PATH"
  printf '%s\n' "$CHECKSTYLE_VIOLATIONS" | head -20
  echo "Run: ./gradlew $CHECKSTYLE_TASK"
fi

exit 0
