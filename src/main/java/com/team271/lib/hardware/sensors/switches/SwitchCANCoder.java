package com.team271.lib.hardware.sensors.switches;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.signals.ForwardLimitTypeValue;
import com.ctre.phoenix6.signals.ForwardLimitValue;
import com.ctre.phoenix6.signals.ReverseLimitTypeValue;
import com.ctre.phoenix6.signals.ReverseLimitValue;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.util.Util;

public class SwitchCANCoder extends SwitchBase {
    protected static final int RETRY_COUNT_CAN = 5;

    /*
     * TalonFX
     */
    protected final ControllerTalonFX controller;
    protected double updateFreqHz = 250.0;
    protected StatusCode fxStatus = StatusCode.OK;

    protected final boolean isFwdLimit;

    protected final SwitchTrigger swTrigger;
    protected final boolean autoZero;
    protected final double autoZeroValueIn;

    protected StatusSignal<ForwardLimitValue> swFwd;
    protected StatusSignal<ReverseLimitValue> swRev;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntFXStatus = new NTEntry(table, "CANCoder Status", "");

    /*
     *
     * Constructors
     *
     */
    public SwitchCANCoder(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final boolean argIsFwdLimit,
            final SwitchTrigger argSwitchTrigger,
            final boolean argAutoZero,
            final double argAutoZeroValueInches,
            final double argUpdateFreqHz) {
        super(argParent, "(CANCoder)" + argName, SwitchType.CANCODER);

        controller = argTalonFX;

        isFwdLimit = argIsFwdLimit;
        swTrigger = argSwitchTrigger;
        autoZero = argAutoZero;
        autoZeroValueIn = argAutoZeroValueInches;

        updateFreqHz = argUpdateFreqHz;
    }

    /*
     *
     * Switch
     *
     */
    protected void create() {
        setTriggerType(swTrigger);

        setAutoSet(autoZero);
        setAutoSetPos(autoZeroValueIn);

        setEnabled(true);
    }

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        if (isFwdLimit) {
            /*
             * Get Switch Object
             */
            swFwd = controller.getTalonFX().getForwardLimit();
            CTREManager.addSignalTalonFX(swFwd, updateFreqHz);

            swRev = null;
        } else {
            /*
             * Get Switch Object
             */
            swRev = controller.getTalonFX().getReverseLimit();
            CTREManager.addSignalTalonFX(swRev, updateFreqHz);

            swFwd = null;
        }
    }

    public void setEnabled(final boolean argEnabled) {
        if (isFwdLimit) {
            controller.getConfig().HardwareLimitSwitch.ForwardLimitEnable = argEnabled;
        } else {
            controller.getConfig().HardwareLimitSwitch.ReverseLimitEnable = argEnabled;
        }
    }

    public void setTriggerType(final SwitchTrigger argTriggerType) {
        if (isFwdLimit) {
            if (argTriggerType == SwitchTrigger.NC) {
                controller.getConfig().HardwareLimitSwitch.ForwardLimitType = ForwardLimitTypeValue.NormallyClosed;
            } else {
                controller.getConfig().HardwareLimitSwitch.ForwardLimitType = ForwardLimitTypeValue.NormallyOpen;
            }
        } else {
            if (argTriggerType == SwitchTrigger.NC) {
                controller.getConfig().HardwareLimitSwitch.ReverseLimitType = ReverseLimitTypeValue.NormallyClosed;
            } else {
                controller.getConfig().HardwareLimitSwitch.ReverseLimitType = ReverseLimitTypeValue.NormallyOpen;
            }
        }
    }

    @Override
    public void setAutoSet(final boolean argAutoSet) {
        super.setAutoSet(argAutoSet);

        if (isFwdLimit) {
            controller.getConfig().HardwareLimitSwitch.ForwardLimitAutosetPositionEnable = autoSet;
        } else {
            controller.getConfig().HardwareLimitSwitch.ReverseLimitAutosetPositionEnable = autoSet;
        }
    }

    @Override
    public void setAutoSetPos(final double argAutoSetPos) {
        super.setAutoSetPos(argAutoSetPos);

        if (isFwdLimit) {
            controller.getConfig().HardwareLimitSwitch.ForwardLimitAutosetPositionValue = autoSetPos;
        } else {
            controller.getConfig().HardwareLimitSwitch.ReverseLimitAutosetPositionValue = autoSetPos;
        }
    }

    @Override
    public boolean getTriggered() {
        if (isFwdLimit) {
            if (swFwd != null && swFwd.getStatus().isOK()) {
                return Util.epsilonEquals(swFwd.getValueAsDouble(), 1.0);
            }
        } else {
            if (swRev != null && swRev.getStatus().isOK()) {
                return Util.epsilonEquals(swRev.getValueAsDouble(), 1.0);
            }
        }

        return false;
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntFXStatus.publish(fxStatus.getDescription());
    }
}
