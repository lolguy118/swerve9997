package com.team271.libtest;

import edu.wpi.first.wpilibj.RobotBase;

public final class Config {
    public enum Mode {
        REAL,
        SIM,
        REPLAY
    }

    public enum RobotType {
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
