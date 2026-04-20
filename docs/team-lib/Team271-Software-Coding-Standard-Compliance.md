<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->
<!-- markdownlint-disable-file MD041 -->

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
}
```

Additional `-Xlint` flags **should** be added to catch common issues.

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
practices in CODE-GEN-004 and CODE-GEN-013.

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
- [ ] Timeout protection: all waiting operations have named timeout constants with fail-safe behavior (CODE-SAF-009c)
- [ ] Runtime tunability: configurable values use `LoggedNTInput` + `checkTuning()` pattern (CODE-BUG-004)

---

## Enforcement Matrix

The table below maps each CODE-* rule group to its enforcement mechanism.

| Rule Group | Prefix | Enforced By | Gate |
| ---------- | ------ | ----------- | ---- |
| Safety Practices | CODE-SAF | Manual code review + `/lib-review` | PR review |
| Formatting | CODE-FMT | Spotless (Google Java Format) | `./gradlew spotlessCheck` |
| Naming | CODE-NAM | Manual code review | PR review |
| Methods | CODE-MTH | Compiler warnings + manual review | Compile + PR review |
| Variables | CODE-VAR | Manual code review | PR review |
| Control Structures | CODE-CTL | Compiler (switch exhaustiveness) + manual review | Compile + PR review |
| Comments | CODE-CMT | Manual code review | PR review |
| Debugging / Telemetry | CODE-DBG | Manual code review + design-drift hook | PR review |
| GC Discipline | CODE-PERF | Manual code review | PR review |
