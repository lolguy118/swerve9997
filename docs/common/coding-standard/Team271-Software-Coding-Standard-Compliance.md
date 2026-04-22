<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->
<!-- markdownlint-disable-file MD013 MD041 -->

## 5. Static Analysis and Tooling

### 5.1 Spotless

Spotless is the primary code formatting tool. Configuration in
`build.gradle`:

```groovy
spotless {
    java {
        target 'src/**/*.java'
        toggleOffOn()
        googleJavaFormat()
                .aosp()
                .reflowLongStrings()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
```

Spotless runs automatically before compilation. All code **shall** pass
`./gradlew spotlessCheck`.

The `toggleOffOn()` feature allows `// spotless:off` / `// spotless:on`
markers to disable formatting for specific sections (e.g., alignment
tables in constants). Use sparingly.

### 5.2 Compiler Warnings

All code **shall** compile with zero warnings. The following compiler
option is configured in `build.gradle`:

```groovy
tasks.withType(JavaCompile) {
    options.compilerArgs.add '-XDstringConcat=inline'
    options.compilerArgs.add '-Xlint:all'
}
```

The `-Xlint:all` flag enables every javac warning category
(`unchecked`, `deprecation`, `serial`, `fallthrough`, `rawtypes`,
etc.). Combined with the zero-warnings rule, this catches many
potential defects at compile time.

### 5.3 JVM Configuration

The robot JVM is configured for real-time performance:

```groovy
jvmArgs.add("-XX:+UnlockExperimentalVMOptions")
jvmArgs.add("-XX:GCTimeRatio=5")
jvmArgs.add("-XX:+UseSerialGC")
```

SerialGC is a stop-the-world collector with the smallest memory footprint,
appropriate for the RoboRIO's limited resources. It does not support
pause-time goals (`MaxGCPauseMillis` is a G1GC/ZGC hint and has no
effect on SerialGC). Instead, we minimize GC pressure through the coding
practices in CODE-GEN-004.

### 5.4 Code Review Checklist

The following items **shall** be verified during code review (these
cannot be caught by automated tools):

- [ ] State machine completeness: all enum values handled, default case present
- [ ] Motor safety: current limits configured, soft limits after homing, voltage bounds
- [ ] Subsystem lifecycle: correct method ordering, outputs in `robotPeriodicAfter()` only
- [ ] Telemetry coverage: key state variables logged in `outputTelemetry()`
- [ ] Cross-subsystem coordination: registration order correct, timing dependencies documented
- [ ] GC pressure: no allocations in periodic methods, objects pre-allocated
- [ ] Naming conventions: `m` prefix on fields, `arg` prefix on parameters, UPPER_SNAKE_CASE / `k` prefix constants
- [ ] Auto safety: missing path handling, timing window correctness, alliance flip testing
- [ ] Thread safety: shared static state (`Globals.*`) not mutated from multiple subsystems
- [ ] Motor direction: `configDirection()` matches physical mechanism intent
- [ ] Telemetry keys: pre-defined string constants, not computed or concatenated per-cycle
- [ ] Resource cleanup: timers and sensor subscriptions cleaned up in mode exit methods
- [ ] Timeout protection: all waiting operations have named timeout constants with fail-safe behavior (implemented in CODE-SAF-002c, CODE-SAF-008, CODE-SAF-010)
- [ ] Runtime tunability: configurable values use `TunableInput` + `applyTuning()` pattern (CODE-BUG-004)

---

## Enforcement Matrix

The table below maps each CODE-* rule prefix to its enforcement mechanism.
Prefixes reflect the actual rule IDs used across the coding-standard
companion files.

| Rule Group | Prefix | Rule Count | Enforced By | Gate |
| ---------- | ------ | ---------: | ----------- | ---- |
| General (keywords, annotations, type safety, exceptions, GC, concurrency) | CODE-GEN | 17 | Error Prone + SpotBugs + manual review + `check-java-postedit.sh` (batched compile+spotless+checkstyle) | Compile + CI + PR review |
| Formatting (braces, parens, blank lines, line endings, imports) | CODE-FMT | 6 | Spotless (AOSP Google Java Format) + Checkstyle (`NeedBraces` → CODE-FMT-002) + `.gitattributes` | `./gradlew spotlessCheck` + `checkstyleMain` |
| Modules and Files (naming, packages, constants, generated code) | CODE-MAF | 4 | Compiler + manual review | Compile + PR review |
| Methods (naming, lifecycle, state machines) | CODE-FUN | 6 | Error Prone (`MissingOverride`, `DefaultCharset`, etc.) + compiler warnings + manual review | Compile + PR review |
| Variables (naming, init, types, magic numbers) | CODE-VAR | 10 | Error Prone (`UnusedVariable`) + SpotBugs (null-deref) + manual review | Compile + CI + PR review |
| Control Structures (if/switch/loops) | CODE-CTL | 6 | Checkstyle (`MissingSwitchDefault` → CODE-CTL-002) + compiler + manual review | `./gradlew checkstyleMain` + PR review |
| Comments (JavaDoc, block, inline) | CODE-COM | 2 | Javadoc task + manual review | `./gradlew javadoc` + PR review |
| Debugging and Telemetry (the replay-faithful tunable-input type, the driver-notification facility, reports) | CODE-BUG | 4 | Manual review + `check-doc-tunables.sh`, `check-design-drift.sh` | PR review + hooks |
| Safety Practices (timeouts, fail-safe, CAN, vision, brownout) | CODE-SAF | 11 | SpotBugs (thread-safety, resource leaks) + manual review + `/lib-review` agent | CI + PR review |

Counts reflect the current rule set (see individual `-*.md`
companion files for rule IDs). Adding or removing rules requires
updating the count in this matrix.

### Static Analysis Tooling

The table above references three Java static analysers, configured in
[`build.gradle`](../../../build.gradle):

| Tool | Scope | Config | Rollout status |
| ---- | ----- | ------ | -------------- |
| Error Prone | Compile-time bug patterns (null checks, `==` on Strings, unused vars) | Plugin `net.ltgt.errorprone:4.0.1` | Fail-soft (`allErrorsAsWarnings = true`) |
| SpotBugs | Bytecode analysis (null deref, concurrency, resource leaks) | Plugin `com.github.spotbugs:6.0.21`; exclusions in [`config/spotbugs/exclude.xml`](../../../config/spotbugs/exclude.xml) | Fail-soft (`ignoreFailures = true`); reports uploaded as CI artifact |
| Checkstyle | Mechanizable SCS rules (currently `NeedBraces`, `MissingSwitchDefault`) | Built-in `checkstyle` plugin; config at [`config/checkstyle/checkstyle.xml`](../../../config/checkstyle/checkstyle.xml) | Strict (`maxWarnings = 0`); config grows incrementally |

Tighten rollout (remove `allErrorsAsWarnings`, `ignoreFailures`) as
historical findings are triaged. See SVP §7.
