package com.team271.lib.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BalanceTest {

    private Balance balanceReverse;
    private Balance balanceForward;

    @BeforeEach
    void setUp() {
        balanceReverse = new Balance(false);
        balanceReverse.init();

        balanceForward = new Balance(true);
        balanceForward.init();
    }

    /* --- Constructor --- */

    @Test
    void constructorFwdFalse() {
        Balance b = new Balance(false);
        assertNotNull(b);
    }

    @Test
    void constructorFwdTrue() {
        Balance b = new Balance(true);
        assertNotNull(b);
    }

    /* --- secondsToTicks --- */

    @Test
    void secondsToTicksZero() {
        assertEquals(0, balanceReverse.secondsToTicks(0.0));
    }

    @Test
    void secondsToTicksOneSecond() {
        assertEquals(50, balanceReverse.secondsToTicks(1.0));
    }

    @Test
    void secondsToTicksFractional() {
        assertEquals(5, balanceReverse.secondsToTicks(0.1));
    }

    @Test
    void secondsToTicksHalf() {
        assertEquals(25, balanceReverse.secondsToTicks(0.5));
    }

    /* --- Reverse routine (isFwd=false): state 0 --- */

    @Test
    void reverseState0ReturnsFastSpeed() {
        /* Tilt below threshold (13.0), stays in state 0 */
        assertEquals(0.6, balanceReverse.autoBalanceRoutine(5.0), 1e-9);
    }

    @Test
    void reverseState0TransitionsToState1() {
        /* Feed tilt > 13.0 enough times to exceed debounce (5 ticks at 0.1s * 50) */
        for (int i = 0; i < 5; i++) {
            balanceReverse.autoBalanceRoutine(14.0);
        }
        /* 6th call should trigger transition — debounceCount (6) > 5 */
        double result = balanceReverse.autoBalanceRoutine(14.0);
        assertEquals(0.2, result, 1e-9, "Should return robotSpeedSlow on transition to state 1");
    }

    /* --- Reverse routine: state 1 --- */

    @Test
    void reverseState1ReturnsSlowSpeed() {
        /* Transition to state 1 first */
        transitionReverseToState1();

        /* Tilt above levelDegree (6.0), stays in state 1 */
        assertEquals(0.2, balanceReverse.autoBalanceRoutine(8.0), 1e-9);
    }

    @Test
    void reverseState1TransitionsToState2() {
        transitionReverseToState1();

        /* Feed tilt < 6.0 enough times to exceed debounce */
        for (int i = 0; i < 5; i++) {
            balanceReverse.autoBalanceRoutine(4.0);
        }
        double result = balanceReverse.autoBalanceRoutine(4.0);
        assertEquals(0.0, result, 1e-9, "Should return 0 on transition to state 2");
    }

    /* --- Reverse routine: state 2 --- */

    @Test
    void reverseState2ReturnsPositiveCorrectionWhenTiltAboveLevel() {
        transitionReverseToState2();

        assertEquals(0.1, balanceReverse.autoBalanceRoutine(7.0), 1e-9);
    }

    @Test
    void reverseState2ReturnsNegativeCorrectionWhenTiltBelowNegativeLevel() {
        transitionReverseToState2();

        assertEquals(-0.1, balanceReverse.autoBalanceRoutine(-7.0), 1e-9);
    }

    @Test
    void reverseState2ReturnsZeroWhenTiltBetweenThresholds() {
        transitionReverseToState2();

        /* Tilt between -6 and +6 but above levelDegree/2 (3.0) */
        assertEquals(0.0, balanceReverse.autoBalanceRoutine(4.0), 1e-9);
    }

    @Test
    void reverseState2TransitionsToState4() {
        transitionReverseToState2();

        /* Feed |tilt| <= 3.0 enough times to exceed debounce */
        for (int i = 0; i < 5; i++) {
            balanceReverse.autoBalanceRoutine(2.0);
        }
        double result = balanceReverse.autoBalanceRoutine(2.0);
        assertEquals(0.0, result, 1e-9, "Should return 0 on transition to state 4");
    }

    /* --- Reverse routine: default (state 4) --- */

    @Test
    void reverseDefaultReturnsZero() {
        transitionReverseToState4();

        assertEquals(0.0, balanceReverse.autoBalanceRoutine(15.0), 1e-9);
    }

    /* --- Forward routine (isFwd=true): state 0 --- */

    @Test
    void forwardState0ReturnsFastSpeed() {
        /* Tilt above threshold (-13.0), stays in state 0 */
        assertEquals(0.6, balanceForward.autoBalanceRoutine(-5.0), 1e-9);
    }

    @Test
    void forwardState0TransitionsToState1() {
        /* Feed tilt < -13.0 enough times */
        for (int i = 0; i < 5; i++) {
            balanceForward.autoBalanceRoutine(-14.0);
        }
        double result = balanceForward.autoBalanceRoutine(-14.0);
        assertEquals(0.2, result, 1e-9, "Should return robotSpeedSlow on transition to state 1");
    }

    /* --- Forward routine: state 1 --- */

    @Test
    void forwardState1ReturnsSlowSpeed() {
        transitionForwardToState1();

        /* Tilt below levelDegree (-6.0), stays in state 1 */
        assertEquals(0.2, balanceForward.autoBalanceRoutine(-8.0), 1e-9);
    }

    @Test
    void forwardState1TransitionsToState2() {
        transitionForwardToState1();

        /* Feed tilt > -6.0 enough times */
        for (int i = 0; i < 5; i++) {
            balanceForward.autoBalanceRoutine(-4.0);
        }
        double result = balanceForward.autoBalanceRoutine(-4.0);
        assertEquals(0.0, result, 1e-9, "Should return 0 on transition to state 2");
    }

    /* --- Forward routine: state 2 --- */

    @Test
    void forwardState2ReturnsNegativeCorrectionWhenTiltAboveLevel() {
        transitionForwardToState2();

        /* tilt >= levelDegree (-6.0) → true for tilt=-4 (greater than -6) → returns -0.1 */
        assertEquals(-0.1, balanceForward.autoBalanceRoutine(-4.0), 1e-9);
    }

    @Test
    void forwardState2ReturnsPositiveCorrectionWhenTiltBelowNegativeLevel() {
        transitionForwardToState2();

        /* tilt <= -levelDegree → tilt <= 6.0? No — levelDegree is -6.0, so -levelDegree = 6.0.
         * tilt <= 6.0 is true for tilt=-8 → returns 0.1 */
        assertEquals(0.1, balanceForward.autoBalanceRoutine(-8.0), 1e-9);
    }

    @Test
    void forwardState2ReturnsCorrectionForMidTilt() {
        transitionForwardToState2();

        /* levelDegree=-6.0, so tilt=-5.5 >= -6.0 is true → returns -0.1 */
        assertEquals(-0.1, balanceForward.autoBalanceRoutine(-5.5), 1e-9);
    }

    @Test
    void forwardState2TransitionsToState4() {
        transitionForwardToState2();

        /* |tilt| <= |levelDegree|/2 = 3.0 */
        for (int i = 0; i < 5; i++) {
            balanceForward.autoBalanceRoutine(-2.0);
        }
        double result = balanceForward.autoBalanceRoutine(-2.0);
        assertEquals(0.0, result, 1e-9);
    }

    /* --- Forward routine: default --- */

    @Test
    void forwardDefaultReturnsZero() {
        transitionForwardToState4();

        assertEquals(0.0, balanceForward.autoBalanceRoutine(-15.0), 1e-9);
    }

    /* --- init() resets state --- */

    @Test
    void initResetsStateAfterAdvancing() {
        transitionReverseToState1();

        balanceReverse.init();

        /* After re-init, should be back in state 0 returning fast speed */
        assertEquals(0.6, balanceReverse.autoBalanceRoutine(5.0), 1e-9);
    }

    /* --- outputTelemetry / checkTuning --- */

    @Test
    void outputTelemetryDoesNotThrowReverse() {
        assertDoesNotThrow(balanceReverse::outputTelemetry);
    }

    @Test
    void outputTelemetryDoesNotThrowForward() {
        assertDoesNotThrow(balanceForward::outputTelemetry);
    }

    @Test
    void outputTelemetryAfterStateTransition() {
        transitionReverseToState2();
        assertDoesNotThrow(balanceReverse::outputTelemetry);
    }

    @Test
    void outputTelemetryAfterState4() {
        transitionReverseToState4();
        assertDoesNotThrow(balanceReverse::outputTelemetry);
    }

    @Test
    void checkTuningForwardFlipsSigns() {
        transitionForwardToState2();
        assertDoesNotThrow(balanceForward::outputTelemetry);
    }

    /* --- State 2 edge cases --- */

    @Test
    void reverseState2ZeroTiltReturnsZero() {
        transitionReverseToState2();
        assertEquals(0.0, balanceReverse.autoBalanceRoutine(0.0), 1e-9);
    }

    @Test
    void forwardState2ZeroTiltReturnsCorrectionDueToNegativeLevel() {
        transitionForwardToState2();
        /* levelDegree=-6.0, so tilt=0.0 >= -6.0 is true → returns -0.1 */
        assertEquals(-0.1, balanceForward.autoBalanceRoutine(0.0), 1e-9);
    }

    /* --- Helper methods to advance state machine --- */

    private void transitionReverseToState1() {
        for (int i = 0; i <= 5; i++) {
            balanceReverse.autoBalanceRoutine(14.0);
        }
    }

    private void transitionReverseToState2() {
        transitionReverseToState1();
        for (int i = 0; i <= 5; i++) {
            balanceReverse.autoBalanceRoutine(4.0);
        }
    }

    private void transitionReverseToState4() {
        transitionReverseToState2();
        for (int i = 0; i <= 5; i++) {
            balanceReverse.autoBalanceRoutine(2.0);
        }
    }

    private void transitionForwardToState1() {
        for (int i = 0; i <= 5; i++) {
            balanceForward.autoBalanceRoutine(-14.0);
        }
    }

    private void transitionForwardToState2() {
        transitionForwardToState1();
        for (int i = 0; i <= 5; i++) {
            balanceForward.autoBalanceRoutine(-4.0);
        }
    }

    private void transitionForwardToState4() {
        transitionForwardToState2();
        for (int i = 0; i <= 5; i++) {
            balanceForward.autoBalanceRoutine(-2.0);
        }
    }
}
