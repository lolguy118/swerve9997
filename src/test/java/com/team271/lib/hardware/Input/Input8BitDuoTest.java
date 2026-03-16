package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.TRobot;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.simulation.DriverStationDataJNI;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Input8BitDuoTest {

    private static final int PORT = 0;
    private static TRobot parent;
    private Input8BitDuo input;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
        parent = new TRobot();
    }

    @BeforeEach
    void setUp() {
        input = new Input8BitDuo(parent, "Test8BitDuo", PORT);
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

    private void injectJoystickData(double[] axes, int buttonBitmask, int buttonCount, int pov) {
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

    /* --- Axis getters return 0.0 when not connected --- */

    @Test
    void getLeftXReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getLeftX());
    }

    @Test
    void getLeftYReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getLeftY());
    }

    @Test
    void getRightXReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getRightX());
    }

    @Test
    void getRightYReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getRightY());
    }

    @Test
    void getLeftTriggerReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getLeftTrigger());
    }

    @Test
    void getRightTriggerReturnsZeroWhenDisconnected() {
        assertEquals(0.0, input.getRightTrigger());
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
    void getLeftBumperReturnsFalseWhenDisconnected() {
        assertFalse(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsFalseWhenDisconnected() {
        assertFalse(input.getRightBumper());
    }

    @Test
    void getMinusReturnsFalseWhenDisconnected() {
        assertFalse(input.getMinus());
    }

    @Test
    void getPlusReturnsFalseWhenDisconnected() {
        assertFalse(input.getPlus());
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

    /* --- Telemetry --- */

    @Test
    void outputTelemetryDoesNotThrowAfterRobotInit() {
        input.robotInit(0.0);
        assertDoesNotThrow(() -> input.outputTelemetry());
    }

    /* ========== Connected Branch Tests ========== */

    @Test
    void getLeftXReturnsInvertedWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        /* 8BitDuo uses XboxController axes, leftX has * 1.0 (no inversion) */
        axes[XboxController.Axis.kLeftX.value] = 0.5;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        /* Input8BitDuo.getLeftX() does * 1.0 */
        assertEquals(0.5, input.getLeftX(), 1e-3);
    }

    @Test
    void getLeftYReturnsInvertedWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[XboxController.Axis.kLeftY.value] = 0.3;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        /* Input8BitDuo.getLeftY() does * -1.0 */
        assertEquals(-0.3, input.getLeftY(), 1e-3);
    }

    @Test
    void getRightXReturnsValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[XboxController.Axis.kRightX.value] = -0.7;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        /* Input8BitDuo.getRightX() does * 1.0 */
        assertEquals(-0.7, input.getRightX(), 1e-3);
    }

    @Test
    void getRightYReturnsInvertedWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[XboxController.Axis.kRightY.value] = 0.8;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        /* Input8BitDuo.getRightY() does * -1.0 */
        assertEquals(-0.8, input.getRightY(), 1e-3);
    }

    @Test
    void getLeftTriggerReturnsConvertedValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[XboxController.Axis.kLeftTrigger.value] = 1.0;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(1.0, input.getLeftTrigger(), 1e-3);
    }

    @Test
    void getRightTriggerReturnsConvertedValueWhenConnected() {
        input.robotInit(0.0);
        double[] axes = new double[6];
        axes[XboxController.Axis.kRightTrigger.value] = -1.0;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightTrigger(), 1e-3);
    }

    @Test
    void getAReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kA.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getA());
    }

    @Test
    void getBReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kB.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getB());
    }

    @Test
    void getXReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kX.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getX());
    }

    @Test
    void getYReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kY.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getY());
    }

    @Test
    void getMinusReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kBack.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getMinus());
    }

    @Test
    void getPlusReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kStart.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getPlus());
    }

    @Test
    void getLeftBumperReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kLeftBumper.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsTrueWhenConnectedAndPressed() {
        input.robotInit(0.0);
        injectJoystickData(null, 1 << (XboxController.Button.kRightBumper.value - 1), 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getRightBumper());
    }

    @Test
    void getDPadUpReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 10, 0);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadUp());
    }

    @Test
    void getDPadRightReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 10, 90);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadRight());
    }

    @Test
    void getDPadDownReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 10, 180);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsTrueWhenConnected() {
        input.robotInit(0.0);
        injectJoystickData(null, 0, 10, 270);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadLeft());
    }
}
