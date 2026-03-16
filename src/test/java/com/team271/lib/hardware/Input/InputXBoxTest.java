package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InputXBoxTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    private InputXBox createAndInit(int port) {
        InputXBox input = new InputXBox(null, "Xbox", port);
        input.robotInit(0.0);
        return input;
    }

    /* Constructor */

    @Test
    void constructorWithPort() {
        InputXBox input = new InputXBox(null, "Xbox", 0);

        assertNotNull(input);
    }

    @Test
    void getControllerReturnsXboxController() {
        InputXBox input = new InputXBox(null, "Xbox", 0);

        assertNotNull(input.getController());
    }

    /* Joystick Axes - Not Connected Returns 0 */

    @Test
    void getLeftXReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getLeftX(), 1e-6);
    }

    @Test
    void getLeftYReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getLeftY(), 1e-6);
    }

    @Test
    void getRightXReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightX(), 1e-6);
    }

    @Test
    void getRightYReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightY(), 1e-6);
    }

    /* Triggers - Not Connected Returns 0 */

    @Test
    void getLeftTriggerReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getLeftTrigger(), 1e-6);
    }

    @Test
    void getRightTriggerReturnsZeroWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightTrigger(), 1e-6);
    }

    /* Buttons - Not Connected Returns False */

    @Test
    void getAReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getA());
    }

    @Test
    void getBReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getB());
    }

    @Test
    void getXReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getX());
    }

    @Test
    void getYReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getY());
    }

    /* Bumpers - Not Connected Returns False */

    @Test
    void getLeftBumperReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getRightBumper());
    }

    /* DPad - Not Connected Returns False */

    @Test
    void getDPadUpReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadUp());
    }

    @Test
    void getDPadDownReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadLeft());
    }

    @Test
    void getDPadRightReturnsFalseWhenNotConnected() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadRight());
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        InputXBox input = createAndInit(0);
        input.robotPeriodicBefore(0.0);

        assertDoesNotThrow(input::outputTelemetry);
    }

    /* Input Shaping */

    @Test
    void inputShapingLinear() {
        InputXBox input = new InputXBox(null, "Xbox", 0);

        assertEquals(0.5, input.inputShaping(Input.InputShaping.INPUT_SHAPING_LINEAR, 0.5), 1e-6);
    }

    @Test
    void inputShapingSquared() {
        InputXBox input = new InputXBox(null, "Xbox", 0);

        assertEquals(0.25, input.inputShaping(Input.InputShaping.INPUT_SHAPING_SQUARED, 0.5), 1e-6);
    }

    @Test
    void inputShapingPreservesNegativeSign() {
        InputXBox input = new InputXBox(null, "Xbox", 0);

        double result = input.inputShaping(Input.InputShaping.INPUT_SHAPING_SQUARED, -0.5);
        assertTrue(result < 0.0);
    }
}
