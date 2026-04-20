package com.team271.lib.vendor.ctre;

import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DynamicMotionMagicDutyCycle;
import com.ctre.phoenix6.controls.DynamicMotionMagicExpoDutyCycle;
import com.ctre.phoenix6.controls.DynamicMotionMagicExpoTorqueCurrentFOC;
import com.ctre.phoenix6.controls.DynamicMotionMagicExpoVoltage;
import com.ctre.phoenix6.controls.DynamicMotionMagicTorqueCurrentFOC;
import com.ctre.phoenix6.controls.DynamicMotionMagicVoltage;
import com.ctre.phoenix6.controls.MotionMagicDutyCycle;
import com.ctre.phoenix6.controls.MotionMagicExpoDutyCycle;
import com.ctre.phoenix6.controls.MotionMagicExpoTorqueCurrentFOC;
import com.ctre.phoenix6.controls.MotionMagicExpoVoltage;
import com.ctre.phoenix6.controls.MotionMagicTorqueCurrentFOC;
import com.ctre.phoenix6.controls.MotionMagicVelocityDutyCycle;
import com.ctre.phoenix6.controls.MotionMagicVelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.MotionMagicVelocityVoltage;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionDutyCycle;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityDutyCycle;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.team271.lib.api.DeviceID;
import com.team271.lib.api.motor.ClosedLoopMotor;
import com.team271.lib.api.motor.FollowStatus;
import com.team271.lib.api.motor.Motor;
import com.team271.lib.api.motor.MotorCapabilities;
import com.team271.lib.api.motor.NeutralMode;
import com.team271.lib.control.PIDGains;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerBase;
import com.team271.lib.hardware.controllers.ControllerBase.ControllerStatus;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.controllers.CurrentLimitConfig;
import com.team271.lib.hardware.controllers.GravityType;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.NTTable;

/**
 * CTRE TalonFX implementation of the vendor-neutral {@link ClosedLoopMotor} interface.
 *
 * <p>Wraps {@link ControllerTalonFX} and exposes the full CTRE feature set through both the
 * vendor-neutral interface and passthrough getters.
 *
 * <h3>Passthrough Access</h3>
 *
 * <pre>
 * CTREMotor motor = ...;
 * motor.getTalonFX();       // raw CTRE TalonFX
 * motor.getConfig();        // TalonFXConfiguration
 * motor.getSimState();      // TalonFXSimState
 * motor.getController();    // underlying ControllerTalonFX
 * </pre>
 */
public class CTREMotor implements ClosedLoopMotor, MotorCapabilities {

    /*
     * Controller
     */
    private final ControllerTalonFX mController;

    /*
     * Closed Loop Position Variants (timesync enabled)
     */
    private final PositionDutyCycle mPositionDuty =
            new PositionDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final PositionTorqueCurrentFOC mPositionTC =
            new PositionTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /*
     * Closed Loop Velocity Variants (timesync enabled)
     */
    private final VelocityDutyCycle mVelocityDuty =
            new VelocityDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final VelocityTorqueCurrentFOC mVelocityTC =
            new VelocityTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /*
     * Motion Magic Position (timesync enabled)
     */
    private final MotionMagicDutyCycle mMMPositionDuty =
            new MotionMagicDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final MotionMagicVoltage mMMPositionVoltage =
            new MotionMagicVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final MotionMagicTorqueCurrentFOC mMMPositionTC =
            new MotionMagicTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /*
     * Motion Magic Velocity (timesync enabled)
     */
    private final MotionMagicVelocityDutyCycle mMMVelocityDuty =
            new MotionMagicVelocityDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final MotionMagicVelocityVoltage mMMVelocityVoltage =
            new MotionMagicVelocityVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final MotionMagicVelocityTorqueCurrentFOC mMMVelocityTC =
            new MotionMagicVelocityTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /*
     * Motion Magic Expo (timesync enabled)
     */
    private final MotionMagicExpoDutyCycle mMMExpoDuty =
            new MotionMagicExpoDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final MotionMagicExpoVoltage mMMExpoVoltage =
            new MotionMagicExpoVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    private final MotionMagicExpoTorqueCurrentFOC mMMExpoTC =
            new MotionMagicExpoTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /*
     * Dynamic Motion Magic (timesync enabled)
     */
    private final DynamicMotionMagicDutyCycle mDynMMDuty =
            new DynamicMotionMagicDutyCycle(0, 0, 0).withUseTimesync(true).withUpdateFreqHz(0);
    private final DynamicMotionMagicVoltage mDynMMVoltage =
            new DynamicMotionMagicVoltage(0, 0, 0).withUseTimesync(true).withUpdateFreqHz(0);
    private final DynamicMotionMagicTorqueCurrentFOC mDynMMTC =
            new DynamicMotionMagicTorqueCurrentFOC(0, 0, 0)
                    .withUseTimesync(true)
                    .withUpdateFreqHz(0);

    /*
     * Dynamic Motion Magic Expo (timesync enabled)
     */
    private final DynamicMotionMagicExpoDutyCycle mDynMMExpoDuty =
            new DynamicMotionMagicExpoDutyCycle(0, 0, 0).withUseTimesync(true).withUpdateFreqHz(0);
    private final DynamicMotionMagicExpoVoltage mDynMMExpoVoltage =
            new DynamicMotionMagicExpoVoltage(0, 0, 0).withUseTimesync(true).withUpdateFreqHz(0);
    private final DynamicMotionMagicExpoTorqueCurrentFOC mDynMMExpoTC =
            new DynamicMotionMagicExpoTorqueCurrentFOC(0, 0, 0)
                    .withUseTimesync(true)
                    .withUpdateFreqHz(0);

    /*
     * Constructor
     */
    public CTREMotor(final ControllerTalonFX argController) {
        mController = argController;
    }

    /*
     * Passthrough
     */

    /** Returns the underlying ControllerTalonFX for full library-level access. */
    public ControllerTalonFX getController() {
        return mController;
    }

    /** Returns the raw CTRE TalonFX device. */
    public TalonFX getTalonFX() {
        return mController.getTalonFX();
    }

    /** Returns the CTRE TalonFX configuration object. */
    public TalonFXConfiguration getConfig() {
        return mController.getConfig();
    }

    /** Returns the CTRE simulation state. */
    public TalonFXSimState getSimState() {
        return mController.getSimState();
    }

    /** Returns the Motion Magic configuration. */
    public MotionMagicConfigs getConfigMM() {
        return mController.getConfigMM();
    }

    /*
     *
     * Motor Interface
     *
     */

    @Override
    public DeviceID getDeviceID() {
        final CANDeviceID id = mController.getID();
        return new DeviceID(id.getBus(), id.getDeviceNumber());
    }

    @Override
    public boolean isConnected() {
        return mController.isConnected();
    }

    @Override
    public boolean isConfigured() {
        return mController.isConfigured();
    }

    @Override
    public MotorBase getMotorModel() {
        return mController.getMotor();
    }

    @Override
    public void stop() {
        mController.stop();
    }

    @Override
    public void setDutyCycle(final double argPercent) {
        mController.setOutputDuty(argPercent);
    }

    @Override
    public double getDutyCycle() {
        return mController.getOutputDuty();
    }

    @Override
    public void setVoltage(final double argVolts) {
        mController.setOutputVoltage(argVolts);
    }

    @Override
    public double getVoltage() {
        return mController.getOutputVoltage();
    }

    @Override
    public void setNeutralMode(final NeutralMode argMode) {
        mController.setNeutralMode(
                argMode == NeutralMode.BRAKE ? NeutralState.BRAKE : NeutralState.COAST);
    }

    @Override
    public NeutralMode getNeutralMode() {
        return mController.getNeutralMode() == NeutralState.BRAKE
                ? NeutralMode.BRAKE
                : NeutralMode.COAST;
    }

    @Override
    public void setInverted(final boolean argInverted) {
        mController.setDirection(argInverted ? MotorDirection.CCW : MotorDirection.CW);
    }

    @Override
    public boolean isInverted() {
        return mController.getDirection() == MotorDirection.CCW;
    }

    @Override
    public FollowStatus follow(final Motor argLeader, final boolean argOppose) {
        if (!(argLeader instanceof CTREMotor ctreLeader)) {
            return FollowStatus.ERROR;
        }
        final ControllerStatus status = mController.follow(ctreLeader.getController(), argOppose);
        return mapStatus(status);
    }

    @Override
    public void setSimVelocity(final double argRotationsPerSec) {
        mController.setSimVelRotations(argRotationsPerSec);
    }

    @Override
    public void setSimPosition(final double argRotations) {
        mController.setSimPosRotations(argRotations);
    }

    /*
     *
     * ClosedLoopMotor Interface
     *
     */

    /* Current Limits */

    @Override
    public void setCurrentLimit(final CurrentLimitConfig argConfig) {
        mController.setCurrentLimit(argConfig);
    }

    @Override
    public CurrentLimitConfig getCurrentLimitConfig() {
        return mController.getCurrentLimitConfig();
    }

    @Override
    public void setStatorCurrentLimit(final boolean argEnable, final double argAmps) {
        mController.setCurrentLimitStator(argEnable, argAmps);
    }

    @Override
    public void setSupplyCurrentLimit(final boolean argEnable, final double argAmps) {
        mController.setCurrentLimitSupply(argEnable, argAmps);
    }

    @Override
    public void setSupplyCurrentLimit(
            final double argLimit, final double argTimeSec, final double argLowerLimit) {
        mController.setCurrentLimitSupply(argLimit, argTimeSec, argLowerLimit);
    }

    /* Voltage Limits */

    @Override
    public void setVoltagePeak(
            final double argFwdVolts, final double argRevVolts, final double argTimeConstant) {
        mController.setVoltagePeak(argFwdVolts, argRevVolts, argTimeConstant);
    }

    /* Ramp Rates */

    @Override
    public void setOpenLoopRampDuty(final double argSec) {
        mController.setRampOpenLoopDuty(argSec);
    }

    @Override
    public void setOpenLoopRampVoltage(final double argSec) {
        mController.setRampOpenLoopVoltage(argSec);
    }

    @Override
    public void setClosedLoopRampDuty(final double argSec) {
        mController.setRampClosedLoopDuty(argSec);
    }

    @Override
    public void setClosedLoopRampVoltage(final double argSec) {
        mController.setRampClosedLoopVoltage(argSec);
    }

    /* PID Gains */

    @Override
    public void setGains(final int argSlot, final PIDGains argGains) {
        mController.setPIDGains(argSlot, argGains);
    }

    @Override
    public PIDGains getGains(final int argSlot) {
        return mController.getPIDGains(argSlot);
    }

    @Override
    public void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        mController.setPIDFSlot(argSlot, argP, argI, argD, argV, argS);
    }

    @Override
    public void setPSlot(final int argSlot, final double argKP) {
        mController.setPSlot(argSlot, argKP);
    }

    @Override
    public double getPSlot(final int argSlot) {
        return mController.getPSlot(argSlot);
    }

    @Override
    public void setISlot(final int argSlot, final double argKI) {
        mController.setISlot(argSlot, argKI);
    }

    @Override
    public double getISlot(final int argSlot) {
        return mController.getISlot(argSlot);
    }

    @Override
    public void setDSlot(final int argSlot, final double argKD) {
        mController.setDSlot(argSlot, argKD);
    }

    @Override
    public double getDSlot(final int argSlot) {
        return mController.getDSlot(argSlot);
    }

    @Override
    public double getVSlot(final int argSlot) {
        return mController.getVSlot(argSlot);
    }

    @Override
    public double getSSlot(final int argSlot) {
        return mController.getSSlot(argSlot);
    }

    /* Gravity Compensation */

    @Override
    public void setGravityType(final int argSlot, final GravityType argType) {
        mController.setGravityType(argSlot, argType);
    }

    @Override
    public GravityType getGravityType(final int argSlot) {
        return mController.getGravityType(argSlot);
    }

    @Override
    public void setGravityGain(final int argSlot, final double argKG) {
        mController.setGravityGain(argSlot, argKG);
    }

    @Override
    public double getGravityGain(final int argSlot) {
        return mController.getGravityGain(argSlot);
    }

    @Override
    public void setAccelGain(final int argSlot, final double argKA) {
        mController.setAccelGain(argSlot, argKA);
    }

    @Override
    public double getAccelGain(final int argSlot) {
        return mController.getAccelGain(argSlot);
    }

    /* Continuous Wrap */

    @Override
    public void setContinuousWrap(final boolean argEnabled) {
        mController.setContinuousWrap(argEnabled);
    }

    @Override
    public boolean getContinuousWrap() {
        return mController.getContinuousWrap();
    }

    /* Software Limits */

    @Override
    public void configSoftLimitForward(final boolean argEnable, final double argLimit) {
        mController.configSoftLimitForward(argEnable, argLimit);
    }

    @Override
    public void configSoftLimitReverse(final boolean argEnable, final double argLimit) {
        mController.configSoftLimitReverse(argEnable, argLimit);
    }

    /* Tolerance */

    @Override
    public void setTolerance(final double argTolerance) {
        mController.setTolerance(argTolerance);
    }

    @Override
    public double getTolerance() {
        return mController.getTolerance();
    }

    /* Closed-Loop Output */

    @Override
    public double getCLError() {
        return mController.getCLError();
    }

    @Override
    public double getCLOutput() {
        return mController.getCLOutput();
    }

    @Override
    public void setOutputPosition(
            final double argRotations, final int argSlot, final double argFFVolts) {
        mController.setOutputPosition(argRotations, argSlot, argFFVolts);
    }

    @Override
    public void setOutputVelocity(final double argRPS, final int argSlot, final double argFFVolts) {
        mController.setOutputVelocity(argRPS, argSlot, argFFVolts);
    }

    /* Config */

    @Override
    public FollowStatus applyConfig() {
        return mapStatus(mController.applyConfig());
    }

    @Override
    public MotorCapabilities capabilities() {
        return this;
    }

    /*
     *
     * MotorCapabilities
     *
     */

    @Override
    public boolean supportsFOC() {
        return true;
    }

    @Override
    public boolean supportsTimesync() {
        return true;
    }

    @Override
    public boolean supportsMotionMagic() {
        return true;
    }

    @Override
    public boolean supportsTorqueCurrentControl() {
        return true;
    }

    @Override
    public boolean supportsStatorCurrentLimit() {
        return true;
    }

    @Override
    public int maxPIDSlots() {
        return 3;
    }

    /*
     *
     * CTRE-Specific Methods (not on neutral interface)
     *
     */

    /**
     * Returns {@code true} if any argument is NaN or infinite. Safety guard on all closed-loop
     * output methods to prevent sending corrupt values to the motor controller.
     */
    private boolean hasInvalidInput(final String argMethod, final double... argValues) {
        for (final double v : argValues) {
            if (!Double.isFinite(v)) {
                edu.wpi.first.wpilibj.DriverStation.reportWarning(
                        getName() + "." + argMethod + ": rejected non-finite input (" + v + ")",
                        false);
                return true;
            }
        }
        return false;
    }

    /** Commands torque current output (FOC). CTRE-specific — not on the neutral interface. */
    public void setOutputTorqueCurrent(final double argTorqueCurrent) {
        mController.setOutputTorqueCurrent(argTorqueCurrent);
    }

    /*
     * NOTE (CODE-GEN-005b): setControl() return values are intentionally not checked in the
     * output methods below. These are called every robot periodic cycle (50Hz+). The isConnected()
     * guard prevents calls to disconnected devices. Checking StatusCode on every cycle would flood
     * DriverStation with redundant warnings during transient CAN faults. Connection loss is already
     * detected by ControllerTalonFX.robotPeriodicBefore() via talonFX.isConnected() and reported
     * through FaultMonitor.
     */

    /* Closed Loop Position — Duty Cycle FF */

    /** Position control with duty cycle feedforward. Native rotor units. */
    public void setOutputPositionDuty(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputPositionDuty", argPositionRot, argFF)) {
            return;
        }
        mPositionDuty.Slot = argSlot;
        mPositionDuty.Position = argPositionRot;
        mPositionDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mPositionDuty);
        }
    }

    /* Closed Loop Position — Torque Current FOC */

    /** Position control with torque current feedforward. Native rotor units. */
    public void setOutputPositionTorqueCurrent(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputPositionTorqueCurrent", argPositionRot, argFF)) {
            return;
        }
        mPositionTC.Slot = argSlot;
        mPositionTC.Position = argPositionRot;
        mPositionTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mPositionTC);
        }
    }

    /* Closed Loop Velocity — Duty Cycle FF */

    /** Velocity control with duty cycle feedforward. Native rotor units. */
    public void setOutputVelocityDuty(final double argRPS, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputVelocityDuty", argRPS, argFF)) {
            return;
        }
        mVelocityDuty.Slot = argSlot;
        mVelocityDuty.Velocity = argRPS;
        mVelocityDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mVelocityDuty);
        }
    }

    /* Closed Loop Velocity — Torque Current FOC */

    /** Velocity control with torque current feedforward. Native rotor units. */
    public void setOutputVelocityTorqueCurrent(
            final double argRPS, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputVelocityTorqueCurrent", argRPS, argFF)) {
            return;
        }
        mVelocityTC.Slot = argSlot;
        mVelocityTC.Velocity = argRPS;
        mVelocityTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mVelocityTC);
        }
    }

    /* Motion Magic Position */

    /** Motion Magic position with duty cycle feedforward. Native rotor units. */
    public void setOutputMMPositionDuty(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMPositionDuty", argPositionRot, argFF)) {
            return;
        }
        mMMPositionDuty.Slot = argSlot;
        mMMPositionDuty.Position = argPositionRot;
        mMMPositionDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMPositionDuty);
        }
    }

    /** Motion Magic position with voltage feedforward. Native rotor units. */
    public void setOutputMMPositionVoltage(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMPositionVoltage", argPositionRot, argFF)) {
            return;
        }
        mMMPositionVoltage.Slot = argSlot;
        mMMPositionVoltage.Position = argPositionRot;
        mMMPositionVoltage.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMPositionVoltage);
        }
    }

    /** Motion Magic position with torque current feedforward. Native rotor units. */
    public void setOutputMMPositionTorqueCurrent(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMPositionTorqueCurrent", argPositionRot, argFF)) {
            return;
        }
        mMMPositionTC.Slot = argSlot;
        mMMPositionTC.Position = argPositionRot;
        mMMPositionTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMPositionTC);
        }
    }

    /* Motion Magic Velocity */

    /** Motion Magic velocity with duty cycle feedforward. Native rotor units. */
    public void setOutputMMVelocityDuty(
            final double argRPS, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMVelocityDuty", argRPS, argFF)) {
            return;
        }
        mMMVelocityDuty.Slot = argSlot;
        mMMVelocityDuty.Velocity = argRPS;
        mMMVelocityDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMVelocityDuty);
        }
    }

    /** Motion Magic velocity with voltage feedforward. Native rotor units. */
    public void setOutputMMVelocityVoltage(
            final double argRPS, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMVelocityVoltage", argRPS, argFF)) {
            return;
        }
        mMMVelocityVoltage.Slot = argSlot;
        mMMVelocityVoltage.Velocity = argRPS;
        mMMVelocityVoltage.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMVelocityVoltage);
        }
    }

    /** Motion Magic velocity with torque current feedforward. Native rotor units. */
    public void setOutputMMVelocityTorqueCurrent(
            final double argRPS, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMVelocityTorqueCurrent", argRPS, argFF)) {
            return;
        }
        mMMVelocityTC.Slot = argSlot;
        mMMVelocityTC.Velocity = argRPS;
        mMMVelocityTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMVelocityTC);
        }
    }

    /* Motion Magic Expo Position */

    /** Motion Magic exponential position with duty cycle feedforward. Native rotor units. */
    public void setOutputMMExpoPositionDuty(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMExpoPositionDuty", argPositionRot, argFF)) {
            return;
        }
        mMMExpoDuty.Slot = argSlot;
        mMMExpoDuty.Position = argPositionRot;
        mMMExpoDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMExpoDuty);
        }
    }

    /** Motion Magic exponential position with voltage feedforward. Native rotor units. */
    public void setOutputMMExpoPositionVoltage(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMExpoPositionVoltage", argPositionRot, argFF)) {
            return;
        }
        mMMExpoVoltage.Slot = argSlot;
        mMMExpoVoltage.Position = argPositionRot;
        mMMExpoVoltage.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMExpoVoltage);
        }
    }

    /** Motion Magic exponential position with torque current feedforward. Native rotor units. */
    public void setOutputMMExpoPositionTorqueCurrent(
            final double argPositionRot, final int argSlot, final double argFF) {
        if (hasInvalidInput("setOutputMMExpoPositionTorqueCurrent", argPositionRot, argFF)) {
            return;
        }
        mMMExpoTC.Slot = argSlot;
        mMMExpoTC.Position = argPositionRot;
        mMMExpoTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mMMExpoTC);
        }
    }

    /* Dynamic Motion Magic Position */

    /** Dynamic Motion Magic position with duty cycle feedforward. Native rotor units. */
    public void setOutputDynMMPositionDuty(
            final double argPositionRot,
            final int argSlot,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMPositionDuty",
                argPositionRot,
                argVelocity,
                argAccel,
                argJerk,
                argFF)) {
            return;
        }
        mDynMMDuty.Slot = argSlot;
        mDynMMDuty.Position = argPositionRot;
        mDynMMDuty.Velocity = argVelocity;
        mDynMMDuty.Acceleration = argAccel;
        mDynMMDuty.Jerk = argJerk;
        mDynMMDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mDynMMDuty);
        }
    }

    /** Dynamic Motion Magic position with voltage feedforward. Native rotor units. */
    public void setOutputDynMMPositionVoltage(
            final double argPositionRot,
            final int argSlot,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMPositionVoltage",
                argPositionRot,
                argVelocity,
                argAccel,
                argJerk,
                argFF)) {
            return;
        }
        mDynMMVoltage.Slot = argSlot;
        mDynMMVoltage.Position = argPositionRot;
        mDynMMVoltage.Velocity = argVelocity;
        mDynMMVoltage.Acceleration = argAccel;
        mDynMMVoltage.Jerk = argJerk;
        mDynMMVoltage.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mDynMMVoltage);
        }
    }

    /** Dynamic Motion Magic position with torque current feedforward. Native rotor units. */
    public void setOutputDynMMPositionTorqueCurrent(
            final double argPositionRot,
            final int argSlot,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMPositionTorqueCurrent",
                argPositionRot,
                argVelocity,
                argAccel,
                argJerk,
                argFF)) {
            return;
        }
        mDynMMTC.Slot = argSlot;
        mDynMMTC.Position = argPositionRot;
        mDynMMTC.Velocity = argVelocity;
        mDynMMTC.Acceleration = argAccel;
        mDynMMTC.Jerk = argJerk;
        mDynMMTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mDynMMTC);
        }
    }

    /* Dynamic Motion Magic Expo Position */

    /** Dynamic Motion Magic Expo position with duty cycle feedforward. Native rotor units. */
    public void setOutputDynMMExpoPositionDuty(
            final double argPositionRot,
            final int argSlot,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMExpoPositionDuty",
                argPositionRot,
                argKV,
                argKA,
                argMaxVelocity,
                argFF)) {
            return;
        }
        mDynMMExpoDuty.Slot = argSlot;
        mDynMMExpoDuty.Position = argPositionRot;
        mDynMMExpoDuty.kV = argKV;
        mDynMMExpoDuty.kA = argKA;
        mDynMMExpoDuty.Velocity = argMaxVelocity;
        mDynMMExpoDuty.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mDynMMExpoDuty);
        }
    }

    /** Dynamic Motion Magic Expo position with voltage feedforward. Native rotor units. */
    public void setOutputDynMMExpoPositionVoltage(
            final double argPositionRot,
            final int argSlot,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMExpoPositionVoltage",
                argPositionRot,
                argKV,
                argKA,
                argMaxVelocity,
                argFF)) {
            return;
        }
        mDynMMExpoVoltage.Slot = argSlot;
        mDynMMExpoVoltage.Position = argPositionRot;
        mDynMMExpoVoltage.kV = argKV;
        mDynMMExpoVoltage.kA = argKA;
        mDynMMExpoVoltage.Velocity = argMaxVelocity;
        mDynMMExpoVoltage.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mDynMMExpoVoltage);
        }
    }

    /** Dynamic Motion Magic Expo position with torque current feedforward. Native rotor units. */
    public void setOutputDynMMExpoPositionTorqueCurrent(
            final double argPositionRot,
            final int argSlot,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMExpoPositionTorqueCurrent",
                argPositionRot,
                argKV,
                argKA,
                argMaxVelocity,
                argFF)) {
            return;
        }
        mDynMMExpoTC.Slot = argSlot;
        mDynMMExpoTC.Position = argPositionRot;
        mDynMMExpoTC.kV = argKV;
        mDynMMExpoTC.kA = argKA;
        mDynMMExpoTC.Velocity = argMaxVelocity;
        mDynMMExpoTC.FeedForward = argFF;
        if (mController.isConnected()) {
            mController.getTalonFX().setControl(mDynMMExpoTC);
        }
    }

    /* Motion Magic Configuration */

    /**
     * Configures Motion Magic cruise velocity, acceleration, and jerk.
     *
     * @param argCruiseVelRPS cruise velocity in rotations per second
     * @param argAccelRPSS acceleration in rotations per second squared
     * @param argJerkRPSSS jerk in rotations per second cubed
     */
    public void configMotionMagic(
            final double argCruiseVelRPS, final double argAccelRPSS, final double argJerkRPSSS) {
        final MotionMagicConfigs mm = mController.getConfigMM();
        if (mm != null) {
            mm.MotionMagicCruiseVelocity = argCruiseVelRPS;
            mm.MotionMagicAcceleration = argAccelRPSS;
            mm.MotionMagicJerk = argJerkRPSSS;
        }
    }

    /** Returns the current supply voltage. */
    public double getSupplyVoltage() {
        return mController.getSupplyVoltage();
    }

    /** Returns the current supply current draw. */
    public double getSupplyCurrent() {
        return mController.getSupplyCurrent();
    }

    /** Returns the torque current output. */
    public double getOutputTorqueCurrent() {
        return mController.getOutputTorqueCurrent();
    }

    /** Returns the device temperature in degrees Celsius. */
    public double getDeviceTemp() {
        return mController.getDeviceTemp();
    }

    /** Returns the rotor acceleration in rotations per second squared. */
    public double getAcceleration() {
        return mController.getAcceleration();
    }

    /** Strict-follows the specified leader (mirrors output exactly). */
    public void setStrictFollow(final ControllerBase argLeader) {
        mController.setStrictFollow(argLeader);
    }

    /** Commands the motor to coast (releases all control). */
    public void coast() {
        mController.coast();
    }

    /**
     * Configures timesync for control requests on this motor.
     *
     * @param argEnabled true for CANivore timesync
     * @param argUpdateFreqHz update frequency when timesync is disabled
     */
    public void configTimesync(final boolean argEnabled, final double argUpdateFreqHz) {
        mController.setControlUpdateFrequency(argEnabled ? 250.0 : argUpdateFreqHz, argEnabled);
    }

    /*
     *
     * Lifecycle
     *
     */

    @Override
    public void robotInit(final double argTimestamp) {
        mController.robotInit(argTimestamp);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        mController.robotPeriodicBefore(argTimestamp);
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
        mController.robotPeriodicAfter(argTimestamp);
    }

    @Override
    public void simulationInit(final double argTimestamp) {
        mController.simulationInit(argTimestamp);
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        mController.simulationPeriodic(argTimestamp);
    }

    @Override
    public void outputTelemetry() {
        mController.outputTelemetry();
    }

    /*
     *
     * Named
     *
     */

    @Override
    public String getName() {
        return mController.getName();
    }

    @Override
    public NTTable getTable() {
        return mController.getTable();
    }

    @Override
    public String logKey(final String argSuffix) {
        return mController.logKey(argSuffix);
    }

    /*
     *
     * Internal
     *
     */

    private static FollowStatus mapStatus(final ControllerStatus argStatus) {
        return switch (argStatus) {
            case OK -> FollowStatus.OK;
            case ERROR -> FollowStatus.ERROR;
            case ERROR_INVALID_BUS -> FollowStatus.ERROR_INVALID_BUS;
            default -> FollowStatus.UNKNOWN;
        };
    }
}
