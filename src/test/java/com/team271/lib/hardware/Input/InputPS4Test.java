package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InputPS4Test {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    private InputPS4 createAndInit(int port) {
        InputPS4 input = new InputPS4(null, "PS4", port);
        input.robotInit(0.0);
        return input;
    }

    /* Constructor */

    @Test
    void constructorWithPort() {
        InputPS4 input = new InputPS4(null, "PS4", 1);

        assertNotNull(input);
    }

    @Test
    void getControllerReturnsPS4Controller() {
        InputPS4 input = new InputPS4(null, "PS4", 1);

        assertNotNull(input.getController());
    }

    /* Joystick Axes - Not Connected Returns 0 */

    @Test
    void getLeftXReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getLeftX(), 1e-6);
    }

    @Test
    void getLeftYReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getLeftY(), 1e-6);
    }

    @Test
    void getRightXReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightX(), 1e-6);
    }

    @Test
    void getRightYReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightY(), 1e-6);
    }

    /* Triggers - Not Connected Returns 0 */

    @Test
    void getLeftTriggerReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getLeftTrigger(), 1e-6);
    }

    @Test
    void getRightTriggerReturnsZeroWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertEquals(0.0, input.getRightTrigger(), 1e-6);
    }

    /* Trigger Buttons - Not Connected Returns False */

    @Test
    void getLeftTriggerBtnReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getLeftTriggerBtn());
    }

    @Test
    void getRightTriggerBtnReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getRightTriggerBtn());
    }

    /* Buttons - Not Connected Returns False */

    @Test
    void getCrossReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getCross());
    }

    @Test
    void getSquareReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getSquare());
    }

    @Test
    void getCircleReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getCircle());
    }

    @Test
    void getTriangleReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getTriangle());
    }

    /* Bumpers - Not Connected Returns False */

    @Test
    void getLeftBumperReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getLeftBumper());
    }

    @Test
    void getRightBumperReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getRightBumper());
    }

    /* Share/Options */

    @Test
    void getShareReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getShare());
    }

    @Test
    void getOptionsReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getOptions());
    }

    /* DPad - Not Connected Returns False */

    @Test
    void getDPadUpReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadUp());
    }

    @Test
    void getDPadDownReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadDown());
    }

    @Test
    void getDPadLeftReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadLeft());
    }

    @Test
    void getDPadRightReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDPadRight());
    }

    /* Disable/Enable Sensors */

    @Test
    void getDisableSensorsReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getDisableSensors());
    }

    @Test
    void getEnableSensorsReturnsFalseWhenNotConnected() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertFalse(input.getEnableSensors());
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        InputPS4 input = createAndInit(1);
        input.robotPeriodicBefore(0.0);

        assertDoesNotThrow(input::outputTelemetry);
    }
}
