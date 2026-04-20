#!/usr/bin/env bash
# Hook: PostToolUse — yamllint on *.yml / *.yaml edits.
# Advisory only (always exits 0). Mirrors ci.yml `lint-docs` job.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

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
