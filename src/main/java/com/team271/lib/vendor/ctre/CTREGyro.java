package com.team271.lib.vendor.ctre;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.team271.lib.api.sensor.Gyro;
import com.team271.lib.hardware.sensors.imu.IMUPigeon2;
import com.team271.lib.nt.NTTable;
import edu.wpi.first.math.geometry.Rotation2d;

/**
 * CTRE Pigeon2 IMU exposed through the vendor-neutral {@link Gyro} interface.
 *
 * <p>Wraps {@link IMUPigeon2}. Passthrough access to the raw Pigeon2 is available via {@link
 * #getPigeon2()}.
 */
public class CTREGyro implements Gyro {

    /*
     * IMU
     */
    private final IMUPigeon2 mIMU;

    /*
     * Constructor
     */
    public CTREGyro(final IMUPigeon2 argIMU) {
        mIMU = argIMU;
    }

    /*
     * Passthrough
     */

    /** Returns the underlying IMUPigeon2 for passthrough access. */
    public IMUPigeon2 getIMU() {
        return mIMU;
    }

    /** Returns the raw CTRE Pigeon2 device. */
    public Pigeon2 getPigeon2() {
        return mIMU.getPigeon2();
    }

    /*
     *
     * Gyro Interface
     *
     */

    @Override
    public double getYaw() {
        return mIMU.getYaw();
    }

    @Override
    public double getYawRate() {
        return mIMU.getYawRate();
    }

    @Override
    public double getRoll() {
        return mIMU.getRoll();
    }

    @Override
    public double getPitch() {
        return mIMU.getPitch();
    }

    @Override
    public Rotation2d getHeading() {
        return mIMU.getHeading();
    }

    @Override
    public void reset() {
        mIMU.reset();
    }

    @Override
    public void setYaw(final double argDegrees) {
        mIMU.setYaw(argDegrees);
    }

    @Override
    public void setSimYaw(final double argDegrees) {
        mIMU.setSimYaw(argDegrees);
    }

    /*
     *
     * Lifecycle
     *
     */

    @Override
    public void robotInit(final double argTimestamp) {
        mIMU.robotInit(argTimestamp);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        mIMU.robotPeriodicBefore(argTimestamp);
    }

    @Override
    public void outputTelemetry() {
        mIMU.outputTelemetry();
    }

    @Override
    public void simulationInit(final double argTimestamp) {
        mIMU.simulationInit(argTimestamp);
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        mIMU.simulationPeriodic(argTimestamp);
    }

    /*
     *
     * Named
     *
     */

    @Override
    public String getName() {
        return mIMU.getName();
    }

    @Override
    public NTTable getTable() {
        return mIMU.getTable();
    }

    @Override
    public String logKey(final String argSuffix) {
        return mIMU.logKey(argSuffix);
    }
}
