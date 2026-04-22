#!/usr/bin/env bash
# Hook: PostToolUse — flag tier-boundary violations in common docs.
# Advisory only (always exits 0).
# Rule: .claude/rules/docs.md "Common docs must stay self-contained"
# and docs/common/README.md#portability.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

case "$REL_PATH" in
  docs/common/*.md) ;;
  *) exit 0 ;;
esac

# Cross-tier Markdown links: (...team-lib/...) or (...<project>/...).
# Matches both file-relative (../../team-lib/) and absolute (docs/team-lib/).
# Also catches placeholder paths like robot-<year>-reference.md that point
# at consuming-project content (the placeholder reveals tier-boundary intent).
LINK_HITS=$(grep -n -E '\]\([^)]*((team-lib|<project>|<robot>|<robot-name>)/|<year>)' \
  "$FILE_PATH" 2>/dev/null || true)

# Bare artifact references by ID (ADR-NNN, SDD-<name>). Code fences and
# placeholder examples ("ADR-NNN", "SDD-<name>") are excluded.
ID_HITS=$(grep -n -E '\b(ADR-[0-9]+|SDD-[a-z][a-z0-9-]+)\b' \
  "$FILE_PATH" 2>/dev/null \
  | grep -vE 'ADR-NNN|SDD-<|SDD-name' \
  || true)

if [ -n "$LINK_HITS" ] || [ -n "$ID_HITS" ]; then
  echo "check-common-tier-isolation: possible tier-boundary leak in $REL_PATH"
  echo "  Rule: docs/common/** must be self-contained (no team-lib/<project>/ refs)."
  echo "  See docs/common/README.md#portability and .claude/rules/docs.md."
  [ -n "$LINK_HITS" ] && echo "  Cross-tier links:" && echo "$LINK_HITS" | head -5
  [ -n "$ID_HITS" ]   && echo "  Bare artifact IDs:" && echo "$ID_HITS" | head -5
fi

exit 0
