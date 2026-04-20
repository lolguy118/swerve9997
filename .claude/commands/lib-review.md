---
description: Review the current branch's library changes against architecture, coding standard, and safety rules.
---

Run a library code review on the current branch.

1. Get the list of changed files:

   ```bash
   git diff main...HEAD --name-only -- 'src/main/java/com/team271/lib/**' 'docs/team-lib/**'
   ```

   If empty, stop and tell the user there is nothing to review.

2. Delegate to the `lib-reviewer` subagent via the Agent tool. Pass it
   the file list and the full diff (`git diff main...HEAD -- <files>`).
   Ask it to group findings as **Blockers / Should fix / Nits**.

3. Relay the subagent's findings back to the user verbatim, followed by
   a one-sentence summary of what needs to happen next.

Do not edit files as part of this command.
