package com.team271.lib.hardware.sensors.encoders;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.*;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

@SuppressWarnings("NullAway.Init")
public abstract class EncoderCTRE extends EncoderBase {
    /*
     * EncoderCTRE
     */
    protected StatusCode ctreStatus;

    protected double updateFreqHz = 250.0;

    protected StatusSignal<Angle> sigPos;
    protected StatusSignal<AngularVelocity> sigVel;

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
    public EncoderCTRE(
            final TObj argParent,
            final String argName,
            final EncoderType argEncoderType,
            final EncoderDirection argEncoderDirection,
            final double argUpdateFreqHz) {
        super(argParent, "(EncoderCTRE)" + argName, argEncoderType);

        /*
         * Store Direction
         */
        direction = argEncoderDirection;

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
    public void refresh() {
        /* Since these are already refreshed we don't need to inline the refresh call */
        if ((sigVel != null) && sigVel.getStatus().isOK()) {
            velRotations = sigVel.getValue().in(RotationsPerSecond);

            if ((sigPos != null) && sigPos.getStatus().isOK()) {
                posRotations =
                        BaseStatusSignal.getLatencyCompensatedValue(sigPos, sigVel).in(Rotations);
            }
        }
    }

    /*
     *
     * Simulation
     *
     */
    @Override
    public void simulationInit(final double argTimestamp) {}

    @Override
    public void simulationPeriodic(final double argTimestamp) {}

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
