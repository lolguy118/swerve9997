package com.team271.lib.api.motor;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;
import com.team271.lib.api.DeviceID;
import com.team271.lib.hardware.motors.MotorBase;

/**
 * Vendor-neutral motor controller interface for open-loop control.
 *
 * <p>Defines the minimal contract for any motor controller: identity, open-loop output, neutral
 * mode, direction, following, and simulation. Implementations wrap vendor-specific hardware (CTRE
 * TalonFX, future REV SparkMax, etc.) while exposing the underlying object via typed passthrough
 * getters.
 *
 * <p>For closed-loop control, current limiting, PID, and advanced features, see {@link
 * ClosedLoopMotor}.
 */
public interface Motor extends Lifecycle, Named {

    /* Identity */

    /** Returns the device ID (bus + device number). */
    DeviceID getDeviceID();

    /** Returns true if the controller is communicating on the bus. */
    boolean isConnected();

    /** Returns true if the controller's configuration has been successfully applied. */
    boolean isConfigured();

    /** Returns the motor model associated with this controller (for simulation). */
    MotorBase getMotorModel();

    /* Open-Loop Output */

    /** Commands the motor to stop (applies neutral output). */
    void stop();

    /**
     * Sets duty cycle output.
     *
     * @param argPercent duty cycle [-1, 1]
     */
    void setDutyCycle(double argPercent);

    /** Returns the current duty cycle output [-1, 1]. */
    double getDutyCycle();

    /**
     * Sets voltage output.
     *
     * @param argVolts output voltage
     */
    void setVoltage(double argVolts);

    /** Returns the current voltage output. */
    double getVoltage();

    /* Configuration */

    /** Sets the neutral mode (brake or coast). */
    void setNeutralMode(NeutralMode argMode);

    /** Returns the current neutral mode. */
    NeutralMode getNeutralMode();

    /**
     * Sets whether the motor output is inverted.
     *
     * @param argInverted true to invert motor output direction
     */
    void setInverted(boolean argInverted);

    /** Returns true if the motor output is inverted. */
    boolean isInverted();

    /* Following */

    /**
     * Configures this controller to follow the specified leader.
     *
     * <p>Leader and follower must be on the same CAN bus.
     *
     * @param argLeader the leader motor
     * @param argOppose true to run in the opposite direction of the leader
     * @return status indicating success or failure reason
     */
    FollowStatus follow(Motor argLeader, boolean argOppose);

    /* Simulation */

    /** Sets the simulated velocity in rotations per second. */
    void setSimVelocity(double argRotationsPerSec);

    /** Sets the simulated position in rotations. */
    void setSimPosition(double argRotations);
}
