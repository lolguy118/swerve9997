package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.TRobot;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.simulation.DriverStationDataJNI;
import edu.wpi.first.wpilibj.DriverStation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputEnvisionProTest {

    private static final int PORT = 1;
    private static TRobot parent;
    private InputEnvisionPro input;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
        parent = new TRobot();
    }

    @BeforeEach
    void setUp() {
        input = new InputEnvisionPro(parent, "TestEnvision", PORT);
    }

    @AfterEach
    void clearJoystickData() {
        for (int i = 0; i < 6; i++) {
            DriverStationDataJNI.setJoystickAxis(PORT, i, 0);
        }
        for (int i = 1; i <= 20; i++) {
            DriverStationDataJNI.setJoystickButton(PORT, i, false);
        }
        DriverStationDataJNI.setJoystickPOV(PORT, 0, -1);
        DriverStationDataJNI.setJoystickAxisCount(PORT, 0);
        DriverStationDataJNI.setJoystickButtonCount(PORT, 0);
        DriverStationDataJNI.setJoystickPOVCount(PORT, 0);
        DriverStationDataJNI.notifyNewData();
        DriverStation.refreshData();
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

    /* --- Constructor --- */

    @Test
    void constructorDoesNotThrow() {
        assertNotNull(input);
    }

    @Test
    void getControllerReturnsNonNull() {
        assertNotNull(input.getController());
    }

    /* --- Button enum --- */

    @Test
    void buttonEnumValuesExist() {
        InputEnvisionPro.Button[] values = InputEnvisionPro.Button.values();
        assertTrue(values.length > 0);
    }

    @Test
    void buttonToStringRemovesLeadingK() {
        assertEquals("AButton", InputEnvisionPro.Button.kA.toString());
        assertEquals("BButton", InputEnvisionPro.Button.kB.toString());
        assertEquals("XButton", InputEnvisionPro.Button.kX.toString());
        assertEquals("YButton", InputEnvisionPro.Button.kY.toString());
        assertEquals("LeftBumperButton", InputEnvisionPro.Button.kLeftBumper.toString());
        assertEquals("RightBumperButton", InputEnvisionPro.Button.kRightBumper.toString());
        assertEquals("ViewButton", InputEnvisionPro.Button.kView.toString());
        assertEquals("MenuButton", InputEnvisionPro.Button.kMenu.toString());
        assertEquals("StartButton", InputEnvisionPro.Button.kStart.toString());
        assertEquals("G1Button", InputEnvisionPro.Button.kG1.toString());
        assertEquals("G5Button", InputEnvisionPro.Button.kG5.toString());
        assertEquals("ProfileButton", InputEnvisionPro.Button.kProfile.toString());
    }

    /* --- Axis enum --- */

    @Test
    void axisEnumValuesExist() {
        InputEnvisionPro.Axis[] values = InputEnvisionPro.Axis.values();
        assertTrue(values.length > 0);
    }

    @Test
    void axisToStringRemovesLeadingK() {
        assertEquals("LeftX", InputEnvisionPro.Axis.kLeftX.toString());
        assertEquals("LeftY", InputEnvisionPro.Axis.kLeftY.toString());
        assertEquals("RightX", InputEnvisionPro.Axis.kRightX.toString());
        assertEquals("RightY", InputEnvisionPro.Axis.kRightY.toString());
    }

    @Test
    void axisToStringAppendAxisForTriggers() {
        assertEquals("LeftTriggerAxis", InputEnvisionPro.Axis.kLeftTrigger.toString());
        assertEquals("RightTriggerAxis", InputEnvisionPro.Axis.kRightTrigger.toString());
    }

    /* --- Axis getters return 0.0 when not connected --- */

    @Test
    void getLeftXReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getLeftX(), 0.0);
    }

    @Test
    void getLeftYReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getLeftY(), 0.0);
    }

    @Test
    void getRightXReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getRightX(), 0.0);
    }

    @Test
    void getRightYReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getRightY(), 0.0);
    }

    @Test
    void getLeftTriggerReturnsConvertedZeroWhenDisconnected() {
        assertEquals(0.5, input.getLeftTrigger(), 0.0);
    }

    @Test
    void getRightTriggerReturnsConvertedZeroWhenDisconnected() {
        assertEquals(0.5, input.getRightTrigger(), 0.0);
    }

    /* --- Button getters return false when not connected --- */

    @Test
    void getAReturnsFalseWhenDisconnected() {
        assertFalse(input.getA());
    }

    @Test
    void getBReturnsFalseWhenDisconnected() {
        assertFalse(input.getB());
    }

    @Test
    void getXReturnsFalseWhenDisconnected() {
        assertFalse(input.getX());
    }

    @Test
    void getYReturnsFalseWhenDisconnected() {
        assertFalse(input.getY());
    }

    @Test
    void getViewReturnsFalseWhenDisconnected() {
        assertFalse(input.getView());
    }

    @Test
    void getMenuReturnsFalseWhenDisconnected() {
        assertFalse(input.getMenu());
    }

    @Test
    void getStartReturnsFalseWhenDisconnected() {
        assertFalse(input.getStart());
    }

    @Test
    void getLeftBumperReturnsFalseWhenDisconnected() {
        assertFalse(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsFalseWhenDisconnected() {
        assertFalse(input.getRightBumper());
    }

    @Test
    void getLeftSideButtonReturnsFalseWhenDisconnected() {
        assertFalse(input.getLeftSideButton());
    }

    @Test
    void getRightSideButtonReturnsFalseWhenDisconnected() {
        assertFalse(input.getRightSideButton());
    }

    /* --- G Buttons return false when not connected --- */

    @Test
    void getG1ReturnsFalseWhenDisconnected() {
        assertFalse(input.getG1());
    }

    @Test
    void getG2ReturnsFalseWhenDisconnected() {
        assertFalse(input.getG2());
    }

    @Test
    void getG3ReturnsFalseWhenDisconnected() {
        assertFalse(input.getG3());
    }

    @Test
    void getG4ReturnsFalseWhenDisconnected() {
        assertFalse(input.getG4());
    }

    @Test
    void getG5ReturnsFalseWhenDisconnected() {
        assertFalse(input.getG5());
    }

    /* --- DPad getters return false when not connected --- */

    @Test
    void getDPadUpReturnsFalseWhenDisconnected() {
        assertFalse(input.getDPadUp());
    }

    @Test
    void getDPadRightReturnsFalseWhenDisconnected() {
        assertFalse(input.getDPadRight());
    }

    @Test
    void getDPadDownReturnsFalseWhenDisconnected() {
        assertFalse(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsFalseWhenDisconnected() {
        assertFalse(input.getDPadLeft());
    }

    /* --- Sensor enable/disable --- */

    @Test
    void getDisableSensorsReturnsFalseWhenDisconnected() {
        assertFalse(input.getDisableSensors());
    }

    @Test
    void getEnableSensorsReturnsFalseWhenDisconnected() {
        assertFalse(input.getEnableSensors());
    }

    /* --- Telemetry --- */

    @Test
    void outputTelemetryDoesNotThrowAfterRobotInit() {
        input.robotInit(0.0);
        assertDoesNotThrow(() -> input.outputTelemetry());
    }

    /* ========== Connected Branch Tests ========== */

    @Test
    void getLeftXReturnsValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[InputEnvisionPro.Axis.kLeftX.value] = 0.5;
        injectJoystickData(axes, 0, 17, -1);
        input.robotPeriodicBefore(0.0);

        /* EnvisionPro uses getAxis() which returns Util.limit(axis[idx], -1, 1) */
        assertEquals(0.5, input.getLeftX(), 1e-3);
    }

    @Test
    void getLeftYReturnsInvertedValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[InputEnvisionPro.Axis.kLeftY.value] = 0.3;
        injectJoystickData(axes, 0, 17, -1);
        input.robotPeriodicBefore(0.0);

        /* getLeftY() = getAxis(kLeftY) * -1.0 */
        assertEquals(-0.3, input.getLeftY(), 1e-3);
    }

    @Test
    void getRightXReturnsValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[InputEnvisionPro.Axis.kRightX.value] = -0.7;
        injectJoystickData(axes, 0, 17, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(-0.7, input.getRightX(), 1e-3);
    }

    @Test
    void getRightYReturnsInvertedValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[InputEnvisionPro.Axis.kRightY.value] = 0.8;
        injectJoystickData(axes, 0, 17, -1);
        input.robotPeriodicBefore(0.0);

        /* getRightY() = getAxis(kRightY) * -1.0 */
        assertEquals(-0.8, input.getRightY(), 1e-3);
    }

    @Test
    void getLeftTriggerReturnsConvertedValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[InputEnvisionPro.Axis.kLeftTrigger.value] = 1.0;
        injectJoystickData(axes, 0, 17, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(1.0, input.getLeftTrigger(), 1e-3);
    }

    @Test
    void getRightTriggerReturnsConvertedValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[InputEnvisionPro.Axis.kRightTrigger.value] = -1.0;
        injectJoystickData(axes, 0, 17, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightTrigger(), 1e-3);
    }

    @Test
    void getAReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kA.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getA());
    }

    @Test
    void getBReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kB.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getB());
    }

    @Test
    void getXReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kX.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getX());
    }

    @Test
    void getYReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kY.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getY());
    }

    @Test
    void getViewReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kView.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getView());
    }

    @Test
    void getMenuReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kMenu.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getMenu());
    }

    @Test
    void getStartReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kStart.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getStart());
    }

    @Test
    void getLeftBumperReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kLeftBumper.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kRightBumper.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getRightBumper());
    }

    @Test
    void getG1ReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kG1.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getG1());
    }

    @Test
    void getG5ReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kG5.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getG5());
    }

    @Test
    void getDisableSensorsReturnsTrueWhenStartPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kStart.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDisableSensors());
    }

    @Test
    void getEnableSensorsReturnsTrueWhenViewPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (InputEnvisionPro.Button.kView.value - 1), 17, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getEnableSensors());
    }

    @Test
    void getDPadUpReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 17, 0);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadUp());
    }

    @Test
    void getDPadRightReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 17, 90);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadRight());
    }

    @Test
    void getDPadDownReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 17, 180);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 17, 270);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadLeft());
    }
}
