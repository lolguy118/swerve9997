#!/usr/bin/env bash
# Hook: PostToolUse — shellcheck on *.sh edits.
# Advisory only (always exits 0). Mirrors ci.yml `shellcheck` job.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  *.sh|*.bash) ;;
  *) exit 0 ;;
esac

command -v shellcheck >/dev/null 2>&1 || exit 0

# Match CI severity: warning (skip style/info by default).
OUTPUT=$(cd "$REPO_ROOT" && timeout 30 shellcheck --severity=warning "$REL_PATH" 2>&1) || true

if [ -n "$OUTPUT" ]; then
  echo "lint-shell: shellcheck issues in $REL_PATH"
  echo "$OUTPUT" | head -30
  echo "Run: shellcheck --severity=warning $REL_PATH"
fi

exit 0
