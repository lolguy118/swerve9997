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
    void booleanEntry_publishDoesNotThrow() {
        NTTable table = new NTTable("BoolTest");
        NTEntry entry = new NTEntry(table, "boolVal", false, 100);
        assertDoesNotThrow(() -> entry.publish(true));
    }

    @Test
    void booleanEntry_defaultConstructor() {
        NTTable table = new NTTable("BoolTest2");
        NTEntry entry = new NTEntry(table, "boolVal2", false);
        assertNotNull(entry);
    }

    @Test
    void booleanEntry_repeatedPublishDoesNotThrow() {
        NTTable table = new NTTable("BoolCache");
        NTEntry entry = new NTEntry(table, "boolCache", false, 100);
        assertDoesNotThrow(
                () -> {
                    entry.publish(true);
                    entry.publish(true);
                });
    }

    // ── Double type ──

    @Test
    void doubleEntry_publishDoesNotThrow() {
        NTTable table = new NTTable("DblTest");
        NTEntry entry = new NTEntry(table, "dblVal", 0.0, 100);
        assertDoesNotThrow(() -> entry.publish(3.14));
    }

    @Test
    void doubleEntry_defaultConstructor() {
        NTTable table = new NTTable("DblTest2");
        NTEntry entry = new NTEntry(table, "dblVal2", 0.0);
        assertNotNull(entry);
    }

    @Test
    void doubleEntry_repeatedPublishDoesNotThrow() {
        NTTable table = new NTTable("DblCache");
        NTEntry entry = new NTEntry(table, "dblCache", 0.0, 100);
        assertDoesNotThrow(
                () -> {
                    entry.publish(1.0);
                    entry.publish(1.0);
                });
    }

    // ── Long type ──

    @Test
    void longEntry_publishDoesNotThrow() {
        NTTable table = new NTTable("LongTest");
        NTEntry entry = new NTEntry(table, "longVal", 0L, 100);
        assertDoesNotThrow(() -> entry.publish(42L));
    }

    @Test
    void longEntry_defaultConstructor() {
        NTTable table = new NTTable("LongTest2");
        NTEntry entry = new NTEntry(table, "longVal2", 0L);
        assertNotNull(entry);
    }

    // ── Int type ──

    @Test
    void intEntry_publishDoesNotThrow() {
        NTTable table = new NTTable("IntTest");
        NTEntry entry = new NTEntry(table, "intVal", 0, 100);
        assertDoesNotThrow(() -> entry.publish(99));
    }

    @Test
    void intEntry_defaultConstructor() {
        NTTable table = new NTTable("IntTest2");
        NTEntry entry = new NTEntry(table, "intVal2", 0);
        assertNotNull(entry);
    }

    // ── String type ──

    @Test
    void stringEntry_publishDoesNotThrow() {
        NTTable table = new NTTable("StrTest");
        NTEntry entry = new NTEntry(table, "strVal", "", 100);
        assertDoesNotThrow(() -> entry.publish("hello"));
    }

    @Test
    void stringEntry_defaultConstructor() {
        NTTable table = new NTTable("StrTest2");
        NTEntry entry = new NTEntry(table, "strVal2", "");
        assertNotNull(entry);
    }

    @Test
    void stringEntry_repeatedPublishDoesNotThrow() {
        NTTable table = new NTTable("StrCache");
        NTEntry entry = new NTEntry(table, "strCache", "", 100);
        assertDoesNotThrow(
                () -> {
                    entry.publish("first");
                    entry.publish("first");
                });
    }

    @Test
    void stringEntry_differentValuesDoNotThrow() {
        NTTable table = new NTTable("StrUpdate");
        NTEntry entry = new NTEntry(table, "strUpdate", "", 100);
        assertDoesNotThrow(
                () -> {
                    entry.publish("v1");
                    entry.publish("v2");
                });
    }

    // ── Setup after construction ──

    @Test
    void setup_boolean_afterBasicConstructor() {
        NTTable table = new NTTable("SetupBool");
        NTEntry entry = new NTEntry(table, "sb");
        assertDoesNotThrow(() -> entry.setup(true));
    }

    @Test
    void setup_double_afterBasicConstructor() {
        NTTable table = new NTTable("SetupDbl");
        NTEntry entry = new NTEntry(table, "sd");
        assertDoesNotThrow(() -> entry.setup(5.5));
    }

    @Test
    void setup_long_afterBasicConstructor() {
        NTTable table = new NTTable("SetupLong");
        NTEntry entry = new NTEntry(table, "sl");
        assertDoesNotThrow(() -> entry.setup(100L));
    }

    @Test
    void setup_int_afterBasicConstructor() {
        NTTable table = new NTTable("SetupInt");
        NTEntry entry = new NTEntry(table, "si");
        assertDoesNotThrow(() -> entry.setup(50));
    }

    @Test
    void setup_string_afterBasicConstructor() {
        NTTable table = new NTTable("SetupStr");
        NTEntry entry = new NTEntry(table, "ss");
        assertDoesNotThrow(() -> entry.setup("default"));
    }

    // ── Subscriber getters still work ──

    @Test
    void gettersReturnDefaultsWhenNoPublisher() {
        NTTable table = new NTTable("GetterDefaults");
        NTEntry entry = new NTEntry(table, "gd", 0.0);
        assertEquals(0.0, entry.getDbl(), 1e-9);
    }
}
