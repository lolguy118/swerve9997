package com.team271.lib.control;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;

/**
 * Functional interface for feedforward calculation.
 *
 * <p>Feedforward compensates for known system dynamics (friction, gravity, inertia) so the PID
 * controller only needs to handle disturbances. Use the static factory methods for common types:
 *
 * <pre>{@code
 * // Simple motor (flywheel, drivetrain)
 * Feedforward ff = Feedforward.simple(0.1, 0.05);
 *
 * // Elevator (constant gravity)
 * Feedforward ff = Feedforward.elevator(0.1, 0.3, 0.05, 0.001);
 *
 * // Wrap a WPILib feedforward (extracts gains, avoids deprecated API)
 * Feedforward ff = Feedforward.fromWPILib(new SimpleMotorFeedforward(0.1, 0.05, 0.001));
 *
 * // Use with TransmissionFX
 * double ffVolts = ff.calculate(desiredVelocity, 0);
 * transmission.setOutputVelocity(desiredVelocity, ffVolts);
 * }</pre>
 *
 * <p>For position-dependent feedforward (arms/pivots), use {@link ArmFeedforward} instead.
 */
@FunctionalInterface
public interface Feedforward {

    /**
     * Calculates the feedforward output.
     *
     * @param argVelocity current or desired velocity (mechanism units per second)
     * @param argAcceleration current or desired acceleration (mechanism units per second squared),
     *     pass 0 if unused
     * @return feedforward output (typically volts)
     */
    double calculate(double argVelocity, double argAcceleration);

    /** Returns a zero feedforward (no-op). */
    static Feedforward zero() {
        return (vel, accel) -> 0.0;
    }

    /**
     * Simple motor feedforward: kS * sign(v) + kV * v.
     *
     * <p>Use for flywheels, drivetrains, and other velocity-controlled mechanisms without gravity.
     */
    static Feedforward simple(final double argKS, final double argKV) {
        return (vel, accel) -> argKS * Math.signum(vel) + argKV * vel;
    }

    /**
     * Simple motor feedforward with acceleration: kS * sign(v) + kV * v + kA * a.
     *
     * <p>Includes acceleration compensation for faster transient response.
     */
    static Feedforward simple(final double argKS, final double argKV, final double argKA) {
        return (vel, accel) -> argKS * Math.signum(vel) + argKV * vel + argKA * accel;
    }

    /**
     * Elevator feedforward: kS * sign(v) + kG + kV * v + kA * a.
     *
     * <p>Adds a constant gravity term (kG) for elevators and linear mechanisms where gravity force
     * is independent of position.
     */
    static Feedforward elevator(
            final double argKS, final double argKG, final double argKV, final double argKA) {
        return (vel, accel) -> argKS * Math.signum(vel) + argKG + argKV * vel + argKA * accel;
    }

    /**
     * Creates a Feedforward from a WPILib {@link SimpleMotorFeedforward}'s gains.
     *
     * <p>Extracts kS, kV, kA from the WPILib object and applies the same formula directly, avoiding
     * deprecated WPILib API methods.
     */
    static Feedforward fromWPILib(final SimpleMotorFeedforward argWpiFF) {
        final double ks = argWpiFF.getKs();
        final double kv = argWpiFF.getKv();
        final double ka = argWpiFF.getKa();
        return simple(ks, kv, ka);
    }
}
