package com.team271.lib.control.pid;

import com.team271.lib.TObj;

public class PIDSimple extends PIDBase {
    /*
     * PID
     */
    protected double setpoint = 0.0;

    /*
     *
     * Constructors
     *
     */
    public PIDSimple(
            final TObj argParent,
            final String argName,
            final double argP,
            final double argI,
            final double argD,
            final double argTol) {
        super(argParent, "(PIDSimple)" + argName, PIDType.PIDSIMP, argP, argI, argD, argTol);
    }

    public PIDSimple(
            final TObj argParent,
            final String argName,
            final double argP,
            final double argI,
            final double argD,
            final double argTol,
            final TelemetryLevel argTelemetryLevel) {
        super(
                argParent,
                "(PIDSimple)" + argName,
                PIDType.PIDSIMP,
                argP,
                argI,
                argD,
                argTol,
                argTelemetryLevel);
    }

    /*
     *
     * PID
     *
     */
    public double getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(final double argSetpoint) {
        setpoint = argSetpoint;
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
    @Override
    public double calc(
            final double argInputMeasurement, final double argSetpoint, final double argTimestamp) {
        setpoint = argSetpoint;

        return super.calc(argInputMeasurement, setpoint, argTimestamp);
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
