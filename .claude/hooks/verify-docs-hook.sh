#!/usr/bin/env bash
# Hook: PostToolUse — wraps verify-docs.sh so it runs when a doc is
# edited. The underlying script is the authoritative CI sweep and must
# keep its exit-1-on-fail semantics; this wrapper self-filters by path
# and always exits 0 (advisory, like the other hooks).

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  docs/*.md|CLAUDE.md|CONTRIBUTING.md) ;;
  *) exit 0 ;;
esac

SWEEP="$REPO_ROOT/.claude/hooks/verify-docs.sh"
[ -x "$SWEEP" ] || exit 0

OUTPUT=$(cd "$REPO_ROOT" && timeout 60 "$SWEEP" 2>&1) || true

# Only surface issues, not the "OK: all doc checks passed" line.
case "$OUTPUT" in
  *"OK: all doc checks passed"*) exit 0 ;;
esac

if [ -n "$OUTPUT" ]; then
  echo "verify-docs: sweep found issues after edit to $REL_PATH"
  echo "$OUTPUT" | head -30
fi

exit 0
