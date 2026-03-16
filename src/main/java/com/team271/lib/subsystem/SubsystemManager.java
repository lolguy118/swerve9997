package com.team271.lib.subsystem;

import com.team271.lib.misc.Elastic;
import com.team271.lib.util.Alert;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
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
    private ArrayList<Subsystem> mAllSubsystems = new ArrayList<>();
    private final Map<String, Double> lastErrorNotificationTime = new HashMap<>();

    /*
     *
     * Constructors
     *
     */
    private SubsystemManager() {}

    /*
     *
     * Getters
     *
     */
    public List<Subsystem> getSubsystems() {
        return mAllSubsystems;
    }

    /*
     *
     * Subsystems
     *
     */
    public void addSubsystem(final Subsystem argSubsystems) {
        if (argSubsystems != null) {
            mAllSubsystems.add(argSubsystems);
        }
    }

    /**
     * Runs an action on each subsystem, catching and logging any exception so that a single failing
     * subsystem does not prevent others from running.
     */
    private void forEachSafe(final String phase, final Consumer<Subsystem> action) {
        for (Subsystem s : mAllSubsystems) {
            try {
                action.accept(s);
            } catch (Throwable t) {
                DriverStation.reportError(
                        s.getName() + " threw in " + phase + ": " + t.getMessage(), true);
                double now = Timer.getFPGATimestamp();
                double lastTime = lastErrorNotificationTime.getOrDefault(s.getName(), 0.0);
                if (now - lastTime > 2.0) {
                    lastErrorNotificationTime.put(s.getName(), now);
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.Notification.NotificationLevel.ERROR,
                                    "Subsystem Error",
                                    s.getName() + " threw in " + phase + ": " + t.getMessage()));
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
        // Init methods rethrow — a subsystem that can't initialize should crash loudly
        try {
            mAllSubsystems.forEach(l -> l.robotInit(argTimestamp));
        } catch (Throwable t) {
            throw t;
        }
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
        Logger.recordOutput("SubsystemManager/Count", mAllSubsystems.size());

        forEachSafe("outputTelemetry", l -> l.outputTelemetry());

        Alert.outputTelemetry();
    }
}
