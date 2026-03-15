// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team271.libtest;

import static com.team271.libtest.Config.*;

import com.team271.lib.TRobot;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.subsystem.SubsystemManager;
import com.team271.lib.wpilib.TimedRobot;
import com.team271.libtest.Config.Mode;
import com.team271.libtest.subsystems.EncoderTest;
import com.team271.libtest.subsystems.Infrastructure;
// import com.ctre.phoenix6.SignalLogger;
import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;
import com.team271.libtest.subsystems.Superstructure;
import com.team271.libtest.subsystems.TransmissionTest;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;

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
    private TRobot ntRobot;

    private double mTimestamp = 0;
    // private double mDisabledStartTime = Double.NaN;

    private final SubsystemManager mSubsystemManager = SubsystemManager.getInstance();

    public Robot() {
        super(LOOPER_DT_S);

        LiveWindow.disableAllTelemetry();

        try {
            ntRobot = new TRobot();
        } catch (Throwable t) {
            throw t;
        }
    }

    /**
     * This function is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        // SignalLogger.setPath("/media/sda1/");

        // Explicitly start the logger
        // SignalLogger.start();

        // Default to blue alliance in sim
        if (Config.getMode() == Mode.SIM) {
            DriverStationSim.setAllianceStationId(AllianceStationID.Blue1);
        }

        /*
         * Register CAN buses for this robot's bus topology.
         * A multi-CANivore robot would register each bus separately, e.g.:
         *   CTREManager.addBus("rio");
         *   CTREManager.addBus("drivetrain");
         *   CTREManager.addBus("subsystems");
         */
        CTREManager.addBus(Constants.CAN_BUS_NAME);

        /*
         * Controls
         */
        try {
            Globals.controllerDriver = InputDriver.getInstance(ntRobot);

            mSubsystemManager.addSubsystem(Globals.controllerDriver);
        } catch (Throwable t) {
            throw t;
        }

        try {
            Globals.controllerOperator = InputOp.getInstance(ntRobot);

            mSubsystemManager.addSubsystem(Globals.controllerOperator);
        } catch (Throwable t) {
            throw t;
        }

        /*
         * Infrastructure
         */
        try {
            Globals.infrastructure = Infrastructure.getInstance(ntRobot);

            mSubsystemManager.addSubsystem(Globals.infrastructure);
        } catch (Throwable t) {
            throw t;
        }

        /*
         * Encoder Test
         */
        try {
            Globals.encoderTest = EncoderTest.getInstance(ntRobot);

            mSubsystemManager.addSubsystem(Globals.encoderTest);
        } catch (Throwable t) {
            throw t;
        }

        /*
         * Transmission Test
         */
        try {
            Globals.transmissionTest = TransmissionTest.getInstance(ntRobot);

            mSubsystemManager.addSubsystem(Globals.transmissionTest);
        } catch (Throwable t) {
            throw t;
        }

        /*
         * Superstructure
         */
        try {
            Globals.superstructure = Superstructure.getInstance(ntRobot);

            mSubsystemManager.addSubsystem(Globals.superstructure);
        } catch (Throwable t) {
            throw t;
        }

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

        Globals.infrastructure.setIsTeleop(false);

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

        Globals.infrastructure.setIsTeleop(true);

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

        Globals.infrastructure.setIsTeleop(true);

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
