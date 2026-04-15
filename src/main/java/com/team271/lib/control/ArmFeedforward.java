package com.team271.lib.control;

/**
 * Position-dependent feedforward for arm/pivot mechanisms.
 *
 * <p>Unlike {@link Feedforward}, this requires the mechanism's current position to compute the
 * gravity compensation term (kG * cos(position)). The position must be in radians.
 *
 * <pre>{@code
 * ArmFeedforward ff = new ArmFeedforward(0.1, 0.3, 0.05, 0.001);
 *
 * // In the control loop:
 * double ffVolts = ff.calculate(currentPositionRad, desiredVelocity, 0);
 * transmission.setOutputPosition(targetPosition, ffVolts);
 * }</pre>
 *
 * <p>Formula: kS * sign(v) + kG * cos(position) + kV * v + kA * a
 */
public class ArmFeedforward {
    private final double kS;
    private final double kG;
    private final double kV;
    private final double kA;

    public ArmFeedforward(final double kS, final double kG, final double kV, final double kA) {
        this.kS = kS;
        this.kG = kG;
        this.kV = kV;
        this.kA = kA;
    }

    /**
     * Calculates the arm feedforward output.
     *
     * @param positionRad mechanism position in radians (0 = horizontal for standard arm mounting)
     * @param velocity mechanism velocity (units per second)
     * @param acceleration mechanism acceleration (units per second squared), pass 0 if unused
     * @return feedforward output (typically volts)
     */
    public double calculate(
            final double positionRad, final double velocity, final double acceleration) {
        return kS * Math.signum(velocity)
                + kG * Math.cos(positionRad)
                + kV * velocity
                + kA * acceleration;
    }

    /** Wraps a WPILib ArmFeedforward. */
    public static ArmFeedforward fromWPILib(
            final edu.wpi.first.math.controller.ArmFeedforward wpiFF) {
        return new ArmFeedforward(wpiFF.getKs(), wpiFF.getKg(), wpiFF.getKv(), wpiFF.getKa());
    }

    public double getKS() {
        return kS;
    }

    public double getKG() {
        return kG;
    }

    public double getKV() {
        return kV;
    }

    public double getKA() {
        return kA;
    }
}
