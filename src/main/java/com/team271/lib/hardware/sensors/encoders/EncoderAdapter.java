package com.team271.lib.hardware.sensors.encoders;

import com.team271.lib.hardware.transmissions.GearRatio;

/**
 * Strategy interface that abstracts over encoder types (FX internal, CANCoder). Each adapter wraps
 * a concrete encoder and a {@link GearRatio}, exposing position/velocity in mechanism output units.
 *
 * <p>This eliminates the {@code if (encCANCoder != null) else if (encFX != null)} cascade in
 * TransmissionBase/TransmissionFX by providing a single polymorphic API.
 */
public interface EncoderAdapter {

    /* --- Refresh (called after CTREManager.refreshAll()) --- */

    /** Update cached position/velocity from the underlying status signals. */
    void refresh();

    /* --- Position in mechanism output units --- */

    /** Position in mechanism output units (rotations * ratio * mechanismToUnits). */
    double getPosition();

    /** Absolute position in mechanism output units. Returns 0 if not supported (e.g., FX). */
    double getAbsolutePosition();

    /* --- Velocity in mechanism output units per second --- */

    /** Velocity in mechanism output units per second. */
    double getVelocity();

    /* --- Raw sensor access (rotations) --- */

    /** Position in raw sensor rotations (before gear ratio). */
    double getPositionRotations();

    /** Velocity in raw sensor RPS (before gear ratio). */
    double getVelocityRPS();

    /** Set the encoder position in raw sensor rotations. */
    void setPositionRotations(double rotations);

    /** Reset position to zero. */
    void reset();

    /* --- Mechanism-to-native conversion (for closed-loop setpoints) --- */

    /**
     * Convert a mechanism output unit position to the native encoder units that the CTRE
     * closed-loop expects. For FX: divides by rotorToMechanism * mechanismToUnits. For CANCoder:
     * divides by sensorRelToMechanism * mechanismToUnits.
     */
    double mechanismToNative(double mechanismUnits);

    /** Convert a mechanism output unit velocity to native encoder units per second. */
    double mechanismVelocityToNative(double mechanismUnitsPerSec);

    /* --- Gear ratio management --- */

    /** Returns the current gear ratio. */
    GearRatio getGearRatio();

    /** Update the gear ratio (e.g., after a gear shift). */
    void updateGearRatio(GearRatio newRatio);

    /* --- Lifecycle --- */

    /** Called during robotInit to register signals with CTREManager. */
    void robotInit(double timestamp);

    /** Publish telemetry to NetworkTables. */
    void outputTelemetry();

    /* --- Simulation --- */

    void setSimPosition(double rotations);

    void setSimVelocity(double rps);

    void simulationInit(double timestamp);

    void simulationPeriodic(double timestamp);
}
