#!/usr/bin/env bash
# Hook: PostToolUse — markdownlint on .md files (native markdownlint-cli2)
# Advisory only (always exits 0). Reports issues as feedback.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  *.md) ;;
  *) exit 0 ;;
esac

command -v markdownlint-cli2 >/dev/null 2>&1 || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 30 markdownlint-cli2 "$REL_PATH" 2>&1) || true

ISSUES=$(echo "$OUTPUT" | grep "^$REL_PATH" || true)
if [ -n "$ISSUES" ]; then
  echo "markdownlint issues in $REL_PATH:"
  echo "$ISSUES"
fi

exit 0
