package com.team271.lib;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConstantsLibTest {

    @Test
    void canRetryCount() {
        assertEquals(5, ConstantsLib.CAN_RETRY_COUNT);
    }

    @Test
    void canTimeoutMs() {
        assertEquals(10, ConstantsLib.CAN_TIMEOUT_MS);
    }

    @Test
    void canLongTimeoutMs() {
        assertEquals(100, ConstantsLib.CAN_LONG_TIMEOUT_MS);
    }

    @Test
    void ntUpdateMs() {
        assertEquals(100.0, ConstantsLib.NT_UPDATE_MS, 1e-9);
    }

    @Test
    void sInvalid() {
        assertEquals("Invalid", ConstantsLib.S_INVALID);
    }

    @Test
    void sensorModeValues() {
        assertEquals(3, ConstantsLib.SensorMode.values().length);
        assertNotNull(ConstantsLib.SensorMode.valueOf("SENSORED"));
        assertNotNull(ConstantsLib.SensorMode.valueOf("SENSORLESS"));
        assertNotNull(ConstantsLib.SensorMode.valueOf("SYSID"));
    }

    @Test
    void controlModeValues() {
        assertEquals(3, ConstantsLib.ControlMode.values().length);
        assertNotNull(ConstantsLib.ControlMode.valueOf("MANUAL"));
        assertNotNull(ConstantsLib.ControlMode.valueOf("HOMING"));
        assertNotNull(ConstantsLib.ControlMode.valueOf("AUTO"));
    }
}
