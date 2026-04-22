<!-- markdownlint-disable MD013 MD060 -->
# Development Setup

This guide gets your computer ready to build, test, and simulate
any Team 271 Java project — the library, a season robot, or a
standalone tool. Follow it once per computer; you only need to
redo parts when the toolchain versions change.

> **Industry bridge.** Every software project starts with a
> *"getting started"* or *"installation"* guide that lists the
> toolchain and walks a new contributor through their first build.
> Keeping this setup consistent across projects (same Java version,
> same build tool, same dashboard) means you don't relearn the
> environment for every season.

---

## What you'll install

| Tool | Version | Purpose |
|------|---------|---------|
| **Java JDK** (Java Development Kit) | 17 | The language runtime the code is written in |
| **WPILib installer** | 2026 | Bundles an FRC-tuned VS Code, the Gradle build system, and FRC-specific tooling (simulator, Driver Station, etc.) |
| **Git** | 2.x or newer | Version control — lets you clone repositories and track changes |

### Installing WPILib

The WPILib installer is the one-stop setup for FRC (FIRST Robotics
Competition) Java development. It installs VS Code with the FRC
extensions, Java 17, and the Gradle wrapper automatically — you
don't need to download those separately.

Download the installer for your platform from
<https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html>
and follow the on-screen instructions.

### Installing Git

If you don't already have Git, install it from
<https://git-scm.com/downloads>. Git is what lets you download
(clone) a project repository and later commit changes back to it.

---

## Clone and Build

```bash
git clone <project-repo-url>
cd <project-folder>

# Verify the build works
./gradlew build
```

The first build downloads every dependency — this can take a few
minutes on a fresh checkout. Subsequent builds reuse the cache
and are much faster.

> **About `./gradlew`.** `./gradlew` (or `gradlew.bat` on Windows)
> is the **Gradle wrapper**. Use it instead of any separately
> installed `gradle` command — the wrapper automatically uses the
> version of Gradle the project was tested with, so every
> contributor builds with the same toolchain.

---

## IDE Setup

### VS Code (recommended for FRC)

1. Open the project folder in **WPILib VS Code** (the FRC-tuned
   VS Code installed by the WPILib installer).
2. The WPILib extension auto-detects the Gradle project.
3. Use the WPILib command palette (`Ctrl+Shift+P` → type `WPILib:`)
   for common tasks:
    - **Build Robot Code** — compile the project.
    - **Deploy Robot Code** — push the build to a robot (robot
      projects only).
    - **Simulate Robot Code** — run the project on your laptop
      without hardware.

### IntelliJ IDEA

1. Open the project as a Gradle project.
2. Set the project SDK to Java 17.
3. Let IntelliJ import the Gradle project settings.
4. Run `./gradlew build` from the terminal to verify.

---

## Running Tests

```bash
# Run every test with coverage measurement
./gradlew test

# View the coverage report in your browser:
#   build/reports/jacoco/test/html/index.html
```

Tests run against the WPILib HAL (Hardware Abstraction Layer) in
**simulation mode** — you don't need a physical robot. This lets
you run the full test suite on any laptop.

---

## Code Formatting

Every Team 271 Java project uses **Spotless** with **Google Java
Format (AOSP style)** to keep formatting identical across files
and contributors. The continuous-integration (CI) build checks
formatting automatically; a helper Gradle task applies it.

```bash
# Auto-format every source file
./gradlew spotlessApply

# Verify formatting without modifying files (CI runs this)
./gradlew spotlessCheck
```

If `spotlessCheck` fails, run `spotlessApply` and commit the
resulting reformatting. See
[`../../../CONTRIBUTING.md`](../../../CONTRIBUTING.md) for the
full contribution workflow.

---

## Simulation

```bash
./gradlew simulateJava
```

This launches the WPILib simulation GUI, which provides simulated
motor controllers, sensors, and a NetworkTables connection. You
can exercise robot code without any hardware connected.

### Connecting a Dashboard

While simulation is running, connect **Elastic Dashboard** or
**Shuffleboard** to `localhost` to see live telemetry values and
interact with tunable parameters.

---

## Project Structure (typical)

Every Team 271 Java project shares roughly this shape:

```text
<project-root>/
├── src/
│   ├── main/java/...        Main source (shipping code)
│   └── test/java/...        JUnit 5 tests
├── docs/                     Design documentation
├── vendordeps/               Vendor dependency JSON files (CTRE, WPILib, etc.)
├── build.gradle              Build configuration
├── CLAUDE.md                 Project overview (AI / LLM routing index)
├── CONTRIBUTING.md           Contribution workflow
└── .editorconfig             Editor formatting hints
```

Each project adds its own package layout on top of this common
shape, documented in that project's own design docs.

---

## Useful Gradle Tasks

| Task | Purpose |
|------|---------|
| `./gradlew build` | Full build — formats, compiles, runs tests |
| `./gradlew compileJava` | Compile main source only (fast check) |
| `./gradlew compileTestJava` | Compile main source + test source |
| `./gradlew test` | Run tests with JaCoCo coverage |
| `./gradlew spotlessApply` | Auto-format every source file |
| `./gradlew spotlessCheck` | Verify formatting (no changes made) |
| `./gradlew simulateJava` | Launch desktop simulation |
| `./gradlew deploy` | Deploy to a RoboRIO (robot projects only) |
| `./gradlew javadoc` | Generate API documentation |

---

## Troubleshooting

### "Could not find tools.jar"

Your `JAVA_HOME` environment variable points at a JRE (Java
Runtime Environment) instead of a JDK (Java Development Kit).
Reinstall Java 17 using the WPILib installer — it bundles the
correct JDK — and make sure `JAVA_HOME` points to the JDK folder.

### Vendor-dependency errors

A vendor dependency failed to download or is out of date. Force a
refresh:

```bash
./gradlew build --refresh-dependencies
```

Project-specific vendor-dependency upgrade procedures live in each
project's own configuration-management plan.

### Tests fail with native-library errors

Tests that create vendor devices need the WPILib HAL initialized
first. Make sure your test class calls `HAL.initialize(500, 0)`
in a `@BeforeAll` method. Project-specific testing conventions
live in each project's own verification plan.

### `./gradlew` command not found (Windows)

On Windows, use `gradlew.bat` instead of `./gradlew`, or use Git
Bash / WSL which understands the Unix-style invocation.

### Build fails immediately with a Java version error

Check your Java version:

```bash
java -version
```

It must report Java 17. If it reports a different version, either
install the WPILib installer (which bundles Java 17) or adjust
`JAVA_HOME` to point at a Java 17 JDK.
