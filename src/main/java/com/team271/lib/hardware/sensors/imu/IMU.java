package com.team271.lib.hardware.sensors.imu;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;
import edu.wpi.first.math.geometry.Rotation2d;

/**
 * Inertial measurement unit interface for any IMU vendor (CTRE Pigeon2, NavX, etc.).
 *
 * <p>Provides heading, orientation, and angular velocity readings. All angles are in degrees. All
 * implementations must provide simulation support via {@link #setSimYaw(double)}.
 *
 * <p>Implementations:
 *
 * <ul>
 *   <li>{@link IMUPigeon2} — CTRE Pigeon 2.0 with latency-compensated yaw
 * </ul>
 */
public interface IMU extends Lifecycle, Named {

    /** Returns the yaw (heading) in degrees. Continuous — may exceed [-180, 180]. */
    double getYaw();

    /** Returns the yaw angular velocity in degrees per second. */
    double getYawRate();

    /** Returns the roll in degrees. */
    double getRoll();

    /** Returns the pitch in degrees. */
    double getPitch();

    /** Returns the heading as a WPILib {@link Rotation2d}. */
    Rotation2d getHeading();

    /** Resets the yaw to zero. */
    void reset();

    /** Sets the yaw to the specified value in degrees. */
    void setYaw(double argDegrees);

    /* --- Simulation --- */

    /** Sets the simulated yaw in degrees. */
    void setSimYaw(double argDegrees);
}
