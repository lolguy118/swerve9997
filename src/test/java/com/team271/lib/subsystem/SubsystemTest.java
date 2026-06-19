package com.team271.lib.subsystem;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.subsystem.Subsystem.SensorMode;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubsystemTest {

    /** Concrete subclass for testing the abstract Subsystem. */
    private static class TestSubsystem extends Subsystem {
        TestSubsystem(final String name) {
            super(null, name);
        }

        void setIsZeroed(final boolean value) {
            isZeroed = value;
        }
    }

    private TestSubsystem subsystem;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        subsystem = new TestSubsystem("TestSub");
    }

    /* --- SensorMode enum values --- */

    @Test
    void sensorModeHasFourValues() {
        SensorMode[] values = SensorMode.values();
        assertEquals(4, values.length);
    }

    @Test
    void sensorModeValuesExist() {
        assertNotNull(SensorMode.SENSORED_AUTO);
        assertNotNull(SensorMode.SENSORED_MANUAL);
        assertNotNull(SensorMode.SENSORLESS);
        assertNotNull(SensorMode.SYSID);
    }

    @Test
    void sensorModeValueOf() {
        assertEquals(SensorMode.SENSORED_AUTO, SensorMode.valueOf("SENSORED_AUTO"));
        assertEquals(SensorMode.SENSORED_MANUAL, SensorMode.valueOf("SENSORED_MANUAL"));
        assertEquals(SensorMode.SENSORLESS, SensorMode.valueOf("SENSORLESS"));
        assertEquals(SensorMode.SYSID, SensorMode.valueOf("SYSID"));
    }

    /* --- Default state --- */

    @Test
    void defaultModeIsSensoredAuto() {
        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
    }

    @Test
    void defaultIsZeroedIsFalse() {
        assertFalse(subsystem.isZeroed());
    }

    /* --- sensorsZero sets isZeroed false --- */

    @Test
    void sensorsZeroSetsIsZeroedFalse() {
        subsystem.setIsZeroed(true);
        assertTrue(subsystem.isZeroed());

        subsystem.sensorsZero();
        assertFalse(subsystem.isZeroed());
    }

    @Test
    void sensorsZeroWhenAlreadyFalse() {
        assertFalse(subsystem.isZeroed());
        subsystem.sensorsZero();
        assertFalse(subsystem.isZeroed());
    }

    /* --- sensorsDisable --- */

    @Test
    void sensorsDisableSetsModeSensorless() {
        subsystem.sensorsDisable();
        assertEquals(SensorMode.SENSORLESS, subsystem.getSensorMode());
    }

    @Test
    void sensorsDisableSetsIsZeroedFalse() {
        subsystem.setIsZeroed(true);
        subsystem.sensorsDisable();
        assertFalse(subsystem.isZeroed());
    }

    /* --- sensorsEnableAuto --- */

    @Test
    void sensorsEnableAutoSetsModeSensoredAuto() {
        subsystem.sensorsDisable();
        assertEquals(SensorMode.SENSORLESS, subsystem.getSensorMode());

        subsystem.sensorsEnableAuto();
        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
    }

    @Test
    void sensorsEnableAutoSetsIsZeroedFalseOnTransition() {
        subsystem.sensorsDisable();
        subsystem.setIsZeroed(true);

        subsystem.sensorsEnableAuto();
        assertFalse(subsystem.isZeroed());
    }

    @Test
    void sensorsEnableAutoNoOpWhenAlreadyAuto() {
        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
        subsystem.setIsZeroed(true);

        subsystem.sensorsEnableAuto();

        assertTrue(
                subsystem.isZeroed(), "isZeroed should not change when already in SENSORED_AUTO");
        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
    }

    /* --- sensorsEnableManual --- */

    @Test
    void sensorsEnableManualSetsModeSensoredManual() {
        subsystem.sensorsEnableManual();
        assertEquals(SensorMode.SENSORED_MANUAL, subsystem.getSensorMode());
    }

    @Test
    void sensorsEnableManualSetsIsZeroedFalseOnTransition() {
        subsystem.setIsZeroed(true);

        subsystem.sensorsEnableManual();
        assertFalse(subsystem.isZeroed());
    }

    @Test
    void sensorsEnableManualNoOpWhenAlreadyManual() {
        subsystem.sensorsEnableManual();
        subsystem.setIsZeroed(true);

        subsystem.sensorsEnableManual();

        assertTrue(
                subsystem.isZeroed(), "isZeroed should not change when already in SENSORED_MANUAL");
        assertEquals(SensorMode.SENSORED_MANUAL, subsystem.getSensorMode());
    }

    /* --- Transitions between modes --- */

    @Test
    void transitionAutoToManualResetsZeroed() {
        subsystem.setIsZeroed(true);
        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());

        subsystem.sensorsEnableManual();

        assertEquals(SensorMode.SENSORED_MANUAL, subsystem.getSensorMode());
        assertFalse(subsystem.isZeroed());
    }

    @Test
    void transitionManualToAutoResetsZeroed() {
        subsystem.sensorsEnableManual();
        subsystem.setIsZeroed(true);

        subsystem.sensorsEnableAuto();

        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
        assertFalse(subsystem.isZeroed());
    }

    @Test
    void transitionSensorlessToAutoResetsZeroed() {
        subsystem.sensorsDisable();
        subsystem.setIsZeroed(true);

        subsystem.sensorsEnableAuto();

        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
        assertFalse(subsystem.isZeroed());
    }

    /* --- outputTelemetry doesn't throw --- */

    @Test
    void outputTelemetryDoesNotThrowInSensoredAuto() {
        assertEquals(SensorMode.SENSORED_AUTO, subsystem.getSensorMode());
        assertDoesNotThrow(() -> subsystem.outputTelemetry());
    }

    @Test
    void outputTelemetryDoesNotThrowInSensorless() {
        subsystem.sensorsDisable();
        assertDoesNotThrow(() -> subsystem.outputTelemetry());
    }

    @Test
    void outputTelemetryDoesNotThrowInSensoredManual() {
        subsystem.sensorsEnableManual();
        assertDoesNotThrow(() -> subsystem.outputTelemetry());
    }

    @Test
    void outputTelemetryDoesNotThrowWhenZeroed() {
        subsystem.setIsZeroed(true);
        assertDoesNotThrow(() -> subsystem.outputTelemetry());
    }

    /* --- getName --- */

    @Test
    void getNameReturnsConstructorName() {
        assertEquals("TestSub", subsystem.getName());
    }
}
