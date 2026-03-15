package com.team271.lib.hardware.Input;

import com.team271.lib.TObj;
import com.team271.lib.util.Util;
import edu.wpi.first.wpilibj.PS4Controller;

public class InputPS4 extends Input {
    /*
     * Variables
     */

    /*
     * Constructor
     */
    public InputPS4(final TObj argParent, final String argName, final int argPort) {
        super(argParent, "(InputPS4)" + argName, argPort, InputType.INPUT_TYPE_PS4);
    }

    /*
     * Driver Utils
     */
    public PS4Controller getController() {
        return (PS4Controller) mController;
    }

    @Override
    public boolean getDisableSensors() {
        if (mController.isConnected()) {
            return getShare();
        }
        return false;
    }

    @Override
    public boolean getEnableSensors() {
        if (mController.isConnected()) {
            return getOptions();
        }
        return false;
    }

    /*
     * Joysticks
     */
    public double getRightY() {
        if (mController.isConnected()) {
            return axis[PS4Controller.Axis.kRightY.value] * -1.0;
        }
        return 0.0;
    }

    public double getRightX() {
        if (mController.isConnected()) {
            return axis[PS4Controller.Axis.kRightX.value] * -1.0;
        }
        return 0.0;
    }

    public double getLeftY() {
        if (mController.isConnected()) {
            return axis[PS4Controller.Axis.kLeftY.value] * -1.0;
        }
        return 0.0;
    }

    public double getLeftX() {
        if (mController.isConnected()) {
            return axis[PS4Controller.Axis.kLeftX.value] * -1.0;
        }
        return 0.0;
    }

    /*
     * Triggers
     */
    public double getLeftTrigger() {
        if (mController.isConnected()) {
            return Util.convertTrigger(axis[PS4Controller.Axis.kL2.value]);
        }
        return 0.0;
    }

    public boolean getLeftTriggerBtn() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kL2.value - 1];
        }
        return false;
    }

    public double getRightTrigger() {
        if (mController.isConnected()) {
            return Util.convertTrigger(axis[PS4Controller.Axis.kR2.value]);
        }
        return 0.0;
    }

    public boolean getRightTriggerBtn() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kR2.value - 1];
        }
        return false;
    }

    /*
     * Bumpers
     */
    public boolean getLeftBumper() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kL1.value - 1];
        }
        return false;
    }

    public boolean getRightBumper() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kR1.value - 1];
        }
        return false;
    }

    /*
     * Buttons
     */
    public boolean getCross() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kCross.value - 1];
        }
        return false;
    }

    public boolean getSquare() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kSquare.value - 1];
        }
        return false;
    }

    public boolean getCircle() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kCircle.value - 1];
        }
        return false;
    }

    public boolean getTriangle() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kTriangle.value - 1];
        }
        return false;
    }

    public boolean getShare() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kShare.value - 1];
        }
        return false;
    }

    public boolean getOptions() {
        if (mController.isConnected()) {
            return buttons[PS4Controller.Button.kOptions.value - 1];
        }
        return false;
    }

    /*
     * DPad
     */
    public boolean getDPadUp() {
        if (mController.isConnected()) {
            return pov[0] == 0;
        }
        return false;
    }

    public boolean getDPadRight() {
        if (mController.isConnected()) {
            return pov[0] == 90;
        }
        return false;
    }

    public boolean getDPadDown() {
        if (mController.isConnected()) {
            return pov[0] == 180;
        }
        return false;
    }

    public boolean getDPadLeft() {
        if (mController.isConnected()) {
            return pov[0] == 270;
        }
        return false;
    }
}
