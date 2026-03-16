package com.team271.lib.sysid;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Threads;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.Objects;

public class Logger {
    /*
     * The initial size of the data collection vectors, set to be large enough so
     * that we avoid resizing the vector during data collection. Determined by: 20
     * seconds of test data * 200 samples/second * 9 doubles/sample(320kB of
     * reserved data).
     */
    protected static final int DATA_VECTOR_SIZE = 36000;

    /*
     * The commanded motor voltage. Either as a rate (V/s) for the quasistatic
     * test or as a voltage (V) for the dynamic test.
     */
    protected double voltageCommand = 0.0;

    /*
     * The voltage that the motors should be set to.
     */
    protected double motorVoltage = 0.0;

    /*
     * The timestamp of when the test starts. Mainly used to keep track of the
     * test running for too long.
     */
    protected double startTime = 0.0;

    /*
     * Determines for Drivetrain tests if the robot should be spinning (value sent
     * via NT).
     */
    protected boolean rotate = false;

    /*
     * The test that is running (e.g. Quasistatic or Dynamic).
     */
    protected String testType;

    /*
     * The mechanism that is being characterized (sent via NT).
     */
    protected String mechanism;

    /*
     * Stores all the collected data.
     */
    protected ArrayList<Double> data = new ArrayList<>(DATA_VECTOR_SIZE);

    protected int ackNum = 0;

    protected static final int THREAD_PRIORITY = 15;
    protected static final int HAL_THREAD_PRIORITY = 40;

    public Logger() {
        org.littletonrobotics.junction.Logger.recordOutput("SysId/VoltageCommand", 0.0);
        org.littletonrobotics.junction.Logger.recordOutput("SysId/TestType", "");
        org.littletonrobotics.junction.Logger.recordOutput("SysId/Test", "");
        org.littletonrobotics.junction.Logger.recordOutput("SysId/Rotate", false);
        org.littletonrobotics.junction.Logger.recordOutput("SysId/Overflow", false);
        org.littletonrobotics.junction.Logger.recordOutput("SysId/WrongMech", false);
        org.littletonrobotics.junction.Logger.recordOutput("SysId/AckNumber", ackNum);
    }

    public void initLogger(final double argTimestamp) {
        reset();

        mechanism = SmartDashboard.getString("SysIdTest", "");
        if (!mechanism.equals("")) {
            org.littletonrobotics.junction.Logger.recordOutput(
                    "SysId/WrongMech", isWrongMechanism());
        }

        testType = SmartDashboard.getString("SysIdTestType", "");
        rotate = SmartDashboard.getBoolean("SysIdRotate", false);
        voltageCommand = SmartDashboard.getNumber("SysIdVoltageCommand", 0.0);
        org.littletonrobotics.junction.Logger.recordOutput("SysId/Telemetry", "");
        ackNum = (int) SmartDashboard.getNumber("SysIdAckNumber", 0);

        startTime = argTimestamp;
    }

    public void sendData() {
        org.littletonrobotics.junction.Logger.recordOutput(
                "SysId/Overflow", data.size() >= DATA_VECTOR_SIZE);

        StringBuilder ss = new StringBuilder();

        for (int i = 0; i < data.size(); ++i) {
            ss.append(data.get(i));
            if (i < data.size() - 1) {
                ss.append(",");
            }
        }

        String type = Objects.equals(testType, "Dynamic") ? "fast" : "slow";
        String direction = voltageCommand > 0 ? "forward" : "backward";
        String test = String.format("%s-%s", type, direction);

        org.littletonrobotics.junction.Logger.recordOutput(
                "SysId/Telemetry", String.format("%s;%s", test, ss.toString()));
        org.littletonrobotics.junction.Logger.recordOutput("SysId/AckNumber", ++ackNum);
    }

    public void clearWhenReceived() {
        if (SmartDashboard.getNumber("SysIdAckNumber", 0.0) > ackNum) {
            org.littletonrobotics.junction.Logger.recordOutput("SysId/Telemetry", "");
            ackNum = (int) SmartDashboard.getNumber("SysIdAckNumber", 0.0);
        }
    }

    public void updateThreadPriority() {
        if (!RobotBase.isSimulation()) {
            if (!Notifier.setHALThreadPriority(true, HAL_THREAD_PRIORITY)
                    || !Threads.setCurrentThreadPriority(true, THREAD_PRIORITY)) {
                throw new IllegalArgumentException("Setting the RT Priority failed");
            }
        }
    }

    public void updateData(final double argTimestamp) {
        if (Objects.equals(testType, "Quasistatic")) {
            motorVoltage = voltageCommand * (argTimestamp - startTime);
        } else if (Objects.equals(testType, "Dynamic")) {
            motorVoltage = voltageCommand;
        } else {
            motorVoltage = 0.0;
        }
    }

    public void reset() {
        motorVoltage = 0.0;
        startTime = 0.0;
        data.clear();
    }

    public boolean isWrongMechanism() {
        return false;
    }
}
