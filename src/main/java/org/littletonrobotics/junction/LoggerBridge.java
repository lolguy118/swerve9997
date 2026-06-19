package org.littletonrobotics.junction;

/**
 * Bridge class to expose Logger's package-private periodic methods for use in custom robot base
 * classes that cannot extend LoggedRobot.
 *
 * <p>AdvantageKit's {@link Logger#periodicBeforeUser()} and {@link Logger#periodicAfterUser(long,
 * long)} are package-private, callable only from {@link LoggedRobot}. This bridge allows custom
 * {@code TimedRobot} implementations to integrate the AK logging lifecycle without extending
 * LoggedRobot.
 */
public final class LoggerBridge {
    private LoggerBridge() {}

    /** Call at the start of the robot loop, before user code runs. */
    public static void periodicBeforeUser() {
        Logger.periodicBeforeUser();
    }

    /**
     * Call at the end of the robot loop, after user code and simulation periodic.
     *
     * @param argPeriodUs the robot loop period in microseconds
     * @param argLoopCycleStartUs the FPGA timestamp (microseconds) at the start of this loop cycle
     */
    public static void periodicAfterUser(final long argPeriodUs, final long argLoopCycleStartUs) {
        Logger.periodicAfterUser(argPeriodUs, argLoopCycleStartUs);
    }
}
