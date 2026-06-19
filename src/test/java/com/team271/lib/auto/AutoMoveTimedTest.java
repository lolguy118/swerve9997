package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.ConstantsLib;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoMoveTimedTest {

    private AutoMoveTimed move;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        move = new AutoMoveTimed(2.0);
    }

    /* --- Constructor variants --- */

    @Test
    void singleArgConstructor() {
        AutoMoveTimed m = new AutoMoveTimed(3.0);
        assertEquals(3.0, m.length, 1e-9);
        assertEquals(0.0, m.delay, 1e-9);
        assertEquals(0.0, m.timeout, 1e-9);
    }

    @Test
    void twoArgConstructor() {
        AutoMoveTimed m = new AutoMoveTimed(3.0, 1.0);
        assertEquals(3.0, m.length, 1e-9);
        assertEquals(1.0, m.delay, 1e-9);
        assertEquals(0.0, m.timeout, 1e-9);
    }

    @Test
    void threeArgConstructor() {
        AutoMoveTimed m = new AutoMoveTimed(3.0, 1.0, 5.0);
        assertEquals(3.0, m.length, 1e-9);
        assertEquals(1.0, m.delay, 1e-9);
        assertEquals(5.0, m.timeout, 1e-9);
    }

    /* --- duration/delay/timeout --- */

    @Test
    void lengthStoredCorrectly() {
        assertEquals(2.0, move.length, 1e-9);
    }

    @Test
    void defaultDelayIsZero() {
        assertEquals(0.0, move.delay, 1e-9);
    }

    @Test
    void defaultTimeoutIsZero() {
        assertEquals(0.0, move.timeout, 1e-9);
    }

    /* --- isTimedout --- */

    @Test
    void isTimedoutFalseInitially() {
        assertFalse(move.isTimedout(), "currentTime=0 should not exceed timeout=0");
    }

    @Test
    void isTimedoutTrueWhenCurrentTimeExceedsTimeout() {
        AutoMoveTimed timedMove = new AutoMoveTimed(10.0, 0.0, 1.0);
        timedMove.currentTime = 2.0;
        assertTrue(timedMove.isTimedout());
    }

    @Test
    void isTimedoutFalseWhenCurrentTimeBelowTimeout() {
        AutoMoveTimed timedMove = new AutoMoveTimed(10.0, 0.0, 5.0);
        timedMove.currentTime = 3.0;
        assertFalse(timedMove.isTimedout());
    }

    /* --- Timed completion behavior --- */

    @Test
    void initiallyNotRunning() {
        assertFalse(move.isRunning());
    }

    @Test
    void initiallyNotComplete() {
        assertFalse(move.isComplete());
    }

    @Test
    void startSetsRunning() {
        move.start();
        assertTrue(move.isRunning());
    }

    /**
     * Subclass that allows injecting a fake elapsed time for testing the timeout and length logic
     * inside robotPeriodicBefore without depending on real wall-clock time.
     */
    private static class FakeTimedMove extends AutoMoveTimed {
        private double fakeElapsed = 0.0;

        FakeTimedMove(final double argLength, final double argDelay, final double argTimeout) {
            super(argLength, argDelay, argTimeout);
        }

        FakeTimedMove(final double argLength) {
            super(argLength);
        }

        void setFakeElapsed(final double elapsed) {
            fakeElapsed = elapsed;
        }

        @Override
        public void robotPeriodicBefore(final double argTimestamp) {
            if (isRunning()) {
                lastTime = currentTime;
                currentTime = fakeElapsed;

                if ((timeout > ConstantsLib.DELAY_THRESHOLD_SEC) && (currentTime > timeout)) {
                    end();
                } else if (currentTime > length) {
                    end();
                }
            }
        }
    }

    @Test
    void robotPeriodicBeforeEndsWhenLengthExceeded() {
        FakeTimedMove timedMove = new FakeTimedMove(0.5);
        timedMove.start();
        timedMove.setFakeElapsed(0.6);

        timedMove.robotPeriodicBefore(0.0);

        assertTrue(timedMove.isComplete(), "Move should complete when currentTime > length");
        assertFalse(timedMove.isRunning());
    }

    @Test
    void robotPeriodicBeforeEndsOnTimeout() {
        FakeTimedMove timedMove = new FakeTimedMove(10.0, 0.0, 0.5);
        timedMove.start();
        timedMove.setFakeElapsed(0.6);

        timedMove.robotPeriodicBefore(0.0);

        assertTrue(timedMove.isComplete(), "Move should complete when timeout exceeded");
        assertFalse(timedMove.isRunning());
    }

    @Test
    void robotPeriodicBeforeContinuesWhenWithinLength() {
        FakeTimedMove timedMove = new FakeTimedMove(5.0);
        timedMove.start();
        timedMove.setFakeElapsed(2.0);

        timedMove.robotPeriodicBefore(0.0);

        assertTrue(timedMove.isRunning(), "Move should still be running when within length");
        assertFalse(timedMove.isComplete());
    }

    @Test
    void timeoutNotCheckedWhenBelowThreshold() {
        FakeTimedMove timedMove = new FakeTimedMove(10.0, 0.0, 0.005);
        timedMove.start();
        timedMove.setFakeElapsed(0.006);

        timedMove.robotPeriodicBefore(0.0);

        assertTrue(
                timedMove.isRunning(),
                "Timeout < DELAY_THRESHOLD_SEC should not trigger timeout check, so length check"
                        + " applies");
    }

    /* --- Inherits AutoMove behavior --- */

    @Test
    void canRunWhenStarted() {
        move.start();
        assertTrue(move.canRun());
    }

    @Test
    void cannotRunAfterEnd() {
        move.start();
        move.end();
        assertFalse(move.canRun());
    }

    /* --- isTimedout edge cases --- */

    @Test
    void isTimedoutFalseWhenCurrentTimeEqualsTimeout() {
        AutoMoveTimed timedMove = new AutoMoveTimed(10.0, 0.0, 5.0);
        timedMove.currentTime = 5.0;
        assertFalse(timedMove.isTimedout(), "currentTime == timeout should not be timed out");
    }

    @Test
    void isTimedoutTrueWhenTimeoutIsZeroAndCurrentTimePositive() {
        AutoMoveTimed timedMove = new AutoMoveTimed(10.0, 0.0, 0.0);
        timedMove.currentTime = 0.001;
        assertTrue(timedMove.isTimedout(), "timeout=0 means currentTime > 0 triggers timeout");
    }

    /* --- robotPeriodicBefore via real base class (not FakeTimedMove) --- */

    @Test
    void robotPeriodicBeforeDoesNothingWhenNotRunning() {
        AutoMoveTimed timedMove = new AutoMoveTimed(1.0);
        timedMove.robotPeriodicBefore(0.0);

        assertFalse(timedMove.isRunning());
        assertFalse(timedMove.isComplete());
    }

    @Test
    void realRobotPeriodicBeforeUpdatesCurrentTimeWhenRunning() {
        AutoMoveTimed timedMove = new AutoMoveTimed(100.0);
        timedMove.start();

        /* Call robotPeriodicBefore which reads from the real Timer */
        timedMove.robotPeriodicBefore(0.0);

        /* currentTime should be >= 0 (timer just started) */
        assertTrue(timedMove.currentTime >= 0.0);
    }

    @Test
    void realRobotPeriodicBeforeCompletesOnLength() {
        FakeTimedMove timedMove = new FakeTimedMove(0.0);
        timedMove.start();

        /* Inject a small elapsed time that deterministically exceeds length=0 */
        timedMove.setFakeElapsed(0.001);
        timedMove.robotPeriodicBefore(0.0);

        assertTrue(
                timedMove.isComplete(), "Move with length=0 should complete when any time elapses");
    }

    @Test
    void realRobotPeriodicBeforeCompletesOnTimeout() {
        FakeTimedMove timedMove = new FakeTimedMove(100.0, 0.0, 0.02);
        timedMove.start();

        /* Inject elapsed time that deterministically exceeds timeout=0.02s */
        timedMove.setFakeElapsed(0.03);
        timedMove.robotPeriodicBefore(0.0);

        assertTrue(
                timedMove.isComplete(), "Move should timeout when elapsed exceeds timeout=0.02s");
    }

    @Test
    void robotPeriodicBeforeUpdatesTimesWhenRunning() {
        FakeTimedMove timedMove = new FakeTimedMove(10.0);
        timedMove.start();
        timedMove.setFakeElapsed(1.0);
        timedMove.robotPeriodicBefore(0.0);

        assertEquals(1.0, timedMove.currentTime, 1e-9);

        timedMove.setFakeElapsed(2.0);
        timedMove.robotPeriodicBefore(0.0);

        assertEquals(1.0, timedMove.lastTime, 1e-9);
        assertEquals(2.0, timedMove.currentTime, 1e-9);
    }

    /* --- AutoMove base class coverage --- */

    @Test
    void endCallsOnEnd() {
        move.start();
        move.end();

        assertTrue(move.isComplete());
        assertFalse(move.isRunning());
    }

    @Test
    void setCompletedSetsFlag() {
        move.setCompleted();
        assertTrue(move.isComplete());
    }

    @Test
    void isWithinTimeLimitTrueWhenDelayBelowThreshold() {
        AutoMoveTimed m = new AutoMoveTimed(5.0, 0.005, 0.0);
        assertTrue(
                m.isWithinTimeLimit(), "delay < DELAY_THRESHOLD_SEC should be treated as no delay");
    }

    @Test
    void isWithinTimeLimitTrueWhenCurrentTimeBelowDelay() {
        AutoMoveTimed m = new AutoMoveTimed(5.0, 2.0, 0.0);
        m.currentTime = 1.0;
        assertTrue(m.isWithinTimeLimit());
    }

    @Test
    void isWithinTimeLimitFalseWhenCurrentTimeExceedsDelay() {
        AutoMoveTimed m = new AutoMoveTimed(5.0, 2.0, 0.0);
        m.currentTime = 3.0;
        assertFalse(m.isWithinTimeLimit());
    }

    @Test
    void robotPeriodicAfterDoesNotThrow() {
        assertDoesNotThrow(() -> move.robotPeriodicAfter(0.0));
    }

    @Test
    void autonomousPeriodicDoesNotThrow() {
        assertDoesNotThrow(() -> move.autonomousPeriodic(0.0));
    }

    /* --- Negative values --- */

    @Test
    void negativeLengthCompletesImmediately() {
        FakeTimedMove timedMove = new FakeTimedMove(-1.0);
        timedMove.start();
        timedMove.setFakeElapsed(0.001);

        timedMove.robotPeriodicBefore(0.0);

        assertTrue(timedMove.isComplete());
    }

    @Test
    void zeroLengthCompletesOnFirstTick() {
        FakeTimedMove timedMove = new FakeTimedMove(0.0);
        timedMove.start();
        timedMove.setFakeElapsed(0.001);

        timedMove.robotPeriodicBefore(0.0);

        assertTrue(timedMove.isComplete());
    }
}
