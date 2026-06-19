package com.team271.lib.subsystem;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubsystemManagerTest {

    /** Concrete Subsystem for testing. */
    private static class TestSubsystem extends Subsystem {
        boolean robotInitCalled = false;
        boolean robotPeriodicBeforeCalled = false;
        boolean robotPeriodicAfterCalled = false;
        boolean disabledInitCalled = false;
        boolean disabledPeriodicCalled = false;
        boolean disabledExitCalled = false;
        boolean autonomousInitCalled = false;
        boolean autonomousPeriodicCalled = false;
        boolean autonomousExitCalled = false;
        boolean teleopInitCalled = false;
        boolean teleopPeriodicCalled = false;
        boolean teleopExitCalled = false;
        boolean simulationInitCalled = false;
        boolean simulationPeriodicCalled = false;
        boolean testInitCalled = false;
        boolean testPeriodicCalled = false;
        boolean testExitCalled = false;
        boolean outputTelemetryCalled = false;

        TestSubsystem(final String argName) {
            super(null, argName);
        }

        @Override
        public void robotInit(final double argTimestamp) {
            robotInitCalled = true;
        }

        @Override
        public void robotPeriodicBefore(final double argTimestamp) {
            robotPeriodicBeforeCalled = true;
        }

        @Override
        public void robotPeriodicAfter(final double argTimestamp) {
            robotPeriodicAfterCalled = true;
        }

        @Override
        public void disabledInit(final double argTimestamp) {
            disabledInitCalled = true;
        }

        @Override
        public void disabledPeriodic(final double argTimestamp) {
            disabledPeriodicCalled = true;
        }

        @Override
        public void disabledExit(final double argTimestamp) {
            disabledExitCalled = true;
        }

        @Override
        public void autonomousInit(final double argTimestamp) {
            autonomousInitCalled = true;
        }

        @Override
        public void autonomousPeriodic(final double argTimestamp) {
            autonomousPeriodicCalled = true;
        }

        @Override
        public void autonomousExit(final double argTimestamp) {
            autonomousExitCalled = true;
        }

        @Override
        public void teleopInit(final double argTimestamp) {
            teleopInitCalled = true;
        }

        @Override
        public void teleopPeriodic(final double argTimestamp) {
            teleopPeriodicCalled = true;
        }

        @Override
        public void teleopExit(final double argTimestamp) {
            teleopExitCalled = true;
        }

        @Override
        public void simulationInit(final double argTimestamp) {
            simulationInitCalled = true;
        }

        @Override
        public void simulationPeriodic(final double argTimestamp) {
            simulationPeriodicCalled = true;
        }

        @Override
        public void testInit(final double argTimestamp) {
            testInitCalled = true;
        }

        @Override
        public void testPeriodic(final double argTimestamp) {
            testPeriodicCalled = true;
        }

        @Override
        public void testExit(final double argTimestamp) {
            testExitCalled = true;
        }

        @Override
        public void outputTelemetry() {
            outputTelemetryCalled = true;
        }
    }

    /** Subsystem that throws on every lifecycle method. */
    private static class ThrowingSubsystem extends Subsystem {
        ThrowingSubsystem(final String argName) {
            super(null, argName);
        }

        @Override
        public void robotInit(final double argTimestamp) {
            throw new RuntimeException("robotInit failure");
        }

        @Override
        public void robotPeriodicBefore(final double argTimestamp) {
            throw new RuntimeException("robotPeriodicBefore failure");
        }

        @Override
        public void robotPeriodicAfter(final double argTimestamp) {
            throw new RuntimeException("robotPeriodicAfter failure");
        }

        @Override
        public void disabledInit(final double argTimestamp) {
            throw new RuntimeException("disabledInit failure");
        }

        @Override
        public void disabledPeriodic(final double argTimestamp) {
            throw new RuntimeException("disabledPeriodic failure");
        }

        @Override
        public void disabledExit(final double argTimestamp) {
            throw new RuntimeException("disabledExit failure");
        }

        @Override
        public void autonomousInit(final double argTimestamp) {
            throw new RuntimeException("autonomousInit failure");
        }

        @Override
        public void autonomousPeriodic(final double argTimestamp) {
            throw new RuntimeException("autonomousPeriodic failure");
        }

        @Override
        public void autonomousExit(final double argTimestamp) {
            throw new RuntimeException("autonomousExit failure");
        }

        @Override
        public void teleopInit(final double argTimestamp) {
            throw new RuntimeException("teleopInit failure");
        }

        @Override
        public void teleopPeriodic(final double argTimestamp) {
            throw new RuntimeException("teleopPeriodic failure");
        }

        @Override
        public void teleopExit(final double argTimestamp) {
            throw new RuntimeException("teleopExit failure");
        }

        @Override
        public void simulationInit(final double argTimestamp) {
            throw new RuntimeException("simulationInit failure");
        }

        @Override
        public void simulationPeriodic(final double argTimestamp) {
            throw new RuntimeException("simulationPeriodic failure");
        }

        @Override
        public void testInit(final double argTimestamp) {
            throw new RuntimeException("testInit failure");
        }

        @Override
        public void testPeriodic(final double argTimestamp) {
            throw new RuntimeException("testPeriodic failure");
        }

        @Override
        public void testExit(final double argTimestamp) {
            throw new RuntimeException("testExit failure");
        }

        @Override
        public void outputTelemetry() {
            throw new RuntimeException("outputTelemetry failure");
        }
    }

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = SubsystemManager.class.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /* --- getInstance (singleton) --- */

    @Test
    void getInstanceReturnsSameObject() {
        SubsystemManager a = SubsystemManager.getInstance();
        SubsystemManager b = SubsystemManager.getInstance();
        assertSame(a, b);
    }

    @Test
    void getInstanceReturnsNonNull() {
        assertNotNull(SubsystemManager.getInstance());
    }

    /* --- addSubsystem --- */

    @Test
    void addSubsystemIncreasesCount() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub1");

        mgr.addSubsystem(sub);

        assertEquals(1, mgr.getSubsystems().size());
    }

    @Test
    void addSubsystemNullIgnored() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        mgr.addSubsystem(null);

        assertEquals(0, mgr.getSubsystems().size());
    }

    @Test
    void addMultipleSubsystems() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        mgr.addSubsystem(new TestSubsystem("Sub1"));
        mgr.addSubsystem(new TestSubsystem("Sub2"));
        mgr.addSubsystem(new TestSubsystem("Sub3"));

        assertEquals(3, mgr.getSubsystems().size());
    }

    @Test
    void getSubsystemsPreservesOrder() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem a = new TestSubsystem("A");
        TestSubsystem b = new TestSubsystem("B");
        mgr.addSubsystem(a);
        mgr.addSubsystem(b);

        List<Subsystem> list = mgr.getSubsystems();
        assertSame(a, list.get(0));
        assertSame(b, list.get(1));
    }

    /* --- forEachSafe exception isolation --- */

    @Test
    void forEachSafeIsolatesExceptions() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.robotPeriodicBefore(0.0);

        assertTrue(
                safe.robotPeriodicBeforeCalled,
                "Safe subsystem should still run after thrower fails");
    }

    @Test
    void forEachSafeIsolatesRobotPeriodicAfter() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.robotPeriodicAfter(0.0);

        assertTrue(safe.robotPeriodicAfterCalled);
    }

    @Test
    void forEachSafeIsolatesOutputTelemetry() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.outputTelemetry();

        assertTrue(safe.outputTelemetryCalled);
    }

    /* --- robotInit rethrows --- */

    @Test
    void robotInitCatchesException() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");

        mgr.addSubsystem(thrower);

        /* robotInit uses forEachSafe — exceptions are caught, not rethrown */
        assertDoesNotThrow(() -> mgr.robotInit(0.0));
    }

    @Test
    void robotInitCallsAllSubsystems() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub1");

        mgr.addSubsystem(sub);
        mgr.robotInit(0.0);

        assertTrue(sub.robotInitCalled);
    }

    /* --- Lifecycle method delegation --- */

    @Test
    void robotPeriodicBeforeDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.robotPeriodicBefore(0.0);

        assertTrue(sub.robotPeriodicBeforeCalled);
    }

    @Test
    void robotPeriodicAfterDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.robotPeriodicAfter(0.0);

        assertTrue(sub.robotPeriodicAfterCalled);
    }

    @Test
    void disabledInitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.disabledInit(0.0);

        assertTrue(sub.disabledInitCalled);
    }

    @Test
    void autonomousInitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.autonomousInit(0.0);

        assertTrue(sub.autonomousInitCalled);
    }

    @Test
    void teleopInitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.teleopInit(0.0);

        assertTrue(sub.teleopInitCalled);
    }

    @Test
    void simulationInitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.simulationInit(0.0);

        assertTrue(sub.simulationInitCalled);
    }

    @Test
    void testInitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.testInit(0.0);

        assertTrue(sub.testInitCalled);
    }

    /* --- outputTelemetry --- */

    @Test
    void outputTelemetryDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.outputTelemetry();

        assertTrue(sub.outputTelemetryCalled);
    }

    @Test
    void outputTelemetryWithNoSubsystemsDoesNotThrow() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        assertDoesNotThrow(() -> mgr.outputTelemetry());
    }

    /* --- Missing lifecycle method delegation --- */

    @Test
    void disabledPeriodicDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.disabledPeriodic(0.0);

        assertTrue(sub.disabledPeriodicCalled);
    }

    @Test
    void disabledExitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.disabledExit(0.0);

        assertTrue(sub.disabledExitCalled);
    }

    @Test
    void autonomousPeriodicDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.autonomousPeriodic(0.0);

        assertTrue(sub.autonomousPeriodicCalled);
    }

    @Test
    void autonomousExitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.autonomousExit(0.0);

        assertTrue(sub.autonomousExitCalled);
    }

    @Test
    void teleopPeriodicDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.teleopPeriodic(0.0);

        assertTrue(sub.teleopPeriodicCalled);
    }

    @Test
    void teleopExitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.teleopExit(0.0);

        assertTrue(sub.teleopExitCalled);
    }

    @Test
    void simulationPeriodicDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.simulationPeriodic(0.0);

        assertTrue(sub.simulationPeriodicCalled);
    }

    @Test
    void testPeriodicDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.testPeriodic(0.0);

        assertTrue(sub.testPeriodicCalled);
    }

    @Test
    void testExitDelegates() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        TestSubsystem sub = new TestSubsystem("Sub");
        mgr.addSubsystem(sub);

        mgr.testExit(0.0);

        assertTrue(sub.testExitCalled);
    }

    /* --- forEachSafe exception isolation for all missing lifecycle methods --- */

    @Test
    void forEachSafeIsolatesDisabledPeriodic() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.disabledPeriodic(0.0);

        assertTrue(safe.disabledPeriodicCalled);
    }

    @Test
    void forEachSafeIsolatesDisabledExit() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.disabledExit(0.0);

        assertTrue(safe.disabledExitCalled);
    }

    @Test
    void forEachSafeIsolatesAutonomousPeriodic() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.autonomousPeriodic(0.0);

        assertTrue(safe.autonomousPeriodicCalled);
    }

    @Test
    void forEachSafeIsolatesAutonomousExit() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.autonomousExit(0.0);

        assertTrue(safe.autonomousExitCalled);
    }

    @Test
    void forEachSafeIsolatesTeleopPeriodic() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.teleopPeriodic(0.0);

        assertTrue(safe.teleopPeriodicCalled);
    }

    @Test
    void forEachSafeIsolatesTeleopExit() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.teleopExit(0.0);

        assertTrue(safe.teleopExitCalled);
    }

    @Test
    void forEachSafeIsolatesSimulationPeriodic() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.simulationPeriodic(0.0);

        assertTrue(safe.simulationPeriodicCalled);
    }

    @Test
    void forEachSafeIsolatesTestInit() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.testInit(0.0);

        assertTrue(safe.testInitCalled);
    }

    @Test
    void forEachSafeIsolatesTestPeriodic() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.testPeriodic(0.0);

        assertTrue(safe.testPeriodicCalled);
    }

    @Test
    void forEachSafeIsolatesTestExit() {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        TestSubsystem safe = new TestSubsystem("Safe");

        mgr.addSubsystem(thrower);
        mgr.addSubsystem(safe);

        mgr.testExit(0.0);

        assertTrue(safe.testExitCalled);
    }

    /* --- Empty subsystem list does not throw for any lifecycle method --- */

    @Test
    void emptySubsystemListLifecycleMethodsDoNotThrow() {
        SubsystemManager mgr = SubsystemManager.getInstance();

        assertDoesNotThrow(() -> mgr.robotPeriodicBefore(0.0));
        assertDoesNotThrow(() -> mgr.robotPeriodicAfter(0.0));
        assertDoesNotThrow(() -> mgr.disabledInit(0.0));
        assertDoesNotThrow(() -> mgr.disabledPeriodic(0.0));
        assertDoesNotThrow(() -> mgr.disabledExit(0.0));
        assertDoesNotThrow(() -> mgr.autonomousInit(0.0));
        assertDoesNotThrow(() -> mgr.autonomousPeriodic(0.0));
        assertDoesNotThrow(() -> mgr.autonomousExit(0.0));
        assertDoesNotThrow(() -> mgr.teleopInit(0.0));
        assertDoesNotThrow(() -> mgr.teleopPeriodic(0.0));
        assertDoesNotThrow(() -> mgr.teleopExit(0.0));
        assertDoesNotThrow(() -> mgr.simulationInit(0.0));
        assertDoesNotThrow(() -> mgr.simulationPeriodic(0.0));
        assertDoesNotThrow(() -> mgr.testInit(0.0));
        assertDoesNotThrow(() -> mgr.testPeriodic(0.0));
        assertDoesNotThrow(() -> mgr.testExit(0.0));
    }

    /* --- forEachSafe rate limiting --- */

    @SuppressWarnings("unchecked")
    private Map<String, Double> getRateLimitMap(final SubsystemManager argMgr) throws Exception {
        Field field = SubsystemManager.class.getDeclaredField("lastErrorNotificationTime");
        field.setAccessible(true);
        return (Map<String, Double>) field.get(argMgr);
    }

    @Test
    void forEachSafeRecordsRateLimitTimeOnException() throws Exception {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("RateLimitTest");
        mgr.addSubsystem(thrower);

        mgr.robotPeriodicBefore(0.0);

        Map<String, Double> map = getRateLimitMap(mgr);
        assertTrue(
                map.containsKey("RateLimitTest"),
                "Rate limit map should track the throwing subsystem");
        assertTrue(map.get("RateLimitTest") >= 0.0, "Recorded time should be non-negative");
    }

    @Test
    void forEachSafeRateLimitIsPerSubsystem() throws Exception {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem throwerA = new ThrowingSubsystem("SubA");
        ThrowingSubsystem throwerB = new ThrowingSubsystem("SubB");
        mgr.addSubsystem(throwerA);
        mgr.addSubsystem(throwerB);

        mgr.robotPeriodicBefore(0.0);

        Map<String, Double> map = getRateLimitMap(mgr);
        assertTrue(map.containsKey("SubA"), "Rate limit map should track SubA independently");
        assertTrue(map.containsKey("SubB"), "Rate limit map should track SubB independently");
    }

    @Test
    void forEachSafeRateLimitMapResetBySingletonReset() throws Exception {
        SubsystemManager mgr = SubsystemManager.getInstance();
        ThrowingSubsystem thrower = new ThrowingSubsystem("Thrower");
        mgr.addSubsystem(thrower);
        mgr.robotPeriodicBefore(0.0);

        // Reset singleton (same as @BeforeEach) and get fresh instance
        resetSingleton();
        SubsystemManager fresh = SubsystemManager.getInstance();

        Map<String, Double> map = getRateLimitMap(fresh);
        assertTrue(map.isEmpty(), "Fresh singleton should have empty rate limit map");
    }
}
