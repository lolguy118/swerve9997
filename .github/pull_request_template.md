## Summary

<!--
Summarize the changes in a couple of sentences.
What does this pull request do and why?
-->

## What changed?

<!--
List the code changes in bullet points. Use sub-headings to separate
different packages (api/, hardware/, control/, auto/, etc.) if the PR
spans multiple areas.
-->

## Type of change

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that changes existing behaviour)
- [ ] Documentation only
- [ ] Refactor / cleanup (no behaviour change)

## Testing checklist

- [ ] `./gradlew spotlessCheck` passes
- [ ] `./gradlew compileJava compileTestJava` passes
- [ ] `./gradlew test` passes (existing tests)
- [ ] New tests added for new behaviour (or explained why not needed)
- [ ] Tested on real hardware (describe below, or check "sim-only")
- [ ] Sim-only change

### Hardware test notes

<!-- If tested on a robot, describe the mechanism, CAN IDs, and what
     you observed. If sim-only, say so explicitly. -->

## Design doc updates

<!--
Any behaviour change requires a matching design doc update in the same
PR (see CLAUDE.md "Documentation Rules"). Check all that apply:
-->

- [ ] `docs/team-lib/planning/sdd/` — SDD for affected layer
- [ ] `docs/team-lib/planning/adr/` — new ADR if decision reversal
- [ ] `docs/team-lib/planning/SRS.md` — requirement added/changed
- [ ] `docs/team-lib/planning/SVP.md` — test strategy change
- [ ] `docs/team-lib/guides/` — guide update
- [ ] No behaviour change — docs unaffected

## Library review

<!--
Before requesting review, run /lib-review and /doc-sync-check locally
(via Claude Code) and paste any findings here, or state "clean".
-->

## Expected change impact

<!--
Could this change break another subsystem or robot project using the lib?
Are there migration steps, deprecated APIs, or caveats?
-->

## Reviewers

<!--
If specific reviewers are needed, tag them here and say what they
should focus on (e.g. "@alice please review the CTREManager refresh
changes").
-->
