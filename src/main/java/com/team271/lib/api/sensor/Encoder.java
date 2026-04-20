package com.team271.lib.api.sensor;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;

/**
 * Vendor-neutral encoder interface for position and velocity measurement.
 *
 * <p>Reports position in rotations and velocity in rotations per second (raw sensor units, before
 * gear ratio conversion). Gear ratio conversion is handled by {@code EncoderAdapter} at the
 * transmission layer.
 *
 * <p>For absolute encoders that retain position across power cycles, see {@link AbsoluteEncoder}.
 */
public interface Encoder extends Lifecycle, Named {

    /** Returns the encoder position in rotations. */
    double getPositionRotations();

    /** Returns the encoder velocity in rotations per second. */
    double getVelocityRPS();

    /**
     * Sets the encoder position.
     *
     * @param argRotations new position in rotations
     */
    void setPositionRotations(double argRotations);

    /** Resets the encoder position to zero. */
    void reset();

    /** Returns true if the encoder is communicating. */
    boolean isConnected();

    /* Simulation */

    /** Sets the simulated position in rotations. */
    void setSimPosition(double argRotations);

    /** Sets the simulated velocity in rotations per second. */
    void setSimVelocity(double argRPS);
}
