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
