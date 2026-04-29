---
paths:
  - "src/**/*.java"
  - "docs/team-lib/**/*.md"
---

# Rule: FRC Documentation Search via MCP

This project ships a documentation-search MCP server (`frc-docs`)
registered in [`.mcp.json`](../../.mcp.json). It indexes WPILib,
CTRE Phoenix, REV, Redux, and PhotonVision docs and exposes four
tools prefixed with `mcp__frc-docs__`. Activates on library Java
files and library design docs — that is where vendor-API questions
get answered.

Upstream: <https://github.com/ramalamadingdong/agentic-csa>.

## Rules Claude must apply

- **Search before answering.** Before answering questions about
  WPILib, CTRE Phoenix 6, PathPlanner, AdvantageKit, PhotonVision,
  or any vendor API surface, call
  `mcp__frc-docs__search_frc_docs` first. This catches API drift
  between season releases that pre-training data cannot.
- **Fall back without ceremony.** If search returns nothing
  relevant, answer from your own knowledge and note that
  documentation was not available. Do not retry the same query
  repeatedly; do not apologize.
- **Cite the URL** when you used a result, so the user can verify.
- **Default `version="2026"`** for this repo (matches
  `vendordeps/Phoenix6-frc2026-latest.json` and
  `vendordeps/ChoreoLib2026.json`). The tool defaults to 2025
  otherwise — pass `version="2026"` explicitly.
- **Keep queries to 2–4 keywords**, not natural-language
  questions. Use the exact product name (`TalonFX`, `CANcoder`,
  `Pigeon2`, `CANrange`) and FRC acronyms intact (`CAN`, `PID`).
- **Trust auto-detection.** The server reads `vendordeps/` and
  `build.gradle` to filter by language and vendor. Do not pass
  `language=` or `vendors=` unless you have a specific reason
  (e.g., comparing two vendors).
- **Skip REV / SparkMax queries.** Team 271's library is
  CTRE-only ([ADR-008](../../docs/team-lib/planning/adr/ADR-008-ctre-phoenix6-primary-vendor.md)).
  Searching for SparkMax APIs in this repo means the task was
  misunderstood.

## Tool reference

| Tool | When to call |
| --- | --- |
| `mcp__frc-docs__detect_project_context` | Once per session if uncertain about auto-detection |
| `mcp__frc-docs__search_frc_docs` | First step — keyword search returns ranked results with URLs |
| `mcp__frc-docs__fetch_frc_doc_page` | Second step — pull full content for a high-confidence URL |
| `mcp__frc-docs__list_frc_doc_sections` | Browse what is indexed for a vendor |

## Vendor mapping for this project

| Hardware | Vendor filter |
| --- | --- |
| TalonFX, Falcon 500, Kraken X60/X44, CANcoder, Pigeon 2, CANdi, CANrange | `vendors=["ctre"]` |
| WPILib core, command-based, simulation | `vendors=["wpilib"]` |
| PathPlanner trajectories | `vendors=["pathplanner"]` |
| PhotonVision, Limelight | `vendors=["photonvision", "wpilib"]` |

## Authoritative docs

- [`ADR-008`](../../docs/team-lib/planning/adr/ADR-008-ctre-phoenix6-primary-vendor.md)
  — vendor selection rationale.
- [`reference-urls.md`](../../docs/reference-urls.md) — direct
  vendor doc URLs (use these when MCP search misses).
