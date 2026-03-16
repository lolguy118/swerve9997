package com.team271.libtest;

import com.team271.libtest.Config.RobotType;
import edu.wpi.first.wpilibj.RobotBase;

public final class Config {
    public static enum Mode {
        REAL,
        SIM,
        REPLAY
    }

    public static enum RobotType {
        COMPETITION_BOT,
        PRACTICE_BOT,
        SIM_BOT
    }

    private static final RobotType ROBOT_TYPE = RobotType.COMPETITION_BOT;

    public static Mode getMode() {
        if (RobotBase.isSimulation()) {
            return Mode.SIM;
        }
        return Mode.REAL;
    }

    public static RobotType getRobotType() {
        return ROBOT_TYPE;
    }

    private Config() {}
}
