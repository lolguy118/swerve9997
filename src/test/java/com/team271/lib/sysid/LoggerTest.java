package com.team271.lib.sysid;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggerTest {

    private Logger logger;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        logger = new Logger();
    }

    // ── Constants ──

    @Test
    void dataVectorSize_is36000() {
        assertEquals(36000, Logger.DATA_VECTOR_SIZE);
    }

    @Test
    void threadPriority_is15() {
        assertEquals(15, Logger.THREAD_PRIORITY);
    }

    @Test
    void halThreadPriority_is40() {
        assertEquals(40, Logger.HAL_THREAD_PRIORITY);
    }

    // ── Constructor ──

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new Logger());
    }

    // ── reset ──

    @Test
    void reset_clearsState() {
        logger.motorVoltage = 5.0;
        logger.startTime = 10.0;
        logger.data.add(1.0);

        logger.reset();

        assertEquals(0.0, logger.motorVoltage, 1e-9);
        assertEquals(0.0, logger.startTime, 1e-9);
        assertTrue(logger.data.isEmpty());
    }

    // ── initLogger ──

    @Test
    void initLogger_setsStartTime() {
        logger.initLogger(1.5);
        assertEquals(1.5, logger.startTime, 1e-9);
    }

    @Test
    void initLogger_resetsData() {
        logger.data.add(42.0);
        logger.initLogger(0.0);
        assertTrue(logger.data.isEmpty());
    }

    // ── updateData ──

    @Test
    void updateData_quasistatic_voltageRampsByTime() {
        logger.testType = "Quasistatic";
        logger.voltageCommand = 2.0;
        logger.startTime = 0.0;

        logger.updateData(1.0);
        assertEquals(2.0, logger.motorVoltage, 1e-9);

        logger.updateData(2.0);
        assertEquals(4.0, logger.motorVoltage, 1e-9);
    }

    @Test
    void updateData_dynamic_voltageIsConstant() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 3.0;
        logger.startTime = 0.0;

        logger.updateData(1.0);
        assertEquals(3.0, logger.motorVoltage, 1e-9);

        logger.updateData(5.0);
        assertEquals(3.0, logger.motorVoltage, 1e-9);
    }

    @Test
    void updateData_unknownType_voltageIsZero() {
        logger.testType = "Unknown";
        logger.voltageCommand = 5.0;

        logger.updateData(1.0);
        assertEquals(0.0, logger.motorVoltage, 1e-9);
    }

    @Test
    void updateData_nullType_voltageIsZero() {
        logger.testType = null;
        logger.voltageCommand = 5.0;

        logger.updateData(1.0);
        assertEquals(0.0, logger.motorVoltage, 1e-9);
    }

    // ── isWrongMechanism ──

    @Test
    void isWrongMechanism_alwaysFalse() {
        assertFalse(logger.isWrongMechanism());
    }

    // ── sendData ──

    @Test
    void sendData_doesNotThrow() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 1.0;
        logger.data.add(0.1);
        logger.data.add(1.0);
        assertDoesNotThrow(() -> logger.sendData());
    }

    @Test
    void sendData_emptyData_doesNotThrow() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 1.0;
        assertDoesNotThrow(() -> logger.sendData());
    }

    // ── clearWhenReceived ──

    @Test
    void clearWhenReceived_doesNotThrow() {
        assertDoesNotThrow(() -> logger.clearWhenReceived());
    }

    @Test
    void sendData_quasistatic_negativeVoltage() {
        logger.testType = "Quasistatic";
        logger.voltageCommand = -1.0;
        logger.data.add(0.1);
        assertDoesNotThrow(() -> logger.sendData());
    }

    @Test
    void sendData_quasistatic_positiveVoltage() {
        logger.testType = "Quasistatic";
        logger.voltageCommand = 1.0;
        assertDoesNotThrow(() -> logger.sendData());
    }

    @Test
    void initLogger_withNonEmptyMechanism() {
        edu.wpi.first.wpilibj.smartdashboard.SmartDashboard.putString("SysIdTest", "Simple");
        assertDoesNotThrow(() -> logger.initLogger(0.0));
    }

    @Test
    void updateThreadPriority_inSimDoesNotThrow() {
        assertDoesNotThrow(() -> logger.updateThreadPriority());
    }
}
