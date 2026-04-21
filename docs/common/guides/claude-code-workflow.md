<!-- markdownlint-disable MD013 MD060 -->
# Claude Code Workflow

This guide teaches you how to get the most out of **Claude Code** —
the AI coding assistant this repository is configured for — when
you're contributing to a Team 271 project. It is not a replacement
for reading the code or understanding the design docs; it is a way
to work faster without taking shortcuts on quality.

> **Industry bridge.** Real software teams are adopting AI coding
> assistants (Claude Code, Copilot, Cursor) the same way they once
> adopted autocompletion and refactoring tools. The engineers who
> get the most out of them learn the *workflow* — when to ask, what
> to ask, and when to stop and think — not just the prompts. This
> guide is that workflow for our repository.

---

## What Claude Code is (and isn't)

Claude Code is an AI assistant that runs in your terminal or editor
and can read, edit, and run code in this repository. It knows
nothing about your task until you tell it. It forgets everything
between sessions except what lives in files
(`CLAUDE.md`, `.claude/rules/`, memory files).

Claude Code is **not**:

- A replacement for understanding what the code does. If you can't
  explain your own PR, don't open it.
- A substitute for the coding standard. Everything in
  [`../coding-standard/Team271-Software-Coding-Standard.md`](../coding-standard/Team271-Software-Coding-Standard.md)
  still applies to AI-generated code.
- An oracle. It can be wrong, especially about recent API changes
  or about code it hasn't read yet.

---

## The core loop: Explore → Plan → Code → Verify

Anthropic's recommended workflow — and the one that produces the
best results on this repo — has four phases. Skipping phases
produces code that doesn't fit our conventions.

### 1. Explore

Before writing anything, ask Claude to **read** the relevant files.
Example first messages:

- "Read `src/main/java/com/team271/lib/subsystem/Subsystem.java`
  and `docs/team-lib/planning/sdd/SDD-subsystem.md`. Don't write
  anything yet."
- "Read the Arm subsystem and its design doc. Summarize the state
  machine in three sentences."

For larger searches ("where is the CAN refresh loop registered?"),
delegate to a subagent so the main conversation doesn't fill up
with search output:

- "Use the `Explore` subagent to find every caller of
  `CTREManager.refreshAll()`."

### 2. Plan

Once Claude has context, ask for a *plan* — not code. Plan Mode
(triggered by starting a message with `plan` or by pressing the
Plan-Mode toggle in the UI) forces Claude to produce a step-by-step
approach you can review before any file changes.

On non-trivial changes — a new subsystem, a new ADR, a library-layer
refactor — always plan first. On a one-line typo fix, you can skip
straight to the edit.

### 3. Code

Approve the plan, then let Claude execute. Watch the diff as it
goes. If it drifts from the plan, stop it and correct course.

Our repository has **hooks** that run after each edit
(markdownlint, `./gradlew compileJava`, Spotless, Checkstyle,
SpotBugs, tunable checks). If a hook fails, Claude will see the
error and either fix it or ask you what to do. Don't let Claude
skip hooks — the deny list in `.claude/settings.json` blocks
`--no-verify`, `--no-gpg-sign`, and `git commit -n`.

### 4. Verify

Before you commit, **read the diff yourself**. Then run:

```bash
./gradlew build
./gradlew test
```

If the change touches subsystem behavior, state machines, homing,
or timeouts, confirm a matching design-doc update is in the same
PR — this is a
[`.claude/rules/docs.md`](../../../.claude/rules/docs.md)
requirement.

---

## Use `/clear` between tasks

Every message you and Claude exchange gets stored in the session
context. When that context grows too large, Claude gets slower and
more likely to forget early instructions. Clear it between
unrelated tasks:

```text
/clear
```

Rule of thumb: one feature, one conversation. A new ticket is a
new session.

---

## Subagents for research

Our repository ships with specialized subagents (see
`.claude/agents/` if it exists, and the `Agent` tool available in
every session). Use them to protect the main conversation from
large amounts of search output:

- `Explore` — fast, read-only codebase search and question-
  answering. Best for *"where is X defined?"* or *"how does Y
  work?"*
- `Plan` — proposes an implementation plan without touching files.
- `lib-reviewer` — applies the full Team271-Lib code-review
  checklist. Invoked via
  [`/lib-review`](../../../.claude/commands/lib-review.md).

The pattern: delegate *research* to subagents; keep *judgment* and
*editing* in the main conversation.

---

## Writer / Reviewer pattern

On anything that matters — a new ADR, a subsystem rewrite, a
public-API change — use two sessions in parallel:

1. **Writer session.** Claude writes the code or the doc.
2. **Reviewer session.** A fresh session (so it has no bias from
   the writer's context) reads the diff and critiques it. Invoke
   [`/lib-review`](../../../.claude/commands/lib-review.md) here,
   or ask the `lib-reviewer` agent directly.

This mirrors the human review process: the author and the reviewer
are never the same person. Doing it this way catches problems the
writer's own context made them blind to.

---

## Slash commands and skills

Our `.claude/` folder ships a small toolbox. Treat these as the
"official" way to do common tasks — they already know our
conventions.

| Command / Skill | Purpose |
|-----------------|---------|
| [`/lib-review`](../../../.claude/commands/lib-review.md) | Branch-level code review with the library checklist |
| [`/doc-sync-check`](../../../.claude/commands/doc-sync-check.md) | Verify code changes have matching design-doc updates |
| `/new-adr` | Scaffold a new Architecture Decision Record with the correct next number |
| `/new-sdd` | Scaffold a new Software Design Description with the nine-section template |
| `/new-subsystem` | Scaffold a robot-project subsystem following ADR-010 + ADR-012 |

Run `/help` inside Claude Code to see the full list, including
the commands the Claude Code CLI itself provides (`/clear`,
`/compact`, `/plan`, etc.).

---

## What lives in `.claude/`

You don't need to memorize this — just know where to look when
something surprises you.

| Path | What it does |
|------|-------------|
| `.claude/rules/*.md` | Path-scoped AI guardrails. Loaded when editing files under a matching path. |
| `.claude/commands/*.md` | Slash commands. Invoked as `/command-name`. |
| `.claude/agents/*.md` | Subagent definitions. |
| `.claude/skills/*/SKILL.md` | Reusable multi-step workflows. |
| `.claude/hooks/*.sh` | Shell scripts that run before or after tool calls (lint, compile, protect files). |
| `.claude/settings.json` | Permissions, hook registrations, Bash allow/deny lists. |

The root [`CLAUDE.md`](../../../CLAUDE.md) is a **routing index**
— it points at authoritative docs; it does not duplicate them.

---

## Habits that help

- **Be specific.** *"Fix the Arm subsystem so it homes correctly
  when the limit switch is wired active-low"* beats *"fix the arm."*
- **Name the rule.** If you want Claude to enforce a rule, cite it:
  *"Apply ADR-012 — every wait needs a named timeout, a fail-safe
  action, and a driver alert."*
- **Ask for the diff first.** *"Show me the diff you would make
  before writing the file."* catches a lot of drift cheaply.
- **Read the hook output.** The hooks print *why* they failed.
  That's often a faster diagnosis than re-asking Claude.
- **Commit often.** Small commits are easier to review and easier
  to revert if an experiment doesn't work out.

---

## Anti-patterns

These are the behaviors that produce bad code fastest:

- **Vibe-coding.** Accepting every diff without reading it. You
  are still the engineer; Claude is the tool.
- **Skipping verification.** *"The compile hook said it passed, so
  I'm done"* is not the same as *"I ran the code and it behaved
  correctly."*
- **Overriding safety.** Never ask Claude to bypass a hook, a
  signing requirement, or the CI gate. If a gate is blocking real
  work, fix the gate — don't route around it.
- **One giant session.** A week-long conversation becomes slow and
  confused. Use `/clear`, `/compact`, or a new session.
- **Trusting memory over code.** Claude's memory files can become
  stale. When in doubt, read the current file.

---

## Learn more

Anthropic's public docs:

- Best practices: <https://code.claude.com/docs/en/best-practices>
- Memory and context: <https://code.claude.com/docs/en/memory>
- Skills: <https://code.claude.com/docs/en/skills>
- Subagents: <https://code.claude.com/docs/en/subagents>

Repository-specific configuration:

- [`CLAUDE.md`](../../../CLAUDE.md) — routing index.
- [`.claude/rules/`](../../../.claude/rules/) — AI guardrails.
- [`../../team-lib/planning/README.md`](../../team-lib/planning/README.md)
  — library planning docs the AI is expected to honor.
