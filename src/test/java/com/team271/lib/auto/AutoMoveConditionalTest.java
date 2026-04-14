package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutoMoveConditionalTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /**
     * Subclass that allows injecting fake elapsed time without depending on real wall-clock time.
     * Stores its own copies of condition and timeout since the parent fields are private.
     */
    private static class FakeConditional extends AutoMoveConditional {
        private double fakeElapsed = 0.0;
        private final java.util.function.BooleanSupplier testCondition;
        private final double testTimeout;

        FakeConditional(
                String name, java.util.function.BooleanSupplier condition, double timeoutSec) {
            super(name, condition, timeoutSec);
            this.testCondition = condition;
            this.testTimeout = timeoutSec;
        }

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

    /* --- Constructor --- */

    @Test
    void namedConstructorStoresFields() {
        AtomicBoolean flag = new AtomicBoolean(false);
        AutoMoveConditional m = new AutoMoveConditional("TestName", flag::get, 5.0);

        assertEquals("TestName", m.getName());
        assertFalse(m.isRunning());
        assertFalse(m.isComplete());
    }

    @Test
    void unnamedConstructorUsesClassName() {
        AutoMoveConditional m = new AutoMoveConditional(() -> false, 2.0);

        assertEquals("AutoMoveConditional", m.getName());
    }

    /* --- Completion on condition true --- */

    @Test
    void completesWhenConditionTrue() {
        AtomicBoolean flag = new AtomicBoolean(false);
        FakeConditional m = new FakeConditional("Test", flag::get, 10.0);
        m.start();

        // Condition false — should not complete
        m.setFakeElapsed(0.1);
        m.robotPeriodicBefore(0.0);
        assertTrue(m.isRunning());
        assertFalse(m.isComplete());

        // Condition true — should complete
        flag.set(true);
        m.setFakeElapsed(0.2);
        m.robotPeriodicBefore(0.0);
        assertTrue(m.isComplete());
        assertFalse(m.isRunning());
    }

    /* --- Timeout --- */

    @Test
    void timesOutWhenConditionNeverTrue() {
        FakeConditional m = new FakeConditional("Timeout", () -> false, 2.0);
        m.start();

        // Before timeout
        m.setFakeElapsed(1.5);
        m.robotPeriodicBefore(0.0);
        assertTrue(m.isRunning(), "Should still run before timeout");

        // After timeout
        m.setFakeElapsed(2.5);
        m.robotPeriodicBefore(0.0);
        assertTrue(m.isComplete(), "Should complete on timeout");
        assertFalse(m.isRunning());
    }

    @Test
    void doesNotCompleteBeforeTimeout() {
        FakeConditional m = new FakeConditional(() -> false, 5.0);
        m.start();

        m.setFakeElapsed(3.0);
        m.robotPeriodicBefore(0.0);

        assertTrue(m.isRunning());
        assertFalse(m.isComplete());
    }

    /* --- Condition checked every cycle --- */

    @Test
    void conditionCheckedEveryRobotPeriodicBefore() {
        int[] callCount = {0};
        FakeConditional m =
                new FakeConditional(
                        () -> {
                            callCount[0]++;
                            return false;
                        },
                        10.0);
        m.start();

        m.setFakeElapsed(0.1);
        m.robotPeriodicBefore(0.0);
        m.setFakeElapsed(0.2);
        m.robotPeriodicBefore(0.0);
        m.setFakeElapsed(0.3);
        m.robotPeriodicBefore(0.0);

        assertEquals(3, callCount[0], "Condition should be checked every robotPeriodicBefore call");
    }

    /* --- Edge cases --- */

    @Test
    void doesNotCheckWhenNotRunning() {
        int[] callCount = {0};
        FakeConditional m =
                new FakeConditional(
                        () -> {
                            callCount[0]++;
                            return true;
                        },
                        10.0);

        // Not started — robotPeriodicBefore should not check condition
        m.robotPeriodicBefore(0.0);
        assertEquals(0, callCount[0]);
    }

    @Test
    void conditionTrueOnFirstCycleCompletesImmediately() {
        FakeConditional m = new FakeConditional(() -> true, 10.0);
        m.start();

        m.setFakeElapsed(0.001);
        m.robotPeriodicBefore(0.0);

        assertTrue(m.isComplete());
    }

    @Test
    void zeroDelayMoveCanRunImmediately() {
        FakeConditional m = new FakeConditional(() -> false, 5.0);
        m.start();
        assertTrue(m.canRun(), "Zero-delay conditional should be runnable immediately");
    }
}
