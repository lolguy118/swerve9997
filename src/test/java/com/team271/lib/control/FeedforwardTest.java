package com.team271.lib.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FeedforwardTest {

    /* --- Zero --- */

    @Test
    void zero_alwaysReturnsZero() {
        Feedforward ff = Feedforward.zero();
        assertEquals(0, ff.calculate(100, 50));
        assertEquals(0, ff.calculate(0, 0));
        assertEquals(0, ff.calculate(-10, -5));
    }

    /* --- Simple (kS, kV) --- */

    @Test
    void simple_twoArg_posVel() {
        Feedforward ff = Feedforward.simple(0.1, 0.05);
        // kS * sign(10) + kV * 10 = 0.1 * 1 + 0.05 * 10 = 0.6
        assertEquals(0.6, ff.calculate(10, 0), 1e-9);
    }

    @Test
    void simple_twoArg_negVel() {
        Feedforward ff = Feedforward.simple(0.1, 0.05);
        // kS * sign(-10) + kV * (-10) = 0.1 * (-1) + 0.05 * (-10) = -0.6
        assertEquals(-0.6, ff.calculate(-10, 0), 1e-9);
    }

    @Test
    void simple_twoArg_zeroVel() {
        Feedforward ff = Feedforward.simple(0.1, 0.05);
        // kS * sign(0) + kV * 0 = 0
        assertEquals(0, ff.calculate(0, 0), 1e-9);
    }

    /* --- Simple (kS, kV, kA) --- */

    @Test
    void simple_threeArg_withAccel() {
        Feedforward ff = Feedforward.simple(0.1, 0.05, 0.01);
        // 0.1*1 + 0.05*10 + 0.01*5 = 0.1 + 0.5 + 0.05 = 0.65
        assertEquals(0.65, ff.calculate(10, 5), 1e-9);
    }

    /* --- Elevator --- */

    @Test
    void elevator_addsConstantGravity() {
        Feedforward ff = Feedforward.elevator(0.1, 0.3, 0.05, 0.01);
        // 0.1*1 + 0.3 + 0.05*10 + 0.01*0 = 0.1 + 0.3 + 0.5 = 0.9
        assertEquals(0.9, ff.calculate(10, 0), 1e-9);
    }

    @Test
    void elevator_gravityAtZeroVelocity() {
        Feedforward ff = Feedforward.elevator(0.1, 0.3, 0.05, 0.01);
        // 0.1*0 + 0.3 + 0.05*0 + 0.01*0 = 0.3 (gravity only)
        assertEquals(0.3, ff.calculate(0, 0), 1e-9);
    }

    /* --- ArmFeedforward --- */

    @Test
    void arm_horizontal_maxGravity() {
        ArmFeedforward ff = new ArmFeedforward(0.1, 0.3, 0.05, 0.01);
        // At 0 rad (horizontal): cos(0) = 1
        // 0.1*1 + 0.3*1 + 0.05*10 + 0.01*0 = 0.1 + 0.3 + 0.5 = 0.9
        assertEquals(0.9, ff.calculate(0, 10, 0), 1e-9);
    }

    @Test
    void arm_vertical_zeroGravity() {
        ArmFeedforward ff = new ArmFeedforward(0.1, 0.3, 0.05, 0.01);
        // At pi/2 rad (vertical): cos(pi/2) ≈ 0
        // 0.1*1 + 0.3*0 + 0.05*10 + 0.01*0 ≈ 0.1 + 0 + 0.5 = 0.6
        assertEquals(0.6, ff.calculate(Math.PI / 2, 10, 0), 1e-6);
    }

    @Test
    void arm_45degrees() {
        ArmFeedforward ff = new ArmFeedforward(0, 1.0, 0, 0);
        // Pure gravity test: cos(pi/4) ≈ 0.7071
        assertEquals(Math.cos(Math.PI / 4), ff.calculate(Math.PI / 4, 0, 0), 1e-9);
    }

    @Test
    void arm_getters() {
        ArmFeedforward ff = new ArmFeedforward(0.1, 0.3, 0.05, 0.01);
        assertEquals(0.1, ff.getKS());
        assertEquals(0.3, ff.getKG());
        assertEquals(0.05, ff.getKV());
        assertEquals(0.01, ff.getKA());
    }
}
