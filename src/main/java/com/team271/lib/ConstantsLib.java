package com.team271.lib;

public final class ConstantsLib {
    public static final int CAN_RETRY_COUNT = 5;
    public static final int CAN_TIMEOUT_MS = 10; // use for important on the fly updates
    public static final int CAN_LONG_TIMEOUT_MS = 100; // use for constructors

    public static final double NT_UPDATE_MS = 100;

    public static final String S_INVALID = "Invalid";

    /*
     * Enums
     */
    public enum SensorMode {
        SENSORED,
        SENSORLESS,
        SYSID
    }

    public enum ControlMode {
        MANUAL,
        HOMING,
        AUTO
    }

    private ConstantsLib() {}
}
