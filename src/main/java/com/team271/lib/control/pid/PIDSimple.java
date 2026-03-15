package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class PIDSimple extends PIDBase {
    /*
     * PID
     */
    protected double setpoint = 0.0;

    /*
     * Network Tables
     */
    protected class IO {
        final NetworkTableInstance inst = NetworkTableInstance.getDefault();
        final NetworkTable table = inst.getTable(name);

        /*
         * PID
         */
        // final NTDoubleEntry ntSetpoint = new NTDoubleEntry(table, "Setpoint");

        /*
         * Setup
         */
        public void setup() {
            // ntSetpoint.setup(setpoint);
        }

        /*
         * Publish
         */
        public void publish() {
            // setpoint = ntSetpoint.publish(setpoint);
        }
    }

    private IO data;

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
    public double calc(final double argInputMeasurement, final double argSetpoint, final double argTimestamp) {
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

        if (data != null) {
            data.publish();
        }
    }
}
