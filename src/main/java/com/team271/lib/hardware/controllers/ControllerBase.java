package com.team271.lib.hardware.controllers;

import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.NTEntry;

public abstract class ControllerBase extends TObj {
    public enum ControllerStatus {
        UNKNOWN,
        ERROR,
        ERROR_INVALID_BUS,
        OK
    }

    public enum ControllerType {
        TALONFX,
        TALONFXS,
        SPARK_MAX,
        SPARK_FLEX
    }

    public enum NeutralState {
        NONE,
        BRAKE,
        COAST
    }

    public enum MotorDirection {
        CW,
        CCW
    }

    protected ControllerStatus status = ControllerStatus.OK;
    protected final ControllerType controllerType;
    protected final CANDeviceID deviceID;
    protected boolean isConnected = false;
    protected boolean isConfigured = false;

    protected CANDeviceID followingID;

    protected final MotorBase motor;

    protected boolean opposeLeader = false;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntControllerStatus = new NTEntry(table, "Controller Status", "");
    final NTEntry ntTypeController = new NTEntry(table, "Controller Type", "");
    final NTEntry ntCANBus = new NTEntry(table, "CAN Bus", "");
    final NTEntry ntCANID = new NTEntry(table, "CAN ID", 0);

    final NTEntry ntIsConnected = new NTEntry(table, "Is Connected", false);
    final NTEntry ntIsConfigured = new NTEntry(table, "Is Configured", false);

    final NTEntry ntTypeMotor = new NTEntry(table, "Motor Type", "");

    final NTEntry ntFollowingCANBus = new NTEntry(table, "Following CAN Bus", "");
    final NTEntry ntFollowingCANID = new NTEntry(table, "Following CAN ID", 0);

    final NTEntry ntNeutralMode = new NTEntry(table, "Neutral Mode", "");
    final NTEntry ntDirection = new NTEntry(table, "Direction", "");
    final NTEntry ntOpposeLeader = new NTEntry(table, "Oppose Leader", false);

    /*
     *
     * Constructors
     *
     */
    protected ControllerBase(
            final TObj argParent,
            final String argName,
            final ControllerType argControllerType,
            final CANDeviceID argID,
            final MotorBase argMotor) {
        super(argParent, "(Controller)" + argName);

        controllerType = argControllerType;

        /*
         * Store Device ID and Bus
         */
        deviceID = argID;

        /*
         * Store Motor
         */
        motor = argMotor;
    }

    /*
     *
     * Core
     *
     */
    protected abstract void create();

    public final boolean isConnected() {
        return isConnected;
    }

    public final boolean isConfigured() {
        return isConfigured;
    }

    public MotorBase getMotor() {
        return motor;
    }

    /*
     * DeviceID
     */
    public final boolean isDevice(final CANDeviceID argID) {
        return deviceID.equals(argID);
    }

    public final CANDeviceID getID() {
        return deviceID;
    }

    public final int getIDNum() {
        return deviceID.getDeviceNumber();
    }

    public final String getBus() {
        return deviceID.getBus();
    }

    /*
     * Following DeviceID
     */
    public final CANDeviceID getFollowingID() {
        return followingID;
    }

    public final int getFollowingIDNum() {
        return followingID.getDeviceNumber();
    }

    public final String getFollowingBus() {
        return followingID.getBus();
    }

    public ControllerStatus follow(final ControllerBase argLeader, final boolean argOpposeLeader) {
        /*
         * Set as Follower
         * Follower and Leader Must be on the same bus
         */
        if (deviceID.isSameBus(argLeader.getID())) {
            opposeLeader = argOpposeLeader;

            followingID = argLeader.getID();
        } else {
            return ControllerStatus.ERROR_INVALID_BUS;
        }

        return ControllerStatus.OK;
    }

    public boolean getOpposeLeader() {
        return opposeLeader;
    }

    /*
     *
     * Config
     *
     */
    public abstract ControllerStatus applyConfig();

    /* Neutral Mode */
    public abstract void setNeutralMode(final NeutralState argNeutralState);

    public abstract NeutralState getNeutralMode();

    /* Direction */
    public abstract void setDirection(final MotorDirection argDirection);

    public abstract MotorDirection getDirection();

    /*
     *
     * Outputs
     *
     */
    public abstract void stop();

    /*
     * Open Loop
     */
    /* Duty Cycle */
    public abstract double getOutputDuty();

    public abstract void setOutputDuty(final double argOutDuty);

    /* Voltage */
    public abstract double getOutputVoltage();

    public abstract void setOutputVoltage(final double argOutputVolts);

    /*
     *
     * Robot Loops
     *
     */
    public void robotPeriodicBefore(final double argTimestamp) {
        // Unused
    }

    public void robotPeriodicAfter(final double argTimestamp) {
        // Unused
    }

    /*
     *
     * Simulation
     *
     */
    public abstract void setSimVelRotations(final double argVelRotations);

    public abstract void setSimPosRotations(final double argPositionRotations);

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        switch (status) {
            case UNKNOWN:
                ntControllerStatus.publish("UNKNOWN");
                break;
            case ERROR:
                ntControllerStatus.publish("ERROR");
                break;
            case ERROR_INVALID_BUS:
                ntControllerStatus.publish("ERROR_INVALID_BUS");
                break;
            case OK:
                ntControllerStatus.publish("OK");
                break;
            default:
                ntControllerStatus.publish(ConstantsLib.S_INVALID);
                break;
        }

        switch (controllerType) {
            case TALONFX:
                ntTypeController.publish("TalonFX");
                break;
            case SPARK_MAX:
                ntTypeController.publish("Spark Max");
                break;
            case SPARK_FLEX:
                ntTypeController.publish("Spark Flex");
                break;
            default:
                ntTypeController.publish(ConstantsLib.S_INVALID);
                break;
        }

        ntCANBus.publish(deviceID.getBus());
        ntCANID.publish(deviceID.getDeviceNumber());

        ntIsConnected.publish(isConnected());
        ntIsConfigured.publish(isConfigured());

        if (motor != null) {
            ntTypeMotor.publish(motor.getMotorName());
        }

        if (followingID != null) {
            ntFollowingCANBus.publish(followingID.getBus());
            ntFollowingCANID.publish(followingID.getDeviceNumber());
        }

        switch (getNeutralMode()) {
            case BRAKE:
                ntNeutralMode.publish("Brake");
                break;
            case COAST:
                ntNeutralMode.publish("Coast");
                break;
            default:
                ntNeutralMode.publish(ConstantsLib.S_INVALID);
                break;
        }

        switch (getDirection()) {
            case CW:
                ntDirection.publish("CW");
                break;
            case CCW:
                ntDirection.publish("CCW");
                break;
            default:
                ntDirection.publish(ConstantsLib.S_INVALID);
                break;
        }

        ntOpposeLeader.publish(opposeLeader);
    }
}
