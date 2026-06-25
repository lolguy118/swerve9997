package com.team271.frc2026;

import edu.wpi.first.wpilibj.DriverStation;

public class Globals {
    public static double getHubX() {
        return DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
                        == DriverStation.Alliance.Red
                ? Constants.Field.RED_HUB_X_COORDINATE
                : Constants.Field.BLUE_HUB_X_COORDINATE;
    }
}