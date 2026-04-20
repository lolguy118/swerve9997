package com.team271.lib.api.motor;

/**
 * Runtime capability query for motor controllers.
 *
 * <p>Allows code to branch based on what a motor supports without casting to a vendor-specific
 * type. For example, checking {@link #supportsMotionMagic()} before attempting a Motion Magic
 * command.
 */
public interface MotorCapabilities {

    /** Returns true if the motor supports Field-Oriented Control. */
    boolean supportsFOC();

    /** Returns true if the motor supports CAN timesync (CANivore synchronization). */
    boolean supportsTimesync();

    /** Returns true if the motor supports Motion Magic profiled position/velocity control. */
    boolean supportsMotionMagic();

    /** Returns true if the motor supports torque current (FOC) control mode. */
    boolean supportsTorqueCurrentControl();

    /** Returns true if the motor supports stator (rotor) current limiting. */
    boolean supportsStatorCurrentLimit();

    /** Returns the maximum number of PID gain slots (e.g., 3 for CTRE, 4 for REV). */
    int maxPIDSlots();
}
