package com.team271.lib.hardware.transmissions;

/**
 * Immutable value object representing the gear ratio chain from motor/sensor to mechanism output
 * units.
 *
 * <p>The conversion chain is:
 *
 * <pre>
 *   Rotor rotations × rotorToMechanism × mechanismToUnits = output units
 *   Sensor rotations × sensorRelToMechanism × mechanismToUnits = output units
 *   Sensor absolute rotations × sensorAbsToMechanism × mechanismToUnits = output units
 * </pre>
 *
 * <p>Use {@link #withRotorToMechanism(double)} to create a modified copy for gear shifts.
 */
public final class GearRatio {
    private final double rotorToMechanism;
    private final double sensorRelToMechanism;
    private final double sensorAbsToMechanism;
    private final double mechanismToUnits;

    /**
     * Creates a gear ratio with all four conversion factors.
     *
     * @throws IllegalArgumentException if any ratio is zero (would cause divide-by-zero in inverse
     *     conversions)
     */
    public GearRatio(
            final double argRotorToMechanism,
            final double argSensorRelToMechanism,
            final double argSensorAbsToMechanism,
            final double argMechanismToUnits) {
        if (argRotorToMechanism == 0
                || argSensorRelToMechanism == 0
                || argSensorAbsToMechanism == 0
                || argMechanismToUnits == 0) {
            throw new IllegalArgumentException(
                    "Gear ratios must be non-zero (got rotor="
                            + argRotorToMechanism
                            + " sensorRel="
                            + argSensorRelToMechanism
                            + " sensorAbs="
                            + argSensorAbsToMechanism
                            + " mechToUnits="
                            + argMechanismToUnits
                            + ")");
        }
        this.rotorToMechanism = argRotorToMechanism;
        this.sensorRelToMechanism = argSensorRelToMechanism;
        this.sensorAbsToMechanism = argSensorAbsToMechanism;
        this.mechanismToUnits = argMechanismToUnits;
    }

    /** Creates a simple gear ratio where all ratios are the same and mechanismToUnits is 1.0. */
    public GearRatio(final double argRatio) {
        this(argRatio, argRatio, argRatio, 1.0);
    }

    /** Identity gear ratio (1:1 everywhere). */
    public static final GearRatio IDENTITY = new GearRatio(1.0, 1.0, 1.0, 1.0);

    /* --- Rotor conversions (for FX internal encoder) --- */

    /** Convert rotor rotations to mechanism output units. */
    public double rotorToOutput(final double argRotorRotations) {
        return argRotorRotations * rotorToMechanism * mechanismToUnits;
    }

    /** Convert mechanism output units to rotor rotations. */
    public double outputToRotor(final double argOutputUnits) {
        return argOutputUnits / (rotorToMechanism * mechanismToUnits);
    }

    /* --- Sensor relative conversions (for CANCoder relative position) --- */

    /** Convert sensor relative rotations to mechanism output units. */
    public double sensorRelToOutput(final double argSensorRotations) {
        return argSensorRotations * sensorRelToMechanism * mechanismToUnits;
    }

    /** Convert mechanism output units to sensor relative rotations. */
    public double outputToSensorRel(final double argOutputUnits) {
        return argOutputUnits / (sensorRelToMechanism * mechanismToUnits);
    }

    /* --- Sensor absolute conversions (for CANCoder absolute position) --- */

    /** Convert sensor absolute rotations to mechanism output units. */
    public double sensorAbsToOutput(final double argSensorRotations) {
        return argSensorRotations * sensorAbsToMechanism * mechanismToUnits;
    }

    /* --- Raw getters --- */

    public double getRotorToMechanism() {
        return rotorToMechanism;
    }

    public double getSensorRelToMechanism() {
        return sensorRelToMechanism;
    }

    public double getSensorAbsToMechanism() {
        return sensorAbsToMechanism;
    }

    public double getMechanismToUnits() {
        return mechanismToUnits;
    }

    /* --- Builders for gear shifts --- */

    /** Returns a new GearRatio with a different rotorToMechanism (e.g., after a gear shift). */
    public GearRatio withRotorToMechanism(final double argNewRotorToMechanism) {
        return new GearRatio(
                argNewRotorToMechanism,
                sensorRelToMechanism,
                sensorAbsToMechanism,
                mechanismToUnits);
    }

    /** Returns a new GearRatio with a different sensorRelToMechanism. */
    public GearRatio withSensorRelToMechanism(final double argNewSensorRelToMechanism) {
        return new GearRatio(
                rotorToMechanism,
                argNewSensorRelToMechanism,
                sensorAbsToMechanism,
                mechanismToUnits);
    }

    @Override
    public String toString() {
        return "GearRatio(rotor="
                + rotorToMechanism
                + " sensorRel="
                + sensorRelToMechanism
                + " sensorAbs="
                + sensorAbsToMechanism
                + " mechToUnits="
                + mechanismToUnits
                + ")";
    }
}
