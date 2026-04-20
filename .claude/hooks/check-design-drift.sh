#!/usr/bin/env bash
# Hook: PostToolUse — nudge the user about doc updates when a
# behavior-defining Java class is edited. Advisory only.

FILE_PATH=$(echo "$TOOL_INPUT" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$FILE_PATH" ] && exit 0

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
REL_PATH="${FILE_PATH#"$REPO_ROOT"/}"
[ "$REL_PATH" = "$FILE_PATH" ] && exit 0

case "$REL_PATH" in
  src/main/java/com/team271/lib/subsystem/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-subsystem.md" ;;
  src/main/java/com/team271/lib/hardware/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-hardware.md" ;;
  src/main/java/com/team271/lib/control/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-control.md" ;;
  src/main/java/com/team271/lib/auto/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-auto.md" ;;
  src/main/java/com/team271/lib/api/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-api.md" ;;
  src/main/java/com/team271/lib/vendor/*.java|src/main/java/com/team271/lib/bridge/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-vendor-ctre.md" ;;
  src/main/java/com/team271/lib/nt/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-nt.md" ;;
  src/main/java/com/team271/lib/sysid/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-sysid.md" ;;
  src/main/java/com/team271/lib/util/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-util.md" ;;
  src/main/java/com/team271/lib/wpilib/*.java|src/main/java/com/team271/lib/*.java)
    DOC="docs/team-lib/planning/sdd/SDD-team271-lib.md" ;;
  *) exit 0 ;;
esac

echo "check-design-drift: edited $REL_PATH"
echo "  CLAUDE.md rule: behavior changes require a doc update."
echo "  Likely doc to update: $DOC"
echo "  If this edit is a rename, refactor, or no-op, you can ignore."

exit 0
