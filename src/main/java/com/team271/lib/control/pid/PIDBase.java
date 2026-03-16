package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import com.team271.lib.nt.LoggedNTInput;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.util.Util;
import edu.wpi.first.math.MathUtil;

public class PIDBase extends TObj {
    public enum PIDType {
        PIDSIMP,
        PIDTRAP,
        WPILIB,
        WPILIB_TRAP,
        TALONFX
    }

    protected static class PIDSlot {
        /* Gain for "proportional" control */
        protected double kP = 0.0;

        /* Gain for "integral" control */
        protected double kI = 0.0;

        /* Gain for "derivative" control */
        protected double kD = 0.0;

        /* The error that is considered at setpoint. */
        protected double posTolerance = 0.05;
        protected double velTolerance = Double.POSITIVE_INFINITY;

        /*
         * If the absolute error is less than deadband then treat error for the
         * proportional term as 0
         */
        private double pDeadband = 0.0;

        /* The error range where "integral" control applies */
        protected double iZone = Double.POSITIVE_INFINITY;

        /* Sets the minimum and maximum values for the integrator. */
        protected double iMin = -1.0;
        protected double iMax = 1.0;
    }

    protected static class Continuous {
        /* Do the endpoints wrap around? e.g. Absolute encoder */
        protected boolean enabled = false;

        /* Minimum and Maximum Input of the Controller, used for continuous mode */
        protected double minInput = 0.0;
        protected double maxInput = 0.0;
    }

    /*
     * General
     */
    protected final PIDType type;

    /*
     * PID Configuration
     */
    /* Continuous Input Mode */
    protected Continuous continuousMode = new Continuous();

    /* Active PID Gains */
    protected PIDSlot pidSlot = new PIDSlot();

    /* Minimum and Maximum Output of the Controller */
    protected double minOutput = -1.0;
    protected double maxOutput = 1.0;

    /*
     * PID Internals
     */
    /* Last input measurement */
    protected double lastInputMeasurement = Double.NaN;

    /* Last time controller output was calculated */
    protected double lastTimestamp = Double.NaN;

    /* Output from the "proportional" control */
    protected double outputP = 0.0;

    /* Output from the "integral" control */
    protected double outputI = 0.0;

    /* Output from the "derivative" control */
    protected double outputD = 0.0;

    /* Controller Output */
    protected double output = 0.0;

    /*
     * The error at the time of the second-most-recent call to calculate() (used to
     * compute velocity)
     */
    protected double prevError = 0.0;

    /* The sum of the errors for use in the integral calc */
    protected double totalError = 0.0;

    /* The error at the time of the most recent call to calculate() */
    protected double posError = 0.0;
    protected double velError = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    /*
     * Continuous Input
     */
    final NTEntry contEnabled =
            new NTEntry(table, "Continuous Input Enabled", continuousMode.enabled);
    final NTEntry contInputMin = new NTEntry(table, "Cont Input Min", continuousMode.minInput);
    final NTEntry contInputMax = new NTEntry(table, "Cont Input Max", continuousMode.maxInput);

    /*
     * PID Slot
     */
    final NTEntry p = new NTEntry(table, "P", pidSlot.kP);
    final NTEntry i = new NTEntry(table, "I", pidSlot.kI);
    final NTEntry d = new NTEntry(table, "D", pidSlot.kD);

    final NTEntry posTol = new NTEntry(table, "Pos Tol", 0.0);
    final NTEntry velTol = new NTEntry(table, "Vel Tol", 0.0);

    final NTEntry pDeadband = new NTEntry(table, "P Deadband", 0.0);

    final NTEntry iZone = new NTEntry(table, "I Zone", 0.0);

    final NTEntry iMin = new NTEntry(table, "I Min", 0.0);
    final NTEntry iMax = new NTEntry(table, "I Max", 0.0);

    final NTEntry outputMin = new NTEntry(table, "Output Min", 0.0);
    final NTEntry outputMax = new NTEntry(table, "Output Max", 0.0);

    /*
     * Internal
     */
    final NTEntry pubLastInputMeasurement = new NTEntry(table, "Last Input Measurement", 0.0);

    final NTEntry pubLastTimestamp = new NTEntry(table, "Last Timestamp", 0.0);

    final NTEntry pubOutputP = new NTEntry(table, "Output P", 0.0);
    final NTEntry pubOutputI = new NTEntry(table, "Output I", 0.0);
    final NTEntry pubOutputD = new NTEntry(table, "Output D", 0.0);

    final NTEntry pubOutputPID = new NTEntry(table, "Output PID", 0.0);

    final NTEntry pubPrevError = new NTEntry(table, "Prev Error", 0.0);
    final NTEntry pubTotalError = new NTEntry(table, "Total Error", 0.0);
    final NTEntry pubPosError = new NTEntry(table, "Positional Error", 0.0);
    final NTEntry pubVelError = new NTEntry(table, "Velocity Error", 0.0);

    final NTEntry ntAtSetpoint = new NTEntry(table, "At Setpoint", false);

    /*
     * Tuning Inputs (LoggedNTInput)
     */
    private LoggedNTInput tuneP;
    private LoggedNTInput tuneI;
    private LoggedNTInput tuneD;
    private LoggedNTInput tunePosTol;
    private LoggedNTInput tunePDeadband;
    private LoggedNTInput tuneIZone;
    private LoggedNTInput tuneOutputMin;
    private LoggedNTInput tuneOutputMax;

    /*
     * Allocate a PID Base object with the given constants for P, I, D, and
     * Tolerance
     *
     * @param argName    The Name of the object, this is used for diagnostics
     * @param argPIDType The Type of the object, this is used for diagnostics
     * @param argP       The Proportional coefficient
     * @param argI       The Integral coefficient
     * @param argD       The Derivative coefficient
     * @param argTol     The Tolerance for the controller
     */
    public PIDBase(
            final TObj argParent,
            final String argName,
            final PIDType argPIDType,
            final double argP,
            final double argI,
            final double argD,
            final double argTol) {
        super(argParent, "PID" + argName);

        type = argPIDType;

        /*
         * Setup Controller
         */
        setPID(argP, argI, argD);
        setTolerance(argTol);

        tuneP = new LoggedNTInput(table, "Tune P", argP);
        tuneI = new LoggedNTInput(table, "Tune I", argI);
        tuneD = new LoggedNTInput(table, "Tune D", argD);
        tunePosTol = new LoggedNTInput(table, "Tune Pos Tol", argTol);
        tunePDeadband = new LoggedNTInput(table, "Tune P Deadband", 0.0);
        tuneIZone = new LoggedNTInput(table, "Tune I Zone", Double.POSITIVE_INFINITY);
        tuneOutputMin = new LoggedNTInput(table, "Tune Output Min", -1.0);
        tuneOutputMax = new LoggedNTInput(table, "Tune Output Max", 1.0);

        reset();
    }

    /*
     * Allocate a PID Base object with the given constants for P, I, D
     *
     * @param argName    The Name of the object, this is used for diagnostics
     * @param argPIDType The Type of the object, this is used for diagnostics
     * @param argP       The Proportional coefficient
     * @param argI       The Integral coefficient
     * @param argD       The Derivative coefficient
     */
    public PIDBase(
            final TObj argParent,
            final String argName,
            final PIDType argPIDType,
            final double argP,
            final double argI,
            final double argD) {
        this(argParent, argName, argPIDType, argP, argI, argD, 0);
    }

    /*
     * Allocate a PID Base object
     *
     * @param argName    The Name of the object, this is used for diagnostics
     * @param argPIDType The Type of the object, this is used for diagnostics
     */
    public PIDBase(final TObj argParent, final String argName, final PIDType argPIDType) {
        this(argParent, argName, argPIDType, 0, 0, 0, 0);
    }

    /*
     * Reset all internal terms
     */
    protected void reset() {
        outputP = 0.0;
        outputI = 0.0;
        outputD = 0.0;
        output = 0.0;

        posError = 0.0;
        velError = 0.0;

        prevError = 0.0;
        totalError = 0.0;

        lastInputMeasurement = Double.NaN;
        lastTimestamp = Double.NaN;
    }

    /*
     * Get the Proportional coefficient
     *
     * @return Proportional coefficient
     */
    public double getP() {
        return pidSlot.kP;
    }

    /*
     * Sets the Proportional coefficient
     *
     * @param argP Proportional coefficient
     */
    public void setP(final double argP) {
        pidSlot.kP = argP;
    }

    /*
     * Get the Integral coefficient
     *
     * @return Integral coefficient
     */
    public double getI() {
        return pidSlot.kI;
    }

    /*
     * Sets the Integral coefficient
     *
     * @param argI Integral coefficient
     */
    public void setI(final double argI) {
        pidSlot.kI = argI;
    }

    /*
     * Get the Differential coefficient
     *
     * @return Differential coefficient
     */
    public double getD() {
        return pidSlot.kD;
    }

    /*
     * Sets the Differential coefficient
     *
     * @param argD Differential coefficient
     */
    public void setD(final double argD) {
        pidSlot.kD = argD;
    }

    /*
     * Set the PID controller gain parameters. Set the proportional, integral, and
     * differential coefficients.
     *
     * @param argP Proportional coefficient
     * @param argI Integral coefficient
     * @param argD Differential coefficient
     */
    public void setPID(final double argP, final double argI, final double argD) {
        pidSlot.kP = argP;
        pidSlot.kI = argI;
        pidSlot.kD = argD;
    }

    /*
     * Sets the error which is considered tolerable for use with atSetpoint().
     *
     * @param argPosTolerance Position error which is tolerable.
     */
    public void setTolerance(final double argPosTolerance) {
        setTolerance(argPosTolerance, Double.POSITIVE_INFINITY);
    }

    /*
     * Sets the error which is considered tolerable for use with atSetpoint().
     *
     * @param argPosTolerance Position error which is tolerable.
     * @param argVelTolerance Velocity error which is tolerable.
     */
    public void setTolerance(final double argPosTolerance, final double argVelTolerance) {
        pidSlot.posTolerance = argPosTolerance;
        pidSlot.velTolerance = argVelTolerance;
    }

    /*
     * Sets the minimum and maximum output of the controller.
     *
     * @param argMinOutput Minimum output.
     * @param argMaxOutput Maximum output.
     */
    public void setOutputRange(final double argMinOutput, final double argMaxOutput) {
        minOutput = argMinOutput;
        maxOutput = argMaxOutput;
    }

    /*
     * Returns true if the error is within the tolerance
     *
     * @return true if the error is less than the tolerance
     */
    public boolean atSetpoint() {
        return !Double.isNaN(lastInputMeasurement)
                && Math.abs(posError) < pidSlot.posTolerance
                && Math.abs(velError) < pidSlot.velTolerance;
    }

    /*
     * Sets the minimum and maximum values for the integrator.
     * <p>
     * When the cap is reached, the integrator value is added to the controller
     * output rather than the integrator value times the integral gain.
     *
     * @param argMinIntegral The minimum value of the integrator.
     * @param argMaxIntegral The maximum value of the integrator.
     */
    public void setIntegratorRange(final double argMinIntegral, final double argMaxIntegral) {
        pidSlot.iMin = argMinIntegral;
        pidSlot.iMax = argMaxIntegral;
    }

    /*
     * Sets the IZone range. When the absolute value of the position error is
     * greater than IZone, the total accumulated error will reset to zero, disabling
     * integral gain until the absolute value of the position error is less than
     * IZone. This is used to prevent integral windup. Must be non-negative. Passing
     * a value of zero will effectively disable integral gain.
     * Passing a value of {@link Double#POSITIVE_INFINITY} disables IZone
     * functionality.
     *
     * @param argIZone Maximum magnitude of error to allow integral control.
     */
    public void setIZone(final double argIZone) {
        if (argIZone < 0) {
            throw new IllegalArgumentException("IZone must be a non-negative number!");
        }
        pidSlot.iZone = argIZone;
    }

    /*
     * Sets the proportional deadband. When the absolute value of the position
     * error is less than the deadband, the proportional term output is zero.
     * This prevents motor jitter near the setpoint.
     *
     * @param argPDeadband The deadband magnitude (must be non-negative).
     */
    public void setPDeadband(final double argPDeadband) {
        if (argPDeadband < 0) {
            throw new IllegalArgumentException("PDeadband must be a non-negative number!");
        }
        pidSlot.pDeadband = argPDeadband;
    }

    /*
     * Enables continuous input.
     * <p>
     * Rather than using the max and min input range as constraints, it considers
     * them to be the same point and automatically calculates the shortest route to
     * the setpoint.
     *
     * @param argMinInput The minimum value expected from the input.
     * @param argMaxInput The maximum value expected from the input.
     */
    public void enableContinuousInput(final double argMinInput, final double argMaxInput) {
        continuousMode.enabled = true;
        continuousMode.minInput = argMinInput;
        continuousMode.maxInput = argMaxInput;
    }

    /*
     * Disables continuous input.
     */
    public void disableContinuousInput() {
        continuousMode.enabled = false;
    }

    /*
     * Returns true if continuous input is enabled.
     *
     * @return True if continuous input is enabled.
     */
    public boolean isContinuousInputEnabled() {
        return continuousMode.enabled;
    }

    /*
     *
     * Calculate
     *
     */

    /*
     * Returns the next output of the PID controller.
     *
     * @param argInputMeasurement The current measurement of the process variable.
     * @return The next controller output.
     */
    public double calc(
            final double argInputMeasurement, final double argSetpoint, final double argTimestamp) {
        /* Get and Limit dt */
        if (Double.isNaN(lastTimestamp)) {
            lastTimestamp = argTimestamp;
        }
        double tmpDT = argTimestamp - lastTimestamp;
        lastTimestamp = argTimestamp;
        tmpDT = Util.limit(tmpDT, 1E-6, Double.POSITIVE_INFINITY);

        /* Store previous error for derivative calculation */
        prevError = posError;

        /* Store our latest input measurement */
        lastInputMeasurement = argInputMeasurement;

        /* Calculate our positional error */
        posError = argSetpoint - lastInputMeasurement;

        /* Handle continuous mode (input wrapping) */
        if (continuousMode.enabled) {
            double errorBound = (continuousMode.maxInput - continuousMode.minInput) / 2.0;
            posError =
                    MathUtil.inputModulus(
                            argSetpoint - lastInputMeasurement, -errorBound, errorBound);
        }

        /* Calculate the rate of change of the error for our derivative calculation */
        velError = (posError - prevError) / tmpDT;

        /*
         * Handle integral windup with the iZone. If the absolute value of the position
         * error is greater than IZone, reset the total error
         */
        if (Math.abs(posError) > pidSlot.iZone) {
            totalError = 0.0;
        } else if (pidSlot.kI != 0) {
            totalError =
                    MathUtil.clamp(
                            totalError + (posError * tmpDT),
                            pidSlot.iMin / pidSlot.kI,
                            pidSlot.iMax / pidSlot.kI);
        }

        /* Don't blow away posError so as to not break derivative */
        double tmpProportionalError = (Math.abs(posError) < pidSlot.pDeadband) ? 0 : posError;

        /* Calculate each gain's output */
        outputP = pidSlot.kP * tmpProportionalError;
        outputI = pidSlot.kI * totalError;
        outputD = pidSlot.kD * velError;

        /* Limit the output */
        output = Util.limit(outputP + outputI + outputD, minOutput, maxOutput);

        return output;
    }

    /*
     *
     * Tuning
     *
     */
    protected void checkTuning() {
        if (tuneP.hasChanged()) setP(tuneP.getDbl());
        if (tuneI.hasChanged()) setI(tuneI.getDbl());
        if (tuneD.hasChanged()) setD(tuneD.getDbl());
        if (tunePosTol.hasChanged()) setTolerance(tunePosTol.getDbl());
        if (tunePDeadband.hasChanged()) setPDeadband(tunePDeadband.getDbl());
        if (tuneIZone.hasChanged()) setIZone(tuneIZone.getDbl());
        if (tuneOutputMin.hasChanged() || tuneOutputMax.hasChanged()) {
            setOutputRange(tuneOutputMin.getDbl(), tuneOutputMax.getDbl());
        }
    }

    /*
     *
     * Telemetry
     *
     */
    public void outputTelemetry() {
        checkTuning();

        contEnabled.publish(continuousMode.enabled);
        contInputMin.publish(continuousMode.minInput);
        contInputMax.publish(continuousMode.maxInput);

        p.publish(pidSlot.kP);
        i.publish(pidSlot.kI);
        d.publish(pidSlot.kD);

        posTol.publish(pidSlot.posTolerance);
        velTol.publish(pidSlot.velTolerance);

        pDeadband.publish(pidSlot.pDeadband);

        iZone.publish(pidSlot.iZone);

        iMin.publish(pidSlot.iMin);
        iMax.publish(pidSlot.iMax);

        outputMin.publish(minOutput);
        outputMax.publish(maxOutput);

        pubLastInputMeasurement.publish(lastInputMeasurement);
        pubLastTimestamp.publish(lastTimestamp);

        pubOutputP.publish(outputP);
        pubOutputI.publish(outputI);
        pubOutputD.publish(outputD);
        pubOutputPID.publish(output);

        pubPrevError.publish(prevError);
        pubTotalError.publish(totalError);
        pubPosError.publish(posError);
        pubVelError.publish(velError);

        ntAtSetpoint.publish(atSetpoint());

        table.publish();
    }
}
