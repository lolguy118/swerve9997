package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoModeTest {

    /** Concrete subclass for testing the abstract AutoMode. */
    private static class TestAutoMode extends AutoMode {
        TestAutoMode(double argDelay) {
            super(argDelay);
        }

        TestAutoMode() {
            super();
        }
    }

    /** Concrete AutoMove subclass that tracks lifecycle calls. */
    private static class TestMove extends AutoMove {
        boolean started = false;
        int periodicCount = 0;

        TestMove() {
            super();
        }

        @Override
        public void onStart() {
            started = true;
        }

        @Override
        public void autonomousPeriodic(double argTimestamp) {
            periodicCount++;
        }
    }

    private TestAutoMode mode;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        mode = new TestAutoMode();
    }

    /* --- Initial state --- */

    @Test
    void initiallyNotRunning() {
        assertFalse(mode.isRunning());
    }

    @Test
    void initiallyNotComplete() {
        assertFalse(mode.isComplete());
    }

    /* --- addMove --- */

    @Test
    void addMoveDoesNotThrow() {
        assertDoesNotThrow(() -> mode.addMove(new TestMove()));
    }

    /* --- start --- */

    @Test
    void startWithNoMovesDoesNotRun() {
        mode.start();
        assertFalse(mode.isRunning(), "Cannot start mode with no moves");
    }

    @Test
    void startWithMovesBecomesRunning() {
        mode.addMove(new TestMove());
        mode.start();
        assertTrue(mode.isRunning());
    }

    @Test
    void startCallsFirstMoveStart() {
        TestMove first = new TestMove();
        mode.addMove(first);
        mode.start();

        assertTrue(first.started);
        assertTrue(first.isRunning());
    }

    @Test
    void startSetsCurrentMoveIndex() {
        mode.addMove(new TestMove());
        mode.start();
        assertEquals(0, mode.currentMoveIdx);
    }

    /* --- nextMove --- */

    @Test
    void nextMoveAdvancesToSecondMove() {
        TestMove first = new TestMove();
        TestMove second = new TestMove();

        mode.addMove(first);
        mode.addMove(second);
        mode.start();

        mode.nextMove();

        assertEquals(1, mode.currentMoveIdx);
        assertTrue(second.started);
        assertTrue(second.isRunning());
    }

    @Test
    void nextMoveEndsWhenNoMoreMoves() {
        TestMove only = new TestMove();
        mode.addMove(only);
        mode.start();

        mode.nextMove();

        assertTrue(mode.isComplete());
        assertFalse(mode.isRunning());
        assertNull(mode.currentMove);
    }

    /* --- isDelayDone --- */

    @Test
    void isDelayDoneTrueWhenNoDelay() {
        assertTrue(mode.isDelayDone(), "No delay means delay is considered done");
    }

    @Test
    void isDelayDoneTrueWhenDelayVerySmall() {
        TestAutoMode smallDelay = new TestAutoMode(0.005);
        assertTrue(smallDelay.isDelayDone(), "Delay < DELAY_THRESHOLD_SEC treated as no delay");
    }

    @Test
    void isDelayDoneFalseWhenCurrentTimeBelowDelay() {
        TestAutoMode delayed = new TestAutoMode(5.0);
        delayed.currentTime = 2.0;

        assertFalse(delayed.isDelayDone());
    }

    @Test
    void isDelayDoneTrueWhenCurrentTimeAtDelay() {
        TestAutoMode delayed = new TestAutoMode(5.0);
        delayed.currentTime = 5.0;

        assertTrue(delayed.isDelayDone());
    }

    @Test
    void isDelayDoneTrueWhenCurrentTimePastDelay() {
        TestAutoMode delayed = new TestAutoMode(5.0);
        delayed.currentTime = 6.0;

        assertTrue(delayed.isDelayDone());
    }

    /* --- getCurrentMove --- */

    @Test
    void currentMoveNullBeforeStart() {
        assertNull(mode.currentMove);
    }

    @Test
    void currentMoveSetAfterStart() {
        TestMove first = new TestMove();
        mode.addMove(first);
        mode.start();

        assertSame(first, mode.currentMove);
    }

    /* --- end --- */

    @Test
    void endSetsCompleteAndNotRunning() {
        mode.addMove(new TestMove());
        mode.start();

        mode.end();

        assertTrue(mode.isComplete());
        assertFalse(mode.isRunning());
    }

    /* --- robotPeriodicBefore delegates to current move --- */

    @Test
    void robotPeriodicBeforeDoesNothingWhenNotRunning() {
        assertDoesNotThrow(() -> mode.robotPeriodicBefore(0.0));
    }

    @Test
    void robotPeriodicBeforeDelegatesToCurrentMove() {
        TestMove first = new TestMove();
        mode.addMove(first);
        mode.start();

        assertDoesNotThrow(() -> mode.robotPeriodicBefore(0.02));
    }

    /* --- robotPeriodicAfter auto-advances on completion --- */

    @Test
    void robotPeriodicAfterAdvancesWhenMoveComplete() {
        TestMove first = new TestMove();
        TestMove second = new TestMove();
        mode.addMove(first);
        mode.addMove(second);
        mode.start();

        first.setCompleted();

        mode.robotPeriodicAfter(0.0);

        assertSame(second, mode.currentMove);
        assertTrue(second.started);
    }

    @Test
    void robotPeriodicAfterDoesNothingWhenMoveNotComplete() {
        TestMove first = new TestMove();
        TestMove second = new TestMove();
        mode.addMove(first);
        mode.addMove(second);
        mode.start();

        mode.robotPeriodicAfter(0.0);

        assertSame(first, mode.currentMove);
        assertFalse(second.started);
    }

    /* --- autonomousInit calls start --- */

    @Test
    void autonomousInitStartsMode() {
        mode.addMove(new TestMove());
        mode.autonomousInit(0.0);

        assertTrue(mode.isRunning());
    }

    /* --- autonomousPeriodic runs canRun moves --- */

    @Test
    void autonomousPeriodicCallsCanRunMoves() {
        TestMove first = new TestMove();
        mode.addMove(first);
        mode.start();

        mode.autonomousPeriodic(0.0);

        assertEquals(1, first.periodicCount);
    }

    @Test
    void autonomousPeriodicSkipsCompletedMoves() {
        TestMove first = new TestMove();
        mode.addMove(first);
        mode.start();

        first.setCompleted();

        mode.autonomousPeriodic(0.0);

        assertEquals(0, first.periodicCount, "Completed move should not get autonomousPeriodic");
    }

    /* --- autonomousExit calls end --- */

    @Test
    void autonomousExitEndsMode() {
        mode.addMove(new TestMove());
        mode.start();

        mode.autonomousExit(0.0);

        assertTrue(mode.isComplete());
        assertFalse(mode.isRunning());
    }

    /* --- Lifecycle defaults don't throw --- */

    @Test
    void disabledLifecycleDoesNotThrow() {
        assertDoesNotThrow(() -> mode.disabledInit(0.0));
        assertDoesNotThrow(() -> mode.disabledPeriodic(0.0));
        assertDoesNotThrow(() -> mode.disabledExit(0.0));
    }

    /* --- Default constructor uses zero delay --- */

    @Test
    void defaultConstructorZeroDelay() {
        assertEquals(0.0, mode.delay, 1e-9);
    }

    @Test
    void delayConstructorStoresDelay() {
        TestAutoMode delayed = new TestAutoMode(3.0);
        assertEquals(3.0, delayed.delay, 1e-9);
    }

    @Test
    void robotPeriodicAfterDoesNothingWhenNotRunning() {
        TestMove first = new TestMove();
        mode.addMove(first);
        // Don't start - currentMove is null, isRunning is false
        assertDoesNotThrow(() -> mode.robotPeriodicAfter(0.0));
    }

    @Test
    void robotPeriodicBeforeWithNullCurrentMoveWhileRunning() {
        TestMove move = new TestMove();
        mode.addMove(move);
        mode.start();
        mode.nextMove(); // exhausts moves, sets currentMove=null and ends
        // Reset running state to test the null currentMove branch in robotPeriodicBefore
        mode.isRunning = true;
        assertDoesNotThrow(() -> mode.robotPeriodicBefore(0.0));
    }

    @Test
    void autonomousPeriodicWithNullCurrentMove() {
        // currentMove is null when mode has no moves
        assertDoesNotThrow(() -> mode.autonomousPeriodic(0.0));
    }

    @Test
    void setCompletedSetsComplete() {
        assertFalse(mode.isComplete());
        mode.setCompleted();
        assertTrue(mode.isComplete());
    }
}
