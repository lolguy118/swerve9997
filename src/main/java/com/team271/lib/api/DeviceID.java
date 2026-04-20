package com.team271.lib.api;

/**
 * Vendor-neutral device identifier for CAN bus devices.
 *
 * @param bus the CAN bus name (e.g., "rio", "canivore1")
 * @param deviceNumber the device number on the bus
 */
public record DeviceID(String bus, int deviceNumber) {

    /** Returns true if this device is on the same CAN bus as another. */
    public boolean isSameBus(final DeviceID argOther) {
        return bus.equals(argOther.bus);
    }
}
