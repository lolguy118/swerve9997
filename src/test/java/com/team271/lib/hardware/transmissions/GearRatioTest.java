package com.team271.lib.hardware.transmissions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GearRatioTest {

    private static final double kEps = 1e-9;

    /* --- Construction and validation --- */

    @Test
    void identityRatioAllOnes() {
        GearRatio gr = GearRatio.IDENTITY;
        assertEquals(1.0, gr.getRotorToMechanism(), kEps);
        assertEquals(1.0, gr.getSensorRelToMechanism(), kEps);
        assertEquals(1.0, gr.getSensorAbsToMechanism(), kEps);
        assertEquals(1.0, gr.getMechanismToUnits(), kEps);
    }

    @Test
    void constructorStoresValues() {
        GearRatio gr = new GearRatio(0.5, 0.25, 0.125, 2.0);
        assertEquals(0.5, gr.getRotorToMechanism(), kEps);
        assertEquals(0.25, gr.getSensorRelToMechanism(), kEps);
        assertEquals(0.125, gr.getSensorAbsToMechanism(), kEps);
        assertEquals(2.0, gr.getMechanismToUnits(), kEps);
    }

    @Test
    void singleArgConstructorSetsAllEqual() {
        GearRatio gr = new GearRatio(3.0);
        assertEquals(3.0, gr.getRotorToMechanism(), kEps);
        assertEquals(3.0, gr.getSensorRelToMechanism(), kEps);
        assertEquals(3.0, gr.getSensorAbsToMechanism(), kEps);
        assertEquals(1.0, gr.getMechanismToUnits(), kEps);
    }

    @Test
    void zeroRotorToMechanismThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(0, 1.0, 1.0, 1.0));
    }

    @Test
    void zeroSensorRelThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(1.0, 0, 1.0, 1.0));
    }

    @Test
    void zeroSensorAbsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(1.0, 1.0, 0, 1.0));
    }

    @Test
    void zeroMechanismToUnitsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(1.0, 1.0, 1.0, 0));
    }

    /* --- Rotor conversions --- */

    @Test
    void rotorToOutputIdentity() {
        assertEquals(5.0, GearRatio.IDENTITY.rotorToOutput(5.0), kEps);
    }

    @Test
    void rotorToOutputWithRatio() {
        // 10:1 gear ratio, 2x mechanism-to-units
        GearRatio gr = new GearRatio(0.1, 1.0, 1.0, 2.0);
        // 100 rotor rotations * 0.1 * 2.0 = 20 output units
        assertEquals(20.0, gr.rotorToOutput(100.0), kEps);
    }

    @Test
    void outputToRotorInvertsRotorToOutput() {
        GearRatio gr = new GearRatio(0.1, 1.0, 1.0, 2.0);
        double outputUnits = 20.0;
        double rotor = gr.outputToRotor(outputUnits);
        assertEquals(outputUnits, gr.rotorToOutput(rotor), kEps);
    }

    /* --- Sensor relative conversions --- */

    @Test
    void sensorRelToOutputWithRatio() {
        GearRatio gr = new GearRatio(1.0, 0.5, 1.0, 3.0);
        // 10 sensor rotations * 0.5 * 3.0 = 15 output units
        assertEquals(15.0, gr.sensorRelToOutput(10.0), kEps);
    }

    @Test
    void outputToSensorRelInvertsSensorRelToOutput() {
        GearRatio gr = new GearRatio(1.0, 0.5, 1.0, 3.0);
        double outputUnits = 15.0;
        double sensor = gr.outputToSensorRel(outputUnits);
        assertEquals(outputUnits, gr.sensorRelToOutput(sensor), kEps);
    }

    /* --- Sensor absolute conversions --- */

    @Test
    void sensorAbsToOutputWithRatio() {
        GearRatio gr = new GearRatio(1.0, 1.0, 0.25, 4.0);
        // 8 sensor rotations * 0.25 * 4.0 = 8 output units
        assertEquals(8.0, gr.sensorAbsToOutput(8.0), kEps);
    }

    /* --- Builder methods --- */

    @Test
    void withRotorToMechanismReturnsNewInstance() {
        GearRatio original = new GearRatio(1.0, 2.0, 3.0, 4.0);
        GearRatio shifted = original.withRotorToMechanism(5.0);

        assertNotSame(original, shifted);
        assertEquals(1.0, original.getRotorToMechanism(), kEps);
        assertEquals(5.0, shifted.getRotorToMechanism(), kEps);
        assertEquals(2.0, shifted.getSensorRelToMechanism(), kEps);
        assertEquals(3.0, shifted.getSensorAbsToMechanism(), kEps);
        assertEquals(4.0, shifted.getMechanismToUnits(), kEps);
    }

    @Test
    void withSensorRelToMechanismReturnsNewInstance() {
        GearRatio original = new GearRatio(1.0, 2.0, 3.0, 4.0);
        GearRatio shifted = original.withSensorRelToMechanism(7.0);

        assertEquals(2.0, original.getSensorRelToMechanism(), kEps);
        assertEquals(7.0, shifted.getSensorRelToMechanism(), kEps);
        assertEquals(1.0, shifted.getRotorToMechanism(), kEps);
    }

    /* --- toString --- */

    @Test
    void toStringContainsValues() {
        GearRatio gr = new GearRatio(1.0, 2.0, 3.0, 4.0);
        String s = gr.toString();
        assertTrue(s.contains("rotor=1.0"));
        assertTrue(s.contains("sensorRel=2.0"));
        assertTrue(s.contains("sensorAbs=3.0"));
        assertTrue(s.contains("mechToUnits=4.0"));
    }
}
