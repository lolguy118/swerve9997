package com.team271.lib.hardware.transmissions;

import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.LoggedNTInput;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.vendor.ctre.CTREMotor;
import java.util.Optional;

public class TransmissionFX extends TransmissionBase {

    /*
     * Motion Magic Config (kept for tuning — delegates to CTREMotor)
     */
    private MotionMagicConfigs mConfigMM;

    /*
     *
     * Telemetry (NT)
     *
     */
    private final NTEntry ntMMCruiseVel = new NTEntry(table, "MM Cruise Vel", 0.0);
    private final NTEntry ntMMAccel = new NTEntry(table, "MM Accel", 0.0);
    private final NTEntry ntMMJerk = new NTEntry(table, "MM Jerk", 0.0);

    /*
     * Tuning Inputs (LoggedNTInput)
     */
    private LoggedNTInput mTuneMMCruiseVel;
    private LoggedNTInput mTuneMMAccel;
    private LoggedNTInput mTuneMMJerk;

    private LoggedNTInput mTunePIDkP;
    private LoggedNTInput mTunePIDkI;
    private LoggedNTInput mTunePIDkD;
    private LoggedNTInput mTunePIDkV;
    private LoggedNTInput mTunePIDkS;

    /*
     * Vendor-Neutral Wrapper
     */
    private CTREMotor mCTRELeader;

    /*
     *
     * Constructors
     *
     */
    public TransmissionFX(
            final TObj argParent,
            final String argName,
            final MotorBase argMotor,
            final CANDeviceID argCANIDMaster) {
        super(argParent, "(FX)" + argName);

        mLeader = new ControllerTalonFX(this, "(FX)" + argName, argCANIDMaster, argMotor);
        mAllControllers.add(mLeader);

        // UpdateFreqHz already set to 0 in field initializers (required for timesync)

        mConfigMM = getLeaderController().getConfigMM();

        final double defaultCruise =
                (mConfigMM != null) ? mConfigMM.MotionMagicCruiseVelocity : 0.0;
        final double defaultAccel = (mConfigMM != null) ? mConfigMM.MotionMagicAcceleration : 0.0;
        final double defaultJerk = (mConfigMM != null) ? mConfigMM.MotionMagicJerk : 0.0;
        mTuneMMCruiseVel = new LoggedNTInput(table, "Tune MM Cruise Vel", defaultCruise);
        mTuneMMAccel = new LoggedNTInput(table, "Tune MM Accel", defaultAccel);
        mTuneMMJerk = new LoggedNTInput(table, "Tune MM Jerk", defaultJerk);

        mTunePIDkP = new LoggedNTInput(table, "Tune PID kP", mLeader.getPSlot(0));
        mTunePIDkI = new LoggedNTInput(table, "Tune PID kI", mLeader.getISlot(0));
        mTunePIDkD = new LoggedNTInput(table, "Tune PID kD", mLeader.getDSlot(0));
        mTunePIDkV = new LoggedNTInput(table, "Tune PID kV", mLeader.getVSlot(0));
        mTunePIDkS = new LoggedNTInput(table, "Tune PID kS", mLeader.getSSlot(0));

        // FOC is enabled by default in Phoenix 6 v26 for all control requests

        mCTRELeader = new CTREMotor(getLeaderController());
    }

    public TransmissionFX(
            final TObj argParent,
            final String argName,
            final MotorBase argMotor,
            final CANDeviceID argCANIDLeader,
            final CANDeviceID argCANIDFollower1,
            final boolean argFollower1OpposeLeader) {
        this(argParent, argName, argMotor, argCANIDLeader);

        mFollower1 =
                new ControllerTalonFX(
                        this,
                        "(FX1)" + argName,
                        argCANIDFollower1,
                        argMotor,
                        getLeaderController(),
                        argFollower1OpposeLeader);

        mAllControllers.add(mFollower1);
    }

    public TransmissionFX(
            final TObj argParent,
            final String argName,
            final MotorBase argMotor,
            final CANDeviceID argCANIDLeader,
            final CANDeviceID argCANIDFollower1,
            final boolean argFollower1OpposeLeader,
            final CANDeviceID argCANIDFollower2,
            final boolean argFollower2OpposeLeader) {
        this(
                argParent,
                argName,
                argMotor,
                argCANIDLeader,
                argCANIDFollower1,
                argFollower1OpposeLeader);

        mFollower2 =
                new ControllerTalonFX(
                        this,
                        "(FX2)" + argName,
                        argCANIDFollower2,
                        argMotor,
                        getLeaderController(),
                        argFollower2OpposeLeader);

        mAllControllers.add(mFollower2);
    }

    public TransmissionFX(
            final TObj argParent,
            final String argName,
            final MotorBase argMotor,
            final CANDeviceID argCANIDLeader,
            final CANDeviceID argCANIDFollower1,
            final boolean argFollower1OpposeLeader,
            final CANDeviceID argCANIDFollower2,
            final boolean argFollower2OpposeLeader,
            final CANDeviceID argCANIDFollower3,
            final boolean argFollower3OpposeLeader) {
        this(
                argParent,
                argName,
                argMotor,
                argCANIDLeader,
                argCANIDFollower1,
                argFollower1OpposeLeader,
                argCANIDFollower2,
                argFollower2OpposeLeader);

        mFollower3 =
                new ControllerTalonFX(
                        this,
                        "(FX3)" + argName,
                        argCANIDFollower3,
                        argMotor,
                        getLeaderController(),
                        argFollower3OpposeLeader);

        mAllControllers.add(mFollower3);
    }

    /*
     *
     * Get Motors
     *
     */
    public ControllerTalonFX getLeaderController() {
        return ((ControllerTalonFX) mLeader);
    }

    public TalonFX getLeader() {
        return getLeaderController().getTalonFX();
    }

    public TalonFXConfiguration getLeaderConfig() {
        return getLeaderController().getConfig();
    }

    /**
     * Returns the vendor-neutral {@link CTREMotor} wrapper for the leader motor.
     *
     * <p>Use this for typed access to CTRE-specific features through the vendor-neutral interface,
     * or for passing the motor to code that accepts {@link
     * com.team271.lib.api.motor.ClosedLoopMotor}.
     */
    public CTREMotor getCTRELeader() {
        return mCTRELeader;
    }

    /**
     * Returns the vendor-neutral wrapper as an Optional. Useful for conditional CTRE-specific
     * operations in code that may handle multiple vendor types in the future.
     */
    public Optional<CTREMotor> getOptionalCTRELeader() {
        return Optional.ofNullable(mCTRELeader);
    }

    /*
     *
     * Config
     *
     */

    /**
     * Configures timesync for all control request objects. Delegates to the CTREMotor leader.
     *
     * @param argEnabled true for CANivore timesync, false for standard CAN
     * @param argUpdateFreqHz update frequency when timesync is disabled (ignored when enabled)
     */
    public void configTimesync(final boolean argEnabled, final double argUpdateFreqHz) {
        mCTRELeader.configTimesync(argEnabled, argUpdateFreqHz);
    }

    /*
     *
     * Shifters
     *
     */
    @Override
    public ShifterState shift(final ShifterState argShiftTo) {
        /*
         * - Update Encoder Gear Ratio BEFORE super.shift() which sets shifterState
         */
        if (mShifterState != argShiftTo) {
            if (argShiftTo == ShifterState.GEAR_1) {
                getLeaderConfig().Feedback.RotorToSensorRatio = mSensorRatioGear1;
                applyConfigs();
            } else if (argShiftTo == ShifterState.GEAR_2) {
                getLeaderConfig().Feedback.RotorToSensorRatio = mSensorRatioGear2;
                applyConfigs();
            }
        }

        /* Activate pneumatics and update mShifterState */
        super.shift(argShiftTo);

        return mShifterState;
    }

    /*
     *
     * Refresh
     *
     */
    public void refresh() {
        if (mEncFX != null) {
            mEncFX.refresh();
        }
        if (mEncCANCoder != null) {
            mEncCANCoder.refresh();
        }
    }

    /*
     *
     * Closed Loop
     *
     */
    @Override
    public double getSetpoint() {
        if (mEncoder == null) {
            return 0;
        }
        /* Read last-commanded reference from the TalonFX closed-loop */
        final double nativeRef = mCTRELeader.getCLOutput();
        if (mEncCANCoder != null) {
            return mGearRatio.sensorRelToOutput(nativeRef);
        }
        return mGearRatio.rotorToOutput(nativeRef);
    }

    /*
     *
     * Outputs
     *
     */

    /*
     * Closed Loop - Position (Voltage FF)
     */
    public void setOutputPosition(
            final double argPosition, final int argSlot, final double argFFVolt) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputPosition(nativePos, argSlot, argFFVolt);
    }

    @Override
    public void setOutputPosition(final double argPosition, final double argFFVolt) {
        setOutputPosition(argPosition, 0, argFFVolt);
    }

    /*
     * Closed Loop - Velocity (Voltage FF)
     */
    public void setOutputVelocity(final double argRPS, final int argSlot, final double argFFVolt) {
        final double nativeVel =
                mEncoder != null ? mEncoder.mechanismVelocityToNative(argRPS) : argRPS;
        mCTRELeader.setOutputVelocity(nativeVel, argSlot, argFFVolt);
    }

    @Override
    public void setOutputVelocity(final double argRPS, final double argFFVolt) {
        setOutputVelocity(argRPS, 0, argFFVolt);
    }

    /*
     * Open Loop - Torque Current
     */
    @Override
    public void setOutputTorqueCurrent(final double argTorqueCurrent) {
        mCTRELeader.setOutputTorqueCurrent(argTorqueCurrent);
    }

    /*
     * Closed Loop - Position (Duty Cycle FF)
     */
    public void setOutputPositionDuty(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputPositionDuty(nativePos, argSlot, argFF);
    }

    public void setOutputPositionDuty(final double argPosition, final double argFF) {
        setOutputPositionDuty(argPosition, 0, argFF);
    }

    /*
     * Closed Loop - Position (Torque Current FOC)
     */
    public void setOutputPositionTorqueCurrent(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputPositionTorqueCurrent(nativePos, argSlot, argFF);
    }

    public void setOutputPositionTorqueCurrent(final double argPosition, final double argFF) {
        setOutputPositionTorqueCurrent(argPosition, 0, argFF);
    }

    /*
     * Closed Loop - Velocity (Duty Cycle FF)
     */
    public void setOutputVelocityDuty(final double argRPS, final int argSlot, final double argFF) {
        final double nativeVel =
                mEncoder != null ? mEncoder.mechanismVelocityToNative(argRPS) : argRPS;
        mCTRELeader.setOutputVelocityDuty(nativeVel, argSlot, argFF);
    }

    public void setOutputVelocityDuty(final double argRPS, final double argFF) {
        setOutputVelocityDuty(argRPS, 0, argFF);
    }

    /*
     * Closed Loop - Velocity (Torque Current FOC)
     */
    public void setOutputVelocityTorqueCurrent(
            final double argRPS, final int argSlot, final double argFF) {
        final double nativeVel =
                mEncoder != null ? mEncoder.mechanismVelocityToNative(argRPS) : argRPS;
        mCTRELeader.setOutputVelocityTorqueCurrent(nativeVel, argSlot, argFF);
    }

    public void setOutputVelocityTorqueCurrent(final double argRPS, final double argFF) {
        setOutputVelocityTorqueCurrent(argRPS, 0, argFF);
    }

    /*
     * Motion Magic Configuration
     */
    public void setMMConfig(
            final double argCruiseVelRPS,
            final double argCruiseAccelRPSS,
            final double argJerkRPSSS) {
        mCTRELeader.configMotionMagic(argCruiseVelRPS, argCruiseAccelRPSS, argJerkRPSSS);
    }

    /*
     * Motion Magic - Position (Duty Cycle)
     */
    public void setOutputMMPositionDuty(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputMMPositionDuty(nativePos, argSlot, argFF);
    }

    public void setOutputMMPositionDuty(final double argPosition, final double argFF) {
        setOutputMMPositionDuty(argPosition, 0, argFF);
    }

    /*
     * Motion Magic - Position (Voltage)
     */
    public void setOutputMMPositionVoltage(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputMMPositionVoltage(nativePos, argSlot, argFF);
    }

    public void setOutputMMPositionVoltage(final double argPosition, final double argFF) {
        setOutputMMPositionVoltage(argPosition, 0, argFF);
    }

    /*
     * Motion Magic - Position (Torque Current FOC)
     */
    public void setOutputMMPositionTorqueCurrent(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputMMPositionTorqueCurrent(nativePos, argSlot, argFF);
    }

    public void setOutputMMPositionTorqueCurrent(final double argPosition, final double argFF) {
        setOutputMMPositionTorqueCurrent(argPosition, 0, argFF);
    }

    /*
     * Motion Magic - Velocity (Duty Cycle)
     */
    public void setOutputMMVelocityDuty(
            final double argRPS, final int argSlot, final double argFF) {
        final double nativeVel =
                mEncoder != null ? mEncoder.mechanismVelocityToNative(argRPS) : argRPS;
        mCTRELeader.setOutputMMVelocityDuty(nativeVel, argSlot, argFF);
    }

    public void setOutputMMVelocityDuty(final double argRPS, final double argFF) {
        setOutputMMVelocityDuty(argRPS, 0, argFF);
    }

    /*
     * Motion Magic - Velocity (Voltage)
     */
    public void setOutputMMVelocityVoltage(
            final double argRPS, final int argSlot, final double argFF) {
        final double nativeVel =
                mEncoder != null ? mEncoder.mechanismVelocityToNative(argRPS) : argRPS;
        mCTRELeader.setOutputMMVelocityVoltage(nativeVel, argSlot, argFF);
    }

    public void setOutputMMVelocityVoltage(final double argRPS, final double argFF) {
        setOutputMMVelocityVoltage(argRPS, 0, argFF);
    }

    /*
     * Motion Magic - Velocity (Torque Current FOC)
     */
    public void setOutputMMVelocityTorqueCurrent(
            final double argRPS, final int argSlot, final double argFF) {
        final double nativeVel =
                mEncoder != null ? mEncoder.mechanismVelocityToNative(argRPS) : argRPS;
        mCTRELeader.setOutputMMVelocityTorqueCurrent(nativeVel, argSlot, argFF);
    }

    public void setOutputMMVelocityTorqueCurrent(final double argRPS, final double argFF) {
        setOutputMMVelocityTorqueCurrent(argRPS, 0, argFF);
    }

    /*
     * Motion Magic Expo - Position (Duty Cycle)
     */
    public void setOutputMMExpoPositionDuty(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputMMExpoPositionDuty(nativePos, argSlot, argFF);
    }

    public void setOutputMMExpoPositionDuty(final double argPosition, final double argFF) {
        setOutputMMExpoPositionDuty(argPosition, 0, argFF);
    }

    /*
     * Motion Magic Expo - Position (Voltage)
     */
    public void setOutputMMExpoPositionVoltage(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputMMExpoPositionVoltage(nativePos, argSlot, argFF);
    }

    public void setOutputMMExpoPositionVoltage(final double argPosition, final double argFF) {
        setOutputMMExpoPositionVoltage(argPosition, 0, argFF);
    }

    /*
     * Motion Magic Expo - Position (Torque Current FOC)
     */
    public void setOutputMMExpoPositionTorqueCurrent(
            final double argPosition, final int argSlot, final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputMMExpoPositionTorqueCurrent(nativePos, argSlot, argFF);
    }

    public void setOutputMMExpoPositionTorqueCurrent(final double argPosition, final double argFF) {
        setOutputMMExpoPositionTorqueCurrent(argPosition, 0, argFF);
    }

    /*
     * Dynamic Motion Magic - Position (Duty Cycle)
     */
    public void setOutputDynMMPositionDuty(
            final double argPosition,
            final int argSlot,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputDynMMPositionDuty(
                nativePos, argSlot, argVelocity, argAccel, argJerk, argFF);
    }

    public void setOutputDynMMPositionDuty(
            final double argPosition,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        setOutputDynMMPositionDuty(argPosition, 0, argVelocity, argAccel, argJerk, argFF);
    }

    /*
     * Dynamic Motion Magic - Position (Voltage)
     */
    public void setOutputDynMMPositionVoltage(
            final double argPosition,
            final int argSlot,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputDynMMPositionVoltage(
                nativePos, argSlot, argVelocity, argAccel, argJerk, argFF);
    }

    public void setOutputDynMMPositionVoltage(
            final double argPosition,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        setOutputDynMMPositionVoltage(argPosition, 0, argVelocity, argAccel, argJerk, argFF);
    }

    /*
     * Dynamic Motion Magic - Position (Torque Current FOC)
     */
    public void setOutputDynMMPositionTorqueCurrent(
            final double argPosition,
            final int argSlot,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputDynMMPositionTorqueCurrent(
                nativePos, argSlot, argVelocity, argAccel, argJerk, argFF);
    }

    public void setOutputDynMMPositionTorqueCurrent(
            final double argPosition,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        setOutputDynMMPositionTorqueCurrent(argPosition, 0, argVelocity, argAccel, argJerk, argFF);
    }

    /*
     * Dynamic Motion Magic Expo - Position (Duty Cycle)
     */
    public void setOutputDynMMExpoPositionDuty(
            final double argPosition,
            final int argSlot,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputDynMMExpoPositionDuty(
                nativePos, argSlot, argKV, argKA, argMaxVelocity, argFF);
    }

    public void setOutputDynMMExpoPositionDuty(
            final double argPosition,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        setOutputDynMMExpoPositionDuty(argPosition, 0, argKV, argKA, argMaxVelocity, argFF);
    }

    /*
     * Dynamic Motion Magic Expo - Position (Voltage)
     */
    public void setOutputDynMMExpoPositionVoltage(
            final double argPosition,
            final int argSlot,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputDynMMExpoPositionVoltage(
                nativePos, argSlot, argKV, argKA, argMaxVelocity, argFF);
    }

    public void setOutputDynMMExpoPositionVoltage(
            final double argPosition,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        setOutputDynMMExpoPositionVoltage(argPosition, 0, argKV, argKA, argMaxVelocity, argFF);
    }

    /*
     * Dynamic Motion Magic Expo - Position (Torque Current FOC)
     */
    public void setOutputDynMMExpoPositionTorqueCurrent(
            final double argPosition,
            final int argSlot,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        final double nativePos =
                mEncoder != null ? mEncoder.mechanismToNative(argPosition) : argPosition;
        mCTRELeader.setOutputDynMMExpoPositionTorqueCurrent(
                nativePos, argSlot, argKV, argKA, argMaxVelocity, argFF);
    }

    public void setOutputDynMMExpoPositionTorqueCurrent(
            final double argPosition,
            final double argKV,
            final double argKA,
            final double argMaxVelocity,
            final double argFF) {
        setOutputDynMMExpoPositionTorqueCurrent(
                argPosition, 0, argKV, argKA, argMaxVelocity, argFF);
    }

    /*
     *
     * Simulation
     *
     */
    public TalonFXSimState getSimState() {
        if (getLeader() != null) {
            return getLeader().getSimState();
        }
        return null;
    }

    /*
     *
     * Telemetry
     *
     */
    protected void checkTuning() {
        if (mTuneMMCruiseVel.hasChanged()
                || mTuneMMAccel.hasChanged()
                || mTuneMMJerk.hasChanged()) {
            setMMConfig(mTuneMMCruiseVel.getDbl(), mTuneMMAccel.getDbl(), mTuneMMJerk.getDbl());
        }
        if (mTunePIDkP.hasChanged()
                || mTunePIDkI.hasChanged()
                || mTunePIDkD.hasChanged()
                || mTunePIDkV.hasChanged()
                || mTunePIDkS.hasChanged()) {
            configPIDFSlot(
                    0,
                    mTunePIDkP.getDbl(),
                    mTunePIDkI.getDbl(),
                    mTunePIDkD.getDbl(),
                    mTunePIDkV.getDbl(),
                    mTunePIDkS.getDbl());
            applyConfigs();
        }
    }

    @Override
    public void outputTelemetry() {
        checkTuning();

        super.outputTelemetry();

        if (mConfigMM != null) {
            ntMMCruiseVel.publish(mConfigMM.MotionMagicCruiseVelocity);
            ntMMAccel.publish(mConfigMM.MotionMagicAcceleration);
            ntMMJerk.publish(mConfigMM.MotionMagicJerk);
        }
    }
}
