<!-- markdownlint-disable MD013 MD060 -->
# Development Setup

> **Scope:** Setup for contributing to Team271-Lib itself. Robot projects
> that depend on this library follow the same prerequisites but have
> their own build and deploy instructions.

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java JDK | 17 | Required by GradleRIO 2026 |
| WPILib VS Code | 2026 | IDE with FRC extensions, Gradle wrapper, toolchain |
| Git | 2.x+ | Version control |

### Installing WPILib

Download and install the WPILib installer for your platform from
<https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html>.
This installs VS Code with FRC extensions, Java 17, and the Gradle
wrapper.

---

## Clone and Build

```bash
git clone <repo-url>
cd Team271-Lib

# Verify the build works
./gradlew build
```

The first build downloads all dependencies (this may take a few
minutes). Subsequent builds are cached.

---

## IDE Setup

### VS Code (Recommended)

1. Open the project folder in WPILib VS Code
2. The WPILib extension auto-detects the GradleRIO project
3. Use the WPILib command palette (`Ctrl+Shift+P` → "WPILib:") for:
   - Build Robot Code
   - Deploy Robot Code
   - Simulate Robot Code

### IntelliJ IDEA

1. Open the project as a Gradle project
2. Set Project SDK to Java 17
3. Import Gradle project settings
4. Run `./gradlew build` from the terminal to verify

---

## Running Tests

```bash
# Run all tests with coverage
./gradlew test

# View coverage report
# Open build/reports/jacoco/test/html/index.html
```

Tests use the WPILib HAL in simulation mode — no robot hardware needed.

---

## Code Formatting

Spotless auto-formats code on build:

```bash
# Auto-format all source files
./gradlew spotlessApply

# Verify formatting (CI check)
./gradlew spotlessCheck
```

Format: Google Java Format with AOSP 4-space indent. See
[CONTRIBUTING.md](../../../CONTRIBUTING.md) for details.

---

## Simulation

### Desktop Simulation

```bash
./gradlew simulateJava
```

This launches the WPILib simulation GUI with:
- Simulated motor controllers (TalonFX sim state)
- Simulated sensors (encoders, IMU, range sensors)
- NetworkTables connection for dashboard testing

### Connecting a Dashboard

While simulation is running, connect Elastic Dashboard or
Shuffleboard to `localhost` to see telemetry and tunable values.

---

## Project Structure

```text
Team271-Lib/
├── src/
│   ├── main/java/com/team271/lib/    Library source
│   └── test/java/com/team271/lib/    JUnit 5 tests
├── docs/                              Design documentation
├── vendordeps/                        Vendor dependency JSONs
├── build.gradle                       Build configuration
├── CLAUDE.md                          Project overview
├── CONTRIBUTING.md                    Contribution guide
└── .editorconfig                      Editor formatting hints
```

See [Library Architecture](../planning/sdd/SDD-team271-lib.md) for the full
package map and class hierarchy.

---

## Useful Gradle Tasks

| Task | Purpose |
|------|---------|
| `./gradlew build` | Full build (format + compile + test) |
| `./gradlew compileJava` | Compile main source only |
| `./gradlew compileTestJava` | Compile main + test source |
| `./gradlew test` | Run tests with JaCoCo |
| `./gradlew spotlessApply` | Auto-format all code |
| `./gradlew spotlessCheck` | Verify formatting |
| `./gradlew simulateJava` | Run desktop simulation |
| `./gradlew deploy` | Deploy to RoboRIO |

---

## Troubleshooting

### "Could not find tools.jar"

Ensure JAVA_HOME points to a JDK 17 installation, not a JRE.

### Vendor dependency errors

Run `./gradlew build --refresh-dependencies` to re-download.
See [Vendor Dependencies](../planning/SCMP.md) for upgrade
instructions.

### Tests fail with native library errors

Ensure `HAL.initialize(500, 0)` is called in `@BeforeAll`.
See [Testing Strategy](../planning/SVP.md).
