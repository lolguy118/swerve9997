package com.team271.lib.nt;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoggedNTInputTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    // ── Null table safety ──

    @Test
    void nullTable_double_noException() {
        LoggedNTInput input = new LoggedNTInput(null, "test", 1.5);
        assertDoesNotThrow(() -> input.getDbl());
        assertEquals(1.5, input.getDbl());
    }

    @Test
    void nullTable_boolean_noException() {
        LoggedNTInput input = new LoggedNTInput(null, "test", true);
        assertDoesNotThrow(() -> input.getBool());
        assertTrue(input.getBool());
    }

    @Test
    void nullTable_long_noException() {
        LoggedNTInput input = new LoggedNTInput(null, "test", 42L);
        assertDoesNotThrow(() -> input.getLong());
        assertEquals(42L, input.getLong());
    }

    @Test
    void nullTable_string_noException() {
        LoggedNTInput input = new LoggedNTInput(null, "test", "hello");
        assertDoesNotThrow(() -> input.getString());
        assertEquals("hello", input.getString());
    }

    // ── hasChanged returns false when no change ──

    @Test
    void nullTable_hasChanged_returnsFalse() {
        LoggedNTInput input = new LoggedNTInput(null, "test", 1.0);
        assertFalse(input.hasChanged());
    }

    @Test
    void nullTable_hasBoolChanged_returnsFalse() {
        LoggedNTInput input = new LoggedNTInput(null, "test", false);
        assertFalse(input.hasBoolChanged());
    }

    @Test
    void nullTable_hasLongChanged_returnsFalse() {
        LoggedNTInput input = new LoggedNTInput(null, "test", 0L);
        assertFalse(input.hasLongChanged());
    }

    @Test
    void nullTable_hasStringChanged_returnsFalse() {
        LoggedNTInput input = new LoggedNTInput(null, "test", "");
        assertFalse(input.hasStringChanged());
    }

    // ── Construction with valid table ──

    @Test
    void validTable_double_constructsSuccessfully() {
        NTTable table = new NTTable("LoggedNTInputTest");
        LoggedNTInput input = new LoggedNTInput(table, "gain", 3.14);
        assertNotNull(input);
        assertEquals(3.14, input.getDbl());
    }

    @Test
    void validTable_boolean_constructsSuccessfully() {
        NTTable table = new NTTable("LoggedNTInputTest");
        LoggedNTInput input = new LoggedNTInput(table, "enabled", true);
        assertNotNull(input);
        assertTrue(input.getBool());
    }

    @Test
    void validTable_long_constructsSuccessfully() {
        NTTable table = new NTTable("LoggedNTInputTest");
        LoggedNTInput input = new LoggedNTInput(table, "count", 99L);
        assertNotNull(input);
        assertEquals(99L, input.getLong());
    }

    @Test
    void validTable_string_constructsSuccessfully() {
        NTTable table = new NTTable("LoggedNTInputTest");
        LoggedNTInput input = new LoggedNTInput(table, "name", "test");
        assertNotNull(input);
        assertEquals("test", input.getString());
    }

    // ── Default value returned when no dashboard change ──

    @Test
    void validTable_hasChanged_falseBeforeAnyChange() {
        NTTable table = new NTTable("LoggedNTInputTest");
        LoggedNTInput input = new LoggedNTInput(table, "stable", 5.0);
        input.getDbl(); // prime the cached value
        assertFalse(input.hasChanged());
    }

    // ── hasChanged variants on valid table ──

    @Test
    void validTable_hasBoolChanged_falseBeforeAnyChange() {
        NTTable table = new NTTable("LoggedNTInputBoolChange");
        LoggedNTInput input = new LoggedNTInput(table, "boolStable", false);
        input.getBool();
        assertFalse(input.hasBoolChanged());
    }

    @Test
    void validTable_hasLongChanged_falseBeforeAnyChange() {
        NTTable table = new NTTable("LoggedNTInputLongChange");
        LoggedNTInput input = new LoggedNTInput(table, "longStable", 10L);
        input.getLong();
        assertFalse(input.hasLongChanged());
    }

    @Test
    void validTable_hasStringChanged_falseBeforeAnyChange() {
        NTTable table = new NTTable("LoggedNTInputStrChange");
        LoggedNTInput input = new LoggedNTInput(table, "strStable", "test");
        input.getString();
        assertFalse(input.hasStringChanged());
    }

    // ── Repeated getDbl/getBool/getLong/getString calls with valid table ──

    @Test
    void validTable_repeatedDblCalls() {
        NTTable table = new NTTable("LoggedNTInputRepeatDbl");
        LoggedNTInput input = new LoggedNTInput(table, "dblRepeat", 2.0);
        assertEquals(2.0, input.getDbl());
        assertEquals(2.0, input.getDbl());
    }

    @Test
    void validTable_repeatedBoolCalls() {
        NTTable table = new NTTable("LoggedNTInputRepeatBool");
        LoggedNTInput input = new LoggedNTInput(table, "boolRepeat", true);
        assertTrue(input.getBool());
        assertTrue(input.getBool());
    }

    @Test
    void validTable_repeatedLongCalls() {
        NTTable table = new NTTable("LoggedNTInputRepeatLong");
        LoggedNTInput input = new LoggedNTInput(table, "longRepeat", 7L);
        assertEquals(7L, input.getLong());
        assertEquals(7L, input.getLong());
    }

    @Test
    void validTable_repeatedStringCalls() {
        NTTable table = new NTTable("LoggedNTInputRepeatStr");
        LoggedNTInput input = new LoggedNTInput(table, "strRepeat", "abc");
        assertEquals("abc", input.getString());
        assertEquals("abc", input.getString());
    }
}
