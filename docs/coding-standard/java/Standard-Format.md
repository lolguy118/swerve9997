<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard — Formatting

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Formatting companion to [`Standard.md`](Standard.md). Contains
`CODE-FMT-*` rules covering braces, parentheses, blank lines,
line endings, and imports.

> **Note:** Most formatting rules are automatically enforced by
> Spotless (Google Java Format, AOSP variant). Specifically,
> Spotless enforces:
>
> - 4-space indentation (AOSP)
> - no tab characters
> - unused-import removal
> - import ordering
> - long-string reflow
>
> The rules below cover **manual-compliance** items (braces,
> parentheses, blank lines, line endings, wildcard-import
> exceptions) that Spotless does not catch.

See [`Standard-Compliance.md`](Standard-Compliance.md#51-spotless)
for the project's Spotless configuration.

---

## 4.2 Formatting

<a id="code-fmt-001"></a>

### CODE-FMT-001 -- Line Length (Source: Team271-Lib)

a. Line length is managed by Spotless `reflowLongStrings()`.
   Developers **should** write readable lines; the formatter
   will wrap as needed.

b. When manually wrapping, continuation lines **should** be
   indented by 8 spaces (double indent) from the original line.

<a id="code-fmt-002"></a>

### CODE-FMT-002 -- Braces (Source: MISRA Rule 15.6, Barr)

> *Industry note: MISRA Rule 15.6 requires that the body of every
> if/else/while/for/switch be a compound statement (i.e., wrapped
> in braces). The Barr Group standard has the same rule. The
> reason: a one-liner without braces invites someone to add a
> second line that looks like it is inside the block but is not.
> This has caused real security vulnerabilities (look up Apple's
> "goto fail" bug).*

a. Braces **shall** surround all blocks following `if`, `else`,
   `while`, `do`, `for`, and `switch` statements, even when the
   block contains a single statement. Enforced by Checkstyle's
   `NeedBraces` rule; Spotless formats existing braces but does
   not add missing ones.

b. Brace style follows Google Java Format AOSP: opening brace on
   the same line as the statement, closing brace on its own line.

<a id="code-fmt-003"></a>

### CODE-FMT-003 -- Parentheses (Source: MISRA Rule 12.1)

> *Industry note: MISRA Rule 12.1 says operator precedence should
> be made explicit with parentheses. Not everyone memorizes that
> `&&` binds tighter than `||`, or that `<<` binds tighter than
> `+`. Adding parentheses costs nothing and prevents misreading.*

a. Parentheses **shall** be used to clarify operator precedence
   when mixing categories of operators (arithmetic with comparison,
   bitwise with logical, ternary within larger expressions):

   ```java
   /* CORRECT: explicit precedence for mixed operators */
   if ((flags & MASK) != 0) { ... }
   if ((a + b) > (c * d)) { ... }
   ```

b. Parentheses **shall** also be used:

   - around each comparison sub-expression that appears inside a
     boolean chain (`&&` / `||`), even when the chain uses only
     one logical operator, and
   - around each sub-group when `&&` and `||` are mixed in the
     same expression.

   Parentheses around a bare boolean variable or boolean-returning
   method call inside a chain are **not required** — the
   precedence concern only arises when comparisons or mixed
   logical operators are involved.

   ```java
   /* CORRECT: comparisons parenthesized inside chains */
   if ((depth > 0) && (depth < MAX_DEPTH)) { ... }
   if ((state == State.IDLE) || (state == State.HOMED)) { ... }

   /* CORRECT: mixed && / || disambiguated */
   if ((isReady && (depth > 0)) || overrideEnabled) { ... }

   /* CORRECT: pure-boolean chain — no extra parens needed */
   if (signals.contains(A) || signals.contains(B)) { ... }

   /* WRONG: relies on implicit comparison-vs-logical precedence */
   if (depth > 0 && depth < MAX_DEPTH) { ... }
   ```

<a id="code-fmt-004"></a>

### CODE-FMT-004 -- Blank Lines and Section Comments (Source: Barr)

a. Each source line **shall** contain only one statement.

b. Blank lines **shall** separate logical blocks: between methods,
   before and after control structures, between declaration
   groups and executable code.

c. Section comments using block comment style **shall** be used
   to organize code within classes:

   ```java
   /*
    * Fields
    */

   /*
    * Lifecycle
    */
   ```

d. Section comment text **shall** be concise (1–3 words).

<a id="code-fmt-005"></a>

### CODE-FMT-005 -- Line Endings (Source: Team271-Lib)

a. All source files **shall** use LF (0x0A) line endings. This is
   **not** automatically enforced by Spotless (the
   `endWithNewline()` setting only ensures a trailing newline
   character, not the line ending style). Enforce LF via
   `.gitattributes` (`*.java text eol=lf`) or by adding
   `lineEndings('UNIX')` to the Spotless configuration.

<a id="code-fmt-006"></a>

### CODE-FMT-006 -- Import Statements (Source: Team271-Lib)

a. Wildcard imports (`import foo.*`) **shall not** be used,
   except for narrow, framework-established conventions
   explicitly enumerated by the consuming project. The default
   policy is no wildcard imports; each accepted exception
   **shall** be documented in the project's own coding-standard
   supplement together with the rationale.

b. Unused imports **shall** be removed. Spotless
   `removeUnusedImports()` enforces this automatically.

c. Import order is managed by Spotless. Do not manually reorder.

d. Static imports **should** be used for frequently referenced
   constants when it improves readability.

---
