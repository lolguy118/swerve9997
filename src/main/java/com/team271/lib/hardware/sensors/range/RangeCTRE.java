package com.team271.lib.hardware.sensors.range;

import com.ctre.phoenix6.*;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.units.measure.Distance;

public abstract class RangeCTRE extends RangeBase {
    /*
     * EncoderCTRE
     */
    protected StatusCode ctreStatus;

    protected double updateFreqHz = 250.0;

    protected StatusSignal<Distance> sigDist;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntCTREStatus = new NTEntry(table, "CTRE Status", "");

    /*
     *
     * Constructors
     *
     */
    public RangeCTRE(
            final TObj argParent,
            final String argName,
            final RangeType argEncoderType,
            final double argUpdateFreqHz) {
        super(argParent, "(RangeCTRE)" + argName, argEncoderType);

        /*
         * Store Update Frequency in Hz
         */
        updateFreqHz = argUpdateFreqHz;
    }

    /*
     *
     * Encoder
     *
     */

    /*
     *
     * Robot
     *
     */
    public StatusCode applyConfig() {
        return ctreStatus;
    }

    /*
     *
     * Refresh
     *
     */
    public abstract void refresh();

    /*
     *
     * Simulation
     *
     */

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        if (ctreStatus != null) {
            ntCTREStatus.publish(ctreStatus.getDescription());
        }
    }
}
