package com.team271.lib.hardware.transmissions;

import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.sim.TalonFXSimState;
// import com.team271.frc2026.Config;
// import com.team271.frc2026.Config.Mode;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;

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

    // set Motion Magic settings
    protected MotionMagicConfigs configMM;
    protected final MotionMagicDutyCycle motorMMOut =
            new MotionMagicDutyCycle(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final MotionMagicVoltage motorMMFF =
            new MotionMagicVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);

    /*
     *
     * Telemetry (NT)
     *
     */

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
     * Motion Magic
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
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();
    }
}
