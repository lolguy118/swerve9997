<!-- markdownlint-disable MD007 MD013 -->
# Team271-Lib Java Coding Standard — Security Coding Practices

| Field | Value |
| ----- | ----- |
| Companion To | [`Standard.md`](Standard.md) |
| Revision | 0.1 |
| Date | `2026-06-14` |
| Status | Draft |

Security companion to [`Standard.md`](Standard.md). Contains
`CODE-SEC-*` rules adapted from the SEI CERT Oracle Coding
Standard for Java and the OWASP Top 10 / OWASP Java cheat sheets,
with CWE traceability.

All `CODE-SEC-*` rules supplement the general rules in
[Sections 4.1–4.6](Standard.md#4-coding-guidelines) of the main
coding standard, which remain applicable.

---

## 4.9 Security Coding Practices

This section defines mandatory security coding rules. The rules
complement the safety-focused rules in Sections 4.1–4.7 by
addressing threats that exploit software vulnerabilities. All
`CODE-SEC-*` rules use **shall** (mandatory).

Each rule references the SEI CERT Java rule or OWASP guidance
it satisfies, along with the applicable CWE (Common Weakness
Enumeration) identifier for traceability.

<a id="code-sec-001"></a>

### CODE-SEC-001 -- Trust Boundary Input Validation (Source: Team271-Lib)

**CWE:** CWE-20 (Improper Input Validation)

a. All data received across a **trust boundary** **shall** be
   validated before use. Trust boundaries include:
   (1) network endpoints (HTTP, gRPC, WebSocket, raw socket),
   (2) inter-process communication (named pipes, shared memory,
   message queues),
   (3) plugin or scripting-engine interfaces,
   (4) user-supplied configuration, asset, or import files,
   (5) data received from third-party libraries or services.

b. Message validation **shall** follow a
   **structure-before-content** order:
   (1) validate the encoded length of the incoming payload
   against the maximum expected size,
   (2) parse and validate protocol-header fields (message type,
   version),
   (3) validate any embedded length fields *before* using them
   to slice into the payload,
   (4) validate payload content last.

   ```java
   public Result<Message> parseMessage(final byte[] argBuf) {
       Result<Message> result = Result.invalid(InvalidReason.UNKNOWN);

       if (argBuf == null) {
           result = Result.invalid(InvalidReason.NULL_INPUT);
       } else if (argBuf.length < MSG_HEADER_SIZE) {
           result = Result.invalid(InvalidReason.SHORT_HEADER);
       } else {
           final int type = argBuf[MSG_TYPE_OFFSET] & 0xFF;
           if (type > MSG_TYPE_MAX) {
               result = Result.invalid(InvalidReason.UNKNOWN_TYPE);
           } else {
               final int payloadLen = readUnsignedShort(argBuf, MSG_LEN_OFFSET);
               if (payloadLen > (argBuf.length - MSG_HEADER_SIZE)) {
                   result = Result.invalid(InvalidReason.LENGTH_OVERFLOW);
               } else {
                   result = parsePayload(argBuf, MSG_HEADER_SIZE, payloadLen);
               }
           }
       }

       return result;
   }
   ```

c. Length fields embedded in received messages **shall** be
   validated against both a minimum and a maximum expected
   value before being used as a loop bound, array index, or
   buffer size.

d. Methods that process data from trust boundaries **shall**
   be identified with a `/* TRUST_BOUNDARY */` marker comment
   in their JavaDoc block to support security review.

   This rule extends
   [CODE-GEN-008](Standard-General.md#code-gen-008)
   (external input validation) with security-specific structure
   and ordering requirements.

<a id="code-sec-002"></a>

### CODE-SEC-002 -- Integer Overflow Prevention (Source: CERT Java NUM00-J)

**CWE:** CWE-190 (Integer Overflow or Wraparound),
CWE-191 (Integer Underflow (Wrap or Wraparound)),
CWE-681 (Incorrect Conversion between Numeric Types)

a. Arithmetic operations on values derived from external input
   (as defined in [CODE-SEC-001](#code-sec-001))
   **shall** include overflow or underflow checks **before**
   the operation is performed.

b. Java arithmetic on `int` and `long` wraps silently. Use the
   exact-arithmetic methods from `java.lang.Math` for
   overflow-sensitive computations on externally-derived values:

   ```java
   /* CORRECT: overflow surfaces as ArithmeticException */
   final int total = Math.addExact(argA, argB);

   /* CORRECT: clamp to bounded range before use */
   final int bounded = Math.max(MIN_LEN, Math.min(MAX_LEN, argLen));
   ```

c. Conversions that narrow a wider type to a smaller one
   (`long` → `int`, `int` → `short`, `int` → `byte`) **shall**
   verify the value fits in the destination range before the
   cast, or use `Math.toIntExact()` / equivalent.

d. Unsigned interpretations of Java's signed integers (e.g.,
   reading a `byte` as `0..255`) **shall** use the
   `Byte.toUnsignedInt()` / `Integer.toUnsignedLong()` /
   `Short.toUnsignedInt()` helpers rather than ad-hoc masking
   that risks sign extension.

<a id="code-sec-003"></a>

### CODE-SEC-003 -- Buffer and Collection Size Enforcement (Source: Team271-Lib, OWASP Top 10 A08)

**CWE:** CWE-789 (Memory Allocation with Excessive Size Value),
CWE-770 (Allocation of Resources Without Limits or Throttling)

a. Methods that allocate a buffer or collection sized by an
   externally-supplied length parameter **shall** validate the
   length against a documented maximum **before** the
   allocation:

   ```java
   /* WRONG: attacker-controlled allocation */
   final byte[] buf = new byte[argLen];

   /* CORRECT: bounded allocation */
   if ((argLen < 0) || (argLen > MAX_PAYLOAD_BYTES)) {
       throw new IllegalArgumentException(
           "argLen out of range: " + argLen);
   }
   final byte[] buf = new byte[argLen];
   ```

b. String operations that build large intermediate values from
   external input (e.g., `String.repeat(n)`, `argInput +
   argInput + ...`) **shall** validate `n` and the cumulative
   input length against documented maxima before the operation.

c. Deserialization of untrusted input **shall not** be
   performed via `java.io.ObjectInputStream` against an
   uncontrolled stream. Use a schema-bounded format (JSON
   parsed with a non-reflective mapper, Protocol Buffers,
   FlatBuffers) and apply schema-level size limits at the
   parser configuration.

d. Decompression, base64 decoding, and any other "expansion
   operation" on external input **shall** enforce a maximum
   output size before allocating the destination buffer
   ("zip-bomb" defence).

<a id="code-sec-004"></a>

### CODE-SEC-004 -- Secure Defaults and Fail-Safe Initialization (Source: Team271-Lib, OWASP Top 10 A05)

**CWE:** CWE-1188 (Initialization of a Resource with an Insecure Default)

a. Fields representing permissions, access levels, or
   enable/disable states **shall** be initialized to the most
   restrictive value (denied, disabled, lowest privilege).

b. Configuration objects **shall** be initialized to a
   **deny-by-default** state — all permissions off, all
   debug surfaces off, all integrations disabled — before any
   fields are selectively enabled from configuration input.

c. The `default` case in `switch` statements that process
   command types, message types, or protocol identifiers from
   external sources **shall** set the result to an error value
   and **shall not** perform any action that modifies system
   state. This strengthens
   [CODE-CTL-002](Standard-Control.md#code-ctl-002)
   for security-relevant switches.

d. Error paths **shall** clear (overwrite or null) any
   partially populated output objects before propagating the
   error, to prevent leaking stale or sensitive data to
   callers.

<a id="code-sec-005"></a>

### CODE-SEC-005 -- Sensitive Data Handling (Source: CERT Java MSC59-J, OWASP Top 10 A02)

**CWE:** CWE-226 (Sensitive Information in Resource Not Removed Before Reuse),
CWE-532 (Insertion of Sensitive Information into Log File)

a. Passwords and other short-lived secrets **shall** be held
   in `char[]` (or `byte[]`), never `String`. `String` is
   immutable and cannot be cleared; once a password lands in a
   `String`, it sits in the heap until garbage collection
   (which may never happen for the lifetime of the JVM).

b. Buffers holding security-sensitive data (cryptographic
   keys, authentication tokens, passwords, session
   identifiers) **shall** be overwritten with zeros immediately
   after their last use:

   ```java
   final char[] password = readPasswordFromKeystore();
   try {
       authenticate(password);
   } finally {
       java.util.Arrays.fill(password, '\0');
   }
   ```

c. Security-sensitive data **shall not** be written to debug
   logs, console output, or diagnostic interfaces, even in
   debug builds. Methods that handle sensitive data **shall**
   include a `/* SENSITIVE_DATA */` marker comment.

d. `toString()` on classes that carry sensitive fields
   **shall** redact those fields. The default
   `Object.toString()` or a record's autogenerated `toString()`
   prints every field by reflection — for classes carrying
   secrets, override `toString()` explicitly to emit only
   non-sensitive metadata (class name, id, length).

e. Security-sensitive data **shall not** be stored in mutable
   `static` fields (reinforces
   [CODE-GEN-015](Standard-General.md#code-gen-015)).
   If cross-method access is required, pass the secret via
   method parameters and clear it after the call.

<a id="code-sec-006"></a>

### CODE-SEC-006 -- Attack Surface Minimization (Source: Team271-Lib, OWASP Top 10 A05)

**CWE:** CWE-489 (Active Debug Code),
CWE-732 (Incorrect Permission Assignment for Critical Resource)

a. Debug, diagnostic, and test endpoints (admin servlets,
   JMX endpoints exposing mutator operations, scripting-engine
   handles, heap-dump triggers) **shall** be disabled by
   default and **shall** be enabled only via a documented
   configuration flag in non-production deployments. Release
   builds **should** strip the implementations entirely where
   the build system supports it.

b. Methods and fields that are not used outside their class
   **shall** be declared `private`. Methods and fields used
   only within the package **shall** use package-private (no
   modifier). This reinforces
   [CODE-GEN-003a](Standard-General.md#code-gen-003);
   the principle here is "narrowest visibility that compiles."

c. Unused methods, unreachable code, and dead code **shall**
   be removed from source files rather than commented out or
   conditionally compiled with permanently false guards. This
   complements
   [CODE-COM-001](Standard-Comments.md#code-com-001).

d. Public API surface exposed by library packages **shall** be
   limited to the minimum set required by the documented
   interface. Internal helper classes **should** live in
   `internal` sub-packages and **should not** be re-exported
   from the public package.

<a id="code-sec-007"></a>

### CODE-SEC-007 -- Concurrency Safety for Shared Resources (Source: CERT Java VNA00-J / VNA01-J / LCK00-J)

**CWE:** CWE-362 (Concurrent Execution using Shared Resource with Improper Synchronization),
CWE-367 (Time-of-check Time-of-use (TOCTOU) Race Condition)

a. All accesses to fields shared between threads **shall** be
   protected by a synchronization mechanism (a `synchronized`
   block on a private final lock, a `java.util.concurrent.atomic`
   type, a `java.util.concurrent.locks.Lock`, or a
   thread-confinement design that guarantees no sharing
   occurs). The chosen mechanism **shall** be documented in a
   comment at the field declaration. See also
   [CODE-GEN-016](Standard-General.md#code-gen-016).

b. Synchronization on a public-visible object (`synchronized
   (this)`, `synchronized (SomeClass.class)`,
   `synchronized (someString.intern())`) **shall not** be
   used. Use a `private final Object` lock instead — external
   code cannot interfere with a lock it cannot reach (CERT
   Java LCK00-J).

c. Time-of-check-to-time-of-use (TOCTOU) vulnerabilities
   **shall** be mitigated: validation of shared state and the
   action based on that validation **shall** occur within the
   same critical section, not spanning a lock release.

d. Inter-thread communication **should** use
   `java.util.concurrent` primitives (`BlockingQueue`,
   `ConcurrentHashMap`, `CompletableFuture`) in preference to
   hand-rolled `wait()` / `notify()` patterns.

e. Nested acquisition of multiple locks **should** be avoided.
   When nested locking is unavoidable, all code paths
   **shall** acquire locks in a single, documented,
   predetermined order to prevent deadlock. The lock
   acquisition order **shall** be documented in a comment at
   each acquisition site.

<a id="code-sec-008"></a>

### CODE-SEC-008 -- Communication Channel Integrity (Source: Team271-Lib, OWASP Top 10 A08)

**CWE:** CWE-345 (Insufficient Verification of Data Authenticity)

This rule applies only to communication crossing trust
boundaries as defined in
[CODE-SEC-001](#code-sec-001).
Internal in-process method calls are exempt.

a. All messages crossing trust boundaries **shall** include
   an integrity-check field (HMAC-SHA-256 or stronger; CRC-32
   is acceptable only for non-adversarial transport-layer
   corruption detection, not for authenticity).

b. Communication protocols crossing trust boundaries **shall**
   include a sequence number or monotonic counter. The
   receiver **shall** verify that the sequence number is
   within the expected window and **shall** reject duplicate
   or out-of-sequence messages.

c. The protocol **shall** include a source identifier field.
   The receiver **shall** verify the source identifier
   against a configured allowlist before processing.

d. Stale message detection **shall** be implemented for
   safety- or security-critical channels: messages older than
   a configured timeout **shall** be discarded. The timeout
   value **shall** be defined as a named constant
   ([CODE-VAR-008](Standard-Variables.md#code-var-008)).

<a id="code-sec-009"></a>

### CODE-SEC-009 -- Constant-Time Security Comparisons (Source: JDK java.security.MessageDigest.isEqual, OWASP)

**CWE:** CWE-208 (Observable Timing Discrepancy)

a. Comparison of security-sensitive byte sequences
   (authentication tokens, HMAC values, message authentication
   codes, password hashes) **shall** be performed using a
   constant-time comparison that does not short-circuit on
   the first mismatched byte.

b. Use `java.security.MessageDigest.isEqual(byte[], byte[])`
   for byte-array comparisons; the JDK implementation is
   documented as constant-time:

   ```java
   import java.security.MessageDigest;

   public boolean tokensMatch(final byte[] argExpected,
                              final byte[] argReceived) {
       return MessageDigest.isEqual(argExpected, argReceived);
   }
   ```

c. `Arrays.equals(byte[], byte[])` and `Objects.equals` on
   `String` **shall not** be used for security-sensitive
   comparisons. Both short-circuit on the first mismatch and
   leak information about the prefix length via timing.

<a id="code-sec-010"></a>

### CODE-SEC-010 -- Security Event Reporting (Source: CERT Java FIO13-J, OWASP Top 10 A09)

**CWE:** CWE-778 (Insufficient Logging),
CWE-532 (Insertion of Sensitive Information into Log File)

a. Methods at trust boundaries
   ([CODE-SEC-001](#code-sec-001))
   that detect a validation failure **shall** report the
   failure through a project-defined security event reporting
   mechanism before returning or throwing. The report
   **shall** include: (1) the module and method identifier,
   (2) the type of failure (range, length, MAC, sequence,
   source), and (3) a monotonic event counter.

b. The security event reporting mechanism **shall not** block
   or delay the calling method. It **shall** use a bounded
   queue with a documented overflow policy (drop oldest, drop
   newest, or sample) — never an unbounded queue.

c. Security event reports **shall not** include the invalid
   data values themselves (to prevent information leakage); a
   report **shall** include only the failure type, the
   location, and any non-sensitive identifiers.

d. Security event counters **shall** be readable by the
   health-monitoring subsystem but **shall not** be clearable
   by external commands (write-once-read-many semantics from
   the software perspective).

---

## 5.4 Security-Specific Static Analysis

In addition to the standard static analysis checks
([Standard-Compliance.md §5.4](Standard-Compliance.md#54-code-review-checklist)),
the following security-focused checks **shall** be applied to
support the `CODE-SEC-*` rules:

- **SpotBugs with FindSecBugs plugin** — adds Java-specific
  security detectors (predictable random, hard-coded
  passwords, XXE, SSRF, command injection patterns).
  Recommended starting profile: enable all detectors,
  triage findings into the project's deviation list.
- **OWASP Dependency-Check** — scans the project's dependency
  graph against the National Vulnerability Database. Run on
  every PR and on a scheduled cadence; CVSS score thresholds
  for build-failure live in the consuming project's
  `build.gradle`.
- **Marker-comment verification** — CI **should** include
  a grep-based check that methods at trust boundaries include
  the `/* TRUST_BOUNDARY */` marker (CODE-SEC-001d) and
  methods handling sensitive data include the
  `/* SENSITIVE_DATA */` marker (CODE-SEC-005c).

<!-- markdownlint-enable MD007 MD013 -->
