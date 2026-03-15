package com.team271.libtest.subsystems.Input;

import com.team271.libtest.Constants;
import com.team271.lib.TObj;
import com.team271.lib.hardware.Input.InputEnvisionPro;
import com.team271.lib.util.Util;

public class InputOp extends InputEnvisionPro {
    /*
     * Singleton
     */
    private static InputOp mInstance;

    public static InputOp getInstance(TObj argParent) {
        if (mInstance == null) {
            mInstance = new InputOp(argParent);
        }

        return mInstance;
    }

    public static InputOp getInstance() {
        return mInstance;
    }

    /*
     * Deadbands
     */
    public static final double DEADBAND_TRIGGER_PIVOT = 0.01;
    public static final double DEADBAND_TRIGGER_ELEVATOR = 0.05;

    /*
     * Constructor
     */
    public InputOp(TObj argParent) {
        super(argParent, "Operator", Constants.PORT_CONTROLLER_OPERATOR);
    }

    /*
     * Operator Buttons
     */
    public boolean getLaunch() {
        return getLeftSideButton();
    }

    public boolean getLaunchStop() {
        return getRightSideButton();
    }

    public boolean getIntake() {
        return getLeftSideButton();
    }

    public boolean getExhust() {
        return getRightSideButton();
    }

    /*
     * Operator Pivot Positions
     */
    public boolean getStow() {
        return getA();
    }

    public boolean getAlgaeProcessor() {
        return getLeftBumper();
    }

    public boolean getAlgaeFloorPickup() {
        return getRightBumper();
    }

    public boolean getAlgaeBarge() {
        return getY();
    }

    public boolean getReefAlgaeL2() {
        return getB();
    }
    
    public boolean getReefAlgaeL3() {
        return getX();
    }

    public double getWristUpValue() {
        double tmpValue = getLeftY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_PIVOT);

        return inputShaping(InputShaping.INPUT_SHAPING_NONE, tmpValue);
    }

    public double getWristDownValue() {
        double tmpValue = getLeftY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_PIVOT);

        return inputShaping(InputShaping.INPUT_SHAPING_NONE, tmpValue);
    }

    public double getShoulderUpValue() {
        double tmpValue = getLeftTrigger();
        //tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_PIVOT);

        //return inputShaping(InputShaping.INPUT_SHAPING_NONE, tmpValue);

        return tmpValue;
    }

    public double getShoulderDownValue() {
        double tmpValue = getRightTrigger();
        //tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_PIVOT);

        //return inputShaping(InputShaping.INPUT_SHAPING_NONE, tmpValue);
        return tmpValue;
    }

    public double getElevatorValue() {
        double tmpValue = getRightY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_ELEVATOR);

        return inputShaping(InputShaping.INPUT_SHAPING_SQUARED, tmpValue);
    }

    public double getElevatorUpValue() {
        double tmpValue = getRightY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_ELEVATOR);

        return inputShaping(InputShaping.INPUT_SHAPING_SQUARED, tmpValue);
    }

    public double getElevatorDownValue() {
        double tmpValue = getRightY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_TRIGGER_ELEVATOR);

        return inputShaping(InputShaping.INPUT_SHAPING_SQUARED, tmpValue);
    }

    public double getDeepClimbOpen() {
        if(getLeftBumper())
        {
            return 0.1;
        }

        return 0.0;
    }

    public double getDeepClimbClose() {
        if(getRightBumper())
        {
            return 0.1;
        }

        return 0.0;
    }
}
