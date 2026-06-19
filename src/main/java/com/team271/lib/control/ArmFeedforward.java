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

    public ArmFeedforward(
            final double argKS, final double argKG, final double argKV, final double argKA) {
        this.kS = argKS;
        this.kG = argKG;
        this.kV = argKV;
        this.kA = argKA;
    }

    /**
     * Calculates the arm feedforward output.
     *
     * @param argPositionRad mechanism position in radians (0 = horizontal for standard arm
     *     mounting)
     * @param argVelocity mechanism velocity (units per second)
     * @param argAcceleration mechanism acceleration (units per second squared), pass 0 if unused
     * @return feedforward output (typically volts)
     */
    public double calculate(
            final double argPositionRad, final double argVelocity, final double argAcceleration) {
        return kS * Math.signum(argVelocity)
                + kG * Math.cos(argPositionRad)
                + kV * argVelocity
                + kA * argAcceleration;
    }

    /** Wraps a WPILib ArmFeedforward. */
    public static ArmFeedforward fromWPILib(
            final edu.wpi.first.math.controller.ArmFeedforward argWpiFF) {
        return new ArmFeedforward(
                argWpiFF.getKs(), argWpiFF.getKg(), argWpiFF.getKv(), argWpiFF.getKa());
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
