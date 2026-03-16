package com.team271.lib.hardware.sensors.imu;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class IMUPigeon2Test {

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
    void constructorCreatesIMU() {
        CANDeviceID id = new CANDeviceID(30);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertNotNull(imu);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(31);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(() -> imu.robotInit(0.0));
    }

    @Test
    void getYawReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(32);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertEquals(0.0, imu.getYaw(), 1e-6);
    }

    @Test
    void getPitchReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(33);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertEquals(0.0, imu.getPitch(), 1e-6);
    }

    @Test
    void getRollReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(34);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertEquals(0.0, imu.getRoll(), 1e-6);
    }

    @Test
    void getYawRateReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(35);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertEquals(0.0, imu.getYawRate(), 1e-6);
    }

    @Test
    void resetClearsAllValues() {
        CANDeviceID id = new CANDeviceID(36);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        imu.reset();
        assertEquals(0.0, imu.getYaw(), 1e-6);
        assertEquals(0.0, imu.getYawRate(), 1e-6);
        assertEquals(0.0, imu.getRoll(), 1e-6);
        assertEquals(0.0, imu.getPitch(), 1e-6);
    }

    @Test
    void getHeadingReturnsRotation2d() {
        CANDeviceID id = new CANDeviceID(37);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertNotNull(imu.getHeading());
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(38);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(imu::outputTelemetry);
    }

    @Test
    void refreshDoesNotThrowBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(39);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(imu::refresh);
    }

    @Test
    void refreshDoesNotThrowAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(70);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        imu.robotInit(0.0);
        assertDoesNotThrow(imu::refresh);
    }

    @Test
    void applyConfigDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(71);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(imu::applyConfig);
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(72);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(() -> imu.simulationPeriodic(0.0));
    }
}
