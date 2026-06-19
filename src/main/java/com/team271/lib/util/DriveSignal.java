package com.team271.lib.util;

/*
 * An immutable drivetrain command consisting of the left, right motor settings and whether
 * the brake mode is enabled. Values may exceed [-1.0, 1.0] from arcade math; call
 * normalize() before sending to motors to scale proportionally.
 */
public final class DriveSignal {
    private final double left;
    private final double right;
    private final boolean brakeMode;

    public DriveSignal(final double left, final double right) {
        this(left, right, false);
    }

    public DriveSignal(final double left, final double right, final boolean brakeMode) {
        this.left = left;
        this.right = right;
        this.brakeMode = brakeMode;
    }

    public static final DriveSignal NEUTRAL = new DriveSignal(0, 0);
    public static final DriveSignal BRAKE = new DriveSignal(0, 0, true);

    public double getLeft() {
        return left;
    }

    public double getRight() {
        return right;
    }

    public boolean getBrakeMode() {
        return brakeMode;
    }

    /**
     * Returns a copy of this signal scaled so the larger-magnitude motor output is at most 1.0.
     *
     * @return a new DriveSignal object with the outputs normalized so the max motor output is 1.0
     */
    public DriveSignal normalize() {
        // if either of the left or right signals is greater than 1, creating a scaling
        // factor so that we can proportionally scale down the motor outputs so the max
        // output is 1.0
        double scalingFactor = Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));

        // divide by scaling factor so that the max motor output is 1
        return new DriveSignal(left / scalingFactor, right / scalingFactor, brakeMode);
    }

    @Override
    public String toString() {
        return "L: " + left + ", R: " + right + (brakeMode ? ", BRAKE" : "");
    }
}
