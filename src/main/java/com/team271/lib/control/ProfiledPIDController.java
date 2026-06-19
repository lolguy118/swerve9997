package com.team271.lib.control;

/**
 * Extended PID controller interface with motion profile constraints.
 *
 * <p>Profiled controllers generate smooth trajectories between the current position and the goal,
 * respecting velocity and acceleration limits. The PID controller then tracks the profiled
 * setpoint.
 *
 * <p>Implementations: {@link com.team271.lib.control.pid.PIDTrap} (custom trapezoidal), {@link
 * com.team271.lib.control.pid.PIDWPI_Trap} (WPILib ProfiledPIDController).
 *
 * <p>For CTRE Motion Magic, use {@code TransmissionFX.setOutputMMPosition*()} directly — it runs
 * the profile on the motor controller at 1 kHz and is not interchangeable with software-side
 * profiled PID.
 */
public interface ProfiledPIDController extends PIDController {

    /**
     * Sets the goal position. The profiled controller will generate a trajectory from the current
     * position to this goal.
     *
     * @param argGoalPosition target position in mechanism units
     */
    void setGoal(double argGoalPosition);

    /**
     * Sets the goal position and velocity.
     *
     * @param argGoalPosition target position
     * @param argGoalVelocity target velocity at the goal (typically 0 for position control)
     */
    void setGoal(double argGoalPosition, double argGoalVelocity);

    /**
     * Sets the motion profile constraints.
     *
     * @param argMaxVelocity maximum velocity (mechanism units per second)
     * @param argMaxAcceleration maximum acceleration (mechanism units per second squared)
     */
    void setConstraints(double argMaxVelocity, double argMaxAcceleration);

    /** Returns true if the profiled setpoint has reached the goal and error is within tolerance. */
    boolean atGoal();

    /** Returns the current profiled position setpoint (may differ from goal during profiling). */
    double getSetpointPosition();

    /** Returns the current profiled velocity setpoint. */
    double getSetpointVelocity();

    /**
     * Resets the profiled controller state to the given position and velocity.
     *
     * @param argMeasuredPosition current actual position
     * @param argMeasuredVelocity current actual velocity
     */
    void reset(double argMeasuredPosition, double argMeasuredVelocity);
}
