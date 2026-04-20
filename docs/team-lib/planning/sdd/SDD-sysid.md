# SDD: `com.team271.lib.sysid` — System Identification Data Capture

| Field | Value |
| ----- | ----- |
| Document No. | TBD-SDD-SYSID |
| Revision | 0.1 |
| Date | 2026-04-20 |
| Status | Draft |
| Requirements Traced | `[SID-001]`..`[SID-004]` (SRS §4.7) |

The normative keywords SHALL, SHOULD, and MAY follow the convention
defined in
[`../../../common/planning/README.md`](../../../common/planning/README.md#normative-keywords).

## 1. Purpose

Provides data capture infrastructure for system identification (SysID)
characterization. Logs motor voltage, velocity, and position at high
frequency into AdvantageKit-compatible format for post-processing.

## 2. Scope and Boundaries

This SDD covers:

- `Logger` — single-mechanism SysID data logger
- `LoggerGeneral` — multi-mechanism or general-purpose SysID logger
- Pre-allocated data vectors to avoid GC pressure during characterization
- Thread priority configuration for timing consistency
- Quasistatic and dynamic test mode routing

## 3. Module Decomposition

### 3.1 `Logger`

Single-mechanism SysID data capture. Owns a fixed-size pre-allocated
vector sized to cover the full characterization window at the
configured sample rate. Starting the logger resets the write index
and begins sampling; every periodic call appends a tuple of
(timestamp, voltage, position, velocity) until the vector is full
or sampling is stopped. `Logger` records samples via AdvantageKit
so the captured data appears in the WPILib data log, which the
SysID tool can read directly.

### 3.2 `LoggerGeneral`

Generalized SysID logger supporting multiple mechanisms or arbitrary
signals per sample. Same pre-allocated vector strategy as `Logger`
but with a configurable column set. Used when characterizing a
mechanism with more than one motor or when capturing auxiliary
signals (current, setpoint error) alongside the SysID inputs.

### 3.3 Memory Discipline

Both loggers allocate their vectors at robot-init time so the
garbage collector never runs during sampling. Vector size and
sample rate are defined as constants in the robot project; the
library does not dictate numeric values.

## 4. Data Flow

```text
// Characterization run
SysID test command (robot project)
  → Logger.start()                                (t = 0)
  → autonomousPeriodic / teleopPeriodic
    → Logger.sample(timestamp, voltage, pos, vel) (at kSysIdSampleRate)
      → append to pre-allocated vector
  → Logger.stop()                                 (end of run)
    → AdvantageKit Logger.recordOutput(...)       (flush to WPILog)
  → robot shutdown / disable

// Post-run analysis (off-robot)
AdvantageScope: open .wpilog
  → export CSV / SysID JSON
  → WPILib SysID tool: compute kS, kV, kA, kP, kD
```

## 5. Key Design Decisions

| Decision | Rationale | Reference |
| -------- | --------- | --------- |
| Pre-allocated vectors | Avoid GC pressure during multi-second characterization | SRS §4.7 |
| Elevated thread priority (below HAL) | Consistent sample timing without starving HAL | [docs/guides/sysid-workflow.md](../../guides/sysid-workflow.md) |
| AdvantageKit output format | Compatible with post-match log replay and SysID tool | [ADR-012](../adr/ADR-012-advantagekit-logging.md) |

## 6. Error Handling

If a sample arrives after the pre-allocated vector is full, the
logger stops recording and posts an `Alert.WARNING` so the operator
sees the truncation on the dashboard. Existing captured data remains
valid — the logger does not discard or overwrite earlier samples.

Timestamps are read from `Timer.getFPGATimestamp()`; if the FPGA
timer is unavailable (unlikely in sim or on robot), sample timestamps
degrade to zero but the logger does not throw.

The logger does not validate the captured voltage/position/velocity
values — NaN or Infinity will propagate into the log and must be
filtered during analysis. This is deliberate: a running
characterization should not be aborted on a single bad sample.

## 7. Platform Portability Notes

The logger compiles and executes in desktop simulation, but real
characterization data requires a robot with repeatable physics.
Simulation-captured SysID data reflects only the accuracy of the
WPILib physics model plus any CTRE firmware simulation — values
identified in sim are not guaranteed to match hardware.

Thread priority elevation (requested via the WPILib native API) is
a no-op on desktop simulation and has no adverse effect.

## 8. Configuration

- **Vector size** — constant defined in the robot project; sized as
  `sampleRateHz * maxRunDurationSec` with headroom for the stop
  transition.
- **Sample rate** — typically matches the robot periodic rate or a
  higher rate driven by a dedicated notifier. Configured in the
  robot project via a named constant.
- **Thread priority** — configurable constant in the logger class;
  raised above the default task priority but held below the HAL
  priority so hardware callbacks remain responsive.
- **Output key prefix** — per-logger string passed at construction so
  multiple loggers (e.g., per mechanism) write to distinct log
  channels.

## 9. Test Coverage Requirements

| Area | HAL Required | Notes |
| ---- | ------------ | ----- |
| Vector allocation and overflow | No | Construct a `Logger`; push more samples than capacity; verify alert |
| `start/stop` bookkeeping | No | Verify index reset, active flag transitions |
| AdvantageKit output wiring | Yes | `HAL.initialize(500, 0)` required; verify recorded keys |

Test IDs: `[TEST-SID-NNN]`. Characterization accuracy is validated
on hardware, not in unit tests.
