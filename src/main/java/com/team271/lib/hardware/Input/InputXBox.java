package com.team271.lib.hardware.Input;

import com.team271.lib.TObj;
import com.team271.lib.util.Util;
import edu.wpi.first.wpilibj.XboxController;

public class InputXBox extends Input {
    public InputXBox(final TObj argParent, final String argName, final int argPort) {
        super(argParent, argName, argPort, InputType.INPUT_TYPE_XBOX);
    }

    public XboxController getController() {
        return (XboxController) mController;
    }

    /*
     * Joysticks
     */
    public double getRightY() {
        if (mController.isConnected()) {
            return axis[XboxController.Axis.kRightY.value] * 1.0;
        }
        return 0.0;
    }

    public double getRightX() {
        if (mController.isConnected()) {
            return axis[XboxController.Axis.kRightX.value] * 1.0;
        }
        return 0.0;
    }

    public double getLeftY() {
        if (mController.isConnected()) {
            return axis[XboxController.Axis.kLeftY.value] * 1.0;
        }
        return 0.0;
    }

    public double getLeftX() {
        if (mController.isConnected()) {
            return axis[XboxController.Axis.kLeftX.value] * 1.0;
        }
        return 0.0;
    }

    /*
     * Triggers
     */

    public double getLeftTrigger() {
        if (mController.isConnected()) {
            return Util.convertTrigger(axis[XboxController.Axis.kLeftTrigger.value]);
        }
        return 0.0;
    }

    public double getRightTrigger() {
        if (mController.isConnected()) {
            return Util.convertTrigger(axis[XboxController.Axis.kRightTrigger.value]);
        }
        return 0.0;
    }

    /*
     * Bumpers
     */
    public boolean getLeftBumper() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kLeftBumper.value - 1];
        }
        return false;
    }

    public boolean getRightBumper() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kRightBumper.value - 1];
        }
        return false;
    }

    /*
     * Buttons
     */
    public boolean getA() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kA.value - 1];
        }
        return false;
    }

    public boolean getB() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kB.value - 1];
        }
        return false;
    }

    public boolean getX() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kX.value - 1];
        }
        return false;
    }

    public boolean getY() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kY.value - 1];
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
