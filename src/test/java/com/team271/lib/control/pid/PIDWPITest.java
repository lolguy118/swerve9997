package com.team271.lib.control.pid;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.controller.PIDController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PIDWPITest {

    private PIDWPI pid;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        pid = new PIDWPI(null, "Test", 0.02, 1.0, 0.0, 0.0, 0.05);
    }

    /* --- Constructor --- */

    @Test
    void constructorSetsGains() {
        assertEquals(1.0, pid.getP());
        assertEquals(0.0, pid.getI());
        assertEquals(0.0, pid.getD());
    }

    @Test
    void constructorWithoutTolerance() {
        PIDWPI simple = new PIDWPI(null, "NoTol", 0.02, 2.0, 0.5, 0.1);
        assertEquals(2.0, simple.getP());
        assertEquals(0.5, simple.getI());
        assertEquals(0.1, simple.getD());
    }

    @Test
    void constructorMinimal() {
        PIDWPI minimal = new PIDWPI(null, "Min", 0.02);
        assertEquals(0.0, minimal.getP());
        assertEquals(0.0, minimal.getI());
        assertEquals(0.0, minimal.getD());
    }

    /* --- getController --- */

    @Test
    void getControllerReturnsNonNull() {
        PIDController controller = pid.getController();
        assertNotNull(controller);
    }

    @Test
    void getControllerSyncsGains() {
        PIDController controller = pid.getController();
        assertEquals(1.0, controller.getP(), 1e-9);
        assertEquals(0.0, controller.getI(), 1e-9);
        assertEquals(0.0, controller.getD(), 1e-9);
    }

    /* --- reset --- */

    @Test
    void resetClearsState() {
        pid.setSetpoint(10.0);
        pid.calc(0.0);

        pid.reset();

        assertFalse(pid.atSetpoint());
    }

    /* --- setSetpoint --- */

    @Test
    void setSetpointDelegates() {
        pid.setSetpoint(5.0);
        assertEquals(5.0, pid.getController().getSetpoint(), 1e-9);
    }

    /* --- calc --- */

    @Test
    void calcProducesOutput() {
        pid.setSetpoint(10.0);
        double output = pid.calc(0.0);

        assertNotEquals(0.0, output, "calc should produce non-zero output with error");
    }

    @Test
    void calcZeroErrorProducesZeroOutput() {
        pid.setSetpoint(5.0);
        double output = pid.calc(5.0);
        assertEquals(0.0, output, 1e-9);
    }

    /* --- atSetpoint --- */

    @Test
    void atSetpointDelegatesToController() {
        pid.setSetpoint(5.0);
        pid.setTolerance(0.1);

        pid.calc(5.0);

        assertTrue(pid.atSetpoint());
    }

    @Test
    void atSetpointFalseWhenOutsideTolerance() {
        pid.setSetpoint(10.0);
        pid.setTolerance(0.1);

        pid.calc(0.0);

        assertFalse(pid.atSetpoint());
    }

    /* --- setP/setI/setD sync with WPILib controller --- */

    @Test
    void setPSyncsWithController() {
        pid.setP(3.0);

        assertEquals(3.0, pid.getP());
        assertEquals(3.0, pid.getController().getP(), 1e-9);
    }

    @Test
    void setISyncsWithController() {
        pid.setI(0.5);

        assertEquals(0.5, pid.getI());
        assertEquals(0.5, pid.getController().getI(), 1e-9);
    }

    @Test
    void setDSyncsWithController() {
        pid.setD(2.0);

        assertEquals(2.0, pid.getD());
        assertEquals(2.0, pid.getController().getD(), 1e-9);
    }

    @Test
    void setPIDSyncsWithController() {
        pid.setPID(4.0, 5.0, 6.0);

        assertEquals(4.0, pid.getP());
        assertEquals(5.0, pid.getI());
        assertEquals(6.0, pid.getD());

        assertEquals(4.0, pid.getController().getP(), 1e-9);
        assertEquals(5.0, pid.getController().getI(), 1e-9);
        assertEquals(6.0, pid.getController().getD(), 1e-9);
    }

    /* --- setTolerance --- */

    @Test
    void setToleranceSyncsWithController() {
        pid.setTolerance(0.2);
        pid.setSetpoint(5.0);

        pid.calc(4.85);
        assertTrue(pid.atSetpoint(), "Error 0.15 should be within tolerance 0.2");

        pid.calc(4.0);
        assertFalse(pid.atSetpoint(), "Error 1.0 should exceed tolerance 0.2");
    }

    /* --- setIntegratorRange --- */

    @Test
    void setIntegratorRangeSyncsWithController() {
        pid.setIntegratorRange(-0.5, 0.5);

        assertDoesNotThrow(() -> pid.calc(0.0));
    }

    /* --- reset with initialized controller --- */

    @Test
    void resetWithInitializedControllerClearsState() {
        pid.setSetpoint(10.0);
        pid.calc(0.0);
        pid.calc(5.0);

        pid.reset();

        // After reset, the controller's internal state is cleared
        pid.setSetpoint(0.0);
        pid.setTolerance(0.1);
        pid.calc(0.0);
        assertTrue(pid.atSetpoint(), "After reset and zero error, should be at setpoint");
    }

    /* --- atSetpoint delegation --- */

    @Test
    void atSetpointReturnsFalseBeforeAnyCalc() {
        PIDWPI fresh = new PIDWPI(null, "Fresh", 0.02, 1.0, 0.0, 0.0, 0.05);
        assertFalse(fresh.atSetpoint());
    }

    /* --- setSetpoint then calc --- */

    @Test
    void setSetpointThenCalcProducesCorrectOutput() {
        pid.setSetpoint(10.0);
        double output = pid.calc(8.0);
        // P=1.0, error=10-8=2, output=2.0
        assertEquals(2.0, output, 1e-9);
    }

    /* --- Two-arg constructor --- */

    @Test
    void twoArgConstructorSetsGainsAndZeroTolerance() {
        PIDWPI twoArg = new PIDWPI(null, "TwoArg", 0.02, 3.0, 1.0, 0.5);
        assertEquals(3.0, twoArg.getP());
        assertEquals(1.0, twoArg.getI());
        assertEquals(0.5, twoArg.getD());
        // Controller should be synced
        assertEquals(3.0, twoArg.getController().getP(), 1e-9);
        assertEquals(1.0, twoArg.getController().getI(), 1e-9);
        assertEquals(0.5, twoArg.getController().getD(), 1e-9);
    }

    /* --- One-arg constructor --- */

    @Test
    void oneArgConstructorDefaultsToZeroGains() {
        PIDWPI oneArg = new PIDWPI(null, "OneArg", 0.02);
        assertEquals(0.0, oneArg.getP());
        assertEquals(0.0, oneArg.getI());
        assertEquals(0.0, oneArg.getD());
        assertEquals(0.0, oneArg.getController().getP(), 1e-9);
    }

    /* --- refresh --- */

    @Test
    void refreshDoesNotThrow() {
        assertDoesNotThrow(() -> pid.refresh());
    }

    /* --- outputTelemetry --- */

    @Test
    void outputTelemetryDoesNotThrow() {
        pid.setSetpoint(1.0);
        pid.calc(0.0);
        assertDoesNotThrow(() -> pid.outputTelemetry());
    }

    /* --- Continuous input --- */

    @Test
    void enableContinuousInputOnController() {
        pid.getController().enableContinuousInput(-Math.PI, Math.PI);
        pid.setSetpoint(3.0);
        double output = pid.calc(-3.0);
        assertNotEquals(0.0, output);
    }
}
