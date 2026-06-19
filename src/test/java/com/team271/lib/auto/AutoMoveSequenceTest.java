package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutoMoveSequenceTest {

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
    void startsFirstChildOnStart() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        assertTrue(a.isRunning(), "First child should start");
        assertTrue(a.onStartCalled);
        assertFalse(b.isRunning(), "Second child should NOT start yet");
        assertFalse(b.onStartCalled);
    }

    /* --- Advancement --- */

    @Test
    void advancesToNextOnCompletion() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        a.complete();
        seq.robotPeriodicAfter(0.0);

        assertTrue(b.isRunning(), "Second child should start after first completes");
        assertTrue(b.onStartCalled);
    }

    @Test
    void completesWhenLastChildDone() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        a.complete();
        seq.robotPeriodicAfter(0.0);

        b.complete();
        seq.robotPeriodicAfter(0.0);

        assertTrue(seq.isComplete(), "Sequence should complete when last child finishes");
    }

    @Test
    void doesNotAdvanceBeforeCurrentChildCompletes() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        seq.robotPeriodicAfter(0.0);

        assertTrue(a.isRunning());
        assertFalse(b.isRunning(), "Should not advance when current child is still running");
    }

    /* --- Edge cases --- */

    @Test
    void emptySequenceCompletesImmediately() {
        AutoMoveSequence seq = new AutoMoveSequence();
        seq.start();

        assertTrue(seq.isComplete(), "Empty sequence should complete immediately");
    }

    @Test
    void singleChildSequenceCompletesWhenChildDone() {
        TestMove a = new TestMove();
        AutoMoveSequence seq = new AutoMoveSequence(a);
        seq.start();

        a.complete();
        seq.robotPeriodicAfter(0.0);

        assertTrue(seq.isComplete());
    }

    /* --- Cleanup --- */

    @Test
    void endsCurrentChildOnSequenceEnd() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        // End the sequence while A is still running
        seq.end();

        assertTrue(a.onEndCalled, "Current running child should be ended on sequence end");
    }

    /* --- Delegation --- */

    @Test
    void delegatesAutonomousPeriodicToCurrentChild() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        seq.autonomousPeriodic(0.0);

        assertTrue(a.autoPeriodicCalled, "Current child should receive autonomousPeriodic");
        assertFalse(b.autoPeriodicCalled, "Future child should NOT receive autonomousPeriodic");
    }

    /* --- Integration: nested Sequence(Parallel(A, B), C) --- */

    @Test
    void nestedSequenceWithParallelExecutesCorrectly() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        TestMove c = new TestMove();

        AutoMoveParallel parallel = new AutoMoveParallel(a, b);
        AutoMoveSequence seq = new AutoMoveSequence(parallel, c);
        seq.start();

        // A and B should both be running (inside parallel)
        assertTrue(a.isRunning());
        assertTrue(b.isRunning());
        assertFalse(c.isRunning(), "C should not start until parallel completes");

        // Complete A — parallel not done yet
        a.complete();
        parallel.autonomousPeriodic(0.0);
        seq.robotPeriodicAfter(0.0);
        assertFalse(parallel.isComplete());
        assertFalse(c.isRunning());

        // Complete B — parallel done, C should start
        b.complete();
        parallel.autonomousPeriodic(0.0);
        seq.robotPeriodicAfter(0.0);
        assertTrue(parallel.isComplete());
        assertTrue(c.isRunning(), "C should start after parallel completes");

        // Complete C — sequence done
        c.complete();
        seq.robotPeriodicAfter(0.0);
        assertTrue(seq.isComplete());
    }

    /* --- Three-child sequence --- */

    @Test
    void threeChildSequenceAdvancesInOrder() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        TestMove c = new TestMove();

        AutoMoveSequence seq = new AutoMoveSequence(a, b, c);
        seq.start();

        assertTrue(a.isRunning());
        assertFalse(b.isRunning());
        assertFalse(c.isRunning());

        a.complete();
        seq.robotPeriodicAfter(0.0);
        assertTrue(b.isRunning());
        assertFalse(c.isRunning());

        b.complete();
        seq.robotPeriodicAfter(0.0);
        assertTrue(c.isRunning());

        c.complete();
        seq.robotPeriodicAfter(0.0);
        assertTrue(seq.isComplete());
    }

    /* --- robotPeriodicBefore telemetry coverage --- */

    @Test
    void robotPeriodicBeforePublishesTelemetry() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        assertDoesNotThrow(() -> seq.robotPeriodicBefore(0.0));
    }

    @Test
    void robotPeriodicBeforeSkipsCompletedCurrentChild() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        a.complete();
        seq.robotPeriodicAfter(0.0); // advance to b

        // Now b is current — robotPeriodicBefore should delegate to b only
        assertDoesNotThrow(() -> seq.robotPeriodicBefore(0.0));
        assertTrue(b.isRunning());
    }

    @Test
    void autonomousPeriodicDoesNotDelegateWhenCurrentChildCannotRun() {
        TestMove a = new TestMove();
        AutoMoveSequence seq = new AutoMoveSequence(a);
        seq.start();

        // Complete a — current becomes null after robotPeriodicAfter
        a.complete();
        seq.robotPeriodicAfter(0.0);

        // autonomousPeriodic with no current child should not throw
        assertDoesNotThrow(() -> seq.autonomousPeriodic(0.0));
    }

    @Test
    void robotPeriodicAfterDoesNotAdvanceWhenCurrentStillRunning() {
        TestMove a = new TestMove();
        TestMove b = new TestMove();
        AutoMoveSequence seq = new AutoMoveSequence(a, b);
        seq.start();

        // a is not complete — robotPeriodicAfter should not advance
        seq.robotPeriodicAfter(0.0);
        assertTrue(a.isRunning());
        assertFalse(b.isRunning());
    }
}
