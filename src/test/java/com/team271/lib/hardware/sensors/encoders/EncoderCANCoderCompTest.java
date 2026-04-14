package com.team271.lib.hardware.sensors.encoders;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.TRobot;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class EncoderCANCoderCompTest {

    private static TRobot parent;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
        parent = new TRobot();
    }

    @BeforeEach
    void resetCTREManager() throws Exception {
        clearStaticField("buses");
        clearStaticField("devicesByBus");
        clearStaticField("devices");
        clearStaticField("signalsAll");
        setStaticField("signalsAllArray", null);
        setStaticField("prevRefreshTime", null);
        setStaticField("lastRefreshTime", null);
        setStaticField("lastErrorNotificationTime", 0.0);
    }

    private void clearStaticField(String fieldName) throws Exception {
        Field f = CTREManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        Object collection = f.get(null);
        if (collection instanceof java.util.Map) {
            ((java.util.Map<?, ?>) collection).clear();
        } else if (collection instanceof java.util.List) {
            ((java.util.List<?>) collection).clear();
        }
    }

    private void setStaticField(String fieldName, Object value) throws Exception {
        Field f = CTREManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    /* --- Constructor --- */

    @Test
    void constructorDoesNotThrow() {
        CANDeviceID canId = new CANDeviceID(81);

        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        assertNotNull(encoder);
    }

    @Test
    void constructorWithCCWDirectionDoesNotThrow() {
        CANDeviceID canId = new CANDeviceID(81);

        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CCW, 250.0);

        assertNotNull(encoder);
    }

    /* --- Position and velocity default to 0 --- */

    @Test
    void getPositionReturnsZeroBeforeRobotInit() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        assertEquals(0.0, encoder.getPosRotations());
    }

    @Test
    void getVelocityReturnsZeroBeforeRobotInit() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        assertEquals(0.0, encoder.getVelRPS());
    }

    /* --- Refresh --- */

    @Test
    void refreshDoesNotThrowBeforeRobotInit() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> encoder.refresh());
    }

    @Test
    void refreshDoesNotThrowAfterRobotInit() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);

        assertDoesNotThrow(() -> encoder.refresh());
    }

    /* --- Values remain zero when signals don't have OK status (expected in sim) --- */

    @Test
    void refreshAfterRobotInitValuesRemainZero() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);
        encoder.refresh();

        /* In sim without real CAN traffic, signals won't have OK status */
        assertEquals(0.0, encoder.getPosRotations(), 1e-9);
        assertEquals(0.0, encoder.getVelRPS(), 1e-9);
    }

    /* --- Simulation lifecycle --- */

    @Test
    void simulationInitDoesNotThrow() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);

        assertDoesNotThrow(() -> encoder.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);
        encoder.simulationInit(0.0);

        assertDoesNotThrow(() -> encoder.simulationPeriodic(0.0));
    }

    @Test
    void outputTelemetryDoesNotThrowAfterRefresh() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);
        encoder.refresh();

        assertDoesNotThrow(() -> encoder.outputTelemetry());
    }

    @Test
    void multipleRefreshCallsDoNotThrow() {
        CANDeviceID canId = new CANDeviceID(81);
        EncoderCANCoderComp encoder =
                new EncoderCANCoderComp(parent, "TestCANCoder", canId, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);

        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> encoder.refresh());
        }

        assertEquals(0.0, encoder.getPosRotations(), 1e-9);
    }
}
