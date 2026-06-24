package com.team271.frc2026.subsystems.Input;

import static edu.wpi.first.units.Units.*;

import com.team271.frc2026.Constants;
import com.team271.frc2026.generated.TunerConstants;
import com.team271.lib.TObj;
import com.team271.lib.hardware.Input.InputXBox;
import com.team271.lib.util.Util;

public class InputDriver extends InputXBox {

    /*
    Constructors and getInstance() methods
    */
    private static InputDriver mInstance;

    public static InputDriver getInstance(final TObj argParent) {
        if (mInstance == null) {
            mInstance = new InputDriver(argParent);
        }

        return mInstance;
    }

    public static InputDriver getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("InputDriver not initialized");
        }
        return mInstance;
    }

    public InputDriver(final TObj argParent) {
        super(argParent, "Driver", Constants.Controller.DRIVER_PORT);
    }

    /*
    Velocity Constants
    */
    double MAX_VELOCITY = TunerConstants.kSpeedAt12Volts.in(MetersPerSecond);
    double MAX_ROTATIONAL_STRAFE = RotationsPerSecond.of(0.75).in(RadiansPerSecond);

    /*
    Deadbands
    */

    // TODO: attain actual values for these from comp repo
    double DEADBAND_FORWARD_VELOCITY = 0.06;
    double DEADZONE_STRAFE_VELOCITY = 0.06;
    double DEADZONE_ROTATIONAL_STRAFE = 0.08;

    /*
    Input Shaping
    */
    InputShaping forwardVelocityInputShaping = InputShaping.INPUT_SHAPING_NONE;
    InputShaping strafeVelocityInputShaping = InputShaping.INPUT_SHAPING_NONE;
    InputShaping rotationalStrafeInputShaping = InputShaping.INPUT_SHAPING_NONE;

    public double getForwardVelocity() {
        double tmpValue = getLeftY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_FORWARD_VELOCITY);
        tmpValue  = inputShaping(forwardVelocityInputShaping, tmpValue);
        return tmpValue;
    }

    public double getStrafeVelocity() {
        double tmpValue = getLeftX();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_FORWARD_VELOCITY);
        tmpValue  = inputShaping(strafeVelocityInputShaping, tmpValue);
        return tmpValue;        
    }
    
    public double getRotationalStrafe() {
        double tmpValue = getRightX();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_FORWARD_VELOCITY);
        tmpValue  = inputShaping(rotationalStrafeInputShaping, tmpValue);
        return tmpValue;
    }
}