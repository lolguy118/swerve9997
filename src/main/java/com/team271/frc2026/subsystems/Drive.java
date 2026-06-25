package com.team271.frc2026.subsystems;

import com.ctre.phoenix6.swerve.SwerveModule;
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.ctre.phoenix6.swerve.SwerveRequest.SwerveDriveBrake;
import com.team271.frc2026.generated.TunerConstants;
import com.team271.lib.TObj;
import com.team271.lib.subsystem.StateMachine;
import com.team271.lib.subsystem.Subsystem;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;

public class Drive extends Subsystem {
    private static Drive mInstance;

    public Drive getInstance(final TObj argParent) {
        if (mInstance == null) {
            mInstance = new Drive(argParent);
        }
        return mInstance;
    }

    public Drive getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Drive Subsystem not Initialized");
        }
        return mInstance;
    }

    /*
    Drive States
    */
   public enum DriveControlState {
        SWERVE,
        PATH_FOLLOWING,
        ALIGNING_WITH_HUB,
        PARKED
   }

   private boolean mRobotCentric = false;

   /*
   State Machine
   */
   private final StateMachine<DriveControlState> stateMachine = new StateMachine<Drive.DriveControlState>(table, DriveControlState.SWERVE);

    public Drive(final TObj argParent) {
        super(argParent, "Drive");
        mDrivetrain = TunerConstants.createDrivetrain();
    }

    /*
    Drivetrain
    */
    TimedSwerveDrivetrain mDrivetrain;

    /*
    Swerve Drive Requests  
    */
    
    // Field Centric
    private final SwerveRequest.FieldCentric swerveRequest = 
        new SwerveRequest.FieldCentric()
            .withDriveRequestType(SwerveModule.DriveRequestType.OpenLoopVoltage);
    
    // Robot Centric
    private final SwerveRequest.ApplyRobotSpeeds  robotCentricRequest = 
        new SwerveRequest.ApplyRobotSpeeds()
            .withDriveRequestType(SwerveModule.DriveRequestType.OpenLoopVoltage);
    
    // Path Following
    private final SwerveRequest.ApplyRobotSpeeds pathFollowingDriveRequest =
            new SwerveRequest.ApplyRobotSpeeds()
                    .withDriveRequestType(DriveRequestType.OpenLoopVoltage);
    
    // Brake
    private final SwerveRequest.SwerveDriveBrake brakeRequest =
        new SwerveDriveBrake();
    
    /*
    Velocities
    */
    private double mVelocityX = 0.0;
    private double mVelocityY = 0.0;
    private double mRotationalRate = 0.0;
    private ChassisSpeeds mRobotCentricSpeeds = new ChassisSpeeds();
    private ChassisSpeeds mHubAlignRobotCentricSpeeds = new ChassisSpeeds();

    /*
    Implementation of Velocities
    */
    private void setValues(double argVelocityX, double argVelocityY, double argRotationalRate) {
        mVelocityX = argVelocityX;
        mVelocityY = argVelocityY;
        mRotationalRate = argRotationalRate;
    }

    private void driveSwerve(double argVelocityX, double argVelocityY, double argRotationalRate) {
        setValues(argVelocityX, argVelocityY, argRotationalRate);
    }

    private void setVelocities() {
        switch (stateMachine.getCurrentState()) {
            case SWERVE:
                if (mRobotCentric) {
                    mRobotCentricSpeeds.vxMetersPerSecond = mVelocityX;
                    mRobotCentricSpeeds.vyMetersPerSecond = mVelocityY;
                    mRobotCentricSpeeds.omegaRadiansPerSecond = mRotationalRate;

                    mDrivetrain.setControl(robotCentricRequest.withSpeeds(mRobotCentricSpeeds));
                } else {
                    mDrivetrain.setControl(
                        swerveRequest
                            .withVelocityX(mVelocityX)
                            .withVelocityY(mVelocityY)
                            .withRotationalRate(mRotationalRate));
                }
                break;
            case ALIGNING_WITH_HUB:
                break;
            case PATH_FOLLOWING:
                break;
            case PARKED:
                mDrivetrain.setControl(brakeRequest);
                break;
            default:
                DriverStation.reportError("Unexpected DriveControlState: " + stateMachine.getCurrentState(), false);
                break;
        }
    }
}
