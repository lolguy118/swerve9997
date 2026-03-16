package com.team271.lib.misc;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.misc.Elastic.Notification;
import com.team271.lib.misc.Elastic.Notification.NotificationLevel;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ElasticTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    // ── NotificationLevel enum ──

    @Test
    void notificationLevel_hasThreeValues() {
        assertEquals(3, NotificationLevel.values().length);
    }

    @Test
    void notificationLevel_containsExpectedValues() {
        assertNotNull(NotificationLevel.valueOf("INFO"));
        assertNotNull(NotificationLevel.valueOf("WARNING"));
        assertNotNull(NotificationLevel.valueOf("ERROR"));
    }

    // ── Notification constructors ──

    @Test
    void notification_defaultConstructor() {
        Notification n = new Notification();
        assertEquals(NotificationLevel.INFO, n.getLevel());
        assertEquals("", n.getTitle());
        assertEquals("", n.getDescription());
        assertEquals(3000, n.getDisplayTimeMillis());
        assertEquals(350.0, n.getWidth(), 1e-9);
        assertEquals(-1.0, n.getHeight(), 1e-9);
    }

    @Test
    void notification_threeArgConstructor() {
        Notification n = new Notification(NotificationLevel.ERROR, "Title", "Desc");
        assertEquals(NotificationLevel.ERROR, n.getLevel());
        assertEquals("Title", n.getTitle());
        assertEquals("Desc", n.getDescription());
        assertEquals(3000, n.getDisplayTimeMillis());
    }

    @Test
    void notification_fourArgConstructor_withDisplayTime() {
        Notification n = new Notification(NotificationLevel.WARNING, "T", "D", 5000);
        assertEquals(5000, n.getDisplayTimeMillis());
    }

    @Test
    void notification_fiveArgConstructor_withDimensions() {
        Notification n = new Notification(NotificationLevel.INFO, "T", "D", 400.0, 200.0);
        assertEquals(400.0, n.getWidth(), 1e-9);
        assertEquals(200.0, n.getHeight(), 1e-9);
    }

    @Test
    void notification_fullConstructor() {
        Notification n = new Notification(NotificationLevel.ERROR, "T", "D", 1000, 500.0, 300.0);
        assertEquals(NotificationLevel.ERROR, n.getLevel());
        assertEquals("T", n.getTitle());
        assertEquals("D", n.getDescription());
        assertEquals(1000, n.getDisplayTimeMillis());
        assertEquals(500.0, n.getWidth(), 1e-9);
        assertEquals(300.0, n.getHeight(), 1e-9);
    }

    // ── Notification setters/getters ──

    @Test
    void notification_setLevel() {
        Notification n = new Notification();
        n.setLevel(NotificationLevel.ERROR);
        assertEquals(NotificationLevel.ERROR, n.getLevel());
    }

    @Test
    void notification_setTitle() {
        Notification n = new Notification();
        n.setTitle("New Title");
        assertEquals("New Title", n.getTitle());
    }

    @Test
    void notification_setDescription() {
        Notification n = new Notification();
        n.setDescription("New Desc");
        assertEquals("New Desc", n.getDescription());
    }

    @Test
    void notification_setDisplayTimeMillis() {
        Notification n = new Notification();
        n.setDisplayTimeMillis(2000);
        assertEquals(2000, n.getDisplayTimeMillis());
    }

    @Test
    void notification_setDisplayTimeSeconds() {
        Notification n = new Notification();
        n.setDisplayTimeSeconds(2.5);
        assertEquals(2500, n.getDisplayTimeMillis());
    }

    @Test
    void notification_setWidth() {
        Notification n = new Notification();
        n.setWidth(500.0);
        assertEquals(500.0, n.getWidth(), 1e-9);
    }

    @Test
    void notification_setHeight() {
        Notification n = new Notification();
        n.setHeight(250.0);
        assertEquals(250.0, n.getHeight(), 1e-9);
    }

    // ── Builder pattern (with*) ──

    @Test
    void notification_withLevel_chainable() {
        Notification n = new Notification().withLevel(NotificationLevel.WARNING);
        assertEquals(NotificationLevel.WARNING, n.getLevel());
    }

    @Test
    void notification_withTitle_chainable() {
        Notification n = new Notification().withTitle("Chained");
        assertEquals("Chained", n.getTitle());
    }

    @Test
    void notification_withDescription_chainable() {
        Notification n = new Notification().withDescription("Chained Desc");
        assertEquals("Chained Desc", n.getDescription());
    }

    @Test
    void notification_withDisplaySeconds_chainable() {
        Notification n = new Notification().withDisplaySeconds(1.5);
        assertEquals(1500, n.getDisplayTimeMillis());
    }

    @Test
    void notification_withDisplayMilliseconds_chainable() {
        Notification n = new Notification().withDisplayMilliseconds(750);
        assertEquals(750, n.getDisplayTimeMillis());
    }

    @Test
    void notification_withWidth_chainable() {
        Notification n = new Notification().withWidth(600.0);
        assertEquals(600.0, n.getWidth(), 1e-9);
    }

    @Test
    void notification_withHeight_chainable() {
        Notification n = new Notification().withHeight(400.0);
        assertEquals(400.0, n.getHeight(), 1e-9);
    }

    @Test
    void notification_withAutomaticHeight() {
        Notification n = new Notification().withHeight(200.0).withAutomaticHeight();
        assertEquals(-1.0, n.getHeight(), 1e-9);
    }

    @Test
    void notification_withNoAutoDismiss() {
        Notification n = new Notification().withNoAutoDismiss();
        assertEquals(0, n.getDisplayTimeMillis());
    }

    @Test
    void notification_fullChaining() {
        Notification n =
                new Notification()
                        .withLevel(NotificationLevel.ERROR)
                        .withTitle("Error!")
                        .withDescription("Something broke")
                        .withDisplaySeconds(5.0)
                        .withWidth(400.0)
                        .withAutomaticHeight();
        assertEquals(NotificationLevel.ERROR, n.getLevel());
        assertEquals("Error!", n.getTitle());
        assertEquals("Something broke", n.getDescription());
        assertEquals(5000, n.getDisplayTimeMillis());
        assertEquals(400.0, n.getWidth(), 1e-9);
        assertEquals(-1.0, n.getHeight(), 1e-9);
    }

    // ── Elastic static methods ──

    @Test
    void sendNotification_doesNotThrow() {
        Notification n = new Notification(NotificationLevel.INFO, "Test", "Testing");
        assertDoesNotThrow(() -> Elastic.sendNotification(n));
    }

    @Test
    void selectTab_byName_doesNotThrow() {
        assertDoesNotThrow(() -> Elastic.selectTab("Autonomous"));
    }

    @Test
    void selectTab_byIndex_doesNotThrow() {
        assertDoesNotThrow(() -> Elastic.selectTab(2));
    }
}
