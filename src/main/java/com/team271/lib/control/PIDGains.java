package com.team271.lib.control;

/**
 * Immutable PID + feedforward gains value object.
 *
 * <p>Encapsulates all closed-loop gains supported by CTRE Phoenix 6 slots: P, I, D, V (velocity
 * feedforward), S (static friction feedforward), G (gravity feedforward), and A (acceleration
 * feedforward).
 *
 * <p>Use the convenience constructors and builder methods for common configurations:
 *
 * <pre>{@code
 * // Basic PID only
 * PIDGains gains = new PIDGains(1.0, 0.0, 0.05);
 *
 * // PID with velocity feedforward
 * PIDGains gains = new PIDGains(1.0, 0.0, 0.05).withFF(0.12, 0.25);
 *
 * // Full gains with gravity compensation
 * PIDGains gains = new PIDGains(1.0, 0.0, 0.05).withFF(0.12, 0.25).withGravity(0.15);
 * }</pre>
 *
 * @param kP proportional gain
 * @param kI integral gain
 * @param kD derivative gain
 * @param kV velocity feedforward (output per unit velocity)
 * @param kS static friction feedforward (output to overcome friction)
 * @param kG gravity feedforward (output to counteract gravity)
 * @param kA acceleration feedforward (output per unit acceleration)
 */
public record PIDGains(
        double kP, double kI, double kD, double kV, double kS, double kG, double kA) {

    /** Creates gains with P, I, D only (all feedforward terms zero). */
    public PIDGains(final double argKP, final double argKI, final double argKD) {
        this(argKP, argKI, argKD, 0, 0, 0, 0);
    }

    /** Returns a copy with velocity (kV) and static friction (kS) feedforward set. */
    public PIDGains withFF(final double argKV, final double argKS) {
        return new PIDGains(kP, kI, kD, argKV, argKS, kG, kA);
    }

    /** Returns a copy with gravity feedforward (kG) set. */
    public PIDGains withGravity(final double argKG) {
        return new PIDGains(kP, kI, kD, kV, kS, argKG, kA);
    }

    /** Returns a copy with acceleration feedforward (kA) set. */
    public PIDGains withAccel(final double argKA) {
        return new PIDGains(kP, kI, kD, kV, kS, kG, argKA);
    }

    /** Returns a copy with all feedforward terms set. */
    public PIDGains withAllFF(
            final double argKV, final double argKS, final double argKG, final double argKA) {
        return new PIDGains(kP, kI, kD, argKV, argKS, argKG, argKA);
    }
}
