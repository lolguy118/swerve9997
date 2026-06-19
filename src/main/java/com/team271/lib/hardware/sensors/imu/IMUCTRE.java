package com.team271.lib.hardware.sensors.imu;

import com.ctre.phoenix6.StatusCode;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.nt.NTEntry;

@SuppressWarnings("NullAway.Init")
public abstract class IMUCTRE extends IMUBase {
    /*
     * IMU
     */
    protected CANDeviceID imuDeviceID;
    protected StatusCode ctreStatus;
    protected double updateFreqHz = 250.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntCTREStatus = new NTEntry(table, "CTRE Status", "");

    /*
     *
     * Constructors
     *
     */
    protected IMUCTRE(
            final TObj argParent,
            final String argName,
            final IMUType argIMUType,
            final CANDeviceID argCANID,
            final double argUpdateFreqHz) {
        super(argParent, "(IMUCTRE)" + argName, argIMUType);

        imuDeviceID = argCANID;

        /*
         * Store Update Frequency in Hz
         */
        updateFreqHz = argUpdateFreqHz;
    }

    /*
     *
     * IMU
     *
     */

    /*
     *
     * Robot
     *
     */
    public StatusCode applyConfig() {
        return ctreStatus;
    }

    /*
     *
     * Refresh
     *
     */
    public abstract void refresh();

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        if (ctreStatus != null) {
            ntCTREStatus.publish(ctreStatus.getDescription());
        }
    }
}
