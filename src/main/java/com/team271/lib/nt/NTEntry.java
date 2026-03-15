package com.team271.lib.nt;

import static com.team271.lib.ConstantsLib.*;

import com.team271.lib.util.Util;
import edu.wpi.first.networktables.GenericPublisher;
import edu.wpi.first.networktables.GenericSubscriber;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.Topic;

public class NTEntry {
    protected final NTTable table;

    protected final Topic topic;

    protected GenericSubscriber sub = null;
    protected GenericPublisher pub = null;

    boolean valueBool = false;
    double valueDbl = 0.0;
    long valueLong = 0;
    long valueInt = 0;
    String valueStr = "";

    public NTEntry(final NTTable argTable, final String argTopicName) {
        table = argTable;

        if (table != null) {
            topic = table.getTopic(argTopicName);
        } else {
            topic = null;
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
            pub =
                    topic.genericPublish(
                            "boolean",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));

            pub.setDefaultBoolean(argDefaultValue);
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
            pub =
                    topic.genericPublish(
                            "double",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));

            pub.setDefaultDouble(argDefaultValue);
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
            pub =
                    topic.genericPublish(
                            "int",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));

            pub.setDefaultInteger(argDefaultValue);
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
            pub =
                    topic.genericPublish(
                            "int",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));

            pub.setDefaultInteger(argDefaultValue);
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
            pub =
                    topic.genericPublish(
                            "string",
                            PubSubOption.keepDuplicates(false),
                            PubSubOption.periodic(argRate),
                            PubSubOption.pollStorage(20),
                            PubSubOption.sendAll(false));

            pub.setDefaultString(argDefaultValue);
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

    /*
     * Publish
     */
    public void publish(final boolean argData) {
        if ((pub != null) && (valueBool != argData)) {
            valueBool = argData;
            pub.setBoolean(valueBool, 0);
        }
    }

    public void publish(final double argData) {
        if ((pub != null) && (!Util.epsilonEquals(valueDbl, argData))) {
            valueDbl = argData;
            pub.setDouble(valueDbl, 0);
        }
    }

    public void publish(final long argData) {
        if ((pub != null) && (valueLong != argData)) {
            valueLong = argData;
            pub.setInteger(valueLong, 0);
        }
    }

    public void publish(final int argData) {
        if ((pub != null) && (valueInt != argData)) {
            valueInt = argData;
            pub.setInteger(valueInt, 0);
        }
    }

    public void publish(final String argData) {
        if ((pub != null) && !valueStr.equals(argData)) {
            valueStr = argData;
            pub.setString(valueStr, 0);
        }
    }
}
