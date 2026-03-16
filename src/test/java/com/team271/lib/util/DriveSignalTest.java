package com.team271.lib.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DriveSignalTest {

    private static final double kEps = 1e-9;

    /* --- Constructors --- */

    @Test
    void twoArgConstructorDefaultsBrakeFalse() {
        DriveSignal ds = new DriveSignal(0.5, -0.5);

        assertEquals(0.5, ds.getLeft(), kEps);
        assertEquals(-0.5, ds.getRight(), kEps);
        assertFalse(ds.getBrakeMode());
    }

    @Test
    void threeArgConstructor() {
        DriveSignal ds = new DriveSignal(1.0, -1.0, true);

        assertEquals(1.0, ds.getLeft(), kEps);
        assertEquals(-1.0, ds.getRight(), kEps);
        assertTrue(ds.getBrakeMode());
    }

    /* --- Getters --- */

    @Test
    void gettersReturnConstructorValues() {
        DriveSignal ds = new DriveSignal(0.3, 0.7, true);

        assertEquals(0.3, ds.getLeft(), kEps);
        assertEquals(0.7, ds.getRight(), kEps);
        assertTrue(ds.getBrakeMode());
    }

    /* --- NEUTRAL constant --- */

    @Test
    void neutralIsZeroNoBrake() {
        assertEquals(0.0, DriveSignal.NEUTRAL.getLeft(), kEps);
        assertEquals(0.0, DriveSignal.NEUTRAL.getRight(), kEps);
        assertFalse(DriveSignal.NEUTRAL.getBrakeMode());
    }

    /* --- BRAKE constant --- */

    @Test
    void brakeIsZeroWithBrake() {
        assertEquals(0.0, DriveSignal.BRAKE.getLeft(), kEps);
        assertEquals(0.0, DriveSignal.BRAKE.getRight(), kEps);
        assertTrue(DriveSignal.BRAKE.getBrakeMode());
    }

    /* --- normalize --- */

    @Test
    void normalizeWithinRangeNoChange() {
        DriveSignal ds = new DriveSignal(0.5, -0.8);
        DriveSignal normalized = ds.normalize();

        assertEquals(0.5, normalized.getLeft(), kEps);
        assertEquals(-0.8, normalized.getRight(), kEps);
    }

    @Test
    void normalizeExceedingRangeScalesDown() {
        DriveSignal ds = new DriveSignal(2.0, 1.0);
        DriveSignal normalized = ds.normalize();

        assertEquals(1.0, normalized.getLeft(), kEps);
        assertEquals(0.5, normalized.getRight(), kEps);
    }

    @Test
    void normalizeNegativeExceedingRange() {
        DriveSignal ds = new DriveSignal(-1.5, 0.75);
        DriveSignal normalized = ds.normalize();

        assertEquals(-1.0, normalized.getLeft(), kEps);
        assertEquals(0.5, normalized.getRight(), kEps);
    }

    /* --- toString --- */

    @Test
    void toStringNoBrake() {
        DriveSignal ds = new DriveSignal(0.5, -0.5);
        String s = ds.toString();

        assertTrue(s.contains("L:"));
        assertTrue(s.contains("R:"));
        assertFalse(s.contains("BRAKE"));
    }

    @Test
    void toStringWithBrake() {
        DriveSignal ds = new DriveSignal(0.0, 0.0, true);
        String s = ds.toString();

        assertTrue(s.contains("BRAKE"));
    }
}
