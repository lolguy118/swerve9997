package com.team271.lib.sysid;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggerGeneralTest {

    private LoggerGeneral logger;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        logger = new LoggerGeneral();
    }

    // ── getMotorVoltage ──

    @Test
    void getMotorVoltage_initiallyZero() {
        assertEquals(0.0, logger.getMotorVoltage(), 1e-9);
    }

    @Test
    void getMotorVoltage_afterDynamicUpdate() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 6.0;
        logger.updateData(1.0);
        assertEquals(6.0, logger.getMotorVoltage(), 1e-9);
    }

    // ── log ──

    @Test
    void log_addsFourDoubles() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 1.0;

        logger.log(0.1, 12.0, 5.0, 2.5);
        assertEquals(4, logger.data.size());
    }

    @Test
    void log_storesCorrectValues() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 1.0;

        logger.log(0.5, 12.0, 3.0, 1.5);
        assertEquals(0.5, logger.data.get(0), 1e-9);
        assertEquals(12.0, logger.data.get(1), 1e-9);
        assertEquals(3.0, logger.data.get(2), 1e-9);
        assertEquals(1.5, logger.data.get(3), 1e-9);
    }

    @Test
    void log_multipleEntries() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 1.0;

        logger.log(0.1, 1.0, 2.0, 3.0);
        logger.log(0.2, 4.0, 5.0, 6.0);
        assertEquals(8, logger.data.size());
    }

    @Test
    void log_callsUpdateData() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 5.0;

        logger.log(1.0, 12.0, 0.0, 0.0);
        assertEquals(5.0, logger.getMotorVoltage(), 1e-9);
    }

    // ── isWrongMechanism ──

    @Test
    void isWrongMechanism_emptyString_false() {
        logger.mechanism = "";
        assertFalse(logger.isWrongMechanism());
    }

    @Test
    void isWrongMechanism_arm_false() {
        logger.mechanism = "Arm";
        assertFalse(logger.isWrongMechanism());
    }

    @Test
    void isWrongMechanism_elevator_false() {
        logger.mechanism = "Elevator";
        assertFalse(logger.isWrongMechanism());
    }

    @Test
    void isWrongMechanism_simple_false() {
        logger.mechanism = "Simple";
        assertFalse(logger.isWrongMechanism());
    }

    @Test
    void isWrongMechanism_unknown_true() {
        logger.mechanism = "Drivetrain";
        assertTrue(logger.isWrongMechanism());
    }

    @Test
    void isWrongMechanism_caseSensitive() {
        logger.mechanism = "arm";
        assertTrue(logger.isWrongMechanism());
    }

    // ── reset clears log data ──

    @Test
    void reset_clearsLoggedData() {
        logger.testType = "Dynamic";
        logger.voltageCommand = 1.0;
        logger.log(0.1, 1.0, 2.0, 3.0);

        logger.reset();
        assertTrue(logger.data.isEmpty());
        assertEquals(0.0, logger.getMotorVoltage(), 1e-9);
    }
}
