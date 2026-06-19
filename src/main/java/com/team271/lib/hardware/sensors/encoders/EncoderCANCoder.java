package com.team271.lib.hardware.sensors.encoders;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.*;
import com.ctre.phoenix6.sim.CANcoderSimState;
import com.ctre.phoenix6.sim.ChassisReference;
import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.FaultMonitor;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.RobotController;

@SuppressWarnings("NullAway.Init")
public class EncoderCANCoder extends EncoderCTRE {
    /*
     * CANCoder
     */
    protected CANDeviceID encDeviceID;
    protected final CANcoderConfiguration encConfig = new CANcoderConfiguration();
    protected CANcoder enc;
    protected CANcoderSimState simState;
    protected FaultMonitor faultMonitor;

    protected StatusSignal<Angle> sigPosBoot;
    protected double posBoot = 0.0;

    protected StatusSignal<Angle> sigPosAbs;
    protected double posAbs = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntPosBoot = new NTEntry(table, "PosBoot", 0.0);
    final NTEntry ntPosAbs = new NTEntry(table, "PosAbs", 0.0);
    final NTEntry ntMagnetOffset = new NTEntry(table, "Magnet Offset", 0.0);

    /*
     *
     * Constructors
     *
     */
    public EncoderCANCoder(
            final TObj argParent,
            final String argName,
            final CANDeviceID argCANID,
            final EncoderDirection argEncoderDirection,
            final double argUpdateFreqHz) {
        super(
                argParent,
                "(CANCoder)" + argName,
                EncoderType.CANCODER,
                argEncoderDirection,
                argUpdateFreqHz);

        encDeviceID = argCANID;

        /*
         * Create Objects
         */
        create();
    }

    /*
     *
     * Encoder
     *
     */
    @Override
    protected void create() {
        /*
         * Create CANCoder
         */
        enc = new CANcoder(encDeviceID.getDeviceNumber(), encDeviceID.getCANBus());
        CTREManager.addDevice(enc);

        simState = enc.getSimState();

        encConfig.MagnetSensor.AbsoluteSensorDiscontinuityPoint = 0.5;

        if (direction == EncoderDirection.CW) {
            encConfig.MagnetSensor.SensorDirection = SensorDirectionValue.Clockwise_Positive;
            simState.Orientation = ChassisReference.Clockwise_Positive;
        } else {
            encConfig.MagnetSensor.SensorDirection = SensorDirectionValue.CounterClockwise_Positive;
            simState.Orientation = ChassisReference.CounterClockwise_Positive;
        }

        encConfig.MagnetSensor.MagnetOffset = 0;

        ctreStatus = applyConfig();

        faultMonitor = new FaultMonitor(this, getName());
        faultMonitor.addFault("BootDuringEnable", enc.getStickyFault_BootDuringEnable(), 250.0);
        faultMonitor.addFault("Undervoltage", enc.getStickyFault_Undervoltage(), 250.0);
        faultMonitor.addFault("BadMagnet", enc.getStickyFault_BadMagnet(), 250.0);
    }

    public CANcoderConfiguration getConfig() {
        return encConfig;
    }

    /** Passthrough — returns the raw CTRE CANcoder device. */
    public CANcoder getCANcoder() {
        return enc;
    }

    /** Passthrough — returns the CTRE CANcoder simulation state. */
    public CANcoderSimState getSimState() {
        return simState;
    }

    public void setMagnetSensor(final double argSensorRange) {
        encConfig.MagnetSensor.AbsoluteSensorDiscontinuityPoint = argSensorRange;
    }

    public void setMagnetOffset(final double argMagnetOffset) {
        encConfig.MagnetSensor.MagnetOffset = argMagnetOffset;
    }

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        /*
         * Get Position and Velocity Objects
         */
        sigPos = enc.getPosition();
        CTREManager.addSignal(sigPos, updateFreqHz);

        sigPosBoot = enc.getPositionSinceBoot();
        CTREManager.addSignal(sigPosBoot, updateFreqHz);

        sigPosAbs = enc.getAbsolutePosition();
        CTREManager.addSignal(sigPosAbs, updateFreqHz);

        sigVel = enc.getVelocity();
        CTREManager.addSignal(sigVel, updateFreqHz);

        if (faultMonitor != null) {
            faultMonitor.registerSignals();
        }
    }

    @Override
    public void reset() {
        super.reset();

        posBoot = 0.0;
        posAbs = 0.0;
    }

    @Override
    public StatusCode applyConfig() {
        /* Retry config apply up to retryCountCAN times, report if failure */
        for (int i = 0; i < ConstantsLib.CAN_RETRY_COUNT; ++i) {
            ctreStatus = enc.getConfigurator().apply(encConfig, ConstantsLib.CAN_LONG_TIMEOUT_MS);
            if (ctreStatus.isOK()) {
                break;
            }
        }

        return ctreStatus;
    }

    /*
     * Position Relative - Rotations
     */
    @Override
    public void setPosRotations(final double argPositionRotations) {
        super.setPosRotations(argPositionRotations);

        if (enc != null) {
            enc.setPosition(posRotations, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    /*
     * Position Boot Relative - Rotations
     */
    public double getPosBootRotations() {
        return posBoot;
    }

    /*
     * Position Abs - Rotations
     */
    public void setPosAbsRotations(final double argPosition) {
        posAbs = argPosition;
    }

    public double getPosAbsRotations() {
        return posAbs;
    }

    /*
     *
     * Refresh
     *
     */
    @Override
    public void refresh() {
        super.refresh();

        /* Latency-compensate boot and absolute positions using velocity signal */
        if ((sigVel != null) && sigVel.getStatus().isOK()) {
            if ((sigPosBoot != null) && sigPosBoot.getStatus().isOK()) {
                posBoot =
                        BaseStatusSignal.getLatencyCompensatedValue(sigPosBoot, sigVel)
                                .in(Rotations);
            }

            if ((sigPosAbs != null) && sigPosAbs.getStatus().isOK()) {
                posAbs =
                        BaseStatusSignal.getLatencyCompensatedValue(sigPosAbs, sigVel)
                                .in(Rotations);
            }
        }

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
    public void setSimPosRotations(final double argPositionRotations) {
        if (simState != null) {
            simState.setRawPosition(argPositionRotations);
        }
    }

    @Override
    public void setSimVelRotations(final double argVelRotations) {
        if (simState != null) {
            simState.setVelocity(argVelRotations);
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

        ntPosBoot.publish(getPosBootRotations());
        ntPosAbs.publish(getPosAbsRotations());
        ntMagnetOffset.publish(encConfig.MagnetSensor.MagnetOffset);

        if (faultMonitor != null) {
            faultMonitor.outputTelemetry();
        }
    }
}
