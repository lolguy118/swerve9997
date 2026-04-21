#!/usr/bin/env bash
# Hook: PostToolUse — warn if a changed doc references a known-deleted
# class name. Advisory only. List lives in .claude/rules/deprecated-symbols.txt.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

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
