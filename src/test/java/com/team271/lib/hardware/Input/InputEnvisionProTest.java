package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.TRobot;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputEnvisionProTest {

    private static TRobot parent;
    private InputEnvisionPro input;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
        parent = new TRobot();
    }

    @BeforeEach
    void setUp() {
        input = new InputEnvisionPro(parent, "TestEnvision", 1);
    }

    /* --- Constructor --- */

    @Test
    void constructorDoesNotThrow() {
        assertNotNull(input);
    }

    /* --- Controller --- */

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
        /* getAxis returns 0.0 when disconnected; convertTrigger maps [-1,1] to [0,1] */
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
}
