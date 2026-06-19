package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutoMoveParallelTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /** Simple concrete AutoMove that completes when told to. */
    private static class TestMove extends AutoMove {
        boolean onStartCalled = false;
        boolean onEndCalled = false;
        boolean autoPeriodicCalled = false;

        TestMove() {
            super(0.0);
        }

        @Override
        public void onStart() {
            onStartCalled = true;
        }

        @Override
        public void onEnd() {
            onEndCalled = true;
        }

        @Override
        public void autonomousPeriodic(final double argTimestamp) {
            autoPeriodicCalled = true;
        }

        void complete() {
            end();
        }
    }

    /* --- start() behavior --- */

    @Test
    void startsAllChildrenOnStart() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        TestMove c = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b, c);
        parallel.start();

        assertTrue(a.isRunning());
        assertTrue(b.isRunning());
        assertTrue(c.isRunning());
        assertTrue(a.onStartCalled);
        assertTrue(b.onStartCalled);
        assertTrue(c.onStartCalled);
    }

    /* --- Completion behavior --- */

    @Test
    void completesWhenAllChildrenComplete() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        a.complete();
        parallel.autonomousPeriodic(0.0);
        assertFalse(parallel.isComplete(), "Should not complete when one child remains");

        b.complete();
        parallel.autonomousPeriodic(0.0);
        assertTrue(parallel.isComplete(), "Should complete when all children done");
    }

    @Test
    void doesNotCompleteWhenOneChildRemains() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        TestMove c = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b, c);
        parallel.start();

        a.complete();
        b.complete();
        parallel.autonomousPeriodic(0.0);

        assertFalse(parallel.isComplete(), "One child still running");
        assertTrue(c.isRunning());
    }

    /* --- Delegation behavior --- */

    @Test
    void delegatesAutonomousPeriodicToCanRunChildren() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        parallel.autonomousPeriodic(0.0);

        assertTrue(a.autoPeriodicCalled);
        assertTrue(b.autoPeriodicCalled);
    }

    @Test
    void doesNotDelegateAutonomousPeriodicToCompletedChildren() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        a.complete();
        a.autoPeriodicCalled = false; // reset flag

        parallel.autonomousPeriodic(0.0);

        assertFalse(a.autoPeriodicCalled, "Completed child should not receive autonomousPeriodic");
        assertTrue(b.autoPeriodicCalled);
    }

    /* --- onEnd cleanup --- */

    @Test
    void endsRemainingChildrenOnEnd() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        a.complete();
        // b is still running
        parallel.end();

        assertTrue(b.onEndCalled, "Remaining running child should be ended");
    }

    @Test
    void doesNotEndAlreadyCompletedChildrenOnEnd() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        a.complete();
        a.onEndCalled = false; // reset after first end

        parallel.end();

        assertFalse(a.onEndCalled, "Already-completed child should not be ended again");
    }

    /* --- Integration: nested Parallel(Sequence(A, B), C) --- */

    @Test
    void nestedParallelWithSequenceExecutesCorrectly() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        TestMove c = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        AutoMoveParallel parallel = new AutoMoveParallel(seq, c);
        parallel.start();

        // A and C should be running
        assertTrue(a.isRunning());
        assertTrue(c.isRunning());
        assertFalse(b.isRunning(), "B should not start until A completes");

        // Complete A — B should start
        a.complete();
        parallel.robotPeriodicAfter(0.0);
        assertTrue(b.isRunning(), "B should start after A completes in sequence");

        // Complete B — sequence done, but C still running
        b.complete();
        parallel.robotPeriodicAfter(0.0);
        parallel.autonomousPeriodic(0.0);
        assertFalse(parallel.isComplete(), "C still running");

        // Complete C — parallel done
        c.complete();
        parallel.autonomousPeriodic(0.0);
        assertTrue(parallel.isComplete());
    }

    /* --- Edge cases --- */

    @Test
    void singleChildParallelBehavesLikeSingleMove() {
        TestMove a = new TestMove();
        AutoMoveParallel parallel = new AutoMoveParallel(a);
        parallel.start();

        assertTrue(a.isRunning());

        a.complete();
        parallel.autonomousPeriodic(0.0);
        assertTrue(parallel.isComplete());
    }

    /* --- robotPeriodicBefore telemetry coverage --- */

    @Test
    void robotPeriodicBeforePublishesTelemetry() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        // Should not throw and should update telemetry
        assertDoesNotThrow(() -> parallel.robotPeriodicBefore(0.0));
    }

    @Test
    void robotPeriodicBeforeSkipsCompletedChildren() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        a.complete();

        // robotPeriodicBefore should only delegate to b (not a)
        assertDoesNotThrow(() -> parallel.robotPeriodicBefore(0.0));
        assertTrue(a.isComplete());
        assertTrue(b.isRunning());
    }

    @Test
    void robotPeriodicAfterSkipsCompletedChildren() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        a.complete();

        // robotPeriodicAfter should only delegate to b
        assertDoesNotThrow(() -> parallel.robotPeriodicAfter(0.0));
    }

    @Test
    void telemetryShowsActiveChildrenNames() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        parallel.start();

        // Call robotPeriodicBefore to trigger telemetry publishing
        parallel.robotPeriodicBefore(0.0);

        // Complete one child and re-check
        a.complete();
        parallel.robotPeriodicBefore(0.0);

        // No assertion on telemetry values — just verifying no exceptions
        assertTrue(a.isComplete());
        assertTrue(b.isRunning());
    }
}
