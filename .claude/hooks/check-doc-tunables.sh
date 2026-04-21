#!/usr/bin/env bash
# Hook: PostToolUse — flag numeric tunables in design docs.
# Advisory only (always exits 0).
# Rule: CLAUDE.md "No Tunable Values in Docs".

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  docs/common/*.md|docs/team-lib/*.md) ;;
  *) exit 0 ;;
esac

# Skip files explicitly marked as tunable-allowed (rare).
grep -q '<!-- allow-tunables -->' "$FILE_PATH" 2>/dev/null && exit 0

# Units suffixed to bare numbers — classic tunable signature.
UNIT_HITS=$(grep -n -E '\b[0-9]+(\.[0-9]+)?\s*(A|V|rps|RPS|rpm|RPM|ms|hz|Hz)\b' \
  "$FILE_PATH" 2>/dev/null \
  | grep -vE '^[[:space:]]*[0-9]+:[[:space:]]*(>|<!--|\|.*datasheet)' \
  || true)

# PID gain patterns.
PID_HITS=$(grep -n -E 'k[PIDVSGA][[:space:]]*=[[:space:]]*-?[0-9]' \
  "$FILE_PATH" 2>/dev/null || true)

if [ -n "$UNIT_HITS" ] || [ -n "$PID_HITS" ]; then
  echo "check-doc-tunables: possible numeric tunables in $REL_PATH"
  echo "  Rule: CLAUDE.md 'No Tunable Values in Docs' — reference constants by name."
  [ -n "$UNIT_HITS" ] && echo "$UNIT_HITS" | head -5
  [ -n "$PID_HITS" ] && echo "$PID_HITS" | head -5
fi

exit 0
