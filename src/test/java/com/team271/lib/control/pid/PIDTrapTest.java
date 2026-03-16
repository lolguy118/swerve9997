package com.team271.lib.control.pid;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PIDTrapTest {

    private PIDTrap pid;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        pid = new PIDTrap(null, "Test", 1.0, 0.0, 0.0, 0.05, 10.0, 20.0);
    }

    /* --- Constructor --- */

    @Test
    void constructorSetsGains() {
        assertEquals(1.0, pid.getP());
        assertEquals(0.0, pid.getI());
        assertEquals(0.0, pid.getD());
    }

    @Test
    void constructorSetsConstraints() {
        TrapezoidProfile.Constraints c = pid.getConstraints();
        assertNotNull(c);
        assertEquals(10.0, c.maxVelocity, 1e-9);
        assertEquals(20.0, c.maxAcceleration, 1e-9);
    }

    /* --- Reset --- */

    @Test
    void resetWithPositionAndVelocity() {
        pid.setGoal(5.0);
        pid.calc(0.0, 0.0);

        pid.reset(2.0, 1.0);

        TrapezoidProfile.State sp = pid.getSetpoint();
        assertEquals(2.0, sp.position, 1e-9);
        assertEquals(1.0, sp.velocity, 1e-9);

        assertFalse(pid.atSetpoint());
    }

    @Test
    void resetWithPositionOnlySetsVelocityZero() {
        pid.reset(3.0);

        TrapezoidProfile.State sp = pid.getSetpoint();
        assertEquals(3.0, sp.position, 1e-9);
        assertEquals(0.0, sp.velocity, 1e-9);
    }

    @Test
    void resetWithState() {
        TrapezoidProfile.State state = new TrapezoidProfile.State(4.0, 2.0);
        pid.reset(state);

        TrapezoidProfile.State sp = pid.getSetpoint();
        assertEquals(4.0, sp.position, 1e-9);
        assertEquals(2.0, sp.velocity, 1e-9);
    }

    /* --- setGoal --- */

    @Test
    void setGoalUpdatesGoalPosition() {
        pid.setGoal(7.5);

        TrapezoidProfile.State goal = pid.getGoal();
        assertEquals(7.5, goal.position, 1e-9);
        assertEquals(0.0, goal.velocity, 1e-9);
    }

    @Test
    void setGoalWithState() {
        TrapezoidProfile.State goalState = new TrapezoidProfile.State(3.0, 1.0);
        pid.setGoal(goalState);

        assertEquals(3.0, pid.getGoal().position, 1e-9);
        assertEquals(0.0, pid.getGoal().velocity, 1e-9);
    }

    /* --- calc produces output --- */

    @Test
    void calcProducesNonZeroOutput() {
        pid.reset(0.0);
        pid.setGoal(5.0);

        double output = pid.calc(0.0, 0.0);
        assertNotEquals(0.0, output, "calc should produce non-zero output when goal differs");
    }

    @Test
    void calcMultipleStepsMovesTowardGoal() {
        pid.reset(0.0);
        pid.setGoal(1.0);

        double prev = 0.0;
        for (int i = 0; i < 50; i++) {
            double output = pid.calc(prev, i * 0.02);
            prev += output * 0.02;
        }

        TrapezoidProfile.State sp = pid.getSetpoint();
        assertTrue(
                sp.position > 0.0,
                "Setpoint should have moved toward the goal after multiple calc steps");
    }

    /* --- atSetpoint --- */

    @Test
    void atSetpointFalseBeforeCalc() {
        assertFalse(pid.atSetpoint());
    }

    @Test
    void atSetpointTrueWhenErrorWithinTolerance() {
        pid.reset(5.0);
        pid.setGoal(5.0);

        pid.calc(5.0, 0.0);
        assertTrue(pid.atSetpoint());
    }

    /* --- atGoal --- */

    @Test
    void atGoalFalseWhenSetpointNotAtGoal() {
        pid.reset(0.0);
        pid.setGoal(10.0);
        pid.calc(0.0, 0.0);

        assertFalse(pid.atGoal());
    }

    @Test
    void atGoalTrueWhenSetpointEqualsGoalAndAtSetpoint() {
        pid.reset(5.0);
        pid.setGoal(5.0);
        pid.calc(5.0, 0.0);

        assertTrue(pid.atGoal());
    }

    /* --- Tolerance --- */

    @Test
    void setToleranceAffectsAtSetpoint() {
        pid.reset(0.0);
        pid.setGoal(0.03);
        pid.setTolerance(0.01);

        pid.calc(0.0, 0.0);

        assertFalse(pid.atSetpoint(), "Error 0.03 should exceed tolerance 0.01");
    }

    @Test
    void largeToleranceAcceptsLargeError() {
        pid.reset(0.0);
        pid.setGoal(0.5);
        pid.setTolerance(1.0);

        pid.calc(0.0, 0.0);

        assertTrue(pid.atSetpoint(), "Error 0.5 should be within tolerance 1.0");
    }

    /* --- setP/setI/setD sync --- */

    @Test
    void setPUpdatesGain() {
        pid.setP(5.0);
        assertEquals(5.0, pid.getP());
    }

    @Test
    void setIUpdatesGain() {
        pid.setI(0.5);
        assertEquals(0.5, pid.getI());
    }

    @Test
    void setDUpdatesGain() {
        pid.setD(2.0);
        assertEquals(2.0, pid.getD());
    }

    @Test
    void setPIDUpdatesAllGains() {
        pid.setPID(3.0, 4.0, 5.0);
        assertEquals(3.0, pid.getP());
        assertEquals(4.0, pid.getI());
        assertEquals(5.0, pid.getD());
    }

    /* --- setConstraints --- */

    @Test
    void setConstraintsUpdatesProfile() {
        TrapezoidProfile.Constraints newConstraints = new TrapezoidProfile.Constraints(5.0, 10.0);
        pid.setConstraints(newConstraints);

        TrapezoidProfile.Constraints c = pid.getConstraints();
        assertEquals(5.0, c.maxVelocity, 1e-9);
        assertEquals(10.0, c.maxAcceleration, 1e-9);
    }

    /* --- getSetpointVel --- */

    @Test
    void getSetpointVelReturnsVelocity() {
        pid.reset(0.0, 3.0);
        assertEquals(3.0, pid.getSetpointVel(), 1e-9);
    }

    /* --- outputTelemetry --- */

    @Test
    void outputTelemetryDoesNotThrow() {
        pid.reset(0.0);
        pid.setGoal(1.0);
        pid.calc(0.0, 0.0);
        assertDoesNotThrow(() -> pid.outputTelemetry());
    }
}
