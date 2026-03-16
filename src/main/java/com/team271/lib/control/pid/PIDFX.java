package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;

/**
 * PID controller that delegates to the TalonFX motor controller's onboard PID.
 *
 * <p>Unlike software PID variants (PIDSimple, PIDWPI), the closed-loop calculation runs on the
 * TalonFX hardware at 1 kHz. This class sends position/velocity goals to the controller and reads
 * back closed-loop error and output for telemetry and {@link #atSetpoint()} checks.
 */
public class PIDFX extends PIDBase {
    protected final ControllerTalonFX controller;

    protected double goal = 0.0;
    protected double feedForward = 0.0;

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
            final double argTol) {
        super(argParent, "(TALONFX)" + argName, PIDType.TALONFX, argP, argI, argD, argTol);

        controller = argTalonFX;

        controller.setPIDFSlot(0, argP, argI, argD, 0.0, 0.0);

        reset();
    }

    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argP,
            final double argI,
            final double argD) {
        this(argParent, argName, argTalonFX, argP, argI, argD, 0.0);
    }

    public PIDFX(final TObj argParent, final String argName, final ControllerTalonFX argTalonFX) {
        this(argParent, argName, argTalonFX, 0.0, 0.0, 0.0, 0.0);
    }

    /*
     *
     * PID
     *
     */

    /**
     * Sends a position goal to the TalonFX closed-loop controller.
     *
     * @param argGoalPosition Target position in rotations
     */
    public void setGoal(final double argGoalPosition) {
        goal = argGoalPosition;
        controller.setOutputPosition(goal, feedForward);
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
        controller.setOutputPosition(goal, feedForward);
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
    }
}
