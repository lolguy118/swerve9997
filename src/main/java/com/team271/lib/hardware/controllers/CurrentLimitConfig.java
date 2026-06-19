package com.team271.lib.hardware.controllers;

/**
 * Immutable configuration for motor controller current limits.
 *
 * <p>Unifies CTRE's stator + supply current limit model and REV's smart current limit into a single
 * value object. Use the factory methods for common configurations.
 *
 * <p>For CTRE TalonFX:
 *
 * <ul>
 *   <li>Stator current limit controls torque output directly
 *   <li>Supply current limit controls battery draw with optional time-based lower limit
 * </ul>
 *
 * @param statorEnabled whether the stator current limit is enabled
 * @param statorLimit stator current limit in amps
 * @param supplyEnabled whether the supply current limit is enabled
 * @param supplyLimit supply current limit in amps (upper threshold)
 * @param supplyLowerLimit supply current lower limit in amps (applied after supplyLowerTime)
 * @param supplyLowerTime time in seconds at supplyLimit before dropping to supplyLowerLimit
 */
public record CurrentLimitConfig(
        boolean statorEnabled,
        double statorLimit,
        boolean supplyEnabled,
        double supplyLimit,
        double supplyLowerLimit,
        double supplyLowerTime) {

    /** Stator-only current limit. */
    public static CurrentLimitConfig statorOnly(final double argAmps) {
        return new CurrentLimitConfig(true, argAmps, false, 0, 0, 0);
    }

    /** Supply-only current limit (no time-based lower limit). */
    public static CurrentLimitConfig supplyOnly(final double argAmps) {
        return new CurrentLimitConfig(false, 0, true, argAmps, 0, 0);
    }

    /** Both stator and supply limits enabled (no time-based lower limit). */
    public static CurrentLimitConfig both(final double argStatorAmps, final double argSupplyAmps) {
        return new CurrentLimitConfig(true, argStatorAmps, true, argSupplyAmps, 0, 0);
    }

    /** Disabled — no current limiting. */
    public static CurrentLimitConfig disabled() {
        return new CurrentLimitConfig(false, 0, false, 0, 0, 0);
    }
}
