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
     * @param goalPosition target position in mechanism units
     */
    void setGoal(double goalPosition);

    /**
     * Sets the goal position and velocity.
     *
     * @param goalPosition target position
     * @param goalVelocity target velocity at the goal (typically 0 for position control)
     */
    void setGoal(double goalPosition, double goalVelocity);

    /**
     * Sets the motion profile constraints.
     *
     * @param maxVelocity maximum velocity (mechanism units per second)
     * @param maxAcceleration maximum acceleration (mechanism units per second squared)
     */
    void setConstraints(double maxVelocity, double maxAcceleration);

    /** Returns true if the profiled setpoint has reached the goal and error is within tolerance. */
    boolean atGoal();

    /** Returns the current profiled position setpoint (may differ from goal during profiling). */
    double getSetpointPosition();

    /** Returns the current profiled velocity setpoint. */
    double getSetpointVelocity();

    /**
     * Resets the profiled controller state to the given position and velocity.
     *
     * @param measuredPosition current actual position
     * @param measuredVelocity current actual velocity
     */
    void reset(double measuredPosition, double measuredVelocity);
}
