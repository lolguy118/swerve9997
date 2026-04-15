package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.nt.NTEntry;

/**
 * PID controller that delegates to the TalonFX motor controller's onboard PID.
 *
 * <p>Unlike software PID variants (PIDSimple, PIDWPI), the closed-loop calculation runs on the
 * TalonFX hardware at 1 kHz. This class sends position/velocity goals to the controller and reads
 * back closed-loop error and output for telemetry and {@link #atSetpoint()} checks.
 */
public class PIDFX extends PIDBase {
    protected final ControllerTalonFX controller;
    protected final int slot;

    protected double goal = 0.0;
    protected double feedForward = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntGoal = new NTEntry(table, "Goal", 0.0);
    final NTEntry ntFeedForward = new NTEntry(table, "Feed Forward", 0.0);

    /*
     *
     * Constructors
     *
     */
    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argP,
            final double argI,
            final double argD,
            final double argTol,
            final int argSlot) {
        super(argParent, "(TALONFX)" + argName, PIDType.TALONFX, argP, argI, argD, argTol);

        controller = argTalonFX;
        slot = argSlot;

        controller.setPIDFSlot(slot, argP, argI, argD, 0.0, 0.0);

        reset();
    }

    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argP,
            final double argI,
            final double argD,
            final double argTol) {
        this(argParent, argName, argTalonFX, argP, argI, argD, argTol, 0);
    }

    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argP,
            final double argI,
            final double argD) {
        this(argParent, argName, argTalonFX, argP, argI, argD, 0.0, 0);
    }

    public PIDFX(final TObj argParent, final String argName, final ControllerTalonFX argTalonFX) {
        this(argParent, argName, argTalonFX, 0.0, 0.0, 0.0, 0.0, 0);
    }

    /*
     *
     * PID
     *
     */
    @Override
    public void setP(final double argP) {
        super.setP(argP);
        controller.setPSlot(slot, argP);
    }

    @Override
    public void setI(final double argI) {
        super.setI(argI);
        controller.setISlot(slot, argI);
    }

    @Override
    public void setD(final double argD) {
        super.setD(argD);
        controller.setDSlot(slot, argD);
    }

    @Override
    public void enableContinuousInput(final double argMinInput, final double argMaxInput) {
        super.enableContinuousInput(argMinInput, argMaxInput);
        controller.setContinuousWrap(true);
    }

    @Override
    public void disableContinuousInput() {
        super.disableContinuousInput();
        controller.setContinuousWrap(false);
    }

    /** Returns the PID slot index used by this controller. */
    public int getSlot() {
        return slot;
    }

    /**
     * Sends a position goal to the TalonFX closed-loop controller.
     *
     * @param argGoalPosition Target position in rotations
     */
    public void setGoal(final double argGoalPosition) {
        goal = argGoalPosition;
        controller.setOutputPosition(goal, slot, feedForward);
    }

    /**
     * Sends a position goal with feed-forward voltage to the TalonFX closed-loop controller.
     *
     * @param argGoalPosition Target position in rotations
     * @param argFeedForward Feed-forward voltage
     */
    public void setGoal(final double argGoalPosition, final double argFeedForward) {
        goal = argGoalPosition;
        feedForward = argFeedForward;
        controller.setOutputPosition(goal, slot, feedForward);
    }

    /*
     *
     * Calculate
     *
     */

    /**
     * Reads closed-loop error and output from the TalonFX hardware PID. Unlike software PID
     * variants, no calculation is performed here — the TalonFX runs PID at 1 kHz onboard.
     *
     * <p>This method populates {@code posError} and {@code output} so that {@link #atSetpoint()}
     * and telemetry work correctly.
     *
     * @param argInputMeasurement Current position measurement (for lastInputMeasurement tracking)
     * @param argSetpoint The setpoint (stored but not used — goal is sent via setGoal())
     * @param argTimestamp Current timestamp
     * @return The closed-loop output from the TalonFX
     */
    @Override
    public double calc(
            final double argInputMeasurement, final double argSetpoint, final double argTimestamp) {
        lastInputMeasurement = argInputMeasurement;
        lastTimestamp = argTimestamp;

        prevError = posError;
        posError = controller.getCLError();
        output = controller.getCLOutput();

        return output;
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntGoal.publish(goal);
        ntFeedForward.publish(feedForward);
    }
}
