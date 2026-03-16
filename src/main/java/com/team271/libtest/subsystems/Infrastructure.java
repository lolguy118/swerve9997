package com.team271.libtest.subsystems;

import com.team271.lib.TObj;
import com.team271.lib.hardware.sensors.imu.IMUPigeon2;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.subsystem.Subsystem;
import com.team271.libtest.Constants;
import edu.wpi.first.wpilibj.RobotController;

public class Infrastructure extends Subsystem {
    public enum RobotMode {
        DISABLED,
        AUTONOMOUS,
        TELEOP,
        TEST
    }

    /*
     * Variables
     */
    private RobotMode robotMode = RobotMode.DISABLED;

    private IMUPigeon2 imu;

    /*
     * Telemetry
     */
    private final NTEntry ntRobotMode = new NTEntry(table, "Robot Mode", 0);
    private final NTEntry ntBatteryVoltage = new NTEntry(table, "Battery Voltage", 0.0);
    private final NTEntry ntHeadingDeg = new NTEntry(table, "Heading Deg", 0.0);

    /*
     * Constructor
     */
    public Infrastructure(final TObj argParent) {
        super(argParent, "Infrastructure");
    }

    /*
     * Getters
     */
    public RobotMode getRobotMode() {
        return robotMode;
    }

    public IMUPigeon2 getIMU() {
        return imu;
    }

    /*
     * Lifecycle
     */
    @Override
    public void robotInit(final double argTimestamp) {
        imu = new IMUPigeon2(this, "Pigeon2", Constants.CAN.PIGEON2, 250.0);
        imu.robotInit(argTimestamp);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        if (imu != null) {
            imu.robotPeriodicBefore(argTimestamp);
        }
    }

    @Override
    public void disabledInit(final double argTimestamp) {
        robotMode = RobotMode.DISABLED;
    }

    @Override
    public void autonomousInit(final double argTimestamp) {
        robotMode = RobotMode.AUTONOMOUS;
    }

    @Override
    public void teleopInit(final double argTimestamp) {
        robotMode = RobotMode.TELEOP;
    }

    @Override
    public void testInit(final double argTimestamp) {
        robotMode = RobotMode.TEST;
    }

    /*
     * Simulation
     */
    @Override
    public void simulationInit(final double argTimestamp) {
        if (imu != null) {
            imu.simulationInit(argTimestamp);
        }
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        if (imu != null) {
            imu.simulationPeriodic(argTimestamp);
        }
    }

    /*
     * Telemetry
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntRobotMode.publish(robotMode.ordinal());
        ntBatteryVoltage.publish(RobotController.getBatteryVoltage());

        if (imu != null) {
            ntHeadingDeg.publish(imu.getHeading().getDegrees());
            imu.outputTelemetry();
        }
    }
}
