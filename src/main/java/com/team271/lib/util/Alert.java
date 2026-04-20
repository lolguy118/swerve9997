package com.team271.lib.util;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.littletonrobotics.junction.Logger;

/* Class for managing persistent alerts to be sent over NetworkTables. */
public class Alert {
    private static final Map<String, SendableAlerts> groups =
            new ConcurrentHashMap<String, SendableAlerts>();

    private final AlertType type;
    private boolean active = false;
    private double activeStartTime = 0.0;
    private String text;

    /**
     * Creates a new Alert in the default group - "Alerts". If this is the first to be instantiated,
     * the appropriate entries will be added to NetworkTables.
     *
     * @param text Text to be displayed when the alert is active.
     * @param type Alert level specifying urgency.
     */
    public Alert(String text, AlertType type) {
        this("Alerts", text, type);
    }

    /**
     * Creates a new Alert. If this is the first to be instantiated in its group, the appropriate
     * entries will be added to NetworkTables.
     *
     * @param group Group identifier, also used as NetworkTables title
     * @param text Text to be displayed when the alert is active.
     * @param type Alert level specifying urgency.
     */
    public Alert(String group, String text, AlertType type) {
        if (!groups.containsKey(group)) {
            groups.put(group, new SendableAlerts());
        }

        this.text = text;
        this.type = type;
        groups.get(group).alerts.add(this);
    }

    /*
     * Sets whether the alert should currently be displayed. When activated, the alert text will also
     * be sent to the console.
     */
    public void set(boolean active) {
        if (active && !this.active) {
            activeStartTime = Timer.getFPGATimestamp();
            switch (type) {
                case ERROR:
                    DriverStation.reportError(text, false);
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.NotificationLevel.ERROR, "Alert", text));
                    break;
                case WARNING:
                    DriverStation.reportWarning(text, false);
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.NotificationLevel.WARNING, "Alert", text));
                    break;
                case INFO:
                    DriverStation.reportWarning(text, false);
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.NotificationLevel.INFO, "Alert", text));
                    break;
                default:
                    break;
            }
        }
        if (this.active != active) {
            this.active = active;
            markDirty();
        }
    }

    /** Marks all groups containing this alert as dirty so cached strings are rebuilt. */
    private void markDirty() {
        for (SendableAlerts group : groups.values()) {
            if (group.alerts.contains(this)) {
                group.dirty = true;
            }
        }
    }

    /* Updates current alert text. Resets activation time if the alert is currently active. */
    public void setText(String text) {
        if (active && !text.equals(this.text)) {
            activeStartTime = Timer.getFPGATimestamp();
            switch (type) {
                case ERROR:
                    DriverStation.reportError(text, false);
                    break;
                case WARNING:
                    DriverStation.reportWarning(text, false);
                    break;
                case INFO:
                    DriverStation.reportWarning(text, false);
                    break;
                default:
                    break;
            }
        }
        this.text = text;
    }

    /* Removes this alert from its group. After removal, calling set() has no visible effect. */
    public void remove() {
        for (SendableAlerts group : groups.values()) {
            group.alerts.remove(this);
        }
    }

    /** Logs all alert string arrays per group via AK Logger. */
    public static void outputTelemetry() {
        for (Map.Entry<String, SendableAlerts> entry : groups.entrySet()) {
            String group = entry.getKey();
            SendableAlerts alerts = entry.getValue();
            Logger.recordOutput(group + "/errors", alerts.getStrings(AlertType.ERROR));
            Logger.recordOutput(group + "/warnings", alerts.getStrings(AlertType.WARNING));
            Logger.recordOutput(group + "/infos", alerts.getStrings(AlertType.INFO));
        }
    }

    private static class SendableAlerts implements Sendable {
        public final List<Alert> alerts = new ArrayList<>();

        // Dirty flag + cached arrays — avoids stream/sort/toArray allocation every telemetry cycle.
        // Only recomputed when an alert's active state changes.
        boolean dirty = true;
        private String[] cachedErrors = new String[0];
        private String[] cachedWarnings = new String[0];
        private String[] cachedInfos = new String[0];

        public String[] getStrings(AlertType type) {
            if (dirty) {
                rebuildCache();
            }
            switch (type) {
                case ERROR:
                    return cachedErrors;
                case WARNING:
                    return cachedWarnings;
                case INFO:
                    return cachedInfos;
                default:
                    return new String[0];
            }
        }

        private void rebuildCache() {
            Comparator<Alert> timeSorter =
                    (Alert a1, Alert a2) -> Double.compare(a2.activeStartTime, a1.activeStartTime);
            cachedErrors = buildArray(AlertType.ERROR, timeSorter);
            cachedWarnings = buildArray(AlertType.WARNING, timeSorter);
            cachedInfos = buildArray(AlertType.INFO, timeSorter);
            dirty = false;
        }

        private String[] buildArray(AlertType type, Comparator<Alert> sorter) {
            List<Alert> filtered = new ArrayList<>();
            for (Alert a : alerts) {
                if (a.type == type && a.active) {
                    filtered.add(a);
                }
            }
            filtered.sort(sorter);
            String[] result = new String[filtered.size()];
            for (int i = 0; i < filtered.size(); i++) {
                result[i] = filtered.get(i).text;
            }
            return result;
        }

        @Override
        public void initSendable(SendableBuilder builder) {
            builder.setSmartDashboardType("Alerts");
            builder.addStringArrayProperty("errors", () -> getStrings(AlertType.ERROR), null);
            builder.addStringArrayProperty("warnings", () -> getStrings(AlertType.WARNING), null);
            builder.addStringArrayProperty("infos", () -> getStrings(AlertType.INFO), null);
        }
    }

    /* Represents an alert's level of urgency. */
    public enum AlertType {
        /*
         * High priority alert - displayed first on the dashboard with a red "X" symbol. Use this type
         * for problems which will seriously affect the robot's functionality and thus require immediate
         * attention.
         */
        ERROR,

        /*
         * Medium priority alert - displayed second on the dashboard with a yellow "!" symbol. Use this
         * type for problems which could affect the robot's functionality but do not necessarily require
         * immediate attention.
         */
        WARNING,

        /*
         * Low priority alert - displayed last on the dashboard with a green "i" symbol. Use this type
         * for problems which are unlikely to affect the robot's functionality, or any other alerts
         * which do not fall under "ERROR" or "WARNING".
         */
        INFO
    }
}
