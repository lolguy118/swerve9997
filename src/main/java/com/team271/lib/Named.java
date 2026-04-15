package com.team271.lib;

import com.team271.lib.nt.NTTable;

/**
 * Identity and NetworkTables hierarchy interface.
 *
 * <p>Provides the name and NT table that define an object's place in the telemetry hierarchy.
 * Classes that need NT identity without extending TObj can implement this interface directly.
 */
public interface Named {
    /** Returns the object's display name. */
    String getName();

    /** Returns the object's NT table for telemetry namespacing. */
    NTTable getTable();

    /** Builds an AdvantageKit log key from this object's NT path. */
    default String logKey(final String suffix) {
        return getTable().getPath() + "/" + suffix;
    }
}
