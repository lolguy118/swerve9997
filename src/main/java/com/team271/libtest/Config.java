package com.team271.libtest;

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

    /** Set to true to enable AdvantageKit log replay mode (only works in simulation). */
    private static final boolean FORCE_REPLAY = false;

    public static Mode getMode() {
        if (RobotBase.isSimulation()) {
            return FORCE_REPLAY ? Mode.REPLAY : Mode.SIM;
        }
        return Mode.REAL;
    }

    public static RobotType getRobotType() {
        return ROBOT_TYPE;
    }

    private Config() {}
}
