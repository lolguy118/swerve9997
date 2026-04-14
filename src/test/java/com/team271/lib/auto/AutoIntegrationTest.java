package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for complex nested auto move compositions. Verifies that Parallel, Sequence,
 * Conditional, and Timed moves interact correctly when deeply nested.
 */
class AutoIntegrationTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /** Simple concrete AutoMove that completes when told to. */
    private static class TestMove extends AutoMove {
        boolean started = false;
        boolean ended = false;

        TestMove() {
            super(0.0);
        }

        @Override
        public void onStart() {
            started = true;
        }

        @Override
        public void onEnd() {
            ended = true;
        }

        void complete() {
            end();
        }
    }

    /**
     * FakeTimedMove with injectable elapsed time, adapted from AutoMoveTimedTest. Avoids dependency
     * on real wall-clock time.
     */
    private static class FakeTimedMove extends AutoMoveTimed {
        private double fakeElapsed = 0.0;

        FakeTimedMove(double argLength) {
            super(argLength);
        }

        void setFakeElapsed(double elapsed) {
            fakeElapsed = elapsed;
        }

        @Override
        public void robotPeriodicBefore(double argTimestamp) {
            if (isRunning()) {
                lastTime = currentTime;
                currentTime = fakeElapsed;

                if (currentTime > length) {
                    end();
                }
            }
        }
    }

    /**
     * FakeConditional with injectable elapsed time and controllable condition. Stores local copies
     * since parent fields are private.
     */
    private static class FakeConditional extends AutoMoveConditional {
        private double fakeElapsed = 0.0;
        private final java.util.function.BooleanSupplier testCondition;
        private final double testTimeout;

        FakeConditional(java.util.function.BooleanSupplier condition, double timeoutSec) {
            super(condition, timeoutSec);
            this.testCondition = condition;
            this.testTimeout = timeoutSec;
        }

        void setFakeElapsed(double elapsed) {
            fakeElapsed = elapsed;
        }

        @Override
        public void robotPeriodicBefore(final double argTimestamp) {
            if (isRunning()) {
                lastTime = currentTime;
                currentTime = fakeElapsed;

                if (testCondition.getAsBoolean()) {
                    end();
                } else if (currentTime > testTimeout) {
                    end();
                }
            }
        }
    }

    /* --- Deeply nested composition --- */

    @Test
    void nestedParallelSequenceExecutesCorrectly() {
        // Structure: Parallel(Sequence(timedA, testB), testC)
        FakeTimedMove timedA = new FakeTimedMove(1.0);
        TestMove testB = new TestMove();
        TestMove testC = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(timedA, testB);
        AutoMoveParallel parallel = new AutoMoveParallel(seq, testC);
        parallel.start();

        // timedA and testC should be running
        assertTrue(timedA.isRunning());
        assertTrue(testC.isRunning());
        assertFalse(testB.started, "testB should not start until timedA completes");

        // timedA completes
        timedA.setFakeElapsed(1.5);
        timedA.robotPeriodicBefore(0.0);
        assertTrue(timedA.isComplete());
        seq.robotPeriodicAfter(0.0);
        assertTrue(testB.isRunning(), "testB should start after timedA in sequence");

        // testB completes — sequence done
        testB.complete();
        seq.robotPeriodicAfter(0.0);
        assertTrue(seq.isComplete());

        // testC still running — parallel not done
        parallel.autonomousPeriodic(0.0);
        assertFalse(parallel.isComplete());

        // testC completes — parallel done
        testC.complete();
        parallel.autonomousPeriodic(0.0);
        assertTrue(parallel.isComplete());
    }

    /* --- Conditional timeout doesn't block sequence --- */

    @Test
    void sequenceAdvancesAfterConditionalTimeout() {
        // Structure: Sequence(Conditional(always-false, 0.5), testMove)
        FakeConditional conditional = new FakeConditional(() -> false, 0.5);
        TestMove afterConditional = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(conditional, afterConditional);
        seq.start();

        assertTrue(conditional.isRunning());
        assertFalse(afterConditional.started);

        // Advance past timeout
        conditional.setFakeElapsed(0.6);
        conditional.robotPeriodicBefore(0.0);

        assertTrue(conditional.isComplete(), "Conditional should timeout");

        // Sequence should advance
        seq.robotPeriodicAfter(0.0);
        assertTrue(
                afterConditional.isRunning(), "Next move should start after conditional times out");

        // Complete the second move
        afterConditional.complete();
        seq.robotPeriodicAfter(0.0);
        assertTrue(seq.isComplete());
    }

    /* --- Three-level nesting --- */

    @Test
    void threeLevelNestingWorksCorrectly() {
        // Structure: Sequence(Parallel(A, B), Sequence(C, D))
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        TestMove c = new TestMove();
        TestMove d = new TestMove();

        AutoMoveParallel p = new AutoMoveParallel(a, b);
        AutoMoveSequence innerSeq = new AutoMoveSequence(c, d);
        AutoMoveSequence outerSeq = new AutoMoveSequence(p, innerSeq);

        outerSeq.start();

        // A and B should be running (inside parallel, inside outer sequence)
        assertTrue(a.isRunning());
        assertTrue(b.isRunning());
        assertFalse(c.started);
        assertFalse(d.started);

        // Complete A and B
        a.complete();
        b.complete();
        p.autonomousPeriodic(0.0);
        outerSeq.robotPeriodicAfter(0.0);

        assertTrue(p.isComplete());
        assertTrue(c.isRunning(), "Inner sequence should start");
        assertFalse(d.started);

        // Complete C
        c.complete();
        innerSeq.robotPeriodicAfter(0.0);
        assertTrue(d.isRunning());

        // Complete D
        d.complete();
        innerSeq.robotPeriodicAfter(0.0);
        outerSeq.robotPeriodicAfter(0.0);

        assertTrue(innerSeq.isComplete());
        assertTrue(outerSeq.isComplete());
    }
}
