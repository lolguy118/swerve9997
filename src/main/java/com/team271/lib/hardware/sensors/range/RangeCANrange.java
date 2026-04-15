package com.team271.lib.hardware.sensors.range;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.*;
import com.ctre.phoenix6.sim.CANrangeSimState;
import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import edu.wpi.first.wpilibj.RobotController;

public class RangeCANrange extends RangeCTRE {
    /*
     * CANCoder
     */
    protected CANDeviceID rangeDeviceID;
    protected final CANrangeConfiguration rangeConfig = new CANrangeConfiguration();
    protected CANrange range;
    protected CANrangeSimState simState;

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
    public RangeCANrange(
            final TObj argParent,
            final String argName,
            final CANDeviceID argCANID,
            final double argUpdateFreqHz) {
        super(argParent, "(CANrange)" + argName, RangeType.CANRANGE, argUpdateFreqHz);

        rangeDeviceID = argCANID;

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
    protected void create() {
        /*
         * Create CANrange
         */
        range = new CANrange(rangeDeviceID.getDeviceNumber(), rangeDeviceID.getCANBus());

        // rangeConfig.FovParams.FOVRangeX = 6.75;
        // rangeConfig.FovParams.FOVRangeY = 6.75;
        rangeConfig.FovParams.FOVRangeX = 15;
        rangeConfig.FovParams.FOVRangeY = 15;

        rangeConfig.ToFParams.UpdateMode = UpdateModeValue.ShortRangeUserFreq;
        rangeConfig.ToFParams.UpdateFrequency = 100;

        ctreStatus = applyConfig();

        simState = range.getSimState();
    }

    public CANrangeConfiguration getConfig() {
        return rangeConfig;
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
        sigDist = range.getDistance();
        CTREManager.addSignal(sigDist, updateFreqHz);
    }

    @Override
    public StatusCode applyConfig() {
        /* Retry config apply up to retryCountCAN times, report if failure */
        for (int i = 0; i < ConstantsLib.CAN_RETRY_COUNT; ++i) {
            ctreStatus =
                    range.getConfigurator().apply(rangeConfig, ConstantsLib.CAN_LONG_TIMEOUT_MS);
            if (ctreStatus.isOK()) break;
        }

        return ctreStatus;
    }

    /*
     *
     * Refresh
     *
     */
    @Override
    public void refresh() {
        /* Since these are already refreshed we don't need to inline the refresh call */
        if ((sigDist != null) && sigDist.getStatus().isOK()) {
            dist = sigDist.getValue().in(Meters);
        }
    }

    /*
     *
     * Simulation
     *
     */
    @Override
    public void simulationPeriodic(final double argTimestamp) {
        simState.setSupplyVoltage(RobotController.getBatteryVoltage());
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
