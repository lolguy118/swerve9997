#!/usr/bin/env bash
# Hook: SessionStart — re-inject critical project conventions after
# context compaction so Claude doesn't forget them. Fires when the
# SessionStart `compact` matcher triggers, so it only runs after a
# compaction (not on normal startup, where CLAUDE.md + rules already
# cover this).
#
# Anything written to stdout is injected into Claude's context.
# Keep this short — every line costs tokens.

cat <<'EOF'
Project reminders (re-injected after compact):

- Team271-Lib is CTRE-focused. Only CTRE Phoenix 6 motors and
  sensors are supported (ADR-006).
- Every waiting operation (homing, launcher spin-up, etc.) must
  have a named timeout constant, a fail-safe action on timeout,
  and an Elastic driver alert (ADR-011).
- Subsystems separate desired state from actual state. Read
  sensors in robotPeriodicBefore(); act on desired state in
  robotPeriodicAfter() (ADR-014).
- All StatusSignals register with CTREManager at robotInit();
  HardwareManager.refreshAll() is the per-cycle entry point
  (ADR-007).
- No numeric tunables in docs; reference named constants (e.g.
  kArmStatorLimit), never values (.claude/rules/docs.md).
- Normative keywords: SHALL = required, SHOULD = recommended,
  MAY = optional
  (docs/common/planning/README.md#normative-keywords).
EOF
