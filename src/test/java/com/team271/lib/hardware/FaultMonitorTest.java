package com.team271.lib.hardware;

import static org.junit.jupiter.api.Assertions.*;

import com.ctre.phoenix6.hardware.TalonFX;
import com.team271.lib.TObj;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
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
        resetCTREManager();
        parent =
                new ControllerTalonFX(
                        null,
                        "TestParent",
                        new CANDeviceID(99),
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
        TalonFX talon = new TalonFX(98);
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
        TalonFX talon = new TalonFX(97);
        fm.addFault("BootDuringEnable", talon.getStickyFault_BootDuringEnable(), 250.0);

        assertDoesNotThrow(fm::registerSignals);
    }

    @Test
    void refreshDoesNotThrow() {
        FaultMonitor fm = new FaultMonitor(parent, "TestDevice");
        TalonFX talon = new TalonFX(96);
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
        TalonFX talon = new TalonFX(95);
        fm.addFault("BootDuringEnable", talon.getStickyFault_BootDuringEnable(), 250.0);
        fm.refresh();

        assertFalse(fm.hasAnyFault());
    }

    /** Reset CTREManager static state via reflection for test isolation. */
    private static void resetCTREManager() throws Exception {
        Class<?> clz = com.team271.lib.hardware.CTREManager.class;
        for (String fieldName : new String[] {"signalsAll", "devices", "devicesByBus", "buses"}) {
            try {
                Field f = clz.getDeclaredField(fieldName);
                f.setAccessible(true);
                Object val = f.get(null);
                if (val instanceof java.util.Collection) {
                    ((java.util.Collection<?>) val).clear();
                } else if (val instanceof java.util.Map) {
                    ((java.util.Map<?, ?>) val).clear();
                }
            } catch (NoSuchFieldException e) {
                // Skip
            }
        }
        try {
            Field f = clz.getDeclaredField("initialized");
            f.setAccessible(true);
            f.setBoolean(null, false);
        } catch (NoSuchFieldException e) {
            // Skip
        }
        try {
            Field f = clz.getDeclaredField("signalsAllArray");
            f.setAccessible(true);
            f.set(null, null);
        } catch (NoSuchFieldException e) {
            // Skip
        }
    }
}
