package com.team271.lib.auto;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoMoveSingleTest {

    private AutoMoveSingle move;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        move = new AutoMoveSingle();
    }

    /* --- Constructor --- */

    @Test
    void defaultConstructorSetsZeroDelay() {
        assertEquals(0.0, move.delay, 1e-9);
    }

    @Test
    void delayConstructorStoresDelay() {
        AutoMoveSingle delayed = new AutoMoveSingle(3.0);
        assertEquals(3.0, delayed.delay, 1e-9);
    }

    /* --- Inherits AutoMove behavior --- */

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

    @Test
    void endSetsComplete() {
        move.start();
        move.end();
        assertTrue(move.isComplete());
    }

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

    @Test
    void isWithinTimeLimitWhenNoDelay() {
        assertTrue(move.isWithinTimeLimit());
    }

    @Test
    void robotPeriodicBeforeDoesNotThrow() {
        move.start();
        assertDoesNotThrow(() -> move.robotPeriodicBefore(0.0));
    }

    @Test
    void robotPeriodicAfterDoesNotThrow() {
        move.start();
        assertDoesNotThrow(() -> move.robotPeriodicAfter(0.0));
    }

    @Test
    void autonomousPeriodicDoesNotThrow() {
        move.start();
        assertDoesNotThrow(() -> move.autonomousPeriodic(0.0));
    }
}
