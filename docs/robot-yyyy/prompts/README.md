<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/prompts/README.md in the robot's own repository.
     This folder is OPTIONAL. Most projects don't need it. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Review Prompts — <Project>

This folder holds **prompts** — structured instructions to an AI
assistant — used during <Project> robot code review. Most robot
projects don't need a custom prompt; the library's
[`../../team-lib/prompts/code-review-prompt-teamlib.md`](../../team-lib/prompts/code-review-prompt-teamlib.md)
generally suffices for projects that vendor the library docs.

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
