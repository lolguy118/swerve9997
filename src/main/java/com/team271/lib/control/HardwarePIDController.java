package com.team271.lib.control;

import com.team271.lib.api.motor.ClosedLoopMotor;

/**
 * Extended PID controller interface for controllers that delegate to hardware (e.g., TalonFX
 * onboard PID at 1 kHz).
 *
 * <p>Adds explicit goal-sending methods that make the hardware PID semantics clear: instead of
 * computing output in software, the controller sends a position or velocity goal to the motor
 * controller hardware and reads back error/output for telemetry.
 *
 * <p>Implementation: {@link com.team271.lib.control.pid.PIDFX}
 *
 * <pre>{@code
 * // Hardware PID (runs on TalonFX at 1kHz):
 * HardwarePIDController pid = new PIDFX(parent, "Arm", talonFX, kP, kI, kD, tol);
 * pid.setGoalPosition(targetPos, feedForward);
 *
 * // Software PID (runs in robot loop at 50Hz):
 * PIDController pid = new PIDSimple(parent, "Arm", kP, kI, kD, tol);
 * double output = pid.calculate(measurement, setpoint, timestamp);
 *
 * // Both support: atSetpoint(), getPositionError(), getOutput(), reset()
 * }</pre>
 */
public interface HardwarePIDController extends PIDController {

    /**
     * Sends a position goal to the hardware closed-loop controller.
     *
     * @param argPosition target position in native units
     * @param argFeedForward feedforward voltage
     */
    void setGoalPosition(double argPosition, double argFeedForward);

    /**
     * Sends a velocity goal to the hardware closed-loop controller.
     *
     * @param argRPS target velocity in rotations per second
     * @param argFeedForward feedforward voltage
     */
    void setGoalVelocity(double argRPS, double argFeedForward);

    /** Returns the motor this hardware PID controller sends goals to. */
    ClosedLoopMotor getMotor();
}
