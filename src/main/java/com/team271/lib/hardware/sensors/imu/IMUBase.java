package com.team271.lib.hardware.sensors.imu;

import static edu.wpi.first.units.Units.*;

import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.math.geometry.Rotation2d;

public abstract class IMUBase extends TObj {
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

    public void reset() {
        yaw = Degrees.of(0).in(Degree);
        yawRate = Degrees.of(0).in(Degree);
        roll = Degrees.of(0).in(Degree);
        pitch = Degrees.of(0).in(Degree);
    }

    public double getYaw() {
        return yaw;
    }

    public double getYawRate() {
        return yawRate;
    }

    public double getRoll() {
        return roll;
    }

    public double getPitch() {
        return pitch;
    }

    public Rotation2d getHeading() {
        return new Rotation2d();
    }

    /*
     *
     * Simulation
     *
     */
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
