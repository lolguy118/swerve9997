#!/usr/bin/env bash
# Hook: PostToolUse — wraps verify-docs.sh so it runs when a doc is
# edited. The underlying script is the authoritative CI sweep and must
# keep its exit-1-on-fail semantics; this wrapper self-filters by path
# and always exits 0 (advisory, like the other hooks).

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

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
