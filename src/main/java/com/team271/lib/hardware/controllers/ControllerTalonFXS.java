package com.team271.lib.hardware.controllers;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorOutputStatusValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.sim.*;
import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.FaultMonitor;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotController;
import org.jspecify.annotations.Nullable;

/**
 * Motor controller wrapper for the CTRE TalonFXS (brushed motor controller).
 *
 * <p>Supports DutyCycle, Voltage, Position, Velocity, and Motion Magic control modes — but NOT
 * FOC/TorqueCurrent variants (those require a brushless motor). For brushless motors, use {@link
 * ControllerTalonFX} instead.
 *
 * <p>Passthrough: {@link #getTalonFXS()} returns the underlying CTRE device, {@link #getConfig()}
 * returns the raw configuration.
 */
@SuppressWarnings("NullAway.Init")
public class ControllerTalonFXS extends ControllerSmart {

    protected StatusCode fxsStatus = StatusCode.OK;
    protected TalonFXS talonFXS;
    protected TalonFXSConfiguration config;
    protected TalonFXSSimState simState;

    protected static final double UPDATE_FREQ_HZ = 250.0;
    protected FaultMonitor faultMonitor;

    protected final NeutralOut motorBrake = new NeutralOut();
    protected final CoastOut motorCoastOut = new CoastOut();

    /* Status Signals */
    protected StatusSignal<MotorOutputStatusValue> motorOutputStatus;
    protected StatusSignal<Voltage> supplyVoltage;
    protected StatusSignal<Current> supplyCurrent;
    protected StatusSignal<Double> outputDuty;
    protected StatusSignal<Voltage> outputVoltage;
    protected StatusSignal<Double> clError;
    protected StatusSignal<Double> clOutput;

    /* Open Loop Controls */
    protected final DutyCycleOut motorOut =
            new DutyCycleOut(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final VoltageOut motorOutV =
            new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Closed Loop Controls (non-FOC only) */
    protected final PositionVoltage motorPosition =
            new PositionVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final VelocityVoltage motorVelocity =
            new VelocityVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);

    /* Telemetry */
    final NTEntry ntFXSStatus = new NTEntry(table, "FXS Status", "");
    final NTEntry ntOutputDuty = new NTEntry(table, "Output Duty", 0);
    final NTEntry ntOutputVoltage = new NTEntry(table, "Output Voltage", 0);
    final NTEntry ntSupplyVoltage = new NTEntry(table, "Supply Voltage", 0.0);
    final NTEntry ntSupplyCurrent = new NTEntry(table, "Supply Current", 0.0);

    /*
     * Constructors
     */
    public ControllerTalonFXS(
            final TObj argParent,
            final String argName,
            final CANDeviceID argID,
            final MotorBase argMotor) {
        super(argParent, "(TalonFXS)" + argName, ControllerType.TALONFXS, argID, argMotor);
        create();
    }

    /*
     * Core
     */
    @Override
    protected void create() {
        if (talonFXS == null) {
            talonFXS = new TalonFXS(deviceID.getDeviceNumber(), deviceID.getCANBus());
            CTREManager.addDevice(talonFXS);

            if (talonFXS.isConnected()) {
                isConnected = true;
            }

            motorOutputStatus = talonFXS.getMotorOutputStatus();
            CTREManager.addSignal(motorOutputStatus, UPDATE_FREQ_HZ);

            talonFXS.clearStickyFaults();

            faultMonitor = new FaultMonitor(this, getName());
            faultMonitor.addFault("Hardware", talonFXS.getStickyFault_Hardware(), UPDATE_FREQ_HZ);
            faultMonitor.addFault("ProcTemp", talonFXS.getStickyFault_ProcTemp(), UPDATE_FREQ_HZ);
            faultMonitor.addFault(
                    "DeviceTemp", talonFXS.getStickyFault_DeviceTemp(), UPDATE_FREQ_HZ);
            faultMonitor.addFault(
                    "Undervoltage", talonFXS.getStickyFault_Undervoltage(), UPDATE_FREQ_HZ);

            config = new TalonFXSConfiguration();
            config.MotorOutput.ControlTimesyncFreqHz = 250.0;
            isConfigured = false;
        }
    }

    /* Passthrough Getters */

    /** Passthrough — returns the raw CTRE TalonFXS device. */
    public TalonFXS getTalonFXS() {
        return talonFXS;
    }

    /** Passthrough — returns the raw TalonFXS configuration. */
    public TalonFXSConfiguration getConfig() {
        return config;
    }

    /** Passthrough — returns the TalonFXS simulation state. */
    public @Nullable TalonFXSSimState getSimState() {
        return simState;
    }

    /*
     * Following
     */
    @Override
    public ControllerStatus follow(final ControllerBase argLeader, final boolean argOpposeLeader) {
        status = super.follow(argLeader, argOpposeLeader);

        if (status == ControllerStatus.OK) {
            talonFXS.setControl(
                    new Follower(
                            followingID.getDeviceNumber(),
                            argOpposeLeader
                                    ? com.ctre.phoenix6.signals.MotorAlignmentValue.Opposed
                                    : com.ctre.phoenix6.signals.MotorAlignmentValue.Aligned));

            // TalonFXSSimState does not have an Orientation field —
            // opposed follower sim behavior is handled by the leader's sim state
        }

        return status;
    }

    /*
     * Robot
     */
    @Override
    public void robotInit(final double argTimestamp) {
        supplyVoltage = talonFXS.getSupplyVoltage();
        CTREManager.addSignal(supplyVoltage, UPDATE_FREQ_HZ);

        supplyCurrent = talonFXS.getSupplyCurrent();
        CTREManager.addSignal(supplyCurrent, UPDATE_FREQ_HZ);

        outputDuty = talonFXS.getDutyCycle();
        CTREManager.addSignal(outputDuty, UPDATE_FREQ_HZ);

        outputVoltage = talonFXS.getMotorVoltage();
        CTREManager.addSignal(outputVoltage, UPDATE_FREQ_HZ);

        clError = talonFXS.getClosedLoopError();
        CTREManager.addSignal(clError, UPDATE_FREQ_HZ);

        clOutput = talonFXS.getClosedLoopOutput();
        CTREManager.addSignal(clOutput, UPDATE_FREQ_HZ);

        if (faultMonitor != null) {
            faultMonitor.registerSignals();
        }
    }

    /*
     * Config
     */
    @Override
    public ControllerStatus applyConfig() {
        ControllerStatus configStatus = ControllerStatus.UNKNOWN;
        for (int i = 0; i < ConstantsLib.CAN_CONFIG_APPLY_RETRIES; ++i) {
            fxsStatus =
                    talonFXS.getConfigurator()
                            .apply(config, ConstantsLib.CAN_CONFIG_APPLY_TIMEOUT_SEC);
            if (fxsStatus.isOK()) {
                configStatus = ControllerStatus.OK;
                isConfigured = true;
                break;
            }
        }
        if (!fxsStatus.isOK()) {
            configStatus = ControllerStatus.ERROR;
            isConfigured = false;
        }
        return configStatus;
    }

    @Override
    public void setNeutralMode(final NeutralState argNeutralState) {
        if (argNeutralState == NeutralState.BRAKE) {
            config.MotorOutput.NeutralMode = NeutralModeValue.Brake;
        } else {
            config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
        }
    }

    @Override
    public NeutralState getNeutralMode() {
        if (config.MotorOutput.NeutralMode == NeutralModeValue.Brake) {
            return NeutralState.BRAKE;
        }
        return NeutralState.COAST;
    }

    @Override
    public void setDirection(final MotorDirection argDirection) {
        if (argDirection == MotorDirection.CW) {
            config.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
        } else {
            config.MotorOutput.Inverted = InvertedValue.CounterClockwise_Positive;
        }
    }

    @Override
    public MotorDirection getDirection() {
        if (config.MotorOutput.Inverted == InvertedValue.Clockwise_Positive) {
            return MotorDirection.CW;
        }
        return MotorDirection.CCW;
    }

    /* Current Limits */
    @Override
    public void setCurrentLimitStator(final boolean argEnable, final double argStatorCurrent) {
        config.CurrentLimits.StatorCurrentLimitEnable = argEnable;
        config.CurrentLimits.StatorCurrentLimit = argStatorCurrent;
    }

    @Override
    public boolean getCurrentLimitStatorEnable() {
        return config.CurrentLimits.StatorCurrentLimitEnable;
    }

    @Override
    public double getCurrentLimitStator() {
        return config.CurrentLimits.StatorCurrentLimit;
    }

    @Override
    public void setCurrentLimitSupply(final boolean argEnable, final double argSupplyCurrent) {
        config.CurrentLimits.SupplyCurrentLimitEnable = argEnable;
        config.CurrentLimits.SupplyCurrentLimit = argSupplyCurrent;
    }

    @Override
    public void setCurrentLimitSupply(
            final double argLimit, final double argTime, final double argLowerLimit) {
        config.CurrentLimits.SupplyCurrentLimit = argLimit;
        config.CurrentLimits.SupplyCurrentLowerTime = argTime;
        config.CurrentLimits.SupplyCurrentLowerLimit = argLowerLimit;
    }

    @Override
    public boolean getCurrentLimitSupplyEnable() {
        return config.CurrentLimits.SupplyCurrentLimitEnable;
    }

    @Override
    public double getCurrentLimitSupply() {
        return config.CurrentLimits.SupplyCurrentLimit;
    }

    @Override
    public double getCurrentLimitSupplyTime() {
        return config.CurrentLimits.SupplyCurrentLowerTime;
    }

    @Override
    public double getCurrentLimitSupplyLowerLimit() {
        return config.CurrentLimits.SupplyCurrentLowerLimit;
    }

    /* Voltage Limits */
    @Override
    public void setVoltagePeak(
            final double argFwd, final double argRev, final double argTimeFilter) {
        config.Voltage.PeakForwardVoltage = argFwd;
        config.Voltage.PeakReverseVoltage = argRev;
        config.Voltage.SupplyVoltageTimeConstant = argTimeFilter;
    }

    @Override
    public double getVoltagePeakFwd() {
        return config.Voltage.PeakForwardVoltage;
    }

    @Override
    public double getVoltagePeakRev() {
        return config.Voltage.PeakReverseVoltage;
    }

    @Override
    public double getVoltagePeakTime() {
        return config.Voltage.SupplyVoltageTimeConstant;
    }

    /* Ramp Rates */
    @Override
    public void setRampOpenLoopDuty(final double argSec) {
        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = argSec;
    }

    @Override
    public double getRampOpenLoopDuty() {
        return config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod;
    }

    @Override
    public void setRampOpenLoopVoltage(final double argSec) {
        config.OpenLoopRamps.VoltageOpenLoopRampPeriod = argSec;
    }

    @Override
    public double getRampOpenLoopVoltage() {
        return config.OpenLoopRamps.VoltageOpenLoopRampPeriod;
    }

    @Override
    public void setRampClosedLoopDuty(final double argSec) {
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = argSec;
    }

    @Override
    public double getRampClosedLoopDuty() {
        return config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod;
    }

    @Override
    public void setRampClosedLoopVoltage(final double argSec) {
        config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = argSec;
    }

    @Override
    public double getRampClosedLoopVoltage() {
        return config.ClosedLoopRamps.VoltageClosedLoopRampPeriod;
    }

    @Override
    public void setTolerance(final double argTol) {
        /* Unused — tolerance managed by PID layer */
    }

    @Override
    public double getTolerance() {
        return 0;
    }

    /* PID Gains */
    @Override
    public void setPSlot(final int argSlot, final double argP) {
        switch (argSlot) {
            case 0:
                config.Slot0.kP = argP;
                break;
            case 1:
                config.Slot1.kP = argP;
                break;
            case 2:
                config.Slot2.kP = argP;
                break;
            default:
                break;
        }
    }

    @Override
    public double getPSlot(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kP;
            case 1 -> config.Slot1.kP;
            case 2 -> config.Slot2.kP;
            default -> 0;
        };
    }

    @Override
    public void setISlot(final int argSlot, final double argI) {
        switch (argSlot) {
            case 0:
                config.Slot0.kI = argI;
                break;
            case 1:
                config.Slot1.kI = argI;
                break;
            case 2:
                config.Slot2.kI = argI;
                break;
            default:
                break;
        }
    }

    @Override
    public double getISlot(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kI;
            case 1 -> config.Slot1.kI;
            case 2 -> config.Slot2.kI;
            default -> 0;
        };
    }

    @Override
    public void setDSlot(final int argSlot, final double argD) {
        switch (argSlot) {
            case 0:
                config.Slot0.kD = argD;
                break;
            case 1:
                config.Slot1.kD = argD;
                break;
            case 2:
                config.Slot2.kD = argD;
                break;
            default:
                break;
        }
    }

    @Override
    public double getDSlot(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kD;
            case 1 -> config.Slot1.kD;
            case 2 -> config.Slot2.kD;
            default -> 0;
        };
    }

    @Override
    public double getVSlot(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kV;
            case 1 -> config.Slot1.kV;
            case 2 -> config.Slot2.kV;
            default -> 0;
        };
    }

    @Override
    public double getSSlot(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kS;
            case 1 -> config.Slot1.kS;
            case 2 -> config.Slot2.kS;
            default -> 0;
        };
    }

    @Override
    public void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        setPSlot(argSlot, argP);
        setISlot(argSlot, argI);
        setDSlot(argSlot, argD);
        switch (argSlot) {
            case 0:
                config.Slot0.kV = argV;
                config.Slot0.kS = argS;
                break;
            case 1:
                config.Slot1.kV = argV;
                config.Slot1.kS = argS;
                break;
            case 2:
                config.Slot2.kV = argV;
                config.Slot2.kS = argS;
                break;
            default:
                break;
        }
    }

    /* Gravity / Acceleration */
    @Override
    public void setGravityGain(final int argSlot, final double argKG) {
        switch (argSlot) {
            case 0:
                config.Slot0.kG = argKG;
                break;
            case 1:
                config.Slot1.kG = argKG;
                break;
            case 2:
                config.Slot2.kG = argKG;
                break;
            default:
                break;
        }
    }

    @Override
    public double getGravityGain(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kG;
            case 1 -> config.Slot1.kG;
            case 2 -> config.Slot2.kG;
            default -> 0;
        };
    }

    @Override
    public void setAccelGain(final int argSlot, final double argKA) {
        switch (argSlot) {
            case 0:
                config.Slot0.kA = argKA;
                break;
            case 1:
                config.Slot1.kA = argKA;
                break;
            case 2:
                config.Slot2.kA = argKA;
                break;
            default:
                break;
        }
    }

    @Override
    public double getAccelGain(final int argSlot) {
        return switch (argSlot) {
            case 0 -> config.Slot0.kA;
            case 1 -> config.Slot1.kA;
            case 2 -> config.Slot2.kA;
            default -> 0;
        };
    }

    @Override
    public void setGravityType(final int argSlot, final GravityType argType) {
        GravityTypeValue ctreType =
                (argType == GravityType.ARM_COSINE)
                        ? GravityTypeValue.Arm_Cosine
                        : GravityTypeValue.Elevator_Static;
        switch (argSlot) {
            case 0:
                config.Slot0.GravityType = ctreType;
                break;
            case 1:
                config.Slot1.GravityType = ctreType;
                break;
            case 2:
                config.Slot2.GravityType = ctreType;
                break;
            default:
                break;
        }
    }

    @Override
    public GravityType getGravityType(final int argSlot) {
        GravityTypeValue ctreType =
                switch (argSlot) {
                    case 0 -> config.Slot0.GravityType;
                    case 1 -> config.Slot1.GravityType;
                    case 2 -> config.Slot2.GravityType;
                    default -> GravityTypeValue.Elevator_Static;
                };
        return (ctreType == GravityTypeValue.Arm_Cosine)
                ? GravityType.ARM_COSINE
                : GravityType.ELEVATOR_STATIC;
    }

    @Override
    public void setContinuousWrap(final boolean argEnabled) {
        config.ClosedLoopGeneral.ContinuousWrap = argEnabled;
    }

    @Override
    public boolean getContinuousWrap() {
        return config.ClosedLoopGeneral.ContinuousWrap;
    }

    @Override
    public void configSoftLimitForward(final boolean argEnable, final double argLimit) {
        config.SoftwareLimitSwitch.ForwardSoftLimitEnable = argEnable;
        config.SoftwareLimitSwitch.ForwardSoftLimitThreshold = argLimit;
    }

    @Override
    public void configSoftLimitReverse(final boolean argEnable, final double argLimit) {
        config.SoftwareLimitSwitch.ReverseSoftLimitEnable = argEnable;
        config.SoftwareLimitSwitch.ReverseSoftLimitThreshold = argLimit;
    }

    /*
     * Outputs
     */
    @Override
    public void stop() {
        talonFXS.stopMotor();
    }

    @Override
    public double getOutputDuty() {
        if ((outputDuty != null) && outputDuty.getStatus().isOK()) {
            return outputDuty.getValueAsDouble();
        }
        return 0;
    }

    @Override
    public void setOutputDuty(final double argOutDuty) {
        motorOut.Output = argOutDuty;
        if (isConnected()) {
            talonFXS.setControl(motorOut);
        }
    }

    @Override
    public double getOutputVoltage() {
        if ((outputVoltage != null) && outputVoltage.getStatus().isOK()) {
            return outputVoltage.getValueAsDouble();
        }
        return 0;
    }

    @Override
    public void setOutputVoltage(final double argOutputVolts) {
        motorOutV.Output = argOutputVolts;
        if (isConnected()) {
            talonFXS.setControl(motorOutV);
        }
    }

    public double getSupplyVoltage() {
        if ((supplyVoltage != null) && supplyVoltage.getStatus().isOK()) {
            return supplyVoltage.getValueAsDouble();
        }
        return 0;
    }

    public double getSupplyCurrent() {
        if ((supplyCurrent != null) && supplyCurrent.getStatus().isOK()) {
            return supplyCurrent.getValueAsDouble();
        }
        return 0;
    }

    /* Closed Loop */
    @Override
    public double getCLError() {
        if ((clError != null) && clError.getStatus().isOK()) {
            return clError.getValueAsDouble();
        }
        return 0;
    }

    @Override
    public double getCLOutput() {
        if ((clOutput != null) && clOutput.getStatus().isOK()) {
            return clOutput.getValueAsDouble();
        }
        return 0;
    }

    @Override
    public void setOutputPosition(
            final double argPosition, final int argSlot, final double argFFVolt) {
        motorPosition.Slot = argSlot;
        motorPosition.Position = argPosition;
        motorPosition.FeedForward = argFFVolt;
        if (isConnected()) {
            talonFXS.setControl(motorPosition);
        }
    }

    @Override
    public void setOutputVelocity(final double argRPS, final int argSlot, final double argFFVolt) {
        motorVelocity.Slot = argSlot;
        motorVelocity.Velocity = argRPS;
        motorVelocity.FeedForward = argFFVolt;
        if (isConnected()) {
            talonFXS.setControl(motorVelocity);
        }
    }

    /*
     * Robot Loops
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        isConnected = talonFXS.isConnected();
        if (faultMonitor != null) {
            faultMonitor.refresh();
        }
    }

    /*
     * Simulation
     */
    @Override
    public void setSimVelRotations(final double argVelRotations) {
        if (simState != null) {
            simState.setRotorVelocity(argVelRotations);
        }
    }

    @Override
    public void setSimPosRotations(final double argPositionRotations) {
        if (simState != null) {
            simState.setRawRotorPosition(argPositionRotations);
        }
    }

    @Override
    public void simulationInit(final double argTimestamp) {
        if (simState == null) {
            simState = talonFXS.getSimState();
        }
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        if (simState != null) {
            simState.setSupplyVoltage(RobotController.getBatteryVoltage());
        }
    }

    /*
     * Telemetry
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntFXSStatus.publish(fxsStatus.getName());
        ntOutputDuty.publish(getOutputDuty());
        ntOutputVoltage.publish(getOutputVoltage());
        ntSupplyVoltage.publish(getSupplyVoltage());
        ntSupplyCurrent.publish(getSupplyCurrent());

        if (faultMonitor != null) {
            faultMonitor.outputTelemetry();
        }
    }
}
