package com.team271.lib.hardware.sensors.switches;

import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;

public abstract class SwitchBase extends TObj {
    public enum SwitchType {
        FX,
        CANCODER,
        MAX,
        RIO_DIO
    }

    public enum SwitchTrigger {
        NO,
        NC
    }

    /*
     * Switch
     */
    protected final SwitchType type;

    protected boolean autoSet = false;
    protected double autoSetPos = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntAutoSet = new NTEntry(table, "Auto Set Enabled", false);
    final NTEntry ntAutoSetPos = new NTEntry(table, "Auto Set Pos", 0.0f);
    final NTEntry ntTriggered = new NTEntry(table, "Triggered", false);

    /*
     *
     * Constructors
     *
     */
    protected SwitchBase(final TObj argParent, final String argName, final SwitchType argSwitchType) {
        super(argParent, "(Switch)" + argName);

        type = argSwitchType;
    }

    /*
     *
     * Switch
     *
     */
    protected abstract void create();

    public abstract void setEnabled(final boolean argEnabled);

    public abstract void setTriggerType(final SwitchTrigger argTriggerType);

    public void setAutoSet(final boolean argAutoSet) {
        autoSet = argAutoSet;
    }

    public boolean getAutoSet() {
        return autoSet;
    }

    public void setAutoSetPos(final double argAutoSetPos) {
        autoSetPos = argAutoSetPos;
    }

    public double getAutoSetPos() {
        return autoSetPos;
    }

    public boolean getTriggered() {
        return false;
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        ntAutoSet.publish(autoSet);
        ntAutoSetPos.publish(autoSetPos);

        ntTriggered.publish(getTriggered());
    }
}
