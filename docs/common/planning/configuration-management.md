# Configuration Management Policy

Framework-agnostic configuration management for Team 271 Java projects.
A concrete project (library or robot) cites this policy and records
only its specific deviations and state (current version, vendordep
list, release history) in its own SCMP.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in [`README.md`](README.md#normative-keywords).

## 1. Versioning (Semantic Versioning)

Every Team 271 deliverable **shall** use Semantic Versioning:

- **MAJOR** — breaking API or behavioural change (rare; offseason only).
- **MINOR** — new API surface, additive.
- **PATCH** — bug fix, no API change.

FRC seasons rhythm the calendar year. Projects **should** use the
version format `YYYY.MINOR.PATCH` where `YYYY` is the season year of
the release. Version numbers are authoritative in `build.gradle`.

## 2. Baseline Control

- `main` **shall** be protected on the remote: no direct push, no
  force-push.
- Season-boundary baselines **shall** be Git tags on `main`, signed.
- Feature branches **should** use the naming pattern
  `feat/<short-description>` or `fix/<short-description>`.

## 3. Change Control

- All changes enter via pull request on a feature branch.
- `.claude/hooks/` **shall** all pass before merge approval is granted.
- At least one maintainer **shall** approve every PR.
- Non-trivial changes **should** carry a review-command artifact (e.g.,
  output from the project's `/lib-review` or equivalent).

## 4. Vendor Dependency Management

### Automated freshness tracking

Every project that pulls in vendordeps **should** automate a
freshness check that fetches each vendordep's upstream `jsonUrl` on
a schedule, compares the version, and opens (or updates) a tracking
issue when upstream moves ahead. This prevents vendordeps from
drifting silently.

### Upgrade procedure

Upgrading a vendordep **shall** follow these steps:

1. Update the vendordep JSON to the new version (from the upstream
   `jsonUrl`).
2. Run `./gradlew compileJava` — fix any API breaks.
3. Run `./gradlew test` — fix any test failures.
4. If the vendor changes fundamentally (new vendor major, semantics
   shift), write a new ADR before merging.
5. Merge via the normal PR process; close the freshness-check issue
   when the upgrade lands on `main`.

### Supply-chain visibility

Projects **should** submit their Gradle dependency graph to GitHub on
every push to `main` so vendordeps appear in the Dependency graph view
and are eligible for Dependabot alerts.

## 5. Deviation Tracking

Every project **shall** maintain a deviation table in its
development plan. Each row records a single deviation from the shared
coding standard or policy, with rationale and an approving maintainer.

The **Rule ID** column **shall** use the coding-standard identifier
form `CODE-<CATEGORY>-NNN` (for example `CODE-SAF-001`,
`CODE-GEN-004`). Deviations from non-coding rules (planning-doc
conventions, documentation rules) **may** cite the authoritative
rule document and section instead.

| Rule ID | File / Scope | Rationale | Approved By | Date |
| ------- | ------------ | --------- | ----------- | ---- |
| `CODE-GEN-004` | `path/to/file.java` | Why the deviation is acceptable | @maintainer | YYYY-MM-DD |

Deviations **shall** be reviewed at season boundaries and retired when
the underlying reason no longer applies.

## 6. Release History

Every project **shall** keep a release-history table in its own SCMP:
the version tag, its date, and a one-line summary of what changed.
Patch releases during competition **should** list the specific bug they
fix and the event at which the fix shipped.
