#!/usr/bin/env bash
# Hook: PostToolUse — ./gradlew javadoc after Edit|Write of main Java
# files. Advisory only (always exits 0). Opt-in because doclint on a
# full source tree is slow (~15–30s).
#
# Enable by exporting TEAM271_RUN_JAVADOC_HOOK=1 in your shell
# profile. CI runs javadoc on every PR with doclint enabled; this hook
# is for local signal during iteration.

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

[ "${TEAM271_RUN_JAVADOC_HOOK:-0}" = "1" ] || exit 0
[ -x "$REPO_ROOT/gradlew" ] || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 240 ./gradlew javadoc -q 2>&1) || true

ISSUES=$(printf '%s\n' "$OUTPUT" | grep -E '\.java:[0-9]+: (error|warning):' || true)

if [ -n "$ISSUES" ]; then
  echo "check-javadoc: javadoc reported issues after edit to $REL_PATH"
  printf '%s\n' "$ISSUES" | head -20
  echo "Run: ./gradlew javadoc"
fi

exit 0
