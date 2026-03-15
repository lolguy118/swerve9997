package com.team271.lib.hardware;

import com.team271.lib.nt.NTEntry;
import com.team271.lib.nt.NTTable;

/**
 * Wrapper around a CTRE {@link com.ctre.phoenix6.CANBus} that adds bus type
 * tracking, utilization monitoring, hoot file logging, and NT telemetry.
 * <p>
 * Note: This class shares a simple name with {@code com.ctre.phoenix6.CANBus}.
 * Within this class, the CTRE type is referenced fully qualified.
 */
public class CANBus {

    public enum CANBusType {
        RIO,
        CANIVORE
    }

    protected final CANBusType canBusType;
    protected final String mBus;
    protected final String hootFile;

    protected final com.ctre.phoenix6.CANBus canBus;

    /* Bus utilization tracking */
    private float busUtil = 0.0f;

    /*
     * Telemetry (NT)
     */
    private final NTTable table;
    private final NTEntry ntBusName;
    private final NTEntry ntBusType;
    private final NTEntry ntBusUtil;

    /**
     * @param argBus      CAN bus name ("rio", "", or a CANivore name)
     * @param argHootFile path for hoot file logging (empty string to disable)
     */
    public CANBus(final String argBus, final String argHootFile) {
        mBus = argBus;
        hootFile = argHootFile;

        /* Detect bus type: empty string or "rio" is the RIO bus, anything else is a CANivore */
        if (mBus.isEmpty() || "rio".equalsIgnoreCase(mBus)) {
            canBusType = CANBusType.RIO;
        } else {
            canBusType = CANBusType.CANIVORE;
        }

        canBus = new com.ctre.phoenix6.CANBus(mBus, hootFile);

        table = new NTTable("CANBus/" + (mBus.isEmpty() ? "rio" : mBus));
        ntBusName = new NTEntry(table, "Name", mBus.isEmpty() ? "rio" : mBus);
        ntBusType = new NTEntry(table, "Type", canBusType == CANBusType.CANIVORE ? "CANivore" : "RIO");
        ntBusUtil = new NTEntry(table, "Utilization", 0.0);
    }

    /** Create a CAN bus with no hoot file logging. */
    public CANBus(final String argBus) {
        this(argBus, "");
    }

    public final String getBus() {
        return mBus;
    }

    public final CANBusType getType() {
        return canBusType;
    }

    public final boolean isCANivore() {
        return canBusType == CANBusType.CANIVORE;
    }

    public final float getBusUtilization() {
        return busUtil;
    }

    /** Refresh bus utilization from hardware. Call periodically (e.g. every 250ms). */
    public void refresh() {
        var canInfo = canBus.getStatus();
        busUtil = canInfo.BusUtilization;
    }

    public void outputTelemetry() {
        ntBusName.publish(mBus.isEmpty() ? "rio" : mBus);
        ntBusType.publish(canBusType == CANBusType.CANIVORE ? "CANivore" : "RIO");
        ntBusUtil.publish(busUtil);
    }

    public final boolean isSameBus(final CANBus argOther) {
        return mBus.equals(argOther.mBus);
    }

    @Override
    public final String toString() {
        return "CANBus(" + (mBus.isEmpty() ? "rio" : mBus) + ")";
    }

    @Override
    public final boolean equals(final Object argOther) {
        if (argOther == this) {
            return true;
        }

        if (!(argOther instanceof CANBus)) {
            return false;
        }

        CANBus c = (CANBus) argOther;

        return mBus.equals(c.mBus);
    }

    @Override
    public final int hashCode() {
        return mBus.hashCode();
    }
}
