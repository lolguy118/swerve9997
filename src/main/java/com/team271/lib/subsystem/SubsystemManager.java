package com.team271.lib.subsystem;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;
import com.team271.lib.util.Alert;
import com.team271.lib.util.Elastic;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.littletonrobotics.junction.Logger;

/*
 * Used to reset, start, stop, and update all subsystems at once
 */
public class SubsystemManager {
    /*
     *
     * Singleton
     *
     */
    private static SubsystemManager mInstance = null;

    public static SubsystemManager getInstance() {
        if (mInstance == null) {
            mInstance = new SubsystemManager();
        }

        return mInstance;
    }

    /*
     *
     * Variables
     *
     */
    private final ArrayList<Lifecycle> mAllLifecycles = new ArrayList<>();
    private final ArrayList<Subsystem> mAllSubsystems = new ArrayList<>();
    private final Map<String, Double> lastErrorNotificationTime = new HashMap<>();
    private final double errorThrottleSec;

    /*
     *
     * Constructors
     *
     */
    private SubsystemManager() {
        this(2.0);
    }

    public SubsystemManager(final double argErrorThrottleSec) {
        errorThrottleSec = argErrorThrottleSec;
    }

    /*
     *
     * Getters
     *
     */
    public List<Subsystem> getSubsystems() {
        return Collections.unmodifiableList(mAllSubsystems);
    }

    public List<Lifecycle> getLifecycles() {
        return Collections.unmodifiableList(mAllLifecycles);
    }

    /*
     *
     * Registration
     *
     */
    public void addSubsystem(final Subsystem argSubsystem) {
        if (argSubsystem != null) {
            mAllSubsystems.add(argSubsystem);
            mAllLifecycles.add(argSubsystem);
        }
    }

    /** Register a Lifecycle participant that is not a Subsystem. */
    public void addLifecycle(final Lifecycle argLifecycle) {
        if (argLifecycle != null) {
            mAllLifecycles.add(argLifecycle);
        }
    }

    /**
     * Runs an action on each lifecycle participant, catching and logging any exception so that a
     * single failing participant does not prevent others from running.
     */
    private void forEachSafe(final String phase, final Consumer<Lifecycle> action) {
        for (Lifecycle l : mAllLifecycles) {
            try {
                action.accept(l);
            } catch (Throwable t) {
                String name = (l instanceof Named n) ? n.getName() : l.getClass().getSimpleName();
                DriverStation.reportError(
                        name + " threw in " + phase + ": " + t.getMessage(), true);
                double now = Timer.getFPGATimestamp();
                double lastTime =
                        lastErrorNotificationTime.getOrDefault(name, Double.NEGATIVE_INFINITY);
                lastErrorNotificationTime.put(name, now);
                if (now - lastTime > errorThrottleSec) {
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.NotificationLevel.ERROR,
                                    "Subsystem Error",
                                    name + " threw in " + phase + ": " + t.getMessage()));
                }
            }
        }
    }

    /*
     *
     * Robot
     *
     */
    public void robotInit(final double argTimestamp) {
        forEachSafe("robotInit", l -> l.robotInit(argTimestamp));
    }

    public void robotPeriodicBefore(final double argTimestamp) {
        forEachSafe("robotPeriodicBefore", l -> l.robotPeriodicBefore(argTimestamp));
    }

    public void robotPeriodicAfter(final double argTimestamp) {
        forEachSafe("robotPeriodicAfter", l -> l.robotPeriodicAfter(argTimestamp));
    }

    /*
     *
     * Disabled
     *
     */
    public void disabledInit(final double argTimestamp) {
        forEachSafe("disabledInit", l -> l.disabledInit(argTimestamp));
    }

    public void disabledPeriodic(final double argTimestamp) {
        forEachSafe("disabledPeriodic", l -> l.disabledPeriodic(argTimestamp));
    }

    public void disabledExit(final double argTimestamp) {
        forEachSafe("disabledExit", l -> l.disabledExit(argTimestamp));
    }

    /*
     *
     * Auto
     *
     */
    public void autonomousInit(final double argTimestamp) {
        forEachSafe("autonomousInit", l -> l.autonomousInit(argTimestamp));
    }

    public void autonomousPeriodic(final double argTimestamp) {
        forEachSafe("autonomousPeriodic", l -> l.autonomousPeriodic(argTimestamp));
    }

    public void autonomousExit(final double argTimestamp) {
        forEachSafe("autonomousExit", l -> l.autonomousExit(argTimestamp));
    }

    /*
     *
     * Teleop
     *
     */
    public void teleopInit(final double argTimestamp) {
        forEachSafe("teleopInit", l -> l.teleopInit(argTimestamp));
    }

    public void teleopPeriodic(final double argTimestamp) {
        forEachSafe("teleopPeriodic", l -> l.teleopPeriodic(argTimestamp));
    }

    public void teleopExit(final double argTimestamp) {
        forEachSafe("teleopExit", l -> l.teleopExit(argTimestamp));
    }

    /*
     *
     * Sim
     *
     */
    public void simulationInit(final double argTimestamp) {
        forEachSafe("simulationInit", l -> l.simulationInit(argTimestamp));
    }

    public void simulationPeriodic(final double argTimestamp) {
        forEachSafe("simulationPeriodic", l -> l.simulationPeriodic(argTimestamp));
    }

    /*
     *
     * Test
     *
     */
    public void testInit(final double argTimestamp) {
        forEachSafe("testInit", l -> l.testInit(argTimestamp));
    }

    public void testPeriodic(final double argTimestamp) {
        forEachSafe("testPeriodic", l -> l.testPeriodic(argTimestamp));
    }

    public void testExit(final double argTimestamp) {
        forEachSafe("testExit", l -> l.testExit(argTimestamp));
    }

    /*
     *
     * Telemetry
     *
     */
    public void outputTelemetry() {
        Logger.recordOutput("SubsystemManager/Count", mAllLifecycles.size());

        forEachSafe("outputTelemetry", Lifecycle::outputTelemetry);

        Alert.outputTelemetry();
    }
}
