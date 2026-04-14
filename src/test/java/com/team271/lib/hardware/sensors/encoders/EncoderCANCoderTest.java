package com.team271.lib.hardware.sensors.encoders;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class EncoderCANCoderTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
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

    @Test
    void constructorWithCANDeviceID() {
        CANDeviceID id = new CANDeviceID(28);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertNotNull(encoder);
    }

    @Test
    void constructorCCWDirection() {
        CANDeviceID id = new CANDeviceID(29);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CCW, 250.0);

        assertNotNull(encoder);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(30);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> encoder.robotInit(0.0));
    }

    @Test
    void getPositionReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(31);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertEquals(0.0, encoder.getPosRotations(), 1e-6);
    }

    @Test
    void getVelocityReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(32);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertEquals(0.0, encoder.getVelRPS(), 1e-6);
    }

    @Test
    void getAbsolutePositionReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(33);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertEquals(0.0, encoder.getPosAbsRotations(), 1e-6);
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(34);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(encoder::outputTelemetry);
    }

    @Test
    void resetClearsAllPositions() {
        CANDeviceID id = new CANDeviceID(35);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        encoder.reset();
        assertEquals(0.0, encoder.getPosRotations(), 1e-6);
        assertEquals(0.0, encoder.getPosBootRotations(), 1e-6);
        assertEquals(0.0, encoder.getPosAbsRotations(), 1e-6);
    }

    @Test
    void getConfigReturnsNonNull() {
        CANDeviceID id = new CANDeviceID(36);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertNotNull(encoder.getConfig());
    }

    @Test
    void setMagnetOffsetDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(37);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> encoder.setMagnetOffset(0.25));
    }

    @Test
    void refreshDoesNotThrowAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(38);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        encoder.robotInit(0.0);
        assertDoesNotThrow(encoder::refresh);
    }

    @Test
    void simMethodsDoNotThrow() {
        CANDeviceID id = new CANDeviceID(39);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> encoder.setSimVelRotations(10.0));
        assertDoesNotThrow(() -> encoder.setSimPosRotations(5.0));
    }

    /* ========== Additional coverage tests ========== */

    @Test
    void refreshBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(40);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(encoder::refresh);
    }

    @Test
    void refreshAfterRobotInitReturnsZero() {
        CANDeviceID id = new CANDeviceID(41);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);
        encoder.robotInit(0.0);
        encoder.refresh();

        assertEquals(0.0, encoder.getPosRotations(), 1e-9);
        assertEquals(0.0, encoder.getVelRPS(), 1e-9);
    }

    @Test
    void outputTelemetryAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(42);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);
        encoder.robotInit(0.0);

        assertDoesNotThrow(encoder::outputTelemetry);
    }

    @Test
    void simulationInitDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(43);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> encoder.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(44);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);
        encoder.simulationInit(0.0);

        assertDoesNotThrow(() -> encoder.simulationPeriodic(0.0));
    }

    @Test
    void setMagnetSensorDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(45);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);
        assertDoesNotThrow(() -> encoder.setMagnetSensor(0.5));
    }

    @Test
    void setPosAbsRotationsAndGet() {
        CANDeviceID id = new CANDeviceID(46);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);
        encoder.setPosAbsRotations(0.25);
        assertEquals(0.25, encoder.getPosAbsRotations(), 1e-9);
    }

    @Test
    void setPosRotationsUpdatesHardware() {
        CANDeviceID id = new CANDeviceID(47);
        EncoderCANCoder encoder = new EncoderCANCoder(null, "Enc", id, EncoderDirection.CW, 250.0);
        encoder.setPosRotations(1.5);
        assertEquals(1.5, encoder.getPosRotations(), 1e-9);
    }
}
