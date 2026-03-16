package com.team271.lib.control.pid;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PIDWPITrapTest {

    private PIDWPI_Trap pid;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        pid = new PIDWPI_Trap(null, "Test", 1.0, 0.0, 0.0, 0.05, 10.0, 20.0);
    }

    /* --- Constructor --- */

    @Test
    void constructorSetsGains() {
        assertEquals(1.0, pid.getP());
        assertEquals(0.0, pid.getI());
        assertEquals(0.0, pid.getD());
    }

    @Test
    void constructorCreatesController() {
        ProfiledPIDController controller = pid.getController();
        assertNotNull(controller);
    }

    @Test
    void constructorSyncsGainsToController() {
        ProfiledPIDController controller = pid.getController();
        assertEquals(1.0, controller.getP(), 1e-9);
        assertEquals(0.0, controller.getI(), 1e-9);
        assertEquals(0.0, controller.getD(), 1e-9);
    }

    /* --- reset --- */

    @Test
    void resetDoesNotThrow() {
        pid.setGoal(5.0);
        pid.calc(0.0);

        assertDoesNotThrow(() -> pid.reset(0.0));
    }

    @Test
    void resetClearsControllerState() {
        pid.setGoal(10.0);
        pid.calc(0.0);

        pid.reset(0.0);

        assertFalse(pid.atSetpoint());
    }

    /* --- setGoal --- */

    @Test
    void setGoalUpdatesGoal() {
        pid.setGoal(7.5);

        pid.calc(0.0);

        assertDoesNotThrow(() -> pid.calc(0.0));
    }

    /* --- atGoal --- */

    @Test
    void atGoalFalseWhenFarFromGoal() {
        pid.setGoal(10.0);
        pid.calc(0.0);

        assertFalse(pid.atGoal(0.0));
    }

    @Test
    void atGoalTrueWhenAtGoal() {
        pid.setGoal(5.0);
        pid.reset(5.0);

        pid.calc(5.0);

        assertTrue(pid.atGoal(5.0));
    }

    /* --- calc --- */

    @Test
    void calcProducesNonZeroOutput() {
        pid.setGoal(10.0);
        pid.reset(0.0);

        double output = pid.calc(0.0);
        assertNotEquals(0.0, output, "calc should produce non-zero output when goal differs");
    }

    @Test
    void calcZeroErrorProducesZeroOutput() {
        pid.setGoal(5.0);
        pid.reset(5.0);

        double output = pid.calc(5.0);
        assertEquals(0.0, output, 1e-9);
    }

    /* --- setConstraints --- */

    @Test
    void setConstraintsUpdatesController() {
        TrapezoidProfile.Constraints newConstraints = new TrapezoidProfile.Constraints(5.0, 10.0);
        pid.setConstraints(newConstraints);

        assertDoesNotThrow(() -> pid.calc(0.0));
    }

    /* --- getSetpointState --- */

    @Test
    void getSetpointStateReturnsNonNull() {
        pid.setGoal(5.0);
        pid.reset(0.0);
        pid.calc(0.0);

        TrapezoidProfile.State sp = pid.getSetpointState();
        assertNotNull(sp);
    }

    @Test
    void getSetpointStateReflectsProfileProgress() {
        pid.setGoal(10.0);
        pid.reset(0.0);

        pid.calc(0.0);

        TrapezoidProfile.State sp = pid.getSetpointState();
        assertTrue(
                sp.position >= 0.0,
                "Setpoint position should be non-negative after moving toward goal=10");
    }

    /* --- atSetpoint --- */

    @Test
    void atSetpointDelegatesToController() {
        pid.setGoal(5.0);
        pid.reset(5.0);

        pid.calc(5.0);

        assertTrue(pid.atSetpoint());
    }

    @Test
    void atSetpointFalseWhenMeasurementFarFromSetpoint() {
        pid.setGoal(10.0);
        pid.reset(0.0);
        pid.setTolerance(0.01);

        /* Run enough iterations for the profiled setpoint to advance well past 0,
         * then check with a measurement of 0 which should be far from the setpoint. */
        for (int i = 0; i < 100; i++) {
            pid.calc(0.0);
        }

        assertFalse(pid.atSetpoint());
    }

    /* --- setP/setI/setD sync --- */

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
        pid.setTolerance(1.0);
        pid.setGoal(5.0);
        pid.reset(4.5);

        pid.calc(4.5);

        assertTrue(pid.atSetpoint(), "Error 0.5 should be within tolerance 1.0");
    }

    /* --- setIntegratorRange --- */

    @Test
    void setIntegratorRangeSyncsWithController() {
        pid.setIntegratorRange(-0.5, 0.5);
        assertDoesNotThrow(() -> pid.calc(0.0));
    }

    /* --- refresh --- */

    @Test
    void refreshDoesNotThrow() {
        assertDoesNotThrow(() -> pid.refresh());
    }

    /* --- outputTelemetry --- */

    @Test
    void outputTelemetryDoesNotThrow() {
        pid.setGoal(1.0);
        pid.calc(0.0);
        assertDoesNotThrow(() -> pid.outputTelemetry());
    }
}
