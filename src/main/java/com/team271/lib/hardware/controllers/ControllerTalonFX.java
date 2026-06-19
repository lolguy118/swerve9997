package com.team271.lib.hardware.controllers;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
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
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import java.util.EnumSet;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway.Init")
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

        DEVICE_TEMP,
        ACCELERATION,

        ALL
    }

    EnumSet<Signals> signals = EnumSet.of(Signals.ALL);

    protected StatusCode fxStatus = StatusCode.OK;
    protected CANBus canBus;
    protected TalonFX talonFX;
    protected TalonFXConfiguration config;
    protected TalonFXSimState simState;

    /*
     * TalonFX
     */
    protected static final double UPDATE_FREQ_HZ_FAULTS = 250.0;
    protected FaultMonitor faultMonitor;

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

    /* Device Temperature */
    protected static final double UPDATE_FREQ_HZ_DEVICE_TEMP = 250.0;
    protected StatusSignal<edu.wpi.first.units.measure.Temperature> deviceTemp;

    /* Acceleration */
    protected static final double UPDATE_FREQ_HZ_ACCELERATION = 250.0;
    protected StatusSignal<edu.wpi.first.units.measure.AngularAcceleration> acceleration;

    /* StrictFollower and CoastOut control requests */
    protected final CoastOut motorCoastOut = new CoastOut();

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

        ControllerStatus followStatus = follow(argLeader, argOpposeLeader);
        if (followStatus != ControllerStatus.OK) {
            DriverStation.reportError(
                    getName()
                            + ": follow() failed for leader "
                            + argLeader.getName()
                            + " (status="
                            + followStatus
                            + ")",
                    false);
        }
    }

    /*
     *
     * Core
     *
     */
    @Override
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

            CTREManager.addSignal(motorOutputStatus, UPDATE_FREQ_HZ_MOTOR_OUTPUT_STATUS);

            /*
             * Clear Sticky Faults
             * Setup Fault Monitor
             */
            talonFX.clearStickyFaults();

            faultMonitor = new FaultMonitor(this, getName());
            faultMonitor.addFault(
                    "BootDuringEnable",
                    talonFX.getStickyFault_BootDuringEnable(),
                    UPDATE_FREQ_HZ_FAULTS);
            faultMonitor.addFault(
                    "DeviceTemp", talonFX.getStickyFault_DeviceTemp(), UPDATE_FREQ_HZ_FAULTS);
            faultMonitor.addFault(
                    "ProcTemp", talonFX.getStickyFault_ProcTemp(), UPDATE_FREQ_HZ_FAULTS);
            faultMonitor.addFault(
                    "Hardware", talonFX.getStickyFault_Hardware(), UPDATE_FREQ_HZ_FAULTS);
            faultMonitor.addFault(
                    "Undervoltage", talonFX.getStickyFault_Undervoltage(), UPDATE_FREQ_HZ_FAULTS);
            faultMonitor.addFault(
                    "BridgeBrownout",
                    talonFX.getStickyFault_BridgeBrownout(),
                    UPDATE_FREQ_HZ_FAULTS);

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
            CTREManager.addSignal(supplyVoltage, UPDATE_FREQ_HZ_SUPPLY_VOLT);
        }

        /*
         * Get Signal: Supply Current
         */
        if (signals.contains(Signals.SUPPLY_CURRENT) || signals.contains(Signals.ALL)) {
            supplyCurrent = talonFX.getSupplyCurrent();
            CTREManager.addSignal(supplyCurrent, UPDATE_FREQ_HZ_SUPPLY_CURRENT);
        }

        /*
         * Get Signal: Motor Output Duty
         */
        if (signals.contains(Signals.OUTPUT_DUTY) || signals.contains(Signals.ALL)) {
            outputDuty = talonFX.getDutyCycle();
            CTREManager.addSignal(outputDuty, UPDATE_FREQ_HZ_OUTPUT_DUTY);
        }

        /*
         * Get Signal: Motor Output Voltage
         */
        if (signals.contains(Signals.OUTPUT_VOLT) || signals.contains(Signals.ALL)) {
            outputVoltage = talonFX.getMotorVoltage();
            CTREManager.addSignal(outputVoltage, UPDATE_FREQ_HZ_OUTPUT_VOLT);
        }

        /*
         * Get Signal: Motor Output Torque Current
         */
        if (signals.contains(Signals.OUTPUT_TORQUE_CURRENT) || signals.contains(Signals.ALL)) {
            outputTorqueCurrent = talonFX.getTorqueCurrent();
            CTREManager.addSignal(outputTorqueCurrent, UPDATE_FREQ_HZ_OUTPUT_TORQUE_CURRENT);
        }

        /*
         * Get Signal: Closed Loop Error
         */
        if (signals.contains(Signals.CLOSED_LOOP_ERROR) || signals.contains(Signals.ALL)) {
            clError = talonFX.getClosedLoopError();
            CTREManager.addSignal(clError, UPDATE_FREQ_HZ_CLOSED_LOOP);
        }

        /*
         * Get Signal: Closed Loop Output
         */
        if (signals.contains(Signals.CLOSED_LOOP_OUTPUT) || signals.contains(Signals.ALL)) {
            clOutput = talonFX.getClosedLoopOutput();
            CTREManager.addSignal(clOutput, UPDATE_FREQ_HZ_CLOSED_LOOP);
        }

        /*
         * Get Signal: Device Temperature
         */
        if (signals.contains(Signals.DEVICE_TEMP) || signals.contains(Signals.ALL)) {
            deviceTemp = talonFX.getDeviceTemp();
            CTREManager.addSignal(deviceTemp, UPDATE_FREQ_HZ_DEVICE_TEMP);
        }

        /*
         * Get Signal: Acceleration
         */
        if (signals.contains(Signals.ACCELERATION) || signals.contains(Signals.ALL)) {
            acceleration = talonFX.getAcceleration();
            CTREManager.addSignal(acceleration, UPDATE_FREQ_HZ_ACCELERATION);
        }

        if (faultMonitor != null) {
            faultMonitor.registerSignals();
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
    @Override
    public ControllerStatus applyConfig() {
        ControllerStatus status = ControllerStatus.UNKNOWN;

        /* Retry config apply — 3 retries x 20ms = 60ms worst case (within loop budget) */
        for (int i = 0; i < ConstantsLib.CAN_CONFIG_APPLY_RETRIES; ++i) {
            fxStatus =
                    talonFX.getConfigurator()
                            .apply(config, ConstantsLib.CAN_CONFIG_APPLY_TIMEOUT_SEC);
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

    /* Direction */
    @Override
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

    @Override
    public MotorDirection getDirection() {
        if (config.MotorOutput.Inverted == InvertedValue.Clockwise_Positive) {
            return MotorDirection.CW;
        }
        return MotorDirection.CCW;
    }

    /* Current Limit - Stator */
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

    /* Current Limit - Supply */
    @Override
    public void setCurrentLimitSupply(final boolean argEnable, final double argSupplyCurrent) {
        config.CurrentLimits.SupplyCurrentLimitEnable = argEnable;
        config.CurrentLimits.SupplyCurrentLimit = argSupplyCurrent;
    }

    @Override
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

    /* Voltage Limit */
    @Override
    public void setVoltagePeak(
            final double argFwdVoltage, final double argRevVoltage, final double argTimeFilter) {
        config.Voltage.PeakForwardVoltage = argFwdVoltage;
        config.Voltage.PeakReverseVoltage = argRevVoltage;
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

    /*
     *
     * Config - Open Loop
     *
     */
    @Override
    public void setRampOpenLoopDuty(final double argRampRateSec) {
        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = argRampRateSec;
    }

    @Override
    public double getRampOpenLoopDuty() {
        return config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod;
    }

    @Override
    public void setRampOpenLoopVoltage(final double argRampRateSec) {
        config.OpenLoopRamps.VoltageOpenLoopRampPeriod = argRampRateSec;
    }

    @Override
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
    @Override
    public void setRampClosedLoopDuty(final double argRampRateSec) {
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = argRampRateSec;
    }

    @Override
    public double getRampClosedLoopDuty() {
        return config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod;
    }

    @Override
    public void setRampClosedLoopVoltage(final double argRampRateSec) {
        config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = argRampRateSec;
    }

    @Override
    public double getRampClosedLoopVoltage() {
        return config.ClosedLoopRamps.VoltageClosedLoopRampPeriod;
    }

    public void setRampClosedLoopTorque(final double argRampRateSec) {
        config.ClosedLoopRamps.TorqueClosedLoopRampPeriod = argRampRateSec;
    }

    public double getRampClosedLoopTorque() {
        return config.ClosedLoopRamps.TorqueClosedLoopRampPeriod;
    }

    @Override
    public void setTolerance(final double argTolerance) {
        // Unused
    }

    @Override
    public double getTolerance() {
        return 0;
    }

    /* PID Values */

    /**
     * Slot accessor record — provides uniform read/write access to any PID slot's gains. CTRE's
     * Slot0Configs, Slot1Configs, Slot2Configs share the same fields but not a common type, so this
     * record bridges the gap.
     */
    private record SlotAccess(
            java.util.function.DoubleSupplier getP,
            java.util.function.DoubleConsumer setP,
            java.util.function.DoubleSupplier getI,
            java.util.function.DoubleConsumer setI,
            java.util.function.DoubleSupplier getD,
            java.util.function.DoubleConsumer setD,
            java.util.function.DoubleSupplier getV,
            java.util.function.DoubleSupplier getS,
            java.util.function.DoubleConsumer setV,
            java.util.function.DoubleConsumer setS,
            java.util.function.DoubleSupplier getG,
            java.util.function.DoubleConsumer setG,
            java.util.function.DoubleSupplier getA,
            java.util.function.DoubleConsumer setA) {}

    @Nullable
    private SlotAccess slotAccess(final int argSlot) {
        return switch (argSlot) {
            case 0 ->
                    new SlotAccess(
                            () -> config.Slot0.kP, v -> config.Slot0.kP = v,
                            () -> config.Slot0.kI, v -> config.Slot0.kI = v,
                            () -> config.Slot0.kD, v -> config.Slot0.kD = v,
                            () -> config.Slot0.kV, () -> config.Slot0.kS,
                            v -> config.Slot0.kV = v, v -> config.Slot0.kS = v,
                            () -> config.Slot0.kG, v -> config.Slot0.kG = v,
                            () -> config.Slot0.kA, v -> config.Slot0.kA = v);
            case 1 ->
                    new SlotAccess(
                            () -> config.Slot1.kP, v -> config.Slot1.kP = v,
                            () -> config.Slot1.kI, v -> config.Slot1.kI = v,
                            () -> config.Slot1.kD, v -> config.Slot1.kD = v,
                            () -> config.Slot1.kV, () -> config.Slot1.kS,
                            v -> config.Slot1.kV = v, v -> config.Slot1.kS = v,
                            () -> config.Slot1.kG, v -> config.Slot1.kG = v,
                            () -> config.Slot1.kA, v -> config.Slot1.kA = v);
            case 2 ->
                    new SlotAccess(
                            () -> config.Slot2.kP, v -> config.Slot2.kP = v,
                            () -> config.Slot2.kI, v -> config.Slot2.kI = v,
                            () -> config.Slot2.kD, v -> config.Slot2.kD = v,
                            () -> config.Slot2.kV, () -> config.Slot2.kS,
                            v -> config.Slot2.kV = v, v -> config.Slot2.kS = v,
                            () -> config.Slot2.kG, v -> config.Slot2.kG = v,
                            () -> config.Slot2.kA, v -> config.Slot2.kA = v);
            default -> null;
        };
    }

    @Override
    public void setPSlot(final int argSlot, final double argSetP) {
        SlotAccess s = slotAccess(argSlot);
        if (s != null) {
            s.setP.accept(argSetP);
        }
    }

    @Override
    public double getPSlot(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getP.getAsDouble() : 0;
    }

    @Override
    public void setISlot(final int argSlot, final double argSetI) {
        SlotAccess s = slotAccess(argSlot);
        if (s != null) {
            s.setI.accept(argSetI);
        }
    }

    @Override
    public double getISlot(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getI.getAsDouble() : 0;
    }

    @Override
    public void setDSlot(final int argSlot, final double argSetD) {
        SlotAccess s = slotAccess(argSlot);
        if (s != null) {
            s.setD.accept(argSetD);
        }
    }

    @Override
    public double getDSlot(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getD.getAsDouble() : 0;
    }

    @Override
    public double getVSlot(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getV.getAsDouble() : 0;
    }

    @Override
    public double getSSlot(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getS.getAsDouble() : 0;
    }

    @Override
    public void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        SlotAccess s = slotAccess(argSlot);
        if (s != null) {
            s.setP.accept(argP);
            s.setI.accept(argI);
            s.setD.accept(argD);
            s.setV.accept(argV);
            s.setS.accept(argS);
        }
    }

    /*
     *
     * Outputs
     *
     */
    @Override
    public void stop() {
        talonFX.stopMotor();
    }

    /*
     * Open Loop
     */
    /* Duty Cycle */
    @Override
    public double getOutputDuty() {
        if ((outputDuty != null) && outputDuty.getStatus().isOK()) {
            return outputDuty.getValueAsDouble();
        }
        return 0;
    }

    @Override
    public void setOutputDuty(final double argOutDuty) {
        valueOutputDuty = argOutDuty;
        motorOut.Output = valueOutputDuty;

        if (isConnected()) {
            talonFX.setControl(motorOut);
        }
    }

    /* Voltage */
    @Override
    public double getOutputVoltage() {
        if ((outputVoltage != null) && outputVoltage.getStatus().isOK()) {
            return outputVoltage.getValueAsDouble();
        }
        return 0;
    }

    @Override
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
            talonFX.setControl(motorPosition);
        }
    }

    @Override
    public void setOutputVelocity(final double argRPS, final int argSlot, final double argFFVolt) {
        motorVelocity.Slot = argSlot;
        motorVelocity.Velocity = argRPS;
        motorVelocity.FeedForward = argFFVolt;

        if (isConnected()) {
            talonFX.setControl(motorVelocity);
        }
    }

    /*
     * StrictFollower — mirrors leader output exactly (no alignment control)
     */
    public void setStrictFollow(final ControllerBase argLeader) {
        if (isConnected()) {
            talonFX.setControl(new StrictFollower(argLeader.getIDNum()));
        }
    }

    /*
     * CoastOut — commands motor to coast (releases all control)
     */
    public void coast() {
        if (isConnected()) {
            talonFX.setControl(motorCoastOut);
        }
    }

    /*
     *
     * Gravity / Acceleration Feedforward
     *
     */
    @Override
    public void setGravityGain(final int argSlot, final double argKG) {
        SlotAccess s = slotAccess(argSlot);
        if (s != null) {
            s.setG.accept(argKG);
        }
    }

    @Override
    public double getGravityGain(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getG.getAsDouble() : 0;
    }

    @Override
    public void setAccelGain(final int argSlot, final double argKA) {
        SlotAccess s = slotAccess(argSlot);
        if (s != null) {
            s.setA.accept(argKA);
        }
    }

    @Override
    public double getAccelGain(final int argSlot) {
        SlotAccess s = slotAccess(argSlot);
        return (s != null) ? s.getA.getAsDouble() : 0;
    }

    /*
     *
     * Gravity Type
     *
     */
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

    /*
     *
     * Continuous Wrap
     *
     */
    @Override
    public void setContinuousWrap(final boolean argEnabled) {
        config.ClosedLoopGeneral.ContinuousWrap = argEnabled;
    }

    @Override
    public boolean getContinuousWrap() {
        return config.ClosedLoopGeneral.ContinuousWrap;
    }

    /*
     *
     * Software Limits
     *
     */
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
     *
     * New Signal Getters
     *
     */
    /** Returns the device temperature in degrees Celsius. */
    public double getDeviceTemp() {
        if ((deviceTemp != null) && deviceTemp.getStatus().isOK()) {
            return deviceTemp.getValueAsDouble();
        }
        return 0;
    }

    /** Returns the rotor acceleration in rotations per second squared. */
    public double getAcceleration() {
        if ((acceleration != null) && acceleration.getStatus().isOK()) {
            return acceleration.getValueAsDouble();
        }
        return 0;
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

        if (faultMonitor != null) {
            faultMonitor.refresh();
        }
    }

    /*
     *
     * Simulation
     *
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
            /*
             * Get Sim State
             */
            simState = talonFX.getSimState();

            // Set CTRE motor type for hardware-accurate current and torque simulation
            switch (motor.getMotorType()) {
                case KRAKENX60:
                    simState.setMotorType(TalonFXSimState.MotorType.KrakenX60);
                    break;
                case KRAKENX44:
                    simState.setMotorType(TalonFXSimState.MotorType.KrakenX44);
                    break;
                default:
                    // Falcon500, NEO, etc. — CTRE only supports Kraken types natively;
                    // default to KrakenX60 as best approximation
                    simState.setMotorType(TalonFXSimState.MotorType.KrakenX60);
                    break;
            }

            // Set orientation for counter-rotating followers
            if (opposeLeader) {
                simState.Orientation = ChassisReference.Clockwise_Positive;
            }
        }
    }

    @Override
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

        if (faultMonitor != null) {
            faultMonitor.outputTelemetry();
        }
    }
}
