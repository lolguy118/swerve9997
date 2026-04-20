---
description: Check that design docs are in sync with code changes on the current branch.
---

# Doc Sync Check

Audit whether the current branch's code changes have matching design
doc updates.

1. List changed Java files under the library:

   ```bash
   git diff main...HEAD --name-only -- 'src/main/java/com/team271/lib/**'
   ```

2. For each changed file, map it to the likely-affected design doc:

   | Code path prefix | Design doc |
   | ---------------- | ---------- |
   | `api/` | `docs/team-lib/planning/sdd/SDD-api.md` |
   | `vendor/ctre/`, `bridge/` | `docs/team-lib/planning/sdd/SDD-vendor-ctre.md` |
   | `hardware/` | `docs/team-lib/planning/sdd/SDD-hardware.md` |
   | `control/` | `docs/team-lib/planning/sdd/SDD-control.md` |
   | `subsystem/` | `docs/team-lib/planning/sdd/SDD-subsystem.md` |
   | `auto/` | `docs/team-lib/planning/sdd/SDD-auto.md` |
   | `sysid/` | `docs/team-lib/planning/sdd/SDD-sysid.md` |
   | `nt/` | `docs/team-lib/planning/sdd/SDD-nt.md` |
   | `util/` | `docs/team-lib/planning/sdd/SDD-util.md` |
   | `TObj`, `TRobot`, `wpilib/` | `docs/team-lib/planning/sdd/SDD-team271-lib.md` |

3. For each expected doc, check whether it was modified in the diff:

   ```bash
   git diff main...HEAD --name-only -- 'docs/team-lib/**'
   ```

4. Also check for stale references to deleted classes:

   ```bash
   rg -n -w -f .claude/rules/deprecated-symbols.txt docs/ CLAUDE.md
   ```

5. Report:
   - Code changes that lack a matching doc update (grouped by
     expected doc)
   - Any stale references to deleted classes
   - A go/no-go recommendation for PR creation

Do not edit files as part of this command.
