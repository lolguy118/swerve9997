package com.team271.lib.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PIDGainsTest {

    /* --- Constructors --- */

    @Test
    void threeArgConstructor_setsOnlyPID() {
        PIDGains gains = new PIDGains(1.0, 0.01, 0.05);
        assertEquals(1.0, gains.kP());
        assertEquals(0.01, gains.kI());
        assertEquals(0.05, gains.kD());
        assertEquals(0, gains.kV());
        assertEquals(0, gains.kS());
        assertEquals(0, gains.kG());
        assertEquals(0, gains.kA());
    }

    @Test
    void fullConstructor_setsAllGains() {
        PIDGains gains = new PIDGains(1.0, 0.01, 0.05, 0.12, 0.25, 0.15, 0.003);
        assertEquals(1.0, gains.kP());
        assertEquals(0.01, gains.kI());
        assertEquals(0.05, gains.kD());
        assertEquals(0.12, gains.kV());
        assertEquals(0.25, gains.kS());
        assertEquals(0.15, gains.kG());
        assertEquals(0.003, gains.kA());
    }

    /* --- Builder Methods --- */

    @Test
    void withFF_setsVelocityAndStaticFeedforward() {
        PIDGains base = new PIDGains(1.0, 0.0, 0.0);
        PIDGains withFF = base.withFF(0.12, 0.25);

        assertEquals(1.0, withFF.kP());
        assertEquals(0.12, withFF.kV());
        assertEquals(0.25, withFF.kS());
        assertEquals(0, withFF.kG());
        assertEquals(0, withFF.kA());
    }

    @Test
    void withGravity_setsKG() {
        PIDGains base = new PIDGains(1.0, 0.0, 0.0).withFF(0.12, 0.25);
        PIDGains withG = base.withGravity(0.15);

        assertEquals(0.12, withG.kV());
        assertEquals(0.25, withG.kS());
        assertEquals(0.15, withG.kG());
    }

    @Test
    void withAccel_setsKA() {
        PIDGains base = new PIDGains(1.0, 0.0, 0.0);
        PIDGains withA = base.withAccel(0.003);

        assertEquals(0.003, withA.kA());
        assertEquals(0, withA.kV());
    }

    @Test
    void withAllFF_setsAllFeedforwardTerms() {
        PIDGains base = new PIDGains(1.0, 0.0, 0.0);
        PIDGains full = base.withAllFF(0.12, 0.25, 0.15, 0.003);

        assertEquals(1.0, full.kP());
        assertEquals(0.12, full.kV());
        assertEquals(0.25, full.kS());
        assertEquals(0.15, full.kG());
        assertEquals(0.003, full.kA());
    }

    /* --- Record Equality --- */

    @Test
    void equality_sameValues() {
        PIDGains a = new PIDGains(1.0, 0.01, 0.05, 0.12, 0.25, 0.15, 0.003);
        PIDGains b = new PIDGains(1.0, 0.01, 0.05, 0.12, 0.25, 0.15, 0.003);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equality_differentValues() {
        PIDGains a = new PIDGains(1.0, 0.0, 0.0);
        PIDGains b = new PIDGains(2.0, 0.0, 0.0);
        assertNotEquals(a, b);
    }

    /* --- Builder immutability --- */

    @Test
    void builders_doNotMutateOriginal() {
        PIDGains original = new PIDGains(1.0, 0.0, 0.0);
        PIDGains modified = original.withFF(0.5, 0.1);

        assertEquals(0, original.kV());
        assertEquals(0.5, modified.kV());
    }
}
