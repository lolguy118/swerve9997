<!-- Part of the Team 271 Software Coding Standard.
     See Team271-Software-Coding-Standard.md for the index. -->

## Appendix H: CTRE Phoenix 6 Usage Patterns

### Timesync

All CTRE control requests **shall** use timesync to synchronize
command execution with the CANivore clock:

```java
.withUseTimesync(true).withUpdateFreqHz(0)
```

`UpdateFreqHz(0)` means "send immediately, don't auto-repeat."
The subsystem code is responsible for sending requests each cycle.

### Bulk Signal Refresh

Use `CTREManager.refreshAll()` at the start of each cycle to refresh
all registered CAN signals in a single bulk operation. This is more
efficient than refreshing signals individually.

### Control Request Reuse

Store control request objects as fields and reuse them:

```java
private final VoltageOut mVoltageRequest =
    new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0);

/* In robotPeriodicAfter: */
mMotor.setControl(mVoltageRequest.withOutput(voltage));
```

### Status Code Checking

Check `StatusCode` returns from configuration methods:

```java
StatusCode status = mMotor.getConfigurator().apply(config);
if (!status.isOK()) {
    DriverStation.reportError("Motor config failed: " + status, false);
}
```

---
