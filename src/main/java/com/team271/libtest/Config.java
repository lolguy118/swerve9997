package com.team271.libtest;

import com.team271.lib.util.Alert;
import com.team271.lib.util.Alert.AlertType;
import edu.wpi.first.wpilibj.RobotBase;

public class Config {
    public static final double LOOPER_DT_S = 0.020;

    public static final double NT_UPDATE_MS = 100;

    // public static final Map<RobotType, String> logFolders = Map.of(RobotType.ROBOT_2023C,
    // "/media/sdb1/");

    public static boolean invalidRobotAlertSent = false;

    // Function to disable HAL interaction when running without native libs
    public static boolean disableHAL = false;

    public static void disableHAL() {
        disableHAL = true;
    }

    public static final boolean tuningMode = false;

    public enum RobotType {
        ROBOT_2024C,
        ROBOT_2024P,
        ROBOT_SIMBOT
    }

    public static final Mode currentMode = Mode.REAL;

    public enum Mode {
        /** Running on a real robot. */
        REAL,

        /** Running a physics simulator. */
        SIM,

        /** Replaying from a log file. */
        REPLAY
    }

    private static final RobotType robot = RobotType.ROBOT_2024C;

    public static RobotType getRobot() {
        if (!disableHAL && RobotBase.isReal()) {
            if (robot == RobotType.ROBOT_SIMBOT) { // Invalid robot selected
                if (!invalidRobotAlertSent) {
                    new Alert(
                                    "Invalid robot selected, using competition robot as default.",
                                    AlertType.ERROR)
                            .set(true);
                    invalidRobotAlertSent = true;
                }
                return RobotType.ROBOT_2024C;
            } else {
                return robot;
            }
        } else {
            return robot;
        }
    }

    public static Mode getMode() {
        /* Auto-detect simulation environment regardless of robot type */
        if (!disableHAL && RobotBase.isSimulation()) {
            return Mode.SIM;
        }

        switch (getRobot()) {
            case ROBOT_2024C:
            case ROBOT_2024P:
                return RobotBase.isReal() ? Mode.REAL : Mode.REPLAY;

            case ROBOT_SIMBOT:
                return Mode.SIM;

            default:
                return Mode.REAL;
        }
    }
}
