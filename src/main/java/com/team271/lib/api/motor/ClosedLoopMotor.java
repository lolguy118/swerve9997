package com.team271.lib.api.motor;

import com.team271.lib.control.PIDGains;
import com.team271.lib.hardware.controllers.CurrentLimitConfig;
import com.team271.lib.hardware.controllers.GravityType;

/**
 * Vendor-neutral motor controller interface with onboard closed-loop control.
 *
 * <p>Extends {@link Motor} with current limiting, voltage limiting, ramp rates, PID slot
 * configuration, gravity compensation, continuous wrap, software limits, and closed-loop
 * position/velocity control.
 *
 * <h2>PID Slot Model</h2>
 *
 * <p>Supports up to 3 independent PID + feedforward gain slots (0, 1, 2). Each slot stores kP, kI,
 * kD, kV, kS, kG, and kA. The active slot is selected at runtime when calling closed-loop output
 * methods.
 *
 * <h2>Gravity Compensation</h2>
 *
 * <ul>
 *   <li>{@link GravityType#ARM_COSINE} — output = kG * cos(position). For pivots/arms.
 *   <li>{@link GravityType#ELEVATOR_STATIC} — output = kG (constant). For elevators.
 * </ul>
 *
 * <h2>Continuous Wrap</h2>
 *
 * <p>When enabled, the closed-loop error wraps at the discontinuity point, taking the shortest path
 * to the target. Essential for swerve azimuth and turrets.
 */
public interface ClosedLoopMotor extends Motor {

    /* Current Limits */

    /** Applies a unified current limit configuration. */
    void setCurrentLimit(CurrentLimitConfig argConfig);

    /** Returns the current limit configuration. */
    CurrentLimitConfig getCurrentLimitConfig();

    /** Configures stator (rotor) current limit. */
    void setStatorCurrentLimit(boolean argEnable, double argAmps);

    /** Configures supply (battery) current limit. */
    void setSupplyCurrentLimit(boolean argEnable, double argAmps);

    /** Configures supply current limit with time-based lower limit. */
    void setSupplyCurrentLimit(double argLimit, double argTimeSec, double argLowerLimit);

    /* Voltage Limits */

    /**
     * Sets peak forward and reverse voltage limits.
     *
     * @param argFwdVolts peak forward voltage
     * @param argRevVolts peak reverse voltage (typically negative)
     * @param argTimeConstant voltage time constant filter
     */
    void setVoltagePeak(double argFwdVolts, double argRevVolts, double argTimeConstant);

    /* Ramp Rates */

    /** Sets the open-loop duty cycle ramp period in seconds. */
    void setOpenLoopRampDuty(double argSec);

    /** Sets the open-loop voltage ramp period in seconds. */
    void setOpenLoopRampVoltage(double argSec);

    /** Sets the closed-loop duty cycle ramp period in seconds. */
    void setClosedLoopRampDuty(double argSec);

    /** Sets the closed-loop voltage ramp period in seconds. */
    void setClosedLoopRampVoltage(double argSec);

    /* PID Gains (per-slot) */

    /**
     * Sets all PID + feedforward gains for the specified slot.
     *
     * @param argSlot PID slot index (0, 1, or 2)
     * @param argGains the complete gain set
     */
    void setGains(int argSlot, PIDGains argGains);

    /** Returns the PID + feedforward gains for the specified slot. */
    PIDGains getGains(int argSlot);

    /** Sets P, I, D, V, S gains for a slot. */
    void setPIDFSlot(int argSlot, double argP, double argI, double argD, double argV, double argS);

    /** Sets the proportional gain for the specified slot. */
    void setPSlot(int argSlot, double argKP);

    /** Returns the proportional gain for the specified slot. */
    double getPSlot(int argSlot);

    /** Sets the integral gain for the specified slot. */
    void setISlot(int argSlot, double argKI);

    /** Returns the integral gain for the specified slot. */
    double getISlot(int argSlot);

    /** Sets the derivative gain for the specified slot. */
    void setDSlot(int argSlot, double argKD);

    /** Returns the derivative gain for the specified slot. */
    double getDSlot(int argSlot);

    /** Returns the velocity feedforward gain (kV) for the specified slot. */
    double getVSlot(int argSlot);

    /** Returns the static feedforward gain (kS) for the specified slot. */
    double getSSlot(int argSlot);

    /* Gravity Compensation */

    /** Sets the gravity compensation type for the specified PID slot. */
    void setGravityType(int argSlot, GravityType argType);

    /** Returns the gravity compensation type for the specified slot. */
    GravityType getGravityType(int argSlot);

    /** Sets the gravity feedforward gain (kG) for the specified slot. */
    void setGravityGain(int argSlot, double argKG);

    /** Returns the gravity feedforward gain (kG) for the specified slot. */
    double getGravityGain(int argSlot);

    /** Sets the acceleration feedforward gain (kA) for the specified slot. */
    void setAccelGain(int argSlot, double argKA);

    /** Returns the acceleration feedforward gain (kA) for the specified slot. */
    double getAccelGain(int argSlot);

    /* Continuous Wrap */

    /** Enables or disables continuous wrap for closed-loop error calculation. */
    void setContinuousWrap(boolean argEnabled);

    /** Returns true if continuous wrap is enabled. */
    boolean getContinuousWrap();

    /* Software Limits */

    /** Configures the forward software position limit. */
    void configSoftLimitForward(boolean argEnable, double argLimit);

    /** Configures the reverse software position limit. */
    void configSoftLimitReverse(boolean argEnable, double argLimit);

    /* Tolerance */

    /** Sets the position tolerance for closed-loop at-setpoint checks. */
    void setTolerance(double argTolerance);

    /** Returns the position tolerance. */
    double getTolerance();

    /* Closed-Loop Output */

    /** Returns the current closed-loop error. */
    double getCLError();

    /** Returns the current closed-loop output. */
    double getCLOutput();

    /**
     * Commands a closed-loop position setpoint.
     *
     * @param argRotations target position in rotor rotations
     * @param argSlot PID slot to use (0, 1, or 2)
     * @param argFFVolts feedforward voltage
     */
    void setOutputPosition(double argRotations, int argSlot, double argFFVolts);

    /** Commands a closed-loop position setpoint using slot 0. */
    default void setOutputPosition(final double argRotations, final double argFFVolts) {
        setOutputPosition(argRotations, 0, argFFVolts);
    }

    /**
     * Commands a closed-loop velocity setpoint.
     *
     * @param argRPS target velocity in rotations per second
     * @param argSlot PID slot to use (0, 1, or 2)
     * @param argFFVolts feedforward voltage
     */
    void setOutputVelocity(double argRPS, int argSlot, double argFFVolts);

    /** Commands a closed-loop velocity setpoint using slot 0. */
    default void setOutputVelocity(final double argRPS, final double argFFVolts) {
        setOutputVelocity(argRPS, 0, argFFVolts);
    }

    /* Config */

    /** Applies the current configuration to the hardware device. */
    FollowStatus applyConfig();

    /* Capabilities */

    /** Returns the capability set for this motor controller. */
    MotorCapabilities capabilities();
}
