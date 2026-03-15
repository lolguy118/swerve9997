package com.team271.lib.hardware.sensors.encoders;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;

public class EncoderFXComp extends EncoderFX {

    /*
     * Variables
     */

    /*
     *
     * Telemetry (NT)
     *
     */

    /*
     *
     * Constructors
     *
     */
    public EncoderFXComp(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argUpdateFreqHz) {
        super(argParent, "(Internal FX Comp)" + argName, argTalonFX, argUpdateFreqHz);
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

    /*
     *
     * Refresh
     *
     */
    @Override
    public void refresh() {
        /* Use the helper function to apply latency compensation to the signals */
        /* Since these are already refreshed we don't need to inline the refresh call */
        if ((sigVel != null) && sigVel.getStatus().isOK()) {
            velRotations = sigVel.getValueAsDouble();

            if ((sigPos != null) && sigPos.getStatus().isOK()) {
                posRotations = BaseStatusSignal.getLatencyCompensatedValue(sigPos, sigVel)
                        .in(Rotations);
            }
        }
    }

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
}
