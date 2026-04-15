package com.team271.lib.hardware.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CurrentLimitConfigTest {

    /* --- Factory Methods --- */

    @Test
    void statorOnly_enablesStatorDisablesSupply() {
        CurrentLimitConfig cfg = CurrentLimitConfig.statorOnly(40.0);
        assertTrue(cfg.statorEnabled());
        assertEquals(40.0, cfg.statorLimit());
        assertFalse(cfg.supplyEnabled());
        assertEquals(0, cfg.supplyLimit());
    }

    @Test
    void supplyOnly_enablesSupplyDisablesStator() {
        CurrentLimitConfig cfg = CurrentLimitConfig.supplyOnly(30.0);
        assertFalse(cfg.statorEnabled());
        assertTrue(cfg.supplyEnabled());
        assertEquals(30.0, cfg.supplyLimit());
    }

    @Test
    void both_enablesBothLimits() {
        CurrentLimitConfig cfg = CurrentLimitConfig.both(40.0, 30.0);
        assertTrue(cfg.statorEnabled());
        assertEquals(40.0, cfg.statorLimit());
        assertTrue(cfg.supplyEnabled());
        assertEquals(30.0, cfg.supplyLimit());
    }

    @Test
    void disabled_disablesEverything() {
        CurrentLimitConfig cfg = CurrentLimitConfig.disabled();
        assertFalse(cfg.statorEnabled());
        assertFalse(cfg.supplyEnabled());
        assertEquals(0, cfg.statorLimit());
        assertEquals(0, cfg.supplyLimit());
    }

    @Test
    void fullConstructor_allFieldsSet() {
        CurrentLimitConfig cfg = new CurrentLimitConfig(true, 60.0, true, 40.0, 20.0, 1.5);
        assertTrue(cfg.statorEnabled());
        assertEquals(60.0, cfg.statorLimit());
        assertTrue(cfg.supplyEnabled());
        assertEquals(40.0, cfg.supplyLimit());
        assertEquals(20.0, cfg.supplyLowerLimit());
        assertEquals(1.5, cfg.supplyLowerTime());
    }

    /* --- Record Equality --- */

    @Test
    void equality_sameValues() {
        CurrentLimitConfig a = CurrentLimitConfig.statorOnly(40.0);
        CurrentLimitConfig b = CurrentLimitConfig.statorOnly(40.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equality_differentValues() {
        CurrentLimitConfig a = CurrentLimitConfig.statorOnly(40.0);
        CurrentLimitConfig b = CurrentLimitConfig.statorOnly(50.0);
        assertNotEquals(a, b);
    }
}
