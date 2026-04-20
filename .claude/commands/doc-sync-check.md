---
description: Check that design docs are in sync with code changes on the current branch.
---

Audit whether the current branch's code changes have matching design
doc updates.

1. List changed Java files under the library:

   ```bash
   git diff main...HEAD --name-only -- 'src/main/java/com/team271/lib/**'
   ```

2. For each changed file, map it to the likely-affected design doc:

   | Code path prefix | Design doc |
   |------------------|------------|
   | `subsystem/` | `docs/team-lib/architecture/library-architecture.md` |
   | `hardware/` | `docs/team-lib/architecture/hardware-abstraction.md` |
   | `control/` | `docs/team-lib/control/control-system.md` |
   | `auto/` | `docs/team-lib/control/auto-design.md` |
   | `api/`, `vendor/`, `bridge/` | `docs/team-lib/architecture/vendor-abstraction-guide.md` |

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
