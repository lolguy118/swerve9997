package com.team271.lib.hardware.sensors.encoders;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;

public class EncoderFX extends EncoderCTRE {

    /*
     * Variables
     */
    protected final ControllerTalonFX controller;

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
    public EncoderFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argUpdateFreqHz) {
        super(argParent, "(Internal FX)" + argName, EncoderType.INTERNAL_FX, EncoderDirection.CW, argUpdateFreqHz);

        /*
         * Store FX Controller
         */
        controller = argTalonFX;

        /*
         * Create Objects
         */
        create();
    }

    /*
     *
     * Encoder
     *
     */
    @Override
    protected void create() {
        // Nothing to be done
    }

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        /*
         * Get Position and Velocity Objects
         */
        sigPos = controller.getTalonFX().getPosition();
        CTREManager.addSignalTalonFX(sigPos, updateFreqHz);

        sigVel = controller.getTalonFX().getVelocity();
        CTREManager.addSignalTalonFX(sigVel, updateFreqHz);
    }

    /*
     * Position Raw - Rotations
     */
    @Override
    public void setPosRotations(final double argPositionRotations) {
        super.setPosRotations(argPositionRotations);

        if (controller != null) {
            ctreStatus = controller.getTalonFX().setPosition(argPositionRotations, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

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
            velRotations = sigVel.getValue().in(RotationsPerSecond);

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
    @Override
    public void setSimPosRotations(final double argPositionRotations) {
        if ((controller != null) && (controller.getSimState() != null)) {
            controller.getSimState().setRawRotorPosition(argPositionRotations);
        }
    }

    @Override
    public void setSimVelRotations(final double argVelRotations) {
        if ((controller != null) && (controller.getSimState() != null)) {
            controller.getSimState().setRotorVelocity(argVelRotations);
        }
    }

    /*
     *
     * Telemetry
     *
     */
}
