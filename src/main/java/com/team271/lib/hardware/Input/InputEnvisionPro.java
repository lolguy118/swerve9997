package com.team271.lib.hardware.Input;

import com.team271.lib.TObj;
import com.team271.lib.util.Util;
import edu.wpi.first.wpilibj.GenericHID;

public class InputEnvisionPro extends Input {
    public enum Button {
        kA(1),
        kB(2),
        kX(3),
        kY(4),
        kLeftBumper(5),
        kRightBumper(6),
        kView(7),
        kMenu(8),
        kLeftStick(9),
        kLeftSide(9),
        kRightStick(10),
        kRightSide(10),
        kStart(11),
        kG1(12),
        kG2(13),
        kG3(14),
        kG4(15),
        kG5(16),
        kProfile(17);

        /* Button value. */
        public final int value;

        Button(final int value) {
            this.value = value;
        }

        /**
         * Get the human-friendly name of the button, matching the relevant methods. This is done by
         * stripping the leading `k`, and appending `Button`.
         *
         * <p>Primarily used for automated unit tests.
         *
         * @return the human-friendly name of the button.
         */
        @Override
        public String toString() {
            // Remove leading `k`
            return this.name().substring(1) + "Button";
        }
    }

    public enum Axis {
        kLeftX(0),
        kLeftY(1),

        kRightX(2),
        kRightY(5),

        kLeftTrigger(3),
        kRightTrigger(4);

        /* Axis value. */
        public final int value;

        Axis(final int value) {
            this.value = value;
        }

        /**
         * Get the human-friendly name of the axis, matching the relevant methods. This is done by
         * stripping the leading `k`, and appending `Axis` if the name ends with `Trigger`.
         *
         * <p>Primarily used for automated unit tests.
         *
         * @return the human-friendly name of the axis.
         */
        @Override
        public String toString() {
            var name = this.name().substring(1); // Remove leading `k`
            if (name.endsWith("Trigger")) {
                return name + "Axis";
            }
            return name;
        }
    }

    /*
     * Variables
     */

    /*
     * Constructor
     */
    public InputEnvisionPro(final TObj argParent, final String argName, final int argPort) {
        super(argParent, "(InputEnvisionPro)" + argName, argPort, InputType.INPUT_TYPE_GENERIC_HID);
    }

    /*
     * Driver Utils
     */
    public GenericHID getController() {
        return mController;
    }

    @Override
    public boolean getDisableSensors() {
        if (mController.isConnected()) {
            return getStart();
        }
        return false;
    }

    @Override
    public boolean getEnableSensors() {
        if (mController.isConnected()) {
            return getView();
        }
        return false;
    }

    /*
     * Joysticks
     */
    public double getRightY() {
        return getAxis(Axis.kRightY.value) * -1.0;
    }

    public double getRightX() {
        return getAxis(Axis.kRightX.value);
    }

    public double getLeftY() {
        return getAxis(Axis.kLeftY.value) * -1.0;
    }

    public double getLeftX() {
        return getAxis(Axis.kLeftX.value);
    }

    /*
     * Triggers
     */
    public double getLeftTrigger() {
        return Util.convertTrigger(getAxis(Axis.kLeftTrigger.value));
    }

    public double getRightTrigger() {
        return Util.convertTrigger(getAxis(Axis.kRightTrigger.value));
    }

    /*
     * Bumpers
     */
    public boolean getLeftBumper() {
        if (mController.isConnected()) {
            return buttons[Button.kLeftBumper.value - 1];
        }
        return false;
    }

    public boolean getRightBumper() {
        if (mController.isConnected()) {
            return buttons[Button.kRightBumper.value - 1];
        }
        return false;
    }

    /*
     * Side Buttons
     */
    public boolean getLeftSideButton() {
        if (mController.isConnected()) {
            return buttons[Button.kLeftSide.value - 1];
        }
        return false;
    }

    public boolean getRightSideButton() {
        if (mController.isConnected()) {
            return buttons[Button.kRightSide.value - 1];
        }
        return false;
    }

    /*
     * Buttons
     */
    public boolean getA() {
        if (mController.isConnected()) {
            return buttons[Button.kA.value - 1];
        }
        return false;
    }

    public boolean getB() {
        if (mController.isConnected()) {
            return buttons[Button.kB.value - 1];
        }
        return false;
    }

    public boolean getY() {
        if (mController.isConnected()) {
            return buttons[Button.kY.value - 1];
        }
        return false;
    }

    public boolean getX() {
        if (mController.isConnected()) {
            return buttons[Button.kX.value - 1];
        }
        return false;
    }

    public boolean getView() {
        if (mController.isConnected()) {
            return buttons[Button.kView.value - 1];
        }
        return false;
    }

    public boolean getMenu() {
        if (mController.isConnected()) {
            return buttons[Button.kMenu.value - 1];
        }
        return false;
    }

    public boolean getStart() {
        if (mController.isConnected()) {
            return buttons[Button.kStart.value - 1];
        }
        return false;
    }

    /*
     * G Buttons
     */
    public boolean getG1() {
        if (mController.isConnected()) {
            return buttons[Button.kG1.value - 1];
        }
        return false;
    }

    public boolean getG2() {
        if (mController.isConnected()) {
            return buttons[Button.kG2.value - 1];
        }
        return false;
    }

    public boolean getG3() {
        if (mController.isConnected()) {
            return buttons[Button.kG3.value - 1];
        }
        return false;
    }

    public boolean getG4() {
        if (mController.isConnected()) {
            return buttons[Button.kG4.value - 1];
        }
        return false;
    }

    public boolean getG5() {
        if (mController.isConnected()) {
            return buttons[Button.kG5.value - 1];
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
