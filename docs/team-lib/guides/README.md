# Guides

This folder holds **tutorials and how-to references** for people
contributing to Team271-Lib and for robot-project developers who
use it. They're written for a first-season high-school student —
you don't need a software background to follow them.

If you're looking for formal design documents (what each layer does,
why we made a decision), go to
[`../planning/`](../planning/README.md) instead.

> **Industry bridge.** In the real world, software libraries ship
> with two kinds of documentation: *reference* (what each part
> does) and *guides* (how to actually use them). This folder is the
> "guides" half — task-oriented walkthroughs — and `../planning/`
> is the reference half.

## Start here (new contributors)

| Step | Document | Purpose |
| ---- | -------- | ------- |
| 1 | [start-here.md](start-here.md) | 5-minute orientation — mental models, critical rules, guided reading path |
| 2 | [development-setup.md](../../common/guides/development-setup.md) | Get Java 17, WPILib, and Gradle working; run tests locally |
| 3 | [../planning/README.md](../planning/README.md) | Navigate the planning hierarchy and find the right Software Design Description (SDD) |
| 4 | [input-shaping-guide.md](input-shaping-guide.md) | Understand joystick input curves before touching operator controls |
| 5 | [simulation-guide.md](simulation-guide.md) | Add physics simulation to a robot project using the library's simulation layer |
| 6 | [sysid-workflow.md](sysid-workflow.md) | Characterize mechanisms using the library's SysID (System Identification) data-capture tools |

## All Guides

| Guide | Audience | Links to |
| ----- | -------- | -------- |
| [start-here.md](start-here.md) | New contributors | All planning docs |
| [development-setup.md](../../common/guides/development-setup.md) | All contributors | `build.gradle`, continuous-integration (CI) workflow |
| [input-shaping-guide.md](input-shaping-guide.md) | Operator-control developers | [SDD-hardware.md](../planning/sdd/SDD-hardware.md) §Input |
| [simulation-guide.md](simulation-guide.md) | Robot-project developers | [SDD-team271-lib.md](../planning/sdd/SDD-team271-lib.md) §Simulation |
| [sysid-workflow.md](sysid-workflow.md) | Characterization engineers | [SDD-sysid.md](../planning/sdd/SDD-sysid.md) |
