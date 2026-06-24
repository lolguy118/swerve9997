<!-- TEMPLATE FOR FORKED ROBOT PROJECTS -- scaffold file renamed in
     place to docs/<project>/prompts/README.md by tools/init-robot.sh
     during project initialization. This folder is OPTIONAL — most
     projects don't need it. This banner is stripped by the init
     script. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Review Prompts — <Project>

This folder holds **prompts** — structured instructions to an AI
assistant — used during <Project> robot code review. Most robot
projects don't need a custom prompt; the inherited library prompt
at [`../../team-lib/prompts/code-review-prompt-teamlib.md`](../../team-lib/prompts/code-review-prompt-teamlib.md)
is always present in the fork and generally suffices.

> **Industry bridge.** Many professional teams now use
> *AI-assisted code review* alongside human review. The quality of
> the AI's feedback depends heavily on the prompt it receives — a
> good prompt encodes the team's standards in a form the assistant
> can apply every time. If a project uses a custom prompt, it lives
> in this folder.

Add a project-specific prompt here only if the robot's code has
patterns a general prompt doesn't cover — for example, mechanism-mode
invariants, controller-binding conventions, or a project-scope state
machine that every reviewer should cross-check.

## Contents

Fill in as prompts are authored.

| File | Purpose |
| ---- | ------- |
| (none yet) | |
