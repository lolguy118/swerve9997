package com.team271.lib.hardware.sensors.encoders;

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
        super(
                argParent,
                "(Internal FX)" + argName,
                EncoderType.INTERNAL_FX,
                EncoderDirection.CW,
                argUpdateFreqHz);

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
        CTREManager.addSignal(sigPos, updateFreqHz);

        sigVel = controller.getTalonFX().getVelocity();
        CTREManager.addSignal(sigVel, updateFreqHz);
    }

    /*
     * Position Raw - Rotations
     */
    @Override
    public void setPosRotations(final double argPositionRotations) {
        super.setPosRotations(argPositionRotations);

        if (controller != null) {
            ctreStatus =
                    controller
                            .getTalonFX()
                            .setPosition(argPositionRotations, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    /* refresh() inherited from EncoderCTRE — uses latency compensation */

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
