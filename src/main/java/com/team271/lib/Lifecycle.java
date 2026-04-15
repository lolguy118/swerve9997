package com.team271.lib;

/**
 * Lifecycle interface defining all robot lifecycle hooks as default no-ops.
 *
 * <p>Classes that need lifecycle participation without extending TObj can implement this interface
 * directly. TObj implements Lifecycle, so existing subclasses are unaffected.
 *
 * <p>SubsystemManager accepts Lifecycle instances, enabling non-TObj classes to participate in the
 * robot lifecycle.
 */
public interface Lifecycle {
    /* Robot */
    default void robotInit(final double argTimestamp) {}

    default void robotPeriodicBefore(final double argTimestamp) {}

    default void robotPeriodicAfter(final double argTimestamp) {}

    /* Disabled */
    default void disabledInit(final double argTimestamp) {}

    default void disabledPeriodic(final double argTimestamp) {}

    default void disabledExit(final double argTimestamp) {}

    /* Autonomous */
    default void autonomousInit(final double argTimestamp) {}

    default void autonomousPeriodic(final double argTimestamp) {}

    default void autonomousExit(final double argTimestamp) {}

    /* Teleop */
    default void teleopInit(final double argTimestamp) {}

    default void teleopPeriodic(final double argTimestamp) {}

    default void teleopExit(final double argTimestamp) {}

    /* Simulation */
    default void simulationInit(final double argTimestamp) {}

    default void simulationPeriodic(final double argTimestamp) {}

    /* Test */
    default void testInit(final double argTimestamp) {}

    default void testPeriodic(final double argTimestamp) {}

    default void testExit(final double argTimestamp) {}

    /* Telemetry */
    default void outputTelemetry() {}
}
