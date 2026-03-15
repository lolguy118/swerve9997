package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;

public class PIDFX extends PIDBase {
    protected final ControllerTalonFX controller;

    /*
     *
     * Constructors
     *
     */
    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argP,
            final double argI,
            final double argD,
            final double argTol) {
        super(argParent, "(TALONFX)" + argName, PIDType.TALONFX, argP, argI, argD, argTol);

        controller = argTalonFX;

        controller.setPIDFSlot(0, argP, argI, argD, 0.0, 0.0);

        reset();
    }

    public PIDFX(
            final TObj argParent,
            final String argName,
            final ControllerTalonFX argTalonFX,
            final double argP,
            final double argI,
            final double argD) {
        this(argParent, argName, argTalonFX, argP, argI, argD, 0.0);
    }

    public PIDFX(final TObj argParent, final String argName, final ControllerTalonFX argTalonFX) {
        this(argParent, argName, argTalonFX, 0.0, 0.0, 0.0, 0.0);
    }

    /*
     *
     * PID
     *
     */
    public void setGoal(final double argGoalPosition) {}

    /*
     *
     * Calculate
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
    }
}
