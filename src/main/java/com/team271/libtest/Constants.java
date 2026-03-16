package com.team271.libtest;

import com.team271.lib.hardware.CANDeviceID;

public final class Constants {
    /*
     * CAN Bus Names
     */
    public static final String CAN_BUS_RIO = "rio";
    public static final String CAN_BUS_CANIVORE = "canivore";

    /*
     * CAN Device IDs
     */
    public static final class CAN {
        /* Drivetrain Motors */
        public static final CANDeviceID DRIVE_LEFT_LEADER = new CANDeviceID(1, CAN_BUS_CANIVORE);
        public static final CANDeviceID DRIVE_LEFT_FOLLOWER = new CANDeviceID(2, CAN_BUS_CANIVORE);
        public static final CANDeviceID DRIVE_RIGHT_LEADER = new CANDeviceID(3, CAN_BUS_CANIVORE);
        public static final CANDeviceID DRIVE_RIGHT_FOLLOWER = new CANDeviceID(4, CAN_BUS_CANIVORE);

        /* Sensors */
        public static final CANDeviceID PIGEON2 = new CANDeviceID(5, CAN_BUS_CANIVORE);

        private CAN() {}
    }

    /*
     * Controller USB Ports
     */
    public static final class Controller {
        public static final int DRIVER_PORT = 0;
        public static final int OPERATOR_PORT = 1;

        private Controller() {}
    }

    /*
     * Physical Dimensions
     */
    public static final class Physical {
        /* Track width in meters */
        public static final double TRACK_WIDTH_M = 0.6;

        /* Wheel radius in meters */
        public static final double WHEEL_RADIUS_M = 0.0508;

        /* Drivetrain gear ratio (motor rotations per wheel rotation) */
        public static final double DRIVE_GEAR_RATIO = 6.75;

        private Physical() {}
    }

    private Constants() {}
}
