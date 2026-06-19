package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoMoveTest {

    /** Concrete subclass for testing the abstract AutoMove. */
    private static class TestAutoMove extends AutoMove {
        boolean onStartCalled = false;
        boolean onEndCalled = false;

        TestAutoMove(final double argDelay) {
            super(argDelay);
        }

        TestAutoMove() {
            super();
        }

        @Override
        public void onStart() {
            onStartCalled = true;
        }

        @Override
        public void onEnd() {
            onEndCalled = true;
        }
    }

    private TestAutoMove move;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        move = new TestAutoMove();
    }

    /* --- Initial state --- */

    @Test
    void initiallyNotRunning() {
        assertFalse(move.isRunning());
    }

    @Test
    void initiallyNotComplete() {
        assertFalse(move.isComplete());
    }

    /* --- start/end lifecycle --- */

    @Test
    void startSetsRunning() {
        move.start();
        assertTrue(move.isRunning());
    }

    @Test
    void startCallsOnStart() {
        move.start();
        assertTrue(move.onStartCalled);
    }

    @Test
    void endSetsNotRunning() {
        move.start();
        move.end();
        assertFalse(move.isRunning());
    }

    @Test
    void endCallsOnEnd() {
        move.start();
        move.end();
        assertTrue(move.onEndCalled);
    }

    @Test
    void endSetsComplete() {
        move.start();
        move.end();
        assertTrue(move.isComplete());
    }

    @Test
    void setCompletedMarksComplete() {
        move.setCompleted();
        assertTrue(move.isComplete());
    }

    /* --- canRun logic --- */

    @Test
    void canRunFalseWhenNotStarted() {
        assertFalse(move.canRun());
    }

    @Test
    void canRunTrueWhenRunningAndNotComplete() {
        move.start();
        assertTrue(move.canRun());
    }

    @Test
    void canRunFalseWhenComplete() {
        move.start();
        move.end();
        assertFalse(move.canRun());
    }

    /* --- isWithinTimeLimit --- */

    @Test
    void isWithinTimeLimitTrueWhenNoDelay() {
        assertTrue(move.isWithinTimeLimit(), "No delay (0.0) means always within time limit");
    }

    @Test
    void isWithinTimeLimitTrueWhenVerySmallDelay() {
        TestAutoMove smallDelay = new TestAutoMove(0.005);
        assertTrue(
                smallDelay.isWithinTimeLimit(),
                "Delay < DELAY_THRESHOLD_SEC treated as no delay, always within time limit");
    }

    @Test
    void isWithinTimeLimitTrueWhenCurrentTimeLessThanDelay() {
        TestAutoMove timedMove = new TestAutoMove(5.0);
        assertTrue(timedMove.isWithinTimeLimit(), "currentTime=0 is within delay=5.0");
    }

    /* --- robotPeriodicBefore updates timestamps --- */

    @Test
    void robotPeriodicBeforeDoesNothingWhenNotRunning() {
        double initialCurrentTime = move.currentTime;
        double initialLastTime = move.lastTime;

        move.robotPeriodicBefore(0.0);

        assertEquals(initialCurrentTime, move.currentTime);
        assertEquals(initialLastTime, move.lastTime);
    }

    @Test
    void robotPeriodicBeforeUpdatesTimesWhenRunning() {
        move.start();

        move.robotPeriodicBefore(0.02);

        assertEquals(0.0, move.lastTime, 1e-9);
    }

    /* --- autonomousPeriodic --- */

    @Test
    void autonomousPeriodicDoesNotThrow() {
        move.start();
        assertDoesNotThrow(() -> move.autonomousPeriodic(0.0));
    }

    /* --- robotPeriodicAfter --- */

    @Test
    void robotPeriodicAfterDoesNotThrow() {
        move.start();
        assertDoesNotThrow(() -> move.robotPeriodicAfter(0.0));
    }

    /* --- Default constructor uses zero delay --- */

    @Test
    void defaultConstructorZeroDelay() {
        assertEquals(0.0, move.delay, 1e-9);
    }

    @Test
    void delayConstructorStoresDelay() {
        TestAutoMove delayed = new TestAutoMove(2.5);
        assertEquals(2.5, delayed.delay, 1e-9);
    }
}
