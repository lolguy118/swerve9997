package com.team271.lib.hardware.sensors.encoders;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.*;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;

public class EncoderCANCoderComp extends EncoderCANCoder {
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
    public EncoderCANCoderComp(
            final TObj argParent,
            final String argName,
            final CANDeviceID argCANID,
            final EncoderDirection argEncoderDirection,
            final double argUpdateFreqHz) {
        super(
                argParent,
                "(CANCoder Comp)" + argName,
                argCANID,
                argEncoderDirection,
                argUpdateFreqHz);
    }

    /*
     *
     * Refresh
     *
     */
    public void refresh() {
        /* Use the helper function to apply latency compensation to the signals */
        /* Since these are already refreshed we don't need to inline the refresh call */
        if ((sigVel != null) && sigVel.getStatus().isOK()) {
            velRotations = sigVel.getValue().in(RotationsPerSecond);

            if ((sigPos != null) && sigPos.getStatus().isOK()) {
                posRotations =
                        BaseStatusSignal.getLatencyCompensatedValue(sigPos, sigVel).in(Rotations);
            }

            if ((sigPosBoot != null) && sigPosBoot.getStatus().isOK()) {
                posBoot =
                        BaseStatusSignal.getLatencyCompensatedValue(sigPosBoot, sigVel)
                                .in(Rotations);
            }

            if ((sigPosAbs != null) && sigPosAbs.getStatus().isOK()) {
                posAbs =
                        BaseStatusSignal.getLatencyCompensatedValue(sigPosAbs, sigVel)
                                .in(Rotations);
            }
        }
    }
}
