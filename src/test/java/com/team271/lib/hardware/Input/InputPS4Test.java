package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.simulation.DriverStationDataJNI;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PS4Controller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InputPS4Test {

    private static final int PORT = 1;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @AfterEach
    void clearJoystickData() {
        for (int i = 0; i < 6; i++) {
            DriverStationDataJNI.setJoystickAxis(PORT, i, 0);
        }
        for (int i = 1; i <= 16; i++) {
            DriverStationDataJNI.setJoystickButton(PORT, i, false);
        }
        DriverStationDataJNI.setJoystickPOV(PORT, 0, -1);
        DriverStationDataJNI.setJoystickAxisCount(PORT, 0);
        DriverStationDataJNI.setJoystickButtonCount(PORT, 0);
        DriverStationDataJNI.setJoystickPOVCount(PORT, 0);
        DriverStationDataJNI.notifyNewData();
        DriverStation.refreshData();
    }

    private InputPS4 createAndInit() {
        InputPS4 input = new InputPS4(null, "PS4", PORT);
        input.robotInit(0.0);
        return input;
    }

    private void injectJoystickData(
            final double[] axes, final int buttonBitmask, final int buttonCount, final int pov) {
        int axisCount = axes != null ? axes.length : 6;
        DriverStationDataJNI.setJoystickAxisCount(PORT, axisCount);
        DriverStationDataJNI.setJoystickButtonCount(PORT, buttonCount);
        DriverStationDataJNI.setJoystickPOVCount(PORT, 1);
        if (axes != null) {
            for (int i = 0; i < axes.length; i++) {
                DriverStationDataJNI.setJoystickAxis(PORT, i, axes[i]);
            }
        }
        for (int i = 1; i <= buttonCount; i++) {
            DriverStationDataJNI.setJoystickButton(PORT, i, (buttonBitmask & (1 << (i - 1))) != 0);
        }
        DriverStationDataJNI.setJoystickPOV(PORT, 0, pov);
        DriverStationDataJNI.notifyNewData();
        DriverStation.refreshData();
    }

    /* Constructor */

    @Test
    void constructorWithPort() {
        InputPS4 input = new InputPS4(null, "PS4", PORT);
        assertNotNull(input);
    }

    @Test
    void getControllerReturnsPS4Controller() {
        InputPS4 input = new InputPS4(null, "PS4", PORT);
        assertNotNull(input.getController());
    }

    /* Joystick Axes - Not Connected Returns 0 */

    @Test
    void getLeftXReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getLeftX(), 1e-6);
    }

    @Test
    void getLeftYReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getLeftY(), 1e-6);
    }

    @Test
    void getRightXReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getRightX(), 1e-6);
    }

    @Test
    void getRightYReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getRightY(), 1e-6);
    }

    /* Triggers - Not Connected Returns 0 */

    @Test
    void getLeftTriggerReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getLeftTrigger(), 1e-6);
    }

    @Test
    void getRightTriggerReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getRightTrigger(), 1e-6);
    }

    /* Trigger Buttons */

    @Test
    void getLeftTriggerBtnReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getLeftTriggerBtn());
    }

    @Test
    void getRightTriggerBtnReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getRightTriggerBtn());
    }

    /* Buttons - Not Connected Returns False */

    @Test
    void getCrossReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getCross());
    }

    @Test
    void getSquareReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getSquare());
    }

    @Test
    void getCircleReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getCircle());
    }

    @Test
    void getTriangleReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getTriangle());
    }

    @Test
    void getLeftBumperReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getRightBumper());
    }

    @Test
    void getShareReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getShare());
    }

    @Test
    void getOptionsReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getOptions());
    }

    /* DPad */

    @Test
    void getDPadUpReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadUp());
    }

    @Test
    void getDPadDownReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadLeft());
    }

    @Test
    void getDPadRightReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadRight());
    }

    /* Disable/Enable Sensors */

    @Test
    void getDisableSensorsReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDisableSensors());
    }

    @Test
    void getEnableSensorsReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getEnableSensors());
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        InputPS4 input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertDoesNotThrow(input::outputTelemetry);
    }

    /* ========== Connected Branch Tests ========== */

    @Test
    void getLeftXReturnsInvertedAxisValueWhenConnected() {
        InputPS4 input = createAndInit();
        double[] axes = new double[6];
        axes[PS4Controller.Axis.kLeftX.value] = 0.5;
        injectJoystickData(axes, 0, 14, -1);
        input.robotPeriodicBefore(0.0);

        /* PS4 inverts: * -1.0 */
        assertEquals(-0.5, input.getLeftX(), 1e-3);
    }

    @Test
    void getLeftYReturnsInvertedAxisValueWhenConnected() {
        InputPS4 input = createAndInit();
        double[] axes = new double[6];
        axes[PS4Controller.Axis.kLeftY.value] = 0.3;
        injectJoystickData(axes, 0, 14, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(-0.3, input.getLeftY(), 1e-3);
    }

    @Test
    void getRightXReturnsInvertedAxisValueWhenConnected() {
        InputPS4 input = createAndInit();
        double[] axes = new double[6];
        axes[PS4Controller.Axis.kRightX.value] = -0.7;
        injectJoystickData(axes, 0, 14, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.7, input.getRightX(), 1e-3);
    }

    @Test
    void getRightYReturnsInvertedAxisValueWhenConnected() {
        InputPS4 input = createAndInit();
        double[] axes = new double[6];
        axes[PS4Controller.Axis.kRightY.value] = 0.8;
        injectJoystickData(axes, 0, 14, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(-0.8, input.getRightY(), 1e-3);
    }

    /* getAxis bounds guard — regression for the ADR-018 audit (negative index must not throw) */

    @Test
    void getAxisNegativeIndexReturnsZeroWhenConnected() {
        InputPS4 input = createAndInit();
        injectJoystickData(new double[6], 0, 14, -1); // axisCount=6, controller connected
        input.robotPeriodicBefore(0.0);

        // A negative index is out of bounds; the lower-bound guard must return 0.0 rather than
        // index axis[-1] (which would throw). Without the guard this is a connected-path crash.
        assertDoesNotThrow(() -> input.getAxis(-1));
        assertEquals(0.0, input.getAxis(-1), 1e-6);
    }

    @Test
    void getLeftTriggerReturnsConvertedValueWhenConnected() {
        InputPS4 input = createAndInit();
        double[] axes = new double[6];
        axes[PS4Controller.Axis.kL2.value] = 1.0;
        injectJoystickData(axes, 0, 14, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(1.0, input.getLeftTrigger(), 1e-3);
    }

    @Test
    void getRightTriggerReturnsConvertedValueWhenConnected() {
        InputPS4 input = createAndInit();
        double[] axes = new double[6];
        axes[PS4Controller.Axis.kR2.value] = -1.0;
        injectJoystickData(axes, 0, 14, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightTrigger(), 1e-3);
    }

    @Test
    void getCrossReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kCross.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getCross());
    }

    @Test
    void getSquareReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kSquare.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getSquare());
    }

    @Test
    void getCircleReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kCircle.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getCircle());
    }

    @Test
    void getTriangleReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kTriangle.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getTriangle());
    }

    @Test
    void getLeftBumperReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kL1.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kR1.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getRightBumper());
    }

    @Test
    void getLeftTriggerBtnReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kL2.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getLeftTriggerBtn());
    }

    @Test
    void getRightTriggerBtnReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kR2.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getRightTriggerBtn());
    }

    @Test
    void getShareReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kShare.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getShare());
    }

    @Test
    void getOptionsReturnsTrueWhenConnectedAndPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kOptions.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getOptions());
    }

    @Test
    void getDisableSensorsReturnsTrueWhenSharePressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kShare.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDisableSensors());
    }

    @Test
    void getEnableSensorsReturnsTrueWhenOptionsPressed() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 1 << (PS4Controller.Button.kOptions.value - 1), 14, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getEnableSensors());
    }

    @Test
    void getDPadUpReturnsTrueWhenConnected() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 0, 14, 0);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadUp());
    }

    @Test
    void getDPadRightReturnsTrueWhenConnected() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 0, 14, 90);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadRight());
    }

    @Test
    void getDPadDownReturnsTrueWhenConnected() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 0, 14, 180);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsTrueWhenConnected() {
        InputPS4 input = createAndInit();
        injectJoystickData(null, 0, 14, 270);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadLeft());
    }
}
