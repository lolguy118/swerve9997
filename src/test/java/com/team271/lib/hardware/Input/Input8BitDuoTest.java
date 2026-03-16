package com.team271.lib.hardware.Input;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.TRobot;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Input8BitDuoTest {

    private static TRobot parent;
    private Input8BitDuo input;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
        parent = new TRobot();
    }

    @BeforeEach
    void setUp() {
        input = new Input8BitDuo(parent, "Test8BitDuo", 0);
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
}
