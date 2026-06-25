package com.team271.frc2026;

import java.util.Set;

import com.team271.lib.control.PIDGains;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;

public class Constants {
    public class Controller {
        public static final int DRIVER_PORT = 0;
    }

    public class DriveConstants {
        public static final PIDGains kHubAlignGains = new PIDGains(2.0, 0.0, 0.002);
    } 

    public static final class Field {
        public static final AprilTagFieldLayout FIELD_LAYOUT =
                AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded);
        public static final double FIELD_LENGTH = FIELD_LAYOUT.getFieldLength();
        public static final double FIELD_WIDTH = FIELD_LAYOUT.getFieldWidth();
        // Hub center on the field, measured from the official 2026 blue-hub AprilTags:
        // tags 18 and
        // 21 lie on the hub center line at X = 4.6256 m (midpoint Y = 4.0346 m = field
        // center =
        // FIELD_WIDTH / 2). Red mirrors across field center. Feeds hub-align heading
        // (Drive.getRotationalRateToAlignToHub) and AUTO_RANGE distance (Globals).
        public static final double BLUE_HUB_X_COORDINATE = 4.6256;
        public static final double RED_HUB_X_COORDINATE = FIELD_LENGTH - BLUE_HUB_X_COORDINATE;
        public static final double HUB_Y_COORDINATE = FIELD_WIDTH / 2;

        // AprilTag IDs visible from each alliance's hub scoring position
        public static final Set<Integer> BLUE_HUB_APRIL_TAG_IDS =
                Set.of(19, 20, 21, 24, 25, 18, 27);
        public static final Set<Integer> RED_HUB_APRIL_TAG_IDS = Set.of(3, 4, 5, 8, 9, 10, 2);

        private Field() {}
    }
}
