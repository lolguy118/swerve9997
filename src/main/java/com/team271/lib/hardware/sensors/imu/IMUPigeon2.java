package com.team271.lib.hardware.sensors.imu;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.sim.Pigeon2SimState;
import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.FaultMonitor;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj.RobotController;

public class IMUPigeon2 extends IMUCTRE {
    /*
     * IMU
     */
    private final Pigeon2Configuration imuConfig = new Pigeon2Configuration();
    private Pigeon2 imu;
    private Pigeon2SimState simState;

    protected StatusSignal<Angle> sigYaw;
    protected StatusSignal<AngularVelocity> sigYawRate;
    protected StatusSignal<Angle> sigRoll;
    protected StatusSignal<Angle> sigPitch;

    protected FaultMonitor faultMonitor;

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
    public IMUPigeon2(
            final TObj argParent,
            final String argName,
            final CANDeviceID argCANID,
            final double argUpdateFreqHz) {
        super(argParent, "(IMUPigeon2)" + argName, IMUType.PIGEON2, argCANID, argUpdateFreqHz);

        create();
    }

    /*
     *
     * Passthrough Getters
     *
     */

    /** Passthrough — returns the raw CTRE Pigeon2 device. */
    public Pigeon2 getPigeon2() {
        return imu;
    }

    /** Passthrough — returns the CTRE Pigeon2 configuration. */
    public Pigeon2Configuration getConfig() {
        return imuConfig;
    }

    /** Passthrough — returns the CTRE Pigeon2 simulation state. */
    public Pigeon2SimState getSimState() {
        return simState;
    }

    /*
     *
     * IMU
     *
     */
    protected void create() {
        /*
         * Create CANCoder
         */
        imu = new Pigeon2(imuDeviceID.getDeviceNumber(), imuDeviceID.getCANBus());

        CTREManager.addDevice(imu);

        /*
         * Get Sim State
         */
        simState = imu.getSimState();

        /*
         * Gyro Config
         */
        ctreStatus = applyConfig();

        imu.setYaw(0, ConstantsLib.CAN_LONG_TIMEOUT_MS);

        faultMonitor = new FaultMonitor(this, getName());
        faultMonitor.addFault("BootDuringEnable", imu.getStickyFault_BootDuringEnable(), 250.0);
        faultMonitor.addFault("Hardware", imu.getStickyFault_Hardware(), 250.0);
        faultMonitor.addFault("Undervoltage", imu.getStickyFault_Undervoltage(), 250.0);
        faultMonitor.addFault("SatMagnetometer", imu.getStickyFault_SaturatedMagnetometer(), 250.0);
        faultMonitor.addFault(
                "SatAccelerometer", imu.getStickyFault_SaturatedAccelerometer(), 250.0);
        faultMonitor.addFault("SatGyroscope", imu.getStickyFault_SaturatedGyroscope(), 250.0);
    }

    @Override
    public StatusCode applyConfig() {
        /* Retry config apply up to retryCountCAN times, report if failure */
        for (int i = 0; i < ConstantsLib.CAN_RETRY_COUNT; ++i) {
            ctreStatus = imu.getConfigurator().apply(imuConfig, ConstantsLib.CAN_LONG_TIMEOUT_MS);
            if (ctreStatus.isOK()) {
                break;
            }
        }

        return ctreStatus;
    }

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        /*
         * Get IMU Objects
         */
        sigYaw = imu.getYaw();
        CTREManager.addSignal(sigYaw, updateFreqHz);

        sigYawRate = imu.getAngularVelocityZWorld();
        CTREManager.addSignal(sigYawRate, updateFreqHz);

        sigRoll = imu.getRoll();
        CTREManager.addSignal(sigRoll, updateFreqHz);

        sigPitch = imu.getPitch();
        CTREManager.addSignal(sigPitch, updateFreqHz);

        if (faultMonitor != null) {
            faultMonitor.registerSignals();
        }
    }

    /**
     * Returns the heading as a Rotation2d using the latency-compensated yaw from {@link
     * #refresh()}. Callers must ensure refresh() is called each cycle (e.g., in
     * robotPeriodicBefore) for accurate data.
     */
    @Override
    public Rotation2d getHeading() {
        return Rotation2d.fromDegrees(yaw);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        refresh();

        if (faultMonitor != null) {
            faultMonitor.refresh();
        }
    }

    @Override
    public void setYaw(final double argDegrees) {
        imu.setYaw(argDegrees, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        yaw = argDegrees;
    }

    /*
     *
     * Simulation
     *
     */
    @Override
    public void setSimYaw(final double argDegrees) {
        if (simState != null) {
            simState.setRawYaw(argDegrees);
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
     * Refresh
     *
     */
    @Override
    public void refresh() {
        /* Yaw: prefer latency-compensated value, fall back to raw signal */
        if ((sigYaw != null) && sigYaw.getStatus().isOK()) {
            if ((sigYawRate != null) && sigYawRate.getStatus().isOK()) {
                yaw = BaseStatusSignal.getLatencyCompensatedValue(sigYaw, sigYawRate).in(Degree);
            } else {
                yaw = sigYaw.getValue().in(Degree);
            }
        }

        if ((sigYawRate != null) && sigYawRate.getStatus().isOK()) {
            yawRate = sigYawRate.getValue().in(DegreesPerSecond);
        }

        if ((sigRoll != null) && sigRoll.getStatus().isOK()) {
            roll = sigRoll.getValue().in(Degree);
        }

        if ((sigPitch != null) && sigPitch.getStatus().isOK()) {
            pitch = sigPitch.getValue().in(Degree);
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

        if (faultMonitor != null) {
            faultMonitor.outputTelemetry();
        }
    }
}
