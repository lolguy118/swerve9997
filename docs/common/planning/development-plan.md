<!-- markdownlint-disable MD013 -->
# Development Plan Framework

Framework-agnostic development-lifecycle policy for Team 271 Java
projects. A concrete project (library or season robot) fills in
specifics — version numbers, per-layer build order, milestone tags —
in its own SDP and cites this document for the shared framework.

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in [`README.md`](README.md#normative-keywords).

## 1. Development Environment Baseline

Every project **shall** pin its toolchain in machine-readable form:

| Component | Source of truth |
| --------- | --------------- |
| Java | `build.gradle` `sourceCompatibility` / `targetCompatibility` |
| Gradle | `gradle/wrapper/gradle-wrapper.properties` |
| WPILib | WPILib installer (matches seasonal release) |
| Spotless | `build.gradle` plugin version |
| JaCoCo | `build.gradle` plugin version |
| Vendor deps | `vendordeps/*.json` |

The project's SDP **shall** cite these sources but **shall not**
duplicate version numbers. When versions change, the machine-readable
source updates; the SDP does not need to track each bump.

## 2. Platform Matrix Pattern

Projects **shall** declare which platforms they target and whether CI
runs on each:

| Platform | Purpose | CI? |
| -------- | ------- | --- |
| roboRIO 2 | Competition target | (hardware-only, typically no CI) |
| Windows (desktop sim) | Primary developer platform | Yes |
| macOS (desktop sim) | Secondary developer platform | Yes |
| Linux (desktop sim) | Developer platform + CI runner | Yes |

Desktop simulation **shall** use the WPILib HAL sim backend.
Vendor-specific sim backends (Phoenix 6 sim, etc.) apply only to
projects using those vendors.

## 3. FRC-Calendar Phase Model

The FRC season imposes a predictable rhythm. Every project's SDP
**shall** adopt this four-phase model. Phase boundaries are events
(a competition happens, a release is cut), not calendar dates. §4
tabulates the compatibility rules these phases imply.

### 3.1 Offseason (post-championship through kickoff)

- **Entry:** competition season concluded.
- **Deliverables:** before API freeze, architectural ADRs, breaking
  API changes, refactors, and new features. At API freeze, breaking
  changes stop and the next-season MINOR version is tagged (e.g.,
  `v<yr+1>.0.0`). After freeze, additive features, documentation
  updates, and integration tests.
- **Exit:** FRC kickoff — Build Season begins.

### 3.2 Build Season (kickoff through first competition)

- **Entry:** FRC kickoff.
- **Deliverables:** **stability only** — bug fixes, tuning, additive
  features that do not break the API.
- **Exit:** first competition. No breaking changes permitted.

### 3.3 Competition Season (first competition through championship)

- **Entry:** first competition.
- **Deliverables:** **hotfix only** — fixes to bugs discovered at
  competition. Each change **shall** carry a review-command artifact
  and at least one maintainer approval, per
  [`configuration-management.md`](configuration-management.md#3-change-control).
- **Exit:** championship concluded. PATCH tags applied per fix.

### 3.4 Postseason (championship through offseason start)

- **Entry:** championship concluded.
- **Deliverables:** retrospective, next-season major tag (e.g.,
  `v<next-year>.0.0`), docs updates, ADR drafts for the next
  offseason.
- **Exit:** offseason begins.

## 4. API Compatibility Policy

The phase model imposes a compatibility gradient that tightens as the
season progresses. The rules apply to every public API a project
publishes (library API, robot-side services). SemVer bump selection
lives in
[`configuration-management.md`](configuration-management.md#1-versioning-semantic-versioning).

| Phase | Breaking change | Additive change | Bug fix |
| ----- | --------------- | --------------- | ------- |
| Offseason\* | Allowed (pre-freeze, with ADR) | Allowed | Allowed |
| Build Season | **Forbidden** | Allowed if tested | Allowed |
| Competition Season | **Forbidden** | **Forbidden** | Allowed (hotfix) |
| Postseason | **Forbidden** | Discouraged | Allowed |

\* Offseason contains an internal **API freeze** milestone (see §3.1).
Breaking changes are acceptable only before freeze; after freeze,
changes **shall** be additive or bug-fix only.

A project that **must** break API after the freeze **shall** first
write (or amend) an ADR justifying the emergency, then follow the
change-control process in
[`configuration-management.md`](configuration-management.md#3-change-control).

## 5. Milestone Events

Every project's SDP **shall** list its version-tag events and its
phase-transition events. Tag events produce a signed Git tag;
phase-transition events simply alter the rules that apply.

### 5.1 Version Tag Events (sample)

| Event | Action | Example |
| ----- | ------ | ------- |
| Offseason start | Tag prior season final | `v<yr>.N.P` |
| Offseason API freeze | Tag next-season MINOR=0 | `v<yr+1>.0.0` |
| Each competition hotfix | Tag patch | `v<yr+1>.0.P` |

### 5.2 Phase Transition Events (sample)

| Event | Action |
| ----- | ------ |
| Build season start | No-new-features rule takes effect |
| Postseason | Retrospective; ADR drafts |

## 6. Layer Build Priority

Projects with multiple internal layers **shall** declare a build
priority so contributors know which layer must be present and tested
before the next one begins. The concrete layer list and dependency
rules are project-specific (documented in that project's SDP and a
supporting ADR).

## 7. Deviations

Every project's SDP **shall** include a deviations table tracking any
departures from the shared coding standard or policy. See
[`configuration-management.md`](configuration-management.md#5-deviation-tracking)
for the format.
