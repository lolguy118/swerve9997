package com.team271.lib.hardware.sensors.encoders;

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
     * Simulation
     *
     */

    /*
     *
     * Telemetry
     *
     */
}
