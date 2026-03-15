package com.team271.libtest.subsystems.Input;

import com.team271.libtest.Constants;
import com.team271.lib.TObj;
import com.team271.lib.hardware.Input.InputEnvisionPro;
import com.team271.lib.util.Util;

public class InputDriver extends InputEnvisionPro {
    /*
     * Singleton
     */
    private static InputDriver mInstance;

    public static InputDriver getInstance(TObj argParent) {
        if (mInstance == null) {
            mInstance = new InputDriver(argParent);
        }

        return mInstance;
    }

    public static InputDriver getInstance() {
        return mInstance;
    }

    /*
     * Deadbands
     */
    public static final double DEADBAND_THROTTLE = 0.06;
    public static final double DEADBAND_THROTTLE_QUICK_TURN = 0.1;

    public static final double DEADBAND_STEER = 0.08;

    /*
     * Constructor
     */
    public InputDriver(TObj argParent) {
        super(argParent, "Driver", Constants.PORT_CONTROLLER_DRIVER);
    }

    /*
     * Driver Throttle and Steer
     */
    public double getThrottle() {
        //final double tmpValueX = getLeftX();
        double tmpValueY = getLeftY();
        //double tmpValues[] = new double[2];

        tmpValueY = Util.handleDeadzone(tmpValueY, DEADBAND_THROTTLE);
        //Util.handleDeadzone_Radial(tmpValues, tmpValueX, tmpValueY, DEADBAND_THROTTLE, DEADBAND_THROTTLE_HIGH);

        tmpValueY = inputShaping(InputShaping.INPUT_SHAPING_LINEAR, tmpValueY);

        tmpValueY = Util.reMap(tmpValueY, 0.0, 1.0, 0,0.7);

        return tmpValueY;
        //return inputShaping(InputShaping.INPUT_SHAPING_CUBED, tmpValueY);
        //return inputShaping(InputShaping.INPUT_SHAPING_LINEAR, tmpValueY);
    }

    public double getSteer() {
        double tmpValue = getRightX();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_STEER);

        tmpValue = inputShaping(InputShaping.INPUT_SHAPING_CUBED, tmpValue);

        tmpValue = Util.reMap(tmpValue, 0.0, 1.0, 0,
                                    0.7);

        //return inputShaping(InputShaping.INPUT_SHAPING_CUBED, tmpValue);
        return tmpValue;

        //return inputShaping(InputShaping.INPUT_SHAPING_CUBED, tmpValue);
    }

    public double getThrottleDriver() {
        return getThrottle();
    }

    public double getThrottlePassenger() {
        double tmpValue = getRightY();
        tmpValue = Util.handleDeadzone(tmpValue, DEADBAND_THROTTLE);

        return inputShaping(InputShaping.INPUT_SHAPING_SQUARED, tmpValue);
    }

    public double getThrottleDriverSlow() {
        return getThrottleDriver();
    }

    public double getThrottlePassengerSlow() {
        return getThrottlePassenger();
    }

    public double getQuickTurnLeft() {
        double tmpValueTurn = getLeftTrigger();

        tmpValueTurn = Util.handleDeadzone(tmpValueTurn, DEADBAND_THROTTLE_QUICK_TURN);

        tmpValueTurn = Util.reMap(tmpValueTurn, 0.0, 1.0, 0,
                                    0.5);

        return tmpValueTurn;
    }

    public double getQuickTurnRight() {
        double tmpValueTurn = getRightTrigger();
        
        tmpValueTurn = Util.handleDeadzone(tmpValueTurn, DEADBAND_THROTTLE_QUICK_TURN);

        tmpValueTurn = Util.reMap(tmpValueTurn, 0.0, 1.0, 0,
                                    0.5);

        return tmpValueTurn;
    }

    /*
     * Driver Intake
     */
    public boolean getStow() {
        return getA();
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

    public boolean getIntake() {
        return getLeftBumper();
    }

    public boolean getExhaust() {
        return getRightBumper();
    }

    public boolean getLaunch() {
        return getLeftTrigger() >= 1.0;
    }

    public boolean getLaunchStop() {
        return getRightTrigger() >= 1.0;
    }

    public boolean getDeepClimbOpen() {
        return getLeftSideButton();
    }

    public boolean getDeepClimbClose() {
        return getRightSideButton();
    }
}
