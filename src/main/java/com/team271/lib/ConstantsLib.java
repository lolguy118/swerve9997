package com.team271.lib;

public final class ConstantsLib {
    public static final int CAN_RETRY_COUNT = 5;
    public static final int CAN_TIMEOUT_MS = 10; // use for important on the fly updates
    public static final int CAN_LONG_TIMEOUT_MS = 100; // use for constructors

    /** Config apply retries — fewer attempts with shorter timeout to stay within loop budget. */
    public static final int CAN_CONFIG_APPLY_RETRIES = 3;

    public static final double CAN_CONFIG_APPLY_TIMEOUT_SEC = 0.020;

    public static final double NT_UPDATE_MS = 100;

    public static final String S_INVALID = "Invalid";

    /**
     * Delays below this threshold are treated as "no delay configured." Applied in AutoMove,
     * AutoMode, and AutoMoveTimed to distinguish zero-delay moves from explicitly timed ones.
     */
    public static final double DELAY_THRESHOLD_SEC = 0.01;

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
