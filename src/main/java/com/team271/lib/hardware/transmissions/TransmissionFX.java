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
        if (encCANCoder != null) {
            return motorPositionFF.Position * sensorRelToMechanism * mechanismToUnits;
        } else if (encFX != null) {
            return motorPositionFF.Position * rotorToMechanism * mechanismToUnits;
        }
        return 0;
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
        if (encCANCoder != null) {
            motorPositionFF.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorPositionFF.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorPositionFF.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorPositionFF);
        }
    }

    @Override
    public void setOutputVelocity(final double argRPS, final double argFFVolt) {
        if (encCANCoder != null) {
            motorVelocityFF.Velocity = argRPS / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorVelocityFF.Velocity = argRPS / (rotorToMechanism * mechanismToUnits);
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
        if (encCANCoder != null) {
            motorPositionDuty.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorPositionDuty.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorPositionDuty.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorPositionDuty);
        }
    }

    public void setOutputPositionTorqueCurrent(final double argPosition, final double argFF) {
        if (encCANCoder != null) {
            motorPositionTC.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorPositionTC.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorPositionTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorPositionTC);
        }
    }

    /*
     * Closed Loop - Velocity
     */
    public void setOutputVelocityDuty(final double argRPS, final double argFF) {
        if (encCANCoder != null) {
            motorVelocityDuty.Velocity = argRPS / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorVelocityDuty.Velocity = argRPS / (rotorToMechanism * mechanismToUnits);
        }

        motorVelocityDuty.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorVelocityDuty);
        }
    }

    public void setOutputVelocityTorqueCurrent(final double argRPS, final double argFF) {
        if (encCANCoder != null) {
            motorVelocityTC.Velocity = argRPS / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorVelocityTC.Velocity = argRPS / (rotorToMechanism * mechanismToUnits);
        }

        motorVelocityTC.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorVelocityTC);
        }
    }

    /*
     * Motion Magic - Position
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
        if (encCANCoder != null) {
            motorMMOut.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMOut.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorMMOut.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMOut);
        }
    }

    public void setOutputMMPositionVoltage(final double argPosition, final double argFFVolt) {
        if (encCANCoder != null) {
            motorMMFF.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMFF.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorMMFF.FeedForward = argFFVolt;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMFF);
        }
    }

    public void setOutputMMPositionTorqueCurrent(final double argPosition, final double argFF) {
        if (encCANCoder != null) {
            motorMMTC.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMTC.Position = argPosition / (rotorToMechanism * mechanismToUnits);
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
        if (encCANCoder != null) {
            motorMMVelOut.Velocity = argRPS / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMVelOut.Velocity = argRPS / (rotorToMechanism * mechanismToUnits);
        }

        motorMMVelOut.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMVelOut);
        }
    }

    public void setOutputMMVelocityVoltage(final double argRPS, final double argFF) {
        if (encCANCoder != null) {
            motorMMVelFF.Velocity = argRPS / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMVelFF.Velocity = argRPS / (rotorToMechanism * mechanismToUnits);
        }

        motorMMVelFF.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMVelFF);
        }
    }

    public void setOutputMMVelocityTorqueCurrent(final double argRPS, final double argFF) {
        if (encCANCoder != null) {
            motorMMVelTC.Velocity = argRPS / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMVelTC.Velocity = argRPS / (rotorToMechanism * mechanismToUnits);
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
        if (encCANCoder != null) {
            motorMMExpoOut.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMExpoOut.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorMMExpoOut.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMExpoOut);
        }
    }

    public void setOutputMMExpoPositionVoltage(final double argPosition, final double argFF) {
        if (encCANCoder != null) {
            motorMMExpoFF.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMExpoFF.Position = argPosition / (rotorToMechanism * mechanismToUnits);
        }

        motorMMExpoFF.FeedForward = argFF;

        if ((leader != null) && leader.isConnected()) {
            getLeader().setControl(motorMMExpoFF);
        }
    }

    public void setOutputMMExpoPositionTorqueCurrent(final double argPosition, final double argFF) {
        if (encCANCoder != null) {
            motorMMExpoTC.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorMMExpoTC.Position = argPosition / (rotorToMechanism * mechanismToUnits);
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
        if (encCANCoder != null) {
            motorDynMMOut.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorDynMMOut.Position = argPosition / (rotorToMechanism * mechanismToUnits);
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
        if (encCANCoder != null) {
            motorDynMMFF.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorDynMMFF.Position = argPosition / (rotorToMechanism * mechanismToUnits);
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
        if (encCANCoder != null) {
            motorDynMMTC.Position = argPosition / (sensorRelToMechanism * mechanismToUnits);
        } else if (encFX != null) {
            motorDynMMTC.Position = argPosition / (rotorToMechanism * mechanismToUnits);
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
