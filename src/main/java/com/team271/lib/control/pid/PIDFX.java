package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import com.team271.lib.api.motor.ClosedLoopMotor;
import com.team271.lib.control.HardwarePIDController;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.vendor.ctre.CTREMotor;

/**
 * PID controller that delegates to the TalonFX motor controller's onboard PID.
 *
 * <p>Unlike software PID variants (PIDSimple, PIDWPI), the closed-loop calculation runs on the
 * TalonFX hardware at 1 kHz. This class sends position/velocity goals to the controller and reads
 * back closed-loop error and output for telemetry and {@link #atSetpoint()} checks.
 */
public class PIDFX extends PIDBase implements HardwarePIDController {
    private final ControllerTalonFX mController;
    private final int mSlot;

    private double mGoal = 0.0;
    private double mFeedForward = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    private final NTEntry ntGoal = new NTEntry(table, "Goal", 0.0);
    private final NTEntry ntFeedForward = new NTEntry(table, "Feed Forward", 0.0);

    /*
     *
     * Constructors
     *
     */
    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argController,
            final double argP,
            final double argI,
            final double argD,
            final double argTol,
            final int argSlot) {
        super(argParent, "(TALONFX)" + argName, PIDType.TALONFX, argP, argI, argD, argTol);

        mController = argController;
        mSlot = argSlot;

        mController.setPIDFSlot(mSlot, argP, argI, argD, 0.0, 0.0);

        reset();
    }

    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argController,
            final double argP,
            final double argI,
            final double argD,
            final double argTol) {
        this(argParent, argName, argController, argP, argI, argD, argTol, 0);
    }

    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argController,
            final double argP,
            final double argI,
            final double argD) {
        this(argParent, argName, argController, argP, argI, argD, 0.0, 0);
    }

    public PIDFX(
            final TObj argParent, final String argName, final ControllerTalonFX argController) {
        this(argParent, argName, argController, 0.0, 0.0, 0.0, 0.0, 0);
    }

    /*
     *
     * PID
     *
     */
    @Override
    public void setP(final double argP) {
        super.setP(argP);
        mController.setPSlot(mSlot, argP);
    }

    @Override
    public void setI(final double argI) {
        super.setI(argI);
        mController.setISlot(mSlot, argI);
    }

    @Override
    public void setD(final double argD) {
        super.setD(argD);
        mController.setDSlot(mSlot, argD);
    }

    @Override
    public void enableContinuousInput(final double argMinInput, final double argMaxInput) {
        super.enableContinuousInput(argMinInput, argMaxInput);
        mController.setContinuousWrap(true);
    }

    @Override
    public void disableContinuousInput() {
        super.disableContinuousInput();
        mController.setContinuousWrap(false);
    }

    /** Returns the PID slot index used by this controller. */
    public int getSlot() {
        return mSlot;
    }

    /**
     * Sends a position goal to the TalonFX closed-loop controller.
     *
     * @param argGoalPosition Target position in rotations
     */
    public void setGoal(final double argGoalPosition) {
        mGoal = argGoalPosition;
        mController.setOutputPosition(mGoal, mSlot, mFeedForward);
    }

    /**
     * Sends a position goal with feed-forward voltage to the TalonFX closed-loop controller.
     *
     * @param argGoalPosition Target position in rotations
     * @param argFeedForward Feed-forward voltage
     */
    public void setGoal(final double argGoalPosition, final double argFeedForward) {
        mGoal = argGoalPosition;
        mFeedForward = argFeedForward;
        mController.setOutputPosition(mGoal, mSlot, mFeedForward);
    }

    /*
     *
     * HardwarePIDController Interface
     *
     */

    @Override
    public void setGoalPosition(final double argPosition, final double argFeedForward) {
        setGoal(argPosition, argFeedForward);
    }

    @Override
    public void setGoalVelocity(final double argRPS, final double argFeedForward) {
        mGoal = argRPS;
        mFeedForward = argFeedForward;
        mController.setOutputVelocity(mGoal, mSlot, mFeedForward);
    }

    @Override
    public ClosedLoopMotor getMotor() {
        return new CTREMotor(mController);
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
        posError = mController.getCLError();
        output = mController.getCLOutput();

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

        ntGoal.publish(mGoal);
        ntFeedForward.publish(mFeedForward);
    }
}
