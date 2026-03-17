package com.team271.lib.util;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.util.Alert.AlertType;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void resetGroups() throws Exception {
        Field groupsField = Alert.class.getDeclaredField("groups");
        groupsField.setAccessible(true);
        ((Map<String, ?>) groupsField.get(null)).clear();
    }

    // ── AlertType enum ──

    @Test
    void alertTypeEnum_hasThreeValues() {
        assertEquals(3, AlertType.values().length);
    }

    @Test
    void alertTypeEnum_containsExpectedValues() {
        assertNotNull(AlertType.valueOf("ERROR"));
        assertNotNull(AlertType.valueOf("WARNING"));
        assertNotNull(AlertType.valueOf("INFO"));
    }

    // ── Constructor ──

    @Test
    void constructor_defaultGroup() {
        assertDoesNotThrow(() -> new Alert("test alert", AlertType.INFO));
    }

    @Test
    void constructor_customGroup() {
        assertDoesNotThrow(() -> new Alert("CustomGroup", "test alert", AlertType.WARNING));
    }

    @Test
    void constructor_sameGroupReusesExisting() {
        new Alert("SharedGroup", "first", AlertType.INFO);
        assertDoesNotThrow(() -> new Alert("SharedGroup", "second", AlertType.ERROR));
    }

    // ── set() ──

    @Test
    void set_activateDoesNotThrow() {
        Alert alert = new Alert("SetTest", "activation test", AlertType.ERROR);
        assertDoesNotThrow(() -> alert.set(true));
    }

    @Test
    void set_deactivateDoesNotThrow() {
        Alert alert = new Alert("SetTest2", "deactivation test", AlertType.WARNING);
        alert.set(true);
        assertDoesNotThrow(() -> alert.set(false));
    }

    @Test
    void set_activateInfoDoesNotThrow() {
        Alert alert = new Alert("SetTest3", "info test", AlertType.INFO);
        assertDoesNotThrow(() -> alert.set(true));
    }

    @Test
    void set_doubleActivateDoesNotThrow() {
        Alert alert = new Alert("SetTest4", "double activate", AlertType.ERROR);
        alert.set(true);
        assertDoesNotThrow(() -> alert.set(true));
    }

    // ── setText() ──

    @Test
    void setText_whileActive() {
        Alert alert = new Alert("TextTest", "original", AlertType.WARNING);
        alert.set(true);
        assertDoesNotThrow(() -> alert.setText("updated"));
    }

    @Test
    void setText_whileInactive() {
        Alert alert = new Alert("TextTest2", "original", AlertType.INFO);
        assertDoesNotThrow(() -> alert.setText("updated"));
    }

    @Test
    void setText_sameTextWhileActive() {
        Alert alert = new Alert("TextTest3", "same", AlertType.ERROR);
        alert.set(true);
        assertDoesNotThrow(() -> alert.setText("same"));
    }

    // ── setText while active for each type ──

    @Test
    void setTextWhileActiveError() {
        Alert alert = new Alert("ErrText", "original error", AlertType.ERROR);
        alert.set(true);
        assertDoesNotThrow(() -> alert.setText("updated error"));
    }

    @Test
    void setTextWhileActiveWarning() {
        Alert alert = new Alert("WarnText", "original warning", AlertType.WARNING);
        alert.set(true);
        assertDoesNotThrow(() -> alert.setText("updated warning"));
    }

    @Test
    void setTextWhileActiveInfo() {
        Alert alert = new Alert("InfoText", "original info", AlertType.INFO);
        alert.set(true);
        assertDoesNotThrow(() -> alert.setText("updated info"));
    }

    // ── outputTelemetry ──

    @Test
    void outputTelemetryDoesNotThrow() {
        assertDoesNotThrow(Alert::outputTelemetry);
    }

    @Test
    void outputTelemetryWithMultipleGroupsAndTypes() {
        Alert errAlert = new Alert("GroupA", "error alert", AlertType.ERROR);
        Alert warnAlert = new Alert("GroupA", "warning alert", AlertType.WARNING);
        Alert infoAlert = new Alert("GroupB", "info alert", AlertType.INFO);
        Alert inactiveAlert = new Alert("GroupB", "inactive", AlertType.ERROR);

        errAlert.set(true);
        warnAlert.set(true);
        infoAlert.set(true);
        // inactiveAlert stays inactive

        assertDoesNotThrow(Alert::outputTelemetry);
    }
}
