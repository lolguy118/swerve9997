<!-- markdownlint-disable MD013 MD060 -->
# Vendor Dependency Management

This document describes how to manage vendor dependencies (vendordeps)
in Team271-Lib.

---

## Current Dependencies

| Dependency | Version | File | Purpose |
|-----------|---------|------|---------|
| CTRE Phoenix 6 | 26.1.3 | `Phoenix6-frc2026-latest.json` | TalonFX, CANCoder, Pigeon2, CANrange |
| AdvantageKit | 26.0.2 | `AdvantageKit.json` | Structured logging, replay |
| PathplannerLib | 2026.1.2 | `PathplannerLib.json` | Autonomous path planning |
| WPILib New Commands | (bundled) | `WPILibNewCommands.json` | WPILib command framework |

All vendordep JSON files live in `vendordeps/`.

---

## How Vendordeps Work

Each JSON file defines:
- **Maven repositories** — where to download the library JARs
- **jsonUrl** — a URL to the latest version of this JSON file
- **uuid** — unique identifier (must not conflict)
- **Java dependencies** — compile-time and runtime JARs
- **JNI dependencies** — native libraries for simulation and RoboRIO

Gradle resolves these at build time using the GradleRIO plugin.

---

## Checking for Updates

### Manual Check

Compare the `version` field in each JSON file against the latest
available from the vendor:

| Dependency | Latest Version URL |
|-----------|-------------------|
| Phoenix 6 | `https://maven.ctr-electronics.com/release/com/ctre/phoenix6/latest/Phoenix6-frc2026-latest.json` |
| AdvantageKit | `https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json` |
| PathplannerLib | `https://3015rangerrobotics.github.io/pathplannerlib/PathplannerLib.json` |

### Using WPILib VS Code

1. Open WPILib command palette (`Ctrl+Shift+P`)
2. Select "WPILib: Manage Vendor Libraries"
3. Select "Check for updates (online)"

---

## Upgrading a Dependency

### Step 1: Download the New JSON

Download the latest JSON from the vendor's `jsonUrl` and replace the
file in `vendordeps/`.

Or use VS Code: "WPILib: Manage Vendor Libraries" → "Install new
libraries (online)" → paste the `jsonUrl`.

### Step 2: Refresh Dependencies

```bash
./gradlew build --refresh-dependencies
```

### Step 3: Verify

1. **Compile check:**
   ```bash
   ./gradlew compileJava compileTestJava
   ```
   Watch for deprecation warnings or API changes.

2. **Test check:**
   ```bash
   ./gradlew test
   ```
   All tests must pass. New vendor versions may change behavior
   (e.g., default config values, signal refresh timing).

3. **Review breaking changes:**
   Check the vendor's release notes for API changes. Common issues:
   - Renamed methods or classes
   - Changed default values (current limits, signal frequencies)
   - New required configuration steps
   - Deprecated API removal

### Step 4: Update Documentation

If the upgrade changes API patterns referenced in design docs (e.g.,
a new Phoenix 6 config method), update the affected docs.

---

## Version Pinning

The vendordep JSON files **are** the version pins. Committing the
JSON file to git locks the version for all developers. Do not use
the `-latest` suffix in the `jsonUrl` for builds that need
reproducibility — download and commit a specific version instead.

**Current approach:** We use the `-latest` JSON URL for Phoenix 6
(`Phoenix6-frc2026-latest.json`) which always resolves to the newest
stable release. For competition-critical builds, consider pinning
to a specific version.

---

## Adding a New Vendordep

1. Find the vendor's `jsonUrl` from their documentation
2. Download the JSON file to `vendordeps/`
3. Run `./gradlew build` to verify resolution
4. Add the dependency to the table at the top of this document
5. Update CLAUDE.md reference documentation if the vendor has docs

---

## Conflict Resolution

Vendordep JSONs include a `conflictsWith` array. For example,
Phoenix 6 conflicts with the Phoenix 6 replay vendordep — you
cannot have both in the same project.

If you see a conflict error during build:
1. Check which vendordeps conflict (the error message names them)
2. Remove the conflicting JSON from `vendordeps/`
3. If you need both libraries, check the vendor's docs for a
   combined vendordep
