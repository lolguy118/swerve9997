package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

public class PIDTrap extends PIDBase {

    /*
     * PID
     */
    private TrapezoidProfile.Constraints constraints;

    private TrapezoidProfile.State goal = new TrapezoidProfile.State();
    private TrapezoidProfile.State setpoint = new TrapezoidProfile.State();

    /* Last time controller output was calculated */
    protected double timestampProfileStart = Double.NaN;

    /* Trapezoidal Profile */
    private TrapezoidProfile profile;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry tGoalPos = new NTEntry(table, "Goal.Pos(Trap)", 0.0);
    final NTEntry tGoalVel = new NTEntry(table, "Goal.Vel(Trap)", 0.0);

    final NTEntry tSetpointPos = new NTEntry(table, "Setpoint.Pos(Trap)", 0.0);
    final NTEntry tSetpointVel = new NTEntry(table, "Setpoint.Vel(Trap)", 0.0);

    /*
     *
     * Constructors
     *
     */
    public PIDTrap(
            final TObj argParent,
            final String argName,
            final double argP,
            final double argI,
            final double argD,
            final double argTol,
            final double argMaxVelocity,
            final double argMaxAcceleration) {
        super(argParent, "(PIDTrap)" + argName, PIDType.PIDTRAP, argP, argI, argD, argTol);

        /*
         * Controller
         */
        constraints = new TrapezoidProfile.Constraints(argMaxVelocity, argMaxAcceleration);

        profile = new TrapezoidProfile(constraints);
    }

    /*
     *
     * PID
     *
     */

    /*
     * Reset the previous error and the integral term.
     *
     * @param argMeasuredPosition The current measured position of the system.
     * @param argMeasuredVelocity The current measured velocity of the system.
     */
    public void reset(final double argMeasuredPosition, final double argMeasuredVelocity) {
        super.reset();

        setpoint.position = argMeasuredPosition;
        setpoint.velocity = argMeasuredVelocity;

        timestampProfileStart = Double.NaN;
    }

    /*
     * Reset the previous error and the integral term.
     *
     * @param argMeasuredPosition The current measured position of the system. The
     *                            velocity is assumed to be zero.
     */
    public void reset(final double argMeasuredPosition) {
        reset(argMeasuredPosition, 0.0);
    }

    public void reset(final TrapezoidProfile.State argMeasuredPosition) {
        reset(argMeasuredPosition.position, argMeasuredPosition.velocity);
    }

    /*
     * Sets the goal for the ProfiledPIDController.
     *
     * @param argGoalPosition The desired goal position.
     */
    public void setGoal(final double argGoalPosition) {
        goal.position = argGoalPosition;
        goal.velocity = 0.0;

        timestampProfileStart = Double.NaN;
    }

    /*
     * Sets the goal for the ProfiledPIDController.
     *
     * @param argGoal The desired goal state.
     */
    public void setGoal(final TrapezoidProfile.State argGoal) {
        setGoal(argGoal.position);
    }

    /*
     * Gets the goal for the ProfiledPIDController.
     *
     * @return The goal.
     */
    public TrapezoidProfile.State getGoal() {
        return goal;
    }

    /*
     * Returns true if the error is within the tolerance of the error.
     *
     * <p>
     * This will return false until at least one input value has been computed.
     *
     * @return True if the error is within the tolerance of the error.
     */
    public boolean atGoal() {
        return atSetpoint() && goal.equals(setpoint);
    }

    /*
     * Set velocity and acceleration constraints for goal.
     *
     * @param argConstraints Velocity and acceleration constraints for goal.
     */
    public void setConstraints(final TrapezoidProfile.Constraints argConstraints) {
        constraints = argConstraints;

        profile = new TrapezoidProfile(constraints);

        setGoal(goal);
    }

    /*
     * Get the velocity and acceleration constraints for this controller.
     *
     * @return Velocity and acceleration constraints.
     */
    public TrapezoidProfile.Constraints getConstraints() {
        return constraints;
    }

    /*
     * Returns the current setpoint of the ProfiledPIDController.
     *
     * @return The current setpoint.
     */
    public TrapezoidProfile.State getSetpoint() {
        return setpoint;
    }

    public double getSetpointVel() {
        return setpoint.velocity;
    }

    /*
     *
     * Refresh
     *
     */

    /*
     *
     * Calculate
     *
     */

    /*
     * Returns the next output of the PID controller.
     *
     * @param argInputMeasurement The current argMeasurement of the process
     *                            variable.
     * @return The controller's next output.
     */
    public double calc(final double argInputMeasurement, final double argTimestamp) {
        /* Check if this is the start of a profile */
        if (Double.isNaN(timestampProfileStart)) {
            timestampProfileStart = argTimestamp;
        }

        /* Handle continuous mode (input wrapping) */
        if (continuousMode.enabled) {
            // Get error which is the smallest distance between goal and argMeasurement
            double errorBound = (continuousMode.maxInput - continuousMode.minInput) / 2.0;
            double goalMinDistance =
                    MathUtil.inputModulus(
                            goal.position - argInputMeasurement, -errorBound, errorBound);
            double setpointMinDistance =
                    MathUtil.inputModulus(
                            setpoint.position - argInputMeasurement, -errorBound, errorBound);

            // Recompute the profile goal with the smallest error, thus giving the shortest
            // path. The goal may be outside the input range after this operation, but
            // that's
            // OK because the controller will still go there and report an error of zero. In
            // other words, the setpoint only needs to be offset from the argMeasurement by
            // the input range modulus; they don't need to be equal.
            goal.position = goalMinDistance + argInputMeasurement;
            setpoint.position = setpointMinDistance + argInputMeasurement;
        }

        /* Calculate the setpoint based off of a trapezoidal profile */
        setpoint = profile.calculate(argTimestamp - timestampProfileStart, goal, setpoint);

        /*
         * Calculate the next step of the PID controller based off of this new setpoint
         */
        return calc(argInputMeasurement, setpoint.position, argTimestamp);
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        tGoalPos.publish(goal.position);
        tGoalVel.publish(goal.velocity);

        tSetpointPos.publish(setpoint.position);
        tSetpointVel.publish(setpoint.velocity);
    }
}
