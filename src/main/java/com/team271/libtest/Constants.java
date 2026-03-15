package com.team271.libtest;

import edu.wpi.first.wpilibj.DriverStation;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean
 * constants. This class should not be used for any other purpose. All constants
 * should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the
 * constants are needed, to reduce verbosity.
 */
public class Constants {
    /*
     *
     * CAN Constants
     * 
     */
    public static final double CANCODER_BOOT_ALLOWANCE_SECS = 10.0;

    public static final String CAN_BUS_NAME_RIO = "rio";
    public static final String CAN_BUS_NAME = "271";

    /*
     * CAN IDs (TalonFX)
     */
    public static final int CAN_ID_DRIVER_LEAD = 1;
    public static final int CAN_ID_DRIVER_FOLLOWER = 2;

    public static final int CAN_ID_PASSENGER_LEAD = 3;
    public static final int CAN_ID_PASSENGER_PASSENGER = 4;

    public static final int CAN_ID_SHOULDER_LEAD = 5;
    public static final int CAN_ID_SHOULDER_FOLLOWER = 6;

    public static final int CAN_ID_ELEVATOR_LEAD = 7;
    public static final int CAN_ID_ELEVATOR_FOLLOWER = 8;

    public static final int CAN_ID_WRIST_LEAD = 14;
    
    public static final int CAN_ID_CLAW_LEAD = 10;
    public static final int CAN_ID_CLAW_FOLLOWER = 16;

    public static final int CAN_ID_CLIMBER_WHEELS_LEAD = 13;

    /*
     * CAN IDs (CANcoders)
     */
    public static final int CAN_ID_CANCODER_SHOULDER = 1;
    public static final int CAN_ID_CANCODER_ELEVATOR = 2;
    public static final int CAN_ID_CANCODER_WRIST = 3;

    /*
     * CAN IDs (CANrange)
     */
    public static final int CAN_ID_CANRANGE_ELEVATOR = 1;

    /*
     * CAN IDs (Gyro)
     */
    public static final int ID_GYRO = 0;

    /* Power */
    //public static final int CAN_ID_PDH = 1;

    /* Pneumatics */
    public static final int CAN_ID_PH = 1;

    /*
     * DIO Pins
     */

    /*
     * Controller Ports
     */
    public static final int PORT_CONTROLLER_DRIVER = 0;
    public static final int PORT_CONTROLLER_OPERATOR = 1;

    /*
     * Physical Properties
     */
    public static final double ROBOT_LENGTH = 31.5;
    public static final double ROBOT_WIDTH = 28.0;

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
        AUTO
    }

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @return the MAC address of the robot
     */
    public static String getMACAddress() {
        try {
            Enumeration<NetworkInterface> nwInterface = NetworkInterface.getNetworkInterfaces();
            StringBuilder ret = new StringBuilder();
            while (nwInterface.hasMoreElements()) {
                NetworkInterface nis = nwInterface.nextElement();
                if (nis != null && "eth0".equals(nis.getDisplayName())) {
                    byte[] mac = nis.getHardwareAddress();
                    if (mac != null) {
                        for (int i = 0; i < mac.length; i++) {
                            ret.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                        }
                        return ret.toString();
                    } else {
                        DriverStation.reportWarning("MAC address doesn't exist or is not accessible", false);
                    }
                }
            }
        } catch (SocketException | NullPointerException e) {
            DriverStation.reportWarning("Failed to get MAC address: " + e.getMessage(), false);
        }

        return "";
    }
}
