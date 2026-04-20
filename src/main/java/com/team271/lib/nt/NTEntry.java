package com.team271.lib.nt;

import static com.team271.lib.ConstantsLib.*;

import edu.wpi.first.networktables.GenericSubscriber;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.Topic;
import java.util.Objects;
import org.littletonrobotics.junction.Logger;

public class NTEntry {
    protected final NTTable table;

    protected final Topic topic;

    /** AK log key built from table path + topic name. Null when table is null. */
    private final String logPath;

    protected GenericSubscriber sub = null;

    public NTEntry(final NTTable argTable, final String argTopicName) {
        table = argTable;

        if (table != null) {
            topic = table.getTopic(argTopicName);
            // NTTable.getPath() returns "/<name>" — strip the leading slash so AK keys
            // match direct Logger.recordOutput("Table/Field", ...) calls. Without this,
            // AK would store at "RealOutputs//Table/Field" (double slash) and the data
            // would appear under an empty-named folder separate from direct-logged data.
            final String tablePath = table.getPath();
            final String cleanTablePath =
                    tablePath.startsWith("/") ? tablePath.substring(1) : tablePath;
            logPath = cleanTablePath + "/" + argTopicName;
        } else {
            topic = null;
            logPath = null;
        }
    }

    /* Boolean */
    public NTEntry(
            final NTTable argTable,
            final String argTopicName,
            final boolean argDefaultValue,
            final double argRate) {
        this(argTable, argTopicName);

        setup(argDefaultValue, argRate);
    }

    public NTEntry(
            final NTTable argTable, final String argTopicName, final boolean argDefaultValue) {
        this(argTable, argTopicName, argDefaultValue, NT_UPDATE_MS);
    }

    /* double */
    public NTEntry(
            final NTTable argTable,
            final String argTopicName,
            final double argDefaultValue,
            final double argRate) {
        this(argTable, argTopicName);

        setup(argDefaultValue, argRate);
    }

    public NTEntry(
            final NTTable argTable, final String argTopicName, final double argDefaultValue) {
        this(argTable, argTopicName, argDefaultValue, NT_UPDATE_MS);
    }

    /* long */
    public NTEntry(
            final NTTable argTable,
            final String argTopicName,
            final long argDefaultValue,
            final double argRate) {
        this(argTable, argTopicName);

        setup(argDefaultValue, argRate);
    }

    public NTEntry(final NTTable argTable, final String argTopicName, final long argDefaultValue) {
        this(argTable, argTopicName, argDefaultValue, NT_UPDATE_MS);
    }

    /* string */
    public NTEntry(
            final NTTable argTable,
            final String argTopicName,
            final String argDefaultValue,
            final double argRate) {
        this(argTable, argTopicName);

        setup(argDefaultValue, argRate);
    }

    public NTEntry(
            final NTTable argTable, final String argTopicName, final String argDefaultValue) {
        this(argTable, argTopicName, argDefaultValue, NT_UPDATE_MS);
    }

    /*
     * Setup Boolean
     */
    public void setup(final boolean argDefaultValue, final double argRate) {
        if (topic != null) {
            sub =
                    topic.genericSubscribe(
                            "boolean",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));
        }
    }

    public void setup(final boolean argDefaultValue) {
        setup(argDefaultValue, NT_UPDATE_MS);
    }

    /*
     * Setup Double
     */
    public void setup(final double argDefaultValue, final double argRate) {
        if (topic != null) {
            sub =
                    topic.genericSubscribe(
                            "double",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));
        }
    }

    public void setup(final double argDefaultValue) {
        setup(argDefaultValue, NT_UPDATE_MS);
    }

    /*
     * Setup Long
     */
    public void setup(final long argDefaultValue, final double argRate) {
        if (topic != null) {
            sub =
                    topic.genericSubscribe(
                            "int",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));
        }
    }

    public void setup(final long argDefaultValue) {
        setup(argDefaultValue, NT_UPDATE_MS);
    }

    /*
     * Setup Int
     */
    public void setup(final int argDefaultValue, final double argRate) {
        if (topic != null) {
            sub =
                    topic.genericSubscribe(
                            "int",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));
        }
    }

    public void setup(final int argDefaultValue) {
        setup(argDefaultValue, NT_UPDATE_MS);
    }

    /*
     * Setup String
     */
    public void setup(final String argDefaultValue, final double argRate) {
        if (topic != null) {
            sub =
                    topic.genericSubscribe(
                            "string",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));
        }
    }

    public void setup(final String argDefaultValue) {
        setup(argDefaultValue, NT_UPDATE_MS);
    }

    /*
     * Getters
     */
    public final boolean getBool() {
        return sub != null ? sub.getBoolean(false) : false;
    }

    public final double getDbl() {
        return sub != null ? sub.getDouble(0.0) : 0.0;
    }

    public final long getLong() {
        return sub != null ? sub.getInteger(0) : 0;
    }

    public final int getInt() {
        return sub != null ? (int) sub.getInteger(0) : 0;
    }

    public final String getString() {
        return sub != null ? sub.getString("") : "";
    }

    /*
     * Publish
     *
     * Change-detection: publish() only calls Logger.recordOutput when the value differs from
     * the last published value for that type. First publish always goes through (null sentinel).
     * AK's WPILOG format is append-on-change anyway — skipping the recordOutput call avoids the
     * LogTable.put() + allocation cost, which dominates when a subsystem has hundreds of NTEntry
     * fields (static config, controller state, fault booleans, etc.) that rarely change.
     */
    private Boolean lastBool = null;
    private Double lastDouble = null;
    private Long lastLong = null;
    private String lastString = null;

    public void publish(final boolean argData) {
        if (logPath != null && (lastBool == null || lastBool.booleanValue() != argData)) {
            lastBool = argData;
            Logger.recordOutput(logPath, argData);
        }
    }

    public void publish(final double argData) {
        if (logPath != null && (lastDouble == null || lastDouble.doubleValue() != argData)) {
            lastDouble = argData;
            Logger.recordOutput(logPath, argData);
        }
    }

    public void publish(final long argData) {
        if (logPath != null && (lastLong == null || lastLong.longValue() != argData)) {
            lastLong = argData;
            Logger.recordOutput(logPath, argData);
        }
    }

    public void publish(final int argData) {
        publish((long) argData);
    }

    public void publish(final String argData) {
        if (logPath != null && !Objects.equals(argData, lastString)) {
            lastString = argData;
            Logger.recordOutput(logPath, argData);
        }
    }
}
