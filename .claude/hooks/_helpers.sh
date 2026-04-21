#!/usr/bin/env bash
# Shared helpers for .claude/hooks/*.sh scripts.
#
# Source at the top of a hook:
#
#   source "$(dirname "$0")/_helpers.sh"
#   FILE_PATH=$(parse_file_path)          # consumes stdin exactly once
#   REPO_ROOT=$(repo_root)
#
# Hook input contract (Claude Code): JSON on stdin, schema varies by
# event. Fields used here live at top level of the hook payload:
#   { "tool_input": { "file_path": "...", ... }, ... }
# The helper tolerates both shapes ("file_path" at top level or nested
# under "tool_input") so it works across PreToolUse/PostToolUse events.
#
# Design notes:
# - Read stdin once. `cat` is non-idempotent; calling it twice blocks.
# - Prefer `jq` when available (robust against escaped quotes); fall
#   back to `sed` for environments without jq (git-bash on Windows
#   often ships without it).
# - Never set `-e` here (hooks are advisory and must exit 0 on the
#   happy path). Callers can opt into stricter modes themselves.
# - `set -uo pipefail` below propagates to every hook that sources
#   this file — unbound vars become errors and pipelines fail on the
#   first non-zero exit. This is intentional: hook authors must use
#   `${VAR:-default}` for optional inputs rather than relying on the
#   empty-is-unset fallback.

set -uo pipefail

# Read hook JSON payload from stdin and echo the `file_path`.
# Echoes empty string if no file_path is present.
parse_file_path() {
  local input
  input=$(cat)
  [ -z "$input" ] && return 0

  if command -v jq >/dev/null 2>&1; then
    # Try top-level, then nested under tool_input.
    printf '%s' "$input" \
      | jq -r '(.tool_input.file_path // .file_path // empty)' 2>/dev/null
    return 0
  fi

  # sed fallback: first match of "file_path": "..." wins.
  printf '%s' "$input" \
    | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' \
    | head -1
}

# Echo the repo root (git toplevel), or `pwd` if not in a git work tree.
repo_root() {
  git rev-parse --show-toplevel 2>/dev/null || pwd
}

# Normalize a path for cross-platform comparison:
#   - Convert MSYS-style "/d/foo" to Windows-style "D:/foo"
#   - Normalize backslashes to forward slashes
#   - Uppercase the drive letter on Windows (git-bash varies)
#   - Prefer `cygpath -m` when available (git-bash on Windows)
# On Linux/macOS this is a near-no-op.
_normalize_path() {
  local p="${1:-}"
  [ -z "$p" ] && return 0
  if command -v cygpath >/dev/null 2>&1; then
    p=$(cygpath -m "$p" 2>/dev/null) || return 0
  else
    # Fallback pure-bash normalization.
    if [[ "$p" =~ ^/([a-zA-Z])/(.*)$ ]]; then
      local drive="${BASH_REMATCH[1]}"
      p="${drive^^}:/${BASH_REMATCH[2]}"
    fi
    p="${p//\\//}"
    if [[ "$p" =~ ^([a-z]):(/.*)$ ]]; then
      local d="${BASH_REMATCH[1]}"
      p="${d^^}:${BASH_REMATCH[2]}"
    fi
  fi
  printf '%s' "$p"
}

# Given an absolute file path and repo root, echo the repo-relative path.
# Echoes empty string if the path is outside the repo (caller should
# treat this as "skip"). Handles Windows path-format mismatches where
# git rev-parse returns "D:/..." but the hook payload carries "/d/...".
relative_path() {
  local abs="${1:-}"
  local root="${2:-$(repo_root)}"
  [ -z "$abs" ] && return 0
  abs=$(_normalize_path "$abs")
  root=$(_normalize_path "$root")
  local rel="${abs#"$root"/}"
  [ "$rel" = "$abs" ] && return 0
  printf '%s' "$rel"
}
