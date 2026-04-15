package com.team271.lib.hardware.controllers;

import com.team271.lib.control.PIDGains;

/**
 * Extended motor controller interface for controllers with onboard closed-loop control.
 *
 * <p>Adds current limiting, voltage limiting, ramp rates, PID slot configuration, gravity
 * compensation, continuous wrap, software limits, and closed-loop position/velocity control.
 *
 * <p>Implementations: {@link ControllerTalonFX} (CTRE TalonFX/Kraken), future REV SparkMax/Flex.
 *
 * <h3>PID Slot Model</h3>
 *
 * <p>Supports up to 3 independent PID + feedforward gain slots (0, 1, 2). Each slot stores kP, kI,
 * kD, kV, kS, kG, and kA. The active slot is selected at runtime when calling closed-loop output
 * methods (e.g., {@link #setOutputPosition(double, int, double)}).
 *
 * <h3>Gravity Compensation</h3>
 *
 * <p>The gravity feedforward (kG) applies differently based on {@link GravityType}:
 *
 * <ul>
 *   <li>{@link GravityType#ARM_COSINE} — output = kG * cos(position). For pivots/arms.
 *   <li>{@link GravityType#ELEVATOR_STATIC} — output = kG (constant). For elevators.
 * </ul>
 *
 * <h3>Continuous Wrap</h3>
 *
 * <p>When enabled, the closed-loop error wraps around the position range (e.g., for swerve azimuth
 * or turrets). The motor takes the shortest path to the target position.
 */
public interface SmartMotorController extends MotorController {

    /* --- Current Limits --- */

    /**
     * Applies a unified current limit configuration.
     *
     * @see CurrentLimitConfig
     */
    void setCurrentLimit(CurrentLimitConfig config);

    /** Returns the current limit configuration. */
    CurrentLimitConfig getCurrentLimitConfig();

    /* --- Individual Current Limit Methods (fine-grained control) --- */

    /** Configures stator current limit. */
    void setCurrentLimitStator(boolean argEnable, double argStatorCurrent);

    boolean getCurrentLimitStatorEnable();

    double getCurrentLimitStator();

    /** Configures supply current limit (simple). */
    void setCurrentLimitSupply(boolean argEnable, double argSupplyCurrent);

    /** Configures supply current limit with time-based lower limit. */
    void setCurrentLimitSupply(
            double argSupplyCurrentLimit, double argTime, double argSupplyCurrentLowerLimit);

    boolean getCurrentLimitSupplyEnable();

    double getCurrentLimitSupply();

    double getCurrentLimitSupplyTime();

    double getCurrentLimitSupplyLowerLimit();

    /* --- Voltage Limits --- */

    /**
     * Sets peak forward and reverse voltage limits.
     *
     * @param argFwdVoltage peak forward voltage
     * @param argRevVoltage peak reverse voltage (typically negative)
     * @param argTimeFilter voltage time constant filter
     */
    void setVoltagePeak(double argFwdVoltage, double argRevVoltage, double argTimeFilter);

    double getVoltagePeakFwd();

    double getVoltagePeakRev();

    double getVoltagePeakTime();

    /* --- Ramp Rates --- */

    void setRampOpenLoopDuty(double argRampRateSec);

    double getRampOpenLoopDuty();

    void setRampOpenLoopVoltage(double argRampRateSec);

    double getRampOpenLoopVoltage();

    void setRampClosedLoopDuty(double argRampRateSec);

    double getRampClosedLoopDuty();

    void setRampClosedLoopVoltage(double argRampRateSec);

    double getRampClosedLoopVoltage();

    /* --- PID Gains (per-slot) --- */

    /**
     * Sets all PID + feedforward gains for the specified slot.
     *
     * @param argSlot PID slot index (0, 1, or 2)
     * @param gains the complete gain set including kP, kI, kD, kV, kS, kG, kA
     */
    void setPIDGains(int argSlot, PIDGains gains);

    /**
     * Returns the PID + feedforward gains for the specified slot.
     *
     * @param argSlot PID slot index (0, 1, or 2)
     */
    PIDGains getPIDGains(int argSlot);

    /** Sets individual PID gains per slot. */
    void setPSlot(int argSlot, double argSetP);

    double getPSlot(int argSlot);

    void setISlot(int argSlot, double argSetI);

    double getISlot(int argSlot);

    void setDSlot(int argSlot, double argSetD);

    double getDSlot(int argSlot);

    double getVSlot(int argSlot);

    double getSSlot(int argSlot);

    /** Sets P, I, D, V, S gains for a slot (legacy method — prefer {@link #setPIDGains}). */
    void setPIDFSlot(int argSlot, double argP, double argI, double argD, double argV, double argS);

    /* --- Gravity Compensation --- */

    /**
     * Sets the gravity compensation type for the specified PID slot.
     *
     * <p>This determines how the kG feedforward gain is applied:
     *
     * <ul>
     *   <li>{@link GravityType#ARM_COSINE} — kG * cos(position), for arms/pivots
     *   <li>{@link GravityType#ELEVATOR_STATIC} — kG (constant), for elevators
     * </ul>
     *
     * @param argSlot PID slot index (0, 1, or 2)
     * @param argType the gravity compensation type
     */
    void setGravityType(int argSlot, GravityType argType);

    /** Returns the gravity type for the specified slot. */
    GravityType getGravityType(int argSlot);

    /* --- Continuous Wrap --- */

    /**
     * Enables or disables continuous wrap for closed-loop control.
     *
     * <p>When enabled, the closed-loop controller wraps error at the discontinuity point (e.g.,
     * 0/360 degrees), taking the shortest path. Essential for swerve azimuth and turrets.
     *
     * @param argEnabled true to enable continuous wrap
     */
    void setContinuousWrap(boolean argEnabled);

    /** Returns true if continuous wrap is enabled. */
    boolean getContinuousWrap();

    /* --- Software Limits --- */

    /**
     * Configures forward software position limit.
     *
     * @param argEnable true to enable the limit
     * @param argLimit position limit in rotor rotations
     */
    void configSoftLimitForward(boolean argEnable, double argLimit);

    /**
     * Configures reverse software position limit.
     *
     * @param argEnable true to enable the limit
     * @param argLimit position limit in rotor rotations
     */
    void configSoftLimitReverse(boolean argEnable, double argLimit);

    /* --- Tolerance --- */

    void setTolerance(double argTolerance);

    double getTolerance();

    /* --- Closed-Loop Output --- */

    /** Returns the current closed-loop error. */
    double getCLError();

    /** Returns the current closed-loop output. */
    double getCLOutput();

    /**
     * Commands a closed-loop position setpoint.
     *
     * @param argPositionRot target position in rotor rotations
     * @param argSlot PID slot to use (0, 1, or 2)
     * @param argFFVolt feedforward voltage
     */
    void setOutputPosition(double argPositionRot, int argSlot, double argFFVolt);

    /** Commands a closed-loop position setpoint using slot 0. */
    default void setOutputPosition(final double argPositionRot, final double argFFVolt) {
        setOutputPosition(argPositionRot, 0, argFFVolt);
    }

    /**
     * Commands a closed-loop velocity setpoint.
     *
     * @param argRPS target velocity in rotations per second
     * @param argSlot PID slot to use (0, 1, or 2)
     * @param argFFVolt feedforward voltage
     */
    void setOutputVelocity(double argRPS, int argSlot, double argFFVolt);

    /** Commands a closed-loop velocity setpoint using slot 0. */
    default void setOutputVelocity(final double argRPS, final double argFFVolt) {
        setOutputVelocity(argRPS, 0, argFFVolt);
    }
}
