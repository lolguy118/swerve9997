package com.team271.lib.control.pid;

import com.team271.lib.TObj;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class PIDWPI extends PIDBase {

    /*
     * PID
     */
    protected final PIDController controller;

    /*
     * Network Tables
     */
    protected class IO {
        final NetworkTableInstance inst = NetworkTableInstance.getDefault();
        final NetworkTable table = inst.getTable(name);

        /*
         * PID
         */

        /*
         * Setup
         */
        public void setupData() {
            // Default Method
        }

        /*
         * Publish
         */
        public void publish() {
            /*
             * PID
             */
        }
    }

    // private final IO mData;

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

        /*
         * Setup Network Tables
         */
        // mData = new IO();
        // mData.SetupData();
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

        controller.reset();
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

        // controller.setTolerance(posTolerance);
    }

    @Override
    public void setP(final double argP) {
        super.setP(argP);

        // controller.setP(kp);
    }

    @Override
    public void setI(final double argI) {
        super.setI(argI);

        // controller.setI(ki);
    }

    @Override
    public void setIntegratorRange(final double argMinimumIntegral, final double argMaximumIntegral) {
        super.setIntegratorRange(argMinimumIntegral, argMaximumIntegral);

        // controller.setIntegratorRange(iMin, iMax);
    }

    @Override
    public void setD(final double argD) {
        super.setD(argD);

        // controller.setD(kd);
    }

    @Override
    public void setPID(final double argP, final double argI, final double argD) {
        super.setPID(argP, argI, argD);

        // controller.setPID(kp, ki, kd);
    }

    /*
     *
     * Refresh
     *
     */
    public void refresh() {
        // error = controller.getPositionError();
    }

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

        // if (mData != null) {
        // mData.Publish();
        // }
    }
}
