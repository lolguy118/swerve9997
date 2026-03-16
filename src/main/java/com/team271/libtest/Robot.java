// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team271.libtest;

import com.team271.lib.TRobot;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.subsystem.SubsystemManager;
import com.team271.lib.wpilib.TimedRobot;
import com.team271.libtest.subsystems.Infrastructure;
import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;
import com.team271.libtest.subsystems.Superstructure;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    /*
     * Variables
     */
    private double mTimestamp = 0;

    private final SubsystemManager mSubsystemManager = SubsystemManager.getInstance();

    public Robot() {
        // Record metadata
        Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
        Logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
        Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
        Logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
        Logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
        Logger.recordMetadata(
                "GitDirty",
                switch (BuildConstants.DIRTY) {
                    case 0 -> "All changes committed";
                    case 1 -> "Uncommitted changes";
                    default -> "Unknown";
                });

        // Set up data receivers & replay source
        switch (Config.getMode()) {
            case REAL:
                // Running on a real robot, log to a USB stick ("/U/logs")
                Logger.addDataReceiver(new WPILOGWriter());
                Logger.addDataReceiver(new NT4Publisher());
                break;

            case SIM:
                // Running a physics simulator, log to NT
                Logger.addDataReceiver(new NT4Publisher());
                break;

            case REPLAY:
                // Replaying a log, set up replay source
                // setUseTiming(false); // Run as fast as possible
                String logPath = LogFileUtil.findReplayLog();
                Logger.setReplaySource(new WPILOGReader(logPath));
                Logger.addDataReceiver(
                        new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
                break;
        }

        // Start AdvantageKit logger
        Logger.start();

        LiveWindow.disableAllTelemetry();
    }

    /**
     * This function is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        TRobot root = new TRobot();

        /*
         * Create Subsystems (order matters — producers before consumers)
         */
        Globals.inputDriver = new InputDriver(root);
        Globals.inputOp = new InputOp(root);
        Globals.infrastructure = new Infrastructure(root);
        Globals.superstructure = new Superstructure(root);

        /*
         * Register Subsystems (iteration order = update order)
         */
        mSubsystemManager.addSubsystem(Globals.inputDriver);
        mSubsystemManager.addSubsystem(Globals.inputOp);
        mSubsystemManager.addSubsystem(Globals.infrastructure);
        mSubsystemManager.addSubsystem(Globals.superstructure);

        /*
         * Subsystem Init
         */
        mSubsystemManager.robotInit(mTimestamp);

        /*
         * CTRE Init (must be AFTER subsystem init — optimizes bus, builds signal arrays)
         */
        CTREManager.init();
    }

    @Override
    public void robotPeriodicBefore() {
        CTREManager.refreshAll();

        mTimestamp = CTREManager.getLastRefreshTime();

        mSubsystemManager.robotPeriodicBefore(mTimestamp);
    }

    @Override
    public void robotPeriodicAfter() {
        mSubsystemManager.robotPeriodicAfter(mTimestamp);

        mSubsystemManager.outputTelemetry();
        CTREManager.outputTelemetry();
    }

    /*
     * Disabled
     */
    @Override
    public void disabledInit() {
        mSubsystemManager.disabledInit(mTimestamp);
    }

    @Override
    public void disabledPeriodic() {
        mSubsystemManager.disabledPeriodic(mTimestamp);
    }

    @Override
    public void disabledExit() {
        mSubsystemManager.disabledExit(mTimestamp);
    }

    /*
     * Auto
     */
    @Override
    public void autonomousInit() {
        mSubsystemManager.autonomousInit(mTimestamp);
    }

    @Override
    public void autonomousPeriodic() {
        mSubsystemManager.autonomousPeriodic(mTimestamp);
    }

    @Override
    public void autonomousExit() {
        mSubsystemManager.autonomousExit(mTimestamp);
    }

    /*
     * Teleop
     */
    @Override
    public void teleopInit() {
        mSubsystemManager.teleopInit(mTimestamp);
    }

    @Override
    public void teleopPeriodic() {
        mSubsystemManager.teleopPeriodic(mTimestamp);
    }

    @Override
    public void teleopExit() {
        mSubsystemManager.teleopExit(mTimestamp);
    }

    /*
     * Test
     */
    @Override
    public void testInit() {
        mSubsystemManager.testInit(mTimestamp);
    }

    @Override
    public void testPeriodic() {
        mSubsystemManager.testPeriodic(mTimestamp);
    }

    @Override
    public void testExit() {
        mSubsystemManager.testExit(mTimestamp);
    }

    /*
     * Simulation
     */
    @Override
    public void simulationInit() {
        mSubsystemManager.simulationInit(mTimestamp);
    }

    @Override
    public void simulationPeriodic() {
        mSubsystemManager.simulationPeriodic(mTimestamp);
    }

    @Override
    public void endCompetition() {}
}
