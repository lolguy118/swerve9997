<!-- markdownlint-disable MD007 -->
# Team271-Lib Java Coding Standard — Modules and Files

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Modules and files companion to [`Standard.md`](Standard.md).
Contains `CODE-MAF-*` rules covering class and file naming,
package structure, constants organization, and generated-code
exemptions.

---

## 4.3 Modules and Files

<a id="code-maf-001"></a>

### CODE-MAF-001 -- Class and File Naming (Source: Barr, Team271-Lib)

a. Class names **shall** use PascalCase: `ExampleService`,
   `ConfigLoader`.

b. Each `.java` file **shall** contain exactly one top-level
   public class. The file name **shall** match the class name.

c. Inner classes and enums **shall** also use PascalCase:
   `ControlMode`, `ExampleConfig`.

d. Test classes **shall** be named `<ClassName>Test.java`.

<a id="code-maf-002"></a>

### CODE-MAF-002 -- Package Structure (Source: Team271-Lib)

a. All Java code **shall** reside under a top-level package
   chosen by the project (e.g., `com.example.app`,
   `org.example.svc`). The consuming project's coding-standard
   supplement fixes the concrete value; vendored libraries
   retain their upstream top-level package.

b. Package names **shall** be all lowercase with no underscores.

Subpackage conventions — for example, where service classes,
input handling, or any vendored-library code must live — belong
in the project's own coding standard and in any library-specific
coding standard it inherits.

<a id="code-maf-003"></a>

### CODE-MAF-003 -- Constants Organization (Source: Team271-Lib)

a. Constants referenced by more than one class **shall** be
   defined in a shared location rather than duplicated. The
   project's coding standard fixes the concrete artifact (e.g.,
   a `Constants` class) and any grouping convention.

b. Utility classes whose sole purpose is to hold `static`
   members **shall** declare a `private` constructor to prevent
   instantiation (see
   [CODE-FUN-003b](Standard-Methods.md#code-fun-003)).

c. Constants used only inside a single class **may** be
   declared as `private static final` fields on that class
   instead of in the shared constants artifact.

<a id="code-maf-004"></a>

### CODE-MAF-004 -- Generated Code (Source: Team271-Lib)

a. Generated files (e.g., `BuildConstants.java`) **shall
   not** be manually edited. Changes to generated code
   **should** be made through the generating tool.

b. Generated files are exempt from all `CODE-*` rules except
   safety-critical rules defined by the consuming project (for
   example, a current-limit configuration in vendor-generated
   tuning output still needs human review even though the
   generator owns the source). The full exemption list is
   recorded in
   [`Standard.md`](Standard.md#12-scope).

---
