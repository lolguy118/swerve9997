# Passthrough Design — Wrapper, Not Wall

Team271-Lib wraps CTRE Phoenix 6, WPILib, and other vendor libraries to add lifecycle management, multi-motor coordination, telemetry, fault monitoring, and gear ratio conversion. The library is **additive** — it never blocks access to the underlying hardware objects.

## Principle

> Every hardware wrapper class exposes its underlying vendor object via a public getter.
> The library provides convenience methods for common operations, but users can always reach through to the raw object for advanced or vendor-specific features.

This means:

- When CTRE releases a new Phoenix 6 feature, you can use it immediately via the passthrough getter — no library update required.
- When WPILib adds a new PID feature, you access it through the underlying controller.
- The library's convenience API and the vendor's native API coexist. Use whichever is appropriate.

## What the Library Adds (Why We Wrap)

| Concern | What the library provides | Example |
|---------|--------------------------|---------|
| **Lifecycle** | `robotInit()`, `robotPeriodicBefore()`, `robotPeriodicAfter()`, `outputTelemetry()` hooks called in deterministic order | SubsystemManager orchestrates all subsystems each cycle |
| **Multi-motor coordination** | Apply config, direction, neutral mode, current limits to all motors in a transmission | `TransmissionBase.configCurrentLimitStator()` applies to leader + all followers |
| **Centralized CAN refresh** | Single bulk `refreshAll()` call with consistent timestamps | CTREManager batches all StatusSignal reads into one CAN frame |
| **Fault monitoring** | Automatic sticky fault tracking with Alert + Elastic notifications | FaultMonitor watches boot-during-enable, device temp, hardware faults |
| **Telemetry** | Structured NT publishing + AdvantageKit logging for every hardware object | NTEntry + LoggedNTInput with change detection |
| **Gear ratio conversion** | Rotor → mechanism → output unit conversions for position and velocity | GearRatio immutable value object with bidirectional conversion |
| **Encoder abstraction** | Polymorphic access to FX internal or CANCoder external encoders | EncoderAdapter eliminates per-type conditional logic |
| **Input validation** | NaN/Infinity guards on closed-loop control commands | `hasInvalidInput()` prevents sending corrupt setpoints to hardware |
| **Timesync coordination** | Consistent timesync configuration across all control requests in a transmission | Array-based loop updates all pre-allocated control requests |

## What the Library Never Hides

The underlying vendor object is always accessible. You can call any vendor method directly.

## Passthrough Getter Reference

### Motor Controllers

| Wrapper Class | Getter | Returns | Notes |
|--------------|--------|---------|-------|
| `ControllerTalonFX` | `getTalonFX()` | `com.ctre.phoenix6.hardware.TalonFX` | Raw CTRE motor controller |
| `ControllerTalonFX` | `getConfig()` | `TalonFXConfiguration` | Deferred config object — modify fields, then call `applyConfig()` |
| `ControllerTalonFX` | `getSimState()` | `TalonFXSimState` | Simulation proxy |
| `ControllerTalonFX` | `getConfigMM()` | `MotionMagicConfigs` | Shortcut into config.MotionMagic |

### Transmissions

| Wrapper Class | Getter | Returns | Notes |
|--------------|--------|---------|-------|
| `TransmissionFX` | `getLeader()` | `TalonFX` | Leader motor's raw CTRE object |
| `TransmissionFX` | `getLeaderConfig()` | `TalonFXConfiguration` | Leader's deferred config |
| `TransmissionFX` | `getLeaderController()` | `ControllerTalonFX` | Leader's library wrapper (for multi-motor access) |
| `TransmissionFX` | `getSimState()` | `TalonFXSimState` | Leader's simulation proxy |
| `TransmissionFX` | `getAllControllers()` | `Set<ControllerSmart>` | All motors (leader + followers) |

### Encoders

| Wrapper Class | Getter | Returns | Notes |
|--------------|--------|---------|-------|
| `EncoderCANCoder` | `getCANcoder()` | `com.ctre.phoenix6.hardware.CANcoder` | Raw CTRE CANcoder device |
| `EncoderCANCoder` | `getConfig()` | `CANcoderConfiguration` | Deferred config object |
| `EncoderCANCoder` | `getSimState()` | `CANcoderSimState` | Simulation proxy |
| `EncoderFX` | *(accessed via controller)* | — | FX encoder signals come from the TalonFX itself |

### IMU

| Wrapper Class | Getter | Returns | Notes |
|--------------|--------|---------|-------|
| `IMUPigeon2` | `getPigeon2()` | `com.ctre.phoenix6.hardware.Pigeon2` | Raw CTRE Pigeon2 IMU |
| `IMUPigeon2` | `getConfig()` | `Pigeon2Configuration` | Deferred config object |
| `IMUPigeon2` | `getSimState()` | `Pigeon2SimState` | Simulation proxy |

### Range Sensors

| Wrapper Class | Getter | Returns | Notes |
|--------------|--------|---------|-------|
| `RangeCANrange` | `getCANrange()` | `com.ctre.phoenix6.hardware.CANrange` | Raw CTRE CANrange sensor |
| `RangeCANrange` | `getConfig()` | `CANrangeConfiguration` | Deferred config object |

### PID Controllers (WPILib)

| Wrapper Class | Getter | Returns | Notes |
|--------------|--------|---------|-------|
| `PIDWPI` | `getController()` | `edu.wpi.first.math.controller.PIDController` | Raw WPILib PID |
| `PIDWPI_Trap` | `getController()` | `edu.wpi.first.math.controller.ProfiledPIDController` | Raw WPILib profiled PID |

## Usage Examples

### Common operation — use the library convenience API

```java
// Library handles multi-motor coordination and applies to all motors
transmission.configCurrentLimitStator(true, 80);
transmission.configDirection(MotorDirection.CW);
transmission.setNeutralMode(NeutralState.BRAKE);
transmission.applyConfigs();

// Library handles gear ratio conversion for closed-loop
transmission.setOutputPosition(targetDegrees, feedforwardVolts);
```

### Advanced operation — reach through to CTRE directly

```java
// Access the raw TalonFXConfiguration for fields the library doesn't wrap
var config = transmission.getLeaderConfig();
config.Audio.BeepOnBoot = false;
config.Audio.BeepOnConfig = false;
config.SoftwareLimitSwitch.ForwardSoftLimitEnable = true;
config.SoftwareLimitSwitch.ForwardSoftLimitThreshold = 10.5;
transmission.applyConfigs();  // still use library's multi-retry apply

// Use a CTRE control mode the library doesn't have a convenience method for
var req = new Follower(leaderID, false);
transmission.getLeader().setControl(req);

// Access a CTRE signal not registered through the library
var temp = transmission.getLeader().getDeviceTemp();
temp.setUpdateFrequency(10);  // manual signal management
double deviceTemp = temp.getValueAsDouble();
```

### WPILib PID — access advanced features

```java
// Use WPILib's continuous input wrapping directly
pidController.getController().enableContinuousInput(-180, 180);

// Access WPILib's IZone directly
pidController.getController().setIZone(5.0);
```

## When to Use Which

| Situation | Approach |
|-----------|----------|
| Setting current limits, direction, neutral mode | Library convenience — applies to all motors |
| Position/velocity closed-loop with gear ratios | Library convenience — handles unit conversion |
| Motion Magic with standard profiles | Library convenience — pre-configured timesync |
| Configuring audio, software limits, or other TalonFX-specific settings | Direct config access via `getLeaderConfig()` |
| Using a brand-new CTRE control mode before the library adds a wrapper | Direct `getLeader().setControl(request)` |
| Reading a signal not registered through CTREManager | Direct signal access via `getTalonFX().getSignalName()` |
| PID features beyond what PIDBase exposes | Direct access via `getController()` |

## Design Rule for Library Contributors

When adding a new hardware wrapper class:

1. Store the vendor hardware object as a `protected` field
2. Provide a `public` getter returning the raw vendor type
3. If the class has a configuration object, provide a `getConfig()` getter
4. If the class has simulation state, provide a `getSimState()` getter
5. Document the getters in this file's reference table
