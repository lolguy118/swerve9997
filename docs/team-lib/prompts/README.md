<!-- markdownlint-disable MD013 -->
# Review Prompts

This folder holds **prompts** — structured instructions to an AI
assistant — used during Team271-Lib code review. They give the
assistant the context it needs (library architecture, coding-standard
rules, safety requirements) so its reviews are consistent with how
a human maintainer would review.

> **Industry bridge.** Many professional teams now use
> *AI-assisted code review* alongside human review. The quality of
> the AI's feedback depends heavily on the prompt it receives — a
> good prompt encodes the team's standards in a form the assistant
> can apply every time. These prompts are that encoding for
> Team271-Lib.

## Contents

| File | Purpose |
| ---- | ------- |
| [`code-review-prompt-teamlib.md`](code-review-prompt-teamlib.md) | Instructions for reviewing a Team271-Lib pull request (architecture layering, safety rules, telemetry conventions, test requirements) |
