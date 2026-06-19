<!-- markdownlint-disable MD007 MD013 -->
# Team271-Lib Java Coding Standard - Compliance Reference

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Compliance companion to [`Standard.md`](Standard.md). Contains
Java-specific tooling configuration (Spotless, compiler warnings,
JVM, static-analysis enforcers) and the code-review checklist
that backs up rules that automated tooling cannot catch.

---

## 5. Static Analysis and Tooling

### 5.1 Spotless

Spotless is the primary code-formatting tool. Reference
configuration in `build.gradle`:

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

Spotless runs automatically before compilation. All code
**shall** pass `./gradlew spotlessCheck`.

The `toggleOffOn()` feature allows `// spotless:off` /
`// spotless:on` markers to disable formatting for specific
sections (e.g., alignment tables in constants). Use sparingly.

### 5.2 Compiler Warnings

All code **shall** compile with zero warnings (see
[CODE-GEN-007](Standard-General.md#code-gen-007)).
The following compiler options are configured in `build.gradle`:

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

The runtime JVM **shall** be configured for the deployment
target's latency and memory characteristics. Concrete settings
(garbage collector choice, heap size, ergonomic tuning flags)
belong in the consuming project's `build.gradle` and **shall**
be documented in the project's own coding-standard supplement -
the right collector for a long-running server is wrong for a
real-time embedded target, and vice versa.

Where the deployment target cannot tolerate the unbounded pauses
of a default JVM configuration, the GC pressure-minimisation
rules in
[CODE-GEN-004](Standard-General.md#code-gen-004)
are the primary tool for keeping per-cycle allocation low so that
the configured collector stays inside its budget.

### 5.4 Code Review Checklist

The following items **shall** be verified during code review
(these cannot reliably be caught by automated tools):

- [ ] State machine completeness: all enum values handled,
      `default` case present
      ([CODE-CTL-002](Standard-Control.md#code-ctl-002),
      [CODE-FUN-004a](Standard-Methods.md#code-fun-004))
- [ ] Lifecycle correctness: initialization, per-cycle, and
      shutdown methods sequenced in the project's documented
      order; resources released in reverse order of acquisition
      ([CODE-GEN-014](Standard-General.md#code-gen-014))
- [ ] Cross-class coordination: registration order correct,
      timing dependencies documented
      ([CODE-COM-002c](Standard-Comments.md#code-com-002))
- [ ] GC pressure: no allocations in per-cycle methods,
      objects pre-allocated
      ([CODE-GEN-004](Standard-General.md#code-gen-004))
- [ ] Naming conventions: `m` on fields, `arg`
      on parameters, UPPER_SNAKE_CASE / `k` prefix on constants
      ([CODE-VAR-001](Standard-Variables.md#code-var-001))
- [ ] Thread safety: shared mutable state is either confined
      to one thread or wrapped in a documented synchronization
      mechanism
      ([CODE-GEN-016](Standard-General.md#code-gen-016))
- [ ] Logger key stability: pre-defined string constants, not
      computed or concatenated per-cycle
      ([CODE-GEN-004d](Standard-General.md#code-gen-004))
- [ ] Return-value handling: status codes and `Optional`
      returns checked or explicitly discarded with a justifying
      comment
      ([CODE-GEN-005](Standard-General.md#code-gen-005))
- [ ] Exception scope: catch the most specific type;
      broad-catch (`Throwable`, `Exception`) only at top-level
      lifecycle methods
      ([CODE-GEN-011](Standard-General.md#code-gen-011))

Project-specific safety and telemetry items (motor safety,
brownout handling, dashboard notifications, runtime tunability,
etc.) are reviewed against the consuming project's own
domain-specific coding standard, not against this pure-Java
standard.

---

## Enforcement Matrix

The table below maps each `CODE-*` rule prefix shipped in this
standard to its enforcement mechanism. Prefixes reflect the
actual rule IDs used across the coding-standard companion files.

| Rule Group | Prefix | Rule Count | Enforced By | Gate |
| ---------- | ------ | ---------: | ----------- | ---- |
| General (keywords, annotations, type safety, exceptions, GC, concurrency) | CODE-GEN | 16 | Error Prone + SpotBugs + Checkstyle (`IllegalCatch` → CODE-GEN-011, `RegexpSinglelineJava` → CODE-GEN-002a, `FinalParameters` → CODE-GEN-003b) + manual review | Compile + CI + PR review |
| Formatting (braces, parens, blank lines, line endings, imports) | CODE-FMT | 6 | Spotless (AOSP Google Java Format) + Checkstyle (`NeedBraces` → CODE-FMT-002) + `.gitattributes` | `./gradlew spotlessCheck` + `checkstyleMain` |
| Modules and Files (naming, packages, constants, generated code) | CODE-MAF | 4 | Checkstyle (`OneTopLevelClass` / `OuterTypeFilename` → CODE-MAF-001b, `HideUtilityClassConstructor` → CODE-MAF-003b) + compiler + manual review | `checkstyleMain` + Compile + PR review |
| Methods (naming, lifecycle, defensive checks) | CODE-FUN | 4 | Error Prone (`MissingOverride`, `DefaultCharset`) + Checkstyle (`FinalClass` / `HideUtilityClassConstructor` → CODE-FUN-003b) + compiler warnings + manual review | Compile + CI + PR review |
| Variables (naming, init, types, magic numbers) | CODE-VAR | 10 | Error Prone (`UnusedVariable`) + SpotBugs (null-deref) + Checkstyle (`LocalVariableName` → CODE-VAR-001c, `ParameterName` → CODE-VAR-001b) + manual review | Compile + CI + PR review |
| Control Structures (if/switch/loops) | CODE-CTL | 6 | Checkstyle (`MissingSwitchDefault` → CODE-CTL-002a, `InnerAssignment` → CODE-CTL-001, `FallThrough` → CODE-CTL-002b, `ModifiedControlVariable` → CODE-CTL-003b) + compiler + manual review | `./gradlew checkstyleMain` + PR review |
| Comments (JavaDoc, block, inline) | CODE-COM | 2 | Javadoc task + manual review | `./gradlew javadoc` + PR review |
| Security (input validation, integer safety, sensitive data, concurrency) | CODE-SEC | 10 | SpotBugs (FindSecBugs profile) + manual review | CI + PR review |

Counts reflect the current shipped rule set; adding or removing
a rule requires updating the count.

### Static Analysis Tooling

The table above references three Java static analyzers. Reference
configuration in `build.gradle`:

| Tool | Scope | Rollout pattern |
| ---- | ----- | --------------- |
| Error Prone | Compile-time bug patterns (null checks, `==` on Strings, unused vars, `MissingOverride`) | Start fail-soft (`allErrorsAsWarnings = true`); promote categories to errors as historical findings are triaged |
| SpotBugs | Bytecode analysis (null deref, concurrency, resource leaks) | Start fail-soft (`ignoreFailures = true`); reports uploaded as a CI artifact for review |
| Checkstyle | Mechanizable structure / control-flow / naming / defensive rules (braces, switch default, fall-through, inner-assignment, modified-control-variable, local-variable naming, one-class-per-file, final/utility classes, broad-catch, banned APIs) | Strict from day one (`maxWarnings = 0`); config grows incrementally. Formatting is owned by Spotless, not Checkstyle. By-design exceptions (e.g. ADR-011 top-level catches) live in `config/checkstyle/suppressions.xml` |

Tighten rollout (remove `allErrorsAsWarnings`, `ignoreFailures`)
as historical findings are triaged. The exact plugin versions
and rule sets live in the consuming project's `build.gradle` and
its Checkstyle / SpotBugs config files.

<!-- markdownlint-enable MD007 MD013 -->
