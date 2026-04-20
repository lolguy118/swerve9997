#!/usr/bin/env bash
# Hook: PostToolUse — ./gradlew jacocoTestReport after Edit|Write of
# Java files. Advisory only (always exits 0). Opt-in because a full
# test run per edit is expensive (1–3 min).
#
# Enable by exporting TEAM271_RUN_JACOCO_HOOK=1 in your shell profile.
# CI runs jacocoTestReport on every PR regardless; this hook is for
# contributors who want the local signal.

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

# Opt-in guard: skip unless explicitly enabled.
[ "${TEAM271_RUN_JACOCO_HOOK:-0}" = "1" ] || exit 0

command -v "$REPO_ROOT/gradlew" >/dev/null 2>&1 || [ -x "$REPO_ROOT/gradlew" ] || exit 0

echo "check-jacoco: running jacocoTestReport (TEAM271_RUN_JACOCO_HOOK=1). This takes 1–3 min."
OUTPUT=$(cd "$REPO_ROOT" && timeout 600 ./gradlew jacocoTestReport -q 2>&1) || true

# Surface test failures; coverage itself is reported to artifact in CI.
FAILURES=$(echo "$OUTPUT" | grep -E '(FAILED|BUILD FAILED|^\S+ > \S+ FAILED$)' || true)
if [ -n "$FAILURES" ]; then
  echo "check-jacoco: test run failed during coverage report"
  echo "$FAILURES" | head -10
  echo "Run: ./gradlew test jacocoTestReport"
fi

REPORT="$REPO_ROOT/build/reports/jacoco/test/html/index.html"
if [ -f "$REPORT" ]; then
  echo "  Report: build/reports/jacoco/test/html/index.html"
fi

exit 0
