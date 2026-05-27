#!/usr/bin/env bash
# verify-docs.sh — docs verification sweep for Team271-Lib.
# Prints one [SEVERITY] line per issue and a final status line.
# Exits 0 with "OK: all doc checks passed" when clean,
# else exits 1 with "FAIL: <N> issues".

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null)
if [ -z "$REPO_ROOT" ]; then
  echo "[BLOCKER] verify-docs: not inside a git repository"
  echo "FAIL: 1 issues"
  exit 1
fi
cd "$REPO_ROOT" || exit 1

PYTHON=$(command -v python3 2>/dev/null || command -v python 2>/dev/null)
if [ -z "$PYTHON" ]; then
  echo "[BLOCKER] verify-docs: python 3 not on PATH"
  echo "FAIL: 1 issues"
  exit 1
fi

ISSUES=0

count_lines() {
  [ -z "$1" ] && { echo 0; return; }
  printf '%s\n' "$1" | grep -c .
}

# ---------- Check 1 [BLOCKER] broken-link ----------
BROKEN_OUT=$("$PYTHON" - <<'PY'
import re, sys
from pathlib import Path

repo = Path('.').resolve()
targets = []
docs = repo / 'docs'
if docs.is_dir():
    targets.extend(sorted(docs.rglob('*.md')))
for extra in ('CLAUDE.md', 'CONTRIBUTING.md'):
    ep = repo / extra
    if ep.exists():
        targets.append(ep)
# Also crawl Claude rule files — they cite ADRs/SDDs by relative
# path and silently break when those targets are renamed. Same crawler
# logic; only the targets list grows.
rules_dir = repo / '.claude' / 'rules'
if rules_dir.is_dir():
    targets.extend(sorted(rules_dir.glob('*.md')))

link_re = re.compile(r'\[[^\]]*\]\(([^)]+)\)')
WHITELIST = {'robot-<year>-reference.md'}

issues = []
for f in targets:
    try:
        text = f.read_text(encoding='utf-8')
    except Exception:
        continue
    # Strip fenced code blocks and inline code so link-like syntax inside
    # examples is not treated as a real link.
    text = re.sub(r'```[\s\S]*?```', '', text)
    text = re.sub(r'`[^`\n]*`', '', text)
    for m in link_re.finditer(text):
        raw = m.group(1).strip()
        if not raw:
            continue
        # Drop title: [text](url "title") -> take first whitespace token.
        target = raw.split()[0]
        if target.startswith(('http://', 'https://', 'mailto:', '#')):
            continue
        # Strip URL fragment.
        path_part = target.split('#', 1)[0]
        if not path_part or path_part in WHITELIST:
            continue
        if path_part.startswith('/'):
            resolved = (repo / path_part.lstrip('/')).resolve()
        else:
            resolved = (f.parent / path_part).resolve()
        if resolved.name in WHITELIST:
            continue
        if not resolved.exists():
            rel = f.relative_to(repo).as_posix()
            issues.append(f"[BLOCKER] broken-link {rel}: {target}")

for line in issues:
    print(line)
PY
)
if [ -n "$BROKEN_OUT" ]; then
  printf '%s\n' "$BROKEN_OUT"
  ISSUES=$((ISSUES + $(count_lines "$BROKEN_OUT")))
fi

# ---------- Check 2 [BLOCKER] stale-path ----------
STALE_PATTERN='docs/team-lib/(architecture|control|quality|reference)/'
STALE_HITS=$(grep -rnE "$STALE_PATTERN" docs CLAUDE.md CONTRIBUTING.md .claude \
  --exclude-dir=.git \
  --exclude=verify-docs.sh \
  2>/dev/null || true)
if [ -n "$STALE_HITS" ]; then
  while IFS= read -r line; do
    [ -n "$line" ] && { echo "[BLOCKER] stale-path $line"; ISSUES=$((ISSUES + 1)); }
  done <<< "$STALE_HITS"
fi

# ---------- Check 3 [SHOULD] unresolved-tbd ----------
# Strips fenced code blocks (preserving line numbers) and inline code
# before searching, so teaching examples of the TODO convention in the
# coding standard are not reported as unresolved work.
TBD_OUT=$("$PYTHON" - <<'PY'
import re
from pathlib import Path

root = Path('docs')
issues = []
if root.is_dir():
    for f in sorted(root.rglob('*.md')):
        rel = f.as_posix()
        if rel == 'docs/team-lib/planning/SRS.md':
            continue
        try:
            text = f.read_text(encoding='utf-8')
        except Exception:
            continue
        # Replace fenced code block content with blank lines to preserve numbering.
        text = re.sub(r'```[\s\S]*?```',
                      lambda m: '\n' * m.group(0).count('\n'),
                      text)
        for i, line in enumerate(text.split('\n'), start=1):
            stripped = re.sub(r'`[^`\n]*`', '', line)
            if not re.search(r'\b(TODO|TBD)\b', stripped):
                continue
            if re.search(r'Document No\.\s*\|\s*TBD', line):
                continue
            issues.append(f"{rel}:{i}:{line.rstrip()}")

for line in issues:
    print(line)
PY
)
if [ -n "$TBD_OUT" ]; then
  while IFS= read -r line; do
    [ -n "$line" ] && { echo "[SHOULD] unresolved-tbd $line"; ISSUES=$((ISSUES + 1)); }
  done <<< "$TBD_OUT"
fi

# ---------- Check 4 [SHOULD] empty-section ----------
EMPTY_OUT=$("$PYTHON" - <<'PY'
import re, sys
from pathlib import Path

sdd_dir = Path('docs/team-lib/planning/sdd')
issues = []
if sdd_dir.is_dir():
    for f in sorted(sdd_dir.glob('*.md')):
        try:
            text = f.read_text(encoding='utf-8')
        except Exception:
            continue
        parts = re.split(r'(?m)^## +(.+?)\s*$', text)
        # parts[0] is preamble; then alternating (heading, body, ...)
        for i in range(1, len(parts), 2):
            heading = parts[i].strip()
            body = parts[i + 1] if i + 1 < len(parts) else ''
            body = re.sub(r'<!--[\s\S]*?-->', '', body)
            stripped = body.strip()
            if len(stripped) < 50:
                rel = f.as_posix()
                issues.append(f"[SHOULD] empty-section {rel}: '## {heading}' has {len(stripped)} chars")

for line in issues:
    print(line)
PY
)
if [ -n "$EMPTY_OUT" ]; then
  printf '%s\n' "$EMPTY_OUT"
  ISSUES=$((ISSUES + $(count_lines "$EMPTY_OUT")))
fi

# ---------- Check 5 [BLOCKER] hook-roster-drift ----------
# Asserts every PreToolUse and PostToolUse hook wired in
# .claude/settings.json appears in SVP §6's hook roster table.
# Catches the failure mode where a hook is added or renamed in
# settings.json but the docs are not updated. SessionStart hooks
# are intentionally not gates and are listed in SVP §6.1 only.
ROSTER_OUT=$("$PYTHON" - <<'PY'
import json, os, re
from pathlib import Path

repo = Path('.').resolve()
settings = repo / '.claude' / 'settings.json'
svp = repo / 'docs' / 'team-lib' / 'planning' / 'SVP.md'

if not settings.exists() or not svp.exists():
    raise SystemExit(0)

try:
    data = json.loads(settings.read_text(encoding='utf-8'))
except Exception as e:
    print(f"[BLOCKER] hook-roster-drift cannot parse settings.json: {e}")
    raise SystemExit(0)

wired = set()
for trigger in ('PreToolUse', 'PostToolUse'):
    for entry in data.get('hooks', {}).get(trigger, []) or []:
        for h in entry.get('hooks', []) or []:
            cmd = h.get('command', '')
            # command is .claude/hooks/foo.sh — extract basename
            base = os.path.basename(cmd)
            if base.endswith('.sh'):
                wired.add(base)

# Find SVP §6 by header anchor, then collect first-column hook names
# from the markdown table that follows. Stop at the next ## heading.
svp_text = svp.read_text(encoding='utf-8')
m = re.search(r'(?m)^## 6\.[^\n]*\n', svp_text)
if not m:
    print("[BLOCKER] hook-roster-drift cannot locate SVP §6 heading")
    raise SystemExit(0)
tail = svp_text[m.end():]
end = re.search(r'(?m)^## ', tail)
section = tail[:end.start()] if end else tail

rostered = set()
for line in section.split('\n'):
    # Match ` | `script.sh` | ... | ... |` and similar
    cell = re.match(r'\s*\|\s*`([^`]+\.sh)`', line)
    if cell:
        rostered.add(cell.group(1))

missing = sorted(wired - rostered)
for hook in missing:
    print(f"[BLOCKER] hook-roster-drift {hook} wired in settings.json but absent from SVP §6")
PY
)
if [ -n "$ROSTER_OUT" ]; then
  printf '%s\n' "$ROSTER_OUT"
  ISSUES=$((ISSUES + $(count_lines "$ROSTER_OUT")))
fi

# ---------- Check 6 [NIT] markdownlint ----------
if command -v markdownlint-cli2 >/dev/null 2>&1; then
  LINT_OUT=$(markdownlint-cli2 'docs/**/*.md' 'CLAUDE.md' 'CONTRIBUTING.md' 2>&1 || true)
  if [ -n "$LINT_OUT" ]; then
    while IFS= read -r line; do
      case "$line" in
        *.md:*) echo "[NIT] $line"; ISSUES=$((ISSUES + 1)) ;;
      esac
    done <<< "$LINT_OUT"
  fi
fi

# ---------- Final tally ----------
if [ "$ISSUES" -eq 0 ]; then
  echo "OK: all doc checks passed"
  exit 0
else
  echo "FAIL: $ISSUES issues"
  exit 1
fi
