package com.team271.lib.hardware.motors;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.motors.MotorBase.MotorControlType;
import com.team271.lib.hardware.motors.MotorBase.MotorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MotorBaseTest {

    @ParameterizedTest
    @EnumSource(MotorType.class)
    void allMotorTypes_constructWithoutException(MotorType type) {
        assertDoesNotThrow(() -> new MotorBase(type));
    }

    @ParameterizedTest
    @EnumSource(MotorType.class)
    void allMotorTypes_returnNonNullMotorName(MotorType type) {
        MotorBase motor = new MotorBase(type);
        assertNotNull(motor.getMotorName());
        assertFalse(motor.getMotorName().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(MotorType.class)
    void allMotorTypes_areBrushless(MotorType type) {
        MotorBase motor = new MotorBase(type);
        assertEquals(MotorControlType.BRUSHLESS, motor.getControlType());
    }

    @ParameterizedTest
    @EnumSource(MotorType.class)
    void allMotorTypes_returnMotorType(MotorType type) {
        MotorBase motor = new MotorBase(type);
        assertEquals(type, motor.getMotorType());
    }

    @Test
    void falcon500_hasPositiveFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.FALCON500);
        assertTrue(motor.getFreeSpeed() > 0);
    }

    @Test
    void krakenX60_hasPositiveFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.KRAKENX60);
        assertTrue(motor.getFreeSpeed() > 0);
    }

    @Test
    void krakenX44_hasPositiveFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.KRAKENX44);
        assertTrue(motor.getFreeSpeed() > 0);
    }

    @Test
    void neo_hasPositiveFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.NEO);
        assertTrue(motor.getFreeSpeed() > 0);
    }

    @Test
    void neo550_hasPositiveFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.NEO550);
        assertTrue(motor.getFreeSpeed() > 0);
    }

    @Test
    void neoVortex_hasPositiveFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.NEO_VORTEX);
        assertTrue(motor.getFreeSpeed() > 0);
    }

    @Test
    void ctreMinion_zeroFreeSpeed() {
        MotorBase motor = new MotorBase(MotorType.CTRE_MINION);
        assertEquals(0.0, motor.getFreeSpeed(), 1e-9);
    }

    @Test
    void controlName_brushless() {
        MotorBase motor = new MotorBase(MotorType.FALCON500);
        assertEquals("Brushless", motor.getControlName());
    }

    @Test
    void motorNames_areCorrect() {
        assertEquals("Falcon 500", new MotorBase(MotorType.FALCON500).getMotorName());
        assertEquals("Kraken X60", new MotorBase(MotorType.KRAKENX60).getMotorName());
        assertEquals("Kraken X44", new MotorBase(MotorType.KRAKENX44).getMotorName());
        assertEquals("CTRE Minion", new MotorBase(MotorType.CTRE_MINION).getMotorName());
        assertEquals("Neo", new MotorBase(MotorType.NEO).getMotorName());
        assertEquals("Neo 550", new MotorBase(MotorType.NEO550).getMotorName());
        assertEquals("Neo Vortex", new MotorBase(MotorType.NEO_VORTEX).getMotorName());
    }

    @ParameterizedTest
    @EnumSource(MotorType.class)
    void getControlName_allTypesReturnBrushless(MotorType type) {
        MotorBase motor = new MotorBase(type);
        assertEquals("Brushless", motor.getControlName());
    }
}
