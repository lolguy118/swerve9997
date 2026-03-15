package com.team271.lib.nt;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NTEntryTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    // ── Constructor with null table ──

    @Test
    void constructor_nullTable_noException() {
        NTEntry entry = new NTEntry(null, "test");
        assertNotNull(entry);
    }

    @Test
    void constructor_nullTable_gettersReturnDefaults() {
        NTEntry entry = new NTEntry(null, "test");
        assertFalse(entry.getBool());
        assertEquals(0.0, entry.getDbl());
        assertEquals(0L, entry.getLong());
        assertEquals(0, entry.getInt());
        assertEquals("", entry.getString());
    }

    @Test
    void constructor_nullTable_publishDoesNotThrow() {
        NTEntry entry = new NTEntry(null, "test");
        assertDoesNotThrow(() -> entry.publish(true));
        assertDoesNotThrow(() -> entry.publish(1.0));
        assertDoesNotThrow(() -> entry.publish(1L));
        assertDoesNotThrow(() -> entry.publish(1));
        assertDoesNotThrow(() -> entry.publish("hello"));
    }

    // ── Boolean type ──

    @Test
    void booleanEntry_publishAndGet() {
        NTTable table = new NTTable("BoolTest");
        NTEntry entry = new NTEntry(table, "boolVal", false, 100);
        entry.publish(true);
        assertTrue(entry.getBool());
    }

    @Test
    void booleanEntry_defaultConstructor() {
        NTTable table = new NTTable("BoolTest2");
        NTEntry entry = new NTEntry(table, "boolVal2", false);
        assertFalse(entry.getBool());
    }

    @Test
    void booleanEntry_cachingSkipsDuplicate() {
        NTTable table = new NTTable("BoolCache");
        NTEntry entry = new NTEntry(table, "boolCache", false, 100);
        entry.publish(true);
        // Publishing same value again should not throw
        entry.publish(true);
        assertTrue(entry.getBool());
    }

    // ── Double type ──

    @Test
    void doubleEntry_publishAndGet() {
        NTTable table = new NTTable("DblTest");
        NTEntry entry = new NTEntry(table, "dblVal", 0.0, 100);
        entry.publish(3.14);
        assertEquals(3.14, entry.getDbl(), 1e-9);
    }

    @Test
    void doubleEntry_defaultConstructor() {
        NTTable table = new NTTable("DblTest2");
        NTEntry entry = new NTEntry(table, "dblVal2", 0.0);
        assertEquals(0.0, entry.getDbl(), 1e-9);
    }

    @Test
    void doubleEntry_epsilonCaching() {
        NTTable table = new NTTable("DblCache");
        NTEntry entry = new NTEntry(table, "dblCache", 0.0, 100);
        entry.publish(1.0);
        // Publish same value - should be skipped by epsilonEquals
        entry.publish(1.0);
        assertEquals(1.0, entry.getDbl(), 1e-9);
    }

    // ── Long type ──

    @Test
    void longEntry_publishAndGet() {
        NTTable table = new NTTable("LongTest");
        NTEntry entry = new NTEntry(table, "longVal", 0L, 100);
        entry.publish(42L);
        assertEquals(42L, entry.getLong());
    }

    @Test
    void longEntry_defaultConstructor() {
        NTTable table = new NTTable("LongTest2");
        NTEntry entry = new NTEntry(table, "longVal2", 0L);
        assertEquals(0L, entry.getLong());
    }

    // ── Int type ──

    @Test
    void intEntry_publishAndGet() {
        NTTable table = new NTTable("IntTest");
        NTEntry entry = new NTEntry(table, "intVal", 0, 100);
        entry.publish(99);
        assertEquals(99, entry.getInt());
    }

    @Test
    void intEntry_defaultConstructor() {
        NTTable table = new NTTable("IntTest2");
        NTEntry entry = new NTEntry(table, "intVal2", 0);
        assertEquals(0, entry.getInt());
    }

    // ── String type ──

    @Test
    void stringEntry_publishAndGet() {
        NTTable table = new NTTable("StrTest");
        NTEntry entry = new NTEntry(table, "strVal", "", 100);
        entry.publish("hello");
        assertEquals("hello", entry.getString());
    }

    @Test
    void stringEntry_defaultConstructor() {
        NTTable table = new NTTable("StrTest2");
        NTEntry entry = new NTEntry(table, "strVal2", "");
        assertEquals("", entry.getString());
    }

    @Test
    void stringEntry_cachingSkipsDuplicate() {
        NTTable table = new NTTable("StrCache");
        NTEntry entry = new NTEntry(table, "strCache", "", 100);
        entry.publish("first");
        entry.publish("first"); // same value, should be skipped
        assertEquals("first", entry.getString());
    }

    @Test
    void stringEntry_updateValue() {
        NTTable table = new NTTable("StrUpdate");
        NTEntry entry = new NTEntry(table, "strUpdate", "", 100);
        entry.publish("v1");
        assertEquals("v1", entry.getString());
        entry.publish("v2");
        assertEquals("v2", entry.getString());
    }

    // ── Setup after construction ──

    @Test
    void setup_boolean_afterBasicConstructor() {
        NTTable table = new NTTable("SetupBool");
        NTEntry entry = new NTEntry(table, "sb");
        entry.setup(true);
        assertTrue(entry.getBool());
    }

    @Test
    void setup_double_afterBasicConstructor() {
        NTTable table = new NTTable("SetupDbl");
        NTEntry entry = new NTEntry(table, "sd");
        entry.setup(5.5);
        assertEquals(5.5, entry.getDbl(), 1e-9);
    }

    @Test
    void setup_long_afterBasicConstructor() {
        NTTable table = new NTTable("SetupLong");
        NTEntry entry = new NTEntry(table, "sl");
        entry.setup(100L);
        assertEquals(100L, entry.getLong());
    }

    @Test
    void setup_int_afterBasicConstructor() {
        NTTable table = new NTTable("SetupInt");
        NTEntry entry = new NTEntry(table, "si");
        entry.setup(50);
        assertEquals(50, entry.getInt());
    }

    @Test
    void setup_string_afterBasicConstructor() {
        NTTable table = new NTTable("SetupStr");
        NTEntry entry = new NTEntry(table, "ss");
        entry.setup("default");
        assertEquals("default", entry.getString());
    }
}
