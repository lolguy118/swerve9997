package com.team271.lib;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TObjTest {

    static class ConcreteTObj extends TObj {
        ConcreteTObj(final String argN) {
            super(argN);
        }

        ConcreteTObj(final TObj argP, final String argN) {
            super(argP, argN);
        }

        String testLogKey(final String argSuffix) {
            return logKey(argSuffix);
        }
    }

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /* --- Constructor with name --- */

    @Test
    void constructorSetsName() {
        ConcreteTObj obj = new ConcreteTObj("TestObj");

        assertEquals("TestObj", obj.getName());
    }

    @Test
    void constructorCreatesTable() {
        ConcreteTObj obj = new ConcreteTObj("TestObj");

        assertNotNull(obj.getTable());
    }

    /* --- Constructor with parent --- */

    @Test
    void parentConstructorSetsName() {
        ConcreteTObj parent = new ConcreteTObj("Parent");
        ConcreteTObj child = new ConcreteTObj(parent, "Child");

        assertEquals("Child", child.getName());
    }

    @Test
    void parentConstructorCreatesTable() {
        ConcreteTObj parent = new ConcreteTObj("Parent");
        ConcreteTObj child = new ConcreteTObj(parent, "Child");

        assertNotNull(child.getTable());
    }

    /* --- Lifecycle methods don't throw --- */

    @Test
    void robotLifecycleMethods() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(() -> obj.robotInit(0.0));
        assertDoesNotThrow(() -> obj.robotPeriodicBefore(0.0));
        assertDoesNotThrow(() -> obj.robotPeriodicAfter(0.0));
    }

    @Test
    void disabledLifecycleMethods() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(() -> obj.disabledInit(0.0));
        assertDoesNotThrow(() -> obj.disabledPeriodic(0.0));
        assertDoesNotThrow(() -> obj.disabledExit(0.0));
    }

    @Test
    void autonomousLifecycleMethods() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(() -> obj.autonomousInit(0.0));
        assertDoesNotThrow(() -> obj.autonomousPeriodic(0.0));
        assertDoesNotThrow(() -> obj.autonomousExit(0.0));
    }

    @Test
    void teleopLifecycleMethods() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(() -> obj.teleopInit(0.0));
        assertDoesNotThrow(() -> obj.teleopPeriodic(0.0));
        assertDoesNotThrow(() -> obj.teleopExit(0.0));
    }

    @Test
    void simulationLifecycleMethods() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(() -> obj.simulationInit(0.0));
        assertDoesNotThrow(() -> obj.simulationPeriodic(0.0));
    }

    @Test
    void testLifecycleMethods() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(() -> obj.testInit(0.0));
        assertDoesNotThrow(() -> obj.testPeriodic(0.0));
        assertDoesNotThrow(() -> obj.testExit(0.0));
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        ConcreteTObj obj = new ConcreteTObj("Lifecycle");

        assertDoesNotThrow(obj::outputTelemetry);
    }

    /* --- logKey --- */

    @Test
    void logKeyBuildsSuffix() {
        ConcreteTObj obj = new ConcreteTObj("MyObj");
        String key = obj.testLogKey("Position");
        assertTrue(key.endsWith("/Position"));
    }

    /* --- null parent table behavior --- */

    @Test
    void nullParentConstructorCreatesTable() {
        ConcreteTObj child = new ConcreteTObj(null, "Orphan");
        assertNotNull(child.getTable());
    }
}
