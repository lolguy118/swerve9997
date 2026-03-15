package com.team271.lib.hardware;

public class CANDeviceID {
    private final int mDeviceNumber;
    private final String mBus;

    private final String mString;

    public CANDeviceID(final int argDeviceNumber, final String argBus) {
        mDeviceNumber = argDeviceNumber;
        mBus = argBus;

        mString = "Bus: " + mBus + "ID: " + mDeviceNumber;
    }

    // Use the default bus name (empty string).
    public CANDeviceID(final int argDeviceNumber) {
        this(argDeviceNumber, "");
    }

    public final int getDeviceNumber() {
        return mDeviceNumber;
    }

    public final String getBus() {
        return mBus;
    }

    public final String toString() {
        return mString;
    }

    public final boolean isSameBus(final CANDeviceID argOther) {
        return mBus.equals(argOther.mBus);
    }

    @Override
    public final boolean equals(final Object argOther) {
        // If the object is compared with itself then return true
        if (argOther == this) {
            return true;
        }

        /*
         * Check if o is an instance of Complex or not
         * "null instanceof [type]" also returns false
         */
        if (!(argOther instanceof CANDeviceID)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        CANDeviceID c = (CANDeviceID) argOther;

        // Compare the data members and return accordingly
        return (mDeviceNumber == c.mDeviceNumber) && isSameBus(c);
    }

    @Override
    public final int hashCode() {
        int result = 31 * 17 + mDeviceNumber;

        if (mBus != null) {
            result = 31 * result + mBus.hashCode();
        }

        return result;
    }
}
