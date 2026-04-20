# ADR-006: CTRE Phoenix 6 as the Primary Motor/Sensor Vendor

## Status

Accepted

## Date

2026-04-20

## Context

FRC teams have multiple motor controller and sensor vendor options:
CTRE (TalonFX, TalonFXS, CANcoder, Pigeon 2, CANrange), REV Robotics
(SparkMax, SparkFlex, Through-Bore Encoder), WPILib's PWM-based
controllers, and specialty vendors (Kauai Labs NavX, etc.).

Team 271 standardized on CTRE hardware several seasons ago based on
CAN bus reliability, Phoenix Tuner X for diagnostics, on-motor closed-
loop control (PIDFX), FOC support in Phoenix 6, and the quality of
the Phoenix 6 Java API. Every motor and rotational sensor on the
robot is a CTRE device.

## Decision

Team271-Lib is CTRE-focused. The only supported motor controllers
and rotational sensors are CTRE Phoenix 6 devices:

- **Motors:** TalonFX (Kraken X60 / Kraken X44 / Falcon 500), TalonFXS
- **Encoders:** CANcoder (absolute + relative), FX internal rotor
- **IMU:** Pigeon 2
- **Range sensor:** CANrange
- **Limit switches:** CANdi, digital input via TalonFX hardware limit pins

WPILib PWM motor controllers (`PWMSparkMax`, `PWMVictorSPX`,
`PWMTalonFX`, etc.), REV Robotics motor controllers (CAN-based
SparkMax, SparkFlex), and Kauai Labs NavX are not wrapped by the
library. They can be used directly via WPILib APIs in robot projects
that need them; the library does not abstract them.

## Rationale

1. **Team inventory.** Every FRC electronics kit 271 builds uses CTRE
   devices. Wrapping a vendor we do not use is speculation.
2. **Phoenix 6 feature breadth.** FOC, MotionMagic, timesync, status
   signal registration, and bulk refresh are CTRE-specific features
   the library leans on heavily (see
   [ADR-007](ADR-007-centralized-can-refresh.md)).
3. **Passthrough coverage.** Every CTRE device has a passthrough
   getter (ADR-003), so unusual features work without library
   extension.
4. **api/ still present.** The vendor-neutral `api/` layer exists so a
   future REV or WPILib implementation could slot in without rewriting
   upper layers. We just don't build one speculatively.

## Consequences

**Easier:**

- Library design assumes Phoenix 6 semantics (StatusSignal, unit
  conventions, timesync).
- CTRE releases can be adopted quickly — the vendor is a single
  tightly-scoped surface.
- Diagnostics through Phoenix Tuner X are first-class.

**Harder:**

- Teams that share our library must also use CTRE hardware.
- A future hardware change (e.g., REV migration) requires implementing
  the `api/` interfaces for the new vendor.
- Mixed-vendor robots must use WPILib APIs directly for non-CTRE
  devices.

## Alternatives Considered

- **Build speculative WPILib PWM wrappers.** Rejected — adds
  maintenance burden for code we never run.
- **Build speculative REV wrappers.** Rejected — same reason.
- **Remove the api/ vendor-neutral layer entirely.** Rejected — the
  small cost of maintaining the neutral interfaces is worth the
  optionality; they also serve testing (a mock `Motor` is a test
  double for `CTREMotor`).

## References

- [SDD-vendor-ctre.md](../sdd/SDD-vendor-ctre.md)
- [SDD-api.md](../sdd/SDD-api.md)
- [SCMP.md §4](../SCMP.md)
- [.claude/rules/team271-lib.md](../../../../.claude/rules/team271-lib.md)
- [ADR-003](ADR-003-passthrough-wrapper-not-wall.md)
