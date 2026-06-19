package com.team271.lib.hardware.Input;

import com.team271.lib.ConstantsLib;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.subsystem.Subsystem;
import com.team271.lib.util.Alert;
import com.team271.lib.util.Alert.AlertType;
import com.team271.lib.util.Elastic;
import com.team271.lib.util.Util;
import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PS4Controller;
import edu.wpi.first.wpilibj.XboxController;

@SuppressWarnings("NullAway.Init")
public class Input extends Subsystem {
    public enum InputShaping {
        INPUT_SHAPING_NONE,
        INPUT_SHAPING_LINEAR,
        INPUT_SHAPING_SOFT,
        INPUT_SHAPING_SQUARED,
        INPUT_SHAPING_CUBED,
        INPUT_SHAPING_AGGRESSIVE,
        INPUT_SHAPING_MORE_AGGRESSIVE,
        INPUT_SHAPING_DYNAMIC
    }

    public enum InputType {
        INPUT_TYPE_PS4,
        INPUT_TYPE_XBOX,
        INPUT_TYPE_JOYSTICK,
        INPUT_TYPE_GENERIC_HID
    }

    /*
     * Variables
     */
    protected final InputType inputType;
    protected final GenericHID mController;

    protected boolean isConnected = false;
    protected boolean isConnectedPrev = false;

    protected final NTEntry ntIsConnected = new NTEntry(table, "Is Connected", false);

    protected Alert notConnectedAlert;

    // Axis
    protected double[] axis;
    protected double[] axisPrev;
    protected int axisCount = 0;

    protected NTEntry ntAxisCount;
    protected NTEntry[] ntAxisRaw;
    protected NTEntry[] ntAxisRawPrev;

    // Buttons
    protected static final int kMaxButtons = 32;
    protected boolean[] buttons;
    protected boolean[] buttonsPrev;
    protected int buttonCount = 0;

    protected NTEntry ntButtonCount;
    protected NTEntry[] ntButtonsRaw;
    protected NTEntry[] ntButtonsRawPrev;

    // POV
    protected int[] pov;
    protected int[] povPrev;
    protected int povCount = 0;

    protected NTEntry ntPOVCount;
    protected NTEntry[] ntPOVRaw;
    protected NTEntry[] ntPOVRawPrev;

    /*
     * Constructor
     */
    public Input(
            final TObj argParent,
            final String argName,
            final int argPort,
            final InputType argInputType) {
        super(argParent, "(Input)" + argName);
        inputType = argInputType;
        switch (inputType) {
            case INPUT_TYPE_PS4:
                mController = new PS4Controller(argPort);
                break;
            case INPUT_TYPE_XBOX:
                mController = new XboxController(argPort);
                break;
            case INPUT_TYPE_JOYSTICK:
                mController = new Joystick(argPort);
                break;
            case INPUT_TYPE_GENERIC_HID:
                mController = new GenericHID(argPort);
                break;

            default:
                throw new IllegalArgumentException("Input: unsupported InputType " + inputType);
        }
    }

    /*
     * Utils
     */
    public boolean getDisableSensors() {
        return false;
    }

    public boolean getEnableSensors() {
        return false;
    }

    /**
     * Returns the axis value clamped to [-1.0, 1.0]. Raw values are passed through without
     * inversion; robot-specific input subclasses are responsible for applying axis inversion per
     * their controller mapping.
     */
    public double getAxis(final int argAxis) {
        if (argAxis >= 0 && argAxis < axisCount && mController.isConnected()) {
            return Util.limit(axis[argAxis], -1.0, 1.0);
        }

        return 0.0;
    }

    /** Returns true on the single cycle when the button transitions from released to pressed. */
    public boolean getButtonPressed(final int argButton) {
        if (argButton >= 0 && argButton < buttonCount) {
            return buttons[argButton] && !buttonsPrev[argButton];
        }
        return false;
    }

    /** Returns true on the single cycle when the button transitions from pressed to released. */
    public boolean getButtonReleased(final int argButton) {
        if (argButton >= 0 && argButton < buttonCount) {
            return !buttons[argButton] && buttonsPrev[argButton];
        }
        return false;
    }

    /** Returns true while the button is held down. */
    public boolean getButton(final int argButton) {
        if (argButton >= 0 && argButton < buttonCount) {
            return buttons[argButton];
        }
        return false;
    }

    public double inputShaping(final InputShaping argShapingMode, final double argValue) {
        /*
         * Take the Absolute Value of the input so we don't have to worry about sign
         */
        double tmpValue = Math.abs(argValue);

        /*
         * Apply Shaping
         */
        switch (argShapingMode) {
            case INPUT_SHAPING_NONE:
            case INPUT_SHAPING_LINEAR:
                break;
            case INPUT_SHAPING_SOFT:
                tmpValue = Math.pow(tmpValue, 1.48);
                break;
            case INPUT_SHAPING_SQUARED:
                tmpValue = Math.pow(tmpValue, 2);
                break;
            case INPUT_SHAPING_CUBED:
                tmpValue = Math.pow(tmpValue, 3);
                break;
            case INPUT_SHAPING_AGGRESSIVE:
                tmpValue = Math.sqrt(tmpValue);
                break;
            case INPUT_SHAPING_MORE_AGGRESSIVE:
                tmpValue = Math.sqrt(1.0 - Math.pow(tmpValue - 1.0, 2));
                break;
            case INPUT_SHAPING_DYNAMIC:
                tmpValue = (1.0 * Math.cos(Math.PI * (tmpValue + 1.0)) / 2.0) + 0.5;
                break;
            default:
                break;
        }

        /*
         * Make sure the returned value is the correct sign
         */
        if (argValue < 0.0) {
            tmpValue *= -1.0;
        }

        return tmpValue;
    }

    /*
     *
     * Periodic
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        /*
         * Note: Controllers are "Not Connected" yet so mController.isConnected() will
         * return false
         */

        /*
         * Setup Axis
         */
        axisCount = DriverStationJNI.kMaxJoystickAxes;

        ntAxisCount = new NTEntry(table, "Axis Count", axisCount);

        axis = new double[axisCount];
        axisPrev = new double[axisCount];

        ntAxisRaw = new NTEntry[axisCount];
        ntAxisRawPrev = new NTEntry[axisCount];

        for (int i = 0; i < axisCount; i++) {
            ntAxisRaw[i] = new NTEntry(table, "Axis Raw [" + i + "]", 0.0);
        }
        for (int i = 0; i < axisCount; i++) {
            ntAxisRawPrev[i] = new NTEntry(table, "Axis Raw Prev [" + i + "]", 0.0);
        }

        /*
         * Setup Buttons
         */
        buttonCount = kMaxButtons;

        ntButtonCount = new NTEntry(table, "Button Count", buttonCount);

        buttons = new boolean[buttonCount];
        buttonsPrev = new boolean[buttonCount];

        ntButtonsRaw = new NTEntry[buttonCount];
        ntButtonsRawPrev = new NTEntry[buttonCount];

        for (int i = 0; i < buttonCount; i++) {
            ntButtonsRaw[i] = new NTEntry(table, "Buttons Raw [" + i + "]", false);
        }
        for (int i = 0; i < buttonCount; i++) {
            ntButtonsRawPrev[i] = new NTEntry(table, "Buttons Raw Prev [" + i + "]", false);
        }

        /*
         * Setup POV
         */
        povCount = DriverStationJNI.kMaxJoystickPOVs;

        ntPOVCount = new NTEntry(table, "POV Count", povCount);

        pov = new int[povCount];
        povPrev = new int[povCount];

        ntPOVRaw = new NTEntry[povCount];
        ntPOVRawPrev = new NTEntry[povCount];

        for (int i = 0; i < povCount; i++) {
            ntPOVRaw[i] = new NTEntry(table, "POV Raw [" + i + "]", 0);
        }
        for (int i = 0; i < povCount; i++) {
            ntPOVRawPrev[i] = new NTEntry(table, "POV Raw Prev [" + i + "]", 0);
        }

        /*
         * Persistent alert — visible in the Elastic Alerts widget for as long as the
         * controller is missing. Complements the one-shot Elastic notifications
         * fired on the connect/disconnect transitions below.
         */
        notConnectedAlert =
                new Alert(
                        "Controllers", getName() + " controller not connected", AlertType.WARNING);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        isConnectedPrev = isConnected;
        isConnected = mController.isConnected();

        /*
         * Check for Connected/Reconnected Controller
         */
        if (isConnected && !isConnectedPrev) {
            /*
             * Controller Connected/Reconnected
             */
            axisCount = Math.min(mController.getAxisCount(), DriverStationJNI.kMaxJoystickAxes);
            buttonCount = Math.min(mController.getButtonCount(), kMaxButtons);
            povCount = Math.min(mController.getPOVCount(), DriverStationJNI.kMaxJoystickPOVs);
            Elastic.sendNotification(
                    new Elastic.Notification(
                            Elastic.NotificationLevel.INFO,
                            "Controller Connected",
                            getName() + " has been connected"));
        } else if (!isConnected && isConnectedPrev) {
            /*
             * Connection Lost
             */
            Elastic.sendNotification(
                    new Elastic.Notification(
                            Elastic.NotificationLevel.WARNING,
                            "Controller Disconnected",
                            getName() + " has been disconnected"));
            axisCount = DriverStationJNI.kMaxJoystickAxes;
            buttonCount = kMaxButtons;
            povCount = DriverStationJNI.kMaxJoystickPOVs;
        }

        notConnectedAlert.set(!isConnected);

        if (isConnected) {
            /*
             * Update All Axis
             */
            for (int i = 0; i < axisCount; i++) {
                axisPrev[i] = axis[i];
                axis[i] = mController.getRawAxis(i);
            }

            /*
             * Update All Buttons
             */
            for (int i = 0; i < buttonCount; i++) {
                buttonsPrev[i] = buttons[i];
                buttons[i] = mController.getRawButton(i + 1);
            }

            /*
             * Update All POVs
             */
            for (int i = 0; i < povCount; i++) {
                povPrev[i] = pov[i];
                pov[i] = mController.getPOV(i);
            }
        } else {
            /*
             * Update All Axis
             */
            for (int i = 0; i < axisCount; i++) {
                axisPrev[i] = axis[i];
                axis[i] = 0.0;
            }

            /*
             * Update All Buttons
             */
            for (int i = 0; i < buttonCount; i++) {
                buttonsPrev[i] = buttons[i];
                buttons[i] = false;
            }

            /*
             * Update All POVs
             */
            for (int i = 0; i < povCount; i++) {
                povPrev[i] = pov[i];
                pov[i] = -1;
            }
        }
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        // Connection state is always-on — drivers need to know if the controller dropped.
        ntIsConnected.publish(isConnected);

        // Raw axis/button/POV dumps are verbose and only useful for bringup/calibration.
        // Subsystems publish their own interpreted input values (e.g. Driver/Forward).
        if (!ConstantsLib.isDebugTelemetryEnabled()) {
            return;
        }

        ntAxisCount.publish(axisCount);
        for (int i = 0; i < axisCount; i++) {
            ntAxisRaw[i].publish(axis[i]);
        }
        for (int i = 0; i < axisCount; i++) {
            ntAxisRawPrev[i].publish(axisPrev[i]);
        }

        /*
         * Publish All Buttons
         */
        ntButtonCount.publish(buttonCount);
        for (int i = 0; i < buttonCount; i++) {
            ntButtonsRaw[i].publish(buttons[i]);
        }
        for (int i = 0; i < buttonCount; i++) {
            ntButtonsRawPrev[i].publish(buttonsPrev[i]);
        }

        /*
         * Publish All POVs
         */
        ntPOVCount.publish(povCount);
        for (int i = 0; i < povCount; i++) {
            ntPOVRaw[i].publish(pov[i]);
        }
        for (int i = 0; i < povCount; i++) {
            ntPOVRawPrev[i].publish(povPrev[i]);
        }
    }
}
