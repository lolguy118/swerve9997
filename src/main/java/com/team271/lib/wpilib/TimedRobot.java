// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team271.lib.wpilib;

import static edu.wpi.first.units.Units.Seconds;

import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.NotifierJNI;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import java.util.PriorityQueue;

/*
 * TimedRobot implements the IterativeRobotBase robot program framework.
 *
 * <p>The TimedRobot class is intended to be subclassed by a user creating a robot program.
 *
 * <p>periodic() functions from the base class are called on an interval by a Notifier instance.
 */
public class TimedRobot extends IterativeRobotBase {
    @SuppressWarnings("MemberName")
    static class Callback implements Comparable<Callback> {
        public Runnable func;
        public long period;
        public long expirationTime;

        /**
         * Construct a callback container.
         *
         * @param argFunc The callback to run.
         * @param argStartTimeUs The common starting point for all callback scheduling in
         *     microseconds.
         * @param argPeriodUs The period at which to run the callback in microseconds.
         * @param argOffsetUs The offset from the common starting time in microseconds.
         */
        Callback(
                final Runnable argFunc,
                final long argStartTimeUs,
                final long argPeriodUs,
                final long argOffsetUs) {
            this.func = argFunc;
            this.period = argPeriodUs;
            this.expirationTime =
                    argStartTimeUs
                            + argOffsetUs
                            + this.period
                            + (RobotController.getFPGATime() - argStartTimeUs)
                                    / this.period
                                    * this.period;
        }

        @Override
        public boolean equals(final Object argRhs) {
            return argRhs instanceof Callback callback && expirationTime == callback.expirationTime;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(expirationTime);
        }

        @Override
        public int compareTo(final Callback argRhs) {
            // Elements with sooner expiration times are sorted as lesser. The head of
            // Java's PriorityQueue is the least element.
            return Long.compare(expirationTime, argRhs.expirationTime);
        }
    }

    /* Default loop period. */
    public static final double kDefaultPeriod = 0.02;

    // The C pointer to the notifier object. We don't use it directly, it is
    // just passed to the JNI bindings.
    private final int m_notifier = NotifierJNI.initializeNotifier();

    private long m_startTimeUs;
    private long m_loopStartTimeUs;

    private final PriorityQueue<Callback> m_callbacks = new PriorityQueue<>();

    /* Constructor for TimedRobot. */
    protected TimedRobot() {
        this(kDefaultPeriod);
    }

    /**
     * Constructor for TimedRobot.
     *
     * @param argPeriod Period in seconds.
     */
    protected TimedRobot(final double argPeriod) {
        super(argPeriod);
        m_startTimeUs = RobotController.getFPGATime();
        addPeriodic(this::loopFunc, argPeriod);
        NotifierJNI.setNotifierName(m_notifier, "TimedRobot");

        HAL.report(tResourceType.kResourceType_Framework, tInstances.kFramework_Timed);
    }

    /**
     * Constructor for TimedRobot.
     *
     * @param argPeriod The period of the robot loop function.
     */
    protected TimedRobot(final Time argPeriod) {
        this(argPeriod.in(Seconds));
    }

    /**
     * Constructor for TimedRobot.
     *
     * @param argFrequency The frequency of the robot loop function.
     */
    protected TimedRobot(final Frequency argFrequency) {
        this(argFrequency.asPeriod());
    }

    @Override
    public void close() {
        NotifierJNI.stopNotifier(m_notifier);
        NotifierJNI.cleanNotifier(m_notifier);
    }

    /* Provide an alternate "main loop" via startCompetition(). */
    @Override
    public void startCompetition() {
        robotInit();

        if (isSimulation()) {
            simulationInit();
        }

        // Tell the DS that the robot is ready to be enabled
        DriverStation.reportWarning("********** Robot program startup complete **********", false);
        DriverStationJNI.observeUserProgramStarting();

        // Loop forever, calling the appropriate mode-dependent function
        while (true) {
            // We don't have to check there's an element in the queue first because
            // there's always at least one (the constructor adds one). It's reenqueued
            // at the end of the loop.
            var callback = m_callbacks.poll();

            NotifierJNI.updateNotifierAlarm(m_notifier, callback.expirationTime);

            long currentTime = NotifierJNI.waitForNotifierAlarm(m_notifier);
            if (currentTime == 0) {
                break;
            }

            m_loopStartTimeUs = RobotController.getFPGATime();

            callback.func.run();

            // Increment the expiration time by the number of full periods it's behind
            // plus one to avoid rapid repeat fires from a large loop overrun. We
            // assume currentTime ≥ expirationTime rather than checking for it since
            // the callback wouldn't be running otherwise.
            callback.expirationTime +=
                    callback.period
                            + (currentTime - callback.expirationTime)
                                    / callback.period
                                    * callback.period;
            m_callbacks.add(callback);

            // Process all other callbacks that are ready to run
            while (m_callbacks.peek().expirationTime <= currentTime) {
                callback = m_callbacks.poll();

                callback.func.run();

                callback.expirationTime +=
                        callback.period
                                + (currentTime - callback.expirationTime)
                                        / callback.period
                                        * callback.period;
                m_callbacks.add(callback);
            }
        }
    }

    /* Ends the main loop in startCompetition(). */
    @Override
    public void endCompetition() {
        NotifierJNI.stopNotifier(m_notifier);
    }

    /**
     * Return the system clock time in micrseconds for the start of the current periodic loop. This
     * is in the same time base as Timer.getFPGATimestamp(), but is stable through a loop. It is
     * updated at the beginning of every periodic callback (including the normal periodic loop).
     *
     * @return Robot running time in microseconds, as of the start of the current periodic function.
     */
    public long getLoopStartTime() {
        return m_loopStartTimeUs;
    }

    /**
     * Add a callback to run at a specific period.
     *
     * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
     * synchronously. Interactions between them are thread-safe.
     *
     * @param argCallback The callback to run.
     * @param argPeriodSeconds The period at which to run the callback in seconds.
     */
    public final void addPeriodic(final Runnable argCallback, final double argPeriodSeconds) {
        m_callbacks.add(
                new Callback(argCallback, m_startTimeUs, (long) (argPeriodSeconds * 1e6), 0));
    }

    /**
     * Add a callback to run at a specific period with a starting time offset.
     *
     * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
     * synchronously. Interactions between them are thread-safe.
     *
     * @param argCallback The callback to run.
     * @param argPeriodSeconds The period at which to run the callback in seconds.
     * @param argOffsetSeconds The offset from the common starting time in seconds. This is useful
     *     for scheduling a callback in a different timeslot relative to TimedRobot.
     */
    public final void addPeriodic(
            final Runnable argCallback,
            final double argPeriodSeconds,
            final double argOffsetSeconds) {
        m_callbacks.add(
                new Callback(
                        argCallback,
                        m_startTimeUs,
                        (long) (argPeriodSeconds * 1e6),
                        (long) (argOffsetSeconds * 1e6)));
    }

    /**
     * Add a callback to run at a specific period.
     *
     * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
     * synchronously. Interactions between them are thread-safe.
     *
     * @param argCallback The callback to run.
     * @param argPeriod The period at which to run the callback.
     */
    public final void addPeriodic(final Runnable argCallback, final Time argPeriod) {
        addPeriodic(argCallback, argPeriod.in(Seconds));
    }

    /**
     * Add a callback to run at a specific period with a starting time offset.
     *
     * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
     * synchronously. Interactions between them are thread-safe.
     *
     * @param argCallback The callback to run.
     * @param argPeriod The period at which to run the callback.
     * @param argOffset The offset from the common starting time. This is useful for scheduling a
     *     callback in a different timeslot relative to TimedRobot.
     */
    public final void addPeriodic(
            final Runnable argCallback, final Time argPeriod, final Time argOffset) {
        addPeriodic(argCallback, argPeriod.in(Seconds), argOffset.in(Seconds));
    }
}
