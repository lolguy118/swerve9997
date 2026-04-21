#!/usr/bin/env bash
# Hook: PostToolUse — ./gradlew spotbugsMain after Edit|Write of main
# Java files. Advisory only (always exits 0). Opt-in because a cold
# SpotBugs run is slow (~30–60s).
#
# Enable by exporting TEAM271_RUN_SPOTBUGS_HOOK=1 in your shell
# profile. CI runs spotbugsMain on every PR (fail-soft during rollout
# per build.gradle); this hook is for local signal during iteration.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  src/main/java/*.java) ;;
  *) exit 0 ;;
esac

[ "${TEAM271_RUN_SPOTBUGS_HOOK:-0}" = "1" ] || exit 0
[ -x "$REPO_ROOT/gradlew" ] || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 300 ./gradlew spotbugsMain -q 2>&1) || true

BUGS=$(printf '%s\n' "$OUTPUT" | grep -Ei '(SpotBugs .* bug| bug (pattern|instance))' || true)
REPORT=$(printf '%s\n' "$OUTPUT" | grep -Ei 'build/reports/spotbugs/.*\.(html|xml)' | head -1 || true)

if [ -n "$BUGS" ]; then
  echo "check-spotbugs: spotbugsMain reported findings after edit to $REL_PATH"
  printf '%s\n' "$BUGS" | head -10
  [ -n "$REPORT" ] && echo "  Report: $REPORT"
  echo "Run: ./gradlew spotbugsMain"
fi

exit 0
