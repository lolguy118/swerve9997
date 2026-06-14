package com.team271.lib.hardware.sensors.imu;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IMUPigeon2Test {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetCTREManager() {
        CTREManager.resetForTesting();
    }

    @AfterEach
    void closeDevices() {
        /*
         * Closing devices unregisters their Phoenix 6 sim state. If left open
         * across tests, the sim library can dereference freed handles during
         * later device construction and SIGSEGV the JVM.
         */
        CTREManager.resetForTesting();
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
        CANDeviceID id = new CANDeviceID(8);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        imu.robotInit(0.0);
        assertDoesNotThrow(imu::refresh);
    }

    @Test
    void applyConfigDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(9);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(imu::applyConfig);
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(10);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(() -> imu.simulationPeriodic(0.0));
    }

    /* ========== Additional coverage tests ========== */

    @Test
    void refreshAfterRobotInitReturnsZeroValues() {
        CANDeviceID id = new CANDeviceID(12);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);
        imu.robotInit(0.0);
        imu.refresh();

        /* In sim, signals won't have OK status */
        assertEquals(0.0, imu.getYaw(), 1e-6);
        assertEquals(0.0, imu.getRoll(), 1e-6);
        assertEquals(0.0, imu.getPitch(), 1e-6);
    }

    @Test
    void outputTelemetryAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(13);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);
        imu.robotInit(0.0);

        assertDoesNotThrow(imu::outputTelemetry);
    }

    @Test
    void simulationInitDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(14);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);

        assertDoesNotThrow(() -> imu.simulationInit(0.0));
    }

    @Test
    void refreshBeforeRobotInitDoesNotChangeValues() {
        CANDeviceID id = new CANDeviceID(15);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);
        imu.refresh();
        assertEquals(0.0, imu.getYaw(), 1e-6);
        assertEquals(0.0, imu.getYawRate(), 1e-6);
        assertEquals(0.0, imu.getRoll(), 1e-6);
        assertEquals(0.0, imu.getPitch(), 1e-6);
    }

    @Test
    void getHeadingReturnsZeroDegInitially() {
        CANDeviceID id = new CANDeviceID(16);
        IMUPigeon2 imu = new IMUPigeon2(null, "IMU", id, 250.0);
        assertNotNull(imu.getHeading());
        assertEquals(0.0, imu.getHeading().getDegrees(), 1e-6);
    }
}
