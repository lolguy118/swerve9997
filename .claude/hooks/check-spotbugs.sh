#!/usr/bin/env bash
# Hook: PostToolUse — ./gradlew spotbugsMain after Edit|Write of main
# Java files. Advisory only (always exits 0). Honors CI's fail-soft
# rollout posture (ignoreFailures=true in build.gradle).

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

# SpotBugs runs on class files — depends on compileJava, which Gradle
# chains automatically. Timeout is generous because first run is slow.
OUTPUT=$(cd "$REPO_ROOT" && timeout 300 ./gradlew spotbugsMain -q 2>&1) || true

# Typical SpotBugs summary line: "SpotBugs reported 3 bug instances" or
# report-path mention of violations.
BUGS=$(echo "$OUTPUT" | grep -Ei '(SpotBugs .* bug| bug (pattern|instance))' || true)
REPORT=$(echo "$OUTPUT" | grep -Ei 'build/reports/spotbugs/.*\.(html|xml)' | head -1 || true)

if [ -n "$BUGS" ]; then
  echo "check-spotbugs: spotbugsMain reported findings after edit to $REL_PATH"
  echo "$BUGS" | head -10
  [ -n "$REPORT" ] && echo "  Report: $REPORT"
  echo "Run: ./gradlew spotbugsMain"
fi

exit 0
