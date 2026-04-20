#!/usr/bin/env bash
# Hook: PostToolUse — compile Java after Edit|Write.
# Advisory only (always exits 0). Reports compile errors if any.

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

OUTPUT=$(cd "$REPO_ROOT" && timeout 180 ./gradlew compileJava -q 2>&1) || true
ERRORS=$(echo "$OUTPUT" | grep -E '^\S+\.java:[0-9]+: error:' || true)

if [ -n "$ERRORS" ]; then
  echo "check-java-compiles: compileJava reported errors after edit to $REL_PATH"
  echo "$ERRORS" | head -20
fi

exit 0
