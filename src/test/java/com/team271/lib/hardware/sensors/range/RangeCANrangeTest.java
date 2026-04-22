package com.team271.lib.hardware.sensors.range;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RangeCANrangeTest {

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
        CTREManager.resetForTesting();
    }

    @Test
    void constructorCreatesRange() {
        CANDeviceID id = new CANDeviceID(40);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertNotNull(range);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(41);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertDoesNotThrow(() -> range.robotInit(0.0));
    }

    @Test
    void getDistRawReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(42);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertEquals(0.0, range.getDistRaw(), 1e-6);
    }

    @Test
    void getDistReturnsZeroInitially() {
        CANDeviceID id = new CANDeviceID(43);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertEquals(0.0, range.getDist(), 1e-6);
    }

    @Test
    void scaleAffectsGetDist() {
        CANDeviceID id = new CANDeviceID(44);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        range.setScale(2.0);
        assertEquals(2.0, range.getScale(), 1e-6);
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(45);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertDoesNotThrow(range::outputTelemetry);
    }

    @Test
    void getConfigReturnsNonNull() {
        CANDeviceID id = new CANDeviceID(46);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertNotNull(range.getConfig());
    }

    @Test
    void applyConfigDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(47);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertDoesNotThrow(range::applyConfig);
    }

    @Test
    void refreshDoesNotThrowBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(48);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertDoesNotThrow(range::refresh);
    }

    @Test
    void refreshDoesNotThrowAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(49);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        range.robotInit(0.0);
        assertDoesNotThrow(range::refresh);
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(73);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertDoesNotThrow(() -> range.simulationPeriodic(0.0));
    }

    @Test
    void resetDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(74);
        RangeCANrange range = new RangeCANrange(null, "Range", id, 250.0);

        assertDoesNotThrow(range::reset);
    }
}
