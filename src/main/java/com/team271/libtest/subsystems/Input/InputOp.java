package com.team271.libtest.subsystems.Input;

import com.team271.lib.TObj;
import com.team271.lib.hardware.Input.InputXBox;
import com.team271.libtest.Constants;
import edu.wpi.first.wpilibj.XboxController;

public class InputOp extends InputXBox {

    public InputOp(final TObj argParent) {
        super(argParent, "Operator", Constants.Controller.OPERATOR_PORT);
    }

    /*
     * Sensor Control
     */
    @Override
    public boolean getDisableSensors() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kBack.value - 1];
        }
        return false;
    }

    @Override
    public boolean getEnableSensors() {
        if (mController.isConnected()) {
            return buttons[XboxController.Button.kStart.value - 1];
        }
        return false;
    }
}
