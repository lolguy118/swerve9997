package com.team271.lib.subsystem;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.nt.NTTable;
import edu.wpi.first.hal.HAL;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StateMachineCallbackTest {

    enum TestState {
        A,
        B,
        C
    }

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @Test
    void transition_firesOnExitThenOnEnter() {
        List<String> events = new ArrayList<>();
        StateMachine<TestState> sm =
                new StateMachine<>(new NTTable("Test"), TestState.A)
                        .withOnExit((from, to) -> events.add("exit:" + from + "->" + to))
                        .withOnEnter((from, to) -> events.add("enter:" + from + "->" + to));

        sm.transition(TestState.B);

        assertEquals(2, events.size());
        assertEquals("exit:A->B", events.get(0));
        assertEquals("enter:A->B", events.get(1));
    }

    @Test
    void transition_sameState_noCallbacksFired() {
        List<String> events = new ArrayList<>();
        StateMachine<TestState> sm =
                new StateMachine<>(new NTTable("Test"), TestState.A)
                        .withOnExit((from, to) -> events.add("exit"))
                        .withOnEnter((from, to) -> events.add("enter"));

        sm.transition(TestState.A);

        assertTrue(events.isEmpty());
        assertEquals(TestState.A, sm.getCurrentState());
    }

    @Test
    void transition_noCallbacksSet_noException() {
        StateMachine<TestState> sm = new StateMachine<>(new NTTable("Test"), TestState.A);

        // Should not throw even with no callbacks
        assertDoesNotThrow(() -> sm.transition(TestState.B));
        assertEquals(TestState.B, sm.getCurrentState());
    }

    @Test
    void transition_onlyOnEnterSet() {
        List<String> events = new ArrayList<>();
        StateMachine<TestState> sm =
                new StateMachine<>(new NTTable("Test"), TestState.A)
                        .withOnEnter((from, to) -> events.add("enter:" + from + "->" + to));

        sm.transition(TestState.C);

        assertEquals(1, events.size());
        assertEquals("enter:A->C", events.get(0));
    }

    @Test
    void transition_multipleTransitions_tracksCorrectFromState() {
        List<String> events = new ArrayList<>();
        StateMachine<TestState> sm =
                new StateMachine<>(new NTTable("Test"), TestState.A)
                        .withOnEnter((from, to) -> events.add(from + "->" + to));

        sm.transition(TestState.B);
        sm.transition(TestState.C);

        assertEquals(2, events.size());
        assertEquals("A->B", events.get(0));
        assertEquals("B->C", events.get(1));
    }

    @Test
    void withOnEnter_returnsThis_forChaining() {
        StateMachine<TestState> sm = new StateMachine<>(new NTTable("Test"), TestState.A);
        StateMachine<TestState> result = sm.withOnEnter((from, to) -> {});
        assertSame(sm, result);
    }

    @Test
    void withOnExit_returnsThis_forChaining() {
        StateMachine<TestState> sm = new StateMachine<>(new NTTable("Test"), TestState.A);
        StateMachine<TestState> result = sm.withOnExit((from, to) -> {});
        assertSame(sm, result);
    }

    @Test
    void backwardCompat_transitionWithoutCallbacks_updatesState() {
        StateMachine<TestState> sm = new StateMachine<>(new NTTable("Test"), TestState.A);
        sm.transition(TestState.B);
        assertEquals(TestState.B, sm.getCurrentState());
        sm.transition(TestState.B); // same state — still B
        assertEquals(TestState.B, sm.getCurrentState());
    }

    @Test
    void transition_reentrantCallFromOnExit_isIgnored() {
        StateMachine<TestState> sm = new StateMachine<>(new NTTable("Test"), TestState.A);
        List<String> events = new ArrayList<>();

        sm.withOnExit(
                (from, to) -> {
                    events.add("exit:" + from + "->" + to);
                    // Re-entrant call — should be silently ignored
                    sm.transition(TestState.C);
                });
        sm.withOnEnter((from, to) -> events.add("enter:" + from + "->" + to));

        sm.transition(TestState.B);

        // The re-entrant transition(C) should have been blocked
        assertEquals(TestState.B, sm.getCurrentState());
        assertEquals(2, events.size());
        assertEquals("exit:A->B", events.get(0));
        assertEquals("enter:A->B", events.get(1));
    }
}
