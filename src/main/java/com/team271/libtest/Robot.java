// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team271.libtest;

import com.team271.lib.hardware.CTREManager;
import com.team271.lib.subsystem.SubsystemManager;
import com.team271.lib.wpilib.TimedRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

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
    // private double mDisabledStartTime = Double.NaN;

    private final SubsystemManager mSubsystemManager = SubsystemManager.getInstance();

    public Robot() {
        LiveWindow.disableAllTelemetry();
    }

    /**
     * This function is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        /*
         * Subsystem Init
         */
        mSubsystemManager.robotInit(mTimestamp);

        /*
         * CTRE Init
         */
        try {
            CTREManager.init();
        } catch (Throwable t) {
            throw t;
        }
    }

    @Override
    public void robotPeriodicBefore() {
        try {
            CTREManager.refreshAll();
        } catch (Throwable t) {
            throw t;
        }

        mTimestamp = CTREManager.getLastRefreshTime();

        mSubsystemManager.robotPeriodicBefore(mTimestamp);
    }

    @Override
    public void robotPeriodicAfter() {
        // mTimestamp = Timer.getFPGATimestamp();

        mSubsystemManager.robotPeriodicAfter(mTimestamp);

        // if ((mTimestamp - mLastTele) > 0.25) {
        mSubsystemManager.outputTelemetry();
        CTREManager.outputTelemetry();
        // mLastTele = mTimestamp;
        // }
    }

    /*
     * Disabled
     */
    @Override
    public void disabledInit() {
        // mDisabledStartTime = mTimestamp;

        mSubsystemManager.disabledInit(mTimestamp);

        // Explicitly stop logging
        // If the user does not call stop(), then it's possible to lose the last few
        // seconds of data
        // .stop();
    }

    @Override
    public void disabledPeriodic() {
        // mTimestamp = Timer.getFPGATimestamp();

        // if ((mTimestamp - mDisabledStartTime) > 5.0 &&
        // (mTimestamp - mDisabledStartTime) < 5.5) {
        // System.out.println("Setting coast!");
        // mDrive.setBrakeMode(false);
        // }

        mSubsystemManager.disabledPeriodic(mTimestamp);
    }

    /*
     * Auto
     */
    @Override
    public void autonomousInit() {
        // Explicitly start the logger
        // SignalLogger.start();

        mSubsystemManager.autonomousInit(mTimestamp);
    }

    @Override
    public void autonomousPeriodic() {
        mSubsystemManager.autonomousPeriodic(mTimestamp);
    }

    /*
     * Teleop
     */
    @Override
    public void teleopInit() {
        // Explicitly start the logger
        // SignalLogger.start();

        mSubsystemManager.teleopInit(mTimestamp);

        // handleShooter(false, false, false);
    }

    @Override
    public void teleopPeriodic() {
        mSubsystemManager.teleopPeriodic(mTimestamp);
    }

    /*
     * Simulation
     */
    @Override
    public void simulationInit() {
        // Explicitly start the logger
        // SignalLogger.start();

        mSubsystemManager.simulationInit(mTimestamp);
    }

    @Override
    public void simulationPeriodic() {
        mSubsystemManager.simulationPeriodic(mTimestamp);
    }

    @Override
    public void endCompetition() {
        // Explicitly stop logging
        // If the user does not call stop(), then it's possible to lose the last few
        // seconds of data
        // SignalLogger.stop();
    }
}
