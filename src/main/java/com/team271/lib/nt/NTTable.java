package com.team271.lib.nt;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Topic;

public class NTTable {
    /*
     *
     * Variables
     *
     */
    protected final String tableName;
    protected final NetworkTableInstance inst;
    protected final NetworkTable table;

    protected final NTTable parent;
    protected final String path;

    protected boolean enabled = false;

    /*
     *
     * Constructors
     *
     */
    public NTTable(final NTTable argParent, final String argName) {
        parent = argParent;
        tableName = argName;

        path = buildPath();

        inst = NetworkTableInstance.getDefault();

        if (inst != null) {
            table = inst.getTable(path);
        } else {
            table = null;
        }
    }

    public NTTable(final String argName) {
        this(null, argName);
    }

    /*
     *
     * Getters
     *
     */
    public final String getTableName() {
        return tableName;
    }

    public final String getPath() {
        return path;
    }

    public final String buildPath() {
        if (parent != null) {
            return parent.getPath() + "/" + tableName;
        }

        return "/" + tableName;
    }

    public final Topic getTopic(final String argTopicName) {
        if (table == null) {
            return null;
        }
        return table.getTopic(argTopicName);
    }

    public void enableTelemetry() {
        enabled = true;
    }

    public void disableTelemetry() {
        enabled = false;
    }

    public void publish() {}
}
