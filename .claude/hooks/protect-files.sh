#!/usr/bin/env bash
# Hook: PreToolUse — protects files that should not be hand-edited.
#
# Exits with code 2 (block) and writes an explanation to stderr when
# Claude tries to Edit/Write:
#   - vendordeps/*.json — managed by vendordep-freshness.yml workflow
#   - .github/workflows/*.yml — CI infrastructure; risky to touch
#     without review
#
# For Accepted ADRs, writes a warning via JSON `permissionDecision:
# "ask"` so the user must approve the edit manually. The typical
# valid edit is status-change to Superseded; other edits should
# trigger a new ADR per .claude/rules/planning.md.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(repo_root)
REL_PATH=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL_PATH" ] && exit 0

# --- Hard blocks (exit 2) ---

case "$REL_PATH" in
  vendordeps/*.json)
    echo "Blocked: ${REL_PATH} is a vendor-dependency file managed by the" >&2
    echo "vendordep-freshness.yml GitHub Actions workflow. Editing by hand" >&2
    echo "risks losing the upstream jsonUrl link and breaking the weekly" >&2
    echo "freshness check. To upgrade a vendordep, follow the procedure in" >&2
    echo "docs/common/planning/configuration-management.md §4." >&2
    exit 2
    ;;
  .github/workflows/*.yml|.github/workflows/*.yaml)
    echo "Blocked: ${REL_PATH} is a GitHub Actions CI workflow file." >&2
    echo "CI changes affect every PR's merge gates and are safety-sensitive." >&2
    echo "If the edit is intentional, update the SVP and CI sections of" >&2
    echo "CLAUDE.md in the same commit, and explicitly confirm with the user" >&2
    echo "before proceeding (the user can invoke this file directly via" >&2
    echo "their editor to override this hook)." >&2
    exit 2
    ;;
esac

# --- Soft warning on Accepted ADRs (JSON 'ask' decision) ---

case "$REL_PATH" in
  docs/team-lib/planning/adr/ADR-*.md)
    # Only warn if the ADR has Status: Accepted (not Proposed).
    if grep -q '^Accepted$' "$FILE_PATH" 2>/dev/null; then
      cat <<'EOF'
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "ask",
    "permissionDecisionReason": "This is an Accepted ADR. Per .claude/rules/planning.md, Accepted ADRs are permanent — typos and reference fixes are fine, but doctrine changes should mark this ADR as 'Superseded by ADR-XXX' and write a new ADR rather than rewrite. Confirm the edit is non-doctrinal."
  }
}
EOF
      exit 0
    fi
    ;;
esac

exit 0
