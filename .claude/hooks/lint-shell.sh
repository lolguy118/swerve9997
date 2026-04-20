#!/usr/bin/env bash
# Hook: PostToolUse — shellcheck on *.sh edits.
# Advisory only (always exits 0). Mirrors ci.yml `shellcheck` job.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

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
