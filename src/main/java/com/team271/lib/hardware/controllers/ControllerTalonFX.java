package com.team271.lib.hardware.controllers;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.MotorOutputStatusValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.sim.*;
import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotController;
import java.util.EnumSet;

public class ControllerTalonFX extends ControllerSmart {
    public enum Signals {
        NONE,

        SUPPLY_VOLT,
        SUPPLY_CURRENT,

        OUTPUT_DUTY,
        OUTPUT_VOLT,
        OUTPUT_TORQUE_CURRENT,

        LIMIT_SW_FWD,
        LIMIT_SW_REV,

        CLOSED_LOOP_ERROR,
        CLOSED_LOOP_OUTPUT,

        ALL
    }

    EnumSet<Signals> signals = EnumSet.of(Signals.ALL);

    protected StatusCode fxStatus = StatusCode.OK;
    protected CANBus canBus = null;
    protected TalonFX talonFX = null;
    protected TalonFXConfiguration config = null;
    protected TalonFXSimState simState = null;

    /*
     * TalonFX
     */
    protected static final double UPDATE_FREQ_HZ_FAULTS = 250.0;
    protected StatusSignal<Boolean> faultBootDuringEnable;

    protected final NeutralOut motorBrake = new NeutralOut();

    protected static final double UPDATE_FREQ_HZ_MOTOR_OUTPUT_STATUS = 250.0;
    protected StatusSignal<MotorOutputStatusValue> motorOutputStatus;

    protected static final double UPDATE_FREQ_HZ_SUPPLY_VOLT = 250.0;
    protected StatusSignal<Voltage> supplyVoltage;

    protected static final double UPDATE_FREQ_HZ_SUPPLY_CURRENT = 250.0;
    protected StatusSignal<Current> supplyCurrent;

    /* Open Loop Controls — timesync enabled for CANivore synchronization, UpdateFreqHz=0 required */
    protected final DutyCycleOut motorOut =
            new DutyCycleOut(0).withUseTimesync(true).withUpdateFreqHz(0);

    protected static final double UPDATE_FREQ_HZ_OUTPUT_DUTY = 250.0;
    protected StatusSignal<Double> outputDuty;
    protected double valueOutputDuty = 0.0;

    protected final VoltageOut motorOutV =
            new VoltageOut(0).withUseTimesync(true).withUpdateFreqHz(0);

    protected static final double UPDATE_FREQ_HZ_OUTPUT_VOLT = 250.0;
    protected StatusSignal<Voltage> outputVoltage;
    protected double valueOutputVoltage = 0.0;

    protected final TorqueCurrentFOC motorOutTC =
            new TorqueCurrentFOC(0).withUseTimesync(true).withUpdateFreqHz(0);

    protected static final double UPDATE_FREQ_HZ_OUTPUT_TORQUE_CURRENT = 250.0;
    protected StatusSignal<Current> outputTorqueCurrent;
    protected double valueOutputTorqueCurrent = 0.0;

    /* Closed Loop Controls — timesync enabled for CANivore synchronization */
    protected final PositionVoltage motorPosition =
            new PositionVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);
    protected final VelocityVoltage motorVelocity =
            new VelocityVoltage(0).withUseTimesync(true).withUpdateFreqHz(0);

    protected static final double UPDATE_FREQ_HZ_CLOSED_LOOP = 250.0;
    protected StatusSignal<Double> clError;
    protected StatusSignal<Double> clOutput;

    /* TalonFX Limit Switches */
    protected static final double UPDATE_FREQ_HZ_LIMIT_SW = 250.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntFXStatus = new NTEntry(table, "FX Status", "");

    final NTEntry ntMotorOutputStatus = new NTEntry(table, "Motor Output Status", "");

    final NTEntry ntOutputDuty = new NTEntry(table, "Output Duty", 0);
    final NTEntry ntOutputVoltage = new NTEntry(table, "Output Voltage", 0);
    final NTEntry ntOutputTorqueCurrent = new NTEntry(table, "Output Torque Current", 0);

    final NTEntry ntSupplyVoltage = new NTEntry(table, "Supply Voltage", 0.0);
    final NTEntry ntSupplyCurrent = new NTEntry(table, "Supply Current", 0.0);

    /*
     *
     * Constructors
     *
     */
    public ControllerTalonFX(
            final TObj argParent,
            final String argName,
            final CANDeviceID argID,
            final MotorBase argMotor) {
        super(argParent, "(TalonFX)" + argName, ControllerType.TALONFX, argID, argMotor);

        create();
    }

    public ControllerTalonFX(
            final TObj argParent,
            final String argName,
            final CANDeviceID argID,
            final MotorBase argMotor,
            final ControllerTalonFX argLeader,
            final boolean argOpposeLeader) {
        this(argParent, argName, argID, argMotor);

        follow(argLeader, argOpposeLeader);
    }

    /*
     *
     * Core
     *
     */
    protected void create() {
        if (talonFX == null) {
            /*
             * Create Talon FX
             */
            talonFX = new TalonFX(deviceID.getDeviceNumber(), deviceID.getCANBus());
            CTREManager.addDevice(talonFX);

            if (talonFX.isConnected()) {
                isConnected = true;
            }

            /*
             * Get Status Signals
             */
            motorOutputStatus = talonFX.getMotorOutputStatus();

            CTREManager.addSignalTalonFX(motorOutputStatus, UPDATE_FREQ_HZ_MOTOR_OUTPUT_STATUS);

            /*
             * Clear Sticky Faults
             * Get Fault Signals
             */
            talonFX.clearStickyFaults();

            faultBootDuringEnable = talonFX.getStickyFault_BootDuringEnable();

            CTREManager.addSignalTalonFX(faultBootDuringEnable, UPDATE_FREQ_HZ_FAULTS);

            /*
             * Create Default Config
             */
            config = new TalonFXConfiguration();
            // Enable timesync on the device so it accepts timesync'd control frames
            config.MotorOutput.ControlTimesyncFreqHz = 250.0;
            isConfigured = false;
        }
    }

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        /*
         * Get Signal: Supply Voltage
         */
        if (signals.contains(Signals.SUPPLY_VOLT) || signals.contains(Signals.ALL)) {
            supplyVoltage = talonFX.getSupplyVoltage();
            CTREManager.addSignalTalonFX(supplyVoltage, UPDATE_FREQ_HZ_SUPPLY_VOLT);
        }

        /*
         * Get Signal: Supply Current
         */
        if (signals.contains(Signals.SUPPLY_CURRENT) || signals.contains(Signals.ALL)) {
            supplyCurrent = talonFX.getSupplyCurrent();
            CTREManager.addSignalTalonFX(supplyCurrent, UPDATE_FREQ_HZ_SUPPLY_CURRENT);
        }

        /*
         * Get Signal: Motor Output Duty
         */
        if (signals.contains(Signals.OUTPUT_DUTY) || signals.contains(Signals.ALL)) {
            outputDuty = talonFX.getDutyCycle();
            CTREManager.addSignalTalonFX(outputDuty, UPDATE_FREQ_HZ_OUTPUT_DUTY);
        }

        /*
         * Get Signal: Motor Output Voltage
         */
        if (signals.contains(Signals.OUTPUT_VOLT) || signals.contains(Signals.ALL)) {
            outputVoltage = talonFX.getMotorVoltage();
            CTREManager.addSignalTalonFX(outputVoltage, UPDATE_FREQ_HZ_OUTPUT_VOLT);
        }

        /*
         * Get Signal: Motor Output Torque Current
         */
        if (signals.contains(Signals.OUTPUT_TORQUE_CURRENT) || signals.contains(Signals.ALL)) {
            outputTorqueCurrent = talonFX.getTorqueCurrent();
            CTREManager.addSignalTalonFX(outputTorqueCurrent, UPDATE_FREQ_HZ_OUTPUT_TORQUE_CURRENT);
        }

        /*
         * Get Signal: Closed Loop Error
         */
        if (signals.contains(Signals.CLOSED_LOOP_ERROR) || signals.contains(Signals.ALL)) {
            clError = talonFX.getClosedLoopError();
            CTREManager.addSignalTalonFX(clError, UPDATE_FREQ_HZ_CLOSED_LOOP);
        }

        /*
         * Get Signal: Closed Loop Output
         */
        if (signals.contains(Signals.CLOSED_LOOP_OUTPUT) || signals.contains(Signals.ALL)) {
            clOutput = talonFX.getClosedLoopOutput();
            CTREManager.addSignalTalonFX(clOutput, UPDATE_FREQ_HZ_CLOSED_LOOP);
        }
    }

    @Override
    public ControllerStatus follow(final ControllerBase argLeader, final boolean argOpposeLeader) {
        status = super.follow(argLeader, argOpposeLeader);

        if (status == ControllerStatus.OK) {
            fxStatus =
                    talonFX.setControl(
                            new Follower(
                                    followingID.getDeviceNumber(),
                                    argOpposeLeader
                                            ? MotorAlignmentValue.Opposed
                                            : MotorAlignmentValue.Aligned));

            if (simState != null) {
                if (opposeLeader) {
                    if (argLeader.getDirection() == MotorDirection.CW) {
                        simState.Orientation = ChassisReference.CounterClockwise_Positive;
                    } else {
                        simState.Orientation = ChassisReference.Clockwise_Positive;
                    }
                } else {
                    if (getDirection() == MotorDirection.CW) {
                        simState.Orientation = ChassisReference.Clockwise_Positive;
                    } else {
                        simState.Orientation = ChassisReference.CounterClockwise_Positive;
                    }
                }
            }
        }

        return status;
    }

    public TalonFX getTalonFX() {
        return talonFX;
    }

    public TalonFXSimState getSimState() {
        return simState;
    }

    /*
     *
     * Config
     *
     */
    public ControllerStatus applyConfig() {
        ControllerStatus status = ControllerStatus.UNKNOWN;

        /* Retry config apply up to CAN_RETRY_COUNT times with 50ms timeout per attempt */
        for (int i = 0; i < ConstantsLib.CAN_RETRY_COUNT; ++i) {
            fxStatus = talonFX.getConfigurator().apply(config, 0.050);
            if (fxStatus.isOK()) {
                status = ControllerStatus.OK;

                isConfigured = true;
                break;
            }
        }

        if (!fxStatus.isOK()) {
            status = ControllerStatus.ERROR;
            isConfigured = false;
        }

        return status;
    }

    public TalonFXConfiguration getConfig() {
        return config;
    }

    public MotionMagicConfigs getConfigMM() {
        return config.MotionMagic;
    }

    public void setControlUpdateFrequency(final double argUpdateFreqHz, final boolean argTimeSync) {
        if (argTimeSync == true) {
            config.MotorOutput.ControlTimesyncFreqHz = argUpdateFreqHz;

            /*
             * When Using Time Sync UpdateFreqHz should be set to 0Hz
             */
            motorOut.UseTimesync = true;
            motorOut.UpdateFreqHz = 0.0;

            motorOutV.UseTimesync = true;
            motorOutV.UpdateFreqHz = 0.0;

            motorOutTC.UseTimesync = true;
            motorOutTC.UpdateFreqHz = 0.0;

            motorPosition.UseTimesync = true;
            motorPosition.UpdateFreqHz = 0.0;

            motorVelocity.UseTimesync = true;
            motorVelocity.UpdateFreqHz = 0.0;
        } else {
            config.MotorOutput.ControlTimesyncFreqHz = 0.0;

            /*
             * When Using Time Sync UpdateFreqHz should be set to 0Hz
             */
            motorOut.UseTimesync = false;
            motorOut.UpdateFreqHz = argUpdateFreqHz;

            motorOutV.UseTimesync = false;
            motorOutV.UpdateFreqHz = argUpdateFreqHz;

            motorOutTC.UseTimesync = false;
            motorOutTC.UpdateFreqHz = argUpdateFreqHz;

            motorPosition.UseTimesync = false;
            motorPosition.UpdateFreqHz = argUpdateFreqHz;

            motorVelocity.UseTimesync = false;
            motorVelocity.UpdateFreqHz = argUpdateFreqHz;
        }
    }

    /* Neutral Mode */
    public void setNeutralMode(final NeutralState argNeutralState) {
        if (argNeutralState == NeutralState.BRAKE) {
            config.MotorOutput.NeutralMode = NeutralModeValue.Brake;
        } else {
            config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
        }
    }

    public NeutralState getNeutralMode() {
        if (config.MotorOutput.NeutralMode == NeutralModeValue.Brake) {
            return NeutralState.BRAKE;
        }
        return NeutralState.COAST;
    }

    /* Direction */
    public void setDirection(final MotorDirection argDirection) {
        if (argDirection == MotorDirection.CW) {
            config.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
            if (simState != null) {
                simState.Orientation = ChassisReference.Clockwise_Positive;
            }
        } else {
            config.MotorOutput.Inverted = InvertedValue.CounterClockwise_Positive;
            if (simState != null) {
                simState.Orientation = ChassisReference.CounterClockwise_Positive;
            }
        }
    }

    public MotorDirection getDirection() {
        if (config.MotorOutput.Inverted == InvertedValue.Clockwise_Positive) {
            return MotorDirection.CW;
        }
        return MotorDirection.CCW;
    }

    /* Current Limit - Stator */
    public void setCurrentLimitStator(final boolean argEnable, final double argStatorCurrent) {
        config.CurrentLimits.StatorCurrentLimitEnable = argEnable;
        config.CurrentLimits.StatorCurrentLimit = argStatorCurrent;
    }

    public boolean getCurrentLimitStatorEnable() {
        return config.CurrentLimits.StatorCurrentLimitEnable;
    }

    public double getCurrentLimitStator() {
        return config.CurrentLimits.StatorCurrentLimit;
    }

    /* Current Limit - Supply */
    public void setCurrentLimitSupply(final boolean argEnable, final double argSupplyCurrent) {
        config.CurrentLimits.SupplyCurrentLimitEnable = argEnable;
        config.CurrentLimits.SupplyCurrentLimit = argSupplyCurrent;
    }

    public void setCurrentLimitSupply(
            final double argSupplyCurrentLimit,
            final double argTime,
            final double argSupplyCurrentLowerLimit) {
        /*
         * Limit to SupplyCurrentLowerLimit if Current exceeds SupplyCurrentLimit for
         * SupplyCurrentLowerTime seconds
         */
        config.CurrentLimits.SupplyCurrentLimit = argSupplyCurrentLimit;
        config.CurrentLimits.SupplyCurrentLowerTime = argTime;

        config.CurrentLimits.SupplyCurrentLowerLimit = argSupplyCurrentLowerLimit;
    }

    public boolean getCurrentLimitSupplyEnable() {
        return config.CurrentLimits.SupplyCurrentLimitEnable;
    }

    public double getCurrentLimitSupply() {
        return config.CurrentLimits.SupplyCurrentLimit;
    }

    public double getCurrentLimitSupplyTime() {
        return config.CurrentLimits.SupplyCurrentLowerTime;
    }

    public double getCurrentLimitSupplyLowerLimit() {
        return config.CurrentLimits.SupplyCurrentLowerLimit;
    }

    /* Voltage Limit */
    public void setVoltagePeak(
            final double argFwdVoltage, final double argRevVoltage, final double argTimeFilter) {
        config.Voltage.PeakForwardVoltage = argFwdVoltage;
        config.Voltage.PeakReverseVoltage = argRevVoltage;
        config.Voltage.SupplyVoltageTimeConstant = argTimeFilter;
    }

    public double getVoltagePeakFwd() {
        return config.Voltage.PeakForwardVoltage;
    }

    public double getVoltagePeakRev() {
        return config.Voltage.PeakReverseVoltage;
    }

    public double getVoltagePeakTime() {
        return config.Voltage.SupplyVoltageTimeConstant;
    }

    /*
     *
     * Config - Open Loop
     *
     */
    public void setRampOpenLoopDuty(final double argRampRateSec) {
        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = argRampRateSec;
    }

    public double getRampOpenLoopDuty() {
        return config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod;
    }

    public void setRampOpenLoopVoltage(final double argRampRateSec) {
        config.OpenLoopRamps.VoltageOpenLoopRampPeriod = argRampRateSec;
    }

    public double getRampOpenLoopVoltage() {
        return config.OpenLoopRamps.VoltageOpenLoopRampPeriod;
    }

    public void setRampOpenLoopTorque(final double argRampRateSec) {
        config.OpenLoopRamps.TorqueOpenLoopRampPeriod = argRampRateSec;
    }

    public double getRampOpenLoopTorque() {
        return config.OpenLoopRamps.TorqueOpenLoopRampPeriod;
    }

    /*
     *
     * Config - Closed Loop
     *
     */
    public void setRampClosedLoopDuty(final double argRampRateSec) {
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = argRampRateSec;
    }

    public double getRampClosedLoopDuty() {
        return config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod;
    }

    public void setRampClosedLoopVoltage(final double argRampRateSec) {
        config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = argRampRateSec;
    }

    public double getRampClosedLoopVoltage() {
        return config.ClosedLoopRamps.VoltageClosedLoopRampPeriod;
    }

    public void setRampClosedLoopTorque(final double argRampRateSec) {
        config.ClosedLoopRamps.TorqueClosedLoopRampPeriod = argRampRateSec;
    }

    public double getRampClosedLoopTorque() {
        return config.ClosedLoopRamps.TorqueClosedLoopRampPeriod;
    }

    public void setTolerance(final double argTolerance) {
        // Unused
    }

    public double getTolerance() {
        return 0;
    }

    /* PID Values */
    public void setPSlot(final int argSlot, final double argSetP) {
        switch (argSlot) {
            case 0:
                config.Slot0.kP = argSetP;
                break;
            case 1:
                config.Slot1.kP = argSetP;
                break;
            case 2:
                config.Slot2.kP = argSetP;
                break;
            default:
                break;
        }
    }

    public double getPSlot(final int argSlot) {
        switch (argSlot) {
            case 0:
                return config.Slot0.kP;
            case 1:
                return config.Slot1.kP;
            case 2:
                return config.Slot2.kP;
            default:
                return 0;
        }
    }

    public void setISlot(final int argSlot, final double argSetI) {
        switch (argSlot) {
            case 0:
                config.Slot0.kI = argSetI;
                break;
            case 1:
                config.Slot1.kI = argSetI;
                break;
            case 2:
                config.Slot2.kI = argSetI;
                break;
            default:
                break;
        }
    }

    public double getISlot(final int argSlot) {
        switch (argSlot) {
            case 0:
                return config.Slot0.kI;
            case 1:
                return config.Slot1.kI;
            case 2:
                return config.Slot2.kI;
            default:
                return 0;
        }
    }

    public void setDSlot(final int argSlot, final double argSetD) {
        switch (argSlot) {
            case 0:
                config.Slot0.kD = argSetD;
                break;
            case 1:
                config.Slot1.kD = argSetD;
                break;
            case 2:
                config.Slot2.kD = argSetD;
                break;
            default:
                break;
        }
    }

    public double getDSlot(final int argSlot) {
        switch (argSlot) {
            case 0:
                return config.Slot0.kD;
            case 1:
                return config.Slot1.kD;
            case 2:
                return config.Slot2.kD;
            default:
                return 0;
        }
    }

    public void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        switch (argSlot) {
            case 0:
                config.Slot0.kP = argP;
                config.Slot0.kI = argI;
                config.Slot0.kD = argD;

                config.Slot0.kV = argV;
                config.Slot0.kS = argS;
                break;
            case 1:
                config.Slot1.kP = argP;
                config.Slot1.kI = argI;
                config.Slot1.kD = argD;

                config.Slot1.kV = argV;
                config.Slot1.kS = argS;
                break;
            case 2:
                config.Slot2.kP = argP;
                config.Slot2.kI = argI;
                config.Slot2.kD = argD;

                config.Slot2.kV = argV;
                config.Slot2.kS = argS;
                break;
            default:
                break;
        }
    }

    /*
     *
     * Outputs
     *
     */
    public void stop() {
        talonFX.stopMotor();
    }

    /*
     * Open Loop
     */
    /* Duty Cycle */
    public double getOutputDuty() {
        if ((outputDuty != null) && outputDuty.getStatus().isOK()) {
            return outputDuty.getValueAsDouble();
        }
        return 0;
    }

    public void setOutputDuty(final double argOutDuty) {
        valueOutputDuty = argOutDuty;
        motorOut.Output = valueOutputDuty;

        if (isConnected()) {
            talonFX.setControl(motorOut);
        }
    }

    /* Voltage */
    public double getOutputVoltage() {
        if ((outputVoltage != null) && outputVoltage.getStatus().isOK()) {
            return outputVoltage.getValueAsDouble();
        }
        return 0;
    }

    public void setOutputVoltage(final double outputVolts) {
        valueOutputVoltage = outputVolts;
        motorOutV.Output = valueOutputVoltage;

        if (isConnected()) {
            talonFX.setControl(motorOutV);
        }
    }

    /* Torque */
    public double getOutputTorqueCurrent() {
        if ((outputTorqueCurrent != null) && outputTorqueCurrent.getStatus().isOK()) {
            return outputTorqueCurrent.getValueAsDouble();
        }
        return 0;
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

    public void setOutputTorqueCurrent(final double outputTorqueCurrent) {
        valueOutputTorqueCurrent = outputTorqueCurrent;
        motorOutTC.Output = valueOutputTorqueCurrent;

        if (isConnected()) {
            talonFX.setControl(motorOutTC);
        }
    }

    /*
     * Closed Loop
     */
    public double getCLError() {
        if ((clError != null) && clError.getStatus().isOK()) {
            return clError.getValueAsDouble();
        }
        return 0;
    }

    public double getCLOutput() {
        if ((clOutput != null) && clOutput.getStatus().isOK()) {
            return clOutput.getValueAsDouble();
        }
        return 0;
    }

    public void setOutputPosition(final double argPosition, final double argFFVolt) {
        motorPosition.Slot = 0;
        motorPosition.Position = argPosition;
        motorPosition.FeedForward = argFFVolt;

        if (isConnected()) {
            talonFX.setControl(motorPosition);
        }
    }

    public void setOutputVelocity(final double argRPS, final double argFFVolt) {
        motorVelocity.Velocity = argRPS;
        motorVelocity.FeedForward = argFFVolt;

        if (isConnected()) {
            talonFX.setControl(motorVelocity);
        }
    }

    /*
     *
     * Robot Loops
     *
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        /*
         * Check if the controller is still alive
         */
        isConnected = talonFX.isConnected();
    }

    /*
     *
     * Simulation
     *
     */
    public void setSimVelRotations(final double argVelRotations) {
        if (simState != null) {
            simState.setRotorVelocity(argVelRotations);
        }
    }

    public void setSimPosRotations(final double argPositionRotations) {
        if (simState != null) {
            simState.setRawRotorPosition(argPositionRotations);
        }
    }

    public void simulationInit(final double argTimestamp) {
        if (simState == null) {
            /*
             * Get Sim State
             */
            simState = talonFX.getSimState();
        }
    }

    public void simulationPeriodic(final double argTimestamp) {
        if (simState != null) {
            simState.setSupplyVoltage(RobotController.getBatteryVoltage());
        }
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntFXStatus.publish(fxStatus.getName());

        if (motorOutputStatus != null && motorOutputStatus.getStatus().isOK()) {
            switch (motorOutputStatus.getValue()) {
                case Unknown:
                    ntMotorOutputStatus.publish("UNKNOWN");
                    break;
                case Off:
                    ntMotorOutputStatus.publish("Off");
                    break;
                case StaticBraking:
                    ntMotorOutputStatus.publish("Static Braking");
                    break;
                case Motoring:
                    ntMotorOutputStatus.publish("Motoring");
                    break;
                case DiscordantMotoring:
                    ntMotorOutputStatus.publish("Discordant Motoring");
                    break;
                case RegenBraking:
                    ntMotorOutputStatus.publish("Regen Braking");
                    break;
                default:
                    ntMotorOutputStatus.publish("Unknown");
                    break;
            }
        }

        ntOutputDuty.publish(getOutputDuty());
        ntOutputVoltage.publish(getOutputVoltage());
        ntOutputTorqueCurrent.publish(getOutputTorqueCurrent());

        ntSupplyVoltage.publish(getSupplyVoltage());
        ntSupplyCurrent.publish(getSupplyCurrent());
    }
}
