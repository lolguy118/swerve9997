#!/usr/bin/env bash
# Hook: PostToolUse - advisory drift checks on coding-standard edits.
#
# Bash port of the project-template's check-coding-standards.ps1, minus the
# placeholder-leakage check: this is a CONSUMING project, so the resolved
# values (Team271-Lib, the m instance-field prefix, BuildConstants.java,
# TunerConstants.java, 271) are correct, not template tokens to flag.
#
# Fires on .md edits under docs/coding-standard/<lang>/ and runs:
#   1. Citation integrity    - every cited CODE-XXX-NNN resolves to a heading
#                              definition in the same language folder.
#   2. Duplicate definitions - no parent ID defined in two files in the folder.
#   3. External-URL allowlist - every URL host is in .claude/external-references.json.
#   4. Normative keyword     - every CODE heading reaches shall/should in 30 lines.
# Advisory only (always exits 0); findings go to stderr.

source "$(dirname "$0")/_helpers.sh"

FILE_PATH=$(parse_file_path)
[ -z "$FILE_PATH" ] && exit 0
case "$FILE_PATH" in
  *.md) ;;
  *) exit 0 ;;
esac

REPO_ROOT=$(repo_root)
REL=$(relative_path "$FILE_PATH" "$REPO_ROOT")
[ -z "$REL" ] && exit 0

case "$REL" in
  docs/coding-standard/*/*.md) ;;
  *) exit 0 ;;
esac

LANG=$(printf '%s' "$REL" | sed -E 's#^docs/coding-standard/([^/]+)/.*#\1#')
LANG_FOLDER="${REPO_ROOT}/docs/coding-standard/${LANG}"
ABS="${REPO_ROOT}/${REL}"
[ -f "$ABS" ] || exit 0

findings=()

# Parent IDs defined by a heading in the language folder.
DEFINED=$(grep -rhoE '^#{2,6}[[:space:]]+CODE-[A-Z]{3,4}-[0-9]{3}\b' "${LANG_FOLDER}"/*.md 2>/dev/null \
  | grep -oE 'CODE-[A-Z]{3,4}-[0-9]{3}' | sort -u)

# --- Check 1: citation integrity (cited parents not defined in the folder) ---
CITED=$(grep -oE 'CODE-[A-Z]{3,4}-[0-9]{3}[a-z]?' "$ABS" 2>/dev/null | sed -E 's/[a-z]$//' | sort -u)
while IFS= read -r id; do
  [ -z "$id" ] && continue
  if ! printf '%s\n' "$DEFINED" | grep -qx "$id"; then
    findings+=("[dangling-citation] ${id} cited in ${REL} but not defined under any heading in ${LANG}/")
  fi
done <<< "$CITED"

# --- Check 2: duplicate parent definitions across files in the folder --------
while IFS= read -r id; do
  [ -z "$id" ] && continue
  n=$(grep -rlE "^#{2,6}[[:space:]]+${id}\b" "${LANG_FOLDER}"/*.md 2>/dev/null | wc -l)
  if [ "$n" -gt 1 ]; then
    findings+=("[duplicate-definition] ${id} defined in ${n} files in ${LANG}/")
  fi
done <<< "$DEFINED"

# --- Check 3: external-URL allowlist -----------------------------------------
REFS="${REPO_ROOT}/.claude/external-references.json"
if [ -f "$REFS" ] && command -v jq >/dev/null 2>&1; then
  mapfile -t ALLOWED < <(jq -r '.allowed_domains[]?' "$REFS" 2>/dev/null)
  while IFS= read -r host; do
    [ -z "$host" ] && continue
    ok=0
    for d in "${ALLOWED[@]}"; do
      if [ "$host" = "$d" ]; then ok=1; break; fi
      case "$host" in
        *".${d}") ok=1; break ;;
      esac
    done
    [ "$ok" -eq 0 ] && findings+=("[untrusted-url] ${host} not in .claude/external-references.json allowed_domains")
  done < <(grep -oE 'https?://[^/[:space:])>]+' "$ABS" 2>/dev/null | sed -E 's#^https?://##' | tr '[:upper:]' '[:lower:]' | sort -u)
fi

# --- Check 4: normative keyword within 30 lines of each CODE heading ---------
TOTAL=$(wc -l < "$ABS")
while IFS= read -r hit; do
  [ -z "$hit" ] && continue
  ln=${hit%%:*}
  id=$(printf '%s' "$hit" | grep -oE 'CODE-[A-Z]{3,4}-[0-9]{3}')
  end=$((ln + 30))
  [ "$end" -gt "$TOTAL" ] && end="$TOTAL"
  if ! sed -n "${ln},${end}p" "$ABS" | grep -qiE '\b(shall|should)( not)?\b'; then
    findings+=("[no-normative-clause] ${id} has no shall/should within 30 lines (line ${ln})")
  fi
done < <(grep -nE '^#{2,6}[[:space:]]+CODE-[A-Z]{3,4}-[0-9]{3}\b' "$ABS" 2>/dev/null)

if [ "${#findings[@]}" -gt 0 ]; then
  echo "check-coding-standards: ${#findings[@]} advisory finding(s) in ${REL}:" >&2
  for f in "${findings[@]}"; do
    echo "  ${f}" >&2
  done
fi

exit 0
