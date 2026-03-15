package com.team271.lib.hardware;

public class CANBus {

    public enum CANBusStatus {
        UNKNOWN,
        ERROR,
        ERROR_INVALID_BUS,
        OK
    }

    public enum CANBusType {
        RIO,
        CANIVORE
    }

    protected CANBusStatus status = CANBusStatus.OK;
    protected final CANBusType canBusType;

    protected final String mBus;
    protected final String hootFile;

    protected final String mString;

    protected final com.ctre.phoenix6.CANBus canBus;

    // retrieve bus utilization for the CAN bus
    com.ctre.phoenix6.CANBus.CANBusStatus canInfo;
    float busUtil = 0.0f;

    public CANBus(final CANBusType argCANBusType, final String argBus, final String argHootFile) {
        mBus = argBus;
        hootFile = argHootFile;

        canBusType = argCANBusType;

        mString = "Bus: " + mBus;

        canBus = new com.ctre.phoenix6.CANBus(mBus, hootFile);

        canInfo = canBus.getStatus();

        busUtil = canInfo.BusUtilization;
    }

    // Use the default bus name (empty string).
    public CANBus(final String argBus) {
        this(CANBusType.RIO, argBus, "");
    }

    public final String getBus() {
        return mBus;
    }

    public final String toString() {
        return mString;
    }

    public final boolean isSameBus(final CANBus argOther) {
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
        if (!(argOther instanceof CANBus)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        CANBus c = (CANBus) argOther;

        // Compare the data members and return accordingly
        return mBus.equals(c.mBus);
    }

    @Override
    public final int hashCode() {
        int result = 31 * 17;

        if (mBus != null) {
            result = result + mBus.hashCode();
        }

        if (hootFile != null) {
            result = result + hootFile.hashCode();
        }

        return result;
    }
}
