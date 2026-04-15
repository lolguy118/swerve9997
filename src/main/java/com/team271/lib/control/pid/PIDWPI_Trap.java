package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

public class PIDWPI_Trap extends PIDBase implements com.team271.lib.control.ProfiledPIDController {

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

    /* --- ProfiledPIDController interface methods --- */

    @Override
    public void setGoal(final double goalPosition, final double goalVelocity) {
        goal = goalPosition;
        controller.setGoal(new TrapezoidProfile.State(goalPosition, goalVelocity));
    }

    @Override
    public void setConstraints(final double maxVelocity, final double maxAcceleration) {
        setConstraints(new TrapezoidProfile.Constraints(maxVelocity, maxAcceleration));
    }

    @Override
    public boolean atGoal() {
        return controller.atGoal();
    }

    @Override
    public double getSetpointPosition() {
        return controller.getSetpoint().position;
    }

    @Override
    public double getSetpointVelocity() {
        return controller.getSetpoint().velocity;
    }

    @Override
    public void reset(final double measuredPosition, final double measuredVelocity) {
        super.reset();
        controller.reset(measuredPosition, measuredVelocity);
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

    /**
     * Calculates the profiled PID output by delegating to the WPILib ProfiledPIDController.
     *
     * <p>Uses the argSetpoint as a position-only goal (zero velocity). If a non-zero velocity goal
     * was set via {@link #setGoal(double, double)}, use {@code calc(measurement)} or {@code
     * calculate(measurement, setpoint, timestamp)} instead — calling this method directly will
     * overwrite the velocity goal to zero.
     */
    @Override
    public double calc(
            final double argInputMeasurement, final double argSetpoint, final double argTimestamp) {
        lastInputMeasurement = argInputMeasurement;
        lastTimestamp = argTimestamp;

        controller.setGoal(argSetpoint);
        goal = argSetpoint;
        output = controller.calculate(argInputMeasurement);

        return output;
    }

    /**
     * Calculates using the PIDController interface. If a goal was set via {@link #setGoal(double,
     * double)}, this preserves the velocity target.
     */
    @Override
    public double calculate(
            final double measurement, final double setpoint, final double timestamp) {
        lastInputMeasurement = measurement;
        lastTimestamp = timestamp;

        /* Use the existing goal if one was set via setGoal; otherwise use setpoint */
        if (controller.getGoal().position != setpoint) {
            controller.setGoal(setpoint);
            goal = setpoint;
        }
        output = controller.calculate(measurement);

        return output;
    }

    /** Single-arg convenience — uses the controller's existing goal. */
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
