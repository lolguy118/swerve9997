package com.team271.lib.hardware.transmissions;

import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.LoggedNTInput;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.wpilibj.DriverStation;

public class TransmissionFX extends TransmissionBase {
    /*
     * TalonFX
     */
    protected final NeutralOut motorBrake = new NeutralOut();

    /* Open Loop Controls */
    // When UseTimesync is true, UpdateFreqHz must be 0 per CTRE docs
    protected final DutyCycleOut motorOut =
            new DutyCycleOut(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final VoltageOut motorOutV =
            new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Closed Loop Controls */
    protected final PositionVoltage motorPositionFF =
            new PositionVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final VelocityVoltage motorVelocityFF =
            new VelocityVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Closed Loop Position variants */
    protected final PositionDutyCycle motorPositionDuty =
            new PositionDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final PositionTorqueCurrentFOC motorPositionTC =
            new PositionTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Closed Loop Velocity variants */
    protected final VelocityDutyCycle motorVelocityDuty =
            new VelocityDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final VelocityTorqueCurrentFOC motorVelocityTC =
            new VelocityTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Motion Magic Position */
    protected MotionMagicConfigs configMM;
    protected final MotionMagicDutyCycle motorMMOut =
            new MotionMagicDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicVoltage motorMMFF =
            new MotionMagicVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicTorqueCurrentFOC motorMMTC =
            new MotionMagicTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Motion Magic Velocity */
    protected final MotionMagicVelocityDutyCycle motorMMVelOut =
            new MotionMagicVelocityDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicVelocityVoltage motorMMVelFF =
            new MotionMagicVelocityVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicVelocityTorqueCurrentFOC motorMMVelTC =
            new MotionMagicVelocityTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Motion Magic Expo (Exponential profile) */
    protected final MotionMagicExpoDutyCycle motorMMExpoOut =
            new MotionMagicExpoDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicExpoVoltage motorMMExpoFF =
            new MotionMagicExpoVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicExpoTorqueCurrentFOC motorMMExpoTC =
            new MotionMagicExpoTorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Dynamic Motion Magic */
    protected final DynamicMotionMagicDutyCycle motorDynMMOut =
            new DynamicMotionMagicDutyCycle(0, 0, 0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final DynamicMotionMagicVoltage motorDynMMFF =
            new DynamicMotionMagicVoltage(0, 0, 0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final DynamicMotionMagicTorqueCurrentFOC motorDynMMTC =
            new DynamicMotionMagicTorqueCurrentFOC(0, 0, 0)
                    .withUseTimesync(true)
                    .withUpdateFreqHz(0);

    /**
     * Timesync applicators for all control requests. Each entry captures setting UseTimesync and
     * UpdateFreqHz on one control request object. Used by configTimesync() to loop instead of
     * field-by-field updates.
     */
    @SuppressWarnings("unchecked")
    private final java.util.function.BiConsumer<Boolean, Double>[] timesyncApplicators =
            new java.util.function.BiConsumer[] {
                (en, hz) -> {
                    motorOut.UseTimesync = (Boolean) en;
                    motorOut.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorOutV.UseTimesync = (Boolean) en;
                    motorOutV.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorPositionFF.UseTimesync = (Boolean) en;
                    motorPositionFF.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorVelocityFF.UseTimesync = (Boolean) en;
                    motorVelocityFF.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorPositionDuty.UseTimesync = (Boolean) en;
                    motorPositionDuty.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorPositionTC.UseTimesync = (Boolean) en;
                    motorPositionTC.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorVelocityDuty.UseTimesync = (Boolean) en;
                    motorVelocityDuty.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorVelocityTC.UseTimesync = (Boolean) en;
                    motorVelocityTC.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMOut.UseTimesync = (Boolean) en;
                    motorMMOut.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMFF.UseTimesync = (Boolean) en;
                    motorMMFF.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMTC.UseTimesync = (Boolean) en;
                    motorMMTC.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMVelOut.UseTimesync = (Boolean) en;
                    motorMMVelOut.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMVelFF.UseTimesync = (Boolean) en;
                    motorMMVelFF.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMVelTC.UseTimesync = (Boolean) en;
                    motorMMVelTC.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMExpoOut.UseTimesync = (Boolean) en;
                    motorMMExpoOut.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMExpoFF.UseTimesync = (Boolean) en;
                    motorMMExpoFF.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorMMExpoTC.UseTimesync = (Boolean) en;
                    motorMMExpoTC.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorDynMMOut.UseTimesync = (Boolean) en;
                    motorDynMMOut.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorDynMMFF.UseTimesync = (Boolean) en;
                    motorDynMMFF.UpdateFreqHz = (Double) hz;
                },
                (en, hz) -> {
                    motorDynMMTC.UseTimesync = (Boolean) en;
                    motorDynMMTC.UpdateFreqHz = (Double) hz;
                },
            };

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntMMCruiseVel = new NTEntry(table, "MM Cruise Vel", 0.0);
    final NTEntry ntMMAccel = new NTEntry(table, "MM Accel", 0.0);
    final NTEntry ntMMJerk = new NTEntry(table, "MM Jerk", 0.0);

    /*
     * Tuning Inputs (LoggedNTInput)
     */
    private LoggedNTInput tuneMMCruiseVel;
    private LoggedNTInput tuneMMAccel;
    private LoggedNTInput tuneMMJerk;

    private LoggedNTInput tunePIDkP;
    private LoggedNTInput tunePIDkI;
    private LoggedNTInput tunePIDkD;
    private LoggedNTInput tunePIDkV;
    private LoggedNTInput tunePIDkS;

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

        leader = new ControllerTalonFX(this, "(FX)" + argName, argCANIDMaster, argMotor);
        allControllers.add(leader);

        // UpdateFreqHz already set to 0 in field initializers (required for timesync)

        configMM = getLeaderController().getConfigMM();

        double defaultCruise = (configMM != null) ? configMM.MotionMagicCruiseVelocity : 0.0;
        double defaultAccel = (configMM != null) ? configMM.MotionMagicAcceleration : 0.0;
        double defaultJerk = (configMM != null) ? configMM.MotionMagicJerk : 0.0;
        tuneMMCruiseVel = new LoggedNTInput(table, "Tune MM Cruise Vel", defaultCruise);
        tuneMMAccel = new LoggedNTInput(table, "Tune MM Accel", defaultAccel);
        tuneMMJerk = new LoggedNTInput(table, "Tune MM Jerk", defaultJerk);

        tunePIDkP = new LoggedNTInput(table, "Tune PID kP", leader.getPSlot(0));
        tunePIDkI = new LoggedNTInput(table, "Tune PID kI", leader.getISlot(0));
        tunePIDkD = new LoggedNTInput(table, "Tune PID kD", leader.getDSlot(0));
        tunePIDkV = new LoggedNTInput(table, "Tune PID kV", leader.getVSlot(0));
        tunePIDkS = new LoggedNTInput(table, "Tune PID kS", leader.getSSlot(0));

        // FOC is enabled by default in Phoenix 6 v26 for all control requests
    }

    public TransmissionFX(
            final TObj argParent,
            final String argName,
            final MotorBase argMotor,
            final CANDeviceID argCANIDLeader,
            final CANDeviceID argCANIDFollower1,
            final boolean argFollower1OpposeLeader) {
        this(argParent, argName, argMotor, argCANIDLeader);

        follower1 =
                new ControllerTalonFX(
                        this,
                        "(FX1)" + argName,
                        argCANIDFollower1,
                        argMotor,
                        getLeaderController(),
                        argFollower1OpposeLeader);

        allControllers.add(follower1);
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

        follower2 =
                new ControllerTalonFX(
                        this,
                        "(FX2)" + argName,
                        argCANIDFollower2,
                        argMotor,
                        getLeaderController(),
                        argFollower2OpposeLeader);

        allControllers.add(follower2);
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

        follower3 =
                new ControllerTalonFX(
                        this,
                        "(FX3)" + argName,
                        argCANIDFollower3,
                        argMotor,
                        getLeaderController(),
                        argFollower3OpposeLeader);

        allControllers.add(follower3);
    }

    /**
     * When true, hasInvalidInput() throws IllegalArgumentException instead of warning. Enable in
     * simulation/testing to catch NaN bugs early. Defaults to false (warn-and-skip for competition
     * safety).
     */
    public static boolean STRICT_VALIDATION = false;

    /**
     * Returns {@code true} if any argument is NaN or infinite. Used as a safety guard on all
     * closed-loop output methods to prevent sending corrupt values to the motor controller.
     */
    private boolean hasInvalidInput(final String argMethod, final double... argValues) {
        for (double v : argValues) {
            if (!Double.isFinite(v)) {
                String msg =
                        getName() + "." + argMethod + ": rejected non-finite input (" + v + ")";
                if (STRICT_VALIDATION) {
                    throw new IllegalArgumentException(msg);
                }
                DriverStation.reportWarning(msg, false);
                return true;
            }
        }
        return false;
    }

    /*
     *
     * Get Motors
     *
     */
    public ControllerTalonFX getLeaderController() {
        return ((ControllerTalonFX) leader);
    }

    public TalonFX getLeader() {
        return getLeaderController().getTalonFX();
    }

    public TalonFXConfiguration getLeaderConfig() {
        return getLeaderController().getConfig();
    }

    /*
     *
     * Config
     *
     */

    /**
     * Configures timesync for all control request objects. When enabled, UpdateFreqHz is set to 0
     * (required by CTRE for timesync). When disabled, UpdateFreqHz is set to the specified rate.
     * Also configures the leader controller's timesync frequency.
     *
     * @param argEnabled true for CANivore timesync, false for standard CAN
     * @param argUpdateFreqHz update frequency when timesync is disabled (ignored when enabled)
     */
    public void configTimesync(final boolean argEnabled, final double argUpdateFreqHz) {
        double freqHz = argEnabled ? 0.0 : argUpdateFreqHz;

        for (var applicator : timesyncApplicators) {
            applicator.accept(argEnabled, freqHz);
        }

        /* Also configure the leader controller's timesync */
        getLeaderController().setControlUpdateFrequency(argEnabled ? 250.0 : 0.0, argEnabled);
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
        if (shifterState != argShiftTo) {
            if (argShiftTo == ShifterState.GEAR_1) {
                getLeaderConfig().Feedback.RotorToSensorRatio = sensorRatioGear1;
                applyConfigs();
            } else if (argShiftTo == ShifterState.GEAR_2) {
                getLeaderConfig().Feedback.RotorToSensorRatio = sensorRatioGear2;
                applyConfigs();
            }
        }

        /* Activate pneumatics and update shifterState */
        super.shift(argShiftTo);

        return shifterState;
    }

    /*
     *
     * Refresh
     *
     */
    public void refresh() {
        if (encFX != null) {
            encFX.refresh();
        }
        if (encCANCoder != null) {
            encCANCoder.refresh();
        }
    }

    /*
     *
     * Closed Loop
     *
     */
    @Override
    public double getSetpoint() {
        if (encoder == null) {
            return 0;
        }
        // Reverse the mechanismToNative conversion: native * ratio = mechanism units
        if (encCANCoder != null) {
            return gearRatio.sensorRelToOutput(motorPositionFF.Position);
        }
        return gearRatio.rotorToOutput(motorPositionFF.Position);
    }

    /*
     *
     * Outputs
     *
     */

    /*
     * Closed Loop
     */
    @Override
    public void setOutputPosition(final double argPosition, final double argFFVolt) {
        if (hasInvalidInput("setOutputPosition", argPosition, argFFVolt)) return;
        motorPositionFF.Slot = 0;
        if (encoder != null) {
            motorPositionFF.Position = encoder.mechanismToNative(argPosition);
        }

        motorPositionFF.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorPositionFF);
        }
    }

    /**
     * Sets closed-loop velocity output with voltage feedforward.
     *
     * @param argRPS velocity in mechanism output units per second. The library converts to
     *     rotor/sensor units internally via the gear ratio chain.
     * @param argFFVolt feedforward voltage (volts)
     */
    @Override
    public void setOutputVelocity(final double argRPS, final double argFFVolt) {
        if (hasInvalidInput("setOutputVelocity", argRPS, argFFVolt)) return;
        motorVelocityFF.Slot = 0;
        if (encoder != null) {
            motorVelocityFF.Velocity = encoder.mechanismVelocityToNative(argRPS);
        }

        motorVelocityFF.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorVelocityFF);
        }
    }

    /*
     * Open Loop - Torque Current
     */
    @Override
    public void setOutputTorqueCurrent(final double argTorqueCurrent) {
        if (leader != null) {
            getLeaderController().setOutputTorqueCurrent(argTorqueCurrent);
        }
    }

    /*
     * Closed Loop - Position
     */
    public void setOutputPositionDuty(final double argPosition, final double argFF) {
        if (hasInvalidInput("setOutputPositionDuty", argPosition, argFF)) return;
        motorPositionDuty.Slot = 0;
        if (encoder != null) {
            motorPositionDuty.Position = encoder.mechanismToNative(argPosition);
        }

        motorPositionDuty.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorPositionDuty);
        }
    }

    public void setOutputPositionTorqueCurrent(final double argPosition, final double argFF) {
        if (hasInvalidInput("setOutputPositionTorqueCurrent", argPosition, argFF)) return;
        motorPositionTC.Slot = 0;
        if (encoder != null) {
            motorPositionTC.Position = encoder.mechanismToNative(argPosition);
        }

        motorPositionTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorPositionTC);
        }
    }

    /*
     * Closed Loop - Velocity
     */
    /**
     * Sets closed-loop velocity output with duty cycle feedforward.
     *
     * @param argRPS velocity in mechanism output units per second. The library converts to
     *     rotor/sensor units internally via the gear ratio chain.
     * @param argFF feedforward duty cycle [-1.0, 1.0]
     */
    public void setOutputVelocityDuty(final double argRPS, final double argFF) {
        if (hasInvalidInput("setOutputVelocityDuty", argRPS, argFF)) return;
        motorVelocityDuty.Slot = 0;
        if (encoder != null) {
            motorVelocityDuty.Velocity = encoder.mechanismVelocityToNative(argRPS);
        }

        motorVelocityDuty.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorVelocityDuty);
        }
    }

    /**
     * Sets closed-loop velocity output with torque current feedforward.
     *
     * @param argRPS velocity in mechanism output units per second. The library converts to
     *     rotor/sensor units internally via the gear ratio chain.
     * @param argFF feedforward torque current (amps)
     */
    public void setOutputVelocityTorqueCurrent(final double argRPS, final double argFF) {
        if (hasInvalidInput("setOutputVelocityTorqueCurrent", argRPS, argFF)) return;
        motorVelocityTC.Slot = 0;
        if (encoder != null) {
            motorVelocityTC.Velocity = encoder.mechanismVelocityToNative(argRPS);
        }

        motorVelocityTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorVelocityTC);
        }
    }

    /*
     * Motion Magic - Position
     */
    /**
     * Configures Motion Magic profile parameters.
     *
     * @param argCruiseVelRPS cruise velocity in rotor rotations per second (raw, not
     *     mechanism-scaled)
     * @param argCruiseAccelRPSS cruise acceleration in rotor rotations per second squared (raw)
     * @param argJerkRPSSS jerk in rotor rotations per second cubed (raw)
     */
    public void setMMConfig(
            final double argCruiseVelRPS,
            final double argCruiseAccelRPSS,
            final double argJerkRPSSS) {
        if (configMM != null) {
            configMM.MotionMagicCruiseVelocity = argCruiseVelRPS; // rps
            configMM.MotionMagicAcceleration = argCruiseAccelRPSS; // rps/s
            configMM.MotionMagicJerk = argJerkRPSSS; // rps/s/s
        }
    }

    public void setOutputMMPositionDuty(double argPosition, double argFFVolt) {
        if (hasInvalidInput("setOutputMMPositionDuty", argPosition, argFFVolt)) return;
        motorMMOut.Slot = 0;
        if (encoder != null) {
            motorMMOut.Position = encoder.mechanismToNative(argPosition);
        }

        motorMMOut.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMOut);
        }
    }

    public void setOutputMMPositionVoltage(final double argPosition, final double argFFVolt) {
        if (hasInvalidInput("setOutputMMPositionVoltage", argPosition, argFFVolt)) return;
        motorMMFF.Slot = 0;
        if (encoder != null) {
            motorMMFF.Position = encoder.mechanismToNative(argPosition);
        }

        motorMMFF.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMFF);
        }
    }

    public void setOutputMMPositionTorqueCurrent(final double argPosition, final double argFF) {
        if (hasInvalidInput("setOutputMMPositionTorqueCurrent", argPosition, argFF)) return;
        motorMMTC.Slot = 0;
        if (encoder != null) {
            motorMMTC.Position = encoder.mechanismToNative(argPosition);
        }

        motorMMTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMTC);
        }
    }

    /*
     * Motion Magic - Velocity
     */
    public void setOutputMMVelocityDuty(final double argRPS, final double argFF) {
        if (hasInvalidInput("setOutputMMVelocityDuty", argRPS, argFF)) return;
        motorMMVelOut.Slot = 0;
        if (encoder != null) {
            motorMMVelOut.Velocity = encoder.mechanismVelocityToNative(argRPS);
        }

        motorMMVelOut.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMVelOut);
        }
    }

    public void setOutputMMVelocityVoltage(final double argRPS, final double argFF) {
        if (hasInvalidInput("setOutputMMVelocityVoltage", argRPS, argFF)) return;
        motorMMVelFF.Slot = 0;
        if (encoder != null) {
            motorMMVelFF.Velocity = encoder.mechanismVelocityToNative(argRPS);
        }

        motorMMVelFF.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMVelFF);
        }
    }

    public void setOutputMMVelocityTorqueCurrent(final double argRPS, final double argFF) {
        if (hasInvalidInput("setOutputMMVelocityTorqueCurrent", argRPS, argFF)) return;
        motorMMVelTC.Slot = 0;
        if (encoder != null) {
            motorMMVelTC.Velocity = encoder.mechanismVelocityToNative(argRPS);
        }

        motorMMVelTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMVelTC);
        }
    }

    /*
     * Motion Magic - Expo (Exponential profile)
     */
    public void setOutputMMExpoPositionDuty(final double argPosition, final double argFF) {
        if (hasInvalidInput("setOutputMMExpoPositionDuty", argPosition, argFF)) return;
        motorMMExpoOut.Slot = 0;
        if (encoder != null) {
            motorMMExpoOut.Position = encoder.mechanismToNative(argPosition);
        }

        motorMMExpoOut.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMExpoOut);
        }
    }

    public void setOutputMMExpoPositionVoltage(final double argPosition, final double argFF) {
        if (hasInvalidInput("setOutputMMExpoPositionVoltage", argPosition, argFF)) return;
        motorMMExpoFF.Slot = 0;
        if (encoder != null) {
            motorMMExpoFF.Position = encoder.mechanismToNative(argPosition);
        }

        motorMMExpoFF.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMExpoFF);
        }
    }

    public void setOutputMMExpoPositionTorqueCurrent(final double argPosition, final double argFF) {
        if (hasInvalidInput("setOutputMMExpoPositionTorqueCurrent", argPosition, argFF)) return;
        motorMMExpoTC.Slot = 0;
        if (encoder != null) {
            motorMMExpoTC.Position = encoder.mechanismToNative(argPosition);
        }

        motorMMExpoTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMExpoTC);
        }
    }

    /*
     * Dynamic Motion Magic
     */
    public void setOutputDynMMPositionDuty(
            final double argPosition,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMPositionDuty", argPosition, argVelocity, argAccel, argJerk, argFF))
            return;
        motorDynMMOut.Slot = 0;
        if (encoder != null) {
            motorDynMMOut.Position = encoder.mechanismToNative(argPosition);
        }

        motorDynMMOut.Velocity = argVelocity;
        motorDynMMOut.Acceleration = argAccel;
        motorDynMMOut.Jerk = argJerk;
        motorDynMMOut.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorDynMMOut);
        }
    }

    public void setOutputDynMMPositionVoltage(
            final double argPosition,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMPositionVoltage",
                argPosition,
                argVelocity,
                argAccel,
                argJerk,
                argFF)) return;
        motorDynMMFF.Slot = 0;
        if (encoder != null) {
            motorDynMMFF.Position = encoder.mechanismToNative(argPosition);
        }

        motorDynMMFF.Velocity = argVelocity;
        motorDynMMFF.Acceleration = argAccel;
        motorDynMMFF.Jerk = argJerk;
        motorDynMMFF.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorDynMMFF);
        }
    }

    public void setOutputDynMMPositionTorqueCurrent(
            final double argPosition,
            final double argVelocity,
            final double argAccel,
            final double argJerk,
            final double argFF) {
        if (hasInvalidInput(
                "setOutputDynMMPositionTorqueCurrent",
                argPosition,
                argVelocity,
                argAccel,
                argJerk,
                argFF)) return;
        motorDynMMTC.Slot = 0;
        if (encoder != null) {
            motorDynMMTC.Position = encoder.mechanismToNative(argPosition);
        }

        motorDynMMTC.Velocity = argVelocity;
        motorDynMMTC.Acceleration = argAccel;
        motorDynMMTC.Jerk = argJerk;
        motorDynMMTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorDynMMTC);
        }
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
        if (tuneMMCruiseVel.hasChanged() || tuneMMAccel.hasChanged() || tuneMMJerk.hasChanged()) {
            setMMConfig(tuneMMCruiseVel.getDbl(), tuneMMAccel.getDbl(), tuneMMJerk.getDbl());
        }
        if (tunePIDkP.hasChanged()
                || tunePIDkI.hasChanged()
                || tunePIDkD.hasChanged()
                || tunePIDkV.hasChanged()
                || tunePIDkS.hasChanged()) {
            configPIDFSlot(
                    0,
                    tunePIDkP.getDbl(),
                    tunePIDkI.getDbl(),
                    tunePIDkD.getDbl(),
                    tunePIDkV.getDbl(),
                    tunePIDkS.getDbl());
            applyConfigs();
        }
    }

    @Override
    public void outputTelemetry() {
        checkTuning();

        super.outputTelemetry();

        if (configMM != null) {
            ntMMCruiseVel.publish(configMM.MotionMagicCruiseVelocity);
            ntMMAccel.publish(configMM.MotionMagicAcceleration);
            ntMMJerk.publish(configMM.MotionMagicJerk);
        }
    }
}
