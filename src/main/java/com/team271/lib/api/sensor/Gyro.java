package com.team271.lib.api.sensor;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;
import edu.wpi.first.math.geometry.Rotation2d;

/**
 * Vendor-neutral gyroscope/IMU interface.
 *
 * <p>Reports yaw, pitch, roll in degrees and yaw rate in degrees per second.
 */
public interface Gyro extends Lifecycle, Named {

    /** Returns the yaw angle in degrees (continuous, not wrapped). */
    double getYaw();

    /** Returns the yaw rate in degrees per second. */
    double getYawRate();

    /** Returns the roll angle in degrees. */
    double getRoll();

    /** Returns the pitch angle in degrees. */
    double getPitch();

    /** Returns the heading as a WPILib Rotation2d. */
    Rotation2d getHeading();

    /** Resets the yaw to zero. */
    void reset();

    /**
     * Sets the yaw angle.
     *
     * @param argDegrees new yaw angle in degrees
     */
    void setYaw(double argDegrees);

    /* Simulation */

    /**
     * Sets the simulated yaw angle.
     *
     * @param argDegrees simulated yaw in degrees
     */
    void setSimYaw(double argDegrees);
}
