package com.team271.lib.control;

/**
 * Unified PID controller interface for all PID implementations.
 *
 * <p>Allows swapping between PID implementations (PIDSimple, PIDWPI, PIDFX, PIDTrap, PIDWPI_Trap)
 * without changing calling code. All five library PID classes implement this interface through
 * {@link com.team271.lib.control.pid.PIDBase}.
 *
 * <pre>{@code
 * // Swap implementations by changing the constructor — calling code stays the same
 * PIDController pid = new PIDSimple(parent, "Arm", 1.0, 0.0, 0.05, 0.02);
 * // or: PIDController pid = new PIDWPI(parent, "Arm", 1.0, 0.0, 0.05, 0.02);
 * // or: PIDController pid = new PIDFX(parent, "Arm", talonFX, 1.0, 0.0, 0.05);
 *
 * double output = pid.calculate(measurement, setpoint, timestamp);
 * if (pid.atSetpoint()) { ... }
 * }</pre>
 *
 * <p>For profiled (motion-constrained) PID, see {@link ProfiledPIDController}.
 *
 * <p><strong>Note:</strong> This is the library's PID interface, distinct from WPILib's {@code
 * edu.wpi.first.math.controller.PIDController} class. Use full package qualification if both are
 * used in the same file.
 */
public interface PIDController {

    /* --- Gains --- */

    void setPID(double kP, double kI, double kD);

    void setP(double kP);

    double getP();

    void setI(double kI);

    double getI();

    void setD(double kD);

    double getD();

    /* --- Tolerances --- */

    void setTolerance(double posTolerance);

    void setTolerance(double posTolerance, double velTolerance);

    /* --- Output Range --- */

    void setOutputRange(double minOutput, double maxOutput);

    /* --- Continuous Input --- */

    void enableContinuousInput(double minInput, double maxInput);

    void disableContinuousInput();

    boolean isContinuousInputEnabled();

    /* --- Calculation --- */

    /**
     * Calculates the next PID output.
     *
     * @param measurement current process variable measurement
     * @param setpoint target setpoint
     * @param timestamp current timestamp in seconds
     * @return controller output (clamped to output range)
     */
    double calculate(double measurement, double setpoint, double timestamp);

    /* --- State --- */

    /** Returns true if the error is within the tolerance. */
    boolean atSetpoint();

    /** Returns the current position error (setpoint - measurement). */
    double getPositionError();

    /** Returns the current velocity error (rate of change of position error). */
    double getVelocityError();

    /** Returns the most recent controller output. */
    double getOutput();

    /** Resets all internal state (errors, integrator, timestamps). */
    void reset();

    /* --- Telemetry --- */

    void outputTelemetry();
}
