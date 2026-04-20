package com.team271.lib.hardware.controllers;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.control.PIDGains;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the new CTRE feature wrappers added in the Layer 1 redesign: slot selection, gravity
 * type, continuous wrap, software limits, kG/kA gains, unified current limit config.
 */
@SuppressWarnings("resource")
class ControllerTalonFXNewFeaturesTest {

    private static final MotorBase KRAKEN = new MotorBase(MotorBase.MotorType.KRAKENX60);
    private ControllerTalonFX controller;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        CTREManager.resetForTesting();
        controller = new ControllerTalonFX(null, "Test", new CANDeviceID(50, ""), KRAKEN);
    }

    /* --- Slot Selection --- */

    @Test
    void setOutputPosition_withSlot_doesNotThrow() {
        assertDoesNotThrow(() -> controller.setOutputPosition(5.0, 0, 0.5));
        assertDoesNotThrow(() -> controller.setOutputPosition(5.0, 1, 0.5));
        assertDoesNotThrow(() -> controller.setOutputPosition(5.0, 2, 0.5));
    }

    @Test
    void setOutputPosition_backwardCompat_usesSlotZero() {
        // The 2-arg version delegates to 3-arg with slot 0
        assertDoesNotThrow(() -> controller.setOutputPosition(5.0, 0.5));
    }

    @Test
    void setOutputVelocity_withSlot_doesNotThrow() {
        assertDoesNotThrow(() -> controller.setOutputVelocity(10.0, 0, 0.2));
        assertDoesNotThrow(() -> controller.setOutputVelocity(10.0, 1, 0.2));
        assertDoesNotThrow(() -> controller.setOutputVelocity(10.0, 2, 0.2));
    }

    @Test
    void setOutputVelocity_backwardCompat_usesSlotZero() {
        assertDoesNotThrow(() -> controller.setOutputVelocity(10.0, 0.2));
    }

    /* --- Gravity Type --- */

    @Test
    void setGravityType_armCosine_slot0() {
        controller.setGravityType(0, GravityType.ARM_COSINE);
        assertEquals(GravityType.ARM_COSINE, controller.getGravityType(0));
    }

    @Test
    void setGravityType_elevatorStatic_slot1() {
        controller.setGravityType(1, GravityType.ELEVATOR_STATIC);
        assertEquals(GravityType.ELEVATOR_STATIC, controller.getGravityType(1));
    }

    @Test
    void setGravityType_slot2() {
        controller.setGravityType(2, GravityType.ARM_COSINE);
        assertEquals(GravityType.ARM_COSINE, controller.getGravityType(2));
    }

    /* --- Continuous Wrap --- */

    @Test
    void setContinuousWrap_enableAndDisable() {
        assertFalse(controller.getContinuousWrap());

        controller.setContinuousWrap(true);
        assertTrue(controller.getContinuousWrap());

        controller.setContinuousWrap(false);
        assertFalse(controller.getContinuousWrap());
    }

    /* --- Software Limits --- */

    @Test
    void configSoftLimitForward_setsConfigValues() {
        controller.configSoftLimitForward(true, 100.0);
        assertTrue(controller.getConfig().SoftwareLimitSwitch.ForwardSoftLimitEnable);
        assertEquals(100.0, controller.getConfig().SoftwareLimitSwitch.ForwardSoftLimitThreshold);
    }

    @Test
    void configSoftLimitReverse_setsConfigValues() {
        controller.configSoftLimitReverse(true, -50.0);
        assertTrue(controller.getConfig().SoftwareLimitSwitch.ReverseSoftLimitEnable);
        assertEquals(-50.0, controller.getConfig().SoftwareLimitSwitch.ReverseSoftLimitThreshold);
    }

    /* --- kG and kA Gains --- */

    @Test
    void setGravityGain_slot0() {
        controller.setGravityGain(0, 0.15);
        assertEquals(0.15, controller.getGravityGain(0), 1e-9);
    }

    @Test
    void setAccelGain_slot0() {
        controller.setAccelGain(0, 0.003);
        assertEquals(0.003, controller.getAccelGain(0), 1e-9);
    }

    @Test
    void setGravityGain_perSlot_independent() {
        controller.setGravityGain(0, 0.1);
        controller.setGravityGain(1, 0.2);
        controller.setGravityGain(2, 0.3);

        assertEquals(0.1, controller.getGravityGain(0), 1e-9);
        assertEquals(0.2, controller.getGravityGain(1), 1e-9);
        assertEquals(0.3, controller.getGravityGain(2), 1e-9);
    }

    /* --- Unified PIDGains --- */

    @Test
    void setPIDGains_setsAllGainsIncludingKGAndKA() {
        PIDGains gains = new PIDGains(1.0, 0.01, 0.05, 0.12, 0.25, 0.15, 0.003);
        controller.setPIDGains(0, gains);

        PIDGains read = controller.getPIDGains(0);
        assertEquals(1.0, read.kP(), 1e-9);
        assertEquals(0.01, read.kI(), 1e-9);
        assertEquals(0.05, read.kD(), 1e-9);
        assertEquals(0.12, read.kV(), 1e-9);
        assertEquals(0.25, read.kS(), 1e-9);
        assertEquals(0.15, read.kG(), 1e-9);
        assertEquals(0.003, read.kA(), 1e-9);
    }

    @Test
    void setPIDGains_differentSlots_independent() {
        controller.setPIDGains(0, new PIDGains(1.0, 0.0, 0.0));
        controller.setPIDGains(1, new PIDGains(2.0, 0.0, 0.0));

        assertEquals(1.0, controller.getPIDGains(0).kP(), 1e-9);
        assertEquals(2.0, controller.getPIDGains(1).kP(), 1e-9);
    }

    /* --- Unified CurrentLimitConfig --- */

    @Test
    void setCurrentLimit_statorOnly() {
        controller.setCurrentLimit(CurrentLimitConfig.statorOnly(40.0));
        assertTrue(controller.getCurrentLimitStatorEnable());
        assertEquals(40.0, controller.getCurrentLimitStator());
    }

    @Test
    void getCurrentLimitConfig_roundTrips() {
        CurrentLimitConfig original = new CurrentLimitConfig(true, 60.0, true, 40.0, 20.0, 1.5);
        controller.setCurrentLimit(original);

        CurrentLimitConfig read = controller.getCurrentLimitConfig();
        assertTrue(read.statorEnabled());
        assertEquals(60.0, read.statorLimit());
        assertEquals(40.0, read.supplyLimit());
        assertEquals(20.0, read.supplyLowerLimit());
        assertEquals(1.5, read.supplyLowerTime());
    }

    @Test
    void setCurrentLimit_timeBased_enablesFlagCorrectly() {
        // Bug fix: when supplyLowerTime > 0, the enable flag must still be set
        CurrentLimitConfig cfg = new CurrentLimitConfig(false, 0, true, 40.0, 20.0, 1.5);
        controller.setCurrentLimit(cfg);

        assertTrue(controller.getCurrentLimitSupplyEnable());
        assertEquals(40.0, controller.getCurrentLimitSupply());
        assertEquals(20.0, controller.getCurrentLimitSupplyLowerLimit());
        assertEquals(1.5, controller.getCurrentLimitSupplyTime());
    }

    /* --- StrictFollower and CoastOut --- */

    @Test
    void coast_doesNotThrow() {
        assertDoesNotThrow(() -> controller.coast());
    }

    /* --- Interface Compliance --- */

    @Test
    void controllerTalonFX_extendsControllerSmart() {
        assertTrue(controller instanceof ControllerSmart);
        assertTrue(controller instanceof ControllerBase);
    }
}
