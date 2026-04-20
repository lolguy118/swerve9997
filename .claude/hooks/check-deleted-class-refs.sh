#!/usr/bin/env bash
# Hook: PostToolUse — warn if a changed doc references a known-deleted
# class name. Advisory only. List lives in .claude/rules/deprecated-symbols.txt.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

case "$REL_PATH" in
  docs/**/*.md) ;;
  *) exit 0 ;;
esac

LIST="$REPO_ROOT/.claude/rules/deprecated-symbols.txt"
[ -f "$LIST" ] || exit 0

HITS=""
while IFS= read -r SYMBOL; do
  [ -z "$SYMBOL" ] && continue
  MATCH=$(grep -n -w "$SYMBOL" "$FILE_PATH" 2>/dev/null || true)
  [ -n "$MATCH" ] && HITS="$HITS$SYMBOL:\n$MATCH\n\n"
done < "$LIST"

if [ -n "$HITS" ]; then
  echo "check-deleted-class-refs: $REL_PATH references known-deleted symbols."
  echo "  Maintained list: .claude/rules/deprecated-symbols.txt"
  printf "%b" "$HITS"
fi

exit 0
