package com.team271.lib.hardware.controllers;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerBase.ControllerStatus;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.motors.MotorBase;

/**
 * Core motor controller interface for any motor controller vendor (CTRE, REV, WPILib).
 *
 * <p>Defines the minimal contract for controlling a motor: identity, open-loop output, neutral
 * mode, direction, following, and simulation. Implementations wrap vendor-specific hardware
 * (TalonFX, SparkMax, etc.) while exposing the underlying object via typed passthrough getters.
 *
 * <p>For closed-loop control, current limiting, and PID, see {@link SmartMotorController}.
 */
public interface MotorController extends Lifecycle, Named {

    /* --- Identity --- */

    /** Returns the CAN device ID. */
    CANDeviceID getID();

    /** Returns true if the controller is communicating on the CAN bus. */
    boolean isConnected();

    /** Returns true if the controller's configuration has been successfully applied. */
    boolean isConfigured();

    /** Returns the motor model associated with this controller. */
    MotorBase getMotor();

    /* --- Configuration --- */

    /** Applies the current configuration to the hardware device. */
    ControllerStatus applyConfig();

    /** Sets the neutral mode (brake or coast). */
    void setNeutralMode(NeutralState argNeutralState);

    /** Returns the current neutral mode. */
    NeutralState getNeutralMode();

    /** Sets the motor rotation direction. */
    void setDirection(MotorDirection argDirection);

    /** Returns the current motor rotation direction. */
    MotorDirection getDirection();

    /* --- Open-Loop Output --- */

    /** Commands the motor to stop (applies neutral output). */
    void stop();

    /** Returns the current duty cycle output [-1, 1]. */
    double getOutputDuty();

    /** Sets duty cycle output [-1, 1]. */
    void setOutputDuty(double argOutDuty);

    /** Returns the current voltage output. */
    double getOutputVoltage();

    /** Sets voltage output. */
    void setOutputVoltage(double argOutputVolts);

    /* --- Following --- */

    /**
     * Configures this controller to follow the specified leader.
     *
     * <p>Leader and follower must be on the same CAN bus.
     *
     * @param argLeader the leader controller
     * @param argOpposeLeader true to run in the opposite direction of the leader
     * @return status indicating success or bus mismatch
     */
    ControllerStatus follow(ControllerBase argLeader, boolean argOpposeLeader);

    /* --- Simulation --- */

    /** Sets the simulated velocity in rotations per second. */
    void setSimVelRotations(double argVelRotations);

    /** Sets the simulated position in rotations. */
    void setSimPosRotations(double argPositionRotations);
}
