package com.team271.lib.nt;

import edu.wpi.first.networktables.GenericPublisher;
import edu.wpi.first.networktables.GenericSubscriber;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.Topic;
import org.littletonrobotics.junction.Logger;

/**
 * AK-aware tuning input that reads values from NetworkTables (for live dashboard tuning) and
 * records every read to AdvantageKit Logger (for replay fidelity).
 *
 * <p>Unlike {@link NTEntry} which is output-only, this class maintains both a NT publisher (to set
 * default values so the dashboard shows the field) and a NT subscriber (to read operator changes).
 *
 * <p>Usage:
 *
 * <pre>
 *   LoggedNTInput tuneP = new LoggedNTInput(table, "Tune P", 1.0);
 *   // In periodic:
 *   if (tuneP.hasChanged()) {
 *       setPGain(tuneP.getDbl());
 *   }
 * </pre>
 */
public class LoggedNTInput {
    private final String logPath;
    private final GenericSubscriber sub;
    private final GenericPublisher pub;

    private double lastDbl = 0.0;
    private boolean lastBool = false;
    private long lastLong = 0;
    private String lastStr = "";

    // Change-detection: skip Logger.recordOutput when the value is unchanged since the last
    // read. `firstLog` per type ensures the default / startup value is recorded once so replays
    // have a concrete value from the start (not "unknown until the driver edits").
    private boolean dblFirstLog = true;
    private boolean boolFirstLog = true;
    private boolean longFirstLog = true;
    private boolean strFirstLog = true;

    /**
     * Build the AdvantageKit log key for this input. NTTable.getPath() returns "/<name>", but
     * Logger.recordOutput prepends "RealOutputs/" — so a leading slash produces "RealOutputs//..."
     * which appears under an empty-named folder in AdvantageScope. Strip the leading slash so
     * LoggedNTInput entries land in the same RealOutputs namespace as direct recordOutput calls.
     */
    private static String akLogKey(final NTTable table, final String name) {
        final String tablePath = table.getPath();
        final String cleanTablePath =
                tablePath.startsWith("/") ? tablePath.substring(1) : tablePath;
        return cleanTablePath + "/" + name;
    }

    /* Double */
    public LoggedNTInput(final NTTable table, final String name, final double defaultValue) {
        lastDbl = defaultValue;
        if (table != null) {
            logPath = akLogKey(table, name);
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "double",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("double", PubSubOption.keepDuplicates(false));
            pub.setDefaultDouble(defaultValue);
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /* Boolean */
    public LoggedNTInput(final NTTable table, final String name, final boolean defaultValue) {
        lastBool = defaultValue;
        if (table != null) {
            logPath = akLogKey(table, name);
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "boolean",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("boolean", PubSubOption.keepDuplicates(false));
            pub.setDefaultBoolean(defaultValue);
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /* Long */
    public LoggedNTInput(final NTTable table, final String name, final long defaultValue) {
        lastLong = defaultValue;
        if (table != null) {
            logPath = akLogKey(table, name);
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "int",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("int", PubSubOption.keepDuplicates(false));
            pub.setDefaultInteger(defaultValue);
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /* String */
    public LoggedNTInput(final NTTable table, final String name, final String defaultValue) {
        lastStr = defaultValue;
        if (table != null) {
            logPath = akLogKey(table, name);
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "string",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("string", PubSubOption.keepDuplicates(false));
            pub.setDefaultString(defaultValue);
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /*
     * Getters — read from NT subscriber + record to AK Logger
     */
    public double getDbl() {
        double val = (sub != null) ? sub.getDouble(lastDbl) : lastDbl;
        if (logPath != null && (dblFirstLog || val != lastDbl)) {
            Logger.recordOutput(logPath, val);
            dblFirstLog = false;
        }
        lastDbl = val;
        return val;
    }

    public boolean getBool() {
        boolean val = (sub != null) ? sub.getBoolean(lastBool) : lastBool;
        if (logPath != null && (boolFirstLog || val != lastBool)) {
            Logger.recordOutput(logPath, val);
            boolFirstLog = false;
        }
        lastBool = val;
        return val;
    }

    public long getLong() {
        long val = (sub != null) ? sub.getInteger(lastLong) : lastLong;
        if (logPath != null && (longFirstLog || val != lastLong)) {
            Logger.recordOutput(logPath, val);
            longFirstLog = false;
        }
        lastLong = val;
        return val;
    }

    public String getString() {
        String val = (sub != null) ? sub.getString(lastStr) : lastStr;
        if (logPath != null && (strFirstLog || !java.util.Objects.equals(val, lastStr))) {
            Logger.recordOutput(logPath, val);
            strFirstLog = false;
        }
        lastStr = val;
        return val;
    }

    /*
     * Setters — write back to NT publisher so dashboard stays in sync when code mutates the value
     * (e.g. operator-controller gain bumps). Also updates the cached last* field so getX() returns
     * the new value immediately without a round-trip through the subscriber.
     */
    public void setDbl(final double val) {
        if (pub != null) {
            pub.setDouble(val);
        }
        lastDbl = val;
    }

    public void setBool(final boolean val) {
        if (pub != null) {
            pub.setBoolean(val);
        }
        lastBool = val;
    }

    public void setLong(final long val) {
        if (pub != null) {
            pub.setInteger(val);
        }
        lastLong = val;
    }

    public void setString(final String val) {
        if (pub != null) {
            pub.setString(val);
        }
        lastStr = val;
    }

    /**
     * Check if the dashboard operator changed the double value since the last {@link #getDbl()}
     * call.
     */
    public boolean hasChanged() {
        if (sub == null) {
            return false;
        }
        double current = sub.getDouble(lastDbl);
        return current != lastDbl;
    }

    /**
     * Check if the dashboard operator changed the boolean value since the last {@link #getBool()}
     * call.
     */
    public boolean hasBoolChanged() {
        if (sub == null) {
            return false;
        }
        boolean current = sub.getBoolean(lastBool);
        return current != lastBool;
    }

    /**
     * Check if the dashboard operator changed the long value since the last {@link #getLong()}
     * call.
     */
    public boolean hasLongChanged() {
        if (sub == null) {
            return false;
        }
        long current = sub.getInteger(lastLong);
        return current != lastLong;
    }

    /**
     * Check if the dashboard operator changed the string value since the last {@link #getString()}
     * call.
     */
    public boolean hasStringChanged() {
        if (sub == null) {
            return false;
        }
        String current = sub.getString(lastStr);
        return !current.equals(lastStr);
    }
}
