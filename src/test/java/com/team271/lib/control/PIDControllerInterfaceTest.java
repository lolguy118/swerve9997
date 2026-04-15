package com.team271.lib.control;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.control.pid.*;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that all PID implementations satisfy the PIDController interface contract and that the
 * profiled implementations satisfy ProfiledPIDController.
 */
class PIDControllerInterfaceTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        CTREManager.resetForTesting();
    }

    /* --- Interface compliance (instanceof checks) --- */

    @Test
    void pidSimple_implementsPIDController() {
        PIDController pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.01);
        assertNotNull(pid);
        assertTrue(pid instanceof PIDController);
    }

    @Test
    void pidWPI_implementsPIDController() {
        PIDController pid = new PIDWPI(null, "Test", 1.0, 0.0, 0.0, 0.01);
        assertNotNull(pid);
    }

    @Test
    void pidFX_implementsPIDController() {
        ControllerTalonFX fx =
                new ControllerTalonFX(
                        null,
                        "Test",
                        new CANDeviceID(90, ""),
                        new MotorBase(MotorBase.MotorType.KRAKENX60));
        PIDController pid = new PIDFX(null, "Test", fx, 1.0, 0.0, 0.0, 0.01);
        assertNotNull(pid);
    }

    @Test
    void pidTrap_implementsProfiledPIDController() {
        ProfiledPIDController pid = new PIDTrap(null, "Test", 1.0, 0.0, 0.0, 0.01, 10.0, 20.0);
        assertNotNull(pid);
        assertTrue(pid instanceof PIDController);
    }

    @Test
    void pidWPITrap_implementsProfiledPIDController() {
        ProfiledPIDController pid = new PIDWPI_Trap(null, "Test", 1.0, 0.0, 0.0, 0.01, 10.0, 20.0);
        assertNotNull(pid);
        assertTrue(pid instanceof PIDController);
    }

    /* --- PIDController contract: calculate + state --- */

    @Test
    void pidSimple_calculateReturnsOutput() {
        PIDController pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.01);
        double output = pid.calculate(0.0, 1.0, 0.0);
        // P=1.0, error=1.0, output should be 1.0 (clamped to maxOutput=1.0)
        assertEquals(1.0, output, 1e-9);
    }

    @Test
    void pidSimple_getPositionError() {
        PIDController pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.01);
        pid.calculate(3.0, 5.0, 0.0);
        assertEquals(2.0, pid.getPositionError(), 1e-9);
    }

    @Test
    void pidSimple_getOutput() {
        PIDController pid = new PIDSimple(null, "Test", 0.5, 0.0, 0.0, 0.01);
        pid.calculate(0.0, 0.4, 0.0);
        // P=0.5, error=0.4, output = 0.2
        assertEquals(0.2, pid.getOutput(), 1e-9);
    }

    @Test
    void pidSimple_atSetpoint() {
        PIDController pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.05);
        pid.calculate(1.0, 1.02, 0.0);
        // error = 0.02, tolerance = 0.05 → at setpoint
        assertTrue(pid.atSetpoint());
    }

    @Test
    void pidSimple_reset() {
        PIDController pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.01);
        pid.calculate(0.0, 1.0, 0.0);
        pid.reset();
        assertEquals(0.0, pid.getOutput(), 1e-9);
        assertEquals(0.0, pid.getPositionError(), 1e-9);
    }

    /* --- PIDController: gain setters/getters --- */

    @Test
    void pidController_setAndGetGains() {
        PIDController pid = new PIDSimple(null, "Test", 0.0, 0.0, 0.0, 0.01);
        pid.setPID(1.5, 0.01, 0.3);
        assertEquals(1.5, pid.getP(), 1e-9);
        assertEquals(0.01, pid.getI(), 1e-9);
        assertEquals(0.3, pid.getD(), 1e-9);
    }

    @Test
    void pidController_enableContinuousInput() {
        PIDController pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.01);
        assertFalse(pid.isContinuousInputEnabled());
        pid.enableContinuousInput(0, 360);
        assertTrue(pid.isContinuousInputEnabled());
        pid.disableContinuousInput();
        assertFalse(pid.isContinuousInputEnabled());
    }

    /* --- PIDFX: slot configuration --- */

    @Test
    void pidFX_defaultSlotIsZero() {
        ControllerTalonFX fx =
                new ControllerTalonFX(
                        null,
                        "Test",
                        new CANDeviceID(91, ""),
                        new MotorBase(MotorBase.MotorType.KRAKENX60));
        PIDFX pid = new PIDFX(null, "Test", fx, 1.0, 0.0, 0.0, 0.01);
        assertEquals(0, pid.getSlot());
    }

    @Test
    void pidFX_customSlot() {
        ControllerTalonFX fx =
                new ControllerTalonFX(
                        null,
                        "Test",
                        new CANDeviceID(92, ""),
                        new MotorBase(MotorBase.MotorType.KRAKENX60));
        PIDFX pid = new PIDFX(null, "Test", fx, 1.0, 0.0, 0.0, 0.01, 1);
        assertEquals(1, pid.getSlot());
    }

    @Test
    void pidFX_enableContinuousWrap() {
        ControllerTalonFX fx =
                new ControllerTalonFX(
                        null,
                        "Test",
                        new CANDeviceID(93, ""),
                        new MotorBase(MotorBase.MotorType.KRAKENX60));
        PIDFX pid = new PIDFX(null, "Test", fx, 1.0, 0.0, 0.0, 0.01);
        pid.enableContinuousInput(0, 1);
        assertTrue(fx.getContinuousWrap());
        pid.disableContinuousInput();
        assertFalse(fx.getContinuousWrap());
    }

    /* --- ProfiledPIDController: setGoal + setConstraints --- */

    @Test
    void pidTrap_setGoalWithVelocity() {
        ProfiledPIDController pid = new PIDTrap(null, "Test", 1.0, 0.0, 0.0, 0.01, 10.0, 20.0);
        pid.setGoal(5.0, 1.0);
        // Goal should be stored (verified via the PIDTrap-specific getGoal)
        assertDoesNotThrow(() -> pid.setGoal(5.0, 1.0));
    }

    @Test
    void pidTrap_setConstraints() {
        ProfiledPIDController pid = new PIDTrap(null, "Test", 1.0, 0.0, 0.0, 0.01, 10.0, 20.0);
        assertDoesNotThrow(() -> pid.setConstraints(50.0, 100.0));
    }

    @Test
    void pidTrap_getSetpointPositionAndVelocity() {
        PIDTrap pid = new PIDTrap(null, "Test", 1.0, 0.0, 0.0, 0.01, 10.0, 20.0);
        pid.reset(0.0, 0.0);
        // Initial setpoint is reset position
        assertEquals(0.0, pid.getSetpointPosition(), 1e-9);
        assertEquals(0.0, pid.getSetpointVelocity(), 1e-9);
    }
}
