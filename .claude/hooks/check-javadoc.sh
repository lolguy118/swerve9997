#!/usr/bin/env bash
# Hook: PostToolUse — ./gradlew javadoc after Edit|Write of main Java
# files. Advisory only (always exits 0). Mirrors CI doclint posture.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

case "$REL_PATH" in
  src/main/java/*.java) ;;
  *) exit 0 ;;
esac

command -v "$REPO_ROOT/gradlew" >/dev/null 2>&1 || [ -x "$REPO_ROOT/gradlew" ] || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 240 ./gradlew javadoc -q 2>&1) || true

# Javadoc prints "error:" / "warning:" prefixed lines for doclint issues.
ISSUES=$(echo "$OUTPUT" | grep -E '\.java:[0-9]+: (error|warning):' || true)

if [ -n "$ISSUES" ]; then
  echo "check-javadoc: javadoc reported issues after edit to $REL_PATH"
  echo "$ISSUES" | head -20
  echo "Run: ./gradlew javadoc"
fi

exit 0
