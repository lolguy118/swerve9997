<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard — Reference Appendices

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Reference appendices companion to [`Standard.md`](Standard.md).
Contains lookup tables, keyword lists, and reference material
cited by specific `CODE-*` rules in the main coding standard.

Appendix lettering follows the upstream Java coding standard the
structure was adapted from. Appendices beyond C and I (lifecycle
reference, vendor usage patterns, GC tuning notes) live in
domain-specific overlays such as the FRC overlay's
`Standard-FRC-Conventions.md`; they are not part of the
pure-Java standard.

---

## Appendix A: Approved Identifier Abbreviations

Short-form tokens permitted in code identifiers (variable, method,
and field names). This is an *allowlist for naming*, not a
glossary — for proper-name acronyms used in the standard's prose,
the project's own glossary or [`Standard.md`](Standard.md) §1.3
is authoritative.

| Abbreviation | Meaning |
| ------------ | ------- |
| addr | address |
| arg | argument (method parameter prefix) |
| avg | average |
| buf | buffer |
| cfg | configuration |
| ch | channel |
| cmd | command |
| cnt | count |
| ctrl | control |
| def | default |
| dev | device |
| drv | driver |
| en | enable |
| err | error |
| ext | extension / extended |
| freq | frequency |
| hw | hardware |
| hz | hertz |
| idx | index |
| init | initialize |
| intf | interface |
| len | length |
| max | maximum |
| min | minimum |
| mod | module |
| msg | message |
| num | number |
| pos | position |
| pwr | power |
| req | request |
| rx | receive |
| sec | second |
| sw | software |
| tmp | temporary |
| tx | transmit |
| val | value |
| vel | velocity |
| ver | version |

Project-specific or domain-specific abbreviations (units,
hardware bus names, framework-specific terms) are added by the
consuming project in its own appendix supplement.

---

## Appendix B: Java Reserved Words

The following identifiers **shall not** be used as variable,
method, or class names. See
[CODE-VAR-001](Standard-Variables.md#code-var-001)
and
[CODE-FUN-001](Standard-Methods.md#code-fun-001).

`abstract`, `assert`, `boolean`, `break`, `byte`, `case`, `catch`,
`char`, `class`, `const`, `continue`, `default`, `do`, `double`,
`else`, `enum`, `extends`, `final`, `finally`, `float`, `for`,
`goto`, `if`, `implements`, `import`, `instanceof`, `int`,
`interface`, `long`, `native`, `new`, `package`, `private`,
`protected`, `public`, `return`, `short`, `static`,
`strictfp` (reserved but no-op since Java 17),
`super`, `switch`, `synchronized`, `this`, `throw`, `throws`,
`transient`, `try`, `void`, `volatile`, `while`

Also avoid: `var` (reserved type name), `record`, `sealed`,
`permits`, `yield` (context keywords), `true`, `false`, `null`
(literal values).

---

## Appendix C: `final` Keyword Usage Guide

This appendix expands on
[CODE-GEN-003b](Standard-General.md#code-gen-003).

### When to Use `final`

| Context | Requirement | Example |
| ------- | ----------- | ------- |
| Method parameters | **Shall** (mandatory) | `void foo(final int argValue)` |
| Fields set once | **Shall** (mandatory) | `private final Timer mWatchdog = new Timer();` |
| Constants | **Shall** (mandatory) | `static final double MAX_RATIO = 4.5` |
| Utility classes | **Shall** (mandatory) | `public final class StringUtil` |
| Constants inner classes | **Shall** (mandatory) | `public static final class Pins` |
| Local variables | **Should** (encouraged) | `final double voltage = level * SCALE;` |
| Methods | Rarely needed | Only to prevent override in specific cases |
| Classes (non-utility) | Rarely needed | Only for leaf classes that must not be extended |

### Why `final`?

- **Parameters:** Prevents accidental reassignment; makes intent
  clear.
- **Fields:** Enables the compiler to detect accidental mutation.
  Communicates "this value is set once and never changes."
- **Constants:** `static final` enables compile-time constant
  folding and communicates that the value is a true constant.
- **Utility classes:** Prevents meaningless subclassing.

---

## Appendix I: Naming Convention Quick Reference

This appendix is a flat lookup table covering the conventions
defined in:

- [CODE-FUN-001](Standard-Methods.md#code-fun-001)
  (method naming)
- [CODE-VAR-001](Standard-Variables.md#code-var-001)
  (variable / field / parameter naming)
- [CODE-VAR-005](Standard-Variables.md#code-var-005)
  (enum naming)
- [CODE-MAF-001](Standard-Modules.md#code-maf-001)
  (class / file naming)
- [CODE-MAF-002](Standard-Modules.md#code-maf-002)
  (package naming)

The originating rule is authoritative; the table summarises.

| Context | Convention | Prefix | Example |
| ------- | ---------- | ------ | ------- |
| Classes | PascalCase | (none) | `ExampleService`, `ConfigLoader` |
| Inner classes | PascalCase | (none) | `ExampleConfig`, `Pins` |
| Enums (type) | PascalCase | (none) | `ControlMode`, `LogLevel` |
| Enum values | UPPER_SNAKE_CASE | (none) | `IDLE`, `RUNNING`, `SHUT_DOWN` |
| Interfaces | PascalCase | (none) | `Closeable`, `Interpolable` |
| Methods | camelCase | (none) | `start()`, `computeOffset()` |
| Boolean methods | camelCase | `is` / `has` / `can` | `isReady()`, `hasData()`, `canRetry()` |
| Instance fields | camelCase | `m` | `mMode`, `mTimer` (*) |
| Boolean fields | camelCase | prefix + `is` / `has` | `mIsReady`, `mHasData` (*) |
| Operational constants | UPPER_SNAKE_CASE | (none) | `MAX_RETRIES`, `DEFAULT_TIMEOUT_MS` |
| Tunable constants | camelCase | `k` | `kRampUpDuration`, `kRetryBackoff` |
| Static mutable fields | camelCase | `m` | `mInstance` (singleton) (*) |
| Method parameters | camelCase | `arg` | `argTimeout`, `argConfig` |
| Local variables | camelCase | (none) | `index`, `delta` |
| Temporary locals | camelCase | `tmp` | `tmpResult`, `tmpBuffer` |
| Packages | lowercase | (none) | `com.example.app`, `com.example.lib.util` |

(*) Field examples are shown with the `m` resolution of the
instance prefix — adjust capitalization to the resolved prefix
per [CODE-VAR-001](Standard-Variables.md#code-var-001).

The instance-field prefix for this standard is
`m`, set when the coding-standards overlay is
applied — common resolutions are `m` (Hungarian-style; the
convention this template was extracted from; capitalize the
first word after the prefix), `m_` (separator style; plain
camelCase after the underscore), and the empty string (no
prefix, modern Google Java style). The project's own coding
standard records the chosen value and the rationale.

<!-- markdownlint-enable MD007 -->
