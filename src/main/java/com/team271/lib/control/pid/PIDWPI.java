package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import edu.wpi.first.math.controller.PIDController;

public class PIDWPI extends PIDBase {

    /*
     * PID
     */
    protected final PIDController controller;

    /*
     *
     * Constructors
     *
     */
    public PIDWPI(
            final TObj argParent,
            final String argName,
            final double argControllerRateSecs,
            final double argP,
            final double argI,
            final double argD,
            final double argTol) {
        super(argParent, "(WPILIB)" + argName, PIDType.WPILIB, argP, argI, argD, argTol);

        /*
         * Controller
         */
        controller = new PIDController(argP, argI, argD, argControllerRateSecs);

        reset();
    }

    public PIDWPI(
            final TObj argParent,
            final String argName,
            final double argControllerRateSecs,
            final double argP,
            final double argI,
            final double argD) {
        this(argParent, argName, argControllerRateSecs, argP, argI, argD, 0.0);
    }

    public PIDWPI(final TObj argParent, final String argName, final double argControllerRateSecs) {
        this(argParent, argName, argControllerRateSecs, 0.0, 0.0, 0.0, 0.0);
    }

    /*
     *
     * PID
     *
     */
    @Override
    public void reset() {
        super.reset();

        if (controller != null) {
            controller.reset();
        }
    }

    public PIDController getController() {
        return controller;
    }

    public void setSetpoint(final double argSetpoint) {
        controller.setSetpoint(argSetpoint);
    }

    @Override
    public boolean atSetpoint() {
        return controller.atSetpoint();
    }

    @Override
    public void setTolerance(final double argTolerance) {
        super.setTolerance(argTolerance);

        if (controller != null) {
            controller.setTolerance(pidSlot.posTolerance);
        }
    }

    @Override
    public void setP(final double argP) {
        super.setP(argP);

        if (controller != null) {
            controller.setP(pidSlot.kP);
        }
    }

    @Override
    public void setI(final double argI) {
        super.setI(argI);

        if (controller != null) {
            controller.setI(pidSlot.kI);
        }
    }

    @Override
    public void setIntegratorRange(
            final double argMinimumIntegral, final double argMaximumIntegral) {
        super.setIntegratorRange(argMinimumIntegral, argMaximumIntegral);

        if (controller != null) {
            controller.setIntegratorRange(pidSlot.iMin, pidSlot.iMax);
        }
    }

    @Override
    public void setD(final double argD) {
        super.setD(argD);

        if (controller != null) {
            controller.setD(pidSlot.kD);
        }
    }

    @Override
    public void setPID(final double argP, final double argI, final double argD) {
        super.setPID(argP, argI, argD);

        if (controller != null) {
            controller.setPID(pidSlot.kP, pidSlot.kI, pidSlot.kD);
        }
    }

    /*
     *
     * Refresh
     *
     */
    public void refresh() {}

    /*
     *
     * Calculate
     *
     */
    public double calc(final double argMeasurement) {
        return controller.calculate(argMeasurement);
    }

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
