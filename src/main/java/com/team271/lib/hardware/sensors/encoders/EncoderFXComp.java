package com.team271.lib.hardware.sensors.encoders;

import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;

/**
 * Marker subclass of EncoderFX for latency-compensated TalonFX internal encoders.
 *
 * <p>Note: {@link EncoderFX#refresh()} already applies latency compensation via {@code
 * BaseStatusSignal.getLatencyCompensatedValue(sigPos, sigVel)}. This subclass exists as a
 * type-level distinction so that {@link
 * com.team271.lib.hardware.transmissions.TransmissionBase#addEncoderFX} can differentiate between
 * compensated and non-compensated encoder configurations.
 */
public class EncoderFXComp extends EncoderFX {

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
