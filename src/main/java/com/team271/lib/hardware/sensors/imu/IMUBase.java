package com.team271.lib.hardware.sensors.imu;

import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.math.geometry.Rotation2d;

public abstract class IMUBase extends TObj implements IMU {
    public enum IMUType {
        PIGEON,
        PIGEON2
    }

    /*
     * IMU
     */
    protected final IMUType type;

    protected double yaw = 0.0;
    protected double yawRate = 0.0;
    protected double roll = 0.0;
    protected double pitch = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntType = new NTEntry(table, "Type", "");

    final NTEntry ntYaw = new NTEntry(table, "Yaw", 0.0);
    final NTEntry ntYawRate = new NTEntry(table, "YawRate", 0.0);
    final NTEntry ntRoll = new NTEntry(table, "Roll", 0.0);
    final NTEntry ntPitch = new NTEntry(table, "Pitch", 0.0);

    /*
     *
     * Constructors
     *
     */
    protected IMUBase(final TObj argParent, final String argName, final IMUType argIMUType) {
        super(argParent, "(IMU)" + argName);

        /*
         * Store Type
         */
        type = argIMUType;
    }

    /*
     *
     * IMU
     *
     */
    protected abstract void create();

    @Override
    public void reset() {
        yaw = 0.0;
        yawRate = 0.0;
        roll = 0.0;
        pitch = 0.0;
    }

    @Override
    public double getYaw() {
        return yaw;
    }

    @Override
    public double getYawRate() {
        return yawRate;
    }

    @Override
    public double getRoll() {
        return roll;
    }

    @Override
    public double getPitch() {
        return pitch;
    }

    @Override
    public Rotation2d getHeading() {
        return Rotation2d.fromDegrees(yaw);
    }

    /** Sets the yaw to the specified value in degrees. */
    @Override
    public abstract void setYaw(double argDegrees);

    /*
     *
     * Simulation
     *
     */
    /** Sets the simulated yaw in degrees. */
    @Override
    public abstract void setSimYaw(double argDegrees);

    @Override
    public void simulationInit(final double argTimestamp) {}

    @Override
    public void simulationPeriodic(final double argTimestamp) {}

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntYaw.publish(getYaw());

        ntYawRate.publish(getYawRate());

        ntRoll.publish(getRoll());

        ntPitch.publish(getPitch());

        switch (type) {
            case PIGEON:
                ntType.publish("Pigeon");
                break;
            case PIGEON2:
                ntType.publish("Pigeon2");
                break;
            default:
                ntType.publish(ConstantsLib.S_INVALID);
                break;
        }
    }
}
