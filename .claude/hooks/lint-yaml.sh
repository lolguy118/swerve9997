#!/usr/bin/env bash
# Hook: PostToolUse — yamllint on *.yml / *.yaml edits.
# Advisory only (always exits 0). Mirrors ci.yml `lint-docs` job.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  *.yml|*.yaml) ;;
  *) exit 0 ;;
esac

command -v yamllint >/dev/null 2>&1 || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 30 yamllint "$REL_PATH" 2>&1) || true

ISSUES=$(echo "$OUTPUT" | grep -E "^($REL_PATH|  [0-9]+:)" || true)
if [ -n "$ISSUES" ]; then
  echo "lint-yaml: yamllint issues in $REL_PATH"
  echo "$ISSUES" | head -20
  echo "Run: yamllint $REL_PATH"
fi

exit 0
