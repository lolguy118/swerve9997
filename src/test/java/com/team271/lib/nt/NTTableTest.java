package com.team271.lib.nt;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.networktables.Topic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NTTableTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    // ── Constructors ──

    @Test
    void constructor_singleArg_setsNameAndPath() {
        NTTable table = new NTTable("TestTable");
        assertEquals("TestTable", table.getTableName());
        assertEquals("/TestTable", table.getPath());
    }

    @Test
    void constructor_withParent_buildsNestedPath() {
        NTTable parent = new NTTable("Parent");
        NTTable child = new NTTable(parent, "Child");
        assertEquals("Child", child.getTableName());
        assertEquals("/Parent/Child", child.getPath());
    }

    @Test
    void constructor_deepNesting() {
        NTTable root = new NTTable("Root");
        NTTable mid = new NTTable(root, "Mid");
        NTTable leaf = new NTTable(mid, "Leaf");
        assertEquals("/Root/Mid/Leaf", leaf.getPath());
    }

    @Test
    void constructor_nullParent_rootPath() {
        NTTable table = new NTTable(null, "Solo");
        assertEquals("/Solo", table.getPath());
    }

    // ── getTopic ──

    @Test
    void getTopic_returnsNonNull() {
        NTTable table = new NTTable("TopicTest");
        Topic topic = table.getTopic("myTopic");
        assertNotNull(topic);
    }

    @Test
    void getTopic_differentNames_differentTopics() {
        NTTable table = new NTTable("TopicTest2");
        Topic t1 = table.getTopic("a");
        Topic t2 = table.getTopic("b");
        assertNotEquals(t1.getName(), t2.getName());
    }

    // ── Telemetry enable/disable ──

    @Test
    void enableDisableTelemetry() {
        NTTable table = new NTTable("TeleTest");
        assertFalse(table.enabled);
        table.enableTelemetry();
        assertTrue(table.enabled);
        table.disableTelemetry();
        assertFalse(table.enabled);
    }

    // ── buildPath ──

    @Test
    void buildPath_matchesGetPath() {
        NTTable parent = new NTTable("P");
        NTTable child = new NTTable(parent, "C");
        assertEquals(child.getPath(), child.buildPath());
    }
}
