package com.team271.lib.hardware;

import static org.junit.jupiter.api.Assertions.*;

import com.ctre.phoenix6.hardware.TalonFX;
import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class FaultMonitorTest {

    private TObj parent;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setUp() throws Exception {
        CTREManager.resetForTesting();
        parent =
                new ControllerTalonFX(
                        null,
                        "TestParent",
                        new CANDeviceID(37),
                        new MotorBase(MotorBase.MotorType.KRAKENX60));
    }

    @Test
    void constructorDoesNotThrow() {
        assertDoesNotThrow(() -> new FaultMonitor(parent, "TestDevice"));
    }

    @Test
    void hasAnyFaultFalseWhenEmpty() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        assertFalse(fm.hasAnyFault());
    }

    @Test
    void addFaultDoesNotThrow() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        TalonFX talon = new TalonFX(36);
        assertDoesNotThrow(
                () ->
                        fm.addFault(
                                "BootDuringEnable",
                                talon.getStickyFault_BootDuringEnable(),
                                250.0));
    }

    @Test
    void registerSignalsDoesNotThrow() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        TalonFX talon = new TalonFX(35);
        fm.addFault("BootDuringEnable", talon.getStickyFault_BootDuringEnable(), 250.0);

        assertDoesNotThrow(fm::registerSignals);
    }

    @Test
    void refreshDoesNotThrow() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        TalonFX talon = new TalonFX(34);
        fm.addFault("BootDuringEnable", talon.getStickyFault_BootDuringEnable(), 250.0);

        assertDoesNotThrow(fm::refresh);
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        assertDoesNotThrow(fm::outputTelemetry);
    }

    @Test
    void hasAnyFaultFalseAfterRefreshWithNoFaults() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        TalonFX talon = new TalonFX(33);
        fm.addFault("BootDuringEnable", talon.getStickyFault_BootDuringEnable(), 250.0);
        fm.refresh();

        assertFalse(fm.hasAnyFault());
    }
}
