#!/usr/bin/env bash
# Hook: SessionStart — `startup` matcher. Fires on fresh sessions
# only (not on resume/clear/compact — those are separate matchers).
#
# Prints *dynamic* repo state that CLAUDE.md cannot encode: current
# branch, any Proposed ADRs in flight, any uncommitted docs/ files.
# Also one-line-surfaces the three `disable-model-invocation: true`
# scaffold skills under .claude/skills/ that are user-invocable but
# not listed in the model's available-skills system reminder.
#
# Anything written to stdout is injected into Claude's context.
# Keep this terse — every line costs tokens. Failure must be silent
# (exit 0) so a missing git or detached HEAD never breaks startup.

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null) || exit 0
cd "$REPO_ROOT" || exit 0

BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)

# Proposed ADRs: after "## Status" header, the next non-blank line
# carries the status word. A one-pass awk is cheap and portable.
PROPOSED=$(awk '
  FNR==1 { in_status=0 }
  /^## Status$/ { in_status=1; next }
  in_status && NF {
    if ($0 == "Proposed") print FILENAME
    in_status=0
    nextfile
  }
' docs/team-lib/planning/adr/ADR-*.md 2>/dev/null \
  | while read -r f; do basename "$f"; done \
  | tr '\n' ' ')

MODIFIED_DOCS=$(git status --porcelain -- docs/ 2>/dev/null \
  | awk '{print $2}' \
  | head -5 \
  | tr '\n' ' ')

echo "Team271-Lib startup state:"
echo "- Branch: ${BRANCH:-unknown}"
[ -n "$PROPOSED" ]      && echo "- Proposed ADRs in flight: $PROPOSED"
[ -n "$MODIFIED_DOCS" ] && echo "- Uncommitted docs/: $MODIFIED_DOCS"

cat <<'EOF'

Scaffolding skills (user-invocable):
- /new-adr <kebab-title>   — next-sequential ADR scaffold
- /new-sdd <package>       — 9-section SDD template
- /new-subsystem <Name>    — subsystem with lifecycle hooks

Opt-in post-edit hooks (export =1 in your shell profile):
- TEAM271_RUN_SPOTBUGS_HOOK — run spotbugsMain after Java edits (~30–60s cold)
- TEAM271_RUN_JAVADOC_HOOK  — run javadoc after Java edits (~15–30s cold)
- TEAM271_RUN_JACOCO_HOOK   — run jacocoTestReport after Java edits (1–3 min)
EOF
