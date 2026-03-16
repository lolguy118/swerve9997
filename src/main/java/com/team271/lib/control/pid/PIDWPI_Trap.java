package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

public class PIDWPI_Trap extends PIDBase {

    /*
     * PID
     */
    protected final TrapezoidProfile.Constraints constraints =
            new TrapezoidProfile.Constraints(0, 0);

    protected final ProfiledPIDController controller =
            new ProfiledPIDController(0, 0, 0, constraints);

    double goal = 0;

    /*
     *
     * Constructors
     *
     */
    public PIDWPI_Trap(
            final TObj argParent,
            final String argName,
            final double argP,
            final double argI,
            final double argD,
            final double argTol,
            final double argMaxVelocity,
            final double argMaxAcceleration) {
        super(argParent, "(WPILIB_Trap)" + argName, PIDType.WPILIB_TRAP, argP, argI, argD, argTol);

        /*
         * Controller
         */
        controller.setConstraints(
                new TrapezoidProfile.Constraints(argMaxVelocity, argMaxAcceleration));
        controller.setPID(argP, argI, argD);
        controller.setTolerance(argTol);
    }

    /*
     *
     * PID
     *
     */
    public void reset(final double argMeasuredPosition) {
        controller.reset(argMeasuredPosition);
    }

    public ProfiledPIDController getController() {
        return controller;
    }

    public void setConstraints(final TrapezoidProfile.Constraints argConstraints) {
        controller.setConstraints(argConstraints);
    }

    public void setGoal(final double argGoal) {
        goal = argGoal;

        controller.setGoal(goal);
    }

    public boolean atGoal(final double argMeasurement) {
        return controller.atGoal();
    }

    public TrapezoidProfile.State getSetpointState() {
        return controller.getSetpoint();
    }

    @Override
    public boolean atSetpoint() {
        return controller.atSetpoint();
    }

    @Override
    public void setTolerance(final double argTolerance) {
        super.setTolerance(argTolerance);

        if (controller != null) {
            controller.setTolerance(pidSlot.posTolerance);
        }
    }

    @Override
    public void setP(final double argP) {
        super.setP(argP);

        if (controller != null) {
            controller.setP(pidSlot.kP);
        }
    }

    @Override
    public void setI(final double argI) {
        super.setI(argI);

        if (controller != null) {
            controller.setI(pidSlot.kI);
        }
    }

    @Override
    public void setIntegratorRange(
            final double argMinimumIntegral, final double argMaximumIntegral) {
        super.setIntegratorRange(argMinimumIntegral, argMaximumIntegral);

        if (controller != null) {
            controller.setIntegratorRange(pidSlot.iMin, pidSlot.iMax);
        }
    }

    @Override
    public void setD(final double argD) {
        super.setD(argD);

        if (controller != null) {
            controller.setD(pidSlot.kD);
        }
    }

    @Override
    public void setPID(final double argP, final double argI, final double argD) {
        super.setPID(argP, argI, argD);

        if (controller != null) {
            controller.setPID(pidSlot.kP, pidSlot.kI, pidSlot.kD);
        }
    }

    /*
     *
     * Refresh
     *
     */
    public void refresh() {}

    /*
     *
     * Calculate
     *
     */
    public double calc(final double argMeasurement) {
        return controller.calculate(argMeasurement);
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
