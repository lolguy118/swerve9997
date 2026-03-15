package com.team271.lib.hardware;

import com.ctre.phoenix6.CANBus;

/**
 * Composite key identifying a CAN device by its device number and bus name.
 * Immutable and safe for use as a HashMap key.
 */
public class CANDeviceID {
    private final int mDeviceNumber;
    private final String mBus;
    private final CANBus mCANBus;

    public CANDeviceID(final int argDeviceNumber, final String argBus) {
        mDeviceNumber = argDeviceNumber;
        mBus = argBus;
        mCANBus = new CANBus(mBus);
    }

    /** Use the RIO bus (empty string). */
    public CANDeviceID(final int argDeviceNumber) {
        this(argDeviceNumber, "");
    }

    public final int getDeviceNumber() {
        return mDeviceNumber;
    }

    public final String getBus() {
        return mBus;
    }

    /**
     * Returns a CTRE {@link CANBus} object for use with Phoenix 6 v26+ device constructors.
     */
    public final CANBus getCANBus() {
        return mCANBus;
    }

    public final boolean isSameBus(final CANDeviceID argOther) {
        return mBus.equals(argOther.mBus);
    }

    @Override
    public final String toString() {
        return "Bus: " + mBus + " ID: " + mDeviceNumber;
    }

    @Override
    public final boolean equals(final Object argOther) {
        if (argOther == this) {
            return true;
        }

        if (!(argOther instanceof CANDeviceID)) {
            return false;
        }

        CANDeviceID c = (CANDeviceID) argOther;

        return (mDeviceNumber == c.mDeviceNumber) && mBus.equals(c.mBus);
    }

    @Override
    public final int hashCode() {
        return 31 * (31 * 17 + mDeviceNumber) + mBus.hashCode();
    }
}
