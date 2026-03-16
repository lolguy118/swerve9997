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

    /* Double */
    public LoggedNTInput(final NTTable table, final String name, final double defaultValue) {
        if (table != null) {
            logPath = table.getPath() + "/" + name;
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "double",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("double", PubSubOption.keepDuplicates(false));
            pub.setDefaultDouble(defaultValue);
            lastDbl = defaultValue;
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /* Boolean */
    public LoggedNTInput(final NTTable table, final String name, final boolean defaultValue) {
        if (table != null) {
            logPath = table.getPath() + "/" + name;
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "boolean",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("boolean", PubSubOption.keepDuplicates(false));
            pub.setDefaultBoolean(defaultValue);
            lastBool = defaultValue;
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /* Long */
    public LoggedNTInput(final NTTable table, final String name, final long defaultValue) {
        if (table != null) {
            logPath = table.getPath() + "/" + name;
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "int",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("int", PubSubOption.keepDuplicates(false));
            pub.setDefaultInteger(defaultValue);
            lastLong = defaultValue;
        } else {
            logPath = null;
            sub = null;
            pub = null;
        }
    }

    /* String */
    public LoggedNTInput(final NTTable table, final String name, final String defaultValue) {
        if (table != null) {
            logPath = table.getPath() + "/" + name;
            Topic topic = table.getTopic(name);
            sub =
                    topic.genericSubscribe(
                            "string",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.pollStorage(20));
            pub = topic.genericPublish("string", PubSubOption.keepDuplicates(false));
            pub.setDefaultString(defaultValue);
            lastStr = defaultValue;
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
        if (logPath != null) {
            Logger.recordOutput(logPath, val);
        }
        lastDbl = val;
        return val;
    }

    public boolean getBool() {
        boolean val = (sub != null) ? sub.getBoolean(lastBool) : lastBool;
        if (logPath != null) {
            Logger.recordOutput(logPath, val);
        }
        lastBool = val;
        return val;
    }

    public long getLong() {
        long val = (sub != null) ? sub.getInteger(lastLong) : lastLong;
        if (logPath != null) {
            Logger.recordOutput(logPath, val);
        }
        lastLong = val;
        return val;
    }

    public String getString() {
        String val = (sub != null) ? sub.getString(lastStr) : lastStr;
        if (logPath != null) {
            Logger.recordOutput(logPath, val);
        }
        lastStr = val;
        return val;
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
