package com.team271.lib.vendor.ctre;

import com.team271.lib.api.sensor.LimitSwitch;
import com.team271.lib.hardware.sensors.switches.SwitchBase;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;
import com.team271.lib.nt.NTTable;

/**
 * CTRE limit switch (TalonFX hardware input or CANCoder-based) exposed through the vendor-neutral
 * {@link LimitSwitch} interface.
 *
 * <p>Wraps {@link SwitchBase} implementations (SwitchFX, SwitchCANCoder).
 */
public class CTRELimitSwitch implements LimitSwitch {

    /*
     * Switch
     */
    private final SwitchBase mSwitch;

    /*
     * Constructor
     */
    public CTRELimitSwitch(final SwitchBase argSwitch) {
        mSwitch = argSwitch;
    }

    /*
     * Passthrough
     */

    /** Returns the underlying SwitchBase for passthrough access. */
    public SwitchBase getSwitch() {
        return mSwitch;
    }

    /*
     *
     * LimitSwitch Interface
     *
     */

    @Override
    public boolean isTriggered() {
        return mSwitch.getTriggered();
    }

    @Override
    public void setEnabled(final boolean argEnabled) {
        mSwitch.setEnabled(argEnabled);
    }

    @Override
    public void setTriggerType(final SwitchTrigger argType) {
        mSwitch.setTriggerType(argType);
    }

    /*
     *
     * Lifecycle
     *
     */

    @Override
    public void robotInit(final double argTimestamp) {
        mSwitch.robotInit(argTimestamp);
    }

    @Override
    public void outputTelemetry() {
        mSwitch.outputTelemetry();
    }

    /*
     *
     * Named
     *
     */

    @Override
    public String getName() {
        return mSwitch.getName();
    }

    @Override
    public NTTable getTable() {
        return mSwitch.getTable();
    }

    @Override
    public String logKey(final String argSuffix) {
        return mSwitch.logKey(argSuffix);
    }
}
