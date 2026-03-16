package com.team271.lib.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Twist2dTest {

    private static final double kEps = 1e-9;

    /* --- Constructor --- */

    @Test
    void constructorSetsFields() {
        Twist2d t = new Twist2d(1.0, 2.0, 3.0);

        assertEquals(1.0, t.dx, kEps);
        assertEquals(2.0, t.dy, kEps);
        assertEquals(3.0, t.dtheta, kEps);
    }

    /* --- identity --- */

    @Test
    void identityIsAllZero() {
        Twist2d id = Twist2d.identity();

        assertEquals(0.0, id.dx, kEps);
        assertEquals(0.0, id.dy, kEps);
        assertEquals(0.0, id.dtheta, kEps);
    }

    /* --- scaled --- */

    @Test
    void scaledMultipliesAllFields() {
        Twist2d t = new Twist2d(1.0, 2.0, 0.5);
        Twist2d s = t.scaled(3.0);

        assertEquals(3.0, s.dx, kEps);
        assertEquals(6.0, s.dy, kEps);
        assertEquals(1.5, s.dtheta, kEps);
    }

    /* --- norm --- */

    @Test
    void normDyZeroUsesAbsDx() {
        Twist2d t = new Twist2d(-5.0, 0.0, 1.0);

        assertEquals(5.0, t.norm(), kEps);
    }

    @Test
    void normGeneralCase() {
        Twist2d t = new Twist2d(3.0, 4.0, 0.0);

        assertEquals(5.0, t.norm(), kEps);
    }

    /* --- curvature --- */

    @Test
    void curvatureStraightLine() {
        Twist2d t = new Twist2d(5.0, 0.0, 0.0);

        assertEquals(0.0, t.curvature(), kEps);
    }

    @Test
    void curvatureCurved() {
        Twist2d t = new Twist2d(1.0, 0.0, 0.5);

        assertEquals(0.5, t.curvature(), kEps);
    }

    @Test
    void curvatureZeroNormAndZeroTheta() {
        Twist2d t = new Twist2d(0.0, 0.0, 0.0);

        assertEquals(0.0, t.curvature(), kEps);
    }

    /* --- toString --- */

    @Test
    void toStringContainsDeg() {
        Twist2d t = new Twist2d(1.0, 2.0, Math.PI);
        String s = t.toString();

        assertNotNull(s);
        assertTrue(s.contains("deg"));
    }
}
