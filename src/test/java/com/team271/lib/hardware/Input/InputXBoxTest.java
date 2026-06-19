package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.simulation.DriverStationDataJNI;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InputXBoxTest {

    private static final int PORT = 0;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @AfterEach
    void clearJoystickData() {
        /* Reset all values first, then zero out counts */
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

    private InputXBox createAndInit() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        input.robotInit(0.0);
        return input;
    }

    /**
     * Sets up simulated joystick with the specified axis/button/POV values in a single batch, then
     * notifies and refreshes once. Axes array can be null (defaults to 6 zeros). Buttons is a
     * bitmask where bit N corresponds to button N+1. POV value of -1 means not pressed.
     */
    private void injectJoystickData(
            final double[] argAxes,
            final int argButtonBitmask,
            final int argButtonCount,
            final int argPov) {
        int axisCount = argAxes != null ? argAxes.length : 6;
        DriverStationDataJNI.setJoystickAxisCount(PORT, axisCount);
        DriverStationDataJNI.setJoystickButtonCount(PORT, argButtonCount);
        DriverStationDataJNI.setJoystickPOVCount(PORT, 1);

        if (argAxes != null) {
            for (int i = 0; i < argAxes.length; i++) {
                DriverStationDataJNI.setJoystickAxis(PORT, i, argAxes[i]);
            }
        }

        /* setJoystickButton uses 1-based indexing matching getRawButton(N) */
        for (int i = 1; i <= argButtonCount; i++) {
            DriverStationDataJNI.setJoystickButton(
                    PORT, i, (argButtonBitmask & (1 << (i - 1))) != 0);
        }

        DriverStationDataJNI.setJoystickPOV(PORT, 0, argPov);
        DriverStationDataJNI.notifyNewData();
        DriverStation.refreshData();
    }

    /* Constructor */

    @Test
    void constructorWithPort() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertNotNull(input);
    }

    @Test
    void getControllerReturnsXboxController() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertNotNull(input.getController());
    }

    /* Joystick Axes - Not Connected Returns 0 */

    @Test
    void getLeftXReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getLeftX(), 1e-6);
    }

    @Test
    void getLeftYReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getLeftY(), 1e-6);
    }

    @Test
    void getRightXReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getRightX(), 1e-6);
    }

    @Test
    void getRightYReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getRightY(), 1e-6);
    }

    /* Triggers - Not Connected Returns 0 */

    @Test
    void getLeftTriggerReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getLeftTrigger(), 1e-6);
    }

    @Test
    void getRightTriggerReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertEquals(0.0, input.getRightTrigger(), 1e-6);
    }

    /* Buttons - Not Connected Returns False */

    @Test
    void getAReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getA());
    }

    @Test
    void getBReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getB());
    }

    @Test
    void getXReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getX());
    }

    @Test
    void getYReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getY());
    }

    /* Bumpers - Not Connected Returns False */

    @Test
    void getLeftBumperReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getRightBumper());
    }

    /* DPad - Not Connected Returns False */

    @Test
    void getDPadUpReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadUp());
    }

    @Test
    void getDPadDownReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadLeft());
    }

    @Test
    void getDPadRightReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.getDPadRight());
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        InputXBox input = createAndInit();
        input.robotPeriodicBefore(0.0);
        assertDoesNotThrow(input::outputTelemetry);
    }

    /* ========== Input Shaping (all modes) ========== */

    @Test
    void inputShapingNone() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertEquals(0.5, input.inputShaping(Input.InputShaping.INPUT_SHAPING_NONE, 0.5), 1e-6);
    }

    @Test
    void inputShapingLinear() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertEquals(0.5, input.inputShaping(Input.InputShaping.INPUT_SHAPING_LINEAR, 0.5), 1e-6);
    }

    @Test
    void inputShapingSoft() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        double expected = Math.pow(0.5, 1.48);
        assertEquals(
                expected, input.inputShaping(Input.InputShaping.INPUT_SHAPING_SOFT, 0.5), 1e-6);
    }

    @Test
    void inputShapingSquared() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertEquals(0.25, input.inputShaping(Input.InputShaping.INPUT_SHAPING_SQUARED, 0.5), 1e-6);
    }

    @Test
    void inputShapingCubed() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertEquals(0.125, input.inputShaping(Input.InputShaping.INPUT_SHAPING_CUBED, 0.5), 1e-6);
    }

    @Test
    void inputShapingAggressive() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        double expected = Math.sqrt(0.5);
        assertEquals(
                expected,
                input.inputShaping(Input.InputShaping.INPUT_SHAPING_AGGRESSIVE, 0.5),
                1e-6);
    }

    @Test
    void inputShapingMoreAggressive() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        double expected = Math.sqrt(1.0 - Math.pow(0.5 - 1.0, 2));
        assertEquals(
                expected,
                input.inputShaping(Input.InputShaping.INPUT_SHAPING_MORE_AGGRESSIVE, 0.5),
                1e-6);
    }

    @Test
    void inputShapingDynamic() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        double expected = (1.0 * Math.cos(Math.PI * (0.5 + 1.0)) / 2.0) + 0.5;
        assertEquals(
                expected, input.inputShaping(Input.InputShaping.INPUT_SHAPING_DYNAMIC, 0.5), 1e-6);
    }

    @Test
    void inputShapingPreservesNegativeSign() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        assertEquals(
                -0.25, input.inputShaping(Input.InputShaping.INPUT_SHAPING_SQUARED, -0.5), 1e-6);
    }

    @Test
    void inputShapingNegativeSoft() {
        InputXBox input = new InputXBox(null, "Xbox", PORT);
        double expected = -Math.pow(0.5, 1.48);
        assertEquals(
                expected, input.inputShaping(Input.InputShaping.INPUT_SHAPING_SOFT, -0.5), 1e-6);
    }

    /* ========== Connected Branch Tests ========== */

    @Test
    void getLeftXReturnsAxisValueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(new double[] {0.5, 0, 0, 0, 0, 0}, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.5, input.getLeftX(), 1e-3);
    }

    @Test
    void getLeftYReturnsAxisValueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(new double[] {0, 0.3, 0, 0, 0, 0}, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.3, input.getLeftY(), 1e-3);
    }

    @Test
    void getRightXReturnsAxisValueWhenConnected() {
        InputXBox input = createAndInit();
        /* kRightX=4 in WPILib Xbox axis mapping */
        double[] axes = new double[6];
        axes[XboxController.Axis.kRightX.value] = -0.7;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(-0.7, input.getRightX(), 1e-3);
    }

    @Test
    void getRightYReturnsAxisValueWhenConnected() {
        InputXBox input = createAndInit();
        double[] axes = new double[6];
        axes[XboxController.Axis.kRightY.value] = 0.8;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.8, input.getRightY(), 1e-3);
    }

    @Test
    void getLeftTriggerReturnsValueWhenConnected() {
        InputXBox input = createAndInit();
        double[] axes = new double[6];
        axes[XboxController.Axis.kLeftTrigger.value] = 1.0;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        /* convertTrigger(1.0) = (1.0 + 1) / 2 = 1.0 */
        assertEquals(1.0, input.getLeftTrigger(), 1e-3);
    }

    @Test
    void getRightTriggerReturnsValueWhenConnected() {
        InputXBox input = createAndInit();
        double[] axes = new double[6];
        axes[XboxController.Axis.kRightTrigger.value] = -1.0;
        injectJoystickData(axes, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        /* convertTrigger(-1.0) = (-1.0 + 1) / 2 = 0.0 */
        assertEquals(0.0, input.getRightTrigger(), 1e-3);
    }

    @Test
    void getAReturnsTrueWhenConnectedAndPressed() {
        InputXBox input = createAndInit();
        /* kA.value=1 → buttons[0] → getRawButton(1) → HAL button index 0 */
        injectJoystickData(null, 1 << 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getA());
    }

    @Test
    void getBReturnsTrueWhenConnectedAndPressed() {
        InputXBox input = createAndInit();
        /* kB.value=2 → buttons[1] → getRawButton(2) → HAL button index 1 */
        injectJoystickData(null, 1 << 1, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getB());
    }

    @Test
    void getXReturnsTrueWhenConnectedAndPressed() {
        InputXBox input = createAndInit();
        /* kX.value=3 → buttons[2] → getRawButton(3) → HAL button index 2 */
        injectJoystickData(null, 1 << 2, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getX());
    }

    @Test
    void getYReturnsTrueWhenConnectedAndPressed() {
        InputXBox input = createAndInit();
        /* kY.value=4 → buttons[3] → getRawButton(4) → HAL button index 3 */
        injectJoystickData(null, 1 << 3, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getY());
    }

    @Test
    void getLeftBumperReturnsTrueWhenConnectedAndPressed() {
        InputXBox input = createAndInit();
        /* kLeftBumper.value=5 → buttons[4] → getRawButton(5) → HAL button index 4 */
        injectJoystickData(null, 1 << 4, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsTrueWhenConnectedAndPressed() {
        InputXBox input = createAndInit();
        /* kRightBumper.value=6 → buttons[5] → getRawButton(6) → HAL button index 5 */
        injectJoystickData(null, 1 << 5, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getRightBumper());
    }

    @Test
    void getDPadUpReturnsTrueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(null, 0, 10, 0);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadUp());
    }

    @Test
    void getDPadRightReturnsTrueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(null, 0, 10, 90);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadRight());
    }

    @Test
    void getDPadDownReturnsTrueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(null, 0, 10, 180);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsTrueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(null, 0, 10, 270);
        input.robotPeriodicBefore(0.0);

        assertTrue(input.getDPadLeft());
    }

    @Test
    void getDPadReturnsFalseForDiagonalPovWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(null, 0, 10, 45);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadUp());
        assertFalse(input.getDPadRight());
        assertFalse(input.getDPadDown());
        assertFalse(input.getDPadLeft());
    }

    /* ========== Input base class: reconnect/disconnect branches ========== */

    @Test
    void robotPeriodicBeforeReconnectionBranch() {
        InputXBox input = createAndInit();

        input.robotPeriodicBefore(0.0);
        assertFalse(input.isConnected);

        /* Connect — triggers isConnected && !isConnectedPrev */
        injectJoystickData(null, 0, 10, -1);
        input.robotPeriodicBefore(0.0);
        assertTrue(input.isConnected);
    }

    @Test
    void robotPeriodicBeforeDisconnectionBranch() {
        InputXBox input = createAndInit();

        /* Connect first */
        injectJoystickData(null, 0, 10, -1);
        input.robotPeriodicBefore(0.0);
        assertTrue(input.isConnected);

        /* Disconnect — triggers !isConnected && isConnectedPrev */
        clearJoystickData();
        input.robotPeriodicBefore(0.0);
        assertFalse(input.isConnected);
    }

    /* ========== Input base class: getAxis() connected branch ========== */

    @Test
    void getAxisReturnsValueWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(new double[] {0.75, 0, 0, 0, 0, 0}, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.75, input.getAxis(0), 1e-3);
    }

    /* outputTelemetry when connected */

    @Test
    void outputTelemetryDoesNotThrowWhenConnected() {
        InputXBox input = createAndInit();
        injectJoystickData(null, 0, 10, -1);
        input.robotPeriodicBefore(0.0);

        assertDoesNotThrow(input::outputTelemetry);
    }
}
