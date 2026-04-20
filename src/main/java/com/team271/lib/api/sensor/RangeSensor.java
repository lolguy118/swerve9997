package com.team271.lib.api.sensor;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;

/**
 * Vendor-neutral range/distance sensor interface.
 *
 * <p>Reports distance in meters and connection status.
 */
public interface RangeSensor extends Lifecycle, Named {

    /** Returns the measured distance in meters. */
    double getDistanceMeters();

    /** Returns true if the sensor is communicating. */
    boolean isConnected();
}
