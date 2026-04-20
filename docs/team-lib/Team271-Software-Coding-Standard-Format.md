<!-- markdownlint-disable MD013 -->
<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Format

> **Note:** Most formatting rules are automatically enforced by Spotless
> (Google Java Format, AOSP variant). This section documents what the
> formatter does and identifies rules that require manual compliance.

### CODE-FMT-001 -- Line Length

a. Line length is managed by Spotless `reflowLongStrings()`. Developers
   should write readable lines; the formatter will wrap as needed.

b. When manually wrapping, continuation lines **should** be indented
   by 8 spaces (double indent) from the original line.

### CODE-FMT-002 -- Braces

> *Industry note: MISRA Rule 15.6 requires that the body of every
> if/else/while/for/switch be a compound statement (i.e., wrapped in
> braces). The Barr Group standard has the same rule. The reason: a
> one-liner without braces invites someone to add a second line that
> looks like it is inside the block but is not. This has caused
> real security vulnerabilities (look up Apple's "goto fail" bug).*

a. Braces **shall** surround all blocks following `if`, `else`,
   `while`, `do`, `for`, and `switch` statements, even when the
   block contains a single statement. This requires manual
   compliance; Spotless formats existing braces but does not add
   missing ones.

b. Brace style follows Google Java Format AOSP: opening brace on
   the same line as the statement, closing brace on its own line.

### CODE-FMT-003 -- Parentheses

> *Industry note: MISRA Rule 12.1 says operator precedence should be
> made explicit with parentheses. Not everyone memorizes that `&&`
> binds tighter than `||`, or that `<<` binds tighter than `+`. Adding
> parentheses costs nothing and prevents misreading.*

a. Parentheses **shall** be used to clarify operator precedence when
   mixing different categories of operators (arithmetic with comparison,
   bitwise with logical, ternary within larger expressions):

   ```java
   /* CORRECT: explicit precedence for mixed operators */
   if ((flags & MASK) != 0) { ... }
   if ((a + b) > (c * d)) { ... }
   ```

   For simple comparisons joined by `&&` or `||`, extra parentheses
   are **recommended** but not required -- Java's precedence for
   comparison operators (`>`, `<`, `==`) binding tighter than logical
   operators (`&&`, `||`) is well-established:

   ```java
   /* Both are acceptable */
   if ((depth > 0) && (depth < MAX_DEPTH)) { ... }
   if (depth > 0 && depth < MAX_DEPTH) { ... }
   ```

### CODE-FMT-004 -- Blank Lines and Section Comments

a. Each source line **shall** contain only one statement.

b. Blank lines **shall** separate logical blocks: between methods,
   before and after control structures, between declaration groups
   and executable code.

c. Section comments using block comment style **shall** be used to
   organize code within classes:

   ```java
   /*
    * Variables
    */

   /*
    * Robot Lifecycle
    */
   ```

d. Section comment text **shall** be concise (1-3 words).

### CODE-FMT-005 -- Indentation

a. Each indentation level **shall** be 4 spaces (AOSP standard).
   Enforced by Spotless.

### CODE-FMT-006 -- Tabs

a. Tab characters **shall** not appear in source files. Enforced
   by Spotless.

### CODE-FMT-007 -- Line Endings

a. All source files **shall** use LF (0x0A) line endings. This is
   **not** automatically enforced by Spotless (the `endWithNewline()`
   setting only ensures a trailing newline character, not the line
   ending style). Enforce LF via `.gitattributes` (`* text=auto eol=lf`)
   or by adding `lineEndings('UNIX')` to the Spotless configuration.

### CODE-FMT-008 -- Import Statements

a. Wildcard imports (`import foo.*`) **shall** not be used, with one
   documented exception: `import static edu.wpi.first.units.Units.*`
   is an established WPILib convention and is permitted.

b. Unused imports **shall** be removed. Spotless
   `removeUnusedImports()` enforces this automatically.

c. Import order is managed by Spotless. Do not manually reorder.

---
