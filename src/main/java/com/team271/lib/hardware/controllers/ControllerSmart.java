package com.team271.lib.hardware.controllers;

import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.LoggedNTInput;
import com.team271.lib.nt.NTEntry;

public abstract class ControllerSmart extends ControllerBase {
    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntCurrentLimitStatorEnable =
            new NTEntry(table, "Current Limit Stator Enable", false);
    final NTEntry ntCurrentLimitStator = new NTEntry(table, "Current Limit Stator", 0);

    final NTEntry ntCurrentLimitSupplyEnable =
            new NTEntry(table, "Current Limit Supply Enable", false);
    final NTEntry ntCurrentLimitSupply = new NTEntry(table, "Current Limit Supply", 0);
    final NTEntry ntCurrentLimitSupplyTime = new NTEntry(table, "Current Limit Supply Time", 0);
    final NTEntry ntCurrentLimitSupplyLowerLimit =
            new NTEntry(table, "Current Limit Supply Lower Limit", 0);

    final NTEntry ntVoltagePeakFwd = new NTEntry(table, "Voltage Peak Fwd", 0);
    final NTEntry ntVoltagePeakRev = new NTEntry(table, "Voltage Peak Rev", 0);
    final NTEntry ntVoltagePeakTime = new NTEntry(table, "Voltage Peak Time", 0);

    final NTEntry ntRampOpenLoopDuty = new NTEntry(table, "Ramp Open Loop Duty", 0);
    final NTEntry ntRampOpenLoopVoltage = new NTEntry(table, "Ramp Open Loop Voltage", 0);

    final NTEntry ntRampClosedLoopDuty = new NTEntry(table, "Ramp Closed Loop Duty", 0);
    final NTEntry ntRampClosedLoopVoltage = new NTEntry(table, "Ramp Closed Loop Voltage", 0);

    final NTEntry ntCLError = new NTEntry(table, "CL Error", 0);
    final NTEntry ntCLOutput = new NTEntry(table, "CL Output", 0);

    /*
     * Tuning Inputs (LoggedNTInput)
     */
    private final LoggedNTInput tuneStatorEnable;
    private final LoggedNTInput tuneStatorLimit;
    private final LoggedNTInput tuneSupplyEnable;
    private final LoggedNTInput tuneSupplyLimit;
    private final LoggedNTInput tuneVoltagePeakFwd;
    private final LoggedNTInput tuneVoltagePeakRev;

    /*
     *
     * Constructors
     *
     */
    protected ControllerSmart(
            final TObj argParent,
            final String argName,
            final ControllerType argControllerType,
            final CANDeviceID argID,
            final MotorBase argMotor) {
        super(argParent, "(Smart)" + argName, argControllerType, argID, argMotor);

        tuneStatorEnable = new LoggedNTInput(table, "Tune Stator Enable", false);
        tuneStatorLimit = new LoggedNTInput(table, "Tune Stator Limit", 40.0);
        tuneSupplyEnable = new LoggedNTInput(table, "Tune Supply Enable", false);
        tuneSupplyLimit = new LoggedNTInput(table, "Tune Supply Limit", 30.0);
        tuneVoltagePeakFwd = new LoggedNTInput(table, "Tune Voltage Peak Fwd", 16.0);
        tuneVoltagePeakRev = new LoggedNTInput(table, "Tune Voltage Peak Rev", -16.0);
    }

    /*
     *
     * Config
     *
     */
    /* Current Limit - Stator */
    public abstract void setCurrentLimitStator(
            final boolean argEnable, final double argStatorCurrent);

    public abstract boolean getCurrentLimitStatorEnable();

    public abstract double getCurrentLimitStator();

    /* Current Limit - Supply */
    public abstract void setCurrentLimitSupply(
            final boolean argEnable, final double argSupplyCurrent);

    public abstract void setCurrentLimitSupply(
            final double argSupplyCurrentLimit,
            final double argTime,
            final double argSupplyCurrentLowerLimit);

    public abstract boolean getCurrentLimitSupplyEnable();

    public abstract double getCurrentLimitSupply();

    public abstract double getCurrentLimitSupplyTime();

    public abstract double getCurrentLimitSupplyLowerLimit();

    /* Voltage Limit */
    public abstract void setVoltagePeak(
            final double argFwdVoltage, final double argRevVoltage, final double argTimeFilter);

    public abstract double getVoltagePeakFwd();

    public abstract double getVoltagePeakRev();

    public abstract double getVoltagePeakTime();

    /*
     *
     * Config - Open Loop
     *
     */
    public abstract void setRampOpenLoopDuty(final double argRampRateSec);

    public abstract double getRampOpenLoopDuty();

    public abstract void setRampOpenLoopVoltage(final double argRampRateSec);

    public abstract double getRampOpenLoopVoltage();

    /*
     *
     * Config - Closed Loop
     *
     */
    public abstract void setRampClosedLoopDuty(final double argRampRateSec);

    public abstract double getRampClosedLoopDuty();

    public abstract void setRampClosedLoopVoltage(final double argRampRateSec);

    public abstract double getRampClosedLoopVoltage();

    public abstract void setTolerance(final double argTolerance);

    public abstract double getTolerance();

    /* PID Values */
    public abstract void setPSlot(final int argSlot, final double argSetP);

    public abstract double getPSlot(final int argSlot);

    public abstract void setISlot(final int argSlot, final double argSetI);

    public abstract double getISlot(final int argSlot);

    public abstract void setDSlot(final int argSlot, final double argSetD);

    public abstract double getDSlot(final int argSlot);

    public abstract void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS);

    /*
     * Closed Loop (CL)
     */
    public abstract double getCLError();

    public abstract double getCLOutput();

    public abstract void setOutputPosition(final double argPositionRot, final double argFFVolt);

    public abstract void setOutputVelocity(final double argRPS, final double argFFVolt);

    /*
     *
     * Outputs
     *
     */

    /*
     *
     * Tuning
     *
     */
    protected void checkTuning() {
        if (tuneStatorEnable.hasBoolChanged() || tuneStatorLimit.hasChanged()) {
            setCurrentLimitStator(tuneStatorEnable.getBool(), tuneStatorLimit.getDbl());
        }
        if (tuneSupplyEnable.hasBoolChanged() || tuneSupplyLimit.hasChanged()) {
            setCurrentLimitSupply(tuneSupplyEnable.getBool(), tuneSupplyLimit.getDbl());
        }
        if (tuneVoltagePeakFwd.hasChanged() || tuneVoltagePeakRev.hasChanged()) {
            setVoltagePeak(tuneVoltagePeakFwd.getDbl(), tuneVoltagePeakRev.getDbl(), 0.0);
        }
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        checkTuning();

        super.outputTelemetry();

        ntCurrentLimitStatorEnable.publish(getCurrentLimitStatorEnable());
        ntCurrentLimitStator.publish(getCurrentLimitStator());

        ntCurrentLimitSupplyEnable.publish(getCurrentLimitSupplyEnable());
        ntCurrentLimitSupply.publish(getCurrentLimitSupply());
        ntCurrentLimitSupplyTime.publish(getCurrentLimitSupplyTime());
        ntCurrentLimitSupplyLowerLimit.publish(getCurrentLimitSupplyLowerLimit());

        ntVoltagePeakFwd.publish(getVoltagePeakFwd());
        ntVoltagePeakRev.publish(getVoltagePeakRev());
        ntVoltagePeakTime.publish(getVoltagePeakTime());

        ntRampOpenLoopDuty.publish(getRampOpenLoopDuty());
        ntRampOpenLoopVoltage.publish(getRampOpenLoopVoltage());

        ntRampClosedLoopDuty.publish(getRampClosedLoopDuty());
        ntRampClosedLoopVoltage.publish(getRampClosedLoopVoltage());

        ntCLError.publish(getCLError());
        ntCLOutput.publish(getCLOutput());
    }
}
