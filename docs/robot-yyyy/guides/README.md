<!-- TEMPLATE FOR CONSUMING ROBOT PROJECTS -- copy to
     docs/<project>/guides/README.md in the robot's own repository. -->
<!-- markdownlint-disable-file MD033 -->
<!-- reason: angle-bracket placeholders in this template are not HTML tags. -->

# Guides — <Project>

This folder holds **tutorials and how-to references** for people
contributing to the <Project> robot code. They're written for a
first-season high-school student — you don't need a software
background to follow them.

If you're looking for formal design documents (what each subsystem
does, why we made a decision), go to [`../planning/`](../planning/README.md)
instead.

> **Industry bridge.** In the real world, software projects ship
> with two kinds of documentation: *reference* (what each part
> does) and *guides* (how to actually use them). This folder is the
> "guides" half — task-oriented walkthroughs — and `../planning/`
> is the reference half.

Library-contributor guides (onboarding to Team271-Lib itself,
simulation, SysID, Claude Code workflow) live in the library tier
at [`../../team-lib/guides/`](../../team-lib/guides/) for projects
that vendor the library docs.

## Start here

Suggested starter guides — add as they are authored:

| Step | Document | Purpose |
| ---- | -------- | ------- |
| 1 | `onboarding.md` | Welcome, project overview, who does what |
| 2 | [`../../common/guides/development-setup.md`](../../common/guides/development-setup.md) | Get Java 17, WPILib, and Gradle working |
| 3 | `mechanism-tuning.md` | How to use the library's `LoggedNTInput` tuning workflow for this robot's mechanisms |
| 4 | `driver-practice.md` | Operator-control walkthrough and practice-field checklist |

## All Guides

Fill in as guides are authored. Remove the example row.

| Guide | Audience | Links to |
| ----- | -------- | -------- |
| `onboarding.md` | New contributors | [`../planning/README.md`](../planning/README.md) |
